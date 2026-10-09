package com.credit.exception;

public class InputEndedException extends RuntimeException {

    private static final long serialVersionUID = 6159032847761204417L;

    public InputEndedException() {
        super("Input berakhir.");
    }
}
