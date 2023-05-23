package ru.job4j;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private Connection cn;
    private Properties config;

    public void init() {
        try (FileInputStream in = new FileInputStream("C:\\projects\\job4j_grabber\\src\\main\\resources\\rabbit.properties")) {
            config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("hibernate.connection.driver_class"));
            cn = DriverManager.getConnection(
                    config.getProperty("hibernate.connection.url"),
                    config.getProperty("hibernate.connection.username"),
                    config.getProperty("hibernate.connection.password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AlertRabbit rabbit = new AlertRabbit();
        rabbit.init();
        int interval = Integer.parseInt(rabbit.config.getProperty("rabbit.interval"));
        int sleep = Integer.parseInt(rabbit.config.getProperty("rabbit.sleep"));
        try {

            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("cn", rabbit.cn);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(sleep);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        private Timestamp created;

        public Rabbit() {
            this.created = new Timestamp(System.currentTimeMillis());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("cn");
            try (PreparedStatement statement = cn.prepareStatement("insert into rabbit(created_date) values (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setTimestamp(1, created);
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}