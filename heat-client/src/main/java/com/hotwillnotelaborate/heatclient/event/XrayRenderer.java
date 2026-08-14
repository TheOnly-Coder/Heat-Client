package com.hotwillnotelaborate.heatclient.event;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * Renders ore ESP highlights through walls.
 * Scans loaded chunks around the player and draws colored wireframe cubes.
 */
public class XrayRenderer {

    private static boolean enabled = false;
    private static boolean altMode = false;

    /* ---- ore registry ---- */
    private static final Set<Block> ORE_BLOCKS = new HashSet<Block>(Arrays.asList(
            Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore,
            Blocks.diamond_ore, Blocks.emerald_ore,
            Blocks.redstone_ore, Blocks.lit_redstone_ore,
            Blocks.lapis_ore
    ));

    private static final Map<Block, float[]> ORE_COLORS = new HashMap<Block, float[]>();
    static {
        ORE_COLORS.put(Blocks.coal_ore, new float[]{0.45f, 0.45f, 0.45f});
        ORE_COLORS.put(Blocks.iron_ore, new float[]{0.75f, 0.65f, 0.50f});
        ORE_COLORS.put(Blocks.gold_ore, new float[]{1.0f, 0.85f, 0.0f});
        ORE_COLORS.put(Blocks.diamond_ore, new float[]{0.0f, 0.9f, 1.0f});
        ORE_COLORS.put(Blocks.emerald_ore, new float[]{0.1f, 1.0f, 0.4f});
        ORE_COLORS.put(Blocks.redstone_ore, new float[]{1.0f, 0.15f, 0.15f});
        ORE_COLORS.put(Blocks.lit_redstone_ore, new float[]{1.0f, 0.15f, 0.15f});
        ORE_COLORS.put(Blocks.lapis_ore, new float[]{0.25f, 0.35f, 0.95f});
    }

    private static final int SCAN_RADIUS = 6;
    private static final int Y_MIN = 5;
    private static final int Y_MAX = 79;

    /* ---- cache ---- */
    private static final Map<Long, List<int[]>> chunkCache = new HashMap<Long, List<int[]>>();
    private static final Queue<int[]> scanQueue = new LinkedList<int[]>();
    private static int lastPlayerCX = Integer.MIN_VALUE;
    private static int lastPlayerCZ = Integer.MIN_VALUE;
    private static int scanCooldown = 0;

    /* ---- public API ---- */
    public static boolean isEnabled() { return enabled; }
    public static boolean isAltMode() { return altMode; }
    public static void setEnabled(boolean v) {
        enabled = v;
        if (!v) { chunkCache.clear(); scanQueue.clear(); }
    }
    public static void setMode(boolean alt) {
        altMode = alt;
    }

    /* ================================================================
     *  Chunk scanning  –  runs a few chunks per tick
     * =============================================================== */

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!enabled) return;

        // Keep fly speed applied
        int pcx = (int) Math.floor(mc.thePlayer.posX / 16.0);
        int pcz = (int) Math.floor(mc.thePlayer.posZ / 16.0);

        // Rebuild queue when player moves to a new chunk
        if (pcx != lastPlayerCX || pcz != lastPlayerCZ) {
            lastPlayerCX = pcx;
            lastPlayerCZ = pcz;
            rebuildQueue(pcx, pcz);
        }

        // Scan 3 chunks per tick
        for (int i = 0; i < 3 && !scanQueue.isEmpty(); i++) {
            int[] coord = scanQueue.poll();
            int cx = coord[0];
            int cz = coord[1];
            long key = cx * 31L + cz;
            Chunk chunk = mc.theWorld.getChunkFromChunkCoords(cx, cz);
            List<int[]> ores = new ArrayList<int[]>();
            for (int y = Y_MIN; y <= Y_MAX; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Block b = chunk.getBlock(x, y, z);
                        if (ORE_BLOCKS.contains(b)) {
                            ores.add(new int[]{
                                    cx * 16 + x, y, cz * 16 + z
                            });
                        }
                    }
                }
            }
            chunkCache.put(key, ores);
        }
    }

    private void rebuildQueue(int pcx, int pcz) {
        scanQueue.clear();
        Set<Long> validKeys = new HashSet<Long>();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                if (dx * dx + dz * dz > SCAN_RADIUS * SCAN_RADIUS) continue;
                int cx = pcx + dx;
                int cz = pcz + dz;
                long key = cx * 31L + cz;
                validKeys.add(key);
                scanQueue.add(new int[]{cx, cz});
            }
        }
        // Remove cache entries for out-of-range chunks
        Iterator<Map.Entry<Long, List<int[]>>> it = chunkCache.entrySet().iterator();
        while (it.hasNext()) {
            if (!validKeys.contains(it.next().getKey())) {
                it.remove();
            }
        }
    }

    /* ================================================================
     *  Rendering  –  draw ore highlights
     * =============================================================== */

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        float partialTicks = event.partialTicks;

        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        // Position relative to camera
        double relX = mc.thePlayer.posX - camX;
        double relY = mc.thePlayer.posY - camY;
        double relZ = mc.thePlayer.posZ - camZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-camX, -camY, -camZ);

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        if (altMode) {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
        } else {
            GL11.glLineWidth(2.0f);
        }

        for (List<int[]> ores : chunkCache.values()) {
            for (int[] pos : ores) {
                float[] col = getOreColor(pos);
                if (col == null) continue;

                double px = pos[0] + 0.5;
                double py = pos[1] + 0.5;
                double pz = pos[2] + 0.5;

                if (altMode) {
                    // Semi-transparent filled quads
                    GlStateManager.color(col[0], col[1], col[2], 0.15f);
                    drawFilledBox(px - 0.5, py - 0.5, pz - 0.5, px + 0.5, py + 0.5, pz + 0.5);
                } else {
                    // Bright wireframe
                    GlStateManager.color(col[0], col[1], col[2], 0.85f);
                    drawOutlinedBox(px - 0.5, py - 0.5, pz - 0.5, px + 0.5, py + 0.5, pz + 0.5);
                }
            }
        }

        GlStateManager.depthMask(true);
        if (altMode) {
            GlStateManager.disableBlend();
        }
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private float[] getOreColor(int[] pos) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return null;
        Block b = mc.theWorld.getBlockState(new BlockPos(pos[0], pos[1], pos[2])).getBlock();
        float[] col = ORE_COLORS.get(b);
        return col != null ? col : new float[]{1f, 1f, 1f};
    }

    /* ---- GL drawing helpers ---- */

    private static void drawOutlinedBox(double x1, double y1, double z1,
                                         double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_LINES);
        // Bottom face
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y1, z1);
        // Top face
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z1); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y2, z2); GL11.glVertex3d(x1, y2, z1);
        // Vertical edges
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glEnd();
    }

    private static void drawFilledBox(double x1, double y1, double z1,
                                       double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_QUADS);
        // Bottom
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        // Top
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y2, z1);
        // North
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y2, z1); GL11.glVertex3d(x2, y1, z1);
        // South
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        // West
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y2, z2); GL11.glVertex3d(x1, y2, z1);
        // East
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y1, z2);
        GL11.glEnd();
    }
}
