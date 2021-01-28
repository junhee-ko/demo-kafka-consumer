package me.jko.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL;

@Configuration
@EnableKafka
@Slf4j
public class ConsumerConfiguration {

  private static final String BOOTSTRAP_SERVERS = "localhost:9091";
  private static final String GROUP_ID = "jko-group-id";

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConcurrency(1);
    factory.getContainerProperties().setAckMode(MANUAL);
    factory.setConsumerFactory(consumerFactory());
    factory.setRetryTemplate(retryTemplate());
    factory.setRecoveryCallback(context -> {
      log.info("-------------------------retry finished");
      return null;
    });

    return factory;
  }

  private ConsumerFactory<String, String> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(consumerConfigs());
  }

  private Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    return props;
  }

  private RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setRetryPolicy(getSimpleRetryPolicy());
    retryTemplate.setBackOffPolicy(getFixedBackOffPolicy());

    return retryTemplate;
  }

  private SimpleRetryPolicy getSimpleRetryPolicy() {
    SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
    simpleRetryPolicy.setMaxAttempts(2);

    return simpleRetryPolicy;
  }

  private FixedBackOffPolicy getFixedBackOffPolicy() {
    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(2000);

    return fixedBackOffPolicy;
  }
}
