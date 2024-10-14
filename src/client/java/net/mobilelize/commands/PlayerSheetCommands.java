package net.mobilelize.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.mobilelize.Casting;
import net.mobilelize.suggestions.Suggestions;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.mobilelize.commands.PlayerSheetFunctions.*;
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
                            .then(literal("toggle")
                                    .executes(context -> togglePlayerSource())
                                    .then(literal("addKeyBind").executes(context -> {
                                        return toggleAddKeyBind();  // Executes the toggleAddKeyBind method
                                    }))
                                    .then(literal("removeKeyBind").executes(context -> {
                                        return toggleRemoveKeyBind();  // Executes the toggleRemoveKeyBind method
                                    }))
                                    .then(literal("getPlayers").executes(context -> {
                                        return togglePlayerSource();  // Executes the listPlayers method
                                    }))
                            )

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
                                    .executes(context -> showMacroList())
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

        client.player.sendMessage(Text.literal(
                "\n§8§m------------------------------------------\n" +
                "§f>>>§6§lPlayer Sheets Help Menu§r§f<<<\n" +
                "§8§m------------------------------------------\n" +
                "\n" +
                "§7§lCommands:\n" +
                "§6/ps help §7- §fShow this help menu.\n" +
                "§6/ps add [playerName] §7- §fAdd a player.\n" +
                "§6/ps remove [playerName] §7- §fRemove a player.\n" +
                "§6/ps list §7- §fList all tracked players.\n" +
                "§6/ps toggle [addKeyBind/removeKeyBind/getPlayers]§7- §fToggle between Tab List and Rendered Players, and a toggle for Add and Remove Key Bind.\n" +
                "§6/ps a [playerName] §7- §fShort command to add a player.\n" +
                "§6/ps r [playerName] §7- §fShort command to remove a player.\n" +
                "§6/ps l §7- §fShort command to list all players.\n" +
                "\n" +
                "§8§m------------------------------------------\n" +
                "§f>>>§6§lMacros Management§r§f<<<\n" +
                "§8§m------------------------------------------\n" +
                "\n" +
                "§7§lCommands:\n" +
                "§6/ps macro add [add/remove] [command] §7- §fAdd a macro to /ps add or /ps remove.\n" +
                "§6/ps macro remove [add/remove] [command] §7- §fRemove a macro from /ps add or /ps remove.\n" +
                "§6/ps macro toggle [add/remove] [true/false] §7- §fEnable or disable macros for /ps add or /ps remove.\n" +
                "§6/ps macro list §7- §fList all macros.\n" +
                "§6/ps macro clear [add/remove/all] §7- §fClear macros for /ps add, /ps remove, or all.\n" +
                "\n" +
                "§8§m------------------------------------------\n" +
                "§cMacros allow you to automate commands you frequently use.\n" +
                "§cYou can create two types of macros:\n" +
                "§7- §a'add': Executes commands when a player is added.\n" +
                "§7- §a'remove': Executes commands when a player is removed.\n" +
                "\n" +
                "§7For example:\n" +
                "§6/ps macro add §aadd §6gamemode creative ${name} §7- §fGive creative mode to the player added to the list.\n" +
                "§6/ps macro add §aremove §6${chat} Goodbye ${name} §7- §fSends a goodbye message in chat with the removed player name from the list.\n" +
                "\n" +
                "§8§m------------------------------------------\n"), false);

        return 1;
    }

    private static int showMacroList() {
        MinecraftClient client = MinecraftClient.getInstance();

        // Load the current macro states
        PlayerSheetFunctions.MacroState state = functions.loadMacroState();

        StringBuilder macroList = new StringBuilder("\n§8§m------------------------------------------\n" +
                "§f>>>§6§lCurrent Macros§r§f<<<\n" +
                "§8§m------------------------------------------\n" + "\n§6Add Macros (Active: " + (state.add.enabled ? "§aEnabled" : "§cDisabled") + "§6):");

        for (String command : state.add.commands) {
            macroList.append("\n§f  - ").append(command);
        }

        macroList.append("\n\n").append("§6Remove Macros (Active: ").append(state.remove.enabled ? "§aEnabled" : "§cDisabled").append("§6):");

        // Add details for the 'remove' macros
        for (String command : state.remove.commands) {
            macroList.append("\n§f  - ").append(command);
        }

        //End of message
        client.player.sendMessage(Text.literal(macroList + "\n\n§8§m------------------------------------------\n"), false);

        return 1;
    }
}
