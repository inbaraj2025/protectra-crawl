package com.protectra.sp.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;  // Kafka template for sending messages

    // Send message to Kafka topic
    public void sendMessage(String topic, String batchId) {
        // Produce the batch ID message to Kafka for further processing
        kafkaTemplate.send(topic, batchId);
        System.out.println("Message sent to Kafka topic '" + topic + "' with Batch ID: " + batchId);
    }

    // For more detailed metadata in Kafka, you can send a custom object instead of just the batchId
    public void sendMessageWithMetadata(String topic, String batchId, String assetInfo) {
        String message = "Batch ID: " + batchId + ", Asset Info: " + assetInfo;
        kafkaTemplate.send(topic, message);
        System.out.println("Message with metadata sent to Kafka topic '" + topic + "': " + message);
    }
}
