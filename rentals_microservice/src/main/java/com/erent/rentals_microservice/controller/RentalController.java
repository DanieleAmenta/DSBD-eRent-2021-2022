package com.erent.rentals_microservice.controller;

import com.erent.rentals_microservice.data.RentalRepository;
import com.erent.rentals_microservice.entities.*;
import com.erent.rentals_microservice.service.RentalService;
import com.google.gson.Gson;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Controller
@RequestMapping(path = "/${api_base}")
public class RentalController {

    @Autowired
    RentalRepository repository;

    /*@Autowired
    MetricsUtil metricsUtil;*/

    @Autowired
    private RentalService service;

    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka_rental_topic}")
    private String rental_topic;

    @Value("${kafka_scooter_requests_topic}")
    private String scooter_requests_topic;

    @Value("${kafka_rental_accepted_key}")
    private String rental_accepted_key;

    @Value("${kafka_rental_completed_key}")
    private String rental_completed_key;

    @Value("${price_per_minute}")
    private Double price_per_minute;

    @Value("${price_per_start}")
    private Double price_per_start;

    @Value("${admin_user_id}")
    private String admin_user_id;

    private final AtomicLong rentalFailureCounter = new AtomicLong();

    private final MeterRegistry registry;

    public RentalController(MeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping(path = "/ping")
    public @ResponseBody
    String getPong() {
        return "Pong";
    }

    public ScooterResponse sendScooterRequest(ScooterRequest scooter) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(scooter_requests_topic, "scooter_request", new Gson().toJson(scooter));
        RequestReplyFuture<String, String, String> future = replyingKafkaTemplate.sendAndReceive(record);
        ConsumerRecord<String, String> response = future.get();
        return new Gson().fromJson(response.value(), ScooterResponse.class);
    }

    @GetMapping(path = "/{id}")
    public @ResponseBody
    String getRentalById(@PathVariable String id,
                         @RequestHeader("X-User-ID") String user_id,
                         HttpServletRequest servletRequest) {

        Optional<Rental> rental = repository.findById(id);
        if (!rental.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (!rental.get().getUserId().equals(admin_user_id) && !rental.get().getUserId().equals(user_id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        return new Gson().toJson(rental.get());
    }

    @Timed(value = "started.rental", description = "Time spent to start rentals")
    @PostMapping(path = "/start")
    public @ResponseBody
    String startRental(@RequestParam String scooterId,
                       @RequestParam String code,
                       @RequestParam Double lat,
                       @RequestParam Double lon,
                       @RequestHeader("X-User-ID") String user_id,
                       HttpServletRequest servletRequest) {

        ScooterRequest scooter = new ScooterRequest();
        scooter.setLat(lat);
        scooter.setLon(lon);
        scooter.setScooter_id(scooterId);
        scooter.setScooterCode(code);
        scooter.setOperation(ScooterOperation.UNLOCK);

        ScooterResponse response;
        try {
            response = sendScooterRequest(scooter);
        } catch (Exception e) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Service communication error."
            );
        }

        if (!response.getSuccess()) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, response.getMessage()
            );
        }

        Rental rental = new Rental();
        rental.setScooterId(response.getScooter_id());
        rental.setUserId(user_id);
        rental.setStartTimestamp(Instant.now().getEpochSecond());

        rental.setStatus(RentalStatus.STARTED);

        rental.setAmount_to_pay(price_per_start);
        rental.setPrice_per_minute(price_per_minute);
        rental.setPrice_per_start(price_per_start);

        rental.setInvoiceGenerated(false);
        rental.setInvoiceOpened(false);

        repository.save(rental);

        kafkaTemplate.send(rental_topic, rental_accepted_key, new Gson().toJson(rental));

        return new Gson().toJson(rental);
    }

    @Timed(value = "finished.rental", description = "Time spent to finish rentals")
    @PostMapping(path = "/stop")
    public @ResponseBody
    String stopRental(@RequestParam String rental_id,
                      @RequestParam String code,
                      @RequestParam Double lat,
                      @RequestParam Double lon,
                      @RequestHeader("X-User-ID") String user_id,
                      HttpServletRequest servletRequest) {

        Optional<Rental> rental = repository.findById(rental_id);
        if (!rental.isPresent()) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Specified object can't be found."
            );
        }

        if (!rental.get().getUserId().equals(user_id) && rental.get().getUserId().equals(admin_user_id)) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't own this resource."
            );
        }

        if (rental.get().getStatus() == RentalStatus.COMPLETED) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Invalid operation."
            );
        }

        ScooterRequest scooterRequest = new ScooterRequest();
        scooterRequest.setScooterCode(code);
        scooterRequest.setLat(lat);
        scooterRequest.setLon(lon);
        scooterRequest.setScooter_id(rental.get().getScooterId());
        scooterRequest.setOperation(ScooterOperation.LOCK);

        ScooterResponse response;
        try {
            response = sendScooterRequest(scooterRequest);
        } catch (Exception e) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Service communication error."
            );
        }

        if (!response.getSuccess()) {
            rentalFailureCounter.incrementAndGet();
            //metricsUtil.incrementCounters("failure");

            // FREEZE THE TIMER
            rental.get().setStatus(RentalStatus.FROZEN);
            rental.get().setStopTimestamp(Instant.now().getEpochSecond());
            repository.save(rental.get());

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Rental issue. Contact the support."
            );
        }

        Rental newRental = rental.get();
        if (newRental.getStatus() == RentalStatus.STARTED) {
            newRental.setStopTimestamp(Instant.now().getEpochSecond());
        }
        newRental.setStatus(RentalStatus.COMPLETED);
        long diff = newRental.getStopTimestamp() - newRental.getStartTimestamp();

        // Convert diff to hours, multiply for price for minute and get only two decimal numbers
        Double to_pay = newRental.getAmount_to_pay() + Math.round(diff / 60d * newRental.getPrice_per_minute() * 100) / 100d;
        newRental.setAmount_to_pay(to_pay);

        repository.save(newRental);

        kafkaTemplate.send(rental_topic, rental_completed_key, new Gson().toJson(newRental));

        return new Gson().toJson(newRental);
    }

    @GetMapping(path = "/rentals")
    public @ResponseBody
    String getAllRentals(@RequestHeader("X-User-ID") String user_id,
                         HttpServletRequest request) {

        List<Rental> rentalsList;

        if (user_id.equals(admin_user_id)) {
            rentalsList = repository.findAll();
        } else {
            rentalsList = repository.findByUserIdOrderByStartTimestamp(user_id);
        }

        return new Gson().toJson(rentalsList);
    }
}
