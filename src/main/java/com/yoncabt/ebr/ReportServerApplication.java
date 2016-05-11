package com.yoncabt.ebr;

import com.yoncabt.abys.core.util.EBRConf;
import com.yoncabt.abys.core.util.EBRParams;
import com.yoncabt.ebr.logger.ReportLogger;
import com.yoncabt.ebr.logger.fs.FileSystemReportLogger;
import com.yoncabt.ebr.security.Authenticator;
import com.yoncabt.ebr.security.AuthenticatorEBRConfImpl;
import com.yoncabt.ebr.util.VersionUtil;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
public class ReportServerApplication {

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        VersionUtil.print();
        SpringApplication.run(ReportServerApplication.class, args);
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, scopeName = "prototype")
    public ReportLogger reportLogger() {
        String impl = EBRConf.INSTANCE.getValue(EBRParams.REPORT_LOGGER_IMPL, FileSystemReportLogger.class.getName());
        return inject(ReportLogger.class, impl);
    }

    @Bean
    @Scope(value = "vaadin-ui", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Authenticator authenticator() {
        String impl = EBRConf.INSTANCE.getValue(EBRParams.REPORT_AUTHENTICATOR_IMPL, AuthenticatorEBRConfImpl.class.getName());
        return inject(Authenticator.class, impl);
    }

    private <T> T inject(Class<T> type, String implName) {
        T ret;
        try {
            ret = (T) Class.forName(implName).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            throw new Error(ex);
        }
        context.getAutowireCapableBeanFactory().autowireBean(ret);
        return ret;
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
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
        pool.setPoolSize(100);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();

        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", EBRConf.INSTANCE.getValue("report.smtp.auth", ""));
        mailProperties.put("mail.smtp.starttls.enable", EBRConf.INSTANCE.getValue("report.smtp.starttls.enable", ""));
        mailProperties.put("mail.smtp.starttls.required", EBRConf.INSTANCE.getValue("report.smtp.starttls.required", ""));
        mailProperties.put("mail.smtp.socketFactory.port", EBRConf.INSTANCE.getValue("mail.smtp.socketFactory.port", ""));
        mailProperties.put("mail.smtp.debug", EBRConf.INSTANCE.getValue("report.smtp.debug", ""));
        mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mailProperties.put("mail.smtp.socketFactory.fallback", EBRConf.INSTANCE.getValue("report.smtp.socketFactory.fallback", "false"));

        impl.setJavaMailProperties(mailProperties);
        impl.setHost(EBRConf.INSTANCE.getValue("report.smtp.server.address", ""));
        impl.setPort(EBRConf.INSTANCE.getValue("report.smtp.server.port", 25));
        impl.setProtocol(EBRConf.INSTANCE.getValue("report.mail.protocol", "smtp"));
        impl.setUsername(EBRConf.INSTANCE.getValue("report.smtp.username", ""));
        impl.setPassword(EBRConf.INSTANCE.getValue("report.smtp.password", ""));

        return impl;
    }
}
