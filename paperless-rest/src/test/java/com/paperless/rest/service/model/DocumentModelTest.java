package com.paperless.rest.service.model;

import com.paperless.rest.exception.BusinessLayerException;
import com.paperless.rest.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DocumentModelTest {

    // GIVEN ein Dokument im Status RECEIVED
    // WHEN transitionTo mit dem erlaubten Folgestatus PROCESSING aufgerufen wird
    // THEN wird der Status auf PROCESSING geändert
    @Test
    void transitionTo_WithAllowedTarget_ChangesStatus() {
        DocumentModel model = DocumentModel.builder().currentStatus(DocumentStatus.RECEIVED).build();

        model.transitionTo(DocumentStatus.PROCESSING);

        assertEquals(DocumentStatus.PROCESSING, model.getCurrentStatus());
    }

    // GIVEN ein Dokument im Endstatus COMPLETED
    // WHEN transitionTo mit einem anderen Status aufgerufen wird
    // THEN wird eine InvalidStatusTransitionException geworfen und der Status bleibt gleich
    @Test
    void transitionTo_FromFinalStatus_ThrowsInvalidStatusTransitionException() {
        DocumentModel model = DocumentModel.builder().currentStatus(DocumentStatus.COMPLETED).build();

        assertThrows(InvalidStatusTransitionException.class, () -> model.transitionTo(DocumentStatus.PROCESSING));
        assertEquals(DocumentStatus.COMPLETED, model.getCurrentStatus());
    }

    // GIVEN ein Dokument ohne Status
    // WHEN transitionTo aufgerufen wird
    // THEN wird von RECEIVED ausgegangen und der erlaubte Wechsel durchgeführt
    @Test
    void transitionTo_WithoutCurrentStatus_StartsFromReceived() {
        DocumentModel model = DocumentModel.builder().build();

        model.transitionTo(DocumentStatus.REJECTED);

        assertEquals(DocumentStatus.REJECTED, model.getCurrentStatus());
    }

    // GIVEN ein beliebiges Dokument
    // WHEN transitionTo mit null aufgerufen wird
    // THEN wird eine BusinessLayerException geworfen
    @Test
    void transitionTo_WithNullTarget_ThrowsBusinessLayerException() {
        DocumentModel model = DocumentModel.builder().currentStatus(DocumentStatus.RECEIVED).build();

        assertThrows(BusinessLayerException.class, () -> model.transitionTo(null));
    }

    // GIVEN alle Kombinationen aus aktuellem Status und Zielstatus
    // WHEN canTransitionTo aufgerufen wird
    // THEN entspricht das Ergebnis dem definierten Lebenszyklus
    @ParameterizedTest
    @CsvSource({
            "RECEIVED, PROCESSING, true",
            "RECEIVED, REJECTED, true",
            "RECEIVED, COMPLETED, false",
            "RECEIVED, RECEIVED, false",
            "PROCESSING, REVIEW_PENDING, true",
            "PROCESSING, REJECTED, true",
            "PROCESSING, COMPLETED, false",
            "REVIEW_PENDING, COMPLETED, true",
            "REVIEW_PENDING, PROCESSING, true",
            "REVIEW_PENDING, REJECTED, true",
            "REVIEW_PENDING, RECEIVED, false",
            "COMPLETED, REJECTED, false",
            "REJECTED, RECEIVED, false"
    })
    void canTransitionTo_FollowsLifecycle(DocumentStatus current, DocumentStatus target, boolean expected) {
        assertEquals(expected, current.canTransitionTo(target));
    }

    // GIVEN ein beliebiger Status
    // WHEN canTransitionTo mit null aufgerufen wird
    // THEN ist der Wechsel nicht erlaubt
    @Test
    void canTransitionTo_WithNullTarget_ReturnsFalse() {
        assertFalse(DocumentStatus.RECEIVED.canTransitionTo(null));
    }
}
