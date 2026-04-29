package com.example.demo.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
class KafkaProducerServiceTest {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void sendMessage_shouldPublishMessageToTopic() {
        String message = "Test message for producer";

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("producer-test-group", "true", embeddedKafkaBroker);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "test-topic");

        kafkaProducerService.sendMessage(message);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "test-topic");
        assertThat(record.value()).isEqualTo(message);

        consumer.close();
    }
}
