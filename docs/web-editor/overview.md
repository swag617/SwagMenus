# Web Editor Overview

SwagMenus includes a fully browser-based visual menu editor. It runs inside the plugin itself — no separate server, no external tools, no installation required.

## Features

- **Visual slot grid** with real Minecraft item icons pulled from the Minecraft Wiki
- **Live color preview** — see `&6`, `&#FF5500` rendered in real time as you type
- **Drag and drop** — move items between slots by dragging
- **Fill Item editor** — configure the background fill item visually
- **Open GUI dropdown** — add `[open] menu_name` to any click action without typing
- **Undo / Redo** — `Ctrl+Z` / `Ctrl+Shift+Z`, with buttons in the top bar
- **Auto-reload** — saving in the editor immediately reloads the menu on the server
- **Password protected** — set your own password in `config.yml`

## Accessing the Editor

Run in-game:

```
/sm editor
```

A clickable link appears in chat. Open it in your browser, enter the password from `config.yml`, and you're in.

If your server is remote (VPS/dedicated), replace `localhost` in the URL with your server's IP address:

```
http://your.server.ip:8080/editor
```

## Changing the Port

```
/sm port 9090
```

This restarts the web server on the new port and saves it to `config.yml` immediately.

## Security

- The editor is protected by a password set in `config.yml` under `web_editor.password`
- Sessions expire after `token_expiry_minutes` minutes of inactivity (default 30)
- Set `bind-address: "127.0.0.1"` to restrict the editor to localhost only

> **Important:** Change the default password before exposing port 8080 to the internet.
