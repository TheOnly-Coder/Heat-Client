package com.hotwillnotelaborate.heatclient.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

/**
 * Forces every attack to be a critical hit by keeping
 * fallDistance > 0 and clearing onGround before the server
 * processes the next hit.
 */
public class CritHandler {

    private static boolean enabled = false;

    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean v) { enabled = v; }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        // Only tweak state while the player is holding left click
        if (Mouse.isButtonDown(0)) {
            mc.thePlayer.fallDistance = 1.5f;
            mc.thePlayer.onGround = false;
        }
    }
}
