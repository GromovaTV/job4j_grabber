package ru.job4j.quartz;

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
            String url = config.getProperty("hibernate.connection.url");
            String login = config.getProperty("hibernate.connection.username");
            String password = config.getProperty("hibernate.connection.password");
            Connection connection = DriverManager.getConnection(url, login, password);
            cn = connection;
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
        private long created;

        public Rabbit() {
            this.created = System.currentTimeMillis();
        }

        @Override
        public void execute(JobExecutionContext context) {
            var cn = (Connection) context.getJobDetail().getJobDataMap().get("cn");
            System.out.println("Rabbit runs here ...");
            try (PreparedStatement statement =
                         cn.prepareStatement("insert into rabbit(created_date) values (?)",
                                 Statement.RETURN_GENERATED_KEYS)) {
                statement.setTimestamp(1, new Timestamp(created));
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

