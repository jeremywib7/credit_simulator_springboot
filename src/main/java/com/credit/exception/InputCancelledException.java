package com.credit.exception;

public class InputCancelledException extends RuntimeException {

    private static final long serialVersionUID = 2938475610293847561L;

    public InputCancelledException() {
        super("Input dibatalkan.");
    }
}
