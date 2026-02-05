package de.hysky.skyblocker.skyblock.accessories;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
import de.hysky.skyblocker.skyblock.accessories.AccessoriesHelper.Accessory;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeResultButton;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class AccessoriesHelperWidget extends AbstractContainerWidget implements HoveredItemStackProvider {
	private static final Identifier TEXTURE = SkyblockerMod.id("background");
	private static final int BORDER_SIZE = 8;
	private static final int BUTTON_COUNT = 20;
	private final List<AbstractWidget> widgets;
	private final List<ResultButton> buttons = new ArrayList<>(BUTTON_COUNT);
	private final FrameLayout layout;
	private final StringWidget pageText = new StringWidget(CommonComponents.EMPTY, Minecraft.getInstance().font).setMaxWidth(30, StringWidget.TextOverflow.SCROLLING);
	private final ArrowButton prevPageButton = new ArrowButton(false);
	private final ArrowButton nextPageButton = new ArrowButton(true);

	private List<AccessoryInfo> accessories = List.of();
	private List<MagicPowerSource> displays = List.of();
	private List<RecombobulateSource> recombDisplays = List.of();

	// Things that should persist when you close the accessory bag
	private static Filter filter = Filter.MISSING;
	private static int page;
	private static boolean open;
	private static boolean showHighestTierOnly;

	static void attachToScreen(ContainerScreen screen) {
		if (!SkyblockerConfigManager.get().helpers.enableAccessoriesHelperWidget) return;
		final AccessoriesHelperWidget widget = new AccessoriesHelperWidget();
		widget.setY((screen.height - widget.getHeight()) / 2);
		Screens.getButtons(screen).add(widget);
		final int previousX = ((AbstractContainerScreenAccessor) screen).getX();
		final int offset = Math.max(180 - previousX, 0);
		TabButton tabButton = new TabButton(button -> {
			boolean toggled = button.toggled;
			widget.visible = open = toggled;
			int x = toggled ? previousX + offset : previousX;
			((AbstractContainerScreenAccessor) screen).setX(x);
			widget.setX(x - widget.getWidth() - 2);
			button.setX((toggled ? widget.getX() : x) - button.getWidth() + 5);
			button.setY((toggled ? widget.getY() : ((AbstractContainerScreenAccessor) screen).getY()) + 8);
			if (toggled) {
				final Set<Accessory> collectedAccessories = AccessoriesHelper.getCollectedAccessories();
				widget.recombDisplays = collectedAccessories.stream()
						.filter(Accessory::recombobulatable)
						.map(Accessory::id)
						.filter(id -> !AccessoriesHelper.isRecombobulated(id))
						.map(ItemRepository::getItemStack)
						.filter(Objects::nonNull)
						.map(SkyblockerStack::getSkyblockRarity)
						.distinct()
						.map(RecombobulateSource::new)
						.toList();

				widget.accessories = AccessoriesHelper.ACCESSORY_DATA.values().stream()
						.filter(accessory -> ItemRepository.getItemStack(accessory.id()) != null) // Removes admin items
						.map(accessory -> {
							if (accessory.family().isPresent()) {
								AccessoriesHelper.FamilyReport report = AccessoriesHelper.calculateFamilyReport(accessory, collectedAccessories);
								return new AccessoryInfo(accessory, report.highestCollectedInFamily(), report.highestInFamily());
							} else
								return new AccessoryInfo(accessory, collectedAccessories.contains(accessory) ? Optional.of(accessory) : Optional.empty(), accessory);
						}).distinct()
						.filter(info -> info.accessory().tier() > info.highestOwned().map(Accessory::tier).orElse(-1))
						.toList();
				widget.updateFilter(); // Update items
				widget.changePage(0); // Updates display
			} else {
				// Reset when you close the helper
				filter = Filter.MISSING;
				page = 0;
				showHighestTierOnly = false;
			}
		});
		Screens.getButtons(screen).add(tabButton);
		tabButton.setToggled(open);
	}

	AccessoriesHelperWidget() {
		super(0, 0, 147, 182, CommonComponents.EMPTY);
		this.layout = new FrameLayout(getWidth() - BORDER_SIZE * 2, getHeight() - BORDER_SIZE * 2);
		LinearLayout mainLayout = layout.addChild(LinearLayout.vertical());
		mainLayout.defaultCellSetting().alignHorizontallyCenter();
		GridLayout.RowHelper adder = mainLayout.addChild(new GridLayout()).createRowHelper(5);
		for (int i = 0; i < BUTTON_COUNT; i++) {
			ResultButton button = new ResultButton();
			buttons.add(button);
			adder.addChild(button);
		}
		ImmutableList.Builder<AbstractWidget> builder = ImmutableList.builder();
		LinearLayout pageSwitcher = mainLayout.addChild(LinearLayout.horizontal().spacing(2), p -> p.paddingVertical(4));
		pageSwitcher.defaultCellSetting().alignVerticallyMiddle();
		pageSwitcher.addChild(prevPageButton);
		pageSwitcher.addChild(new FrameLayout(30, 0)).addChild(pageText);
		pageSwitcher.addChild(nextPageButton);

		int filterWidth = layout.getWidth() - 2;

		mainLayout.addChild(CycleButton.builder(f -> Component.translatable(f.toString()), filter)
				.withValues(Filter.values())
				.create(0, 0, filterWidth, 16, Component.translatable("skyblocker.accessoryHelper.filter"), (b, v) -> {
					filter = v;
					updateFilter();
					changePage(0);
				})
		);
		mainLayout.addChild(CycleButton.booleanBuilder(Component.translatable("skyblocker.accessoryHelper.highestTierOnly"), Component.translatable("skyblocker.accessoryHelper.allTiers"), showHighestTierOnly)
				.displayOnlyValue()
				.create(0, 0, filterWidth, 16, CommonComponents.EMPTY, (button, value) -> {
					showHighestTierOnly = value;
					updateFilter();
					changePage(0);
				})
		);
		mainLayout.visitWidgets(builder::add);
		widgets = builder.build();
		mainLayout.arrangeElements();
		updatePageSwitcher();
	}

	private void updateFilter() {
		Predicate<AccessoryInfo> predicate = switch (filter) {
			case ALL -> info -> true;
			case MISSING -> info -> info.highestOwned().isEmpty();
			case UPGRADES -> info -> info.highestOwned().isPresent() && info.accessory().tier() > info.highestOwned().get().tier();
		};
		Stream<AccessoryInfo.Source> stream = accessories.stream()
				.filter(predicate)
				.filter(info -> !showHighestTierOnly || info.accessory().tier() >= info.highestInFamily().tier())
				.map(AccessoryInfo.Source::new);
		displays = Stream.concat(stream, recombDisplays.stream())
				.sorted(Comparator.comparingDouble(MagicPowerSource::pricePerMp)
				).toList();
	}

	/**
	 * Checks bazaar, lbin and craft cost.
	 */
	private static OptionalDouble getPrice(Accessory acc) {
		ItemStack stack = ItemRepository.getItemStack(acc.id());
		if (stack == null) return OptionalDouble.empty();
		DoubleBooleanPair optionalPrice = ItemUtils.getItemPrice(stack);
		double price;
		if (optionalPrice.rightBoolean()) price = optionalPrice.firstDouble();
		else price = ItemUtils.getCraftCost(stack.getSkyblockApiId());
		if (price <= 0) return OptionalDouble.empty();
		return OptionalDouble.of(price);
	}

	private void changePage(int offset) {
		page = Math.clamp(page + offset, 0, getPageCount());
		updatePageSwitcher();
		for (int i = 0; i < BUTTON_COUNT; i++) {
			int j = i + page * BUTTON_COUNT;
			if (j < displays.size()) buttons.get(i).setSource(displays.get(j));
			else buttons.get(i).clearDisplayStack();
		}
	}

	private int getPageCount() {
		return displays.size() / BUTTON_COUNT + 1;
	}

	private void updatePageSwitcher() {
		pageText.setMessage(Component.translatable("gui.recipebook.page", page + 1, getPageCount()));
		prevPageButton.visible = page > 0;
		nextPageButton.visible = page < getPageCount() - 1;
		layout.arrangeElements();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		layout.setX(x + BORDER_SIZE);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		layout.setY(y + BORDER_SIZE);
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), getWidth(), getHeight());
		String prevHighlighted = AccessoriesContainerSolver.INSTANCE.highlightedAccessory;
		AccessoriesContainerSolver.INSTANCE.highlightedAccessory = null;
		for (AbstractWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
		if (!Objects.equals(prevHighlighted, AccessoriesContainerSolver.INSTANCE.highlightedAccessory)) ContainerSolverManager.markHighlightsDirty();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected double scrollRate() {
		return 0;
	}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	public @Nullable ItemStack getFocusedItem() {
		return buttons.stream().map(ResultButton::getFocusedItem).filter(Objects::nonNull).findFirst().orElse(null);
	}

	private class ArrowButton extends AbstractWidget {
		private final boolean next;
		private final WidgetSprites textures;

		ArrowButton(boolean next) {
			super(0, 0, 12, 17, CommonComponents.EMPTY);
			this.next = next;
			this.textures = next ? RecipeBookPage.PAGE_FORWARD_SPRITES : RecipeBookPage.PAGE_BACKWARD_SPRITES;
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			changePage(next ? 1 : -1);
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, textures.get(true, isHovered()), getX(), getY(), getWidth(), getHeight());
			if (isHovered()) context.requestCursor(CursorTypes.POINTING_HAND);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	// TODO abstract away this and SkyblockRecipeTabButton
	private static class TabButton extends ImageButton {
		private final Consumer<TabButton> onToggled;
		private boolean toggled;

		TabButton(Consumer<TabButton> onToggled) {
			super(35, 27, RecipeBookTabButton.SPRITES, b -> {}, CommonComponents.EMPTY);
			this.onToggled = onToggled;
		}

		@Override
		public void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			if (this.sprites == null) return;
			int x = this.getX();
			if (this.toggled) x -= 2;

			context.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(true, this.toggled), x, this.getY(), this.width, this.height);

			int offset = this.toggled ? -2 : 0;
			WidgetSprites buttonTextures = this.toggled ? RecipeBookPage.PAGE_FORWARD_SPRITES : RecipeBookPage.PAGE_BACKWARD_SPRITES;
			context.blitSprite(RenderPipelines.GUI_TEXTURED,
					buttonTextures.get(false, isHovered()),
					getX() + offset + 9,
					getY() + (getHeight() - 17) / 2,
					12,
					17);


			if (this.isHovered()) {
				context.requestCursor(CursorTypes.POINTING_HAND);
			}
		}

		@Override
		public void onPress(InputWithModifiers input) {
			setToggled(!this.toggled);
		}

		public void setToggled(boolean toggled) {
			this.toggled = toggled;
			onToggled.accept(this);
		}
	}

	private static class ResultButton extends SkyblockRecipeResultButton implements HoveredItemStackProvider {

		private @Nullable MagicPowerSource source;

		private void setSource(MagicPowerSource source) {
			this.source = source;
			setDisplayStack(source.icon());
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			super.renderWidget(context, mouseX, mouseY, delta);
			ItemStack stack = getDisplayStack();
			if (isHovered() && stack != null && source != null) {
				source.drawTooltip(context, mouseX, mouseY);
				context.requestCursor(CursorTypes.POINTING_HAND);
			}
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			if (source != null) source.click();
		}

		@Override
		protected void clearDisplayStack() {
			super.clearDisplayStack();
		}
		@Override
		public @Nullable ItemStack getFocusedItem() {
			return isHovered() ? getDisplayStack() : null;
		}

	}

	private record AccessoryInfo(Accessory accessory, Optional<Accessory> highestOwned, Accessory highestInFamily) {
		private static class Source implements MagicPowerSource {
			private static final Component smoothLine = LineSmoothener.createSmoothLine();
			private static final Component wikiLine = Component.translatable("skyblocker.accessoryHelper.openWiki").withStyle(ChatFormatting.YELLOW);
			private static final Component fandomLine = Component.translatable("skyblocker.accessoryHelper.fandom").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

			private final AccessoryInfo info;
			private final @Nullable List<Component> afterSelling;

			private @Nullable ItemStack icon;

			private Source(AccessoryInfo info) {
				this.info = info;
				afterSelling = info.highestOwned()
						.map(Accessory::id)
						.map(ItemRepository::getItemStack)
						.flatMap(accStack -> {
							OptionalDouble priceOpt = getPrice(info.accessory());
							if (priceOpt.isEmpty()) return Optional.empty();
							DoubleBooleanPair price = ItemUtils.getItemPrice(accStack);
							if (!price.rightBoolean()) return Optional.empty();
							return Optional.of(List.of(
									Component.translatable("skyblocker.accessoryHelper.afterSelling", ItemTooltip.getCoinsMessage(priceOpt.getAsDouble() - price.leftDouble(), 1)),
									accStack.getHoverName(),
									Component.empty()
							));
						})
						.orElse(null);
			}

			@Override
			public ItemStack icon() {
				return icon != null ? icon : (icon = Optional.ofNullable(ItemRepository.getItemStack(info.accessory().id())).orElse(ItemStack.EMPTY));
			}

			@Override
			public void drawTooltip(GuiGraphics context, int mouseX, int mouseY) {
				if (icon == null) {
					return;
				}
				info.highestOwned().ifPresent(owned -> AccessoriesContainerSolver.INSTANCE.highlightedAccessory = owned.id());
				Minecraft client = Minecraft.getInstance();
				List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(client, icon));
				tooltip.add(smoothLine);
				if (afterSelling != null) {
					tooltip.addAll(afterSelling);
				}
				tooltip.add(wikiLine);
				tooltip.add(fandomLine);
				context.setTooltipForNextFrame(client.font, tooltip, icon.getTooltipImage(), mouseX, mouseY, icon.get(DataComponents.TOOLTIP_STYLE));
			}

			@Override
			public double pricePerMp() {
				OptionalDouble priceOpt = getPrice(info.accessory());
				if (priceOpt.isEmpty()) return Double.MAX_VALUE;
				double price = priceOpt.getAsDouble();
				int originalMP = 0;
				if (info.highestOwned().isPresent()) {
					OptionalDouble ownedPrice = getPrice(info.highestOwned().get());
					price -= ownedPrice.orElse(0);
					ItemStack stack = ItemRepository.getItemStack(info.highestOwned().get().id());
					originalMP = stack != null ? stack.getSkyblockRarity().getMP() : 0;
				}
				ItemStack stack = ItemRepository.getItemStack(info.accessory().id());
				if (stack == null) return Double.MAX_VALUE;
				int mp = stack.getSkyblockRarity().getMP() - originalMP;
				return mp <= 0 ? Double.MAX_VALUE : price / mp;
			}

			@Override
			public void click() {
				if (icon == null) return;
				LocalPlayer player = Minecraft.getInstance().player;
				if (player == null) return;
				WikiLookupManager.openWiki(icon, player, !Minecraft.getInstance().hasShiftDown());
			}
		}
	}

	private static class RecombobulateSource implements MagicPowerSource {
		private final ItemStack icon;
		private final double pricePerMp;
		private final List<Component> tooltip;
		private RecombobulateSource(SkyblockItemRarity rarity) {
			this.icon = ItemRepository.getItemStack("RECOMBOBULATOR_3000", ItemStack.EMPTY);
			DoubleBooleanPair pair = ItemUtils.getItemPrice("RECOMBOBULATOR_3000");
			double price = pair.rightBoolean() ? pair.leftDouble() : 6000000;
			int mp = rarity.recombobulate().getMP() - rarity.getMP();
			pricePerMp = mp <= 0 ? Double.MAX_VALUE : price / mp;
			tooltip = List.of(
					Component.translatable(
							"skyblocker.accessoryHelper.recombobulate",
							Component.literal(rarity.name().replace("_", " ")).withColor(rarity.color)
					),
					ItemTooltip.getCoinsMessage(price, 1)
			);
		}

		@Override
		public ItemStack icon() {
			return icon;
		}

		@Override
		public void drawTooltip(GuiGraphics context, int mouseX, int mouseY) {
			Font textRenderer = Minecraft.getInstance().font;
			context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
		}

		@Override
		public double pricePerMp() {
			return pricePerMp;
		}

		@Override
		public void click() {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null) return;
			WikiLookupManager.openWikiLinkName("Recombobulator_3000#Usage", player, !Minecraft.getInstance().hasShiftDown());
		}
	}

	private interface MagicPowerSource {
		ItemStack icon();

		void drawTooltip(GuiGraphics context, int mouseX, int mouseY);

		double pricePerMp();

		void click();
	}

	private enum Filter {
		ALL,
		MISSING,
		UPGRADES;

		@Override
		public String toString() {
			return "skyblocker.accessoryHelper.filter." + name();
		}
	}
}
