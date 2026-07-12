package com.estudy.utils;

import com.estudy.config.AppConfig;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component("MailUtils")
public class MailUtils {

    @Resource
    private AppConfig appConfig;

    @Resource
    private JavaMailSender mailSender;

    public void sendSimpleMail(String receiver, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(appConfig.getSendCodeEmail());
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setText(text);
        mailSender.send(message);
    }

    public void sendHtmlMail(String receiver, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(appConfig.getSendCodeEmail());
        helper.setTo(receiver);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}

