package de.hysky.skyblocker.skyblock.accessories;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.accessories.AccessoriesHelper.Accessory;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeResultButton;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

class AccessoryHelperWidget extends ContainerWidget {
	private static final Identifier TEXTURE = SkyblockerMod.id("background");
	private static final int BORDER_SIZE = 8;
	private static final int BUTTON_COUNT = 20;
	private final List<ClickableWidget> widgets;
	private final List<ResultButton> buttons = new ArrayList<>(BUTTON_COUNT);
	private final SimplePositioningWidget layout;
	private final TextWidget pageText = new TextWidget(ScreenTexts.EMPTY, MinecraftClient.getInstance().textRenderer).setMaxWidth(30, TextWidget.TextOverflow.SCROLLING);
	private final ArrowButton prevPageButton = new ArrowButton(false);
	private final ArrowButton nextPageButton = new ArrowButton(true);
	private Filter filter = Filter.MISSING;

	private List<AccessoryInfo> accessories = List.of();
	private List<AccessoryInfo> displayedAccessories = List.of();

	private int page;

	AccessoryHelperWidget() {
		super(0, 0, 147, 166, ScreenTexts.EMPTY);
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
		mainLayout.add(CyclingButtonWidget.<Filter>builder(f -> Text.translatable(f.toString()))
				.values(Filter.values())
				.initially(filter)
				.build(0, 0, 80, 16, Text.literal("Filter"), (b, v) -> {
					setFilter(v);
					changePage(0);
				}), Positioner::alignRight
		);
		mainLayout.forEachChild(builder::add);
		widgets = builder.build();
		mainLayout.refreshPositions();
		updatePageSwitcher();
	}

	void setAccessories(Collection<AccessoryInfo> accessories) {
		this.accessories = accessories.stream()
				.filter(info -> info.accessory().tier() > info.highestOwned().map(Accessory::tier).orElse(-1))
				.toList();
		setFilter(filter);
		changePage(0); // Updates items
	}

	private void setFilter(Filter filter) {
		this.filter = filter;
		Predicate<AccessoryInfo> predicate = switch (this.filter) {
			case ALL -> info -> true;
			case MISSING -> info -> info.highestOwned().isEmpty();
			case UPGRADES -> info -> info.highestOwned().isPresent() && info.accessory().tier() > info.highestOwned().get().tier();
		};
		displayedAccessories = accessories.stream()
				.filter(predicate)
				.sorted(Comparator.comparingDouble(info -> {
					OptionalDouble priceOpt = getPrice(info.accessory());
					if (priceOpt.isEmpty()) return Double.MAX_VALUE;
					double price = priceOpt.getAsDouble();
					if (info.highestOwned().isPresent()) {
						OptionalDouble ownedPrice = getPrice(info.highestOwned().get());
						price -= ownedPrice.orElse(0);
					}
					ItemStack stack = ItemRepository.getItemStack(info.accessory().id());
					if (stack == null) return Double.MAX_VALUE;
					int mp = switch (stack.getSkyblockRarity()) {
						case COMMON, SPECIAL -> 3;
						case UNCOMMON, VERY_SPECIAL -> 5;
						case RARE -> 8;
						case EPIC -> 12;
						case LEGENDARY -> 13;
						case MYTHIC -> 22;
						default -> 1;
					};
					return price / mp;
				})
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
			if (j < displayedAccessories.size()) buttons.get(i).setAccessory(displayedAccessories.get(j));
			else buttons.get(i).clearDisplayStack();
		}
	}

	private int getPageCount() {
		return displayedAccessories.size() / BUTTON_COUNT + 1;
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
		for (ClickableWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
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

	private static class ResultButton extends SkyblockRecipeResultButton {
		private AccessoryInfo accessory;
		private @Nullable Text afterSelling;

		private void setAccessory(AccessoryInfo info) {
			this.accessory = info;
			ItemStack stack = ItemRepository.getItemStack(info.accessory().id());
			afterSelling = null;
			if (stack == null) return;
			OptionalDouble priceOpt = getPrice(info.accessory);
			if (priceOpt.isPresent() && accessory.highestOwned().isPresent()) {
				Accessory acc = accessory.highestOwned().get();
				ItemStack accStack = ItemRepository.getItemStack(acc.id());
				if (accStack != null) {
					DoubleBooleanPair price = ItemUtils.getItemPrice(accStack);
					if (price.rightBoolean()) {
						afterSelling = Text.empty()
								.append(ItemTooltip.getCoinsMessage(priceOpt.getAsDouble() - price.leftDouble(), 1))
								.append(" after selling ")
								.append(accStack.getName());
					}
				}
			}
			setDisplayStack(stack);
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			super.renderWidget(context, mouseX, mouseY, delta);
			ItemStack stack = getDisplayStack();
			if (isHovered() && stack != null) {
				MinecraftClient client = MinecraftClient.getInstance();
				List<Text> tooltip = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
				if (afterSelling != null) {
					tooltip.add(afterSelling);
				}
				context.drawTooltip(client.textRenderer, tooltip, stack.getTooltipData(), mouseX, mouseY, stack.get(DataComponentTypes.TOOLTIP_STYLE));
			}
		}

		@Override
		protected void clearDisplayStack() {
			super.clearDisplayStack();
		}
	}

	static class TabButton extends ToggleButtonWidget {
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

	record AccessoryInfo(Accessory accessory, Optional<Accessory> highestOwned) {}

	private enum Filter {
		ALL,
		MISSING,
		UPGRADES
	}
}
