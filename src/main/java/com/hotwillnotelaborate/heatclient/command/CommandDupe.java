package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.DupeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandDupe implements Command {
    @Override public String getName() { return "dupe"; }
    @Override public String getDescription() { return "Auto-disconnect dupe (Right Shift for GUI)"; }
    @Override public String getUsage() { return "!dupe [ticks]"; }
    @Override public List<String> getAliases() { return Arrays.asList("duplicate", "dupegui"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (args.length >= 1) {
            try {
                int d = Integer.parseInt(args[0]);
                if (args[0].equalsIgnoreCase("gui") || args[0].equalsIgnoreCase("menu")) {
                    mc.displayGuiScreen(new com.hotwillnotelaborate.heatclient.client.DupeGUI());
                    return;
                }
                DupeHandler.setTickDelay(d);
                if (!DupeHandler.enabled) DupeHandler.enabled = true;
                DupeHandler.reset();
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                    HeatClient.CHAT_PREFIX + EnumChatFormatting.RED + "Invalid argument. Use !dupe [ticks] or !dupe gui"));
                return;
            }
        } else {
            DupeHandler.enabled = !DupeHandler.enabled;
            DupeHandler.reset();
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(
            HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Dupe "
                + (DupeHandler.enabled
                    ? EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY
                        + " (delay: " + EnumChatFormatting.AQUA + DupeHandler.tickDelay
                        + EnumChatFormatting.GRAY + " ticks)"
                    : EnumChatFormatting.RED + "disabled")));
    }
}
