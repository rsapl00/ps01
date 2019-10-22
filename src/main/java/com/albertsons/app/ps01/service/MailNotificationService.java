package com.albertsons.app.ps01.service;

import javax.mail.internet.MimeMessage;

import com.albertsons.app.ps01.service.resource.Ps01MailMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class MailNotificationService {

    private final Log logger = LogFactory.getLog(MailNotificationService.class);

    private JavaMailSender javaMailSender;

    public MailNotificationService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendNotification(Ps01MailMessage message) {
        try {

            if (logger.isDebugEnabled()) {
                logger.debug("Sending simple message email notification.");
            }

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(message.getToRecipient().stream().toArray(String[]::new));
            mail.setSubject(message.getMessageTitle());
            mail.setText(message.getMessageBody());
            javaMailSender.send(mail);

        } catch (MailException me) {
            logger.error("There was an issue while sending email notification.", me);
        }
    }

    @Async
    public void sendMailAttachmentNotification(Ps01MailMessage message) {

        try {

            if (logger.isDebugEnabled()) {
                logger.debug("Sending email with attachment.");
            }

            Assert.notNull(message.getAttachment(), "Empty attachment.");

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(message.getToRecipient().stream().toArray(String[]:: new));
            helper.setSubject(message.getMessageTitle());
            helper.setText(message.getMessageBody());

            helper.addAttachment("Host-POS.xlsx", message.getMessageAttachment());

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            logger.error("There was an issue while sending email notification with attachement.", e);
        }

    }

}