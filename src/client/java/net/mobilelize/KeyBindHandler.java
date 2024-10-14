package net.mobilelize;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.mobilelize.commands.PlayerSheetFunctions;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.include.com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class KeyBindHandler {

    private static KeyBinding addPlayerKey;
    private static KeyBinding removePlayerKey;

    private static final Path configPath = FileHandler.getConfigFilePath();
    private static final Gson GSON = new Gson();

    public static void registerKeyBinds() {
        // Load the keys from the settings file
        addPlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Add Player",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B,
                "Player Sheets"
        ));

        removePlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Remove Player",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M,
                "Player Sheets"
        ));

        // Add a tick listener for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (addPlayerKey.wasPressed()) {
                handleAddPlayer();
            }

            if (removePlayerKey.wasPressed()) {
                handleRemovePlayer();
            }
        });
    }

    private static void handleAddPlayer() {
        PlayerSheetFunctions.addLookedAtPlayer();
    }

    private static void handleRemovePlayer() {
        PlayerSheetFunctions.removeLookedAtPlayer();
    }
}
