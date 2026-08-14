package com.hotwillnotelaborate.heatclient.command;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.event.RenderHandler;
import com.hotwillnotelaborate.heatclient.event.TickHandler;
import com.hotwillnotelaborate.heatclient.event.XrayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

class CommandAntiAFK implements Command {
    @Override public String getName() { return "antiafk"; }
    @Override public String getDescription() { return "Prevent AFK kick"; }
    @Override public String getUsage() { return "!antiafk"; }
    @Override public List<String> getAliases() { return Arrays.asList("afk"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.antiAFK = !TickHandler.antiAFK;
        TickHandler.resetAfkTimer();
        send(mc, "AntiAFK", TickHandler.antiAFK);
    }
    private void send(Minecraft mc, String name, boolean on) {
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + name + " "
                        + (on ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAutoRespawn implements Command {
    @Override public String getName() { return "autorespawn"; }
    @Override public String getDescription() { return "Auto-respawn on death"; }
    @Override public String getUsage() { return "!autorespawn"; }
    @Override public List<String> getAliases() { return Arrays.asList("respawn", "autoresp"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.autoRespawn = !TickHandler.autoRespawn;
        send(mc, "AutoRespawn", TickHandler.autoRespawn);
    }
    private void send(Minecraft mc, String name, boolean on) {
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + name + " "
                        + (on ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandSprint implements Command {
    @Override public String getName() { return "sprint"; }
    @Override public String getDescription() { return "Force sprint when moving"; }
    @Override public String getUsage() { return "!sprint"; }
    @Override public List<String> getAliases() { return Arrays.asList("autosprint"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.sprint = !TickHandler.sprint;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Sprint "
                        + (TickHandler.sprint ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandDerp implements Command {
    @Override public String getName() { return "derp"; }
    @Override public String getDescription() { return "Random head rotation"; }
    @Override public String getUsage() { return "!derp"; }
    @Override public List<String> getAliases() { return Arrays.asList("spin"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.derp = !TickHandler.derp;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Derp "
                        + (TickHandler.derp ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandSpammer implements Command {
    @Override public String getName() { return "spammer"; }
    @Override public String getDescription() { return "Spam chat messages"; }
    @Override public String getUsage() { return "!spammer"; }
    @Override public List<String> getAliases() { return Arrays.asList("spam"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.spammer = !TickHandler.spammer;
        TickHandler.resetSpamTimer();
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Spammer "
                        + (TickHandler.spammer ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandTriggerbot implements Command {
    @Override public String getName() { return "triggerbot"; }
    @Override public String getDescription() { return "Auto-attack crosshair target"; }
    @Override public String getUsage() { return "!triggerbot"; }
    @Override public List<String> getAliases() { return Arrays.asList("trigger", "autoattack"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.triggerbot = !TickHandler.triggerbot;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "TriggerBot "
                        + (TickHandler.triggerbot ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAutoClicker implements Command {
    @Override public String getName() { return "autoclicker"; }
    @Override public String getDescription() { return "Auto-click at configurable CPS"; }
    @Override public String getUsage() { return "!autoclicker [min] [max]"; }
    @Override public List<String> getAliases() { return Arrays.asList("autoclick", "clicker"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        if (args.length >= 2) {
            try {
                int min = Integer.parseInt(args[0]);
                int max = Integer.parseInt(args[1]);
                TickHandler.setCps(Math.max(1, min), Math.max(1, max));
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        HeatClient.CHAT_PREFIX + EnumChatFormatting.RED + "Invalid CPS. Use numbers 1-20."));
                return;
            }
            if (!TickHandler.autoClicker) TickHandler.autoClicker = true;
        } else {
            TickHandler.autoClicker = !TickHandler.autoClicker;
        }
        int[] cps = TickHandler.getCps();
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "AutoClicker "
                        + (TickHandler.autoClicker
                        ? EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY + " (" + cps[0] + "-" + cps[1] + " CPS)"
                        : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandFastPlace implements Command {
    @Override public String getName() { return "fastplace"; }
    @Override public String getDescription() { return "Remove block placement delay"; }
    @Override public String getUsage() { return "!fastplace"; }
    @Override public List<String> getAliases() { return Arrays.asList("nodelay", "fastbuild"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.fastPlace = !TickHandler.fastPlace;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "FastPlace "
                        + (TickHandler.fastPlace ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNuker implements Command {
    @Override public String getName() { return "nuker"; }
    @Override public String getDescription() { return "Auto-break blocks in radius"; }
    @Override public String getUsage() { return "!nuker [range]"; }
    @Override public List<String> getAliases() { return Arrays.asList("breaker", "autobreak"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        if (args.length >= 1) {
            try {
                float r = Float.parseFloat(args[0]);
                TickHandler.setNukerRange(r);
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        HeatClient.CHAT_PREFIX + EnumChatFormatting.RED + "Invalid range. Use 1-6."));
                return;
            }
            if (!TickHandler.nuker) TickHandler.nuker = true;
        } else {
            TickHandler.nuker = !TickHandler.nuker;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Nuker "
                        + (TickHandler.nuker ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandChestStealer implements Command {
    @Override public String getName() { return "cheststealer"; }
    @Override public String getDescription() { return "Auto-steal from chests"; }
    @Override public String getUsage() { return "!cheststealer"; }
    @Override public List<String> getAliases() { return Arrays.asList("stealer", "autosteal"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.chestStealer = !TickHandler.chestStealer;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "ChestStealer "
                        + (TickHandler.chestStealer ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandTracers implements Command {
    @Override public String getName() { return "tracers"; }
    @Override public String getDescription() { return "Lines to entities through walls"; }
    @Override public String getUsage() { return "!tracers"; }
    @Override public List<String> getAliases() { return Arrays.asList("lines"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.tracers = !RenderHandler.tracers;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Tracers "
                        + (RenderHandler.tracers ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandESP implements Command {
    @Override public String getName() { return "esp"; }
    @Override public String getDescription() { return "Entity bounding boxes through walls"; }
    @Override public String getUsage() { return "!esp"; }
    @Override public List<String> getAliases() { return Arrays.asList("boxes", "espbox"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.esp = !RenderHandler.esp;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "ESP "
                        + (RenderHandler.esp ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNametags implements Command {
    @Override public String getName() { return "nametags"; }
    @Override public String getDescription() { return "Enhanced name tags with health"; }
    @Override public String getUsage() { return "!nametags"; }
    @Override public List<String> getAliases() { return Arrays.asList("tags"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.nametags = !RenderHandler.nametags;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "NameTags "
                        + (RenderHandler.nametags ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandBlockOverlay implements Command {
    @Override public String getName() { return "blockoverlay"; }
    @Override public String getDescription() { return "Highlight block you're looking at"; }
    @Override public String getUsage() { return "!blockoverlay"; }
    @Override public List<String> getAliases() { return Arrays.asList("highlight", "blockhl"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.blockOverlay = !RenderHandler.blockOverlay;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "BlockOverlay "
                        + (RenderHandler.blockOverlay ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandBreadcrumbs implements Command {
    @Override public String getName() { return "breadcrumbs"; }
    @Override public String getDescription() { return "Trail of your past positions"; }
    @Override public String getUsage() { return "!breadcrumbs"; }
    @Override public List<String> getAliases() { return Arrays.asList("trail"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.breadcrumbs = !RenderHandler.breadcrumbs;
        if (RenderHandler.breadcrumbs) RenderHandler.clearBreadcrumbs();
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Breadcrumbs "
                        + (RenderHandler.breadcrumbs ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandNoBob implements Command {
    @Override public String getName() { return "nobob"; }
    @Override public String getDescription() { return "Remove view bobbing"; }
    @Override public String getUsage() { return "!nobob"; }
    @Override public List<String> getAliases() { return Arrays.asList("antibob"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.noBob = !TickHandler.noBob;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "NoBob "
                        + (TickHandler.noBob ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandInventoryWalk implements Command {
    @Override public String getName() { return "inventorywalk"; }
    @Override public String getDescription() { return "Move while in inventory/GUI"; }
    @Override public String getUsage() { return "!inventorywalk"; }
    @Override public List<String> getAliases() { return Arrays.asList("invwalk"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.inventoryWalk = !TickHandler.inventoryWalk;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "InventoryWalk "
                        + (TickHandler.inventoryWalk ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandAutoBow implements Command {
    @Override public String getName() { return "autobow"; }
    @Override public String getDescription() { return "Auto-release bow at full charge"; }
    @Override public String getUsage() { return "!autobow"; }
    @Override public List<String> getAliases() { return Arrays.asList("bow"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        TickHandler.autoBow = !TickHandler.autoBow;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "AutoBow "
                        + (TickHandler.autoBow ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandTimer implements Command {
    @Override public String getName() { return "timer"; }
    @Override public String getDescription() { return "Change game tick speed"; }
    @Override public String getUsage() { return "!timer [speed]"; }
    @Override public List<String> getAliases() { return Arrays.asList("speed"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        if (args.length >= 1) {
            try {
                float s = Float.parseFloat(args[0]);
                TickHandler.setTimerSpeed(s);
                if (!TickHandler.timer) TickHandler.timer = true;
            } catch (NumberFormatException e) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        HeatClient.CHAT_PREFIX + EnumChatFormatting.RED + "Invalid speed. Use 0.1-20.0."));
                return;
            }
        } else {
            TickHandler.timer = !TickHandler.timer;
            if (!TickHandler.timer) TickHandler.disableTimer(mc);
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "Timer "
                        + (TickHandler.timer
                        ? EnumChatFormatting.GREEN + "enabled" + EnumChatFormatting.GRAY + " (" + TickHandler.getTimerSpeed() + "x)"
                        : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandItemESP implements Command {
    @Override public String getName() { return "itemesp"; }
    @Override public String getDescription() { return "Highlight dropped items"; }
    @Override public String getUsage() { return "!itemesp"; }
    @Override public List<String> getAliases() { return Arrays.asList("items"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.itemESP = !RenderHandler.itemESP;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "ItemESP "
                        + (RenderHandler.itemESP ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandStorageESP implements Command {
    @Override public String getName() { return "storageesp"; }
    @Override public String getDescription() { return "Highlight storage blocks"; }
    @Override public String getUsage() { return "!storageesp"; }
    @Override public List<String> getAliases() { return Arrays.asList("storage", "chestesp"); }
    @Override public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        RenderHandler.storageESP = !RenderHandler.storageESP;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.GRAY + "StorageESP "
                        + (RenderHandler.storageESP ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled")));
    }
}

class CommandPanic implements Command {
    @Override public String getName() { return "panic"; }
    @Override public String getDescription() { return "Disable ALL active modules"; }
    @Override public String getUsage() { return "!panic"; }
    @Override public List<String> getAliases() { return Arrays.asList("disableall", "off"); }
    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft(); if (mc.thePlayer == null) return;
        if (TickHandler.fullbright) { TickHandler.fullbright = false; TickHandler.disableFullbright(mc); }
        TickHandler.antiAFK = false;
        TickHandler.autoRespawn = false;
        TickHandler.sprint = false;
        TickHandler.derp = false;
        TickHandler.spammer = false;
        TickHandler.triggerbot = false;
        TickHandler.autoClicker = false;
        TickHandler.fastPlace = false;
        TickHandler.nuker = false;
        TickHandler.chestStealer = false;
        TickHandler.noBob = false;
        TickHandler.inventoryWalk = false;
        TickHandler.autoBow = false;
        if (TickHandler.timer) { TickHandler.timer = false; TickHandler.disableTimer(mc); }
        if (CommandFly.isFlying()) { String[] a = {}; new CommandFly().execute(a); }
        XrayRenderer.setEnabled(false);
        RenderHandler.tracers = false;
        RenderHandler.esp = false;
        RenderHandler.nametags = false;
        RenderHandler.blockOverlay = false;
        RenderHandler.itemESP = false;
        RenderHandler.storageESP = false;
        if (RenderHandler.breadcrumbs) { RenderHandler.breadcrumbs = false; RenderHandler.clearBreadcrumbs(); }
        mc.thePlayer.addChatMessage(new ChatComponentText(
                HeatClient.CHAT_PREFIX + EnumChatFormatting.RED + "PANIC - All modules disabled."));
    }
}
