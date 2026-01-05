package com.aston.usermicroservice.service;

import com.aston.core.UserCreatedDeletedEvent;
import com.aston.usermicroservice.model.User;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test_kafka")
@DirtiesContext
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
class UserProducerImplIntegrationTest {

    // Аннотация @EmbeddedKafka используется для внедрения экземпляра EmbeddedKafkaBroker в наши тесты.
    // count = 3 - три брокера
    // controlledShutdown = true - мягкое завершение работы брокера (после завершения всех процессов)
    // TestInstance.Lifecycle.PER_CLASS - будет создан один объект тестового класса и будет переиспользоваться в каждом
    // методе

    @Autowired
    private UserProducerImpl userProducer;

    @Autowired
    private Environment environment;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaMessageListenerContainer<String, UserCreatedDeletedEvent> container;
    // Является прослойкой между очередью сообщений и consumer'ом.

    private BlockingQueue<ConsumerRecord<String, UserCreatedDeletedEvent>> records;


    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("user-created-deleted-events-topic-name"));

        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, UserCreatedDeletedEvent>) records::add);
        // Все сообщения, поступающие в контейнер, необходимо добавлять в очередь records
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }


    @Test
    void testCreatedUser_whenGivenValidUserDetails_successfullySendsKafkaMessage() throws InterruptedException {

        User user = new User();
        user.setId(1L);
        user.setName("testName1");
        user.setEmail("test1@gmail.com");
        user.setAge(28);
        user.setCreated_at(LocalDateTime.now());

        userProducer.sendEventCreatedUser(user);

        ConsumerRecord<String, UserCreatedDeletedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
        // В течение 3 секунд ждем отправленного producer'ом сообщения
        Assertions.assertNotNull(message);
        Assertions.assertNotNull(message.key());
        UserCreatedDeletedEvent event = message.value();
        Assertions.assertEquals("UserCreated", event.getType());
        Assertions.assertEquals(user.getName(), event.getName());
        Assertions.assertEquals(user.getEmail(), event.getEmail());
    }

    @Test
    void sendEventDeletedUser() throws InterruptedException {

        User user = new User();
        user.setId(1L);
        user.setName("testName1");
        user.setEmail("test1@gmail.com");
        user.setAge(28);
        user.setCreated_at(LocalDateTime.now());

        userProducer.sendEventDeletedUser(user);

        ConsumerRecord<String, UserCreatedDeletedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
        // В течение 3 секунд ждем отправленного producer'ом сообщения
        Assertions.assertNotNull(message);
        Assertions.assertNotNull(message.key());
        UserCreatedDeletedEvent event = message.value();
        Assertions.assertEquals("UserDeleted", event.getType());
        Assertions.assertEquals(user.getName(), event.getName());
        Assertions.assertEquals(user.getEmail(), event.getEmail());
    }


    private Map<String, Object> getConsumerProperties() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG,
                                     Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.group-id")),
                JsonDeserializer.TRUSTED_PACKAGES,
                Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages")),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.auto-offset-reset"))
        );
    }


    @AfterAll
    void tearDown() {
        container.stop();
    }

}