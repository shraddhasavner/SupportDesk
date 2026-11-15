package com.supportdesk.controller;

import com.supportdesk.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new IT support ticket.
 */
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required (e.g. Hardware, Software, Network, Access)")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    private TicketPriority priority = TicketPriority.MEDIUM;

    @NotNull(message = "Employee ID (creator) is required")
    private Long employeeId;

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String title, String description, String category, TicketPriority priority, Long employeeId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
        this.employeeId = employeeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
