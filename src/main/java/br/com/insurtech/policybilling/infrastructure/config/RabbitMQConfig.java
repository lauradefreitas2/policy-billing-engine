package br.com.insurtech.policybilling.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String POLICY_EVENTS_EXCHANGE = "policy.events.exchange";
    public static final String POLICY_CANCELED_QUEUE = "policy.canceled.queue";
    public static final String POLICY_CANCELED_ROUTING_KEY = "policy.canceled.key";

    @Bean
    public DirectExchange policyEventsExchange() {
        return new DirectExchange(POLICY_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue policyCanceledQueue() {
        return new Queue(POLICY_CANCELED_QUEUE);
    }

    @Bean
    public Binding policyCanceledBinding(Queue policyCanceledQueue, DirectExchange policyEventsExchange) {
        return BindingBuilder
                .bind(policyCanceledQueue)
                .to(policyEventsExchange)
                .with(POLICY_CANCELED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
