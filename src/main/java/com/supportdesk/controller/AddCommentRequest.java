package com.supportdesk.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AddCommentRequest: Request body payload for adding a comment to a ticket.
 * 
 * [English]:
 * Encapsulates the employee ID of the comment author and the text content of the comment.
 * Validates that employee ID is not null and comment text is not blank.
 * 
 * [Hinglish]:
 * Ticket par comment post karne ke liye request body ka data transfer object (DTO).
 * Isme employee ID aur comment ka text hota hai.
 */
public class AddCommentRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Comment text is required")
    private String commentText;

    public AddCommentRequest() {
    }

    public AddCommentRequest(Long employeeId, String commentText) {
        this.employeeId = employeeId;
        this.commentText = commentText;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * Alias setter to support "authorId" in JSON payload if sent by client.
     */
    public void setAuthorId(Long authorId) {
        if (this.employeeId == null) {
            this.employeeId = authorId;
        }
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Alias setter to support "comment" in JSON payload if sent by client.
     */
    public void setComment(String comment) {
        if (this.commentText == null || this.commentText.isBlank()) {
            this.commentText = comment;
        }
    }
}
