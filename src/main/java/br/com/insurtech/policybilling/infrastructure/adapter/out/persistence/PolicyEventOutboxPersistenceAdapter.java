package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventOutboxPort;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PolicyEventOutboxPersistenceAdapter implements PolicyEventOutboxPort {

    public static final String POLICY_CANCELED_EVENT_TYPE = "PolicyCanceledEvent";

    private final SpringDataOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public PolicyEventOutboxPersistenceAdapter(
            SpringDataOutboxEventRepository outboxRepository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "outboxRepository must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
    }

    @Override
    public void appendPolicyCanceledEvent(PolicyCanceledEvent event) {
        Objects.requireNonNull(event, "event must not be null");

        OutboxEventEntity outboxEvent = new OutboxEventEntity(
                event.eventId(),
                event.policyId(),
                POLICY_CANCELED_EVENT_TYPE,
                serialize(event),
                event.canceledAt()
        );
        outboxRepository.save(outboxEvent);
        meterRegistry.counter("outbox.events", "result", "created").increment();
    }

    private String serialize(PolicyCanceledEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize policy canceled event", ex);
        }
    }
}
