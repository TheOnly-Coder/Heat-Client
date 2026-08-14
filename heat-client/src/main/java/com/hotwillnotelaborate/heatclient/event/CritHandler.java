package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;

/**
 * Forces every attack to be a critical hit by keeping
 * fallDistance > 0 and clearing onGround before the server
 * processes the next hit.
 */
public class CritHandler {

    private static boolean enabled = false;

    /* ---- reflected fields (SRG name, MCP name) ---- */
    private static final Field ENTITY_FALL_DISTANCE = ReflectionUtil.getField(
            Entity.class, "field_70143_R", "fallDistance");
    private static final Field ENTITY_ON_GROUND = ReflectionUtil.getField(
            Entity.class, "field_70122_E", "onGround");

    static {
        ENTITY_FALL_DISTANCE.setAccessible(true);
        ENTITY_ON_GROUND.setAccessible(true);
    }

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
            ReflectionUtil.setFloat(mc.thePlayer, ENTITY_FALL_DISTANCE, 1.5f);
            ReflectionUtil.setBoolean(mc.thePlayer, ENTITY_ON_GROUND, false);
        }
    }
}
