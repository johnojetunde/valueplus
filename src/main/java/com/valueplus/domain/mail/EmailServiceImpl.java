package com.valueplus.domain.mail;

import com.valueplus.persistence.entity.ProductOrder;
import com.valueplus.persistence.entity.User;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.StringWriter;
import java.math.BigDecimal;

@Service
public class EmailServiceImpl implements EmailService {

    public static final String PASSWORD_SUBJECT = "Value Plus Forgot Password";
    public static final String VERIFY_EMAIL_SUBJECT = "Value Plus Verify Email";
    public static final String USER_CREATION_SUBJECT = "Value Plus Admin Account";
    public static final String USER_SUPER_AGENT_CREATION_SUBJECT = "Value Plus Super Agent Account";
    public static final String WALLET_NOTIFICATION_SUBJECT = "Value Plus Wallet Notification";
    public static final String PRODUCT_ORDER_STATUS_SUBJECT = "Value Plus Product Order Notification";
    public static final String PIN_UPDATE = "Value Plus PIN UPDATE Notification";

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
    public void sendPinNotification(User user) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/pinupdate.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), PIN_UPDATE, stringWriter.toString());
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

    @Override
    public void sendAdminUserCreationEmail(User user, String password) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/admincreation.vm");
        VelocityContext context = new VelocityContext();
        context.put("username", user.getEmail());
        context.put("name", user.getFirstname());
        context.put("password", password);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), USER_CREATION_SUBJECT, stringWriter.toString());
    }

    @Override
    public void sendSuperAgentUserCreationEmail(User user, String password) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/superagentcreate.vm");
        VelocityContext context = new VelocityContext();
        context.put("username", user.getEmail());
        context.put("name", user.getFirstname());
        context.put("password", password);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), USER_SUPER_AGENT_CREATION_SUBJECT, stringWriter.toString());
    }

    @Override
    public void sendCreditNotification(User user, BigDecimal amount) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/walletcredit.vm");
        walletNotification(user, amount, template);
    }

    @Override
    public void sendDebitNotification(User user, BigDecimal amount) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/walletdebit.vm");
        walletNotification(user, amount, template);
    }

    @Override
    public void sendProductOrderStatusUpdate(User user, ProductOrder productOrder) throws Exception {
        Template template = velocityEngine.getTemplate("/templates/product-order-status.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        context.put("status", productOrder.getStatus().name());
        context.put("productOrderId", productOrder.getId());
        context.put("productName", productOrder.getProduct().getName());
        ;
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), PRODUCT_ORDER_STATUS_SUBJECT, stringWriter.toString());
    }

    private void walletNotification(User user, BigDecimal amount, Template template) throws MessagingException {
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstname());
        context.put("amount", amount);
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendSimpleMessage(user.getEmail(), WALLET_NOTIFICATION_SUBJECT, stringWriter.toString());
    }
}
