package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import java.time.Duration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SkyblockCraftingTableScreen extends AbstractContainerScreen<SkyblockCraftingTableScreenHandler> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png");
	protected static final WidgetSprites MORE_CRAFTS_TEXTURES = new WidgetSprites(
			SkyblockerMod.id("quick_craft/more_button"),
			SkyblockerMod.id("quick_craft/more_button_disabled"),
			SkyblockerMod.id("quick_craft/more_button_highlighted")
	);

	protected static final Identifier QUICK_CRAFT = SkyblockerMod.id("textures/gui/sprites/quick_craft/quick_craft_overlay.png");
	private final SkyblockRecipeBookWidget recipeBook = new SkyblockRecipeBookWidget(menu);
	private boolean narrow;
	private ImageButton moreCraftsButton;

	public SkyblockCraftingTableScreen(SkyblockCraftingTableScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		this.narrow = this.width < 379;
		this.recipeBook.init(this.width, this.height, this.minecraft, this.narrow);
		this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
		this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
			this.recipeBook.toggleVisibility();
			this.leftPos = this.recipeBook.updateScreenPosition(this.width, this.imageWidth);
			button.setPosition(this.leftPos + 5, this.height / 2 - 49);
			if (moreCraftsButton != null) moreCraftsButton.setPosition(this.leftPos + 152, this.topPos + 63);
		}));
		if (!menu.mirrorverse) {
			moreCraftsButton = new ImageButton(this.leftPos + 152, topPos + 63, 16, 16, MORE_CRAFTS_TEXTURES,
					button -> this.slotClicked(menu.slots.get(26), menu.slots.get(26).index, 0, ClickType.PICKUP));
			moreCraftsButton.setTooltipDelay(Duration.ofMillis(250L));
			moreCraftsButton.setTooltip(Tooltip.create(Component.literal("More Crafts")));
			this.addRenderableWidget(moreCraftsButton);
		}
		assert (minecraft != null ? minecraft.player : null) != null;
		this.addWidget(this.recipeBook);
		this.setInitialFocus(this.recipeBook);
		this.titleLabelX = 29;
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.recipeBook.tick();
		if (moreCraftsButton == null) return;
		ItemStack stack = menu.slots.get(26).getItem();
		moreCraftsButton.active = stack.isEmpty() || stack.is(Items.PLAYER_HEAD);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (this.recipeBook.isVisible() && this.narrow) {
			this.renderBackground(context, mouseX, mouseY, delta);
			this.recipeBook.render(context, mouseX, mouseY, delta);
		} else {
			super.render(context, mouseX, mouseY, delta);
			this.recipeBook.render(context, mouseX, mouseY, delta);
			this.recipeBook.renderGhostRecipe(context, true);
		}
		this.renderTooltip(context, mouseX, mouseY);
		this.recipeBook.renderTooltip(context, mouseX, mouseY, null);
	}

	@Override
	protected void renderSlot(GuiGraphics context, Slot slot, int mouseX, int mouseY) {
		ItemStack stack = slot.getItem();
		if (slot.index == 23 && stack.is(Items.BARRIER)) return;
		if (stack.is(Items.GRAY_STAINED_GLASS_PANE) && stack.getSkyblockId().isEmpty()) return;
		super.renderSlot(context, slot, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = (this.height - this.imageHeight) / 2;
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		//4 px of margin to allow some space for custom resource packs that have size differences on the crafting table/inventory textures
		if (!menu.mirrorverse) context.blit(RenderPipelines.GUI_TEXTURED, QUICK_CRAFT, i + 143, j - 3, 0, 0, 37, 90, 37, 90);
	}

	@Override
	protected boolean isHovering(int x, int y, int width, int height, double pointX, double pointY) {
		return (!this.narrow || !this.recipeBook.isVisible()) && super.isHovering(x, y, width, height, pointX, pointY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (this.recipeBook.mouseClicked(click, doubled)) {
			this.setFocused(this.recipeBook);
			return true;
		}
		if (this.narrow && this.recipeBook.isVisible()) {
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
		boolean bl = mouseX < (double) left || mouseY < (double) top || mouseX >= (double) (left + this.imageWidth) || mouseY >= (double) (top + this.imageHeight);
		return this.recipeBook.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight) && bl;
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType) {
		super.slotClicked(slot, slotId, button, actionType);
		this.recipeBook.slotClicked(slot);
	}
}
