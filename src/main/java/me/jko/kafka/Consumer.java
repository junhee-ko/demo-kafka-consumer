package me.jko.kafka;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Consumer {

  private static final String TOPIC = "jko-topic";

  @KafkaListener(id = "container-id", topics = TOPIC, containerFactory = "kafkaListenerContainerFactory")
  public void consume(String data, Acknowledgment acknowledgment) {
    log.info("Consumed Data : {}", data);

    acknowledgment.acknowledge();
  }
}
