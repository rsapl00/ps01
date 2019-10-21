package com.albertsons.app.ps01.service;

import javax.mail.internet.MimeMessage;

import com.albertsons.app.ps01.service.resource.Ps01MailMessage;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailNotificationService {

    private JavaMailSender javaMailSender;

    public MailNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendNotification(Ps01MailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("message.getTo");
        mail.setSubject("subject");
        mail.setText("");
        javaMailSender.send(mail);
    }

    @Async
    public void sendMailAttachmentNotification(Ps01MailMessage message) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo("to");
            helper.setSubject("subject");
            helper.setText("text");

            // FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            // helper.addAttachment("Invoice", file);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {

        }

    }

}