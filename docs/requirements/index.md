# Requirements

Requirements gate whether an item is **visible** (`view_requirement`) or whether a **click action executes** (`click_requirement`). Multiple requirements in a set use AND logic — all must pass.

## Structure

```yaml
view_requirement:
  requirements:
    check_name:           # unique key, any string
      type: has_permission
      permission: "my.perm"
  deny_commands:          # runs if requirements are NOT met
    - "[message] &cNo permission!"

click_requirement:
  left_click_requirements:
    requirements:
      balance_check:
        type: has_money
        amount: 100
    deny_commands:
      - "[message] &cYou need at least $100!"
  right_click_requirements:
    requirements:
      admin_check:
        type: has_permission
        permission: "server.admin"
    deny_commands:
      - "[message] &cAdmins only."
```

## Requirement Types

### has_permission
Checks if the player has a specific permission node.

```yaml
type: has_permission
permission: "my.permission.node"
```

### has_money
Checks if the player has at least a specified amount of money (requires Vault).

```yaml
type: has_money
amount: 500
```

### Numeric Comparisons
Compares a placeholder value numerically. Supported operators: `>=`, `>`, `<=`, `<`, `==`, `!=`.
The placeholder goes in the `input:` field (not `placeholder:` — this must match the actual
YAML key the parser reads).

```yaml
type: ">="
input: "%player_level%"
value: "10"
```

```yaml
type: "=="
input: "%statistic_player_kills%"
value: "100"
```

### expression
Evaluates a full expression with a placeholder. Useful for complex comparisons.

```yaml
type: expression
expression: "%player_level% >= 10"
```

### string equals
Checks if a resolved placeholder value equals another value. Case-insensitive by default;
set `case_sensitive: true` to require an exact-case match.

```yaml
type: string equals
input: "%player_world%"
value: "survival"
case_sensitive: false
```

### string contains
Checks if a resolved placeholder value contains a string.

```yaml
type: string contains
input: "%luckperms_prefix%"
value: "VIP"
case_sensitive: false
```

### regex matches
Matches a resolved placeholder value against a regular expression.

```yaml
type: regex matches
input: "%player_name%"
regex: "^[A-Z].*"
```

## Multiple Requirements

All requirements in a set must pass (AND logic):

```yaml
view_requirement:
  requirements:
    needs_vip:
      type: has_permission
      permission: "rank.vip"
    needs_level:
      type: ">="
      input: "%player_level%"
      value: "20"
  deny_commands:
    - "[message] &cRequires VIP rank and level 20!"
```

## Showing a Different Item Instead of Hiding It

By default, an item whose `view_requirement` fails just doesn't appear — the slot is empty (or
shows the fill item, if one is configured). If you'd rather show a "locked" placeholder item
instead, add a `deny_item:` block alongside `view_requirement:`:

```yaml
items:
  vip_kit:
    material: DIAMOND
    slot: 13
    display_name: "&bVIP Kit"
    view_requirement:
      requirements:
        vip_check:
          type: has_permission
          permission: "rank.vip"

    deny_item:
      material: BARRIER
      display_name: "&cVIP Only"
      lore:
        - "&7Requires VIP rank."
      # slot/slots are optional here — if omitted, the deny item automatically
      # uses the same slot(s) as the item it's replacing.
```

`deny_item` accepts the same properties as a normal item (material, display_name, lore, glow,
click commands, etc). It is only shown when the parent item's `view_requirement` fails — it has
no effect on `click_requirement`.
