package br.com.insurtech.policybilling.application.port.in;

import java.time.LocalDate;

public interface SuspendOverduePoliciesUseCase {

    void execute(LocalDate currentDate);
}
