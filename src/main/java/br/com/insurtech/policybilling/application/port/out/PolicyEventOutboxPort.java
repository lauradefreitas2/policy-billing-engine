package br.com.insurtech.policybilling.application.port.out;

public interface PolicyEventOutboxPort {

    void appendPolicyCanceledEvent(PolicyCanceledEvent event);
}
