package com.hotwillnotelaborate.heatclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Timer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized Minecraft access via reflection. ALL game-state
 * access from feature code should go through here.
 * ALL SRG names verified against mcp-srg.srg (stable_22).
 */
public class McHelper {

    // ---- Entity fields ----
    public static final Field ENTITY_POS_X = ref(Entity.class, "field_70165_t", "posX");
    public static final Field ENTITY_POS_Y = ref(Entity.class, "field_70163_u", "posY");
    public static final Field ENTITY_POS_Z = ref(Entity.class, "field_70161_v", "posZ");
    public static final Field ENTITY_ROTATION_YAW = ref(Entity.class, "field_70177_z", "rotationYaw");
    public static final Field ENTITY_ROTATION_PITCH = ref(Entity.class, "field_70125_A", "rotationPitch");
    public static final Field ENTITY_WIDTH = ref(Entity.class, "field_70130_N", "width");
    public static final Field ENTITY_HEIGHT = ref(Entity.class, "field_70131_O", "height");
    public static final Field ENTITY_IS_DEAD = ref(Entity.class, "field_70128_L", "isDead");
    public static final Field ENTITY_ON_GROUND = ref(Entity.class, "field_70122_E", "onGround");
    public static final Field ENTITY_FALL_DISTANCE = ref(Entity.class, "field_70143_R", "fallDistance");
    public static final Field ENTITY_DISTANCE_WALKED = ref(Entity.class, "field_70140_Q", "distanceWalkedModified");

    // ---- EntityLivingBase fields ----
    public static final Field ELB_MOVE_FORWARD = ref(EntityLivingBase.class, "field_70701_bs", "moveForward");
    public static final Field ELB_HURT_TIME = ref(EntityLivingBase.class, "field_70737_aN", "hurtTime");

    // ---- Minecraft fields ----
    public static final Field MC_GAME_SETTINGS = ref(Minecraft.class, "field_71474_y", "gameSettings");
    public static final Field MC_THE_PLAYER = ref(Minecraft.class, "field_71439_g", "thePlayer");
    public static final Field MC_THE_WORLD = ref(Minecraft.class, "field_71441_e", "theWorld");
    public static final Field MC_OBJECT_MOUSE_OVER = ref(Minecraft.class, "field_71476_x", "objectMouseOver");
    public static final Field MC_RENDER_MANAGER = ref(Minecraft.class, "field_175616_W", "renderManager");
    public static final Field MC_CURRENT_SCREEN = ref(Minecraft.class, "field_71462_r", "currentScreen");
    public static final Field MC_RIGHT_CLICK_DELAY = ref(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");
    public static final Field MC_FONT_RENDERER = ref(Minecraft.class, "field_71466_p", "fontRendererObj");
    public static final Field MC_DISPLAY_WIDTH = ref(Minecraft.class, "field_71443_c", "displayWidth");
    public static final Field MC_DISPLAY_HEIGHT = ref(Minecraft.class, "field_71440_d", "displayHeight");
    public static final Field MC_TIMER = ref(Minecraft.class, "field_71428_T", "timer");

    // ---- RenderManager fields ----
    public static final Field RM_VIEWER_X = ref(RenderManager.class, "field_78730_l", "viewerPosX");
    public static final Field RM_VIEWER_Y = ref(RenderManager.class, "field_78731_m", "viewerPosY");
    public static final Field RM_VIEWER_Z = ref(RenderManager.class, "field_78728_n", "viewerPosZ");
    public static final Field RM_PLAYER_VIEW_Y = ref(RenderManager.class, "field_78735_i", "playerViewY");
    public static final Field RM_PLAYER_VIEW_X = ref(RenderManager.class, "field_78732_j", "playerViewX");

    // ---- GameSettings fields ----
    public static final Field GS_GAMMA = refCls("net.minecraft.client.settings.GameSettings", "field_74333_Y", "gammaSetting");
    public static final Field GS_FOV = refCls("net.minecraft.client.settings.GameSettings", "field_74334_X", "fovSetting");
    public static final Field GS_KEY_BIND_FORWARD = refCls("net.minecraft.client.settings.GameSettings", "field_74351_w", "keyBindForward");
    public static final Field GS_KEY_BIND_BACK = refCls("net.minecraft.client.settings.GameSettings", "field_74368_y", "keyBindBack");
    public static final Field GS_KEY_BIND_LEFT = refCls("net.minecraft.client.settings.GameSettings", "field_74370_x", "keyBindLeft");
    public static final Field GS_KEY_BIND_RIGHT = refCls("net.minecraft.client.settings.GameSettings", "field_74366_z", "keyBindRight");
    public static final Field GS_KEY_BIND_JUMP = refCls("net.minecraft.client.settings.GameSettings", "field_74314_A", "keyBindJump");
    public static final Field GS_KEY_BIND_SPRINT = refCls("net.minecraft.client.settings.GameSettings", "field_151444_V", "keyBindSprint");
    public static final Field KEY_BINDING_PRESSED = refCls("net.minecraft.client.settings.KeyBinding", "field_74513_e", "pressed");

    // ---- World fields ----
    public static final Field WORLD_LOADED_ENTITY_LIST = ref(World.class, "field_72996_f", "loadedEntityList");
    public static final Field WORLD_LOADED_TILE_ENTITY_LIST = ref(World.class, "field_147482_g", "loadedTileEntityList");

    // ---- EntityPlayer fields ----
    public static final Field EP_OPEN_CONTAINER = ref(EntityPlayer.class, "field_71070_bA", "openContainer");

    // ---- InventoryPlayer fields ----
    public static final Field IP_CURRENT_ITEM = refCls("net.minecraft.entity.player.InventoryPlayer", "field_70461_c", "currentItem");

    // ---- Container fields ----
    public static final Field CONTAINER_INVENTORY_STACKS = refCls("net.minecraft.inventory.Container", "field_75153_a", "inventoryItemStacks");

    // ---- MovingObjectPosition fields ----
    public static final Field MOP_TYPE_OF_HIT = ref(MovingObjectPosition.class, "field_72313_a", "typeOfHit");
    public static final Field MOP_BLOCK_POS = ref(MovingObjectPosition.class, "field_178783_e", "blockPos");
    public static final Field MOP_ENTITY_HIT = ref(MovingObjectPosition.class, "field_72308_g", "entityHit");

    // ---- Timer fields ----
    public static final Field TIMER_SPEED = refCls("net.minecraft.util.Timer", "field_74278_d", "timerSpeed");

    // ---- PlayerControllerMP fields ----
    public static final Field PCM_BLOCK_HIT_DELAY = refCls("net.minecraft.client.multiplayer.PlayerControllerMP", "field_78781_i", "blockHitDelay");
    public static final Field PCM_CUR_BLOCK_DAMAGE = refCls("net.minecraft.client.multiplayer.PlayerControllerMP", "field_78770_f", "curBlockDamageMP");

    // ---- EntityRenderer fields ----
    public static final Field ER_CAMERA_ZOOM = refCls("net.minecraft.client.renderer.EntityRenderer", "field_78503_V", "cameraZoom");
    public static final Field ER_CAMERA_YAW = refCls("net.minecraft.client.renderer.EntityRenderer", "field_78502_W", "cameraYaw");

    // ---- Reflected Methods (Entity) ----
    public static final Method ENTITY_IS_SNEAKING = mref(Entity.class, "func_70093_af", "isSneaking");
    public static final Method ENTITY_IS_IN_WATER = mref(Entity.class, "func_70090_H", "isInWater");
    public static final Method ENTITY_GET_EYE_HEIGHT = mref(Entity.class, "func_70047_e", "getEyeHeight");
    public static final Method ENTITY_GET_DISPLAY_NAME = mref(Entity.class, "func_145748_c_", "getDisplayName");
    public static final Method ENTITY_GET_DISTANCE = mref(Entity.class, "func_70032_d", "getDistanceToEntity", Entity.class);
    public static final Method ENTITY_SET_SPRINTING = mref(Entity.class, "func_70031_b", "setSprinting", boolean.class);

    // ---- Reflected Methods (EntityLivingBase) ----
    public static final Method ELB_JUMP = mref(EntityLivingBase.class, "func_70664_aZ", "jump");
    public static final Method ELB_GET_MAX_HEALTH = mref(EntityLivingBase.class, "func_110138_aP", "getMaxHealth");
    public static final Method ELB_GET_HEALTH = mref(EntityLivingBase.class, "func_110143_aJ", "getHealth");

    // ---- Reflected Methods (World) ----
    public static final Method WORLD_GET_BLOCKSTATE = mref(World.class, "func_180495_p", "getBlockState", BlockPos.class);

    static {
        for (Field f : new Field[]{
                ENTITY_POS_X, ENTITY_POS_Y, ENTITY_POS_Z,
                ENTITY_ROTATION_YAW, ENTITY_ROTATION_PITCH,
                ENTITY_WIDTH, ENTITY_HEIGHT, ENTITY_IS_DEAD, ENTITY_ON_GROUND,
                ENTITY_FALL_DISTANCE, ENTITY_DISTANCE_WALKED,
                ELB_MOVE_FORWARD, ELB_HURT_TIME,
                MC_GAME_SETTINGS, MC_THE_PLAYER, MC_THE_WORLD,
                MC_OBJECT_MOUSE_OVER, MC_RENDER_MANAGER, MC_CURRENT_SCREEN,
                MC_RIGHT_CLICK_DELAY, MC_FONT_RENDERER, MC_DISPLAY_WIDTH, MC_DISPLAY_HEIGHT, MC_TIMER,
                RM_VIEWER_X, RM_VIEWER_Y, RM_VIEWER_Z, RM_PLAYER_VIEW_Y, RM_PLAYER_VIEW_X,
                GS_GAMMA, GS_FOV,
                GS_KEY_BIND_FORWARD, GS_KEY_BIND_BACK, GS_KEY_BIND_LEFT,
                GS_KEY_BIND_RIGHT, GS_KEY_BIND_JUMP, GS_KEY_BIND_SPRINT,
                KEY_BINDING_PRESSED,
                WORLD_LOADED_ENTITY_LIST, WORLD_LOADED_TILE_ENTITY_LIST,
                EP_OPEN_CONTAINER, IP_CURRENT_ITEM, CONTAINER_INVENTORY_STACKS,
                MOP_TYPE_OF_HIT, MOP_BLOCK_POS, MOP_ENTITY_HIT,
                TIMER_SPEED, PCM_BLOCK_HIT_DELAY, PCM_CUR_BLOCK_DAMAGE,
                ER_CAMERA_ZOOM, ER_CAMERA_YAW
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
    public static float getFallDistance(Entity e) { return ReflectionUtil.getFloat(e, ENTITY_FALL_DISTANCE); }
    public static boolean isSneaking(Entity e) { return ReflectionUtil.invoke(e, ENTITY_IS_SNEAKING); }
    public static boolean isInWater(Entity e) { return ReflectionUtil.invoke(e, ENTITY_IS_IN_WATER); }
    public static float getEyeHeight(Entity e) { return ReflectionUtil.invoke(e, ENTITY_GET_EYE_HEIGHT); }
    public static float getHealth(EntityLivingBase e) { return ReflectionUtil.invoke(e, ELB_GET_HEALTH); }
    public static float getMaxHealth(EntityLivingBase e) {
        try { return ReflectionUtil.invoke(e, ELB_GET_MAX_HEALTH); }
        catch (Exception ex) { return 20f; }
    }
    public static float getMoveForward(EntityLivingBase e) { return ReflectionUtil.getFloat(e, ELB_MOVE_FORWARD); }
    public static void jump(EntityLivingBase e) { ReflectionUtil.invoke(e, ELB_JUMP); }
    public static void setSprinting(Entity e, boolean v) { ReflectionUtil.invoke(e, ENTITY_SET_SPRINTING, v); }
    public static void setHurtTime(EntityLivingBase e, int v) { ReflectionUtil.setInt(e, ELB_HURT_TIME, v); }

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
    public static Timer getTimer(Minecraft mc) {
        try { return (Timer) MC_TIMER.get(mc); }
        catch (Exception e) { return null; }
    }
    public static void setTimerSpeed(Timer t, float speed) {
        try { TIMER_SPEED.setFloat(t, speed); } catch (Exception ignored) {}
    }
    public static void setPcBlockHitDelay(PlayerControllerMP pc, int v) {
        try { PCM_BLOCK_HIT_DELAY.setInt(pc, v); } catch (Exception ignored) {}
    }
    public static float getPcCurBlockDamage(PlayerControllerMP pc) {
        try { return PCM_CUR_BLOCK_DAMAGE.getFloat(pc); } catch (Exception e) { return 0; }
    }
    public static void setPcCurBlockDamage(PlayerControllerMP pc, float v) {
        try { PCM_CUR_BLOCK_DAMAGE.setFloat(pc, v); } catch (Exception ignored) {}
    }
    public static void setErCameraZoom(EntityRenderer er, double v) {
        try { ER_CAMERA_ZOOM.setDouble(er, v); } catch (Exception ignored) {}
    }
    public static void setErCameraYaw(EntityRenderer er, double v) {
        try { ER_CAMERA_YAW.setDouble(er, v); } catch (Exception ignored) {}
    }
    public static void setInventoryCurrentItem(InventoryPlayer inv, int slot) {
        try { IP_CURRENT_ITEM.setInt(inv, slot); } catch (Exception ignored) {}
    }
    @SuppressWarnings("unchecked")
    public static List getContainerInventoryStacks(Container c) {
        try { return (List) CONTAINER_INVENTORY_STACKS.get(c); }
        catch (Exception e) { return null; }
    }
}
