package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;

import static de.hysky.skyblocker.skyblock.item.custom.screen.CustomizeScreen.CLIENT;

public class ArmorTab extends GridScreenTab implements Closeable {
	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");
	private static final int PLAYER_WIDGET_WIDTH = 84;
	private static final int PADDING = 10;

	private static final EquipmentSlot[] ARMOR_SLOTS = EquipmentSlot.VALUES.stream().filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new);
	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);

	private final ItemStack[] armor = new ItemStack[4];
	private final CustomizeScreen parent;
	private int selectedSlot = 0;
	private TrimSelectionWidget trimSelectionWidget;
	private ColorSelectionWidget colorSelectionWidget;
	private HeadSelectionWidget headSelectionWidget;
	private ModelFieldContainer modelFieldContainer;

	private final boolean nothingCustomizable;
	private final OtherClientPlayerEntity player = new OtherClientPlayerEntity(CLIENT.world, CLIENT.getGameProfile()) {
		@Override
		public boolean isInvisibleTo(PlayerEntity player) {
			return true;
		}

		@Override
		public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {}
	};

	public ArmorTab(CustomizeScreen parent) {
		super(Text.literal("Armor"));
		this.parent = parent;
		grid.setRowSpacing(PADDING / 2).setColumnSpacing(PADDING);

		List<ItemStack> list = ItemUtils.getArmor(CLIENT.player);
		for (int i = 0; i < list.size(); i++) {
			ItemStack copy = list.get(i).copy();
			armor[3 - i] = copy;
			player.equipStack(ARMOR_SLOTS[i], copy);
		}
		while (selectedSlot < armor.length - 1 && !canEdit(armor[selectedSlot])) selectedSlot++;
		nothingCustomizable = !canEdit(armor[selectedSlot]);

		DirectionalLayoutWidget vertical = DirectionalLayoutWidget.vertical().spacing(1);
		PlayerWidget playerWidget = new PlayerWidget(0, 0, 84, 165, player);
		vertical.add(playerWidget);
		PieceSelectionWidget pieceSelectionWidget = new PieceSelectionWidget(0, 0);
		vertical.add(pieceSelectionWidget);
		grid.add(vertical, 0, 0, 2, 1, Positioner::alignVerticalCenter);


		if (!nothingCustomizable) {
			int width = 200;
			headSelectionWidget = new HeadSelectionWidget(0, 0, width, 165);
			grid.add(headSelectionWidget, 0, 1, 2, 1, Positioner::alignVerticalCenter);

			DirectionalLayoutWidget layoutWidget = DirectionalLayoutWidget.horizontal().spacing(PADDING / 2);
			int containerWidth = (int) (width * (1/3f));
			trimSelectionWidget = new TrimSelectionWidget(0, 0, width - containerWidth - PADDING / 2, 80);
			modelFieldContainer = layoutWidget.add(new ModelFieldContainer(containerWidth, 80));
			layoutWidget.add(trimSelectionWidget);
			layoutWidget.refreshPositions();
			grid.add(layoutWidget, 0, 1);

			colorSelectionWidget = new ColorSelectionWidget(0, 0, width, 100, MinecraftClient.getInstance().textRenderer);
			grid.add(colorSelectionWidget, 1, 1);

			updateWidgets();
		}
	}

	private static boolean canEdit(ItemStack stack) {
		boolean hasUuid = !stack.getUuid().isEmpty();
		if (stack.isOf(Items.PLAYER_HEAD)) return hasUuid;
		return stack.isIn(ItemTags.TRIMMABLE_ARMOR) && hasUuid;
	}

	private void updateWidgets() {
		if (nothingCustomizable) return;
		ItemStack item = armor[selectedSlot];
		parent.backupConfigs(item);
		boolean isPlayerHead = item.isOf(Items.PLAYER_HEAD);
		headSelectionWidget.setCurrentItem(item);
		trimSelectionWidget.setCurrentItem(item);
		colorSelectionWidget.setCurrentItem(item);
		headSelectionWidget.visible = isPlayerHead;
		trimSelectionWidget.visible = !isPlayerHead;
		colorSelectionWidget.visible = !isPlayerHead;
		modelFieldContainer.visible = !isPlayerHead;
		String uuid = item.getUuid();
		if (SkyblockerConfigManager.get().general.customArmorModel.containsKey(uuid)) {
			Identifier identifier = SkyblockerConfigManager.get().general.customArmorModel.get(uuid);
			String string = identifier.toString();
			modelFieldContainer.field.setText(string);
		} else {
			modelFieldContainer.field.setText("");
		}
	}

	void tick() {
		player.age++;
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		int width = Math.min(460, tabArea.width()) - PLAYER_WIDGET_WIDTH - PADDING * 3;
		headSelectionWidget.setWidth(width);
		int modelFieldWidth = (int) (width * (1/3f));
		trimSelectionWidget.setWidth(width - modelFieldWidth - PADDING / 2);
		modelFieldContainer.setWidth(modelFieldWidth);
		colorSelectionWidget.setWidth(width);
		super.refreshGrid(tabArea);
	}

	public void recreate() {
		if (colorSelectionWidget != null) colorSelectionWidget.getTimelineWidget().recreateImage();
	}

	@Override
	public void close() {
		colorSelectionWidget.close();
	}

	private class PieceSelectionWidget extends ClickableWidget {

		private static final Identifier HOTBAR_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "armor_customization_screen/mini_hotbar");
		private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "hotbar_selection_full");

		private final boolean[] selectable;

		private PieceSelectionWidget(int x, int y) {
			super(x, y, 84, 24, Text.of(""));
			selectable = new boolean[armor.length];
			for (int i = 0; i < armor.length; i++) {
				selectable[i] = canEdit(armor[i]);
			}
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, getX() + 1, getY() + 1, 82, 22);

			int hoveredSlot = -1;
			int localX = mouseX - getX() - 2;
			int localY = mouseY - getY() - 2;
			if (localY >= 0 && localY < 20) {
				hoveredSlot = localX / 20 >= armor.length ? -1 : localX / 20;
			}

			if (hoveredSlot >= 0 && selectable[hoveredSlot]) {
				int i = getX() + 2 + hoveredSlot * 20;
				context.fill(i, getY() + 2, i + 20, getY() + 22, 0x20_FF_FF_FF);
			}

			for (int i = 0; i < armor.length; i++) {
				context.drawItem(armor[i], getX() + 4 + i * 20, getY() + 4);
				if (!selectable[i]) {
					context.drawItem(BARRIER, getX() + 4 + i * 20, getY() + 4);
				}
			}
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_TEXTURE, getX() + selectedSlot * 20, getY(), 24, 24);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			double localX = mouseX - getX() - 2;
			double localY = mouseY - getY() - 2;
			if (localY < 0 || localY >= 20) return;
			int i = (int) (localX / 20);
			if (i < 0 || i >= armor.length || !selectable[i]) return;
			if (i != selectedSlot) {
				selectedSlot = i;
				updateWidgets();
			}
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return this.active && this.visible && mouseX >= this.getX() + 2 && mouseY >= this.getY() + 2 && mouseX < this.getRight() - 2 && mouseY < this.getBottom() - 2;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private class ModelFieldContainer extends ContainerWidget {
		private final Text TEXT = Text.translatable("skyblocker.customization.armor.modelOverride").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
		private final SimplePositioningWidget containerLayout;
		private final IdentifierTextField field;

		private ModelFieldContainer(int width, int height) {
			super(0, 0, width, height, Text.empty());
			containerLayout = new SimplePositioningWidget();
			field = containerLayout.add(new IdentifierTextField(width - 10, 20, identifier -> {
				String uuid = armor[selectedSlot].getUuid();
				if (uuid.isEmpty()) return;
				if (identifier == null) SkyblockerConfigManager.get().general.customArmorModel.remove(uuid);
				else SkyblockerConfigManager.get().general.customArmorModel.put(uuid, identifier);
				colorSelectionWidget.refresh();
			}));
			containerLayout.refreshPositions();
			field.setTooltip(Tooltip.of(Text.translatable("skyblocker.customization.armor.modelOverride.tooltip")));
			field.setTooltipDelay(Duration.ofMillis(400));
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			SimplePositioningWidget.setPos(getX(), getWidth(), containerLayout.getWidth(), containerLayout::setX, 0.5f);
		}

		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			containerLayout.setMinWidth(width);
			containerLayout.refreshPositions();
			field.setWidth(width - 10);
			setX(getX());
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			SimplePositioningWidget.setPos(getY(), getHeight(), containerLayout.getHeight(), containerLayout::setY, 0.5f);
		}

		@Override
		public List<? extends Element> children() {
			return visible ? List.of(field) : List.of();
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return 0;
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 0;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			if (!visible) return;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
					INNER_SPACE_TEXTURE,
					getX(),
					getY(),
					getWidth(),
					getHeight()
			);
			int padding = 5;
			int startY = getY() + padding;
			drawScrollableText(context, CLIENT.textRenderer, TEXT, getX() + padding, startY, getRight() - padding, startY + 9, -1);
			field.render(context, mouseX, mouseY, deltaTicks);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}
	}
}
