package net.mobilelize.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.mobilelize.suggestions.Suggestions;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.mobilelize.commands.PlayerSheetFunctions.clearMacros;
import static net.mobilelize.suggestions.Suggestions.*;

public class PlayerSheetCommands {

    private static final PlayerSheetFunctions functions = new PlayerSheetFunctions();

    private static final SuggestionProvider<FabricClientCommandSource> macroTypeSuggestions = (context, builder) -> {
        if (CommandSource.shouldSuggest(builder.getRemaining().toLowerCase(), "add")) {
            builder.suggest("add");
        }
        if (CommandSource.shouldSuggest(builder.getRemaining().toLowerCase(), "remove")) {
            builder.suggest("remove");
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> getOnlinePlayers = (context, builder) -> {
        String[] suggestions = getAllOnlinePlayers(); // Get online players
        for (String player : suggestions) {
            if (CommandSource.shouldSuggest(builder.getRemaining().toLowerCase(), player.toLowerCase())) {
                builder.suggest(player);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<FabricClientCommandSource> getConfigNames = (context, builder) -> {
        String[] suggestions = getAllNamesFromConfig(); // Get names from config
        for (String player : suggestions) {
            if (CommandSource.shouldSuggest(builder.getRemaining().toLowerCase(), player.toLowerCase())) {
                builder.suggest(player);
            }
        }
        return builder.buildFuture();
    };

    public static void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Register the main /ps command
            Suggestions Suggestion = null;
            dispatcher.register(
                    literal("ps").executes(context -> showHelp())
                            .then(literal("help").executes(context -> showHelp()))
                            .then(literal("clear")
                                    .executes(context -> {
                                        // Clear the player list
                                        PlayerSheetFunctions.clearPlayers();

                                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§cCleared player list."), false);
                                        return 1; // Return success
                                    })
                            )
                            .then(literal("add")
                                    .then(argument("playerName", StringArgumentType.string())
                                            .suggests(getOnlinePlayers)
                                            .executes(context -> {
                                                String playerName = StringArgumentType.getString(context, "playerName");
                                                return functions.addPlayer(playerName);
                                            })
                                    )
                            )
                            .then(literal("a") // Short command for add
                                    .then(argument("playerName", StringArgumentType.string())
                                            .suggests(getOnlinePlayers)
                                            .executes(context -> {
                                                String playerName = StringArgumentType.getString(context, "playerName");
                                                return functions.addPlayer(playerName);
                                            })
                                    )
                            )
                            .then(literal("remove")
                                    .then(argument("playerName", StringArgumentType.string())
                                            .suggests(getConfigNames)
                                            .executes(context -> {
                                                String playerName = StringArgumentType.getString(context, "playerName");
                                                return functions.removePlayer(playerName);
                                            })
                                    )
                            )
                            .then(literal("r") // Short command for remove
                                    .then(argument("playerName", StringArgumentType.string())
                                            .suggests(getConfigNames)
                                            .executes(context -> {
                                                String playerName = StringArgumentType.getString(context, "playerName");
                                                return functions.removePlayer(playerName);
                                            })
                                    )
                            )
                            .then(literal("list").executes(context -> functions.listPlayers()))
                            .then(literal("l") // Short command for list
                                    .executes(context -> functions.listPlayers())
                            )
                            .then(literal("macro")
                                    .then(literal("add")
                                            .then(argument("type", StringArgumentType.word()) // Argument for "add" or "remove"
                                                    .suggests(macroTypeSuggestions)  // Suggest "add" and "remove"
                                                    .then(argument("command", StringArgumentType.greedyString()) // Command to add
                                                            .suggests((context, builder) -> {
                                                                // Determine which suggestions to provide based on the type entered
                                                                String type = context.getArgument("type", String.class);
                                                                if ("add".equalsIgnoreCase(type)) {
                                                                    return CommandSource.suggestMatching(getMacroCommands("add"), builder);
                                                                } else if ("remove".equalsIgnoreCase(type)) {
                                                                    return CommandSource.suggestMatching(getMacroCommands("remove"), builder);
                                                                }
                                                                // Default to empty suggestions if type is not recognized
                                                                return builder.buildFuture();
                                                            })//.suggests(getMacroCommands("add"))
                                                            .executes(context -> {
                                                                String type = StringArgumentType.getString(context, "type");
                                                                String command = StringArgumentType.getString(context, "command");
                                                                return functions.addMacro(type, command);
                                                            })
                                                    )
                                            )
                                    )
                                    .then(literal("remove")
                                            .then(argument("type", StringArgumentType.word()) // Argument for "add" or "remove"
                                                    .suggests(macroTypeSuggestions)  // Suggest "add" and "remove"
                                                    .then(argument("command", StringArgumentType.greedyString()) // Command to remove
                                                            .suggests((context, builder) -> {
                                                                // Determine which suggestions to provide based on the type entered
                                                                String type = context.getArgument("type", String.class);
                                                                if ("add".equalsIgnoreCase(type)) {
                                                                    return CommandSource.suggestMatching(getMacroCommands("add"), builder);
                                                                } else if ("remove".equalsIgnoreCase(type)) {
                                                                    return CommandSource.suggestMatching(getMacroCommands("remove"), builder);
                                                                }
                                                                // Default to empty suggestions if type is not recognized
                                                                return builder.buildFuture();
                                                            })
                                                            .executes(context -> {
                                                                String type = StringArgumentType.getString(context, "type");
                                                                String command = StringArgumentType.getString(context, "command");
                                                                return functions.removeMacro(type, command);
                                                            })
                                                    )
                                            )
                                    )
                                    .then(literal("toggle")
                                            .then(argument("type", StringArgumentType.word()) // Argument for "add" or "remove"
                                                    .suggests(macroTypeSuggestions)  // Suggest "add" and "remove"
                                                    .executes(context -> {
                                                        // Clear all macros
                                                        String type = StringArgumentType.getString(context, "type");
                                                        functions.toggleMacroFlip(type);
                                                        return 1; // Return success
                                                    })
                                                    .then(argument("enabled", BoolArgumentType.bool()) // true or false
                                                            .executes(context -> {
                                                                String type = StringArgumentType.getString(context, "type");
                                                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                                                return functions.toggleMacro(type, enabled);
                                                            })
                                                    )
                                            )
                                    )
                                    .then(literal("list")
                                            .executes(context -> {
                                                return showMacroList(); // Call the method to show the macro list
                                            }))
                                    .then(literal("clear")
                                            .executes(context -> {
                                                // Clear all macros
                                                clearMacros("all");
                                                return 1; // Return success
                                            })
                                            .then(argument("type", StringArgumentType.word()) // Argument for "add" or "remove"
                                                    .suggests(macroTypeSuggestions)  // Suggest "add" and "remove"
                                                    .executes(context -> {
                                                        String type = StringArgumentType.getString(context, "type");
                                                        return PlayerSheetFunctions.clearMacros(type);
                                                    })
                                            )
                                    )
            ));
        });
    }

    private static int showHelp() {
        MinecraftClient client = MinecraftClient.getInstance();

        client.player.sendMessage(Text.literal("§6/ps help §b- Show this message"), false);
        client.player.sendMessage(Text.literal("§6/ps add [playerName] §b- Add a player"), false);
        client.player.sendMessage(Text.literal("§6/ps remove [playerName] §b- Remove a player"), false);
        client.player.sendMessage(Text.literal("§6/ps list §b- List all tracked players"), false);
        client.player.sendMessage(Text.literal("§6/ps a [playerName] §b- Short command to add a player"), false);
        client.player.sendMessage(Text.literal("§6/ps r [playerName] §b- Short command to remove a player"), false);
        client.player.sendMessage(Text.literal("§6/ps l §b- Short command to list all players"), false);
        client.player.sendMessage(Text.literal("§6-=-=-=-=-"), false);

        // Add macro management to the help menu
        client.player.sendMessage(Text.literal("§b§l-=§6Macros Management§b=-"), false);
        client.player.sendMessage(Text.literal("§6/ps macro add [add/remove] [command] §b- Add a macro to /ps add or /ps remove"), false);
        client.player.sendMessage(Text.literal("§6/ps macro remove [add/remove] [command] §b- removes a macro from /ps add or /ps remove"), false);
        client.player.sendMessage(Text.literal("§6/ps macro toggle [add/remove] [true/false] §b- Enable or disable macros for /ps add or /ps remove"), false);
        client.player.sendMessage(Text.literal("§6-=-=-=-=-"), false);

        client.player.sendMessage(Text.literal("§cMacros allow you to automate commands you frequently use."), false);
        client.player.sendMessage(Text.literal("§cYou can create two types of macros:"), false);
        client.player.sendMessage(Text.literal("§a- 'add': Executes commands when a player is added."), false);
        client.player.sendMessage(Text.literal("§a- 'remove': Executes commands when a player is removed."), false);
        client.player.sendMessage(Text.literal("§6For example:"), false);
        client.player.sendMessage(Text.literal("§b/ps macro add §6add §asay Welcome ${name} §b- Sends a welcome message when a player is added."), false);
        client.player.sendMessage(Text.literal("§b/ps macro add §6remove §asay Goodbye ${name} §b- Sends a goodbye message when a player is removed."), false);
        client.player.sendMessage(Text.literal("§6-=-=-=-=-"), false);
        return 1;
    }

    private static int showMacroList() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Load the current macro states
        PlayerSheetFunctions.MacroState state = functions.loadMacroState();

        // Display current macro states
        client.player.sendMessage(Text.literal("§bCurrent Macros:"), false);

        // Add details for the 'add' macros
        client.player.sendMessage(Text.literal("§6Add Macros (Active: " + (state.add.enabled ? "§aEnabled" : "§cDisabled") + "):"), false);
        for (String command : state.add.commands) {
            client.player.sendMessage(Text.literal("  - " + command), false);
        }

        // Add details for the 'remove' macros
        client.player.sendMessage(Text.literal("§6Remove Macros (Active: " + (state.remove.enabled ? "§aEnabled" : "§cDisabled") + "):"), false);
        for (String command : state.remove.commands) {
            client.player.sendMessage(Text.literal("  - " + command), false);
        }

        return 1;
    }
}
