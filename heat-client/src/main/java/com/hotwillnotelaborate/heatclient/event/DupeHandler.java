package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;

import java.util.List;

public class DupeHandler {

    public static boolean enabled = false;
    // Default: 1 tick after item enters pickup range
    public static int tickDelay = 1;
    public static boolean pending = false;
    private static int countdown = -1;
    private static final float PICKUP_RANGE = 1.8f;
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

    private static void disconnect(final Minecraft mc) {
        // Schedule the disconnect on the client thread via addScheduledTask.
        // This runs at the START of the next game loop iteration, BEFORE
        // the tick and BEFORE the render. Using the vanilla Save and Quit
        // code path (sendQuittingDisconnectingPacket + loadWorld(null)):
        //   1. sendQuittingDisconnectingPacket tells the server to stop
        //   2. loadWorld(null) calls integratedServer.stopServer() which
        //      BLOCKS until the server thread finishes saving
        //   3. Then theWorld is set to null, main menu shown
        //   4. Next render frame: theWorld is null, render is skipped
        // No concurrent GPU access = no Mesa driver crash.
        try {
            mc.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mc.theWorld != null) {
                            mc.theWorld.sendQuittingDisconnectingPacket();
                        }
                        mc.loadWorld(null);
                    } catch (Exception e) {
                        // If loadWorld fails, try at least showing main menu
                        try {
                            mc.displayGuiScreen(
                                new net.minecraft.client.gui.GuiMainMenu());
                        } catch (Exception ignored) {}
                    }
                }
            });
        } catch (Exception e) {
            // addScheduledTask itself failed (shouldn't happen)
        }
        enabled = false;
        reset();
    }
}
