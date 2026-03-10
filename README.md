<div align="center">

<br>

# ✦ SwagMenus

<p align="center">
  <img src="https://img.shields.io/badge/Paper-1.21.x-667eea?style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgo=" alt="Paper 1.21.x">
  <img src="https://img.shields.io/badge/Java-21-764ba2?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/PlaceholderAPI-supported-5fe05f?style=for-the-badge" alt="PAPI">
  <img src="https://img.shields.io/badge/License-All%20Rights%20Reserved-f0c060?style=for-the-badge" alt="License">
</p>

**A deeply configurable, player-experience-first GUI menu plugin for Paper servers.**
Define menus in YAML *or* build them visually with the built-in web editor — no client mods, no external setup.

<br>

</div>

---

## ✦ Features

- **YAML-based menus** — one file per menu, hot-reloaded on save
- **Built-in web editor** — full visual GUI builder accessible from your browser
- **15+ action types** — `[player]`, `[console]`, `[open]`, `[back]`, `[close]`, `[sound]`, `[title]`, `[delay]`, `[nextpage]`, `[prevpage]`, and more
- **7 requirement types** — permission, money, expression, string equals/contains, regex, numeric comparisons
- **PlaceholderAPI support** — use any PAPI placeholder in names, lore, commands, and requirements
- **Pagination** — multi-page menus with `[nextpage]` / `[prevpage]` actions
- **Navigation history** — `[back]` returns to the previously opened menu
- **Auto-refresh** — menus update live on a configurable tick interval
- **Fill items** — auto-fill empty slots with a background item
- **Multi-slot items** — place one item across multiple slots with `slots: [0, 1, 2]`
- **Skull support** — player heads with `skull_owner: %player_name%` or base64 texture
- **Hex color support** — `&#RRGGBB` alongside standard `&` color codes
- **File watcher** — menus reload automatically when you save the YAML file

---

## ✦ Installation

1. Download `SwagMenus.jar` from [Releases](https://github.com/swag617/SwagMenus/releases)
2. Drop it into your server's `plugins/` folder
3. Restart your server
4. Two example menus are generated automatically in `plugins/SwagMenus/menus/`

> **Requirements:** Paper 1.21.x — Java 21 — PlaceholderAPI *(optional but recommended)*

---

## ✦ Web Editor

SwagMenus includes a browser-based visual menu editor. No separate installation needed — it runs directly inside the plugin.

**Starting the editor:**
```
/sm editor
```
Click the link that appears in chat, enter your password, and you're in.

**Features:**
- Visual slot grid with real Minecraft item icons
- Live color code preview (`&6`, `&#FF5500`)
- Drag-and-drop to move items between slots
- Fill item editor
- Open GUI dropdown — add `[open] menu_name` to any click action in one click
- Undo / Redo (`Ctrl+Z` / `Ctrl+Shift+Z`)
- Auto-save and reload on the server the moment you hit Save

**Configuration:**
```yaml
web_editor:
  enabled: true
  port: 8080
  bind-address: "0.0.0.0"   # 0.0.0.0 = all interfaces, 127.0.0.1 = localhost only
  password: "changeme"       # change this!
  token_expiry_minutes: 30
```

**Change port in-game:**
```
/sm port 9090
```

---

## ✦ Commands

| Command | Description | Permission |
|---|---|---|
| `/sm open <menu>` | Open a menu for yourself | `swagmenus.open` |
| `/sm open <menu> <player>` | Open a menu for another player | `swagmenus.open.others` |
| `/sm list` | List all loaded menus | `swagmenus.list` |
| `/sm reload` | Reload all menus | `swagmenus.reload` |
| `/sm reload <menu>` | Reload a specific menu | `swagmenus.reload` |
| `/sm info <menu>` | Show menu details | `swagmenus.list` |
| `/sm execute <player> <action>` | Execute an action on a player | `swagmenus.execute` |
| `/sm editor` | Open the web editor | `swagmenus.admin` |
| `/sm port <number>` | Change the web editor port | `swagmenus.admin` |
| `/<open_command>` | Open a menu via its custom command | `swagmenus.open` |

---

## ✦ Menu Configuration

Menus live in `plugins/SwagMenus/menus/`. Each `.yml` file is one menu.

```yaml
menu_title: "&8&lMy Menu"
menu_size: 54              # 9 / 18 / 27 / 36 / 45 / 54
open_command: mymenu       # registers /mymenu
open_commands:             # additional aliases
  - mymenu
  - menu
update_interval: 60        # ticks between auto-refresh (0 = off)

fill_item:
  material: GRAY_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true

items:
  my_item:
    material: DIAMOND
    slot: 13               # or slots: [0, 1, 2]
    display_name: "&bShiny Diamond"
    lore:
      - "&7A beautiful diamond."
      - "&7Worth: &e100 coins"
    amount: 1
    glow: true
    hide_flags: true
    page: 0                # 0 = all pages, 1+ = specific page
    left_click_commands:
      - "[sound] ENTITY_PLAYER_LEVELUP"
      - "[message] &aYou clicked the diamond!"
      - "[open] other_menu"
    right_click_commands:
      - "[message] &cRight clicked!"
```

### Slot Layout

```
[ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]   row 1
[ 9][10][11][12][13][14][15][16][17]   row 2
[18][19][20][21][22][23][24][25][26]   row 3
[27][28][29][30][31][32][33][34][35]   row 4
[36][37][38][39][40][41][42][43][44]   row 5
[45][46][47][48][49][50][51][52][53]   row 6
```

Named aliases: `top_left`, `top_middle`, `top_right`, `center`, `bottom_left`, `bottom_middle`, `bottom_right`

---

## ✦ Action Types

Actions go in `left_click_commands`, `right_click_commands`, `shift_left_click_commands`, `shift_right_click_commands`, or `middle_click_commands`.

| Action | Description |
|---|---|
| `[player] <cmd>` | Run command as the player |
| `[console] <cmd>` | Run command as console |
| `[op] <cmd>` | Run command as OP |
| `[chat] <message>` | Send a chat message as the player |
| `[message] <text>` | Send a message to the player |
| `[broadcast] <text>` | Broadcast to all players |
| `[close]` | Close the inventory |
| `[open] <menu>` | Open another menu |
| `[back]` | Return to the previously opened menu |
| `[refresh]` | Refresh the current menu |
| `[nextpage]` | Advance to the next page |
| `[prevpage]` | Go to the previous page |
| `[sound] <sound> [vol] [pitch]` | Play a sound |
| `[title] <title>;<subtitle>` | Show a title |
| `[actionbar] <text>` | Show an action bar message |
| `[delay] <ticks>` | Delay before the next action |
| `[json] <json>` | Send a raw JSON tellraw message |

---

## ✦ Requirements

Requirements control whether an item is visible or a click action executes.

```yaml
view_requirement:
  requirements:
    my_check:
      type: has_permission
      permission: "my.permission"
  deny_commands:
    - "[message] &cYou don't have permission!"

click_requirement:
  left_click_requirements:
    requirements:
      balance_check:
        type: ">="
        placeholder: "%vault_balance%"
        value: "100"
    deny_commands:
      - "[message] &cYou need $100 to do that!"
```

| Type | Description |
|---|---|
| `has_permission` | Player has a permission node |
| `has_money` | Player has ≥ amount (requires Vault) |
| `expression` | Evaluate `%placeholder% >= value` |
| `string equals` | Two placeholder strings are equal |
| `string contains` | String contains a value |
| `regex matches` | Regex match on a placeholder |
| `>=` `>` `<=` `<` `==` `!=` | Numeric placeholder comparisons |

---

## ✦ Chaining Menus

Menus chain together naturally using `[open]` and `[back]`. SwagMenus tracks navigation history per player automatically.

```yaml
# Main menu item
left_click_commands:
  - "[open] confirmation_menu"

# Confirmation menu — yes button
left_click_commands:
  - "[console] give %player_name% diamond 1"
  - "[message] &aPurchased!"
  - "[open] success_menu"

# Confirmation menu — no button
left_click_commands:
  - "[back]"   # returns to main menu
```

---

## ✦ Pagination

Add `page: N` to items to show them only on a specific page. Items with `page: 0` show on all pages — use this for navigation buttons and borders.

```yaml
items:
  next_button:
    material: ARROW
    slot: 53
    display_name: "&aNext Page"
    page: 0                  # always visible
    left_click_commands:
      - "[nextpage]"

  item_page_1:
    material: DIAMOND
    slot: 10
    page: 1

  item_page_2:
    material: EMERALD
    slot: 10
    page: 2
```

---

## ✦ PlaceholderAPI

Any `%placeholder%` from PlaceholderAPI works in:
- `menu_title`
- `display_name`
- `lore`
- `skull_owner`
- All action command strings
- Requirement values

PlaceholderAPI is a soft dependency — the plugin loads without it, but placeholders won't resolve.

---

<div align="center">

**Built by [SwagDev](https://swag617.github.io/) · [Portfolio](https://swag617.github.io/) · [SwagFishing](https://swag617.github.io/swagfishing-docs/)**

</div>
