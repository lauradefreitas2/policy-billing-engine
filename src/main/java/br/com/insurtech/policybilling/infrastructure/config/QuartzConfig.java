package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.BillingJob;
import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.CancellationJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    public static final String DAILY_BILLING_CRON = "0 0 0 * * ?";
    public static final String DAILY_CANCELLATION_CRON = "0 15 0 * * ?";

    private static final String BILLING_JOB_IDENTITY = "dailyBillingJob";
    private static final String BILLING_TRIGGER_IDENTITY = "dailyBillingTrigger";
    private static final String CANCELLATION_JOB_IDENTITY = "overduePolicyCancellationJob";
    private static final String CANCELLATION_TRIGGER_IDENTITY = "overduePolicyCancellationTrigger";
    private static final int LOCAL_BILLING_INTERVAL_SECONDS = 30;
    private static final int LOCAL_CANCELLATION_INTERVAL_SECONDS = 45;

    @Bean
    public JobDetail billingJobDetail() {
        return JobBuilder.newJob(BillingJob.class)
                .withIdentity(BILLING_JOB_IDENTITY)
                .usingJobData("productionCronExpression", DAILY_BILLING_CRON)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger billingJobTrigger(JobDetail billingJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(billingJobDetail)
                .withIdentity(BILLING_TRIGGER_IDENTITY)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(LOCAL_BILLING_INTERVAL_SECONDS)
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail cancellationJobDetail() {
        return JobBuilder.newJob(CancellationJob.class)
                .withIdentity(CANCELLATION_JOB_IDENTITY)
                .usingJobData("productionCronExpression", DAILY_CANCELLATION_CRON)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger cancellationJobTrigger(JobDetail cancellationJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(cancellationJobDetail)
                .withIdentity(CANCELLATION_TRIGGER_IDENTITY)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(LOCAL_CANCELLATION_INTERVAL_SECONDS)
                        .repeatForever())
                .build();
    }
}
