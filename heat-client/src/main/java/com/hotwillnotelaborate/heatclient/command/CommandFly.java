package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandFly implements Command {

    private static boolean flying = false;
    private static double speed = 2.0;

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
            // If not already flying, enable it with the new speed
            if (!flying) {
                flying = true;
            }
        } else {
            flying = !flying;
        }

        mc.thePlayer.capabilities.allowFlying = flying;
        if (flying) {
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.setFlySpeed((float) (speed * 0.05f));
        }
        mc.thePlayer.sendPlayerAbilities();

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
