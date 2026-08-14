package com.hotwillnotelaborate.heatclient;

import com.hotwillnotelaborate.heatclient.client.CompletionOverlay;
import com.hotwillnotelaborate.heatclient.command.*;
import com.hotwillnotelaborate.heatclient.event.ChatEventHandler;
import com.hotwillnotelaborate.heatclient.event.RenderHandler;
import com.hotwillnotelaborate.heatclient.event.TickHandler;
import com.hotwillnotelaborate.heatclient.event.XrayRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = HeatClient.MODID, name = HeatClient.NAME, version = HeatClient.VERSION, clientSideOnly = true)
public class HeatClient {

    public static final String MODID = "heatclient";
    public static final String NAME = "Heat Client";
    public static final String VERSION = "1.4.0";
    public static final String PREFIX = "!";
    public static final String CHAT_PREFIX = "\u00a76[Heat] ";

    public static final Logger logger = LogManager.getLogger(MODID);
    public static final CommandManager commandManager = new CommandManager();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Heat Client v{} initializing...", VERSION);

        // Register commands
        commandManager.register(new CommandFly());
        commandManager.register(new CommandHelp());
        commandManager.register(new CommandXray());
        commandManager.register(new CommandActive());
        commandManager.register(new CommandFullbright());
        CommandRegistrar.registerAll(commandManager);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new CompletionOverlay());
        MinecraftForge.EVENT_BUS.register(new XrayRenderer());
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.register(new RenderHandler());

        logger.info("Heat Client v{} loaded - {} commands registered.", VERSION,
                commandManager.getCommands().size());
    }
}
