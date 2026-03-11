package com.swag.swagmenus.menu;

import java.util.*;

public enum MenuAnimationType {

    NONE {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            List<Integer> all = new ArrayList<>(size);
            for (int i = 0; i < size; i++) all.add(i);
            return List.of(all);
        }
    },

    CASCADE {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            int rows = size / 9;
            List<List<Integer>> groups = new ArrayList<>(rows);
            for (int r = 0; r < rows; r++) {
                List<Integer> row = new ArrayList<>(9);
                for (int c = 0; c < 9; c++) row.add(r * 9 + c);
                groups.add(row);
            }
            return groups;
        }
    },

    REVERSE_CASCADE {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            List<List<Integer>> groups = CASCADE.getSlotGroups(size);
            List<List<Integer>> reversed = new ArrayList<>(groups);
            Collections.reverse(reversed);
            return reversed;
        }
    },

    COLUMN {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            int rows = size / 9;
            List<List<Integer>> groups = new ArrayList<>(9);
            for (int c = 0; c < 9; c++) {
                List<Integer> col = new ArrayList<>(rows);
                for (int r = 0; r < rows; r++) col.add(r * 9 + c);
                groups.add(col);
            }
            return groups;
        }
    },

    WIPE_RIGHT {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            return COLUMN.getSlotGroups(size);
        }
    },

    WIPE_LEFT {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            List<List<Integer>> groups = new ArrayList<>(COLUMN.getSlotGroups(size));
            Collections.reverse(groups);
            return groups;
        }
    },

    FROM_CENTER {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            int rows = size / 9;
            double centerRow = (rows - 1) / 2.0;
            double centerCol = 4.0;

            // group slots by Manhattan distance from center (rounded to int)
            Map<Integer, List<Integer>> byDist = new TreeMap<>();
            for (int i = 0; i < size; i++) {
                int r = i / 9, c = i % 9;
                int dist = (int) Math.round(Math.abs(r - centerRow) + Math.abs(c - centerCol));
                byDist.computeIfAbsent(dist, k -> new ArrayList<>()).add(i);
            }
            return new ArrayList<>(byDist.values());
        }
    },

    FROM_EDGES {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            List<List<Integer>> groups = new ArrayList<>(FROM_CENTER.getSlotGroups(size));
            Collections.reverse(groups);
            return groups;
        }
    },

    RANDOM {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            List<Integer> slots = new ArrayList<>(size);
            for (int i = 0; i < size; i++) slots.add(i);
            Collections.shuffle(slots);

            List<List<Integer>> groups = new ArrayList<>();
            int groupSize = 3;
            for (int i = 0; i < slots.size(); i += groupSize) {
                groups.add(new ArrayList<>(slots.subList(i, Math.min(i + groupSize, slots.size()))));
            }
            return groups;
        }
    },

    SPIRAL {
        @Override
        public List<List<Integer>> getSlotGroups(int size) {
            int rows = size / 9, cols = 9;

            // Each "ring" from outermost to innermost is one group.
            // We determine ring by min(r, c, rows-1-r, cols-1-c).
            Map<Integer, List<Integer>> byRing = new TreeMap<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int ring = Math.min(Math.min(r, c), Math.min(rows - 1 - r, cols - 1 - c));
                    byRing.computeIfAbsent(ring, k -> new ArrayList<>()).add(r * 9 + c);
                }
            }

            // Within each ring, order the slots in spiral traversal order
            List<List<Integer>> groups = new ArrayList<>();
            List<Integer> spiral = buildSpiralOrder(rows, cols);
            Map<Integer, Integer> spiralIndex = new HashMap<>();
            for (int i = 0; i < spiral.size(); i++) spiralIndex.put(spiral.get(i), i);

            for (List<Integer> ring : byRing.values()) {
                ring.sort(Comparator.comparingInt(slot -> spiralIndex.getOrDefault(slot, 0)));
                groups.add(ring);
            }
            return groups;
        }

        private List<Integer> buildSpiralOrder(int rows, int cols) {
            List<Integer> order = new ArrayList<>(rows * cols);
            boolean[][] seen = new boolean[rows][cols];
            int[] dr = {0, 1, 0, -1};
            int[] dc = {1, 0, -1, 0};
            int r = 0, c = 0, dir = 0;
            for (int i = 0; i < rows * cols; i++) {
                order.add(r * 9 + c);
                seen[r][c] = true;
                if (i == rows * cols - 1) break;
                int nr = r + dr[dir], nc = c + dc[dir];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || seen[nr][nc]) {
                    dir = (dir + 1) % 4;
                    nr = r + dr[dir];
                    nc = c + dc[dir];
                }
                r = nr;
                c = nc;
            }
            return order;
        }
    };

    public abstract List<List<Integer>> getSlotGroups(int size);

    public static MenuAnimationType fromString(String value) {
        if (value == null || value.isBlank()) return NONE;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}
