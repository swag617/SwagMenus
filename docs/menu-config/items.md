# Items & Slots

Items are defined under the `items:` key in your menu file. Each item has a unique key (any string you choose) and its properties nested below it.

## Full Item Reference

```yaml
items:
  my_item:                              # unique key — can be anything
    material: DIAMOND                   # Bukkit Material name
    slot: 13                            # single slot
    slots: [0, 1, 2, 8]                # OR multiple slots
    display_name: "&bMy Item"           # supports & and &#RRGGBB color codes
    lore:
      - "&7First line"
      - "&7Second line"
    amount: 1                           # 1–64
    glow: true                          # adds enchantment glow
    hide_flags: true                    # hides enchantments, attributes, etc.
    custom_model_data: 0                # custom model data value
    page: 0                             # 0 = all pages, 1+ = specific page
    skull_owner: "%player_name%"        # for PLAYER_HEAD — accepts placeholders
    skull_texture: "base64string..."    # for PLAYER_HEAD — raw base64 texture

    left_click_commands:
      - "[message] &aLeft clicked!"
    right_click_commands:
      - "[message] &cRight clicked!"
    shift_left_click_commands: []
    shift_right_click_commands: []
    middle_click_commands: []

    view_requirement:
      requirements:
        my_check:
          type: has_permission
          permission: "my.permission"
      deny_commands:
        - "[message] &cNo permission!"

    click_requirement:
      left_click_requirements:
        requirements:
          balance:
            type: ">="
            placeholder: "%vault_balance%"
            value: "100"
        deny_commands:
          - "[message] &cNeed $100!"
```

## Properties

| Property | Type | Description |
|---|---|---|
| `material` | String | Any valid Bukkit `Material` name |
| `slot` | Integer | Single slot index (0-based) |
| `slots` | List | Multiple slot indexes |
| `display_name` | String | Item display name with color codes |
| `lore` | List | Lines of lore with color codes |
| `amount` | Integer | Stack size, 1–64 |
| `glow` | Boolean | Enchantment glow effect |
| `hide_flags` | Boolean | Hide enchantments, attributes, etc. |
| `custom_model_data` | Integer | Custom model data for resource packs |
| `page` | Integer | Page number (0 = all pages) |
| `skull_owner` | String | Player head owner, supports placeholders |
| `skull_texture` | String | Player head base64 texture |

## Color Codes

Standard `&` color codes work everywhere:

```yaml
display_name: "&c&lRed Bold"
```

Hex colors using `&#RRGGBB`:

```yaml
display_name: "&#FF5500Gradient Text"
```

> **Tip:** The web editor shows a live color preview as you type.
