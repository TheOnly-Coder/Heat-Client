package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class DupeHandler {

    public static boolean enabled = false;
    public static int tickDelay = 0;
    public static boolean pending = false;
    private static int countdown = -1;
    private static final float PICKUP_RANGE = 1.5f;

    public static void setTickDelay(int d) {
        tickDelay = Math.max(-10, Math.min(20, d));
    }
    public static void reset() {
        countdown = -1;
        pending = false;
    }

    public static void onTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!enabled || mc.thePlayer == null || mc.theWorld == null) {
            reset();
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
                if (countdown <= 0) {
                    pending = true;
                    disconnect(mc);
                    return;
                }
                pending = true;
            }
            countdown--;
            if (countdown <= 0) {
                disconnect(mc);
            }
        } else {
            countdown = -1;
            pending = false;
        }
    }

    private static void disconnect(Minecraft mc) {
        try {
            mc.theWorld.sendQuittingDisconnectingPacket();
            mc.loadWorld(null);
        } catch (Exception e) {
            try {
                mc.thePlayer.sendQueue.getNetworkManager().closeChannel(
                    new net.minecraft.util.ChatComponentText("Dupe"));
                mc.loadWorld(null);
            } catch (Exception e2) {}
        }
        reset();
    }
}
