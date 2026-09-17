package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQPolicyEventPublisherAdapter implements PolicyEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPolicyEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPolicyEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishPolicyCanceledEvent(PolicyCanceledEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY,
                event
        );
        log.info("Published policy canceled event for policy {}", event.policyId());
    }
}
