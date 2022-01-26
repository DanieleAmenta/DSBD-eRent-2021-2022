package com.erent.scooters_microservice.kafka;

import com.erent.scooters_microservice.service.ScooterService;
import com.google.gson.Gson;
import com.erent.shared_entities.scooter.ScooterOperation;
import com.erent.shared_entities.scooter.ScooterRequest;
import com.erent.shared_entities.scooter.ScooterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaScooterListener {

    @Autowired
    private ScooterService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.application.name}")
    private String service_name;

    @Value("${kafka_scooter_topic}")
    private String scooter_topic;

    @Value("${scooter_unlocked_key}")
    private String scooter_unlocked;

    @Value("${scooter_locked_key}")
    private String scooter_locked;

    private void SendKafkaResponse(String topic, String key, ScooterRequest req, ScooterResponse res) {
        Map<String, Object> message = new HashMap<>();
        message.put("scooter_id", req.getScooter_id());
        message.put("user_id", req.getUser_id());
        message.put("rental_id", req.getRental_id());
        message.put("timestamp", Instant.now().getEpochSecond());
        message.put("service", service_name);
        message.put("message", res.getMessage());
        message.put("success", res.getSuccess());

        kafkaTemplate.send(topic, key, new Gson().toJson(message));
    }

    @KafkaListener(topics = "scooter_requests", groupId = "scooter_replies_group")
    @SendTo("scooter_responses")
    public String scooterRequestsHandler(String data) {
        System.out.println("New request: " + data);
        ScooterRequest request = new Gson().fromJson(data, ScooterRequest.class);
        ScooterResponse response = service.SwitchScooterStatus(request);

        if (response.getSuccess()) {
            String key = request.getOperation() == ScooterOperation.LOCK ? scooter_locked : scooter_unlocked;

            SendKafkaResponse(scooter_topic, key, request, response);
        }

        return new Gson().toJson(response);
    }
}
