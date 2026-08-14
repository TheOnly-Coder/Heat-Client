package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

class CommandVelocity implements Command {
    @Override public String getName() { return "velocity"; }
    @Override public String getDescription() { return "Anti-knockback (cancel KB packets)"; }
    @Override public String getUsage() { return "!velocity"; }
    @Override public List<String> getAliases() { return Arrays.asList("antikb", "nokb"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        PacketHandler.velocity = !PacketHandler.velocity;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Velocity "
                        + (PacketHandler.velocity ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNoFall implements Command {
    @Override public String getName() { return "nofall"; }
    @Override public String getDescription() { return "Prevent fall damage (spoof ground)"; }
    @Override public String getUsage() { return "!nofall"; }
    @Override public List<String> getAliases() { return Arrays.asList("antifall"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        PacketHandler.noFall = !PacketHandler.noFall;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "NoFall "
                        + (PacketHandler.noFall ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandBlink implements Command {
    @Override public String getName() { return "blink"; }
    @Override public String getDescription() { return "Choke position packets then release"; }
    @Override public String getUsage() { return "!blink"; }
    @Override public List<String> getAliases() { return Arrays.asList("freeze"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        if (!PacketHandler.blink) {
            PacketHandler.blink = true;
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Blink "
                            + EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY + " (" + EnumChatFormatting.AQUA + PacketHandler.getBlinkQueueSize() + EnumChatFormatting.GRAY + " packets queued)"));
        } else {
            PacketHandler.blink = false;
            int count = PacketHandler.getBlinkQueueSize();
            PacketHandler.releaseBlink();
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Blink "
                            + EnumChatFormatting.RED + "disabled" + EnumChatFormatting.GRAY + " (released " + EnumChatFormatting.AQUA + count + EnumChatFormatting.GRAY + " packets)"));
        }
    }
}

class CommandCriticals implements Command {
    @Override public String getName() { return "criticals"; }
    @Override public String getDescription() { return "Force critical hits on every attack"; }
    @Override public String getUsage() { return "!criticals"; }
    @Override public List<String> getAliases() { return Arrays.asList("crits"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        CombatHandler.criticals = !CombatHandler.criticals;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Criticals "
                        + (CombatHandler.criticals ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandSuperKnockback implements Command {
    @Override public String getName() { return "superknockback"; }
    @Override public String getDescription() { return "Max knockback on every hit (WTap)"; }
    @Override public String getUsage() { return "!superknockback"; }
    @Override public List<String> getAliases() { return Arrays.asList("wtap", "skb"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        CombatHandler.superKnockback = !CombatHandler.superKnockback;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "SuperKnockback "
                        + (CombatHandler.superKnockback ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandFastUse implements Command {
    @Override public String getName() { return "fastuse"; }
    @Override public String getDescription() { return "Instantly eat/drink items"; }
    @Override public String getUsage() { return "!fastuse"; }
    @Override public List<String> getAliases() { return Arrays.asList("instantuse", "fasteat"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.fastUse = !TickHandler.fastUse;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "FastUse "
                        + (TickHandler.fastUse ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandFastBreak implements Command {
    @Override public String getName() { return "fastbreak"; }
    @Override public String getDescription() { return "Break blocks faster"; }
    @Override public String getUsage() { return "!fastbreak"; }
    @Override public List<String> getAliases() { return Arrays.asList("speedmine", "fastmine"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.fastBreak = !TickHandler.fastBreak;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "FastBreak "
                        + (TickHandler.fastBreak ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAutoTool implements Command {
    @Override public String getName() { return "autotool"; }
    @Override public String getDescription() { return "Auto-switch to best tool when mining"; }
    @Override public String getUsage() { return "!autotool"; }
    @Override public List<String> getAliases() { return Arrays.asList("autoswitch", "toolswitch"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        PlayerHandler.autoTool = !PlayerHandler.autoTool;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "AutoTool "
                        + (PlayerHandler.autoTool ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNoHurtCam implements Command {
    @Override public String getName() { return "nohurtcam"; }
    @Override public String getDescription() { return "Remove hurt screen shake"; }
    @Override public String getUsage() { return "!nohurtcam"; }
    @Override public List<String> getAliases() { return Arrays.asList("antihurt"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.noHurtCam = !TickHandler.noHurtCam;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "NoHurtCam "
                        + (TickHandler.noHurtCam ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAntiBlind implements Command {
    @Override public String getName() { return "antiblind"; }
    @Override public String getDescription() { return "Remove confusion/nausea effects"; }
    @Override public String getUsage() { return "!antiblind"; }
    @Override public List<String> getAliases() { return Arrays.asList("antinausea"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.antiBlind = !TickHandler.antiBlind;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "AntiBlind "
                        + (TickHandler.antiBlind ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAntiExploit implements Command {
    @Override public String getName() { return "antiexploit"; }
    @Override public String getDescription() { return "Clamp malicious exploit packets"; }
    @Override public String getUsage() { return "!antiexploit"; }
    @Override public List<String> getAliases() { return Arrays.asList("ant crash"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        PacketHandler.antiExploit = !PacketHandler.antiExploit;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "AntiExploit "
                        + (PacketHandler.antiExploit ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNoSwing implements Command {
    @Override public String getName() { return "noswing"; }
    @Override public String getDescription() { return "Hide arm swing from other players"; }
    @Override public String getUsage() { return "!noswing"; }
    @Override public List<String> getAliases() { return Arrays.asList("hideswing"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        PacketHandler.noSwing = !PacketHandler.noSwing;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "NoSwing "
                        + (PacketHandler.noSwing ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}