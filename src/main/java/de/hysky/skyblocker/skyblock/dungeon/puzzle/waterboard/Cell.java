package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

public class Cell {
    public static final Cell BLOCK = new Cell(Type.BLOCK);
    public static final Cell EMPTY = new Cell(Type.EMPTY);
    public final Type type;

    private Cell(Type type) {
        this.type = type;
    }

    public boolean isOpen() {
        return type == Type.EMPTY;
    }

    public static class SwitchCell extends Cell {
        public final int id;
        private boolean open;

        public SwitchCell(int id) {
            super(Type.SWITCH);
            this.id = id;
        }

        public static SwitchCell ofOpened(int id) {
            SwitchCell switchCell = new SwitchCell(id);
            switchCell.open = true;
            return switchCell;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) || obj instanceof SwitchCell switchCell && id == switchCell.id && open == switchCell.open;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        public void toggle() {
            open = !open;
        }
    }

    public enum Type {
        BLOCK,
        EMPTY,
        SWITCH
    }
}
