package org.example.signer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.service.ImpersonationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImpersonationScheduler {

    private final ImpersonationSessionRepository sessionRepository;
    private final ImpersonationService impersonationService;

    /**
     * Runs every minute to check for expired active sessions and auto-terminate them.
     */
    @Scheduled(fixedDelay = 60000)
    public void terminateExpiredSessions() {
        try {
            List<ImpersonationSession> expiredSessions =
                    sessionRepository.findExpiredActiveSessions(LocalDateTime.now());

            for (ImpersonationSession session : expiredSessions) {
                try {
                    impersonationService.terminateImpersonation(
                            session.getSessionUuid(),
                            "Session expired automatically");

                    log.info("Auto-terminated expired impersonation session: {}",
                            session.getSessionUuid());

                } catch (Exception e) {
                    log.error("Failed to auto-terminate session: {}",
                            session.getSessionUuid(), e);
                }
            }

            if (!expiredSessions.isEmpty()) {
                log.info("Auto-terminated {} expired impersonation sessions",
                        expiredSessions.size());
            }

        } catch (Exception e) {
            log.error("Error in impersonation session termination scheduler", e);
        }
    }
}
