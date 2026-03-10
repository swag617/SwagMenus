# Using the Editor

## Layout

The editor has three main areas:

- **Left sidebar** — list of all your menus, create/delete buttons
- **Center grid** — visual representation of the menu's slots
- **Right panel** — item editor, opens when you click a slot

The **top bar** contains the menu title, size selector, open command, update interval, undo/redo buttons, and the Save button.

## Creating a Menu

1. Click **New Menu** in the left sidebar
2. Enter a name (lowercase, no spaces — this becomes the file name)
3. The editor opens with a blank 54-slot grid

## Editing a Slot

**Click any slot** to open the item editor in the right panel:

- **Material** — type any Bukkit material name (autocomplete available)
- **Slot(s)** — comma-separated slot numbers for multi-slot items
- **Display Name** — color preview renders below the input in real time
- **Lore** — one line per row, color preview shown
- **Glow / Hide Flags / Amount** — checkboxes and number inputs
- **Skull Owner** — visible only when material is `PLAYER_HEAD`
- **Click Commands** — one action per line for each click type

Click **Apply to Slot** to confirm, or **Remove** to delete the item.

## Open GUI Dropdown

Above the click command textareas is the **Open GUI** section. Select a menu from the dropdown, choose which click type to add it to, then click **Add**. This appends `[open] menu_name` to the selected textarea automatically.

## Fill Item

Click the **Fill Item** button above the slot grid to configure the background fill item. The editor switches to fill item mode — empty slots show a dimmed preview of the fill item. Click **Apply to Slot** to save, or **Clear** to remove it.

## Drag and Drop

Drag a filled slot to another slot to move it. Dragging onto an occupied slot swaps the two items. The move is undoable.

## Undo / Redo

- `Ctrl+Z` — undo last change
- `Ctrl+Shift+Z` or `Ctrl+Y` — redo
- Buttons in the top bar show the same actions

The undo stack holds up to 50 states and is cleared when you load a different menu.

## Saving

Click **Save** in the top bar. The menu is written to disk and reloaded on the server instantly — no `/sm reload` needed.
