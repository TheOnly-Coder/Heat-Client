package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CommandHelp implements Command {

    @Override
    public String getName() { return "help"; }

    @Override
    public String getDescription() { return "Show all available commands"; }

    @Override
    public String getUsage() { return "!help"; }

    @Override
    public List<String> getAliases() { return Arrays.asList("?", "commands"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
                        + "=== Heat Client Commands ==="));
        mc.thePlayer.addChatMessage(new ChatComponentText(""));

        Collection<Command> cmds = HeatClient.commandManager.getCommands();
        for (Command cmd : cmds) {
            StringBuilder aliasHint = new StringBuilder();
            if (!cmd.getAliases().isEmpty()) {
                aliasHint.append(EnumChatFormatting.DARK_GRAY)
                        .append(" (")
                        .append(String.join(", ", cmd.getAliases()))
                        .append(")");
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "  " + cmd.getUsage()
                            + aliasHint.toString()
                            + EnumChatFormatting.GRAY + " - " + cmd.getDescription()));
        }

        mc.thePlayer.addChatMessage(new ChatComponentText(""));
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_GRAY + "  Use Tab to auto-complete while typing."));
    }
}
