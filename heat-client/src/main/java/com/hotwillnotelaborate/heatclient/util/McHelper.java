package com.hotwillnotelaborate.heatclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized Minecraft access via reflection. ALL game-state
 * access from feature code should go through here.
 */
public class McHelper {

    // ---- Entity fields ----
    public static final Field ENTITY_POS_X = ref(Entity.class, "field_70165_t", "posX");
    public static final Field ENTITY_POS_Y = ref(Entity.class, "field_70163_u", "posY");
    public static final Field ENTITY_POS_Z = ref(Entity.class, "field_70161_v", "posZ");
    public static final Field ENTITY_ROTATION_YAW = ref(Entity.class, "field_70177_z", "rotationYaw");
    public static final Field ENTITY_ROTATION_PITCH = ref(Entity.class, "field_70179_y", "rotationPitch");
    public static final Field ENTITY_WIDTH = ref(Entity.class, "field_70130_a", "width");
    public static final Field ENTITY_HEIGHT = ref(Entity.class, "field_70131_b", "height");
    public static final Field ENTITY_IS_DEAD = ref(Entity.class, "field_70128_L", "isDead");
    public static final Field ENTITY_ON_GROUND = ref(Entity.class, "field_70122_E", "onGround");

    // ---- EntityLivingBase fields ----
    public static final Field ELB_HEALTH = ref(EntityLivingBase.class, "field_70162_ad", "health");
    public static final Field ELB_MOVE_FORWARD = ref(EntityLivingBase.class, "field_70701_bs", "moveForward");
    public static final Field DISTANCE_WALKED_MODIFIED = ref(EntityLivingBase.class, "field_70708_bq", "distanceWalkedModified");

    // ---- Minecraft fields ----
    public static final Field MC_GAME_SETTINGS = ref(Minecraft.class, "field_71474_y", "gameSettings");
    public static final Field MC_THE_PLAYER = ref(Minecraft.class, "field_71439_g", "thePlayer");
    public static final Field MC_THE_WORLD = ref(Minecraft.class, "field_71441_e", "theWorld");
    public static final Field MC_OBJECT_MOUSE_OVER = ref(Minecraft.class, "field_71476_f", "objectMouseOver");
    public static final Field MC_RENDER_MANAGER = ref(Minecraft.class, "field_175616_an", "renderManager");
    public static final Field MC_CURRENT_SCREEN = ref(Minecraft.class, "field_71462_r", "currentScreen");
    public static final Field MC_RIGHT_CLICK_DELAY = ref(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");
    public static final Field MC_FONT_RENDERER = ref(Minecraft.class, "field_71466_p", "fontRendererObj");
    public static final Field MC_DISPLAY_WIDTH = ref(Minecraft.class, "field_146274_m", "displayWidth");
    public static final Field MC_DISPLAY_HEIGHT = ref(Minecraft.class, "field_146275_n", "displayHeight");
    public static final Field MC_TIMER = ref(Minecraft.class, "field_71428_T", "timer");

    // ---- RenderManager fields ----
    public static final Field RM_VIEWER_X = ref(RenderManager.class, "field_78730_l", "viewerPosX");
    public static final Field RM_VIEWER_Y = ref(RenderManager.class, "field_78731_m", "viewerPosY");
    public static final Field RM_VIEWER_Z = ref(RenderManager.class, "field_78728_n", "viewerPosZ");
    public static final Field RM_PLAYER_YAW = ref(RenderManager.class, "field_78734_f", "playerPosX");
    public static final Field RM_PLAYER_PITCH = ref(RenderManager.class, "field_78735_g", "playerPosY");

    // ---- GameSettings fields ----
    public static final Field GS_GAMMA = refCls("net.minecraft.client.settings.GameSettings", "field_74333_Y", "gammaSetting");
    public static final Field GS_FOV = refCls("net.minecraft.client.settings.GameSettings", "field_74340_Q", "fovSetting");
    public static final Field GS_KEY_BIND_FORWARD = refCls("net.minecraft.client.settings.GameSettings", "field_74313_G", "keyBindForward");
    public static final Field GS_KEY_BIND_BACK = refCls("net.minecraft.client.settings.GameSettings", "field_74317_K", "keyBindBack");
    public static final Field GS_KEY_BIND_LEFT = refCls("net.minecraft.client.settings.GameSettings", "field_74314_H", "keyBindLeft");
    public static final Field GS_KEY_BIND_RIGHT = refCls("net.minecraft.client.settings.GameSettings", "field_74311_E", "keyBindRight");
    public static final Field GS_KEY_BIND_JUMP = refCls("net.minecraft.client.settings.GameSettings", "field_74315_I", "keyBindJump");
    public static final Field GS_KEY_BIND_SPRINT = refCls("net.minecraft.client.settings.GameSettings", "field_74312_F", "keyBindSprint");
    public static final Field KEY_BINDING_PRESSED = refCls("net.minecraft.client.settings.KeyBinding", "field_74513_e", "pressed");

    // ---- World fields ----
    public static final Field WORLD_LOADED_ENTITY_LIST = ref(World.class, "field_72996_f", "loadedEntityList");
    public static final Field WORLD_LOADED_TILE_ENTITY_LIST = ref(World.class, "field_147482_g", "loadedTileEntityList");

    // ---- EntityPlayer fields ----
    public static final Field EP_OPEN_CONTAINER = ref(EntityPlayer.class, "field_71070_bA", "openContainer");

    // ---- MovingObjectPosition fields ----
    public static final Field MOP_TYPE_OF_HIT = ref(MovingObjectPosition.class, "field_72312_a", "typeOfHit");
    public static final Field MOP_BLOCK_POS = ref(MovingObjectPosition.class, "field_72313_b", "getBlockPos");
    public static final Field MOP_ENTITY_HIT = ref(MovingObjectPosition.class, "field_72311_c", "entityHit");

    // ---- Reflected Methods (Entity) ----
    public static final Method ENTITY_IS_SNEAKING = mref(Entity.class, "func_70093_af", "isSneaking");
    public static final Method ENTITY_IS_IN_WATER = mref(Entity.class, "func_70090_H", "isInWater");
    public static final Method ENTITY_GET_EYE_HEIGHT = mref(Entity.class, "func_70047_e", "getEyeHeight");
    public static final Method ENTITY_GET_DISPLAY_NAME = mref(Entity.class, "func_145748_c_", "getDisplayName");
    public static final Method ENTITY_GET_DISTANCE = mref(Entity.class, "func_70032_d", "getDistanceToEntity", Entity.class);

    // ---- Reflected Methods (EntityLivingBase) ----
    public static final Method ELB_JUMP = mref(EntityLivingBase.class, "func_70664_aZ", "jump");
    public static final Method ELB_GET_MAX_HEALTH = mref(EntityLivingBase.class, "func_110138_aP", "getMaxHealth");

    // ---- Reflected Methods (EntityPlayerSP) ----
    public static final Method EPS_SET_SPRINTING = mref(Entity.class, "func_70031_b", "setSprinting", boolean.class);

    // ---- Reflected Methods (World) ----
    public static final Method WORLD_GET_BLOCKSTATE = mref(World.class, "func_180495_p", "getBlockState", BlockPos.class);

    static {
        for (Field f : new Field[]{
                ENTITY_POS_X, ENTITY_POS_Y, ENTITY_POS_Z,
                ENTITY_ROTATION_YAW, ENTITY_ROTATION_PITCH,
                ENTITY_WIDTH, ENTITY_HEIGHT, ENTITY_IS_DEAD, ENTITY_ON_GROUND,
                ELB_HEALTH, ELB_MOVE_FORWARD,
                DISTANCE_WALKED_MODIFIED,
                MC_GAME_SETTINGS, MC_THE_PLAYER, MC_THE_WORLD,
                MC_OBJECT_MOUSE_OVER, MC_RENDER_MANAGER, MC_CURRENT_SCREEN,
                MC_RIGHT_CLICK_DELAY, MC_FONT_RENDERER, MC_DISPLAY_WIDTH, MC_DISPLAY_HEIGHT, MC_TIMER,
                RM_VIEWER_X, RM_VIEWER_Y, RM_VIEWER_Z, RM_PLAYER_YAW, RM_PLAYER_PITCH,
                GS_GAMMA, GS_FOV,
                GS_KEY_BIND_FORWARD, GS_KEY_BIND_BACK, GS_KEY_BIND_LEFT,
                GS_KEY_BIND_RIGHT, GS_KEY_BIND_JUMP, GS_KEY_BIND_SPRINT,
                KEY_BINDING_PRESSED,
                WORLD_LOADED_ENTITY_LIST, WORLD_LOADED_TILE_ENTITY_LIST,
                EP_OPEN_CONTAINER,
                MOP_TYPE_OF_HIT, MOP_BLOCK_POS, MOP_ENTITY_HIT
        }) { f.setAccessible(true); }
    }

    private static Field ref(Class<?> clazz, String srg, String mcp) {
        return ReflectionUtil.getField(clazz, srg, mcp);
    }
    private static Field refCls(String cls, String srg, String mcp) {
        try { return ReflectionUtil.getField(Class.forName(cls), srg, mcp); }
        catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }
    private static Method mref(Class<?> clazz, String srg, String mcp, Class<?>... p) {
        return ReflectionUtil.getMethod(clazz, srg, mcp, p);
    }

    // ---- Accessors ----
    public static Minecraft mc() { return Minecraft.getMinecraft(); }
    public static EntityPlayerSP player() { return mc().thePlayer; }

    public static double getPosX(Entity e) { return ReflectionUtil.getDouble(e, ENTITY_POS_X); }
    public static double getPosY(Entity e) { return ReflectionUtil.getDouble(e, ENTITY_POS_Y); }
    public static double getPosZ(Entity e) { return ReflectionUtil.getDouble(e, ENTITY_POS_Z); }
    public static float getYaw(Entity e) { return ReflectionUtil.getFloat(e, ENTITY_ROTATION_YAW); }
    public static float getPitch(Entity e) { return ReflectionUtil.getFloat(e, ENTITY_ROTATION_PITCH); }
    public static void setYaw(Entity e, float v) { ReflectionUtil.setFloat(e, ENTITY_ROTATION_YAW, v); }
    public static void setPitch(Entity e, float v) { ReflectionUtil.setFloat(e, ENTITY_ROTATION_PITCH, v); }
    public static float getWidth(Entity e) { return ReflectionUtil.getFloat(e, ENTITY_WIDTH); }
    public static float getHeight(Entity e) { return ReflectionUtil.getFloat(e, ENTITY_HEIGHT); }
    public static boolean isDead(Entity e) { return ReflectionUtil.getBoolean(e, ENTITY_IS_DEAD); }
    public static boolean isOnGround(Entity e) { return ReflectionUtil.getBoolean(e, ENTITY_ON_GROUND); }
    public static boolean isSneaking(Entity e) { return ReflectionUtil.invoke(e, ENTITY_IS_SNEAKING); }
    public static boolean isInWater(Entity e) { return ReflectionUtil.invoke(e, ENTITY_IS_IN_WATER); }
    public static float getEyeHeight(Entity e) { return ReflectionUtil.invoke(e, ENTITY_GET_EYE_HEIGHT); }
    public static float getHealth(EntityLivingBase e) { return ReflectionUtil.getFloat(e, ELB_HEALTH); }
    public static float getMaxHealth(EntityLivingBase e) {
        try { return ReflectionUtil.invoke(e, ELB_GET_MAX_HEALTH); }
        catch (Exception ex) { return 20f; }
    }
    public static float getMoveForward(EntityLivingBase e) { return ReflectionUtil.getFloat(e, ELB_MOVE_FORWARD); }
    public static void jump(EntityLivingBase e) { ReflectionUtil.invoke(e, ELB_JUMP); }
    public static void setSprinting(EntityPlayerSP e, boolean v) { ReflectionUtil.invoke(e, EPS_SET_SPRINTING, v); }

    @SuppressWarnings("unchecked")
    public static List<Entity> getLoadedEntities(World w) {
        try { return (List<Entity>) WORLD_LOADED_ENTITY_LIST.get(w); }
        catch (Exception e) { return new ArrayList<Entity>(); }
    }
    public static Container getOpenContainer(EntityPlayer p) {
        try { return (Container) EP_OPEN_CONTAINER.get(p); }
        catch (Exception e) { return null; }
    }
    public static float getGamma() {
        try { return GS_GAMMA.getFloat(MC_GAME_SETTINGS.get(mc())); }
        catch (Exception e) { return 0; }
    }
    public static void clickMouse(Minecraft mc) {
        try { Method m = Minecraft.class.getDeclaredMethod("func_147116_af"); m.setAccessible(true); m.invoke(mc); }
        catch (Exception ignored) {}
    }
    public static void setGamma(float v) {
        try {
            Object gs = MC_GAME_SETTINGS.get(mc());
            GS_GAMMA.setFloat(gs, v);
            gs.getClass().getMethod("saveOptions").invoke(gs);
        } catch (Exception ignored) {}
    }
    public static FontRenderer getFontRenderer() {
        try { return (FontRenderer) MC_FONT_RENDERER.get(mc()); }
        catch (Exception e) { return null; }
    }
    public static int getDisplayWidth() {
        try { return MC_DISPLAY_WIDTH.getInt(mc()); }
        catch (Exception e) { return 854; }
    }
    public static int getDisplayHeight() {
        try { return MC_DISPLAY_HEIGHT.getInt(mc()); }
        catch (Exception e) { return 480; }
    }
    public static float getFov() {
        try { return GS_FOV.getFloat(MC_GAME_SETTINGS.get(mc())); }
        catch (Exception e) { return 70; }
    }
    public static float getFloat(Object obj, java.lang.reflect.Field f) { return ReflectionUtil.getFloat(obj, f); }
    public static void setKeyBindingPressed(Object keyBinding, boolean pressed) {
        try { KEY_BINDING_PRESSED.setBoolean(keyBinding, pressed); }
        catch (Exception ignored) {}
    }
    public static net.minecraft.util.IChatComponent getDisplayName(Entity e) {
        return (net.minecraft.util.IChatComponent) ReflectionUtil.invoke(e, ENTITY_GET_DISPLAY_NAME);
    }
    public static net.minecraft.util.Timer getTimer(Minecraft mc) {
        try { return (net.minecraft.util.Timer) MC_TIMER.get(mc); }
        catch (Exception e) { return null; }
    }
}