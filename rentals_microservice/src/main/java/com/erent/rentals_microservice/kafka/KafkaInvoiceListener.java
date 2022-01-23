package com.erent.rentals_microservice.kafka;

import com.erent.rentals_microservice.service.RentalService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.erent.shared_entities.rental.RentalRequest;
import com.erent.shared_entities.rental.RentalResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaInvoiceListener {

    @Autowired
    private RentalService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.application.name}")
    private String service_name;

    @Value("${kafka_invoice_created_key}")
    private String invoice_created_key;

    @Value("${kafka_invoice_opened_key}")
    private String invoice_opened_key;

    @Value("${kafka_update_invoice_failure_key}")
    private String update_invoice_failure;

    @Value("${kafka_open_invoice_failure_key}")
    private String open_invoice_failure;

    private void SendKafkaResponse(String topic, String key, RentalRequest req, RentalResponse res) {
        Map<String, Object> message = new HashMap<>();
        message.put("scooter_id", req.getScooterId());
        message.put("user_id", req.getUser_id());
        message.put("rental_id", req.getRental_id());
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("service", service_name);
        message.put("message", res.getMessage());
        message.put("success", res.getSuccess());

        kafkaTemplate.send(topic, key, new Gson().toJson(message));
    }

    @KafkaListener(topics = "${kafka_invoice_topic}", groupId = "${spring.application.name}")
    private void consumeInvoiceTopic(ConsumerRecord<String, String> data) {
        if (data.key().equals(invoice_created_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("rentalId").toString();
                String user_id = m.get("userId").toString();
                String scooter_id = m.get("scooterId").toString();

                RentalRequest request = new RentalRequest();
                request.setRental_id(rental_id);
                request.setUser_id(user_id);
                request.setScooterId(scooter_id);

                RentalResponse response = service.MarkInvoiceGenerated(request);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(data.key().equals(invoice_opened_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("rentalId").toString();
                String user_id = m.get("userId").toString();
                String scooter_id = m.get("scooterId").toString();

                RentalRequest request = new RentalRequest();
                request.setRental_id(rental_id);
                request.setUser_id(user_id);
                request.setScooterId(scooter_id);

                RentalResponse response = service.MarkInvoiceOpened(request);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
