package com.credit.exception;

import java.io.Serial;

public class LoanApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7305519280417733094L;

    public LoanApiException(String message) {
        super(message);
    }

    public LoanApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
