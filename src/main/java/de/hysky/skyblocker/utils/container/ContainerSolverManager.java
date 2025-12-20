package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.RaffleTaskHighlight;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakeBagHelper;
import de.hysky.skyblocker.skyblock.accessories.newyearcakes.NewYearCakesHelper;
import de.hysky.skyblocker.skyblock.auction.CopyUnderbidPrice;
import de.hysky.skyblocker.skyblock.bazaar.ReorderHelper;
import de.hysky.skyblocker.skyblock.chocolatefactory.ChocolateFactorySolver;
import de.hysky.skyblocker.skyblock.dungeon.CroesusHelper;
import de.hysky.skyblocker.skyblock.dungeon.CroesusProfit;
import de.hysky.skyblocker.skyblock.dungeon.SalvageHelper;
import de.hysky.skyblocker.skyblock.dungeon.SellableItemsHighlighter;
import de.hysky.skyblocker.skyblock.dungeon.terminal.ColorTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.LightsOnTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.OrderTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.SameColorTerminal;
import de.hysky.skyblocker.skyblock.dungeon.terminal.StartsWithTerminal;
import de.hysky.skyblocker.skyblock.dwarven.CommissionHighlight;
import de.hysky.skyblocker.skyblock.dwarven.fossil.FossilSolver;
import de.hysky.skyblocker.skyblock.end.EndStatsBestiaryUpdater;
import de.hysky.skyblocker.skyblock.experiment.ChronomatronSolver;
import de.hysky.skyblocker.skyblock.experiment.SuperpairsSolver;
import de.hysky.skyblocker.skyblock.experiment.UltrasequencerSolver;
import de.hysky.skyblocker.skyblock.galatea.TunerSolver;
import de.hysky.skyblocker.skyblock.garden.StereoHarmonyHelper;
import de.hysky.skyblocker.skyblock.hunting.HuntingBoxHelper;
import de.hysky.skyblocker.skyblock.item.AnvilHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.BitsHelper;
import de.hysky.skyblocker.skyblock.slayers.features.RewardsHighlighter;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Manager class for {@link SimpleContainerSolver}s like terminal solvers and experiment solvers. To add a new gui solver, extend {@link SimpleContainerSolver} and register it in {@link #ContainerSolverManager()}.
 */
public class ContainerSolverManager {
	private static final ContainerSolver[] solvers = new ContainerSolver[]{
			new ColorTerminal(),
			new OrderTerminal(),
			new StartsWithTerminal(),
			new LightsOnTerminal(),
			CroesusHelper.INSTANCE,
			CroesusProfit.INSTANCE,
			new SalvageHelper(),
			new ChronomatronSolver(),
			new CommissionHighlight(),
			new SuperpairsSolver(),
			UltrasequencerSolver.INSTANCE,
			new NewYearCakeBagHelper(),
			NewYearCakesHelper.INSTANCE,
			ChocolateFactorySolver.INSTANCE,
			TunerSolver.INSTANCE,
			new ReorderHelper(),
			BitsHelper.INSTANCE,
			new RaffleTaskHighlight(),
			new FossilSolver(),
			SameColorTerminal.INSTANCE,
			new CopyUnderbidPrice(),
			new HuntingBoxHelper(),
			new SellableItemsHighlighter(),
			StereoHarmonyHelper.INSTANCE,
			new EndStatsBestiaryUpdater(),
			new AnvilHelper(),
			new RewardsHighlighter(),
	};
	private static @Nullable ContainerSolver currentSolver = null;
	private static @Nullable List<ColorHighlight> highlights;
	/**
	 * Useful for keeping track of a solver's state in a Screen instance, such as if Hypixel closes & reopens a screen after every click (as they do with terminals).
	 */
	private static int screenId = 0;

	private ContainerSolverManager() {}

	public static @Nullable ContainerSolver getCurrentSolver() {
		return currentSolver;
	}

	@Init
	public static void init() {
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof ContainerScreen genericContainerScreen) {
				ScreenEvents.remove(screen).register(screen1 -> clearScreen());
				onSetScreen(genericContainerScreen);
			} else {
				clearScreen();
			}
		});
	}

	@SuppressWarnings({"ConstantValue", "java:S1066"})
	public static void onSetScreen(ContainerScreen screen) {
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
	public static boolean onSlotClick(int slot, ItemStack stack, int button) {
		return currentSolver != null && currentSolver.onClickSlot(slot, stack, screenId, button);
	}

	public static void onDraw(GuiGraphics context, AbstractContainerScreen<ChestMenu> handledScreen, List<Slot> slots) {
		if (currentSolver == null) return;

		context.pose().pushMatrix();
		context.pose().translate(((AbstractContainerScreenAccessor) handledScreen).getX(), ((AbstractContainerScreenAccessor) handledScreen).getY());

		if (highlights == null) highlights = currentSolver.getColors(slotMap(currentSolver instanceof ContainerAndInventorySolver ? slots : slots.subList(0, handledScreen.getMenu().getRowCount() * 9)));
		for (ColorHighlight highlight : highlights) {
			Slot slot = slots.get(highlight.slot());
			int color = highlight.color();
			context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
		}

		context.pose().popMatrix();
	}

	public static Int2ObjectMap<ItemStack> slotMap(List<Slot> slots) {
		Int2ObjectMap<ItemStack> slotMap = new Int2ObjectRBTreeMap<>();
		for (int i = 0; i < slots.size(); i++) {
			slotMap.put(i, slots.get(i).getItem());
		}
		return slotMap;
	}
}
