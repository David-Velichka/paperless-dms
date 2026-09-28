package com.paperless.rest.service.model;

/**
 * Lifecycle states of a document
 */
public enum DocumentStatus {
    RECEIVED,
    PROCESSING,
    REVIEW_PENDING,
    COMPLETED,
    REJECTED;

    public boolean canTransitionTo(DocumentStatus target) {
        if (target == null || this == target) {
            return false;
        }
        return switch (this) {
            case RECEIVED -> target == PROCESSING || target == REJECTED;
            case PROCESSING -> target == REVIEW_PENDING || target == REJECTED;
            case REVIEW_PENDING -> target == COMPLETED || target == REJECTED || target == PROCESSING;
            case COMPLETED, REJECTED -> false;
        };
    }
}
