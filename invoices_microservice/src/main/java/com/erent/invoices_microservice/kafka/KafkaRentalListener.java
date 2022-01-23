package com.erent.invoices_microservice.kafka;

import com.erent.invoices_microservice.entities.Invoice;
import com.erent.invoices_microservice.service.InvoiceService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

@Component
public class KafkaRentalListener {

    @Autowired
    private InvoiceService service;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka_invoice_topic}")
    private String invoice_topic;

    @Value("${kafka_rental_completed_key}")
    private String rental_completed_key;

    @Value("${kafka_invoice_created_key}")
    private String invoice_created_key;

    @KafkaListener(topics = "${kafka_rental_topic}", groupId = "${spring.application.name}")
    private void consumeRentalTopic(ConsumerRecord<String, String> data) {
        if (data.key().equals(rental_completed_key)) {
            try {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, Object> m = new Gson().fromJson(data.value(), type);

                String rental_id = m.get("id").toString();
                String user_id = m.get("userId").toString();
                String scooter_id = m.get("scooterId").toString();

                Long start_timestamp = Long.parseLong(m.get("startTimestamp").toString());
                Long end_timestamp = Long.parseLong(m.get("stopTimestamp").toString());
                Date start_d = new Date(start_timestamp * 1000);
                Date end_d = new Date(end_timestamp * 1000);

                Double total = Double.parseDouble(m.get("amount_to_pay").toString());
                Double price_per_minute = Double.parseDouble(m.get("price_per_minute").toString());
                Double price_per_start = Double.parseDouble(m.get("price_per_start").toString());

                Invoice i = new Invoice();
                i.setRentalId(rental_id);
                i.setScooterId(scooter_id);
                i.setUserId(user_id);

                i.setStart_timestamp(start_d);
                i.setEnd_timestamp(end_d);

                i.setTotal(total);
                i.setPrice_per_minute(price_per_minute);
                i.setPrice_per_start(price_per_start);

                i = service.CreateInvoice(i);

                if (i != null) {
                    kafkaTemplate.send(invoice_topic, invoice_created_key, new Gson().toJson(i));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
