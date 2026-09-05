package org.example.signer.service;

import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.from:noreply@npsplaybox.com}")
    private String fromEmail;

    @Async
    public void sendImpersonationRequestNotification(User targetUser, User supportUser,
                                                      ImpersonationSession session) {
        String supportName = (supportUser.getFirstName() != null ? supportUser.getFirstName() : "")
                + (supportUser.getLastName() != null ? " " + supportUser.getLastName() : "");
        if (supportName.isBlank()) {
            supportName = supportUser.getEmail();
        }

        String body = String.format(
                "Dear %s,\n\n" +
                "A support team member has requested temporary access to your account.\n\n" +
                "Support User: %s (%s)\n" +
                "Reason: %s\n" +
                "Duration: %d minutes\n" +
                "Session ID: %s\n\n" +
                "This request requires approval before access is granted.\n\n" +
                "If you have concerns, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "NPS Play Box Support Team",
                targetUser.getFirstName() != null ? targetUser.getFirstName() : "User",
                supportName,
                supportUser.getEmail(),
                session.getReason(),
                session.getMaxDurationMinutes(),
                session.getSessionUuid()
        );

        sendMailSafely(targetUser.getEmail(), "Support Access Request Notification", body);
    }

    @Async
    public void sendImpersonationApprovedNotification(User supportUser,
                                                       ImpersonationSession session) {
        String body = String.format(
                "Dear %s,\n\n" +
                "Your impersonation request has been approved.\n\n" +
                "Session ID: %s\n" +
                "Duration: %d minutes\n\n" +
                "You can now start the impersonation session.\n\n" +
                "Best regards,\n" +
                "NPS Play Box Support Team",
                supportUser.getFirstName() != null ? supportUser.getFirstName() : "Support User",
                session.getSessionUuid(),
                session.getMaxDurationMinutes()
        );

        sendMailSafely(supportUser.getEmail(), "Impersonation Request Approved", body);
    }

    @Async
    public void sendImpersonationStartedNotification(User targetUser,
                                                      ImpersonationSession session) {
        String body = String.format(
                "Dear %s,\n\n" +
                "A support team member has started accessing your account.\n\n" +
                "Session started: %s\n" +
                "Session expires: %s\n" +
                "Session ID: %s\n\n" +
                "All actions during this session are fully audited.\n\n" +
                "If you did not expect this access, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "NPS Play Box Support Team",
                targetUser.getFirstName() != null ? targetUser.getFirstName() : "User",
                session.getStartedAt(),
                session.getExpiresAt(),
                session.getSessionUuid()
        );

        sendMailSafely(targetUser.getEmail(), "Support Access Session Started", body);
    }

    @Async
    public void sendImpersonationTerminatedNotification(User targetUser,
                                                         ImpersonationSession session) {
        String body = String.format(
                "Dear %s,\n\n" +
                "The support access session to your account has ended.\n\n" +
                "Session ID: %s\n" +
                "Duration: %d minutes\n" +
                "Ended at: %s\n\n" +
                "All actions during this session have been audited.\n\n" +
                "Best regards,\n" +
                "NPS Play Box Support Team",
                targetUser.getFirstName() != null ? targetUser.getFirstName() : "User",
                session.getSessionUuid(),
                session.getMaxDurationMinutes(),
                session.getTerminatedAt()
        );

        sendMailSafely(targetUser.getEmail(), "Support Access Session Ended", body);
    }

    private void sendMailSafely(String toEmail, String subject, String body) {
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Notification email dispatched to {}", toEmail);
                return;
            } catch (Exception e) {
                log.warn("Failed to dispatch email to {}: {}. Falling back to log.", toEmail, e.getMessage());
            }
        }
        log.info("Impersonation Notification for {}: [{}] {}", toEmail, subject, body.replace("\n", " "));
    }
}
