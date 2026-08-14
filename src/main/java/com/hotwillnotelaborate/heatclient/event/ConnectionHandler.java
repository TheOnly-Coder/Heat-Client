package com.hotwillnotelaborate.heatclient.event;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.lang.reflect.Field;

public class ConnectionHandler {

    private static Field channelField;
    private static boolean fieldResolved = false;

    private static Channel getChannel(NetworkManager nm) {
        if (!fieldResolved) {
            fieldResolved = true;
            for (String name : new String[]{"field_150744_m", "channel"}) {
                try {
                    Field f = NetworkManager.class.getDeclaredField(name);
                    f.setAccessible(true);
                    channelField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
            }
        }
        if (channelField == null) return null;
        try { return (Channel) channelField.get(nm); }
        catch (Exception e) { return null; }
    }

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            Object nm = event.manager;
            if (nm == null) return;
            Channel ch = getChannel((NetworkManager) nm);
            if (ch == null) return;
            ChannelPipeline pipeline = ch.pipeline();
            if (pipeline.get("heat_handler") == null) {
                pipeline.addBefore("packet_handler", "heat_handler", new PacketHandler());
                PacketHandler.setInjected(true);
            }
        } catch (Exception e) {
            com.hotwillnotelaborate.heatclient.HeatClient.logger.error("Failed to inject packet handler", e);
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        PacketHandler.releaseBlink();
        PacketHandler.setInjected(false);
    }
}