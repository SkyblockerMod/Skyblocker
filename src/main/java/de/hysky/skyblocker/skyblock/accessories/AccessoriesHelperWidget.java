package de.hysky.skyblocker.skyblock.accessories;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.injected.SkyblockerStack;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

class AccessoriesHelperWidget extends ContainerWidget implements HoveredItemStackProvider {
	private static final Identifier TEXTURE = SkyblockerMod.id("background");
	private static final int BORDER_SIZE = 8;
	private static final int BUTTON_COUNT = 20;
	private final List<ClickableWidget> widgets;
	private final List<ResultButton> buttons = new ArrayList<>(BUTTON_COUNT);
	private final SimplePositioningWidget layout;
	private final TextWidget pageText = new TextWidget(ScreenTexts.EMPTY, MinecraftClient.getInstance().textRenderer).setMaxWidth(30, TextWidget.TextOverflow.SCROLLING);
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

	static void attachToScreen(GenericContainerScreen screen) {
		if (!SkyblockerConfigManager.get().helpers.enableAccessoriesHelperWidget) return;
		final AccessoriesHelperWidget widget = new AccessoriesHelperWidget();
		widget.setY((screen.height - widget.getHeight()) / 2);
		Screens.getButtons(screen).add(widget);
		final int previousX = ((HandledScreenAccessor) screen).getX();
		final int offset = Math.max(180 - previousX, 0);
		TabButton tabButton = new TabButton(button -> {
			boolean toggled = button.isToggled();
			widget.visible = open = toggled;
			int x = toggled ? previousX + offset : previousX;
			((HandledScreenAccessor) screen).setX(x);
			widget.setX(x - widget.getWidth() - 2);
			button.setX((toggled ? widget.getX() : x) - button.getWidth() + 5);
			button.setY((toggled ? widget.getY() : ((HandledScreenAccessor) screen).getY()) + 8);
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
		super(0, 0, 147, 182, ScreenTexts.EMPTY);
		this.layout = new SimplePositioningWidget(getWidth() - BORDER_SIZE * 2, getHeight() - BORDER_SIZE * 2);
		DirectionalLayoutWidget mainLayout = layout.add(DirectionalLayoutWidget.vertical());
		mainLayout.getMainPositioner().alignHorizontalCenter();
		GridWidget.Adder adder = mainLayout.add(new GridWidget()).createAdder(5);
		for (int i = 0; i < BUTTON_COUNT; i++) {
			ResultButton button = new ResultButton();
			buttons.add(button);
			adder.add(button);
		}
		ImmutableList.Builder<ClickableWidget> builder = ImmutableList.builder();
		DirectionalLayoutWidget pageSwitcher = mainLayout.add(DirectionalLayoutWidget.horizontal().spacing(2), p -> p.marginY(4));
		pageSwitcher.getMainPositioner().alignVerticalCenter();
		pageSwitcher.add(prevPageButton);
		pageSwitcher.add(new SimplePositioningWidget(30, 0)).add(pageText);
		pageSwitcher.add(nextPageButton);

		int filterWidth = layout.getWidth() - 2;

		mainLayout.add(CyclingButtonWidget.<Filter>builder(f -> Text.translatable(f.toString()))
				.values(Filter.values())
				.initially(filter)
				.build(0, 0, filterWidth, 16, Text.translatable("skyblocker.accessory_helper.filter"), (b, v) -> {
					filter = v;
					updateFilter();
					changePage(0);
				})
		);
		mainLayout.add(CyclingButtonWidget.onOffBuilder(Text.translatable("skyblocker.accessory_helper.highestTierOnly"), Text.translatable("skyblocker.accessory_helper.allTiers"))
				.initially(showHighestTierOnly)
				.omitKeyText()
				.build(0, 0, filterWidth, 16, ScreenTexts.EMPTY, (button, value) -> {
					showHighestTierOnly = value;
					updateFilter();
					changePage(0);
				})
		);
		mainLayout.forEachChild(builder::add);
		widgets = builder.build();
		mainLayout.refreshPositions();
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
		pageText.setMessage(Text.translatable("gui.recipebook.page", page + 1, getPageCount()));
		prevPageButton.visible = page > 0;
		nextPageButton.visible = page < getPageCount() - 1;
		layout.refreshPositions();
	}

	@Override
	public List<? extends Element> children() {
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
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), getWidth(), getHeight());
		String prevHighlighted = AccessoriesContainerSolver.INSTANCE.highlightedAccessory;
		AccessoriesContainerSolver.INSTANCE.highlightedAccessory = null;
		for (ClickableWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
		if (!Objects.equals(prevHighlighted, AccessoriesContainerSolver.INSTANCE.highlightedAccessory)) ContainerSolverManager.markHighlightsDirty();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	@Override
	protected int getContentsHeightWithPadding() {
		return 0;
	}

	@Override
	public @Nullable ItemStack getFocusedItem() {
		return buttons.stream().map(ResultButton::getFocusedItem).filter(Objects::nonNull).findFirst().orElse(null);
	}

	private class ArrowButton extends ClickableWidget {
		private final boolean next;
		private final ButtonTextures textures;

		ArrowButton(boolean next) {
			super(0, 0, 12, 17, ScreenTexts.EMPTY);
			this.next = next;
			this.textures = next ? RecipeBookResults.PAGE_FORWARD_TEXTURES : RecipeBookResults.PAGE_BACKWARD_TEXTURES;
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			changePage(next ? 1 : -1);
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, textures.get(true, isHovered()), getX(), getY(), getWidth(), getHeight());
			if (isHovered()) context.setCursor(StandardCursors.POINTING_HAND);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	// TODO abstract away this and SkyblockRecipeTabButton
	private static class TabButton extends ToggleButtonWidget {
		private final Consumer<TabButton> onToggled;

		TabButton(Consumer<TabButton> onToggled) {
			super(0, 0, 35, 27, false);
			this.setTextures(RecipeGroupButtonWidget.TEXTURES);
			this.onToggled = onToggled;
		}

		@Override
		public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			if (this.textures == null) return;
			int x = this.getX();
			if (this.toggled) x -= 2;

			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.textures.get(true, this.toggled), x, this.getY(), this.width, this.height);

			int offset = this.toggled ? -2 : 0;
			ButtonTextures buttonTextures = this.toggled ? RecipeBookResults.PAGE_FORWARD_TEXTURES : RecipeBookResults.PAGE_BACKWARD_TEXTURES;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
					buttonTextures.get(false, isHovered()),
					getX() + offset + 9,
					getY() + (getHeight() - 17) / 2,
					12,
					17);


			if (this.isHovered()) {
				context.setCursor(StandardCursors.POINTING_HAND);
			}
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			super.onClick(click, doubled);
			setToggled(!this.toggled);
		}

		@Override
		public void setToggled(boolean toggled) {
			super.setToggled(toggled);
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
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			super.renderWidget(context, mouseX, mouseY, delta);
			ItemStack stack = getDisplayStack();
			if (isHovered() && stack != null && source != null) {
				source.drawTooltip(context, mouseX, mouseY);
				context.setCursor(StandardCursors.POINTING_HAND);
			}
		}

		@Override
		public void onClick(Click click, boolean doubled) {
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
			private static final Text smoothLine = LineSmoothener.createSmoothLine();
			private static final Text wikiLine = Text.translatable("skyblocker.accessory_helper.openWiki").formatted(Formatting.YELLOW);
			private static final Text fandomLine = Text.translatable("skyblocker.accessory_helper.fandom").formatted(Formatting.GRAY, Formatting.ITALIC);

			private final AccessoryInfo info;
			private final @Nullable List<Text> afterSelling;

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
									Text.translatable("skyblocker.accessory_helper.afterSelling", ItemTooltip.getCoinsMessage(priceOpt.getAsDouble() - price.leftDouble(), 1)),
									accStack.getName(),
									Text.empty()
							));
						})
						.orElse(null);
			}

			@Override
			public ItemStack icon() {
				return icon != null ? icon : (icon = Optional.ofNullable(ItemRepository.getItemStack(info.accessory().id())).orElse(ItemStack.EMPTY));
			}

			@Override
			public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
				if (icon == null) {
					return;
				}
				info.highestOwned().ifPresent(owned -> AccessoriesContainerSolver.INSTANCE.highlightedAccessory = owned.id());
				MinecraftClient client = MinecraftClient.getInstance();
				List<Text> tooltip = new ArrayList<>(Screen.getTooltipFromItem(client, icon));
				tooltip.add(smoothLine);
				if (afterSelling != null) {
					tooltip.addAll(afterSelling);
				}
				tooltip.add(wikiLine);
				tooltip.add(fandomLine);
				context.drawTooltip(client.textRenderer, tooltip, icon.getTooltipData(), mouseX, mouseY, icon.get(DataComponentTypes.TOOLTIP_STYLE));
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
				ClientPlayerEntity player = MinecraftClient.getInstance().player;
				if (player == null) return;
				WikiLookupManager.openWiki(icon, player, !MinecraftClient.getInstance().isShiftPressed());
			}
		}
	}

	private static class RecombobulateSource implements MagicPowerSource {
		private final ItemStack icon;
		private final double pricePerMp;
		private final List<Text> tooltip;
		private RecombobulateSource(SkyblockItemRarity rarity) {
			this.icon = ItemRepository.getItemStack("RECOMBOBULATOR_3000", ItemStack.EMPTY);
			DoubleBooleanPair pair = ItemUtils.getItemPrice("RECOMBOBULATOR_3000");
			double price = pair.rightBoolean() ? pair.leftDouble() : 6000000;
			int mp = rarity.recombulate().getMP() - rarity.getMP();
			pricePerMp = mp <= 0 ? Double.MAX_VALUE : price / mp;
			tooltip = List.of(
					Text.translatable(
							"skyblocker.accessory_helper.recombobulate",
							Text.literal(rarity.name().replace("_", " ")).withColor(rarity.color)
					),
					ItemTooltip.getCoinsMessage(price, 1)
			);
		}

		@Override
		public ItemStack icon() {
			return icon;
		}

		@Override
		public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
		}

		@Override
		public double pricePerMp() {
			return pricePerMp;
		}

		@Override
		public void click() {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) return;
			WikiLookupManager.openWikiLinkName("Recombobulator_3000#Usage", player, !MinecraftClient.getInstance().isShiftPressed());
		}
	}

	private interface MagicPowerSource {
		ItemStack icon();

		void drawTooltip(DrawContext context, int mouseX, int mouseY);

		double pricePerMp();

		void click();
	}

	private enum Filter {
		ALL,
		MISSING,
		UPGRADES;

		@Override
		public String toString() {
			return "skyblocker.accessory_helper.filter." + name();
		}
	}
}
