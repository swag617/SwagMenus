# Configuration

The main config file is located at `plugins/SwagMenus/config.yml`.

```yaml
# Watch the menus/ folder for file changes and auto-reload.
# Edit a menu YAML and it reloads instantly — no /sm reload needed.
auto_reload_on_change: true

# Whether to log detailed debug information to console.
debug: false

# =====================================================
# Web Editor
# =====================================================
web_editor:
  enabled: true
  port: 8080
  bind-address: "0.0.0.0"   # 0.0.0.0 = all interfaces, 127.0.0.1 = localhost only
  password: "changeme"       # change this before going live!
  token_expiry_minutes: 30

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
| `enabled` | `true` | Enable or disable the web editor |
| `port` | `8080` | Port the HTTP server listens on |
| `bind-address` | `0.0.0.0` | Interface to bind to |
| `password` | `"changeme"` | Login password for the editor |
| `token_expiry_minutes` | `30` | Session expiry after inactivity |

> **Important:** Change the default password before exposing the server to the internet. Use `/sm port <number>` to change the port without editing the file.
