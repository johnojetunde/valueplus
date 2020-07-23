package com.codeemma.valueplus.domain.mail;


import com.codeemma.valueplus.persistence.entity.User;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class EmailServiceImpl implements EmailService {

    public static final String PASSWORD_SUBJECT = "Value Plus Password Reset";

    private final EmailClient emailClient;
    private final VelocityEngine velocityEngine;

    public EmailServiceImpl(EmailClient emailClient, VelocityEngine velocityEngine) {
        this.emailClient = emailClient;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public void sendPasswordReset(User user, String newPassword) {
        Template template = velocityEngine.getTemplate("/templates/OutworkerSignUp.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        context.put("email", user.getEmail());
        context.put("password", newPassword);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), PASSWORD_SUBJECT, stringWriter.toString());
    }
}
