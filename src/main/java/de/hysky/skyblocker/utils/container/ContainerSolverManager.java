package de.hysky.skyblocker.utils.container;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakeBagHelper;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakesHelper;
import de.hysky.skyblocker.skyblock.bazaar.ReorderHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.dungeon.CroesusHelper;
import de.hysky.skyblocker.skyblock.dungeon.CroesusProfit;
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.LightsOnTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.OrderTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.StartsWithTerminal;
import de.hysky.skyblocker.skyblock.dwarven.CommissionHighlight;
import de.hysky.skyblocker.skyblock.experiment.ChronomatronSolver;
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
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

/**
 * Manager class for {@link SimpleContainerSolver}s like terminal solvers and experiment solvers. To add a new gui solver, extend {@link SimpleContainerSolver} and register it in {@link #ContainerSolverManager()}.
 */
public class ContainerSolverManager {
	private static final ContainerSolver[] solvers = new ContainerSolver[]{
			new ColorTerminal(),
			new OrderTerminal(),
			new StartsWithTerminal(),
			new LightsOnTerminal(),
			new CroesusHelper(),
			new CroesusProfit(),
			new ChronomatronSolver(),
			new CommissionHighlight(),
			new SuperpairsSolver(),
			UltrasequencerSolver.INSTANCE,
			new NewYearCakeBagHelper(),
			NewYearCakesHelper.INSTANCE,
			ChocolateFactorySolver.INSTANCE,
			new ReorderHelper()
	};
	private static ContainerSolver currentSolver = null;
	private static List<ColorHighlight> highlights;
	/**
	 * Useful for keeping track of a solver's state in a Screen instance, such as if Hypixel closes & reopens a screen after every click (as they do with terminals).
	 */
	private static int screenId = 0;

	private ContainerSolverManager() {}

	public static ContainerSolver getCurrentSolver() {
		return currentSolver;
	}

	@Init
	public static void init() {
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

	@SuppressWarnings({"ConstantValue", "java:S1066"})
	public static void onSetScreen(@NotNull GenericContainerScreen screen) {
		String screenName = screen.getTitle().getString();
		for (ContainerSolver solver : solvers) {
			if (solver.isEnabled()) {
				//Ignore the result of instanceof being always true.
				//This only happens because all solvers in the `solvers` array are SimpleContainerSolvers, which extend RegexContainerMatcher.
				//This may not be the case as more and more solvers are added.
				//Also don't merge this with the above `if`, the parenthesis mess gets hard to read. (java:S1066 for sonarlint users)
				if ((solver instanceof RegexContainerMatcher containerMatcher && containerMatcher.test(screenName)) || solver.test(screen)) {
					++screenId;
					currentSolver = solver;
					currentSolver.start(screen);
					markHighlightsDirty();
					return;
				}
			}
		}
		clearScreen();
	}

	public static void clearScreen() {
		if (currentSolver != null) {
			currentSolver.reset();
			currentSolver = null;
		}
	}

	public static void markHighlightsDirty() {
		highlights = null;

		if (currentSolver != null) {
			currentSolver.markDirty();
		}
	}

	/**
	 * @return Whether the click should be disallowed.
	 */
	public static boolean onSlotClick(int slot, ItemStack stack) {
		return currentSolver != null && currentSolver.onClickSlot(slot, stack, screenId);
	}

	public static void onDraw(DrawContext context, List<Slot> slots) {
		if (currentSolver == null) return;
		if (highlights == null) highlights = currentSolver.getColors(slotMap(slots));
		RenderSystem.enableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		for (ColorHighlight highlight : highlights) {
			Slot slot = slots.get(highlight.slot());
			int color = highlight.color();
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
		}
		RenderSystem.colorMask(true, true, true, true);
	}

	public static Int2ObjectMap<ItemStack> slotMap(List<Slot> slots) {
		Int2ObjectMap<ItemStack> slotMap = new Int2ObjectRBTreeMap<>();
		for (int i = 0; i < slots.size(); i++) {
			slotMap.put(i, slots.get(i).getStack());
		}
		return slotMap;
	}
}
