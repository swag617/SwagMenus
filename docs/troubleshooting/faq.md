# FAQ

## The menu doesn't open when I type the command

- Make sure the menu file exists in `plugins/SwagMenus/menus/`
- Check the console for any loading errors
- Verify the `open_command` in the menu YAML matches what you're typing
- Run `/sm reload` then try again
- Confirm the player has the `swagmenus.open` permission

## Placeholders aren't working

- Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — it's a soft dependency and not bundled
- Run `/papi ecloud download Player` for built-in player placeholders
- Make sure the placeholder you're using has its expansion installed: `/papi ecloud download <expansion>`

## The web editor says "site can't be reached"

- Run `/sm editor` in-game and check the port shown
- If your server is remote, use the server's public IP address, not `localhost`
- Check that port 8080 (or your configured port) is open in your firewall/hosting panel
- Verify `web_editor.enabled: true` in `config.yml`

## Items are showing as STONE in the menu

An unknown `material` value was used. Check the console for a warning like:
```
Unknown material 'BED' for item 'spawn_tp' — using STONE
```
Use a valid 1.21 Bukkit material name. Note that legacy names like `BED` were removed in 1.13 — use `RED_BED` instead.

## [open] followed by [close] doesn't work

Put `[close]` before `[open]` in the action list. SwagMenus delays `[open]` by 2 ticks automatically so the close completes first.

## JavaScript / expression requirements aren't working

The `expression` type supports simple comparisons like `%player_level% >= 10`. Complex JavaScript expressions are not supported as Nashorn was removed in Java 15+. Use the built-in requirement types for complex logic.

## The console shows double plugin prefix like `[SwagMenus] [SwagMenus]`

Update to the latest version — this was fixed in an early release.

## A menu reloads twice when I save the file

This happens with editors that write files in two passes (save + finalize). The file watcher has a 500ms debounce to prevent this. If you still see it, it's cosmetic — the second reload is a no-op.
