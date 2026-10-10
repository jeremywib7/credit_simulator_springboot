package com.credit.command;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandRegistry register(Command command) {
        commands.put(command.name(), command);
        return this;
    }

    public Optional<Command> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public Collection<Command> all() {
        return List.copyOf(commands.values());
    }

    public List<String> names() {
        return List.copyOf(commands.keySet());
    }
}
