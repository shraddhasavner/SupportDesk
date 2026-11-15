package com.supportdesk.controller;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for adding resolution notes and resolving a ticket.
 */
public class ResolutionRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;

    private boolean markResolved = true;

    public ResolutionRequest() {
    }

    public ResolutionRequest(String resolutionNotes, boolean markResolved) {
        this.resolutionNotes = resolutionNotes;
        this.markResolved = markResolved;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public boolean isMarkResolved() {
        return markResolved;
    }

    public void setMarkResolved(boolean markResolved) {
        this.markResolved = markResolved;
    }
}
