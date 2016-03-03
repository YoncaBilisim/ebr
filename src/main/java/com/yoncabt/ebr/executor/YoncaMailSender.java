/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yoncabt.ebr.executor;

import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 *
 * @author myururdurmaz
 */
@Component
public class YoncaMailSender {

    @Autowired
    private JavaMailSender mailSender;

    public void send(String to, String text, Map<String, byte[]> attachments) throws MessagingException {

        MimeMessage mm = mailSender.createMimeMessage();
        MimeMessageHelper mmh = new MimeMessageHelper(mm, true);
        mmh.setTo(to);
        mmh.setText(text);
        for (Map.Entry<String, byte[]> entrySet : attachments.entrySet()) {
            String key = entrySet.getKey();
            byte[] value = entrySet.getValue();
            ByteArrayResource isr = new ByteArrayResource(value);
            mmh.addAttachment(key, isr);

        }
        mailSender.send(mm);
    }

}
