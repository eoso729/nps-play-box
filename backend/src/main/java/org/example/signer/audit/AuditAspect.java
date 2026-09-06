package org.example.signer.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.User;
import org.example.signer.security.TenantContext;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.AuditService;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

        StandardEvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Long tenantId = resolveTenantId(context);
        Long userId = resolveUserId(context);

        Object result = null;
        try {
            result = joinPoint.proceed();

            // Set result in SpEL context
            context.setVariable("result", result);

            String resourceId = resolveResourceId(auditable.resourceId(), context);

            if (tenantId == null) {
                if ("TENANT".equalsIgnoreCase(auditable.resourceType()) && resourceId != null) {
                    try {
                        tenantId = Long.parseLong(resourceId);
                    } catch (NumberFormatException ignored) {}
                }
                if (tenantId == null && "TENANT".equalsIgnoreCase(auditable.resourceType())) {
                    Object idVar = context.lookupVariable("id");
                    if (idVar instanceof Long idVal) {
                        tenantId = idVal;
                    }
                }
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("method", method.getName());

            try {
                auditService.logEvent(auditService.builder()
                        .tenantId(tenantId)
                        .userId(userId)
                        .eventType(auditable.eventType())
                        .action(auditable.action())
                        .resourceType(auditable.resourceType())
                        .resourceId(resourceId)
                        .status(AuditEvent.EventStatus.SUCCESS)
                        .metadata(metadata));
            } catch (Exception e) {
                log.error("Failed to log audit event in aspect: {}", e.getMessage(), e);
            }

            return result;
        } catch (Throwable ex) {
            if (auditable.logOnFailure()) {
                String resourceId = resolveResourceId(auditable.resourceId(), context);
                if (tenantId == null && "TENANT".equalsIgnoreCase(auditable.resourceType())) {
                    Object idVar = context.lookupVariable("id");
                    if (idVar instanceof Long idVal) {
                        tenantId = idVal;
                    }
                }

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("method", method.getName());
                metadata.put("exception", ex.getClass().getSimpleName());

                try {
                    auditService.logEvent(auditService.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .eventType(auditable.eventType())
                            .action(auditable.action())
                            .resourceType(auditable.resourceType())
                            .resourceId(resourceId)
                            .status(AuditEvent.EventStatus.FAILURE)
                            .errorMessage(ex.getMessage())
                            .metadata(metadata));
                } catch (Exception e) {
                    log.error("Failed to log audit failure event in aspect: {}", e.getMessage(), e);
                }
            }
            throw ex;
        }
    }

    private String resolveResourceId(String expressionStr, StandardEvaluationContext context) {
        if (!StringUtils.hasText(expressionStr)) {
            return null;
        }
        try {
            Expression expression = expressionParser.parseExpression(expressionStr);
            Object value = expression.getValue(context);
            return value != null ? String.valueOf(value) : null;
        } catch (Exception e) {
            log.debug("Could not evaluate SpEL expression '{}': {}", expressionStr, e.getMessage());
            return null;
        }
    }

    private Long resolveTenantId(StandardEvaluationContext context) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud) {
                return tud.getTenantId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getTenantId();
            }
        }

        // Try extracting from SpEL variables if passed as method parameter
        Object tenantIdVar = context.lookupVariable("tenantId");
        if (tenantIdVar instanceof Long tid) {
            return tid;
        }

        return null;
    }

    private Long resolveUserId(StandardEvaluationContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud && tud.getUser() != null) {
                return tud.getUser().getId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getId();
            }
        }

        Object userIdVar = context.lookupVariable("userId");
        if (userIdVar instanceof Long uid) {
            return uid;
        }
        Object actorIdVar = context.lookupVariable("actorId");
        if (actorIdVar instanceof Long aid) {
            return aid;
        }
        Object currentUserIdVar = context.lookupVariable("currentUserId");
        if (currentUserIdVar instanceof Long cuid) {
            return cuid;
        }

        return null;
    }
}
