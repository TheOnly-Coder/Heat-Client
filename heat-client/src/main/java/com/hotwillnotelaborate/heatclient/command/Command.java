package com.hotwillnotelaborate.heatclient.command;

import java.util.Collections;
import java.util.List;

/**
 * Base interface for all Heat Client commands.
 * Every command is triggered via chat using the "!" prefix.
 */
public interface Command {

    /** The primary command name (e.g. "fly"). */
    String getName();

    /** Short one-line description shown in the completion tooltip and !help. */
    String getDescription();

    /** Usage string shown on error (e.g. "!fly"). */
    String getUsage();

    /** Execute the command. args is everything after the command name, already split by whitespace. */
    void execute(String[] args);

    /** Alternative names the player can type. Empty by default. */
    default List<String> getAliases() {
        return Collections.emptyList();
    }
}
