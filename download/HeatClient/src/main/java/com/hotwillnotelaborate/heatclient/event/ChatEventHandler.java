package com.hotwillnotelaborate.heatclient.event;

import com.hotwillnotelaborate.heatclient.HeatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

/**
 * Intercepts the Enter key while the chat GUI is open so that messages
 * starting with "!" are handled client-side instead of being sent to the server.
 * <p>
 * Also shows a one-time welcome message shortly after the player loads into a world.
 */
public class ChatEventHandler {

    /* ---- welcome message bookkeeping ---- */
    private boolean welcomeShown = false;
    private int  tickCounter  = 0;

    /* ---- reflection cache for GuiChat.inputField ---- */
    private static Field inputFieldField;
    private static boolean fieldResolved = false;

    /**
     * Grab the private GuiTextField from GuiChat via reflection.
     * Tries the MCP name first, then common SRG / obfuscated fallbacks.
     */
    private static Object getInputField(Object guiChat) {
        if (!fieldResolved) {
            for (String name : new String[]{"inputField", "field_146415_a", "b"}) {
                try {
                    Field f = net.minecraft.client.gui.GuiChat.class.getDeclaredField(name);
                    f.setAccessible(true);
                    inputFieldField = f;
                    fieldResolved = true;
                    break;
                } catch (NoSuchFieldException ignored) {
                }
            }
            if (!fieldResolved) fieldResolved = true; // give up after one attempt
        }
        if (inputFieldField == null) return null;
        try {
            return inputFieldField.get(guiChat);
        } catch (Exception ignored) {
            return null;
        }
    }

    /* ================================================================
     *  Command execution  –  intercepts Enter in GuiChat
     * ================================================================ */

    @SubscribeEvent
    public void onKeyInputPre(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat)) return;

        int key = Keyboard.getEventKey();
        if (!Keyboard.getEventKeyState()) return;
        if (key != Keyboard.KEY_RETURN && key != Keyboard.KEY_NUMPADENTER) return;

        Object field = getInputField(mc.currentScreen);
        if (!(field instanceof net.minecraft.client.gui.GuiTextField)) return;
        net.minecraft.client.gui.GuiTextField input =
                (net.minecraft.client.gui.GuiTextField) field;

        String text = input.getText().trim();
        if (!text.startsWith("!") || text.length() <= 1) return;

        // Cancel so GuiChat never sends the packet
        event.setCanceled(true);

        boolean handled = HeatClient.commandManager.execute(text);
        if (!handled) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[Heat] " + EnumChatFormatting.GRAY
                            + "Unknown command. Type " + EnumChatFormatting.YELLOW
                            + "!help" + EnumChatFormatting.GRAY + " for available commands."));
        }

        mc.displayGuiScreen(null);
    }

    /* ================================================================
     *  Welcome message  –  fires ~2 s after the player object exists
     * ================================================================ */

    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        if (welcomeShown) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) { tickCounter = 0; return; }

        tickCounter++;
        if (tickCounter < 60) return; // ~3 seconds at 20 tps

        welcomeShown = true;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD
                        + "[Heat Client] " + EnumChatFormatting.RESET
                        + EnumChatFormatting.GRAY + "v" + HeatClient.VERSION
                        + " loaded. Type " + EnumChatFormatting.YELLOW
                        + "!help" + EnumChatFormatting.GRAY + " for commands."));
    }
}