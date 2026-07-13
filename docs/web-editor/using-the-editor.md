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
- **View Requirement** — requirements that gate whether the item is shown at all
- **Click Requirement (Left / Right)** — requirements that gate whether that click's commands run

Click **Apply to Slot** to confirm, or **Remove** to delete the item.

### View / Click Requirements

Each requirement section (View, Left Click, Right Click) works the same way:

1. Click **+ Add Requirement** to add a row
2. Pick a requirement type from the dropdown — the fields below it change to match (e.g.
   `has_permission` shows a Permission Node field, `string equals` shows Input/Value/Case
   Sensitive fields)
3. Add as many rows as you need — all of them must pass (AND logic)
4. Optionally fill in **Deny Commands** — actions run when the requirement(s) fail (for View
   Requirement, this usually isn't visible to the player since the item just doesn't show; it's
   mainly useful for Click Requirement, e.g. `[message] &cYou can't afford that!`)

See [Requirements](../requirements/index.md) for what each requirement type checks.

> **Note:** Fields the editor doesn't have dedicated controls for yet — `page`, `deny_item`,
> `lore_frames`, `on_chat_input`, `skull_texture`, and the `player_list` item type — are preserved
> as-is when you edit an item through the editor; they just aren't visually editable, so use the
> YAML file directly for those.

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
