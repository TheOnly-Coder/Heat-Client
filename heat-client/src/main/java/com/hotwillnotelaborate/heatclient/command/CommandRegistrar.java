package com.hotwillnotelaborate.heatclient.command;

/**
 * Registers all package-private command implementations.
 * Called from HeatClient.init() to avoid cross-package visibility issues.
 */
public class CommandRegistrar {
    public static void registerAll(CommandManager mgr) {
        // Hydrogen features
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
        // SkidBounce features
        mgr.register(new CommandVelocity());
        mgr.register(new CommandNoFall());
        mgr.register(new CommandBlink());
        mgr.register(new CommandCriticals());
        mgr.register(new CommandSuperKnockback());
        mgr.register(new CommandFastUse());
        mgr.register(new CommandFastBreak());
        mgr.register(new CommandAutoTool());
        mgr.register(new CommandNoHurtCam());
        mgr.register(new CommandAntiBlind());
        mgr.register(new CommandAntiExploit());
        mgr.register(new CommandNoSwing());
        // System
        mgr.register(new CommandPanic());
    }
}