package net.mobilelize;

import net.fabricmc.api.ClientModInitializer;
import net.mobilelize.commands.PlayerSheetCommands;
import net.mobilelize.commands.PlayerSheetFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class PlayerSheetsClient implements ClientModInitializer {
	public static final String MOD_ID = "player-sheets";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		PlayerSheetCommands.registerClientCommands();
		PlayerSheetFunctions.playerTracker();
		LOGGER.info("Player Sheets Initialized");
	}
}