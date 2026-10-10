package com.credit.exception;

import java.io.Serial;

public class InputEndedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6159032847761204417L;

    public InputEndedException() {
        super("Input berakhir.");
    }
}
