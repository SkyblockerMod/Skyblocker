package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakeBagHelper;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakesHelper;
import de.hysky.skyblocker.skyblock.dungeon.CroesusHelper;
import de.hysky.skyblocker.skyblock.dungeon.CroesusProfit;
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.OrderTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.StartsWithTerminal;
import de.hysky.skyblocker.skyblock.experiment.ChronomatronSolver;
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for {@link ContainerSolver}s like terminal solvers and experiment solvers. To add a new gui solver, extend {@link ContainerSolver} and register it in {@link #ContainerSolverManager()}.
 */
public class ContainerSolverManager {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("");
    private final ContainerSolver[] solvers;
    private ContainerSolver currentSolver = null;
    private String[] groups;
    private List<ColorHighlight> highlights;
    /**
     * Useful for keeping track of a solver's state in a Screen instance, such as if Hypixel closes & reopens a screen after every click (as they do with terminals).
     */
    private int screenId = 0;

    public ContainerSolverManager() {
        solvers = new ContainerSolver[]{
                new ColorTerminal(),
                new OrderTerminal(),
                new StartsWithTerminal(),
                new CroesusHelper(),
                new CroesusProfit(),
                new ChronomatronSolver(),
                new SuperpairsSolver(),
                UltrasequencerSolver.INSTANCE,
                new NewYearCakeBagHelper(),
                NewYearCakesHelper.INSTANCE
        };
    }

    public ContainerSolver getCurrentSolver() {
        return currentSolver;
    }

    public void init() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
                ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, delta) -> {
                    MatrixStack matrices = context.getMatrices();
                    matrices.push();
                    matrices.translate(((HandledScreenAccessor) genericContainerScreen).getX(), ((HandledScreenAccessor) genericContainerScreen).getY(), 300);
                    onDraw(context, genericContainerScreen.getScreenHandler().slots.subList(0, genericContainerScreen.getScreenHandler().getRows() * 9));
                    matrices.pop();
                });
                ScreenEvents.remove(screen).register(screen1 -> clearScreen());
                onSetScreen(genericContainerScreen);
            } else {
                clearScreen();
            }
        });
    }

    public void onSetScreen(@NotNull GenericContainerScreen screen) {
        String screenName = screen.getTitle().getString();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(screenName);
        for (ContainerSolver solver : solvers) {
            if (solver.isEnabled()) {
                matcher.usePattern(solver.getName());
                matcher.reset();
                if (matcher.matches()) {
                    ++screenId;
                    currentSolver = solver;
                    groups = new String[matcher.groupCount()];
                    for (int i = 0; i < groups.length; i++) {
                        groups[i] = matcher.group(i + 1);
                    }
                    currentSolver.start(screen);
                    markDirty();

                    return;
                }
            }
        }
        clearScreen();
    }

    public void clearScreen() {
        if (currentSolver != null) {
            currentSolver.reset();
            currentSolver = null;
        }
    }

    public void markDirty() {
        highlights = null;
    }

    public void onSlotClick(int slot, ItemStack stack) {
        if (currentSolver != null) {
            currentSolver.onClickSlot(slot, stack, screenId, groups);
        }
    }

    public void onDraw(DrawContext context, List<Slot> slots) {
        if (currentSolver == null)
            return;
        if (highlights == null)
            highlights = currentSolver.getColors(groups, slotMap(slots));
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        for (ColorHighlight highlight : highlights) {
            Slot slot = slots.get(highlight.slot());
            int color = highlight.color();
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
        }
        RenderSystem.colorMask(true, true, true, true);
    }

    private Int2ObjectMap<ItemStack> slotMap(List<Slot> slots) {
    	Int2ObjectMap<ItemStack> slotMap = new Int2ObjectRBTreeMap<>();
        for (int i = 0; i < slots.size(); i++) {
            slotMap.put(i, slots.get(i).getStack());
        }
        return slotMap;
    }
}
