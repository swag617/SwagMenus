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
        type: ">="
        placeholder: "%vault_balance%"
        value: "100"
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
Compares a placeholder value numerically. Supported operators: `>=`, `>`, `<=`, `<`, `==`, `!=`

```yaml
type: ">="
placeholder: "%player_level%"
value: "10"
```

```yaml
type: "=="
placeholder: "%statistic_player_kills%"
value: "100"
```

### expression
Evaluates a full expression with a placeholder. Useful for complex comparisons.

```yaml
type: expression
expression: "%player_level% >= 10"
```

### string equals
Checks if two placeholder values are equal (case-sensitive).

```yaml
type: string equals
placeholder: "%player_world%"
value: "survival"
```

### string contains
Checks if a placeholder value contains a string.

```yaml
type: string contains
placeholder: "%luckperms_prefix%"
value: "VIP"
```

### regex matches
Matches a placeholder value against a regular expression.

```yaml
type: regex matches
placeholder: "%player_name%"
value: "^[A-Z].*"
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
      placeholder: "%player_level%"
      value: "20"
  deny_commands:
    - "[message] &cRequires VIP rank and level 20!"
```
