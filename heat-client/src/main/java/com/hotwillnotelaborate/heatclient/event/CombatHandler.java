package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.HeatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CombatHandler {

    private static Minecraft mc = Minecraft.getMinecraft();
    public static boolean criticals = false;
    public static boolean superKnockback = false;

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (mc.thePlayer == null || event.entity != mc.thePlayer) return;
        if (criticals) doCriticals();
        if (superKnockback) doSuperKnockback();
    }

    private void doCriticals() {
        try {
            double px = mc.thePlayer.posX;
            double py = mc.thePlayer.posY;
            double pz = mc.thePlayer.posZ;
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(px, py + 0.0625, pz, false));
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(px, py, pz, false));
        } catch (Exception ignored) {}
    }

    private void doSuperKnockback() {
        try {
            EntityPlayerSP p = mc.thePlayer;
            p.sendQueue.addToSendQueue(new C0BPacketEntityAction(p, C0BPacketEntityAction.Action.STOP_SPRINTING));
            p.sendQueue.addToSendQueue(new C0BPacketEntityAction(p, C0BPacketEntityAction.Action.START_SPRINTING));
            p.sendQueue.addToSendQueue(new C0BPacketEntityAction(p, C0BPacketEntityAction.Action.STOP_SPRINTING));
            p.sendQueue.addToSendQueue(new C0BPacketEntityAction(p, C0BPacketEntityAction.Action.START_SPRINTING));
            p.setSprinting(true);
        } catch (Exception ignored) {}
    }
}
