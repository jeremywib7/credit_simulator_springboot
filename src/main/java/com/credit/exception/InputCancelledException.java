package com.credit.exception;

import java.io.Serial;

public class InputCancelledException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2938475610293847561L;

    public InputCancelledException() {
        super("Input dibatalkan.");
    }
}
