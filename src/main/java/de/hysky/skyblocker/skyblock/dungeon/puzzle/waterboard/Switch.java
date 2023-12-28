package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Switch extends AbstractCollection<Cell.SwitchCell> {
    public final int id;
    public final List<Cell.SwitchCell> cells = new ArrayList<>();

    public Switch(int id) {
        this.id = id;
    }

    @Override
    @NotNull
    public Iterator<Cell.SwitchCell> iterator() {
        return cells.iterator();
    }

    @Override
    public int size() {
        return cells.size();
    }

    @Override
    public boolean add(Cell.SwitchCell cell) {
        return cells.add(cell);
    }

    public void toggle() {
        for (Cell.SwitchCell cell : cells) {
            cell.toggle();
        }
    }
}
