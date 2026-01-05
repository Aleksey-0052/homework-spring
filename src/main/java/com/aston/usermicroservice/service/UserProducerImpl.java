package com.aston.usermicroservice.service;

import com.aston.core.UserCreatedDeletedEvent;
import com.aston.usermicroservice.exception.ErrorMessageException;
import com.aston.usermicroservice.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class UserProducerImpl implements UserProducer {

    private static final String CREATED_USER = "UserCreated";
    private static final String DELETED_USER = "UserDeleted";

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendEventCreatedUser(User user) {

        String userId = user.getId().toString();
        UserCreatedDeletedEvent event =
                new UserCreatedDeletedEvent(CREATED_USER, user.getName(), user.getEmail());
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "user-created-deleted-events-topic",
                userId,
                event
        );
        SendResult<String, Object> result;
        try {
            result = kafkaTemplate.send(record).get();
            // Если мы хотим заблокировать поток отправки и получить результат об отправленном сообщении, мы можем
            // вызвать get API объекта CompletableFuture. Поток будет ждать результата, но это замедлит работу
            // производителя.
        } catch (Exception e) {
            throw new ErrorMessageException(LocalDateTime.now(), "Failed to send message: " + e.getMessage());
        }

        log.info("Topic: {}", result.getRecordMetadata().topic());
        log.info("Partition: {}", result.getRecordMetadata().partition());
        log.info("Offset: {}", result.getRecordMetadata().offset());
        log.info("Timestamp: {}", result.getRecordMetadata().timestamp());
        log.info("SerializedKeySize: {}", result.getRecordMetadata().serializedKeySize());
        log.info("SerializedValueSize: {}", result.getRecordMetadata().serializedValueSize());

        log.info("Return created user: {}", user);
    }

    // SendResult — это value-object, который связывает отправленный ProducerRecord и RecordMetadata, возвращённые
    // брокером.
    // Через методы getProducerRecord() и getRecordMetadata() можно получить ключ, значение, партицию, offset и
    // timestamp записи.
    // Структура класса:
    // Поля: final ProducerRecord<K,V> producerRecord, final RecordMetadata recordMetadata.
    // Конструктор: SendResult(ProducerRecord<K,V>, RecordMetadata) вызывается Spring Kafka внутри операций отправки.
    // Методы: getProducerRecord(), getRecordMetadata(), стандартные equals/hashCode/toString.
    // Какие данные доступны:
    // Из ProducerRecord: topic, partition, key, value, headers, timestamp.
    // Из RecordMetadata: topic, partition, offset, timestamp, serialized key/value size, checksum.


    public void sendEventDeletedUser(User user) {

        String userId = String.valueOf(user.getId());
        UserCreatedDeletedEvent event = new UserCreatedDeletedEvent(DELETED_USER, user.getName(), user.getEmail());
        SendResult<String, Object> result;
        try {
            result = kafkaTemplate.send("user-created-deleted-events-topic", userId, event).get();
        } catch (Exception e) {
            throw new ErrorMessageException(LocalDateTime.now(), "Failed to send message: " + e.getMessage());
        }

        log.debug("Topic: {}", result.getRecordMetadata().topic());
        log.debug("Partition: {}", result.getRecordMetadata().partition());
        log.debug("Offset: {}", result.getRecordMetadata().offset());
        log.debug("Timestamp: {}", result.getRecordMetadata().timestamp());
        log.debug("SerializedKeySize: {}", result.getRecordMetadata().serializedKeySize());
        log.debug("SerializedValueSize: {}", result.getRecordMetadata().serializedValueSize());

        log.debug("Return deleted user: {}", user);
    }


}
