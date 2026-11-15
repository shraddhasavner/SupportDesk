package com.supportdesk.service;

/**
 * Custom runtime exception thrown when attempting to register or update an employee
 * with an email that already exists in the system.
 * 
 * [English]:
 * Caught by GlobalExceptionHandler to return HTTP 409 Conflict.
 * 
 * [Hinglish]:
 * Agar koi aisa email daala jaye jo pehle se kisi doosre employee ka hai, toh yeh exception aayegi.
 * GlobalExceptionHandler ise HTTP 409 Conflict status ke saath return karega.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
