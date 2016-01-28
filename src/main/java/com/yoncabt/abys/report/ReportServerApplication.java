package com.yoncabt.abys.report;

import com.yoncabt.abys.core.util.ABYSConf;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class ReportServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServerApplication.class, args);
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(25);
        pool.setMaxPoolSize(100);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();

        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", ABYSConf.INSTANCE.getValue("report.smtp.auth", ""));
        mailProperties.put("mail.smtp.starttls.enable", ABYSConf.INSTANCE.getValue("report.smtp.starttls.enable", ""));
        mailProperties.put("mail.smtp.starttls.required", ABYSConf.INSTANCE.getValue("report.smtp.starttls.required", ""));
        mailProperties.put("mail.smtp.socketFactory.port", ABYSConf.INSTANCE.getValue("report.smtp.auth", ""));
        mailProperties.put("mail.smtp.debug", ABYSConf.INSTANCE.getValue("report.smtp.debug", ""));
        mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailProperties.put("mail.smtp.socketFactory.fallback", ABYSConf.INSTANCE.getValue("report.smtp.socketFactory.fallback", "false"));

        impl.setJavaMailProperties(mailProperties);
        impl.setHost(ABYSConf.INSTANCE.getValue("report.smtp.server.address", ""));
        impl.setPort(ABYSConf.INSTANCE.getValue("report.smtp.server.port", 25));
        impl.setProtocol(ABYSConf.INSTANCE.getValue("report.mail.protocol", "smtp"));
        impl.setUsername(ABYSConf.INSTANCE.getValue("report.smtp.username", ""));
        impl.setPassword(ABYSConf.INSTANCE.getValue("report.smtp.password", ""));

        return impl;
    }
}
