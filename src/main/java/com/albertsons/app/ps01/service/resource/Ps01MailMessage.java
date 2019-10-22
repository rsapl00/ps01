package com.albertsons.app.ps01.service.resource;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import lombok.Data;

@Data
public class Ps01MailMessage {

    private final Log logger = LogFactory.getLog(Ps01MailMessage.class);

    private List<String> toRecipient;

    @Value("${spring.mail.username}")
    private String fromRecipient;
    
    private String messageTitle;
    private String messageBody;
    private Object attachment;

    public DataSource getMessageAttachment() {
        
        Assert.notNull(attachment, "Attachment should not be empty.");

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving message attachment.");
        }
        
        if (!(attachment instanceof Workbook)) {
            logger.error("Attachment is not an excel file.");
            return null;
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ((Workbook) attachment).write(bos);
            bos.close();

            return new ByteArrayDataSource(bos.toByteArray(), "application/vnd.ms-excel");
        } catch (Exception e) {
            logger.error("There is an issue while retrieving message attachment.", e);
            return null;
        }
    }

}