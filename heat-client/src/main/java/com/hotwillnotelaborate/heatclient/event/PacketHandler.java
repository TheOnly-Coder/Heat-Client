package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.HeatClient;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class PacketHandler extends ChannelDuplexHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final LinkedList<Packet<?>> blinkQueue = new LinkedList<>();
    private static boolean injected = false;

    // ---- Flags ----
    public static boolean velocity = false;
    public static boolean noFall = false;
    public static boolean noSwing = false;
    public static boolean blink = false;
    public static boolean antiExploit = false;

    // ---- Reflected fields ----
    private static Field VEL_ENTITY_ID;
    private static Field C03_ON_GROUND;
    private static Field EXP_MOTION_X, EXP_MOTION_Y, EXP_MOTION_Z;
    static {
        try {
            VEL_ENTITY_ID = S12PacketEntityVelocity.class.getDeclaredField("field_149417_a");
            VEL_ENTITY_ID.setAccessible(true);
        } catch (Exception e) {
            try { VEL_ENTITY_ID = S12PacketEntityVelocity.class.getDeclaredField("entityId"); VEL_ENTITY_ID.setAccessible(true); } catch (Exception e2) {}
        }
        try {
            C03_ON_GROUND = C03PacketPlayer.class.getDeclaredField("field_149474_g");
            C03_ON_GROUND.setAccessible(true);
        } catch (Exception e) {
            try { C03_ON_GROUND = C03PacketPlayer.class.getDeclaredField("onGround"); C03_ON_GROUND.setAccessible(true); } catch (Exception e2) {}
        }
        try {
            Class<?> cls = S27PacketExplosion.class;
            EXP_MOTION_X = cls.getDeclaredField("field_149152_f"); EXP_MOTION_X.setAccessible(true);
            EXP_MOTION_Y = cls.getDeclaredField("field_149153_g"); EXP_MOTION_Y.setAccessible(true);
            EXP_MOTION_Z = cls.getDeclaredField("field_149159_h"); EXP_MOTION_Z.setAccessible(true);
        } catch (Exception e) {
            try {
                Class<?> cls = S27PacketExplosion.class;
                EXP_MOTION_X = cls.getDeclaredField("motionX"); EXP_MOTION_X.setAccessible(true);
                EXP_MOTION_Y = cls.getDeclaredField("motionY"); EXP_MOTION_Y.setAccessible(true);
                EXP_MOTION_Z = cls.getDeclaredField("motionZ"); EXP_MOTION_Z.setAccessible(true);
            } catch (Exception e2) {}
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet && mc.thePlayer != null) {
            if (velocity && msg instanceof S12PacketEntityVelocity) {
                try {
                    int eid = VEL_ENTITY_ID.getInt(msg);
                    if (eid == mc.thePlayer.getEntityId()) return;
                } catch (Exception ignored) {}
            }
            if (antiExploit && msg instanceof S27PacketExplosion) {
                try {
                    if (EXP_MOTION_X != null) EXP_MOTION_X.setFloat(msg, 0f);
                    if (EXP_MOTION_Y != null) EXP_MOTION_Y.setFloat(msg, 0f);
                    if (EXP_MOTION_Z != null) EXP_MOTION_Z.setFloat(msg, 0f);
                } catch (Exception ignored) {}
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet && mc.thePlayer != null) {
            if (noFall && msg instanceof C03PacketPlayer) {
                try {
                    if (McHelper.getFallDistance(mc.thePlayer) > 2.0f && C03_ON_GROUND != null) {
                        C03_ON_GROUND.setBoolean(msg, true);
                    }
                } catch (Exception ignored) {}
            }
            if (blink && msg instanceof C03PacketPlayer) {
                blinkQueue.add((Packet<?>) msg);
                return;
            }
            if (noSwing && msg instanceof C0APacketAnimation) {
                return;
            }
        }
        super.write(ctx, msg, promise);
    }

    public static void releaseBlink() {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
        Packet<?> pkt;
        while ((pkt = blinkQueue.poll()) != null) {
            mc.thePlayer.sendQueue.addToSendQueue(pkt);
        }
        blinkQueue.clear();
    }
    public static void clearBlink() { blinkQueue.clear(); }
    public static int getBlinkQueueSize() { return blinkQueue.size(); }
    public static boolean isInjected() { return injected; }
    public static void setInjected(boolean v) { injected = v; }
}