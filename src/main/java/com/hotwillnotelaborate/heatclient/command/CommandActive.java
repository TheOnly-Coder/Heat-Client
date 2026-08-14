package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandActive implements Command {

    @Override
    public String getName() { return "active"; }

    @Override
    public String getDescription() { return "Show currently active modules"; }

    @Override
    public String getUsage() { return "!active"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("modules", "toggled"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        List<String> active = new ArrayList<String>();

        if (CommandFly.isFlying()) {
            active.add(EnumChatFormatting.GREEN + "Fly"
                    + EnumChatFormatting.GRAY + " (speed: "
                    + EnumChatFormatting.AQUA + CommandFly.getSpeed() + "x"
                    + EnumChatFormatting.GRAY + ")");
        }
        if (XrayRenderer.isEnabled()) {
            String mode = XrayRenderer.isAltMode() ? "alt" : "default";
            active.add(EnumChatFormatting.GREEN + "X-Ray"
                    + EnumChatFormatting.GRAY + " ("
                    + EnumChatFormatting.AQUA + mode
                    + EnumChatFormatting.GRAY + ")");
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
                        + "=== Active Modules ==="));

        if (active.isEmpty()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_GRAY + "  No modules active."));
        } else {
            for (String line : active) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.YELLOW + "  \u2714 " + line));
            }
        }
    }
}