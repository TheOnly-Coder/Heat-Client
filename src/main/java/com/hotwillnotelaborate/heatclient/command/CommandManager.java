package com.hotwillnotelaborate.heatclient.command;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry and execution engine for Heat Client commands.
 * Commands are stored in insertion order; aliases map back to the same instance.
 */
public class CommandManager {

    /** name/alias (lowercase) -> Command instance */
    private final Map<String, Command> registry = new LinkedHashMap<>();
    /** Set of primary names only (for dedup when iterating) */
    private final Set<String> primaryNames = new HashSet<>();

    /** Register a command and all its aliases. */
    public void register(Command command) {
        String key = command.getName().toLowerCase();
        registry.put(key, command);
        primaryNames.add(key);
        for (String alias : command.getAliases()) {
            registry.put(alias.toLowerCase(), command);
        }
    }

    /** Look up a command by name or alias (case-insensitive). */
    public Command getCommand(String name) {
        return registry.get(name.toLowerCase());
    }

    /**
     * Return unique command instances in registration order.
     * Aliases are collapsed so each command appears exactly once.
     */
    public Collection<Command> getCommands() {
        List<Command> unique = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Command> entry : registry.entrySet()) {
            String primary = entry.getValue().getName().toLowerCase();
            if (seen.add(primary)) {
                unique.add(entry.getValue());
            }
        }
        return unique;
    }

    /**
     * Given the text after "!", return all primary command names that start with it.
     * Used by the auto-completion overlay.
     */
    public List<String> getCompletions(String partial) {
        String lower = partial.toLowerCase();
        return primaryNames.stream()
                .filter(name -> name.startsWith(lower))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Parse and execute a raw chat message that starts with "!".
     * Returns true if a command was found and executed, false otherwise.
     */
    public boolean execute(String raw) {
        String trimmed = raw.trim();
        if (trimmed.length() <= 1) return false;

        String[] parts = trimmed.substring(1).split("\\s+"); // skip the "!"
        if (parts[0].isEmpty()) return false;

        Command cmd = getCommand(parts[0]);
        if (cmd == null) return false;

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        cmd.execute(args);
        return true;
    }
}
