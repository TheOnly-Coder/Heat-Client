package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.CritHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CommandCrit implements Command {

    @Override
    public String getName() { return "crit"; }

    @Override
    public String getDescription() { return "Toggle forced critical hits on every attack"; }

    @Override
    public String getUsage() { return "!crit"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("criticals", "critical"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        CritHandler.setEnabled(!CritHandler.isEnabled());

        String status = CritHandler.isEnabled()
                ? EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY
                : EnumChatFormatting.RED + "disabled" + EnumChatFormatting.GRAY;

        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Critical Hits " + status + "."));
    }
}