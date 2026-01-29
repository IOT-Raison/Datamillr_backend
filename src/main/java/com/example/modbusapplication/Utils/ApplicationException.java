package com.example.modbusapplication.Utils;

public class ApplicationException extends Exception {

    // Default constructor
    public ApplicationException() {
        super();
    }

    // Constructor with message
    public ApplicationException(String message) {
        super(message);
    }

    // Constructor with message + cause
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor with cause only
    public ApplicationException(Throwable cause) {
        super(cause);
    }
}
