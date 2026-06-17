package br.com.insurtech.policybilling.application.port.in;

import java.time.LocalDate;

public interface ProcessDailyBillingUseCase {

    void execute(LocalDate currentDate);
}
