package com.supportdesk.service;

/**
 * Custom runtime exception thrown when a requested resource (like an Employee or Ticket) is not found.
 * 
 * [English]:
 * Extending RuntimeException allows throwing unchecked exceptions in service methods.
 * Caught by GlobalExceptionHandler to return HTTP 404 Not Found.
 * 
 * [Hinglish]:
 * Jab database me koi record (ID ke basis par) nahi milta, tab yeh exception throw hoti hai.
 * GlobalExceptionHandler ise catch karke HTTP 404 error return karta hai.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
