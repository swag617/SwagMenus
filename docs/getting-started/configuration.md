# Configuration

The main config file is located at `plugins/SwagMenus/config.yml`.

```yaml
# Watch the menus/ folder for file changes and auto-reload.
# Edit a menu YAML and it reloads instantly — no /sm reload needed.
auto_reload_on_change: true

# Whether to log detailed debug information to console (menu load timings, action execution
# traces, and requirement pass/fail results). Off by default — noisy on a busy server.
debug: false

# =====================================================
# Web Editor
# =====================================================
web_editor:
  # Enable or disable the web editor entirely.
  # Requires the SwagAPI plugin to be installed and enabled — the web editor is served
  # through SwagAPI's shared web server (see /sm editor for the live URL). SwagMenus does not
  # run its own HTTP server and has no port, bind-address, or password of its own; login is
  # handled entirely by SwagAPI's own shared session system.
  enabled: true

# =====================================================
# Messages
# =====================================================
messages:
  no_permission: "&cYou don't have permission to do that."
  menu_not_found: "&c[SwagMenus] Menu '%menu%' was not found."
  player_not_found: "&c[SwagMenus] Player '%player%' was not found."
```

## Web Editor Options

| Key | Default | Description |
|---|---|---|
| `enabled` | `true` | Enable or disable the web editor. Requires SwagAPI to be installed and enabled. |

> **Note:** There is no `port`, `bind-address`, or `password` key for the web editor — those
> belong to SwagAPI's own web server configuration, since SwagAPI is what actually hosts it. See
> SwagAPI's documentation for host/port/authentication settings.
