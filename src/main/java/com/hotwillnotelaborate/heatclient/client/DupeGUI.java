package com.hotwillnotelaborate.heatclient.client;

import com.hotwillnotelaborate.heatclient.event.DupeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

public class DupeGUI extends GuiScreen {

    private static final int BTN_TOGGLE = 0;
    private static final int BTN_DELAY_MINUS = 1;
    private static final int BTN_DELAY_PLUS = 2;
    private static final int BTN_CLOSE = 3;

    @Override
    public void initGui() {
        int cx = width / 2;
        int cy = height / 2;
        buttonList.clear();
        buttonList.add(new GuiButton(BTN_TOGGLE, cx - 100, cy - 40, 200, 20, null));
        buttonList.add(new GuiButton(BTN_DELAY_MINUS, cx - 100, cy, 40, 20, "-1"));
        buttonList.add(new GuiButton(BTN_DELAY_PLUS, cx + 60, cy, 40, 20, "+1"));
        buttonList.add(new GuiButton(BTN_CLOSE, cx - 50, cy + 50, 100, 20, "Close"));
        updateButtons();
    }

    private void updateButtons() {
        for (Object obj : buttonList) {
            GuiButton btn = (GuiButton) obj;
            if (btn.id == BTN_TOGGLE) {
                btn.displayString = DupeHandler.enabled
                    ? EnumChatFormatting.GREEN + "Dupe: ON" + EnumChatFormatting.GRAY + " (click to disable)"
                    : EnumChatFormatting.RED + "Dupe: OFF" + EnumChatFormatting.GRAY + " (click to enable)";
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int cx = width / 2;
        int cy = height / 2;

        String title = EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "Dupe Module";
        mc.fontRendererObj.drawStringWithShadow(title, cx - mc.fontRendererObj.getStringWidth(title) / 2, cy - 70, 0);

        String status = DupeHandler.pending
            ? EnumChatFormatting.YELLOW + "PENDING DISCONNECT..."
            : (DupeHandler.enabled ? EnumChatFormatting.GREEN + "Monitoring for item pickup..." : EnumChatFormatting.GRAY + "Module inactive");
        mc.fontRendererObj.drawStringWithShadow(status, cx - mc.fontRendererObj.getStringWidth(status) / 2, cy - 15, 0);

        String delayLabel = EnumChatFormatting.WHITE + "Tick Delay: " + EnumChatFormatting.AQUA + DupeHandler.tickDelay;
        mc.fontRendererObj.drawStringWithShadow(delayLabel, cx - mc.fontRendererObj.getStringWidth(delayLabel) / 2, cy + 7, 0);

        String hint = EnumChatFormatting.DARK_GRAY + "(-) = disconnect before pickup, 0 = instant, (+) = after";
        mc.fontRendererObj.drawStringWithShadow(hint, cx - mc.fontRendererObj.getStringWidth(hint) / 2, cy + 27, 0);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_TOGGLE) {
            DupeHandler.enabled = !DupeHandler.enabled;
            DupeHandler.reset();
            updateButtons();
        } else if (button.id == BTN_DELAY_MINUS) {
            DupeHandler.setTickDelay(DupeHandler.tickDelay - 1);
        } else if (button.id == BTN_DELAY_PLUS) {
            DupeHandler.setTickDelay(DupeHandler.tickDelay + 1);
        } else if (button.id == BTN_CLOSE) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RSHIFT || keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        if (typedChar == '-') {
            DupeHandler.setTickDelay(DupeHandler.tickDelay - 1);
        } else if (typedChar == '+' || typedChar == '=') {
            DupeHandler.setTickDelay(DupeHandler.tickDelay + 1);
        }
    }
}
