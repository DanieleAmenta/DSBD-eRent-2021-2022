package com.erent.rentals_microservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka_rental_topic}")
    private String rental_topic_name;

    @Value("${kafka_invoice_topic}")
    private String invoice_topic_name;

    @Value("${kafka_scooter_requests_topic}")
    private String scooter_requests_topic;

    @Value("${kafka_scooter_responses_topic}")
    private String scooter_responses_topic;

    @Value("${kafka_replies_group}")
    private String scooter_replies_group;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, String> replyTemplate(ProducerFactory<String, String> pf,
                                                       ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(pf);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.setReplyTemplate(kafkaTemplate);
        return kafkaTemplate;
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(ProducerFactory<String, String> pf,
                                                                               ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        ConcurrentMessageListenerContainer<String, String> replyContainer = factory.createContainer(scooter_responses_topic);
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);
        replyContainer.getContainerProperties().setGroupId(scooter_replies_group);

        return new ReplyingKafkaTemplate<>(pf, replyContainer);
    }

    @Bean
    public NewTopic rentalTopic() {
//        return new NewTopic(rental_topic_name, 10, (short) 1);
        return TopicBuilder.name(rental_topic_name)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic invoiceTopic() {
//        return new NewTopic(invoice_topic_name, 10, (short) 1);
        return TopicBuilder.name(invoice_topic_name)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic scooter_requests_topic() {
//        return new NewTopic(scooter_requests_topic, 10, (short) 1);
        return TopicBuilder.name(scooter_requests_topic)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic kReplies() {
//        return new NewTopic(scooter_responses_topic, 10, (short) 1);
        return TopicBuilder.name(scooter_responses_topic)
                .partitions(10)
                .build();
    }
}
