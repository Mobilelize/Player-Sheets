package net.mobilelize.suggestions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.mobilelize.FileHandler;
import net.mobilelize.commands.PlayerSheetFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil.GSON;

public class Suggestions {

    private static final String CONFIG_DIRECTORY = "config/player-sheets";
    private static final String CONFIG_FILE = "player-sheet.txt";
    private static final String MACRO_FILE_NAME = "player-macros.json";

    /**
     * Gets all online players on the server.
     *
     * @return An array of online player names.
     */
    public static String[] getAllRenderedPlayers() {
        List<String> onlinePlayers = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();

        // Ensure we have a valid world and the player list
        if (client.world != null) {
            client.world.getPlayers().forEach(player -> onlinePlayers.add(player.getName().getString()));
        }

        return onlinePlayers.toArray(new String[0]); // Convert List to String[]
    }

    public static String[] getAllOnlinePlayers() {
        if (!PlayerSheetFunctions.useTabList)
        {
            return getAllRenderedPlayers();
        }
        List<String> onlinePlayers = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();

        // Ensure the client has a connection to the server and get the tab list
        if (client.getNetworkHandler() != null) {
            // Fetch all entries from the player's tab list (includes all online players)
            client.getNetworkHandler().getPlayerList().forEach(playerListEntry -> {
                onlinePlayers.add(playerListEntry.getProfile().getName());
            });
        }
        return onlinePlayers.toArray(new String[0]); // Convert List to String[]
    }

    /**
     * Gets all names from the player sheet configuration file.
     *
     * @return An array of player names from the config file.
     */
    public static String[] getAllNamesFromConfig() {
        List<String> names = readPlayers();
        return names.toArray(new String[0]); // Convert List to String[]
    }

    // Helper method to read player names from the player-sheet.txt file
    private static List<String> readPlayers() {
        List<String> players = new ArrayList<>();
        try {
            Path path = Path.of(CONFIG_DIRECTORY, CONFIG_FILE);
            if (Files.exists(path)) {
                BufferedReader reader = Files.newBufferedReader(path);
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

    /**
     * Gets all macros commands of the specified type from the macro file.
     *
     * @param type The type of macros to retrieve ("add" or "remove").
     * @return An array of command strings.
     */
    public static Iterable<String> getMacroCommands(String type) {
        List<String> commands = new ArrayList<>();
        Path path = Path.of(CONFIG_DIRECTORY, MACRO_FILE_NAME);

        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                MacroState state = GSON.fromJson(reader, MacroState.class);

                if ("add".equalsIgnoreCase(type)) {
                    commands.addAll(state.add.commands);
                } else if ("remove".equalsIgnoreCase(type)) {
                    commands.addAll(state.remove.commands);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return List.of(commands.toArray(new String[0])); // Convert List to String[]
    }

    // JSON structure to match the macro file
    private static class MacroState {
        public MacroSet add = new MacroSet();
        public MacroSet remove = new MacroSet();
    }

    private static class MacroSet {
        public boolean enabled = false;
        public List<String> commands = new ArrayList<>();
    }
}
