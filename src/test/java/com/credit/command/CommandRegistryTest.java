package com.credit.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryTest {

    private static Command command(String name) {
        return new Command() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return "desc " + name;
            }

            @Override
            public void execute(String argument) {
            }
        };
    }

    @Test
    void keepsRegistrationOrder() {
        CommandRegistry registry = new CommandRegistry().register(command("b")).register(command("a"));
        assertEquals(List.of("b", "a"), registry.names());
    }

    @Test
    void findIsCaseInsensitiveAndEmptyWhenUnknown() {
        CommandRegistry registry = new CommandRegistry().register(command("show"));
        assertTrue(registry.find("SHOW").isPresent());
        assertTrue(registry.find("nope").isEmpty());
    }

    @Test
    void registeringSameNameReplacesPrevious() {
        CommandRegistry registry = new CommandRegistry().register(command("x")).register(command("x"));
        assertEquals(1, registry.all().size());
    }
}
