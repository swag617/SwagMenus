# Commands & Permissions

## Commands

| Command | Description | Permission |
|---|---|---|
| `/sm open <menu>` | Open a menu for yourself | `swagmenus.open` |
| `/sm open <menu> <player>` | Open a menu for another player | `swagmenus.open.others` |
| `/sm list` | List all loaded menus | `swagmenus.list` |
| `/sm reload` | Reload all menus | `swagmenus.reload` |
| `/sm reload <menu>` | Reload a specific menu | `swagmenus.reload` |
| `/sm info <menu>` | Show menu details | `swagmenus.list` |
| `/sm execute <player> <action>` | Execute an action on a player | `swagmenus.execute` |
| `/sm editor` | Open the web editor link | `swagmenus.admin` |
| `/sm port <number>` | Change the web editor port | `swagmenus.admin` |
| `/<open_command>` | Open menu via its custom command | `swagmenus.open` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `swagmenus.open` | Open menus for yourself | `true` |
| `swagmenus.open.others` | Open menus for other players | `op` |
| `swagmenus.list` | List and inspect menus | `op` |
| `swagmenus.reload` | Reload menus | `op` |
| `swagmenus.execute` | Execute actions on players | `op` |
| `swagmenus.admin` | Web editor access, port changes, admin notifications | `op` |

## Notes

**Custom open commands** registered via `open_command` / `open_commands` in the menu config always require `swagmenus.open`. You can override this by granting or denying the permission through LuckPerms or another permissions plugin.

**Admin notifications** — players with `swagmenus.admin` receive in-game chat messages when a menu fails to load or a config error is detected, making it easy to catch mistakes without watching the console.

**Console** — all commands work from the console. Commands that require a player (like `/sm open <menu>`) need a target player specified: `/sm open mymenu Steve`.
