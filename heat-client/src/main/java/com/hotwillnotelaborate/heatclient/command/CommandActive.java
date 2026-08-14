package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.RenderHandler;
import com.hotwillnotelaborate.heatclient.event.TickHandler;
import com.hotwillnotelaborate.heatclient.event.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class CommandActive implements Command {

    @Override
    public String getName() { return "active"; }

    @Override
    public String getDescription() { return "Show currently active modules"; }

    @Override
    public String getUsage() { return "!active"; }

    @Override
    public List<String> getAliases() { return java.util.Arrays.asList("modules", "toggled"); }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        List<String> active = new ArrayList<String>();

        if (CommandFly.isFlying())
            active.add(EnumChatFormatting.GREEN + "Fly"
                    + EnumChatFormatting.GRAY + " (speed: " + EnumChatFormatting.AQUA + CommandFly.getSpeed() + "x" + EnumChatFormatting.GRAY + ")");
        if (XrayRenderer.isEnabled())
            active.add(EnumChatFormatting.GREEN + "X-Ray" + EnumChatFormatting.GRAY + " (" + EnumChatFormatting.AQUA + (XrayRenderer.isAltMode() ? "alt" : "default") + EnumChatFormatting.GRAY + ")");
        if (TickHandler.fullbright) active.add(EnumChatFormatting.GREEN + "Fullbright");
        if (TickHandler.antiAFK) active.add(EnumChatFormatting.GREEN + "AntiAFK");
        if (TickHandler.autoRespawn) active.add(EnumChatFormatting.GREEN + "AutoRespawn");
        if (TickHandler.sprint) active.add(EnumChatFormatting.GREEN + "Sprint");
        if (TickHandler.derp) active.add(EnumChatFormatting.GREEN + "Derp");
        if (TickHandler.spammer) active.add(EnumChatFormatting.GREEN + "Spammer");
        if (TickHandler.triggerbot) active.add(EnumChatFormatting.GREEN + "TriggerBot");
        if (TickHandler.autoClicker) active.add(EnumChatFormatting.GREEN + "AutoClicker");
        if (TickHandler.fastPlace) active.add(EnumChatFormatting.GREEN + "FastPlace");
        if (TickHandler.nuker) active.add(EnumChatFormatting.GREEN + "Nuker");
        if (TickHandler.chestStealer) active.add(EnumChatFormatting.GREEN + "ChestStealer");
        if (RenderHandler.tracers) active.add(EnumChatFormatting.GREEN + "Tracers");
        if (RenderHandler.esp) active.add(EnumChatFormatting.GREEN + "ESP");
        if (RenderHandler.nametags) active.add(EnumChatFormatting.GREEN + "NameTags");
        if (RenderHandler.blockOverlay) active.add(EnumChatFormatting.GREEN + "BlockOverlay");
        if (RenderHandler.breadcrumbs) active.add(EnumChatFormatting.GREEN + "Breadcrumbs");

        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "=== Active Modules ==="));

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
