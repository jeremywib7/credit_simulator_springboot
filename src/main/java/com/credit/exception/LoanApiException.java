package com.credit.exception;

public class LoanApiException extends RuntimeException {

    private static final long serialVersionUID = -7305519280417733094L;

    public LoanApiException(String message) {
        super(message);
    }

    public LoanApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
