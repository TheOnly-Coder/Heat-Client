package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class CommandFly implements Command {

    private static boolean flying = false;
    private static double speed = 2.0;

    /* ---- reflected fields ---- */
    private static final Field PLAYER_CAPABILITIES = ReflectionUtil.getField(
            EntityPlayer.class, "field_71075_bZ", "capabilities");
    private static final Field CAP_ALLOW_FLYING = ReflectionUtil.getField(
            PlayerCapabilities.class, "field_75101_c", "allowFlying");
    private static final Field CAP_IS_FLYING = ReflectionUtil.getField(
            PlayerCapabilities.class, "field_75100_b", "isFlying");

    static {
        PLAYER_CAPABILITIES.setAccessible(true);
        CAP_ALLOW_FLYING.setAccessible(true);
        CAP_IS_FLYING.setAccessible(true);
    }

    @Override
    public String getName() { return "fly"; }

    @Override
    public String getDescription() { return "Toggle creative flight (optional speed 0.5-20)"; }

    @Override
    public String getUsage() { return "!fly [speed]"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("flight"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        // Parse optional speed argument
        if (args.length > 0) {
            try {
                double parsed = Double.parseDouble(args[0]);
                if (parsed < 0.1) parsed = 0.1;
                if (parsed > 20) parsed = 20;
                speed = parsed;
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        HeatClient.CHAT_PREFIX + EnumChatFormatting.RED
                                + "Invalid speed. Use a number between 0.1 and 20."));
                return;
            }
            if (!flying) {
                flying = true;
            }
        } else {
            flying = !flying;
        }

        try {
            PlayerCapabilities caps = (PlayerCapabilities) PLAYER_CAPABILITIES.get(mc.thePlayer);
            ReflectionUtil.setBoolean(caps, CAP_ALLOW_FLYING, flying);
            if (flying) {
                ReflectionUtil.setBoolean(caps, CAP_IS_FLYING, true);
                caps.setFlySpeed((float) (speed * 0.05f));
            }
            mc.thePlayer.sendPlayerAbilities();
        } catch (IllegalAccessException e) {
            // fallback: should not happen since setAccessible was called
        }

        String status;
        if (flying) {
            status = EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY
                    + " (speed: " + EnumChatFormatting.AQUA + speed + "x" + EnumChatFormatting.GRAY + ")";
        } else {
            status = EnumChatFormatting.RED + "disabled" + EnumChatFormatting.GRAY;
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Fly " + status + "."));
    }

    public static boolean isFlying() { return flying; }
    public static double getSpeed() { return speed; }
}
