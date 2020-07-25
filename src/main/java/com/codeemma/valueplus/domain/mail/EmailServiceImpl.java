package com.codeemma.valueplus.domain.mail;


import com.codeemma.valueplus.persistence.entity.User;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.StringWriter;

@Service
public class EmailServiceImpl implements EmailService {

    public static final String PASSWORD_SUBJECT = "Value Plus Forgot Password";
    public static final String VERIFY_EMAIL_SUBJECT = "Value Plus Verify Email";

    private final EmailClient emailClient;
    private final VelocityEngine velocityEngine;

    public EmailServiceImpl(EmailClient emailClient, VelocityEngine velocityEngine) {
        this.emailClient = emailClient;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public void sendPasswordReset(User user, String link) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/passwordreset.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        context.put("link", link);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), PASSWORD_SUBJECT, stringWriter.toString());
    }

    @Override
    public void sendEmailVerification(User user, String link) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/verifyemail.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        context.put("link", link);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), VERIFY_EMAIL_SUBJECT, stringWriter.toString());
    }
}
