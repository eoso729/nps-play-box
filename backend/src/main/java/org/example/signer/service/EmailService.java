package org.example.signer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@npsplaybox.com}")
    private String fromEmail;

    public void sendInvitationEmail(
            String toEmail,
            String tenantName,
            String inviterName,
            String token,
            String customMessage) {

        String invitationUrl = frontendUrl + "/accept-invitation?token=" + token;

        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append(inviterName).append(" has invited you to join ")
                .append(tenantName).append(" on NPS Play Box.\n\n");

        if (StringUtils.hasText(customMessage)) {
            body.append("Message from ").append(inviterName).append(":\n");
            body.append(customMessage).append("\n\n");
        }

        body.append("Click the link below to accept your invitation:\n");
        body.append(invitationUrl).append("\n\n");
        body.append("This invitation will expire in 72 hours.\n\n");
        body.append("If you didn't expect this invitation, you can safely ignore this email.\n\n");
        body.append("Best regards,\n");
        body.append("The NPS Play Box Team");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("You're invited to join " + tenantName);
                message.setText(body.toString());
                mailSender.send(message);
                log.info("Invitation email successfully dispatched to: {}", toEmail);
                return;
            } catch (Exception e) {
                log.warn("Failed to dispatch email to {}: {}. Falling back to log-based notification.", toEmail, e.getMessage());
            }
        }

        log.info("Invitation token generated for {} in tenant '{}': [Link: {}]", toEmail, tenantName, invitationUrl);
    }

    public void sendSeatRequestStatusEmail(
            String toEmail,
            String tenantName,
            boolean approved,
            int seatCount,
            String reasonOrNotes) {

        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        if (approved) {
            body.append("Your request for additional seats for ").append(tenantName)
                    .append(" has been APPROVED.\n\n")
                    .append("Approved Seats: ").append(seatCount).append("\n");
            if (StringUtils.hasText(reasonOrNotes)) {
                body.append("Notes: ").append(reasonOrNotes).append("\n");
            }
        } else {
            body.append("Your request for additional seats for ").append(tenantName)
                    .append(" has been DENIED.\n\n");
            if (StringUtils.hasText(reasonOrNotes)) {
                body.append("Reason: ").append(reasonOrNotes).append("\n");
            }
        }
        body.append("\nBest regards,\nThe NPS Play Box Team");

        String subject = "Seat Request Update for " + tenantName + ": " + (approved ? "APPROVED" : "DENIED");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body.toString());
                mailSender.send(message);
                log.info("Seat request status email successfully dispatched to: {}", toEmail);
                return;
            } catch (Exception e) {
                log.warn("Failed to dispatch email to {}: {}. Falling back to log-based notification.", toEmail, e.getMessage());
            }
        }

        log.info("Seat request status for {} ({}) - Approved: {}, Seats: {}, Notes/Reason: {}",
                toEmail, tenantName, approved, seatCount, reasonOrNotes);
    }

    public String buildInvitationUrl(String token) {
        return frontendUrl + "/accept-invitation?token=" + token;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }
}
