package me.xmrvizzy.skyblocker.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.xmrvizzy.skyblocker.skyblock.dungeon.CroesusHelper;
import me.xmrvizzy.skyblocker.skyblock.dungeon.terminal.ColorTerminal;
import me.xmrvizzy.skyblocker.skyblock.dungeon.terminal.OrderTerminal;
import me.xmrvizzy.skyblocker.skyblock.dungeon.terminal.StartsWithTerminal;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for {@link ContainerSolver}s like terminal solvers and experiment solvers. To add a new gui solver, extend {@link ContainerSolver} and register it in {@link #ContainerSolverManager()}.
 */
public class ContainerSolverManager extends DrawableHelper {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");
    private final ContainerSolver[] solvers;
    private ContainerSolver currentSolver = null;
    private String[] groups;
    private List<ColorHighlight> highlights;

    public ContainerSolverManager() {
        solvers = new ContainerSolver[]{
                new ColorTerminal(),
                new OrderTerminal(),
                new StartsWithTerminal(),
                new CroesusHelper()
        };
    }

    public void onSetScreen(@NotNull GenericContainerScreen screen) {
        String screenName = screen.getTitle().getString();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(screenName);
        for (ContainerSolver solver : solvers) {
            if (solver.isEnabled()) {
                matcher.usePattern(solver.getName());
                matcher.reset();
                if (matcher.matches()) {
                    currentSolver = solver;
                    groups = new String[matcher.groupCount()];
                    for (int i = 0; i < groups.length; i++)
                        groups[i] = matcher.group(i + 1);
                    return;
                }
            }
        }
        currentSolver = null;
    }

    public void clearScreen() {
        currentSolver = null;
    }

    public void markDirty() {
        highlights = null;
    }

    public void onDraw(MatrixStack matrices, List<Slot> slots) {
        if (currentSolver == null)
            return;
        if (highlights == null)
            highlights = currentSolver.getColors(groups, slotMap(slots));
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        for (ColorHighlight highlight : highlights) {
            Slot slot = slots.get(highlight.slot());
            int color = highlight.color();
            fillGradient(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, color, color);
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    private Map<Integer, ItemStack> slotMap(List<Slot> slots) {
        Map<Integer, ItemStack> slotMap = new TreeMap<>();
        for (int i = 0; i < slots.size(); i++)
            slotMap.put(i, slots.get(i).getStack());
        return slotMap;
    }
}
