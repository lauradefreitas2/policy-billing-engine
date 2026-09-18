package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from OutboxEventEntity event
            where event.status in :statuses
              and event.nextAttemptAt <= :now
            order by event.createdAt, event.id
            """)
    List<OutboxEventEntity> findReadyEvents(
            @Param("statuses") Collection<String> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );
}
