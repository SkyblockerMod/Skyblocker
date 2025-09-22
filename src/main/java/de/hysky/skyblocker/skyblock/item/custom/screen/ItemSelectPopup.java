package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.function.Consumer;

public class ItemSelectPopup extends AbstractPopupScreen {

	private static final Identifier BACKGROUND_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/inventory_item_selection.png");
	private static final int TEXTURE_WIDTH = 176;
	private static final int TEXTURE_HEIGHT = 132;
	private final Consumer<ItemStack> callback;

	private final GridWidget mainGrid = new GridWidget();
	private final DirectionalLayoutWidget equipmentLayout = DirectionalLayoutWidget.horizontal();

	private int x, y;

	protected ItemSelectPopup(Screen backgroundScreen, Consumer<ItemStack> callback) {
		super(Text.literal("Select Item"), backgroundScreen);
		this.callback = callback;
	}

	@Override
	protected void init() {
		super.init();
		GridWidget.Adder adder = mainGrid.createAdder(9);
		ClientPlayerEntity player = client.player;
		for (ItemStack stack : player.getInventory().getMainStacks()) {
			if (stack.isEmpty()) adder.add(new EmptyWidget(18, 18));
			else adder.add(new ItemWidget(stack));
		}
		Arrays.stream(EquipmentSlot.values()).filter(e -> e.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).forEach(slot -> {
			ItemStack stack = player.getEquippedStack(slot);
			if (stack.isEmpty()) equipmentLayout.add(new EmptyWidget(18, 18));
			else equipmentLayout.add(new ItemWidget(stack));
		});
		equipmentLayout.add(new EmptyWidget(18, 18));
		for (ItemStack stack : (Utils.isInTheRift() ? SkyblockInventoryScreen.equipment_rift : SkyblockInventoryScreen.equipment)) {
			if (stack.isEmpty()) equipmentLayout.add(new EmptyWidget(18, 18));
			else equipmentLayout.add(new ItemWidget(stack));
		}
		mainGrid.forEachChild(this::addDrawableChild);
		mainGrid.refreshPositions();
		equipmentLayout.forEachChild(this::addDrawableChild);
		equipmentLayout.refreshPositions();
		refreshWidgetPositions();
	}

	@Override
	protected void refreshWidgetPositions() {
		x = (width - TEXTURE_WIDTH) / 2;
		y = (height - TEXTURE_HEIGHT) / 2;
		mainGrid.setPosition(x + 7, y + 53);
		equipmentLayout.setPosition(x + 7, y + 17);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	private class ItemWidget extends ClickableWidget {
		private static final Identifier SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla("container/slot_highlight_back");
		private static final Identifier SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla("container/slot_highlight_front");

		private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
		private final ItemStack item;
		private final boolean selectable;

		private ItemWidget(ItemStack item) {
			super(0, 0, 18, 18, item.getName());
			setTooltip(Tooltip.of(getMessage()));
			this.item = item;
			selectable = !ItemUtils.getItemUuid(item).isEmpty();
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			int x = getX() + 1;
			int y = getY() + 1;
			if (isHovered()) context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_TEXTURE, getX() - 3, getY() - 3, 24, 24);
			context.drawItem(item, x, y);
			if (isHovered()) context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_TEXTURE, getX() - 3, getY() - 3, 24, 24);
			if (!selectable) context.drawItem(BARRIER, x, y);

		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			if (selectable) {
				callback.accept(item);
				close();
			}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
