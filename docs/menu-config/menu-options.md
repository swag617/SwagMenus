# Menu Options

Every menu is a `.yml` file inside `plugins/SwagMenus/menus/`. The filename (without `.yml`) becomes the menu's ID.

## Top-Level Options

| Key | Type | Default | Description |
|---|---|---|---|
| `menu_title` | String | `"&8Menu"` | Title shown in the inventory. Supports `&` color codes and `&#RRGGBB` hex. |
| `menu_size` | Integer | `54` | Number of slots. Must be a multiple of 9 between 9 and 54. |
| `open_command` | String | *(none)* | Registers a custom command to open this menu. |
| `open_commands` | List | *(none)* | Additional command aliases. |
| `update_interval` | Integer | `0` | Ticks between auto-refresh. `0` disables it. 20 ticks = 1 second. |

## Example

```yaml
menu_title: "&8&lMy Menu"
menu_size: 54
open_command: mymenu
open_commands:
  - mymenu
  - menu
  - m
update_interval: 40
```

## Menu Sizes

| Value | Rows | Common Use |
|---|---|---|
| `9` | 1 | Hotbar-style menus |
| `18` | 2 | Small selection menus |
| `27` | 3 | Shop menus, confirmations |
| `36` | 4 | Medium menus |
| `45` | 5 | Large menus |
| `54` | 6 | Full-size menus, main hubs |

## Slot Layout

```
[ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]   row 1
[ 9][10][11][12][13][14][15][16][17]   row 2
[18][19][20][21][22][23][24][25][26]   row 3
[27][28][29][30][31][32][33][34][35]   row 4
[36][37][38][39][40][41][42][43][44]   row 5
[45][46][47][48][49][50][51][52][53]   row 6
```

Named slot aliases: `top_left` `top_middle` `top_right` `center` `bottom_left` `bottom_middle` `bottom_right`
