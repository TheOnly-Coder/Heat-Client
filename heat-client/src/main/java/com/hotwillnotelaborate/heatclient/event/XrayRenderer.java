package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.util.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Renders ore ESP highlights through walls.
 * Scans loaded chunks around the player and draws colored wireframe cubes.
 * All Minecraft field/method access uses reflection to avoid reobfuscation issues.
 */
public class XrayRenderer {

    private static boolean enabled = false;
    private static boolean altMode = false;

    /* ---- reflected fields (SRG, MCP) ---- */
    private static final Field ENTITY_POS_X = ReflectionUtil.getField(Entity.class, "field_70165_t", "posX");
    private static final Field ENTITY_POS_Y = ReflectionUtil.getField(Entity.class, "field_70163_u", "posY");
    private static final Field ENTITY_POS_Z = ReflectionUtil.getField(Entity.class, "field_70161_v", "posZ");
    private static final Field RM_VIEWER_X  = ReflectionUtil.getField(RenderManager.class, "field_78730_l", "viewerPosX");
    private static final Field RM_VIEWER_Y  = ReflectionUtil.getField(RenderManager.class, "field_78731_m", "viewerPosY");
    private static final Field RM_VIEWER_Z  = ReflectionUtil.getField(RenderManager.class, "field_78728_n", "viewerPosZ");

    /* ---- reflected methods (SRG, MCP) ---- */
    private static final Method WORLD_GET_CHUNK = ReflectionUtil.getMethod(
            World.class, "func_72964_e", "getChunkFromChunkCoords", int.class, int.class);
    private static final Method CHUNK_GET_BLOCK = ReflectionUtil.getMethod(
            Chunk.class, "func_177438_a", "getBlock", int.class, int.class, int.class);
    private static final Method WORLD_GET_BLOCKSTATE = ReflectionUtil.getMethod(
            World.class, "func_180495_p", "getBlockState", BlockPos.class);
    private static final Method IBS_GET_BLOCK = ReflectionUtil.getMethod(
            IBlockState.class, "func_177230_c", "getBlock");

    static {
        for (Field f : new Field[]{ENTITY_POS_X, ENTITY_POS_Y, ENTITY_POS_Z,
                                   RM_VIEWER_X, RM_VIEWER_Y, RM_VIEWER_Z}) {
            f.setAccessible(true);
        }
    }

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

    /* ---- public API ---- */
    public static boolean isEnabled() { return enabled; }
    public static boolean isAltMode() { return altMode; }
    public static void setEnabled(boolean v) {
        enabled = v;
        if (!v) { chunkCache.clear(); scanQueue.clear(); }
    }
    public static void setMode(boolean alt) { altMode = alt; }

    /* ================================================================
     *  Chunk scanning  -  runs a few chunks per tick
     * =============================================================== */

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!enabled) return;

        double px = ReflectionUtil.getDouble(mc.thePlayer, ENTITY_POS_X);
        double pz = ReflectionUtil.getDouble(mc.thePlayer, ENTITY_POS_Z);
        int pcx = (int) Math.floor(px / 16.0);
        int pcz = (int) Math.floor(pz / 16.0);

        if (pcx != lastPlayerCX || pcz != lastPlayerCZ) {
            lastPlayerCX = pcx;
            lastPlayerCZ = pcz;
            rebuildQueue(pcx, pcz);
        }

        for (int i = 0; i < 3 && !scanQueue.isEmpty(); i++) {
            int[] coord = scanQueue.poll();
            int cx = coord[0];
            int cz = coord[1];
            long key = cx * 31L + cz;
            Chunk chunk = ReflectionUtil.invoke(mc.theWorld, WORLD_GET_CHUNK, cx, cz);
            List<int[]> ores = new ArrayList<int[]>();
            if (chunk != null) {
                for (int y = Y_MIN; y <= Y_MAX; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            Block b = ReflectionUtil.invoke(chunk, CHUNK_GET_BLOCK, x, y, z);
                            if (ORE_BLOCKS.contains(b)) {
                                ores.add(new int[]{cx * 16 + x, y, cz * 16 + z});
                            }
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
        Iterator<Map.Entry<Long, List<int[]>>> it = chunkCache.entrySet().iterator();
        while (it.hasNext()) {
            if (!validKeys.contains(it.next().getKey())) {
                it.remove();
            }
        }
    }

    /* ================================================================
     *  Rendering  -  draw ore highlights
     * =============================================================== */

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        RenderManager rm = mc.getRenderManager();
        double camX = ReflectionUtil.getDouble(rm, RM_VIEWER_X);
        double camY = ReflectionUtil.getDouble(rm, RM_VIEWER_Y);
        double camZ = ReflectionUtil.getDouble(rm, RM_VIEWER_Z);

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

                double bx = pos[0] + 0.5;
                double by = pos[1] + 0.5;
                double bz = pos[2] + 0.5;

                if (altMode) {
                    GlStateManager.color(col[0], col[1], col[2], 0.15f);
                    drawFilledBox(bx - 0.5, by - 0.5, bz - 0.5,
                                  bx + 0.5, by + 0.5, bz + 0.5);
                } else {
                    GlStateManager.color(col[0], col[1], col[2], 0.85f);
                    drawOutlinedBox(bx - 0.5, by - 0.5, bz - 0.5,
                                    bx + 0.5, by + 0.5, bz + 0.5);
                }
            }
        }

        GlStateManager.depthMask(true);
        if (altMode) GlStateManager.disableBlend();
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
        IBlockState state = ReflectionUtil.invoke(mc.theWorld, WORLD_GET_BLOCKSTATE,
                new BlockPos(pos[0], pos[1], pos[2]));
        if (state == null) return null;
        Block b = ReflectionUtil.invoke(state, IBS_GET_BLOCK);
        float[] col = ORE_COLORS.get(b);
        return col != null ? col : new float[]{1f, 1f, 1f};
    }

    /* ---- GL drawing helpers ---- */

    private static void drawOutlinedBox(double x1, double y1, double z1,
                                         double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z1); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y2, z2); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glEnd();
    }

    private static void drawFilledBox(double x1, double y1, double z1,
                                       double x2, double y2, double z2) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y2, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x2, y1, z2);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y1, z2);
        GL11.glVertex3d(x1, y2, z2); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x2, y1, z2);
        GL11.glEnd();
    }
}
