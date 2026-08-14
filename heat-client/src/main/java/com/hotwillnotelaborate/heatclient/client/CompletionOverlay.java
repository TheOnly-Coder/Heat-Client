package com.hotwillnotelaborate.heatclient.client;

import com.hotwillnotelaborate.heatclient.HeatClient;
import com.hotwillnotelaborate.heatclient.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Backported 1.13+ style command-suggestion overlay for "!" commands.
 * <p>
 * Renders a floating tooltip-like list above the chat bar that updates
 * in real-time as the player types, and supports Tab-cycling through
 * the completions.
 */
public class CompletionOverlay {

    /* ---- heat-themed colours (ARGB) ---- */
    private static final int BG           = 0xE0101010;
    private static final int ACCENT       = 0xFFFF6600;   // orange top bar
    private static final int BORDER       = 0x60FFFFFF;
    private static final int TEXT_CMD     = 0xFFFFAA00;   // gold  – command name
    private static final int TEXT_DESC    = 0xFFAAAAAA;   // grey  – description
    private static final int TEXT_SEL_CMD = 0xFFFFFF55;   // bright gold when selected
    private static final int TEXT_SEL_BG  = 0x44FF6600;   // translucent orange highlight
    private static final int TEXT_MORE    = 0xFF666666;

    private static final int LINE_H    = 14;
    private static final int PAD       = 3;
    private static final int MAX_LINES = 7;

    /* ---- reflection cache ---- */
    private static Field inputFieldField;
    private static boolean fieldResolved = false;

    /* ---- tab-cycle state ---- */
    private int          tabCycleIndex       = -1;
    private List<String> cachedCompletions  = new ArrayList<>();
    private String       cachedPrefix        = "";
    private String       preTabText          = "";  // what was in the field before first Tab

    /* ================================================================
     *  Reflection helper – same approach as ChatEventHandler
     * =============================================================== */

    private static GuiTextField getInputField(GuiChat chat) {
        if (!fieldResolved) {
            for (String name : new String[]{"inputField", "field_146415_a", "b"}) {
                try {
                    Field f = GuiChat.class.getDeclaredField(name);
                    f.setAccessible(true);
                    inputFieldField = f;
                    fieldResolved = true;
                    break;
                } catch (NoSuchFieldException ignored) {
                }
            }
            if (!fieldResolved) fieldResolved = true;
        }
        if (inputFieldField == null) return null;
        try {
            return (GuiTextField) inputFieldField.get(chat);
        } catch (Exception ignored) {
            return null;
        }
    }

    /* ================================================================
     *  Tab-key handling  (cancels default server-side tab-complete)
     * =============================================================== */

    @SubscribeEvent
    public void onKeyInputPre(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) {
            resetState();
            return;
        }

        int key = Keyboard.getEventKey();
        boolean pressed = Keyboard.getEventKeyState();

        // If a non-Tab key is pressed, reset the cycle so the next Tab
        // starts fresh from whatever the player has typed.
        if (pressed && key != Keyboard.KEY_TAB) {
            resetState();
            return;
        }

        if (!pressed || key != Keyboard.KEY_TAB) return;

        GuiChat chat = (GuiChat) mc.currentScreen;
        GuiTextField input = getInputField(chat);
        if (input == null) return;

        String text = input.getText();
        if (!text.startsWith("!") || text.length() <= 1) return;

        // Cancel so GuiChat never sends C14PacketTabComplete
        event.setCanceled(true);

        // Extract the partial command token (after "!", before any space)
        String afterBang = text.substring(1);
        String partial = afterBang.split("\\s+", 2)[0];

        // Refresh completion list when the prefix has changed
        if (!partial.equals(cachedPrefix)) {
            cachedPrefix = partial;
            cachedCompletions = HeatClient.commandManager.getCompletions(partial);
            tabCycleIndex = 0;
            preTabText = text;
        }

        if (cachedCompletions.isEmpty()) return;

        // Determine which completion to show
        int idx = tabCycleIndex % cachedCompletions.size();

        // If we've cycled past the last entry, restore the original text
        if (tabCycleIndex > 0 && tabCycleIndex % cachedCompletions.size() == 0
                && cachedCompletions.size() > 1) {
            input.setText(preTabText);
            input.setCursorPositionZero();
            input.setCursorPositionEnd();
            tabCycleIndex = -1;           // next Tab starts a new cycle
            return;
        }

        String completion = cachedCompletions.get(idx);
        // Preserve any arguments the player already typed after the command name
        String trailing = "";
        int spaceIdx = afterBang.indexOf(' ');
        if (spaceIdx >= 0) {
            trailing = afterBang.substring(spaceIdx); // includes the leading space
        }
        input.setText("!" + completion + trailing + " ");
        input.setCursorPositionZero();
        input.setCursorPositionEnd();

        tabCycleIndex++;
    }

    /* ================================================================
     *  Rendering  –  the floating suggestion list
     * =============================================================== */

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiChat)) return;

        GuiChat chat = (GuiChat) mc.currentScreen;
        GuiTextField input = getInputField(chat);
        if (input == null) return;

        String text = input.getText();
        if (!text.startsWith("!") || text.length() <= 1) return;

        String afterBang = text.substring(1);
        String partial = afterBang.split("\\s+", 2)[0];

        List<String> completions = HeatClient.commandManager.getCompletions(partial);
        if (completions.isEmpty()) return;

        /* ---- calculate box dimensions ---- */
        ScaledResolution sr = new ScaledResolution(mc);
        int screenW = sr.getScaledWidth();
        int screenH = sr.getScaledHeight();

        // Compute the widest line  ("!cmd  description")
        int maxTextW = 0;
        for (String name : completions) {
            Command cmd = HeatClient.commandManager.getCommand(name);
            String line = "!" + name;
            if (cmd != null) line += "  " + cmd.getDescription();
            int w = mc.fontRendererObj.getStringWidth(line);
            if (w > maxTextW) maxTextW = w;
        }

        int visible = Math.min(completions.size(), MAX_LINES);
        int boxW = maxTextW + PAD * 2 + 8;
        int boxH = visible * LINE_H + PAD * 2;
        int boxX = 2;
        int boxY = screenH - 46 - boxH;  // just above the chat input bar

        /* ---- draw background ---- */
        Gui.drawRect(boxX, boxY, boxX + boxW, boxY + boxH, BG);
        // orange accent line along the top
        Gui.drawRect(boxX, boxY, boxX + boxW, boxY + 1, ACCENT);
        // subtle border
        Gui.drawRect(boxX, boxY, boxX + 1, boxY + boxH, BORDER);
        Gui.drawRect(boxX + boxW - 1, boxY, boxX + boxW, boxY + boxH, BORDER);

        /* ---- draw each completion row ---- */
        int selectedVisual = tabCycleIndex >= 0
                ? (tabCycleIndex - 1 + cachedCompletions.size()) % cachedCompletions.size()
                : -1;
        // Only highlight if the cached prefix matches what's currently displayed
        if (!partial.equals(cachedPrefix)) selectedVisual = -1;

        for (int i = 0; i < visible; i++) {
            String name = completions.get(i);
            Command cmd = HeatClient.commandManager.getCommand(name);
            int y = boxY + PAD + i * LINE_H;

            // Selection highlight
            if (i == selectedVisual) {
                Gui.drawRect(boxX + 1, y - 1, boxX + boxW - 1, y + LINE_H - 1, TEXT_SEL_BG);
            }

            int cmdColour = (i == selectedVisual) ? TEXT_SEL_CMD : TEXT_CMD;
            int descColour = (i == selectedVisual) ? 0xFFDDDDDD : TEXT_DESC;

            // Render "!cmd"
            mc.fontRendererObj.drawStringWithShadow("!" + name, boxX + PAD + 2, y + 2, cmdColour);

            // Render description after a gap
            if (cmd != null) {
                int cmdW = mc.fontRendererObj.getStringWidth("!" + name);
                mc.fontRendererObj.drawStringWithShadow(
                        cmd.getDescription(),
                        boxX + PAD + 2 + cmdW + 8, y + 2, descColour);
            }
        }

        /* ---- "N more" indicator ---- */
        if (completions.size() > MAX_LINES) {
            int remaining = completions.size() - MAX_LINES;
            String more = "..." + remaining + " more";
            mc.fontRendererObj.drawStringWithShadow(
                    more,
                    boxX + boxW - mc.fontRendererObj.getStringWidth(more) - PAD - 2,
                    boxY + boxH - LINE_H + 2, TEXT_MORE);
        }

        // Restore GL state (some older 1.8 setups are finicky)
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableAlpha();
    }

    /* ---- helpers ---- */

    private void resetState() {
        tabCycleIndex = -1;
        cachedCompletions.clear();
        cachedPrefix = "";
        preTabText = "";
    }
}