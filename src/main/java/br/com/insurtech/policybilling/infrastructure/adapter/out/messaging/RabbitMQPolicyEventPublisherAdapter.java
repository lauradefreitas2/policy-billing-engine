package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class RabbitMQPolicyEventPublisherAdapter implements PolicyEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPolicyEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPolicyEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = Objects.requireNonNull(rabbitTemplate, "rabbitTemplate must not be null");
    }

    @Override
    public void publishPolicyCanceledEvent(PolicyCanceledEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        CorrelationData correlationData = new CorrelationData(event.eventId().toString());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(event.eventId().toString());
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
        );

        try {
            CorrelationData.Confirm confirm = correlationData.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ rejected event: " + confirm.getReason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("RabbitMQ did not route event " + event.eventId());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while awaiting RabbitMQ confirmation", ex);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException ex) {
            throw new IllegalStateException("RabbitMQ did not confirm event " + event.eventId(), ex);
        }

        log.info("RabbitMQ confirmed policy canceled event {} for policy {}", event.eventId(), event.policyId());
    }
}
