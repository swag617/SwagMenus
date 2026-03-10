# Action Types

Actions are strings placed in `left_click_commands`, `right_click_commands`, `shift_left_click_commands`, `shift_right_click_commands`, or `middle_click_commands`. They execute top to bottom when a player clicks the item.

## Full Reference

| Action | Description |
|---|---|
| `[player] <cmd>` | Run a command as the player |
| `[console] <cmd>` | Run a command as console |
| `[op] <cmd>` | Run a command with temporary OP |
| `[chat] <message>` | Send a chat message as the player |
| `[message] <text>` | Send a private message to the player |
| `[broadcast] <text>` | Broadcast a message to all players |
| `[close]` | Close the inventory |
| `[open] <menu>` | Open another menu |
| `[back]` | Return to the previously opened menu |
| `[refresh]` | Refresh the current menu's items |
| `[nextpage]` | Advance to the next page |
| `[prevpage]` | Go back to the previous page |
| `[sound] <sound> [vol] [pitch]` | Play a sound to the player |
| `[title] <title>;<subtitle>` | Show a title and subtitle |
| `[actionbar] <text>` | Show an action bar message |
| `[delay] <ticks>` | Delay before executing the next action |
| `[json] <json>` | Send a raw JSON tellraw message |

## Examples

**Buy an item with feedback:**
```yaml
left_click_commands:
  - "[console] eco take %player_name% 100"
  - "[console] give %player_name% diamond 1"
  - "[sound] ENTITY_PLAYER_LEVELUP 1.0 1.2"
  - "[title] &aPurchased!;&7You bought a Diamond."
  - "[message] &aDiamond added to your inventory."
  - "[close]"
```

**Delayed teleport:**
```yaml
left_click_commands:
  - "[message] &aTeleporting in 3 seconds..."
  - "[close]"
  - "[delay] 60"
  - "[player] spawn"
```

**Play a sound with custom volume and pitch:**
```yaml
left_click_commands:
  - "[sound] ENTITY_PLAYER_LEVELUP 0.8 1.5"
```

**Navigate between menus:**
```yaml
left_click_commands:
  - "[open] server_selector"
```

**Broadcast announcement:**
```yaml
left_click_commands:
  - "[broadcast] &6%player_name% &ejust bought the VIP rank!"
  - "[console] lp user %player_name% parent set vip"
```

> **Tip:** Placeholders like `%player_name%` work in all action strings when PlaceholderAPI is installed.
