package br.com.insurtech.policybilling.infrastructure.transaction;

import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentCommand;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentUseCase;
import br.com.insurtech.policybilling.domain.model.Policy;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalConfirmPaymentUseCase implements ConfirmPaymentUseCase {

    private final ConfirmPaymentUseCase delegate;

    public TransactionalConfirmPaymentUseCase(ConfirmPaymentUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    @Override
    @Transactional
    public Policy execute(ConfirmPaymentCommand command) {
        return delegate.execute(command);
    }
}
