package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class DupeHandler {

    public static boolean enabled = false;
    // Default: 1 tick after item enters pickup range
    public static int tickDelay = 1;
    public static boolean pending = false;
    private static int countdown = -1;
    private static final float PICKUP_RANGE = 1.8f;
    // Flag to disconnect on the NEXT tick
    private static boolean shouldDisconnect = false;

    public static void setTickDelay(int d) {
        tickDelay = Math.max(-10, Math.min(20, d));
    }
    public static void reset() {
        countdown = -1;
        pending = false;
        shouldDisconnect = false;
    }

    public static void onTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc.thePlayer == null || mc.theWorld == null) {
            reset();
            return;
        }

        // If disconnect was flagged last tick, do it now
        if (shouldDisconnect) {
            shouldDisconnect = false;
            pending = false;
            disconnect(mc);
            return;
        }

        if (mc.currentScreen != null) return;

        boolean foundPickup = false;
        double px = McHelper.getPosX(mc.thePlayer);
        double py = McHelper.getPosY(mc.thePlayer);
        double pz = McHelper.getPosZ(mc.thePlayer);

        List entities = McHelper.getLoadedEntities(mc.theWorld);
        for (Object obj : entities) {
            if (!(obj instanceof EntityItem)) continue;
            EntityItem item = (EntityItem) obj;
            if (McHelper.getItemPickupDelay(item) > 0) continue;

            double ix = McHelper.getPosX(item);
            double iy = McHelper.getPosY(item);
            double iz = McHelper.getPosZ(item);
            double dx = px - ix, dy = py - iy, dz = pz - iz;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist < PICKUP_RANGE) {
                foundPickup = true;
                break;
            }
        }

        if (foundPickup) {
            if (countdown < 0) {
                countdown = tickDelay;
                pending = true;
                if (countdown <= 0) {
                    shouldDisconnect = true;
                    return;
                }
            }
            countdown--;
            if (countdown <= 0) {
                shouldDisconnect = true;
            }
        } else {
            countdown = -1;
            pending = false;
        }
    }

    private static void disconnect(Minecraft mc) {
        // Just close the network channel and let Minecraft's own disconnect
        // handler (NetHandlerPlayClient.onDisconnect) do everything safely.
        // This avoids the GPU driver crash caused by manually calling
        // sendQuittingDisconnectingPacket + displayGuiScreen concurrently
        // with the server stopping on another thread.
        try {
            if (mc.thePlayer != null && mc.thePlayer.sendQueue != null
                    && mc.thePlayer.sendQueue.getNetworkManager() != null) {
                mc.thePlayer.sendQueue.getNetworkManager().closeChannel(
                    new ChatComponentText("Dupe"));
            }
        } catch (Exception e) {
            // Fallback: let Minecraft handle it naturally
        }
        enabled = false;
        reset();
    }
}
