package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.TickHandler;
import com.hotwillnotelaborate.heatclient.util.McHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandFullbright implements Command {
    @Override public String getName() { return "fullbright"; }
    @Override public String getDescription() { return "Full brightness"; }
    @Override public String getUsage() { return "!fullbright"; }
    @Override public List<String> getAliases() { return Arrays.asList("brightness", "gamma", "fb"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        TickHandler.fullbright = !TickHandler.fullbright;
        if (TickHandler.fullbright) {
            TickHandler.savedGamma = McHelper.getGamma();
            McHelper.setGamma(100f);
        } else {
            TickHandler.disableFullbright(mc);
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Fullbright "
                        + (TickHandler.fullbright ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}