package com.hotwillnotelaborate.heatclient;

import com.hotwillnotelaborate.heatclient.client.CompletionOverlay;
import com.hotwillnotelaborate.heatclient.command.CommandFly;
import com.hotwillnotelaborate.heatclient.command.CommandHelp;
import com.hotwillnotelaborate.heatclient.command.CommandManager;
import com.hotwillnotelaborate.heatclient.event.ChatEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = HeatClient.MODID, name = HeatClient.NAME, version = HeatClient.VERSION, clientSideOnly = true)
public class HeatClient {

    public static final String MODID = "heatclient";
    public static final String NAME = "Heat Client";
    public static final String VERSION = "1.0.0";
    public static final String PREFIX = "!";
    public static final String CHAT_PREFIX = "\u00a76[Heat] "; // gold [Heat]

    public static final Logger logger = LogManager.getLogger(MODID);
    public static final CommandManager commandManager = new CommandManager();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Heat Client v{} initializing...", VERSION);

        // Register commands
        commandManager.register(new CommandFly());
        commandManager.register(new CommandHelp());

        // Register event handlers on the Forge event bus
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new CompletionOverlay());

        logger.info("Heat Client v{} loaded - {} commands registered.", VERSION,
                commandManager.getCommands().size());
    }
}
