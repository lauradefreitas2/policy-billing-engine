package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.BillingJob;
import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.CancellationJob;
import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.OutboxPublisherJob;
import br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler.SuspensionJob;
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
    public static final String DAILY_SUSPENSION_CRON = "0 5 0 * * ?";
    public static final String DAILY_CANCELLATION_CRON = "0 15 0 * * ?";

    private static final String BILLING_JOB_IDENTITY = "dailyBillingJob";
    private static final String BILLING_TRIGGER_IDENTITY = "dailyBillingTrigger";
    private static final String SUSPENSION_JOB_IDENTITY = "overduePolicySuspensionJob";
    private static final String SUSPENSION_TRIGGER_IDENTITY = "overduePolicySuspensionTrigger";
    private static final String CANCELLATION_JOB_IDENTITY = "overduePolicyCancellationJob";
    private static final String CANCELLATION_TRIGGER_IDENTITY = "overduePolicyCancellationTrigger";
    private static final String OUTBOX_JOB_IDENTITY = "outboxPublisherJob";
    private static final String OUTBOX_TRIGGER_IDENTITY = "outboxPublisherTrigger";
    private static final int LOCAL_BILLING_INTERVAL_SECONDS = 30;
    private static final int LOCAL_SUSPENSION_INTERVAL_SECONDS = 40;
    private static final int LOCAL_CANCELLATION_INTERVAL_SECONDS = 45;
    private static final int OUTBOX_INTERVAL_SECONDS = 5;

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
    public JobDetail suspensionJobDetail() {
        return JobBuilder.newJob(SuspensionJob.class)
                .withIdentity(SUSPENSION_JOB_IDENTITY)
                .usingJobData("productionCronExpression", DAILY_SUSPENSION_CRON)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger suspensionJobTrigger(JobDetail suspensionJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(suspensionJobDetail)
                .withIdentity(SUSPENSION_TRIGGER_IDENTITY)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(LOCAL_SUSPENSION_INTERVAL_SECONDS)
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

    @Bean
    public JobDetail outboxPublisherJobDetail() {
        return JobBuilder.newJob(OutboxPublisherJob.class)
                .withIdentity(OUTBOX_JOB_IDENTITY)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger outboxPublisherJobTrigger(JobDetail outboxPublisherJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(outboxPublisherJobDetail)
                .withIdentity(OUTBOX_TRIGGER_IDENTITY)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(OUTBOX_INTERVAL_SECONDS)
                        .repeatForever())
                .build();
    }
}
