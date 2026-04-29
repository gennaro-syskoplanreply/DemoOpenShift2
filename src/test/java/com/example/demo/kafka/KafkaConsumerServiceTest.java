package com.example.demo.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.timeout;

import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"test-topic"},
    bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=",
    "KAFKA_TOPIC=test-topic",
    "KAFKA_GROUP_ID=test-group",
    "KAFKA_GROUP_ID_DEMOAPP2=test-group-demoapp2"
})
@DirtiesContext
class KafkaConsumerServiceTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SpyBean
    private KafkaConsumerService kafkaConsumerService;

    @Test
    void listen_shouldReceiveMessageFromTopic() throws Exception {
        String message = "Test message for consumer";

        kafkaTemplate.send("test-topic", message).get();

        verify(kafkaConsumerService, timeout(10_000)).listen(message);
    }
}
