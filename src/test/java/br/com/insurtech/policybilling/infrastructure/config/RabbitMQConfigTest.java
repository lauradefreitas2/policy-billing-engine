package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    @DisplayName("should expose durable policy events exchange and cancellation queue")
    void shouldExposeDurablePolicyEventsExchangeAndCancellationQueue() {
        DirectExchange exchange = config.policyEventsExchange();
        Queue queue = config.policyCanceledQueue();

        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.POLICY_EVENTS_EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_QUEUE);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("should bind cancellation queue to policy events exchange with routing key")
    void shouldBindCancellationQueueToPolicyEventsExchangeWithRoutingKey() {
        DirectExchange exchange = config.policyEventsExchange();
        Queue queue = config.policyCanceledQueue();

        Binding binding = config.policyCanceledBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_QUEUE);
        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.POLICY_EVENTS_EXCHANGE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY);
    }

    @Test
    @DisplayName("should use Jackson JSON converter for readable event payloads")
    void shouldUseJacksonJsonConverterForReadableEventPayloads() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Jackson2JsonMessageConverter converter = config.jackson2JsonMessageConverter(objectMapper);
        PolicyCanceledEvent event = new PolicyCanceledEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-09-18T12:00:00Z")
        );

        Message message = converter.toMessage(event, new MessageProperties());

        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
        assertThat(new String(message.getBody(), StandardCharsets.UTF_8))
                .contains("\"canceledAt\":\"2026-09-18T12:00:00Z\"");
    }
}
