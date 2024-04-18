package de.hysky.skyblocker.skyblock.fancybars;

import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BarPositioner {

    private final Map<BarAnchor, LinkedList<LinkedList<StatusBar>>> map = new HashMap<>(BarAnchor.values().length);

    public BarPositioner() {
        for (BarAnchor value : BarAnchor.values()) {
            map.put(value, new LinkedList<>());
        }
    }


    public int getRowCount(@NotNull BarAnchor barAnchor) {
        return map.get(barAnchor).size();
    }

    /**
     * Adds a row to the end of an anchor
     *
     * @param barAnchor the anchor
     */
    public void addRow(@NotNull BarAnchor barAnchor) {
        map.get(barAnchor).add(new LinkedList<>());
    }

    /**
     * Adds a row at the specified index
     *
     * @param barAnchor the anchor
     * @param row       row index
     */
    public void addRow(@NotNull BarAnchor barAnchor, int row) {
        map.get(barAnchor).add(row, new LinkedList<>());
    }

    /**
     * adds a bar to the end of a row
     *
     * @param barAnchor the anchor
     * @param row       the row
     * @param bar       the bar to add
     */
    public void addBar(@NotNull BarAnchor barAnchor, int row, StatusBar bar) {
        LinkedList<StatusBar> statusBars = map.get(barAnchor).get(row);
        statusBars.add(bar);
        bar.gridY = row;
        bar.gridX = statusBars.lastIndexOf(bar); // optimization baby, start with the end!
        bar.anchor = barAnchor;
    }

    /**
     * adds a bar to the specified x in a row
     *
     * @param barAnchor the anchor
     * @param row       the row
     * @param x         the index in the row
     * @param bar       the bar to add
     */
    public void addBar(@NotNull BarAnchor barAnchor, int row, int x, StatusBar bar) {
        LinkedList<StatusBar> statusBars = map.get(barAnchor).get(row);
        statusBars.add(x, bar);
        bar.gridY = row;
        bar.gridX = statusBars.indexOf(bar);
        bar.anchor = barAnchor;
    }

    /**
     * removes the specified bar at x on the row. If it's row is empty after being removed, the row will be auto removed
     *
     * @param barAnchor the anchor
     * @param row       dah row
     * @param x         dah x
     */
    public void removeBar(@NotNull BarAnchor barAnchor, int row, int x) {
        LinkedList<StatusBar> statusBars = map.get(barAnchor).get(row);
        StatusBar remove = statusBars.remove(x);
        remove.anchor = null;
        for (int i = x; i < statusBars.size(); i++) {
            statusBars.get(i).gridX--;
        }
        if (statusBars.isEmpty()) removeRow(barAnchor, row);
    }

    /**
     * removes the specified bar on the row. If it's row is empty after being removed, the row will be auto removed
     *
     * @param barAnchor the anchor
     * @param row       dah row
     * @param bar       dah bar
     */
    public void removeBar(@NotNull BarAnchor barAnchor, int row, StatusBar bar) {
        LinkedList<StatusBar> barRow = map.get(barAnchor).get(row);
        int x = barRow.indexOf(bar);
        if (x < 0) return; // probably a bad idea

        barRow.remove(bar);
        bar.anchor = null;
        for (int i = x; i < barRow.size(); i++) {
            barRow.get(i).gridX--;
        }
        if (barRow.isEmpty()) removeRow(barAnchor, row);
    }

    /**
     * row must be empty
     *
     * @param barAnchor the anchor
     * @param row       the row to remove
     */
    public void removeRow(@NotNull BarAnchor barAnchor, int row) {
        LinkedList<StatusBar> barRow = map.get(barAnchor).get(row);
        if (!barRow.isEmpty())
            throw new IllegalStateException("Can't remove a non-empty row (" + barAnchor + "," + row + ")");
        map.get(barAnchor).remove(row);
        for (int i = row; i < map.get(barAnchor).size(); i++) {
            for (StatusBar statusBar : map.get(barAnchor).get(i)) {
                statusBar.gridY--;
            }
        }
    }


    public LinkedList<StatusBar> getRow(@NotNull BarAnchor barAnchor, int row) {
        return map.get(barAnchor).get(row);
    }

    public StatusBar getBar(@NotNull BarAnchor barAnchor, int row, int x) {
        return map.get(barAnchor).get(row).get(x);
    }

    public boolean hasNeighbor(@NotNull BarAnchor barAnchor, int row, int x, boolean right) {
        LinkedList<StatusBar> statusBars = map.get(barAnchor).get(row);
        if (barAnchor.isRight()) {
            return (right && x < statusBars.size() - 1) || (!right && x > 0);
        } else {
            return (right && x > 0) || (!right && x < statusBars.size() - 1);
        }
    }


    public enum BarAnchor {
        HOTBAR_LEFT(true, false,
                (scaledWidth, scaledHeight) -> new ScreenPos(scaledWidth / 2 - 91 - 2, scaledHeight - 5),
                SizeRule.freeSize(25, 2, 6)),

        HOTBAR_RIGHT(true, true,
                (scaledWidth, scaledHeight) -> new ScreenPos(scaledWidth / 2 + 91 + 2, scaledHeight - 5),
                SizeRule.freeSize(25, 2, 6)),

        HOTBAR_TOP(true, true,
                (scaledWidth, scaledHeight) -> new ScreenPos(scaledWidth / 2 - 91, scaledHeight - 23),
                SizeRule.targetSize(12, 182, 2),
                anchorPosition -> new ScreenRect(anchorPosition.x(), anchorPosition.y() - 20, 182, 20)),

        SCREEN_TOP_LEFT(false, true,
                ((scaledWidth, scaledHeight) -> new ScreenPos(5, 5)),
                SizeRule.freeSize(25, 2, 6)
        ),
        SCREEN_TOP_RIGHT(false, false,
                ((scaledWidth, scaledHeight) -> new ScreenPos(scaledWidth - 5, 5)),
                SizeRule.freeSize(25, 2, 6)
        ),
        SCREEN_BOTTOM_LEFT(true, true,
                ((scaledWidth, scaledHeight) -> new ScreenPos(5, scaledHeight - 5)),
                SizeRule.freeSize(25, 2, 6)
        ),
        SCREEN_BOTTOM_RIGHT(true, false,
                ((scaledWidth, scaledHeight) -> new ScreenPos(scaledWidth - 5, scaledHeight - 5)),
                SizeRule.freeSize(25, 2, 6)
        );

        private final AnchorPositionProvider positionProvider;
        private final AnchorHitboxProvider hitboxProvider;
        private final boolean up;
        private final boolean right;
        private final SizeRule sizeRule;

        /**
         * @param up               whether the rows stack towards the top of the screen from the anchor (false is bottom)
         * @param right            whether the bars are line up towards the right of the screen from the anchor (false is left)
         * @param positionProvider provides the position of the anchor for a give screen size
         * @param sizeRule         the rule the bars should follow. See {@link SizeRule}
         * @param hitboxProvider   provides the hitbox for when the anchor has no bars for the config screen
         */
        BarAnchor(boolean up, boolean right, AnchorPositionProvider positionProvider, SizeRule sizeRule, AnchorHitboxProvider hitboxProvider) {
            this.positionProvider = positionProvider;
            this.up = up;
            this.right = right;
            this.hitboxProvider = hitboxProvider;
            this.sizeRule = sizeRule;
        }

        BarAnchor(boolean up, boolean right, AnchorPositionProvider positionProvider, SizeRule sizeRule) {
            this(up, right, positionProvider, sizeRule,
                    anchorPosition -> new ScreenRect(anchorPosition.x() - (right ? 0 : 20), anchorPosition.y() - (up ? 20 : 0), 20, 20));
        }

        public ScreenPos getAnchorPosition(int scaledWidth, int scaledHeight) {
            return positionProvider.getPosition(scaledWidth, scaledHeight);
        }

        public ScreenRect getAnchorHitbox(ScreenPos anchorPosition) {
            return hitboxProvider.getHitbox(anchorPosition);
        }

        /**
         * whether the rows stack towards the top of the screen from the anchor (false is bottom)
         *
         * @return true if towards the top, false otherwise
         */
        public boolean isUp() {
            return up;
        }

        /**
         * whether the bars are line up towards the right of the screen from the anchor (false is left)
         *
         * @return true if towards the right, false otherwise
         */
        public boolean isRight() {
            return right;
        }

        public SizeRule getSizeRule() {
            return sizeRule;
        }

        private static final List<BarAnchor> cached = List.of(values());

        /**
         * cached version of {@link BarAnchor#values()}
         *
         * @return the list of anchors
         */
        public static List<BarAnchor> allAnchors() {
            return cached;
        }
    }

    /**
     * The rules the bars on an anchor should follow
     *
     * @param isTargetSize whether the bars went to fit to a target width
     * @param targetSize   the size of all the bars on a row should add up to this (target size)
     * @param totalWidth   the total width taken by all the bars on the row (target size)
     * @param widthPerSize the width of each size "unit" (free size)
     * @param minSize      the minimum (free and target size)
     * @param maxSize      the maximum (free and target size, THIS SHOULD BE THE SAME AS {@code targetSize} FOR {@code isTargetSize = true})
     */
    public record SizeRule(boolean isTargetSize, int targetSize, int totalWidth, int widthPerSize, int minSize, int maxSize) {
        public static SizeRule freeSize(int widthPerSize, int minSize, int maxSize) {
            return new SizeRule(false, -1, -1, widthPerSize, minSize, maxSize);
        }

        public static SizeRule targetSize(int targetSize, int totalWidth, int minSize) {
            return new SizeRule(true, targetSize, totalWidth, -1, minSize, targetSize);
        }
    }

    /**
     * A record representing a snapshot of a bar's position
     *
     * @param barAnchor
     * @param x
     * @param y         the row
     */
    public record BarLocation(@Nullable BarAnchor barAnchor, int x, int y) {

        public static final BarLocation NULL = new BarLocation(null, -1, -1);

        public static BarLocation of(StatusBar bar) {
            return new BarLocation(bar.anchor, bar.gridX, bar.gridY);
        }

        public boolean equals(BarAnchor barAnchor, int x, int y) {
            return x == this.x && y == this.y && barAnchor == this.barAnchor;
        }
    }

    /**
     * provides the position of the anchor for a give screen size
     */
    @FunctionalInterface
    interface AnchorPositionProvider {

        ScreenPos getPosition(int scaledWidth, int scaledHeight);
    }

    @FunctionalInterface
    interface AnchorHitboxProvider {

        /**
         * The hitbox, as in how large the area of "snapping" is if there are no bars on this anchor
         *
         * @param anchorPosition the position of the anchor
         * @return the rectangle that represents the hitbox
         */
        ScreenRect getHitbox(ScreenPos anchorPosition);
    }
}
