package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandFly implements Command {

    private static boolean flying = false;

    @Override
    public String getName() { return "fly"; }

    @Override
    public String getDescription() { return "Toggle creative flight"; }

    @Override
    public String getUsage() { return "!fly"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("flight"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        flying = !flying;
        mc.thePlayer.capabilities.allowFlying = flying;
        if (flying) {
            mc.thePlayer.capabilities.isFlying = true;
        }
        mc.thePlayer.sendPlayerAbilities();

        String status;
        if (flying) {
            status = EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY;
        } else {
            status = EnumChatFormatting.RED + "disabled" + EnumChatFormatting.GRAY;
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Fly " + status + "."));
    }

    public static boolean isFlying() { return flying; }
}
