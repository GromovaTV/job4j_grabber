package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        Properties config = new Properties();
        interval(config);
        int interval = Integer.parseInt(config.getProperty("rabbit.interval"));
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }

    public static void interval(Properties config) {
        try (FileInputStream in = new FileInputStream("C:\\projects\\job4j_grabber\\src\\main\\resources\\rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

