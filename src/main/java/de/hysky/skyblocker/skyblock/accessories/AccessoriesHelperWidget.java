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
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.item.wikilookup.WikiLookupManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeResultButton;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.hoveredItem.HoveredItemStackProvider;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
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
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
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
	private boolean refreshWhenDoneLoading = false;

	// Things that should persist when you close the accessory bag
	private static Filter filter = Filter.ALL;
	private static int page;
	private static boolean open;
	private static boolean showHighestTierOnly;

	static void attachToScreen(ContainerScreen screen) {
		if (!SkyblockerConfigManager.get().general.itemTooltip.enableAccessoriesHelper || !SkyblockerConfigManager.get().helpers.enableAccessoriesHelperWidget) return;
		final AccessoriesHelperWidget widget = new AccessoriesHelperWidget();
		widget.setY((screen.height - widget.getHeight()) / 2);
		Screens.getWidgets(screen).add(widget);
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
				widget.refreshData();
			} else {
				// Reset page when you close the helper. keep rest for UX
				page = 0;
			}
		});
		Screens.getWidgets(screen).add(tabButton);
		tabButton.setToggled(open);
	}

	AccessoriesHelperWidget() {
		super(0, 0, 147, 182, CommonComponents.EMPTY, AbstractScrollArea.defaultSettings(4));
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
				.create(0, 0, filterWidth, 16, Component.translatable("skyblocker.accessoryHelper.filter"), (_, v) -> {
					filter = v;
					updateFilter();
					changePage(0);
				})
		);
		mainLayout.addChild(CycleButton.booleanBuilder(Component.translatable("skyblocker.accessoryHelper.highestTierOnly"), Component.translatable("skyblocker.accessoryHelper.allTiers"), showHighestTierOnly)
				.displayOnlyValue()
				.create(0, 0, filterWidth, 16, CommonComponents.EMPTY, (_, value) -> {
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

	private void refreshData() {
		final Set<Accessory> collectedAccessories = AccessoriesHelper.getCollectedAccessories();
		this.recombDisplays = collectedAccessories.stream()
				.filter(Accessory::recombobulatable)
				.map(Accessory::id)
				.filter(id -> !AccessoriesHelper.isRecombobulated(id))
				.map(ItemRepository::getItemStack)
				.filter(Objects::nonNull)
				.map(SkyblockerStack::getSkyblockRarity)
				.distinct()
				.map(RecombobulateSource::new)
				.toList();

		this.accessories = AccessoriesHelper.ACCESSORY_DATA.values().stream()
				.filter(accessory -> NEURepoManager.isLoading() || !NEURepoManager.getConstants().getMisc().getIgnoredTalismans().contains(accessory.id())) // Removes admin and rift items
				.map(accessory -> {
					if (accessory.family().isPresent()) {
						AccessoriesHelper.FamilyReport report = AccessoriesHelper.calculateFamilyReport(accessory, collectedAccessories);
						return new AccessoryInfo(accessory, report.highestCollectedInFamily(), report.highestInFamily());
					} else
						return new AccessoryInfo(accessory, collectedAccessories.contains(accessory) ? Optional.of(accessory) : Optional.empty(), accessory);
				}).distinct()
				.filter(info -> info.accessory().tier() > info.highestOwned().map(Accessory::tier).orElse(-1))
				.toList();
		this.updateFilter(); // Update items
		this.changePage(0); // Updates display
	}

	private void updateFilter() {
		Predicate<AccessoryInfo> predicate = switch (filter) {
			case ALL -> _ -> true;
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
		FlexibleItemStack stack = ItemRepository.getItemStack(acc.id());
		if (stack == null) return OptionalDouble.empty();
		DoubleBooleanPair optionalPrice = ItemUtils.getItemPrice(stack);
		double price;
		if (optionalPrice.rightBoolean()) price = optionalPrice.firstDouble();
		else price = ItemUtils.getCraftCost(stack.getNeuName());
		if (price <= 0) return OptionalDouble.empty();
		return OptionalDouble.of(price);
	}

	private void changePage(int offset) {
		page = Math.clamp(page + offset, 0, getPageCount() - 1);
		updatePageSwitcher();
		for (int i = 0; i < BUTTON_COUNT; i++) {
			int j = i + page * BUTTON_COUNT;
			if (j < displays.size()) buttons.get(i).setSource(displays.get(j));
			else buttons.get(i).clearDisplayStack();
		}
	}

	private int getPageCount() {
		return Math.max(1, Math.ceilDiv(displays.size(), BUTTON_COUNT));
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
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), getWidth(), getHeight());
		String prevHighlighted = AccessoriesContainerSolver.INSTANCE.highlightedAccessory;
		AccessoriesContainerSolver.INSTANCE.highlightedAccessory = null;
		for (AbstractWidget widget : widgets) {
			widget.extractRenderState(graphics, mouseX, mouseY, a);
		}
		if (!ItemRepository.filesImported() || TooltipInfoType.ACCESSORIES.getData() == null) {
			refreshWhenDoneLoading = true;
			int x = getX() + getWidth() / 2;
			int y = getY() + getHeight() / 4;
			graphics.centeredText(Minecraft.getInstance().font, "Loading...", x, y, -1);
			graphics.centeredText(Minecraft.getInstance().font, LoadingDotsText.get(Util.getMillis()), x, y + 10, -1);
		} else if (refreshWhenDoneLoading) {
			refreshWhenDoneLoading = false;
			refreshData();
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
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, textures.get(true, isHovered()), getX(), getY(), getWidth(), getHeight());
			if (isHovered()) graphics.requestCursor(CursorTypes.POINTING_HAND);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	// TODO abstract away this and SkyblockRecipeTabButton
	private static class TabButton extends ImageButton {
		private final Consumer<TabButton> onToggled;
		private boolean toggled;

		TabButton(Consumer<TabButton> onToggled) {
			super(35, 27, RecipeBookTabButton.SPRITES, _ -> {}, CommonComponents.EMPTY);
			this.onToggled = onToggled;
		}

		@Override
		public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			int x = this.getX();
			if (this.toggled) x -= 2;

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(true, this.toggled), x, this.getY(), this.width, this.height);

			int offset = this.toggled ? -2 : 0;
			WidgetSprites buttonTextures = this.toggled ? RecipeBookPage.PAGE_FORWARD_SPRITES : RecipeBookPage.PAGE_BACKWARD_SPRITES;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
					buttonTextures.get(false, isHovered()),
					getX() + offset + 9,
					getY() + (getHeight() - 17) / 2,
					12,
					17);


			if (this.isHovered()) {
				graphics.requestCursor(CursorTypes.POINTING_HAND);
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
			setDisplayStack(source.icon().getStackOrThrow());
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
			ItemStack stack = getDisplayStack();
			if (isHovered() && stack != null && source != null) {
				source.extractTooltip(graphics, mouseX, mouseY);
				graphics.requestCursor(CursorTypes.POINTING_HAND);
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
			private final @Nullable List<FormattedCharSequence> afterSelling;

			private @Nullable FlexibleItemStack icon;

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
							Component translatable = Component.translatable(
									"skyblocker.accessoryHelper.afterSelling",
									ItemTooltip.getCoinsMessage(priceOpt.getAsDouble() - price.leftDouble(), 1),
									accStack.getStackOrThrow().getHoverName());
							return Optional.of(Minecraft.getInstance().font.split(translatable, 170));
						})
						.orElse(null);
			}

			@Override
			public FlexibleItemStack icon() {
				return icon != null ? icon : (icon = Optional.ofNullable(ItemRepository.getItemStack(info.accessory().id())).orElse(ItemUtils.getItemIdPlaceholder(info.accessory().id())));
			}

			@Override
			public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
				if (icon == null) {
					return;
				}
				info.highestOwned().ifPresent(owned -> AccessoriesContainerSolver.INSTANCE.highlightedAccessory = owned.id());
				Minecraft client = Minecraft.getInstance();
				List<FormattedCharSequence> tooltip = Screen.getTooltipFromItem(client, icon.getStackOrThrow()).stream().map(Component::getVisualOrderText).collect(Util.toMutableList());
				tooltip.add(smoothLine.getVisualOrderText());
				if (afterSelling != null) {
					tooltip.addAll(afterSelling);
					tooltip.add(FormattedCharSequence.EMPTY);
				}
				tooltip.add(wikiLine.getVisualOrderText());
				tooltip.add(fandomLine.getVisualOrderText());
				graphics.setTooltipForNextFrame(client.font, tooltip, mouseX, mouseY, icon.get(DataComponents.TOOLTIP_STYLE));
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
					FlexibleItemStack stack = ItemRepository.getItemStack(info.highestOwned().get().id());
					originalMP = stack != null ? stack.getSkyblockRarity().getMP() : 0;
				}
				FlexibleItemStack stack = ItemRepository.getItemStack(info.accessory().id());
				if (stack == null) return Double.MAX_VALUE;
				int mp = stack.getSkyblockRarity().getMP() - originalMP;
				return mp <= 0 ? Double.MAX_VALUE : price / mp;
			}

			@Override
			public void click() {
				if (icon == null) return;
				LocalPlayer player = Minecraft.getInstance().player;
				if (player == null) return;
				WikiLookupManager.openWiki(icon.getStackOrThrow(), player, !Minecraft.getInstance().hasShiftDown());
			}
		}
	}

	private static class RecombobulateSource implements MagicPowerSource {
		private final FlexibleItemStack icon;
		private final double pricePerMp;
		private final List<Component> tooltip;
		private RecombobulateSource(SkyblockItemRarity rarity) {
			this.icon = ItemRepository.getItemStack("RECOMBOBULATOR_3000", Ico.BARRIER);
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
		public FlexibleItemStack icon() {
			return icon;
		}

		@Override
		public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
			Font textRenderer = Minecraft.getInstance().font;
			graphics.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
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
		FlexibleItemStack icon();

		void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

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
