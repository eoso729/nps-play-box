package org.example.signer.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationCleanupTask {

    private final UserService userService;

    // Run daily at 2 AM
    @Scheduled(cron = "${app.invitation.cleanup-cron:0 0 2 * * *}")
    public void cleanupExpiredInvitations() {
        log.info("Starting expired invitation cleanup task");
        userService.cleanupExpiredInvitations();
        log.info("Expired invitation cleanup task completed");
    }
}
