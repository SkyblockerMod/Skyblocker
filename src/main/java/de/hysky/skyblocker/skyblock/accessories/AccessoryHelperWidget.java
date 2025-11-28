package de.hysky.skyblocker.skyblock.accessories;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
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
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.RecipeBookResults;
import net.minecraft.client.gui.screen.recipebook.RecipeGroupButtonWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

	private List<AccessoriesHelper.Accessory> missingAccessories = List.of();
	private List<AccessoriesHelper.Accessory> upgradableAccessories = List.of();
	private List<AccessoriesHelper.Accessory> accessories = List.of();

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

	void setAccessories(List<AccessoriesHelper.Accessory> missingAccessories, List<AccessoriesHelper.Accessory> upgradableAccessories) {
		this.missingAccessories = missingAccessories;
		this.upgradableAccessories = upgradableAccessories;
		setFilter(filter);
		changePage(0); // Updates items
	}

	private void setFilter(Filter filter) {
		this.filter = filter;
		accessories = Stream.concat(
				filter == Filter.UPGRADES ? Stream.empty() : missingAccessories.stream(),
				filter == Filter.MISSING ? Stream.empty() : upgradableAccessories.stream()
		).sorted(Comparator.comparingDouble(acc -> {
					ItemStack stack = ItemRepository.getItemStack(acc.id());
					if (stack == null) return Double.MAX_VALUE;
					DoubleBooleanPair optionalPrice = ItemUtils.getItemPrice(stack);
					double price;
					if (optionalPrice.rightBoolean()) price = optionalPrice.firstDouble();
					else price = ItemUtils.getCraftCost(stack.getSkyblockApiId());
					if (price <= 0) return Double.MAX_VALUE;
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

	private void changePage(int offset) {
		page = Math.clamp(page + offset, 0, getPageCount());
		updatePageSwitcher();
		for (int i = 0; i < BUTTON_COUNT; i++) {
			int j = i + page * BUTTON_COUNT;
			if (j < accessories.size()) buttons.get(i).setAccessory(accessories.get(j));
			else buttons.get(i).clearDisplayStack();
		}
	}

	private int getPageCount() {
		return accessories.size() / BUTTON_COUNT + 1;
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
		private AccessoriesHelper.Accessory accessory;

		private void setAccessory(AccessoriesHelper.Accessory accessory) {
			this.accessory = accessory;
			ItemStack stack = ItemRepository.getItemStack(accessory.id());
			if (stack == null) return;
			setDisplayStack(stack);
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			super.renderWidget(context, mouseX, mouseY, delta);
			ItemStack stack = getDisplayStack();
			if (isHovered() && stack != null) context.drawItemTooltip(MinecraftClient.getInstance().textRenderer, stack, mouseX, mouseY);
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

	private enum Filter {
		ALL,
		MISSING,
		UPGRADES
	}
}
