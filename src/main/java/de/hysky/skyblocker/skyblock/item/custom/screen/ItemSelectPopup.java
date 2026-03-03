package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemSelectPopup extends AbstractPopupScreen {

	private static final Identifier BACKGROUND_TEXTURE = SkyblockerMod.id("textures/gui/inventory_item_selection.png");
	private static final int TEXTURE_WIDTH = 176;
	private static final int TEXTURE_HEIGHT = 132;
	private final Consumer<ItemStack> callback;

	private final GridLayout mainGrid = new GridLayout();
	private final LinearLayout equipmentLayout = LinearLayout.horizontal();

	private int x, y;

	protected ItemSelectPopup(Screen backgroundScreen, Consumer<ItemStack> callback) {
		super(Component.literal("Select Item"), backgroundScreen);
		this.callback = callback;
	}

	@Override
	protected void init() {
		super.init();
		GridLayout.RowHelper adder = mainGrid.createRowHelper(9);
		LocalPlayer player = minecraft.player;
		NonNullList<ItemStack> stacks = player.getInventory().getNonEquipmentItems();
		for (int i = Inventory.SELECTION_SIZE; i < stacks.size() + Inventory.SELECTION_SIZE; i++) {
			ItemStack stack = stacks.get(i % stacks.size());
			if (stack.isEmpty()) adder.addChild(new SpacerElement(18, 18));
			else adder.addChild(new ItemWidget(stack));
		}
		Arrays.stream(EquipmentSlot.values()).filter(e -> e.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toList().reversed().forEach(slot -> {
			ItemStack stack = player.getItemBySlot(slot);
			if (stack.isEmpty()) equipmentLayout.addChild(new SpacerElement(18, 18));
			else equipmentLayout.addChild(new ItemWidget(stack));
		});
		equipmentLayout.addChild(new SpacerElement(18, 18));
		for (ItemStack stack : (Utils.isInTheRift() ? SkyblockInventoryScreen.equipment_rift : SkyblockInventoryScreen.equipment)) {
			if (stack.isEmpty()) equipmentLayout.addChild(new SpacerElement(18, 18));
			else equipmentLayout.addChild(new ItemWidget(stack));
		}
		mainGrid.visitWidgets(this::addRenderableWidget);
		mainGrid.arrangeElements();
		equipmentLayout.visitWidgets(this::addRenderableWidget);
		equipmentLayout.arrangeElements();
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		x = (width - TEXTURE_WIDTH) / 2;
		y = (height - TEXTURE_HEIGHT) / 2;
		mainGrid.setPosition(x + 7, y + 53);
		equipmentLayout.setPosition(x + 7, y + 17);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	private class ItemWidget extends AbstractWidget {
		private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_back");
		private static final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.withDefaultNamespace("container/slot_highlight_front");

		private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
		private final ItemStack item;
		private final boolean selectable;

		private ItemWidget(ItemStack item) {
			super(0, 0, 18, 18, item.getHoverName());
			selectable = !item.getUuid().isEmpty();
			Component message = selectable ? getMessage() : getMessage().copy().append("\n").append(Component.translatable("skyblocker.customization.item.cannotCustomize").withStyle(ChatFormatting.RED));
			setTooltip(Tooltip.create(message));
			this.item = item;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			int x = getX() + 1;
			int y = getY() + 1;
			if (isHovered()) context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, getX() - 3, getY() - 3, 24, 24);
			context.renderItem(item, x, y);
			if (isHovered()) context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, getX() - 3, getY() - 3, 24, 24);
			if (!selectable) context.renderItem(BARRIER, x, y);

		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			if (selectable) {
				callback.accept(item);
				onClose();
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}
}
