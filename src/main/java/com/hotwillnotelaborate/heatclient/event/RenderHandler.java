package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.command.*;
import com.hotwillnotelaborate.heatclient.util.McHelper;
import com.hotwillnotelaborate.heatclient.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RenderHandler {

    public static boolean tracers = false;
    public static boolean esp = false;
    public static boolean nametags = false;
    public static boolean blockOverlay = false;
    public static boolean breadcrumbs = false;
    public static boolean itemESP = false;
    public static boolean storageESP = false;
    private static final LinkedList<double[]> crumbTrail = new LinkedList<double[]>();
    private static final int MAX_CRUMBS = 500;
    private static int crumbTimer = 0;
    private static float renderRange = 50f;
    public static void setRenderRange(float r) { renderRange = r; }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        double camX = ReflectionUtil.getDouble(mc.getRenderManager(), McHelper.RM_VIEWER_X);
        double camY = ReflectionUtil.getDouble(mc.getRenderManager(), McHelper.RM_VIEWER_Y);
        double camZ = ReflectionUtil.getDouble(mc.getRenderManager(), McHelper.RM_VIEWER_Z);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-camX, -camY, -camZ);
        if (tracers) drawTracers(mc);
        if (esp) drawESP(mc);
        if (nametags) drawNametags(mc, camX, camY, camZ);
        if (blockOverlay) drawBlockOverlay(mc);
        if (breadcrumbs) drawBreadcrumbs(mc);
        if (itemESP) drawItemESP(mc);
        if (storageESP) drawStorageESP(mc);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
        drawHUD(mc);
    }

    public static void onTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (breadcrumbs) {
            crumbTimer++;
            if (crumbTimer >= 3) { crumbTimer = 0;
                crumbTrail.add(new double[]{McHelper.getPosX(mc.thePlayer), McHelper.getPosY(mc.thePlayer), McHelper.getPosZ(mc.thePlayer)});
                while (crumbTrail.size() > MAX_CRUMBS) crumbTrail.removeFirst();
            }
        }
    }
    public static void clearBreadcrumbs() { crumbTrail.clear(); }

    private void drawTracers(Minecraft mc) {
        double px = McHelper.getPosX(mc.thePlayer);
        double py = McHelper.getPosY(mc.thePlayer) + McHelper.getEyeHeight(mc.thePlayer);
        double pz = McHelper.getPosZ(mc.thePlayer);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINES);
        for (Entity e : McHelper.getLoadedEntities(mc.theWorld)) {
            if (e == mc.thePlayer || e.isDead) continue;
            if (mc.thePlayer.getDistanceToEntity(e) > renderRange) continue;
            double ex = McHelper.getPosX(e), ey = McHelper.getPosY(e) + e.height / 2.0, ez = McHelper.getPosZ(e);
            float r = 1, g = 0.2f, b = 0.2f;
            if (e instanceof EntityPlayer) { r = 1; g = 0.5f; b = 0; }
            GL11.glColor3f(r, g, b);
            GL11.glVertex3d(px, py, pz);
            GL11.glVertex3d(ex, ey, ez);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private void drawESP(Minecraft mc) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glLineWidth(2.0f);
        for (Entity e : McHelper.getLoadedEntities(mc.theWorld)) {
            if (e == mc.thePlayer || e.isDead) continue;
            if (mc.thePlayer.getDistanceToEntity(e) > renderRange) continue;
            float w = McHelper.getWidth(e), h = McHelper.getHeight(e);
            double x = McHelper.getPosX(e) - w/2.0, y = McHelper.getPosY(e), z = McHelper.getPosZ(e) - w/2.0;
            float r = 1, g = 0.2f, b = 0.2f;
            if (e instanceof EntityPlayer) { r = 1; g = 1; b = 0; }
            else if (e instanceof EntityLivingBase) { r = 1; g = 0.3f; b = 0.3f; }
            GlStateManager.color(r, g, b, 0.6f);
            drawOutlinedBox(x, y, z, x+w, y+h, z+w);
        }
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private void drawNametags(Minecraft mc, double camX, double camY, double camZ) {
        FontRenderer fr = McHelper.getFontRenderer();
        if (fr == null) return;
        double playerYaw = Math.toRadians(McHelper.getYaw(mc.thePlayer));
        double playerPitch = Math.toRadians(McHelper.getPitch(mc.thePlayer));
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        for (Entity e : McHelper.getLoadedEntities(mc.theWorld)) {
            if (e == mc.thePlayer || e.isDead) continue;
            if (mc.thePlayer.getDistanceToEntity(e) > renderRange) continue;
            double x = McHelper.getPosX(e), y = McHelper.getPosY(e) + e.height + 0.3, z = McHelper.getPosZ(e);
            double dx = x - camX, dy = y - camY, dz = z - camZ;
            double cos = Math.cos(-playerYaw), sin = Math.sin(-playerYaw);
            double nx = dx*cos - dz*sin, nz = dx*sin + dz*cos;
            double cos2 = Math.cos(-playerPitch), sin2 = Math.sin(-playerPitch);
            double ny = dy*cos2 - nz*sin2, nz2 = dy*sin2 + nz*cos2;
            if (nz2 < 0.1) continue;
            float fov = McHelper.getFov();
            float scale = (float) (McHelper.getDisplayHeight() / (2.0 * Math.tan(Math.toRadians(fov) / 2.0)) / nz2);
            float sx = (float) ((McHelper.getDisplayWidth() / 2.0) + nx * scale);
            float sy = (float) ((McHelper.getDisplayHeight() / 2.0) - ny * scale);
            String name = McHelper.getDisplayName(e).getFormattedText();
            float hpPct = 1.0f;
            if (e instanceof EntityLivingBase) {
                hpPct = McHelper.getHealth((EntityLivingBase)e) / McHelper.getMaxHealth((EntityLivingBase)e);
                name += " " + (int)McHelper.getHealth((EntityLivingBase)e) + "hp";
            }
            float barW = fr.getStringWidth(name) + 4;
            float bgY = sy - 2;
            GlStateManager.disableTexture2D();
            GlStateManager.color(0, 0, 0, 0.5f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(sx-2, bgY-2); GL11.glVertex2f(sx-2+barW, bgY-2);
            GL11.glVertex2f(sx-2+barW, bgY); GL11.glVertex2f(sx-2, bgY);
            GL11.glEnd();
            float hpW = barW * Math.max(0, Math.min(1, hpPct));
            GlStateManager.color(hpPct > 0.5 ? 0 : 1, hpPct > 0.5 ? 1 : (hpPct*2), 0, 0.8f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(sx-2, bgY-2); GL11.glVertex2f(sx-2+hpW, bgY-2);
            GL11.glVertex2f(sx-2+hpW, bgY); GL11.glVertex2f(sx-2, bgY);
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            GlStateManager.color(1, 1, 1, 1);
            fr.drawStringWithShadow(name, sx, sy - 10, 0);
        }
        GlStateManager.enableDepth();
    }

    private void drawBlockOverlay(Minecraft mc) {
        try {
            MovingObjectPosition mop = (MovingObjectPosition) McHelper.MC_OBJECT_MOUSE_OVER.get(mc);
            if (mop == null) return;
            Object type = McHelper.MOP_TYPE_OF_HIT.get(mop);
            if (type == null || !type.toString().contains("BLOCK")) return;
            BlockPos pos = (BlockPos) McHelper.MOP_BLOCK_POS.get(mop);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glLineWidth(2.0f);
            GlStateManager.color(0, 1, 1, 0.5f);
            drawOutlinedBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY()+1, pos.getZ()+1);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);
        } catch (Exception ignored) {}
    }

    private void drawBreadcrumbs(Minecraft mc) {
        if (crumbTrail.size() < 2) return;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < crumbTrail.size(); i++) {
            double[] p = crumbTrail.get(i);
            float a = (float) i / crumbTrail.size();
            GlStateManager.color(0.3f, 0.7f, 1.0f, a);
            GL11.glVertex3d(p[0], p[1], p[2]);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private void drawItemESP(Minecraft mc) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        for (Entity e : McHelper.getLoadedEntities(mc.theWorld)) {
            if (!(e instanceof EntityItem)) continue;
            if (mc.thePlayer.getDistanceToEntity(e) > renderRange) continue;
            double x = McHelper.getPosX(e) - 0.15, y = McHelper.getPosY(e) - 0.05, z = McHelper.getPosZ(e) - 0.15;
            GlStateManager.color(1.0f, 0.8f, 0.2f, 0.5f);
            drawOutlinedBox(x, y, z, x + 0.3, y + 0.3, z + 0.3);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    private void drawStorageESP(Minecraft mc) {
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glLineWidth(1.5f);
            java.util.List teList = (java.util.List) McHelper.WORLD_LOADED_TILE_ENTITY_LIST.get(mc.theWorld);
            double px = McHelper.getPosX(mc.thePlayer);
            double py = McHelper.getPosY(mc.thePlayer);
            double pz = McHelper.getPosZ(mc.thePlayer);
            for (Object te : teList) {
                net.minecraft.tileentity.TileEntity tile = (net.minecraft.tileentity.TileEntity) te;
                double dx = tile.getPos().getX() + 0.5 - px;
                double dy = tile.getPos().getY() + 0.5 - py;
                double dz = tile.getPos().getZ() + 0.5 - pz;
                if (dx * dx + dy * dy + dz * dz > renderRange * renderRange) continue;
                float r = 1, g = 1, b = 1;
                if (tile instanceof net.minecraft.tileentity.TileEntityFurnace) { r = 1; g = 0.6f; b = 0; }
                else if (tile instanceof net.minecraft.tileentity.TileEntityChest) { r = 1; g = 1; b = 0; }
                else if (tile instanceof net.minecraft.tileentity.TileEntityEnderChest) { r = 0.6f; g = 0.2f; b = 1; }
                else if (tile instanceof net.minecraft.tileentity.TileEntityHopper) { r = 0.5f; g = 0.5f; b = 0.5f; }
                else if (tile instanceof net.minecraft.tileentity.TileEntityBrewingStand) { r = 0.2f; g = 0.4f; b = 1; }
                else continue;
                double bx = tile.getPos().getX(), by = tile.getPos().getY(), bz = tile.getPos().getZ();
                GlStateManager.color(r, g, b, 0.5f);
                drawOutlinedBox(bx, by, bz, bx + 1, by + 1, bz + 1);
            }
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);
        } catch (Exception ignored) {}
    }

    private void drawHUD(Minecraft mc) {
        List<String> active = new ArrayList<String>();
        if (CommandFly.isFlying())
            active.add(EnumChatFormatting.GREEN + "Fly" + EnumChatFormatting.GRAY + " " + CommandFly.getSpeed() + "x");
        if (XrayRenderer.isEnabled())
            active.add(EnumChatFormatting.AQUA + "X-Ray" + EnumChatFormatting.GRAY + " " + (XrayRenderer.isAltMode() ? "alt" : "default"));
        if (TickHandler.fullbright) active.add(EnumChatFormatting.YELLOW + "Fullbright");
        if (TickHandler.antiAFK) active.add(EnumChatFormatting.YELLOW + "AntiAFK");
        if (TickHandler.autoRespawn) active.add(EnumChatFormatting.YELLOW + "AutoRespawn");
        if (TickHandler.sprint) active.add(EnumChatFormatting.YELLOW + "Sprint");
        if (TickHandler.derp) active.add(EnumChatFormatting.YELLOW + "Derp");
        if (TickHandler.spammer) active.add(EnumChatFormatting.YELLOW + "Spammer");
        if (TickHandler.triggerbot) active.add(EnumChatFormatting.RED + "TriggerBot");
        if (TickHandler.autoClicker) active.add(EnumChatFormatting.RED + "AutoClicker");
        if (TickHandler.fastPlace) active.add(EnumChatFormatting.YELLOW + "FastPlace");
        if (TickHandler.nuker) active.add(EnumChatFormatting.RED + "Nuker");
        if (TickHandler.chestStealer) active.add(EnumChatFormatting.YELLOW + "ChestStealer");
        if (tracers) active.add(EnumChatFormatting.AQUA + "Tracers");
        if (esp) active.add(EnumChatFormatting.AQUA + "ESP");
        if (nametags) active.add(EnumChatFormatting.AQUA + "NameTags");
        if (blockOverlay) active.add(EnumChatFormatting.AQUA + "BlockOverlay");
        if (itemESP) active.add(EnumChatFormatting.AQUA + "ItemESP");
        if (storageESP) active.add(EnumChatFormatting.AQUA + "StorageESP");
        if (breadcrumbs) active.add(EnumChatFormatting.AQUA + "Breadcrumbs");
        if (TickHandler.noBob) active.add(EnumChatFormatting.YELLOW + "NoBob");
        if (TickHandler.inventoryWalk) active.add(EnumChatFormatting.YELLOW + "InventoryWalk");
        if (TickHandler.autoBow) active.add(EnumChatFormatting.YELLOW + "AutoBow");
        if (TickHandler.timer) active.add(EnumChatFormatting.YELLOW + "Timer" + EnumChatFormatting.GRAY + " " + TickHandler.getTimerSpeed() + "x");
        if (active.isEmpty()) return;
        FontRenderer fr = mc.fontRendererObj;
        int x = mc.displayWidth - 4, y = 4;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (String line : active) {
            int w = fr.getStringWidth(line);
            GlStateManager.disableTexture2D();
            GlStateManager.color(0, 0, 0, 0.4f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x-w-4, y-1); GL11.glVertex2f(x+2, y-1);
            GL11.glVertex2f(x+2, y+11); GL11.glVertex2f(x-w-4, y+11);
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            fr.drawStringWithShadow(line, x-w-2, y, 0);
            y += 14;
        }
        GlStateManager.disableBlend();
    }

    private static void drawOutlinedBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x1,y1,z1); GL11.glVertex3d(x2,y1,z1);
        GL11.glVertex3d(x2,y1,z1); GL11.glVertex3d(x2,y1,z2);
        GL11.glVertex3d(x2,y1,z2); GL11.glVertex3d(x1,y1,z2);
        GL11.glVertex3d(x1,y1,z2); GL11.glVertex3d(x1,y1,z1);
        GL11.glVertex3d(x1,y2,z1); GL11.glVertex3d(x2,y2,z1);
        GL11.glVertex3d(x2,y2,z1); GL11.glVertex3d(x2,y2,z2);
        GL11.glVertex3d(x2,y2,z2); GL11.glVertex3d(x1,y2,z2);
        GL11.glVertex3d(x1,y2,z2); GL11.glVertex3d(x1,y2,z1);
        GL11.glVertex3d(x2,y1,z1); GL11.glVertex3d(x2,y2,z1);
        GL11.glVertex3d(x2,y1,z2); GL11.glVertex3d(x2,y2,z2);
        GL11.glVertex3d(x1,y1,z2); GL11.glVertex3d(x1,y2,z2);
        GL11.glEnd();
    }
}