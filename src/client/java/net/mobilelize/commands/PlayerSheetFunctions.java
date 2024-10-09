package net.mobilelize.commands;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.mobilelize.FileHandler;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlayerSheetFunctions {

    private static final String CONFIG_DIRECTORY = "config/Player Sheets mobilelize";
    private static final String CONFIG_FILE = "player-sheet.txt";
    private static final String MACRO_FILE = "player-macros.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static MacroState loadMacroState() {
        Path path = Path.of(CONFIG_DIRECTORY, MACRO_FILE);
        try {
            if (Files.exists(path)) {
                BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                MacroState state = GSON.fromJson(reader, MacroState.class);
                reader.close();
                return state;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MacroState();  // Return default empty state if no file found
    }

    // Save macros state to JSON
    private static void saveMacroState(MacroState state) {
        Path path = Path.of(CONFIG_DIRECTORY, MACRO_FILE);
        try {
            Files.createDirectories(path.getParent());
            BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
            GSON.toJson(state, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a macro to the list
    public int addMacro(String type, String command) {
        MacroState state = loadMacroState();

        if ("add".equals(type)) {
            state.add.commands.add(command);
        } else if ("remove".equals(type)) {
            state.remove.commands.add(command);
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Invalid macro type: " + type), false);
            return 0;
        }

        saveMacroState(state);
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§aAdded macro for §6/ps " + type + ": " + command), false);
        return 1;
    }

    // Remove a macro from the list
    public int removeMacro(String type, String command) {
        MacroState state = loadMacroState();

        if ("add".equals(type)) {
            state.add.commands.remove(command);
        } else if ("remove".equals(type)) {
            state.remove.commands.remove(command);
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Invalid macro type: " + type), false);
            return 0;
        }

        saveMacroState(state);
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§cRemoved macro for §b/ps " + type + ": " + command), false);
        return 1;
    }

    public int toggleMacroFlip(String type){
        MacroState state = loadMacroState();
        boolean enabled = false;

        if ("add".equals(type)) {
            state.add.enabled = !state.add.enabled;
            enabled = state.add.enabled;
        } else if ("remove".equals(type)) {
            state.remove.enabled = !state.remove.enabled;
            enabled = state.remove.enabled;
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Invalid macro type: " + type), false);
            return 0;
        }

        saveMacroState(state);
        MinecraftClient.getInstance().player.sendMessage(Text.literal((enabled ? "§aEnabled" : "§cDisabled") + " macros for /ps " + type), false);
        return 1;
    }

    // Toggle macro state for /ps add or /ps remove
    public int toggleMacro(String type, boolean enabled) {
        MacroState state = loadMacroState();

        if ("add".equals(type)) {
            state.add.enabled = enabled;
        } else if ("remove".equals(type)) {
            state.remove.enabled = enabled;
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Invalid macro type: " + type), false);
            return 0;
        }

        saveMacroState(state);
        MinecraftClient.getInstance().player.sendMessage(Text.literal((enabled ? "§aEnabled" : "§cDisabled") + " macros for /ps " + type), false);
        return 1;
    }

    // Execute macros for /ps add or /ps remove
    private void executeMacros(String type, String playerName) {
        MacroState state = loadMacroState();

        List<String> commandsToExecute;
        if ("add".equals(type) && state.add.enabled) {
            commandsToExecute = state.add.commands;
        } else if ("remove".equals(type) && state.remove.enabled) {
            commandsToExecute = state.remove.commands;
        } else {
            return;
        }

        for (String command : commandsToExecute) {
            String formattedCommand = command.replace("${name}", playerName);
            // Execute the command in-game using the player name
            MinecraftClient.getInstance().player.networkHandler.sendChatCommand((formattedCommand));
        }
    }

    // Method to clear players from the file
    public static void clearPlayers() {
        Path playerFilePath = FileHandler.findOrCreatePlayerFile();
        try {
            Files.writeString(playerFilePath, ""); // Overwrite the file with an empty string
            System.out.println("§cPlayer list cleared.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static int clearMacros(String type) {
        MacroState state = loadMacroState();

        // Clear all commands
        if (Objects.equals(type, "add"))
        {
            state.add.commands.clear();
            saveMacroState(state);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§cCleared all §aadd§c macros."), false);
            return 1;
        } else if (Objects.equals(type, "remove")) {
            state.remove.commands.clear();
            saveMacroState(state);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§cCleared all §aremove§c macros."), false);
            return 1;
        }
        else if (!Objects.equals(type, "all"))
        {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§cNothing has been Cleared."), false);
            return 1;
        }

        state.add.commands.clear();
        state.remove.commands.clear();

        // Save the updated state back to the JSON file
        saveMacroState(state);
        MinecraftClient.getInstance().player.sendMessage(Text.literal("§cCleared all macros."), false);
        return 1; // Success
    }

    // Existing addPlayer method, now integrated with macros
    public int addPlayer(String playerName) {
        Set<String> players = readPlayers();
        if (players.add(playerName)) {
            writePlayers(players);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§aAdded player:§6 " + playerName), false);
            executeMacros("add", playerName);  // Trigger the add macros
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Player already exists:§6 " + playerName), false);
        }
        return 1;
    }

    // Existing removePlayer method, now integrated with macros
    public int removePlayer(String playerName) {
        Set<String> players = readPlayers();
        if (players.remove(playerName)) {
            writePlayers(players);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§aRemoved player:§6 " + playerName), false);
            executeMacros("remove", playerName);  // Trigger the remove macros
        } else {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4Player not found:§6 " + playerName), false);
        }
        return 1;
    }

    public int listPlayers() {
        Set<String> players = readPlayers();
        int totalPlayers = players.size(); // Get the total number of players

        if (totalPlayers == 0) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4No players tracked."), false);
        } else {
            // Send a message with the total number of players and their names
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§cTracked players §6(" + totalPlayers + "):§a " + String.join(", ", players)), false);
        }
        return 1;
    }

    // JSON structure to store macro state
    public static class MacroState {
        public MacroSet add = new MacroSet();
        public MacroSet remove = new MacroSet();
    }

    // JSON structure for each type (add/remove)
    public static class MacroSet {
        public boolean enabled = false;
        public List<String> commands = new ArrayList<>();
    }

    // Read the player names from the configuration file
    private Set<String> readPlayers() {
        Set<String> players = new HashSet<>();
        try {
            Path path = Path.of(CONFIG_DIRECTORY, CONFIG_FILE);
            if (Files.exists(path)) {
                BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        players.add(line.trim()); // Keep original case
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return players;
    }

    // Write the player names to the configuration file
    private void writePlayers(Set<String> players) {
        try {
            // Ensure the config directory exists
            Files.createDirectories(Path.of(CONFIG_DIRECTORY));
            BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(CONFIG_DIRECTORY, CONFIG_FILE).toFile()));
            for (String player : players) {
                writer.write(player); // Write as-is
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
