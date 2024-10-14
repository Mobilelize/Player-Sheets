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

public class KeyBindHandler {

    private static KeyBinding addPlayerKey;
    private static KeyBinding removePlayerKey;

    // Load toggle settings from the settings file
    private static boolean addEnabled = loadSetting("toggleAddKeyBind", true);
    private static boolean removeEnabled = loadSetting("toggleRemoveKeyBind", true);

    private static final Path configPath = FileHandler.getConfigFilePath();
    private static final Gson GSON = new Gson();

    public static void registerKeyBinds() {
        // Load the keys from the settings file
        addPlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Add Player",
                InputUtil.Type.KEYSYM,
                loadKeyBinding("addPlayerKeybind", GLFW.GLFW_KEY_B),
                "Player Sheets"
        ));

        removePlayerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Remove Player",
                InputUtil.Type.KEYSYM,
                loadKeyBinding("removePlayerKeybind", GLFW.GLFW_KEY_M),
                "Player Sheets"
        ));

        // Add a tick listener for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (addEnabled && addPlayerKey.wasPressed()) {
                handleAddPlayer();
            }

            if (removeEnabled && removePlayerKey.wasPressed()) {
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

    // Load settings from the config file
    private static boolean loadSetting(String key, boolean defaultValue) {
        try {
            List<String> lines = Files.readAllLines(configPath);
            for (String line : lines) {
                if (line.startsWith(key + " :")) {
                    return Boolean.parseBoolean(line.split(":")[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    // Load keybinding from settings
    private static int loadKeyBinding(String key, int defaultKey) {
        try {
            List<String> lines = Files.readAllLines(configPath);
            for (String line : lines) {
                if (line.startsWith(key + " :")) {
                    return Integer.parseInt(line.split(":")[1].trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultKey;
    }

    // Save settings to the config file
    private static void saveSetting(String key, boolean value) {
        try {
            List<String> lines = Files.readAllLines(configPath);
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(key + " :")) {
                    lines.set(i, key + " : " + value);
                    found = true;
                    break;
                }
            }
            if (!found) {
                lines.add(key + " : " + value);
            }
            Files.write(configPath, lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
