package com.hardrockunion.boot.config;

import java.util.concurrent.Executors;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;

@Configuration
@EnableAsync
public class VirtualThreadConfig {

    @Bean(name = TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        return applicationTaskExecutor();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        SimpleAsyncTaskScheduler taskScheduler = new SimpleAsyncTaskScheduler();
        taskScheduler.setVirtualThreads(true);
        taskScheduler.setThreadNamePrefix("hardrock-vt-scheduler-");
        return taskScheduler;
    }
}
