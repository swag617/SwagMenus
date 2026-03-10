# Fill Items

A fill item automatically occupies every slot in the menu that isn't claimed by a defined item. It's the easiest way to add a background or border to your menu.

## Configuration

Add `fill_item:` at the top level of your menu file:

```yaml
fill_item:
  material: GRAY_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true
```

The fill item supports all standard item properties except `slot`, `slots`, and click commands.

## Common Patterns

**Simple gray background:**
```yaml
fill_item:
  material: GRAY_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true
```

**Black border (combined with specific items in center slots):**
```yaml
fill_item:
  material: BLACK_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true
```

**Colored background by category:**
```yaml
fill_item:
  material: PURPLE_STAINED_GLASS_PANE
  display_name: " "
  hide_flags: true
```

## Web Editor

The fill item can be configured visually in the web editor using the **Fill Item** button above the slot grid. Empty slots show the fill item at reduced opacity so you can preview the layout before saving.
