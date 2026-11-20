package com.example.reporting.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String serviceName, String message) {
        super("Service [" + serviceName + "] is currently unavailable: " + message);
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}

