package org.example.signer.audit;

import org.example.signer.entity.AuditEvent;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be automatically audited.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Event type for the audit log.
     */
    AuditEvent.EventType eventType();

    /**
     * Action description (e.g., "CREATE_USER", "LOGIN").
     */
    String action();

    /**
     * Resource type (e.g., "USER", "TENANT").
     */
    String resourceType();

    /**
     * SpEL expression to extract resource ID from method parameters or result.
     * Example: "#result.id" or "#request.userId" or "#id"
     */
    String resourceId() default "";

    /**
     * Whether to log on failure as well as success (default: true).
     */
    boolean logOnFailure() default true;
}
