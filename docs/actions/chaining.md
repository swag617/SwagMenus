# Chaining Menus

Menus chain together naturally using `[open]` and `[back]`. SwagMenus automatically tracks each player's navigation history so `[back]` always returns them to where they came from.

## How It Works

When a player opens a menu via `[open]`, their current menu is pushed onto a navigation stack. `[back]` pops the stack and reopens the previous menu. If the stack is empty, the inventory closes.

## Example — Confirmation Flow

```yaml
# shop.yml — main shop menu
items:
  diamond_button:
    material: DIAMOND
    slot: 13
    display_name: "&bBuy Diamond — &e$100"
    left_click_commands:
      - "[open] shop_confirm"
```

```yaml
# shop_confirm.yml — confirmation screen
menu_title: "&8Confirm Purchase"
menu_size: 27

items:
  confirm:
    material: LIME_STAINED_GLASS_PANE
    slot: 11
    display_name: "&a&lConfirm"
    left_click_commands:
      - "[console] eco take %player_name% 100"
      - "[console] give %player_name% diamond 1"
      - "[sound] ENTITY_PLAYER_LEVELUP"
      - "[message] &aPurchased!"
      - "[close]"

  cancel:
    material: RED_STAINED_GLASS_PANE
    slot: 15
    display_name: "&c&lCancel"
    left_click_commands:
      - "[back]"    # returns to shop.yml automatically
```

## Example — Hub Navigation

```yaml
# main_hub.yml
items:
  survival:
    left_click_commands:
      - "[open] survival_menu"

  creative:
    left_click_commands:
      - "[open] creative_menu"

# survival_menu.yml
items:
  back:
    material: ARROW
    slot: 45
    display_name: "&7← Back"
    left_click_commands:
      - "[back]"    # returns to main_hub.yml
```

## Navigation Stack

The stack tracks the full history, not just one level back. If a player navigates A → B → C, pressing `[back]` on C goes to B, pressing it again goes to A, and again closes the inventory.

> **Tip:** The stack is per-player and per-session. Closing the inventory clears the history.
