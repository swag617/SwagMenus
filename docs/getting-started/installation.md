# Installation

## Requirements

- **Paper 1.21.x** — Spigot is not supported
- **Java 21** or higher
- **PlaceholderAPI** *(optional but recommended)*

## Steps

**1. Download the plugin**

Grab the latest `SwagMenus.jar` from the [GitHub Releases](https://github.com/swag617/SwagMenus/releases) page.

**2. Add to your server**

Drop the jar into your server's `plugins/` folder.

**3. Restart**

Start or restart your server. SwagMenus will generate its folder structure automatically.

```
plugins/
└── SwagMenus/
    ├── config.yml
    └── menus/
        ├── example_main.yml
        └── example_server_selector.yml
```

**4. Verify**

You should see this in your console on startup:

```
[SwagMenus] Loaded 2 menu(s).
[SwagMenus] Web editor started on port 8080
[SwagMenus] Enabled successfully.
```

## PlaceholderAPI

PlaceholderAPI is a soft dependency — SwagMenus loads without it, but `%placeholders%` in menu titles, item names, lore, and commands won't resolve. Install it from [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/) for the best experience.

> **Tip:** After installing PlaceholderAPI, run `/papi ecloud download Player` to get the built-in player placeholders used in the example menus.
