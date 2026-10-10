package com.credit.exception;

import java.io.Serial;

public class InvalidLoanException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4821906374105580231L;

    public InvalidLoanException(String message) {
        super(message);
    }

    public InvalidLoanException(String message, Throwable cause) {
        super(message, cause);
    }
}
