package de.hysky.skyblocker.skyblock.fancybars;

import java.util.LinkedList;
import java.util.List;

public class BarGrid {
    private final LinkedList<LinkedList<StatusBar>> top = new LinkedList<>();
    private final LinkedList<LinkedList<StatusBar>> bottomLeft = new LinkedList<>();
    private final LinkedList<LinkedList<StatusBar>> bottomRight = new LinkedList<>();

    public BarGrid() {}

    public void add(int row, StatusBar bar, boolean right) {
        if (row > 0)
            top.get(row).add(bar);
        else if (row < 0) {
            if (right) {
                bottomRight.get(Math.abs(row)).add(bar);
            } else {
                bottomLeft.get(Math.abs(row)).add(bar);
            }
        }
    }

    public void add(int x, int y, StatusBar bar) {
        if (y > 0) {
            if (x < 1) throw new IllegalArgumentException("x can't be negative, x: " + x);
            LinkedList<StatusBar> statusBars = top.get(y-1);
            statusBars.add(Math.min(x-1, statusBars.size()), bar);
            bar.gridY = y;
            bar.gridX = statusBars.indexOf(bar)+1;
        } else if (y < 0) {
            LinkedList<StatusBar> statusBars = (x < 0? bottomLeft: bottomRight).get(Math.abs(y)-1);
            statusBars.add(Math.min(Math.abs(x)-1, statusBars.size()), bar);
            bar.gridY = y;
            bar.gridX = -(statusBars.indexOf(bar)+1);
        }
    }

    public List<StatusBar> getRow(int row, boolean right) {
        if (row > 0) {
            return top.get(row-1);
        } else {
            return (right ? bottomRight: bottomLeft).get(Math.abs(row)-1);
        }
    }

    public void addRow(int row, boolean right) {
        if (row>0) {
            top.add(row-1, new LinkedList<>());
        } else if (row<0) {
            (right ? bottomRight: bottomLeft).add(Math.abs(row)-1, new LinkedList<>());
        }
    }

    public void remove(int x, int y) {
        if (y > 0) {
            if (x < 1) throw new IllegalArgumentException("x can't be negative, x: " + x);
            LinkedList<StatusBar> statusBars = top.get(y-1);
            StatusBar remove = statusBars.remove(x-1);
            for (int i = x-1; i < statusBars.size(); i++) {
                statusBars.get(i).gridX--;
            }
            remove.gridX = 0;
            remove.gridY = 0;
            if (statusBars.isEmpty()) {
                top.remove(y - 1);
                for (int i = y-1; i < top.size(); i++) {
                    for (StatusBar bar : top.get(i)) {
                        bar.gridY--;
                    }
                }
            }
        } else if (y < 0) {
            LinkedList<LinkedList<StatusBar>> bottom = x < 0 ? bottomLeft : bottomRight;
            LinkedList<StatusBar> statusBars = bottom.get(Math.abs(y)-1);
            StatusBar remove = statusBars.remove(Math.abs(x) - 1);
            for (int i = Math.abs(x)-1; i < statusBars.size(); i++) {
                statusBars.get(i).gridX--;
            }
            remove.gridX = 0;
            remove.gridY = 0;
            if (statusBars.isEmpty()) {
                bottom.remove(Math.abs(y) - 1);
                for (int i = Math.abs(y)-1; i < bottom.size(); i++) {
                    for (StatusBar bar : bottom.get(i)) {
                        bar.gridY--;
                    }
                }
            }
        }
    }

    public int getTopSize() {return top.size();}

    public int getBottomLeftSize() {return bottomLeft.size();}
    public int getBottomRightSize() {return bottomRight.size();}
}
