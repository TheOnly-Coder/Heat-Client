package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Random;

public class TickHandler {

    private static final Random RAND = new Random();

    public static boolean fullbright = false;
    public static float savedGamma = 0;
    public static boolean antiAFK = false;
    private static int afkTimer = 0;
    public static boolean autoRespawn = false;
    public static boolean sprint = false;
    public static boolean derp = false;
    public static boolean spammer = false;
    private static int spamTimer = 0;
    public static boolean triggerbot = false;
    private static long lastTriggerTime = 0;
    public static boolean autoClicker = false;
    private static int cpsMin = 8, cpsMax = 12;
    private static long nextClickTime = 0;
    public static boolean fastPlace = false;
    public static boolean nuker = false;
    private static float nukerRange = 4.5f;
    private static int nukerDelay = 0;
    public static boolean chestStealer = false;
    private static int stealerTimer = 0;
    public static boolean noBob = false;
    public static boolean inventoryWalk = false;
    public static boolean autoBow = false;
    public static boolean timer = false;
    private static float timerSpeed = 2.0f;
    public static boolean fastUse = false;
    public static boolean fastBreak = false;
    public static boolean noHurtCam = false;
    public static boolean antiBlind = false;

    public static void setCps(int min, int max) { cpsMin = Math.max(1, min); cpsMax = Math.max(1, max); }
    public static int[] getCps() { return new int[]{cpsMin, cpsMax}; }
    public static void setNukerRange(float r) { nukerRange = Math.max(1, Math.min(6, r)); }
    public static void setTimerSpeed(float s) { timerSpeed = Math.max(0.1f, Math.min(20f, s)); }
    public static float getTimerSpeed() { return timerSpeed; }
    public static void resetAfkTimer() { afkTimer = 0; }
    public static void resetSpamTimer() { spamTimer = 0; }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (fullbright) doFullbright(mc);
        if (antiAFK) doAntiAFK(mc);
        if (autoRespawn) doAutoRespawn(mc);
        if (sprint) doSprint(mc);
        if (derp) doDerp(mc);
        if (spammer) doSpammer(mc);
        if (triggerbot) doTriggerbot(mc);
        if (autoClicker) doAutoClicker(mc);
        if (fastPlace) doFastPlace(mc);
        if (nuker) doNuker(mc);
        if (chestStealer) doChestStealer(mc);
        if (noBob) doNoBob(mc);
        if (inventoryWalk) doInventoryWalk(mc);
        if (autoBow) doAutoBow(mc);
        if (timer) doTimer(mc);
        if (fastUse) doFastUse(mc);
        if (fastBreak) doFastBreak(mc);
        if (noHurtCam) doNoHurtCam(mc);
        if (antiBlind) doAntiBlind(mc);
    }

    private void doFullbright(Minecraft mc) {
        float cur = McHelper.getGamma();
        if (cur < 100f) McHelper.setGamma(100f);
    }
    public static void disableFullbright(Minecraft mc) {
        McHelper.setGamma(savedGamma);
    }

    private void doAntiAFK(Minecraft mc) {
        afkTimer++;
        if (afkTimer >= 60) { afkTimer = 0;
            if (McHelper.isOnGround(mc.thePlayer)) McHelper.jump(mc.thePlayer);
        }
    }

    private void doAutoRespawn(Minecraft mc) {
        if (McHelper.isDead(mc.thePlayer)) {
            mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }

    private void doSprint(Minecraft mc) {
        float fwd = McHelper.getMoveForward(mc.thePlayer);
        if (fwd > 0 && !McHelper.isSneaking(mc.thePlayer)
                && !McHelper.isInWater(mc.thePlayer)) {
            McHelper.setSprinting(mc.thePlayer, true);
        }
    }

    private void doDerp(Minecraft mc) {
        McHelper.setYaw(mc.thePlayer, McHelper.getYaw(mc.thePlayer) + RAND.nextFloat() * 360f);
        McHelper.setPitch(mc.thePlayer, RAND.nextFloat() * 180f - 90f);
    }

    private void doSpammer(Minecraft mc) {
        spamTimer++;
        if (spamTimer >= 40) { spamTimer = 0;
            mc.thePlayer.sendChatMessage("[Heat] Heat Client v1.5.0 - github.com/TheOnly-Coder/Heat-Client");
        }
    }

    private void doTriggerbot(Minecraft mc) {
        long now = System.currentTimeMillis();
        if (now - lastTriggerTime < 150) return;
        try {
            MovingObjectPosition mop = (MovingObjectPosition) McHelper.MC_OBJECT_MOUSE_OVER.get(mc);
            if (mop == null) return;
            Object type = McHelper.MOP_TYPE_OF_HIT.get(mop);
            if (type == null) return;
            if (type.toString().contains("ENTITY")) {
                Entity target = (Entity) McHelper.MOP_ENTITY_HIT.get(mop);
                if (target != null && target instanceof EntityLivingBase
                        && !McHelper.isDead(target) && target != mc.thePlayer
                        && mc.thePlayer.getDistanceToEntity(target) <= 4.0) {
                    mc.playerController.attackEntity(mc.thePlayer, target);
                    mc.thePlayer.swingItem();
                    lastTriggerTime = now;
                }
            }
        } catch (Exception ignored) {}
    }

    private void doAutoClicker(Minecraft mc) {
        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;
        if (Mouse.isButtonDown(0)) McHelper.clickMouse(mc);
        int cps = cpsMin + RAND.nextInt(Math.max(1, cpsMax - cpsMin + 1));
        nextClickTime = now + (1000 / Math.max(1, cps));
    }

    private void doFastPlace(Minecraft mc) {
        try { McHelper.MC_RIGHT_CLICK_DELAY.setInt(mc, 0); } catch (Exception ignored) {}
    }

    private void doNuker(Minecraft mc) {
        nukerDelay++;
        if (nukerDelay < 2) return;
        nukerDelay = 0;
        try {
            double px = McHelper.getPosX(mc.thePlayer);
            double py = McHelper.getPosY(mc.thePlayer);
            double pz = McHelper.getPosZ(mc.thePlayer);
            float r = nukerRange;
            int minX = (int) Math.floor(px - r), maxX = (int) Math.floor(px + r);
            int minY = (int) Math.floor(py - r), maxY = (int) Math.floor(py + r);
            int minZ = (int) Math.floor(pz - r), maxZ = (int) Math.floor(pz + r);
            for (int x = minX; x <= maxX && x < minX + 3; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = x + 0.5 - px, dy = y + 0.5 - py, dz = z + 0.5 - pz;
                        if (dx*dx + dy*dy + dz*dz > r*r) continue;
                        BlockPos pos = new BlockPos(x, y, z);
                        Block b = mc.theWorld.getBlockState(pos).getBlock();
                        if (b != Blocks.air && b != Blocks.bedrock
                                && b != Blocks.water && b != Blocks.lava
                                && b != Blocks.flowing_water && b != Blocks.flowing_lava) {
                            mc.playerController.clickBlock(pos, EnumFacing.UP);
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void doChestStealer(Minecraft mc) {
        try {
            Object container = McHelper.EP_OPEN_CONTAINER.get(mc.thePlayer);
            if (!(container instanceof ContainerChest)) return;
            ContainerChest chest = (ContainerChest) container;
            stealerTimer++;
            if (stealerTimer < 2) return;
            stealerTimer = 0;
            List stacks = McHelper.getContainerInventoryStacks(chest);
            if (stacks == null) return;
            int lowerSize = chest.inventorySlots.size() - stacks.size();
            for (Slot slot : chest.inventorySlots) {
                if (slot == null || !slot.getHasStack()) continue;
                int id = slot.slotNumber;
                if (id < stacks.size()) continue;
                mc.playerController.windowClick(chest.windowId, id, 0, 1, mc.thePlayer);
                return;
            }
            mc.thePlayer.closeScreen();
        } catch (Exception ignored) {}
    }

    private void doNoBob(Minecraft mc) {
        try {
            McHelper.ENTITY_DISTANCE_WALKED.setFloat(mc.thePlayer, 0f);
        } catch (Exception ignored) {}
    }

    private void doInventoryWalk(Minecraft mc) {
        if (mc.currentScreen == null) return;
        try {
            Object gs = McHelper.MC_GAME_SETTINGS.get(mc);
            Object kbf = McHelper.GS_KEY_BIND_FORWARD.get(gs);
            Object kbb = McHelper.GS_KEY_BIND_BACK.get(gs);
            Object kbl = McHelper.GS_KEY_BIND_LEFT.get(gs);
            Object kbr = McHelper.GS_KEY_BIND_RIGHT.get(gs);
            Object kbj = McHelper.GS_KEY_BIND_JUMP.get(gs);
            Object kbs = McHelper.GS_KEY_BIND_SPRINT.get(gs);
            McHelper.setKeyBindingPressed(kbf, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbf)));
            McHelper.setKeyBindingPressed(kbb, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbb)));
            McHelper.setKeyBindingPressed(kbl, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbl)));
            McHelper.setKeyBindingPressed(kbr, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbr)));
            McHelper.setKeyBindingPressed(kbj, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbj)));
            McHelper.setKeyBindingPressed(kbs, org.lwjgl.input.Keyboard.isKeyDown(getKeyCode(kbs)));
        } catch (Exception ignored) {}
    }
    private static int getKeyCode(Object keyBinding) {
        try {
            java.lang.reflect.Field f = keyBinding.getClass().getDeclaredField("field_74512_d");
            f.setAccessible(true); return f.getInt(keyBinding);
        } catch (Exception e) {
            try {
                java.lang.reflect.Field f = keyBinding.getClass().getDeclaredField("keyCode");
                f.setAccessible(true); return f.getInt(keyBinding);
            } catch (Exception e2) { return 0; }
        }
    }

    private void doAutoBow(Minecraft mc) {
        try {
            if (!mc.thePlayer.isUsingItem()) return;
            if (mc.thePlayer.getHeldItem() == null) return;
            if (mc.thePlayer.getHeldItem().getItem() != net.minecraft.init.Items.bow) return;
            if (mc.thePlayer.getItemInUseDuration() < 20) return;
            mc.thePlayer.stopUsingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C07PacketPlayerDigging(
                    net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    net.minecraft.util.BlockPos.ORIGIN, net.minecraft.util.EnumFacing.UP));
        } catch (Exception ignored) {}
    }

    private void doTimer(Minecraft mc) {
        try {
            net.minecraft.util.Timer t = McHelper.getTimer(mc);
            if (t != null) McHelper.setTimerSpeed(t, timerSpeed);
        } catch (Exception ignored) {}
    }

    public static void disableTimer(Minecraft mc) {
        try {
            net.minecraft.util.Timer t = McHelper.getTimer(mc);
            if (t != null) McHelper.setTimerSpeed(t, 1.0f);
        } catch (Exception ignored) {}
    }

    private void doFastUse(Minecraft mc) {
        try {
            if (!mc.thePlayer.isUsingItem()) return;
            if (mc.thePlayer.getItemInUseDuration() > 14) {
                for (int i = 0; i < 20; i++) {
                    boolean onGround = McHelper.isOnGround(mc.thePlayer);
                    mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C03PacketPlayer(onGround));
                }
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
            }
        } catch (Exception ignored) {}
    }

    private void doFastBreak(Minecraft mc) {
        try {
            McHelper.setPcBlockHitDelay(mc.playerController, 0);
            float v = McHelper.getPcCurBlockDamage(mc.playerController);
            if (v > 0.8f) McHelper.setPcCurBlockDamage(mc.playerController, 1.0f);
        } catch (Exception ignored) {}
    }

    private void doNoHurtCam(Minecraft mc) {
        try {
            McHelper.setHurtTime(mc.thePlayer, 0);
        } catch (Exception ignored) {}
    }

    private void doAntiBlind(Minecraft mc) {
        try {
            if (mc.entityRenderer != null) {
                McHelper.setErCameraZoom(mc.entityRenderer, 1.0);
                McHelper.setErCameraYaw(mc.entityRenderer, 0.0);
            }
        } catch (Exception ignored) {}
    }
}