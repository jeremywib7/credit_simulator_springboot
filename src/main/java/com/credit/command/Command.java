package com.credit.command;

public interface Command {

    String name();

    String description();

    void execute(String argument);
}
