package org.example.signer.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AuditEventSpecification {

    public static Specification<AuditEvent> buildSpecification(
            Long tenantId, AuditEventSearchRequest searchRequest) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // If tenantId is specified, enforce tenant isolation
            if (tenantId != null) {
                predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));
            }

            if (searchRequest != null) {
                if (searchRequest.getEventType() != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("eventType"), searchRequest.getEventType()));
                }

                if (StringUtils.hasText(searchRequest.getAction())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("action")),
                            "%" + searchRequest.getAction().trim().toLowerCase() + "%"));
                }

                if (StringUtils.hasText(searchRequest.getResourceType())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("resourceType"), searchRequest.getResourceType().trim()));
                }

                if (StringUtils.hasText(searchRequest.getResourceId())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("resourceId"), searchRequest.getResourceId().trim()));
                }

                if (searchRequest.getUserId() != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("userId"), searchRequest.getUserId()));
                }

                if (searchRequest.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("status"), searchRequest.getStatus()));
                }

                if (searchRequest.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("createdAt"), searchRequest.getStartDate()));
                }

                if (searchRequest.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("createdAt"), searchRequest.getEndDate()));
                }

                if (StringUtils.hasText(searchRequest.getIpAddress())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("ipAddress"), searchRequest.getIpAddress().trim()));
                }

                if (StringUtils.hasText(searchRequest.getSearchTerm())) {
                    String term = "%" + searchRequest.getSearchTerm().trim().toLowerCase() + "%";
                    Predicate actionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("action")), term);
                    Predicate resourceMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("resourceId")), term);
                    predicates.add(criteriaBuilder.or(actionMatch, resourceMatch));
                }
            }

            // Order by created_at descending by default
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
