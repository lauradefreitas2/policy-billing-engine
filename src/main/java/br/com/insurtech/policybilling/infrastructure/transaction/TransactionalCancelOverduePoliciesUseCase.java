package br.com.insurtech.policybilling.infrastructure.transaction;

import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;

public class TransactionalCancelOverduePoliciesUseCase implements CancelOverduePoliciesUseCase {

    private final CancelOverduePoliciesUseCase delegate;

    public TransactionalCancelOverduePoliciesUseCase(CancelOverduePoliciesUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    @Transactional
    public void execute(LocalDate currentDate) {
        delegate.execute(currentDate);
    }
}
