# Web Editor Overview

SwagMenus includes a fully browser-based visual menu editor. It does **not** run its own HTTP
server — it's mounted as a module inside [SwagAPI](https://github.com/swag617/SwagAPI)'s shared
web server, so it requires SwagAPI to be installed and enabled alongside SwagMenus.

## Features

- **Visual slot grid** with real Minecraft item icons pulled from the Minecraft Wiki
- **Live color preview** — see `&6`, `&#FF5500` rendered in real time as you type
- **Drag and drop** — move items between slots by dragging
- **Fill Item editor** — configure the background fill item visually
- **Open GUI dropdown** — add `[open] menu_name` to any click action without typing
- **View / Click Requirement editor** — add and edit gating requirements visually
- **Undo / Redo** — `Ctrl+Z` / `Ctrl+Shift+Z`, with buttons in the top bar
- **Auto-reload** — saving in the editor immediately reloads the menu on the server
- **Shared login** — authenticated by SwagAPI's own session-cookie login; SwagMenus has no
  password or login screen of its own

## Requirements

- [SwagAPI](https://github.com/swag617/SwagAPI) must be installed and enabled — it provides the
  actual HTTP server and login system the editor runs on
- `web_editor.enabled: true` in SwagMenus' `config.yml` (default)

If SwagAPI isn't installed or enabled, SwagMenus still works normally (menus, commands, actions,
requirements) — only the web editor is unavailable. The console logs a warning on startup in this
case, and `/sm editor` tells you the editor isn't available.

## Accessing the Editor

Run in-game:

```
/sm editor
```

If the editor is available, a clickable link appears in chat pointing at the URL SwagAPI mounted
it under (something like `http://your.server:port/swagapi/swagmenus/`). Open it in your browser.

If SwagAPI has its own login enabled, you'll be redirected to SwagAPI's shared login page first —
sign in there once and you're in every module SwagAPI hosts, including this one. There is nothing
SwagMenus-specific to log into.

## Security

Access control is entirely delegated to SwagAPI: whatever host/port SwagAPI's web server binds
to, and whatever login SwagAPI has configured, applies here too. See SwagAPI's own documentation
for securing its web server (binding address, authentication, HTTPS/reverse-proxy setup, etc.) —
SwagMenus does not add or need any authentication of its own.
