package com.hotwillnotelaborate.heatclient.command;

/**
 * Registers all package-private command implementations.
 * Called from HeatClient.init() to avoid cross-package visibility issues.
 */
public class CommandRegistrar {
    public static void registerAll(CommandManager mgr) {
        mgr.register(new CommandAntiAFK());
        mgr.register(new CommandAutoRespawn());
        mgr.register(new CommandSprint());
        mgr.register(new CommandDerp());
        mgr.register(new CommandSpammer());
        mgr.register(new CommandTriggerbot());
        mgr.register(new CommandAutoClicker());
        mgr.register(new CommandFastPlace());
        mgr.register(new CommandNuker());
        mgr.register(new CommandChestStealer());
        mgr.register(new CommandTracers());
        mgr.register(new CommandESP());
        mgr.register(new CommandNametags());
        mgr.register(new CommandBlockOverlay());
        mgr.register(new CommandBreadcrumbs());
        mgr.register(new CommandNoBob());
        mgr.register(new CommandInventoryWalk());
        mgr.register(new CommandAutoBow());
        mgr.register(new CommandTimer());
        mgr.register(new CommandItemESP());
        mgr.register(new CommandStorageESP());
        mgr.register(new CommandPanic());
    }
}