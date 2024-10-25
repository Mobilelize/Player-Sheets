# Player Sheets Mod

Welcome to the Player Sheets Mod! This mod enhances your Minecraft experience by allowing you to manage and track players effortlessly. With easy-to-use commands and customizable settings, keeping track of your friends has never been easier!

## Features

- **Add/Remove Players**: Quickly add or remove players from your tracking list with simple commands.
- **Key Bindings**: 
  - **Add Player**: Press **V** to add the player you are currently looking at.
  - **Remove Player**: Press **B** to remove the player you are currently looking at.
- **Toggle Settings**: Toggle between using the Tab List or Rendered Players, as well as enabling/disabling key binds directly from the config settings.
- **Macros Management**: Automate commands you frequently use with macros for adding and removing players.
- **Command Prefix**: The default command prefix is **ps** for easy access to commands.

## Commands

### Player Commands

```
/ps help              - Show this help menu.
/ps add [playerName]  - Add a player to your list.
/ps remove [playerName]- Remove a player from your list.
/ps list              - List all tracked players.
/ps toggle [addKeyBind/removeKeyBind/getPlayers] - Toggle settings.
/ps settings          - List all settings.
```

### Short Commands

```
/ps a [playerName]    - Short command to add a player.
/ps r [playerName]    - Short command to remove a player.
/ps l                 - Short command to list all players.
```

### Macros Management

```
/ps macro add [add/remove] [command]       - Add a macro for adding/removing players.
/ps macro remove [add/remove] [command]    - Remove a macro from adding/removing players.
/ps macro toggle [add/remove] [true/false] - Enable or disable macros for adding/removing.
/ps macro list                             - List all macros.
/ps macro clear [add/remove/all]           - Clear macros for adding/removing or all.
```

## Key Bindings

- **Add Player**: **V**
- **Remove Player**: **B**

## Configuration

- All settings can be adjusted in the config file, including:
  - `useTabList: true`
  - `toggleAddKeyBind: false`
  - `toggleRemoveKeyBind: false`

## Suggestions

We’re always looking to improve! If you have any suggestions for additional features or changes, feel free to share!
