package com.taskqueue.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class EmailTaskExecutor implements TaskExecutor {
    
    @Override
    public String getTaskType() {
        return "EMAIL_SEND";
    }
    
    @Override
    public void execute(Map<String, Object> payload) throws Exception {
        String to = (String) payload.get("to");
        String subject = (String) payload.get("subject");
        String body = (String) payload.get("body");
        
        log.info("Sending email to: {}, subject: {}", to, subject);
        
        // Simulate email sending
        Thread.sleep(1000); // Simulate network delay
        
        // In production, integrate with JavaMail, SendGrid, or AWS SES
        // Example:
        // SimpleMailMessage message = new SimpleMailMessage();
        // message.setTo(to);
        // message.setSubject(subject);
        // message.setText(body);
        // mailSender.send(message);
        
        log.info("Email sent successfully to: {}", to);
    }
}
