package com.hotwillnotelaborate.heatclient.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHandler {

    private static Minecraft mc = Minecraft.getMinecraft();
    public static boolean autoTool = false;

    @SubscribeEvent
    public void onLeftClick(PlayerInteractEvent event) {
        if (!autoTool || mc.thePlayer == null) return;
        if (event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        if (event.entity != mc.thePlayer) return;
        try {
            BlockPos pos = event.pos;
            IBlockState state = mc.theWorld.getBlockState(pos);
            if (state.getBlock() == Blocks.air) return;
            float bestSpeed = 1.0f;
            int bestSlot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null) continue;
                float speed = stack.getStrVsBlock(state.getBlock());
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
            if (bestSlot != -1 && bestSlot != mc.thePlayer.inventory.currentItem) {
                mc.thePlayer.inventory.currentItem = bestSlot;
            }
        } catch (Exception ignored) {}
    }
}