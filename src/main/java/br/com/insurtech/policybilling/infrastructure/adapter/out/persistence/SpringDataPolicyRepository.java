package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataPolicyRepository extends JpaRepository<PolicyEntity, UUID> {

    List<PolicyEntity> findByDueDayAndStatus(int dueDay, String status);
}
