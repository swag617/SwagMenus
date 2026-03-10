# Quick Start

This guide walks you through creating your first menu in under two minutes.

## 1. Create the file

Create a new file in `plugins/SwagMenus/menus/` called `shop.yml`.

## 2. Paste this starter template

```yaml
menu_title: "&6&lShop"
menu_size: 27
open_command: shop

fill_item:
  material: GRAY_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true

items:

  diamond:
    material: DIAMOND
    slot: 13
    display_name: "&bDiamond"
    lore:
      - "&7Cost: &e$100"
      - ""
      - "&eLeft-click to buy!"
    glow: true
    hide_flags: true
    left_click_commands:
      - "[console] eco take %player_name% 100"
      - "[console] give %player_name% diamond 1"
      - "[sound] ENTITY_PLAYER_LEVELUP"
      - "[message] &aYou bought a Diamond!"

  close:
    material: BARRIER
    slot: 26
    display_name: "&cClose"
    hide_flags: true
    left_click_commands:
      - "[close]"
```

## 3. Load it

Since `auto_reload_on_change` is enabled by default, just **save the file** and the menu loads automatically. Or run:

```
/sm reload shop
```

## 4. Open it

```
/shop
```

That's it. Players with the `swagmenus.open` permission can now open your menu with `/shop`.

## Next Steps

- [Menu Options](/menu-config/menu-options) — all available menu settings
- [Items & Slots](/menu-config/items) — full item configuration reference
- [Action Types](/actions/action-types) — everything you can do on click
- [Web Editor](/web-editor/overview) — build menus visually in your browser
