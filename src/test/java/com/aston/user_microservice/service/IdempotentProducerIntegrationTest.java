package com.aston.user_microservice.service;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

@SpringBootTest
public class IdempotentProducerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private KafkaAdmin kafkaAdmin;
    // Выполняет функцию по созданию новых топиков. Но тест не связан с созданием топиков. Поэтому для ускорения
    // процесса выполнения теста мокаем KafkaAdmin.

    @Test
    void testProducerConfig_whenIdempotenceEnable_AssertsIdempotentProperties() {

        // Arrange
        ProducerFactory<String, Object> producerFactory = kafkaTemplate.getProducerFactory();

        // Actual
        Map<String, Object> config = producerFactory.getConfigurationProperties();

        // Assert
        Assertions.assertEquals("true", config.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        Assertions.assertEquals("5", config.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
        Assertions.assertTrue("all".equalsIgnoreCase((String) config.get(ProducerConfig.ACKS_CONFIG)));
        if(config.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            Assertions.assertTrue(
                    Integer.parseInt(config.get(ProducerConfig.RETRIES_CONFIG).toString()) > 0
            );
        }

    }
}
