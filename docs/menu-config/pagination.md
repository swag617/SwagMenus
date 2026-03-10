# Pagination

SwagMenus supports multi-page menus. Items can be assigned to specific pages, and navigation buttons use `[nextpage]` and `[prevpage]` actions to move between them.

## How It Works

Every item has a `page` property:

- `page: 0` — shows on **all pages** (use for borders, navigation buttons, persistent UI)
- `page: 1` — shows only on page 1
- `page: 2` — shows only on page 2
- and so on...

## Example

```yaml
menu_title: "&8Item Shop — Page %page%"
menu_size: 54

fill_item:
  material: GRAY_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true

items:

  # --- Navigation (page: 0 = always visible) ---
  prev_button:
    material: ARROW
    slot: 45
    display_name: "&7← Previous"
    page: 0
    hide_flags: true
    left_click_commands:
      - "[prevpage]"

  next_button:
    material: ARROW
    slot: 53
    display_name: "&7Next →"
    page: 0
    hide_flags: true
    left_click_commands:
      - "[nextpage]"

  close_button:
    material: BARRIER
    slot: 49
    display_name: "&cClose"
    page: 0
    hide_flags: true
    left_click_commands:
      - "[close]"

  # --- Page 1 items ---
  diamond:
    material: DIAMOND
    slot: 10
    page: 1
    display_name: "&bDiamond"
    lore:
      - "&7Cost: &e$100"

  emerald:
    material: EMERALD
    slot: 11
    page: 1
    display_name: "&aEmerald"
    lore:
      - "&7Cost: &e$50"

  # --- Page 2 items ---
  netherite_ingot:
    material: NETHERITE_INGOT
    slot: 10
    page: 2
    display_name: "&8Netherite Ingot"
    lore:
      - "&7Cost: &e$500"
```

> **Tip:** If `[prevpage]` is called on page 1 or `[nextpage]` on the last page, nothing happens — the menu stays on the current page.
