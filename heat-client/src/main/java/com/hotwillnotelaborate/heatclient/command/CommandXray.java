package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandXray implements Command {

    @Override
    public String getName() { return "xray"; }

    @Override
    public String getDescription() { return "Toggle ore ESP (use 'alt' for subtle mode)"; }

    @Override
    public String getUsage() { return "!xray [alt|default]"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("esp", "ore"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (args.length > 0) {
            String sub = args[0].toLowerCase();
            if (sub.equals("alt")) {
                XrayRenderer.setMode(true);
                if (!XrayRenderer.isEnabled()) {
                    XrayRenderer.setEnabled(true);
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY
                                    + "X-Ray " + EnumChatFormatting.GREEN + "enabled"
                                    + EnumChatFormatting.GRAY + " ("
                                    + EnumChatFormatting.AQUA + "alt" + EnumChatFormatting.GRAY + " mode)."));
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY
                                    + "X-Ray mode: " + EnumChatFormatting.AQUA + "alt" + EnumChatFormatting.GRAY + "."));
                }
                return;
            } else if (sub.equals("default")) {
                XrayRenderer.setMode(false);
                if (!XrayRenderer.isEnabled()) {
                    XrayRenderer.setEnabled(true);
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY
                                    + "X-Ray " + EnumChatFormatting.GREEN + "enabled"
                                    + EnumChatFormatting.GRAY + " (default mode)."));
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY
                                    + "X-Ray mode: default."));
                }
                return;
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        HeatClient.CHAT_PREFIX + EnumChatFormatting.RED
                                + "Unknown mode. Use !xray, !xray alt, or !xray default."));
                return;
            }
        }

        // No args: toggle on/off
        boolean nowEnabled = !XrayRenderer.isEnabled();
        XrayRenderer.setEnabled(nowEnabled);
        String status;
        if (nowEnabled) {
            String modeStr = XrayRenderer.isAltMode()
                    ? EnumChatFormatting.AQUA + "alt" + EnumChatFormatting.GRAY
                    : "default";
            status = EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY
                    + " (" + modeStr + " mode)";
        } else {
            status = EnumChatFormatting.RED + "disabled" + EnumChatFormatting.GRAY;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "X-Ray " + status + "."));
    }
}
