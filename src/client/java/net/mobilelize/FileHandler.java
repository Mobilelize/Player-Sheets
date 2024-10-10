package net.mobilelize;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;
import org.spongepowered.include.com.google.gson.JsonElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileHandler {

    private static final String DIRECTORY_NAME = "player-sheets";  // Directory in the config folder
    private static final String PLAYER_FILE_NAME = "player-sheet.txt";        // The file that will store player names
    private static final String MACRO_FILE_NAME = "player-macros.json";       // The file that will store macros
    private static final String CONFIG_FILE = "settings.txt";

    // Get the path to the text config file
    private static Path getConfigFilePath() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(DIRECTORY_NAME);
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir); // Create directory if it doesn't exist
            }
            if (!Files.exists(configFile)) {
                Files.createFile(configFile); // Create file if it doesn't exist
                Files.write(configFile, List.of("useTabList : true")); // Default to 'true'
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configFile;
    }

    // Method to load the value of useTabList from the config file
    public static boolean loadUseTabList() {
        Path configFile = getConfigFilePath();
        try {
            List<String> lines = Files.readAllLines(configFile);
            for (String line : lines) {
                if (line.startsWith("useTabList :")) {
                    return Boolean.parseBoolean(line.split(":")[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true; // Default to 'true' if not found or error
    }

    // Method to save the value of useTabList to the config file
    public static void saveUseTabList(boolean useTabList) {
        Path configFile = getConfigFilePath();
        try {
            Files.write(configFile, List.of("useTabList : " + useTabList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Method to find or create the player file
    public static Path findOrCreatePlayerFile() {
        return findOrCreateFile(PLAYER_FILE_NAME, null);
    }

    // Method to find or create the macro file with default data
    public static Path findOrCreateMacroFile() {
        // Default structure for the macro file (empty macro sets for add/remove)
        String defaultMacroData = GSON.toJson(new DefaultMacroState());
        return findOrCreateFile(MACRO_FILE_NAME, defaultMacroData);
    }

    // Method to find or create a file with the specified name and optionally write default content
    private static Path findOrCreateFile(String fileName, String defaultContent) {
        Path configDir = FabricLoader.getInstance().getConfigDir();            // Get the config directory
        Path customDir = configDir.resolve(DIRECTORY_NAME);                    // Define the sub-directory path
        Path filePath = customDir.resolve(fileName);                           // Define the full path to the file

        try {
            // Create directory if it doesn't exist
            if (!Files.exists(customDir)) {
                Files.createDirectories(customDir);
                System.out.println("Directory created: " + customDir.toString());
            }

            // Create file if it doesn't exist and optionally write default content
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("File created: " + filePath.toString());

                // Write default content if provided
                if (defaultContent != null) {
                    Files.writeString(filePath, defaultContent, StandardOpenOption.WRITE);
                    System.out.println("Default content written to: " + filePath.toString());
                }
            } else {
                System.out.println("File already exists: " + filePath.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();  // Handle any I/O exceptions
        }

        return filePath;  // Return the file path
    }

    // Class to define the default macro structure
    private static class DefaultMacroState extends JsonElement {
        public MacroSet add = new MacroSet();
        public MacroSet remove = new MacroSet();
    }

    // Class representing a macro set (enabled and command list)
    private static class MacroSet {
        public boolean enabled = false;
        public java.util.List<String> commands = new java.util.ArrayList<>();
    }
}
