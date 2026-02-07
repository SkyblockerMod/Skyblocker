package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static de.hysky.skyblocker.skyblock.item.custom.screen.CustomizeScreen.CLIENT;

public class ArmorTab extends GridLayoutTab implements Closeable {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final int PLAYER_WIDGET_WIDTH = 84;
	private static final int PADDING = 10;

	private static final EquipmentSlot[] ARMOR_SLOTS = EquipmentSlot.VALUES.stream().filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new);
	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);

	private final ItemStack[] armor = new ItemStack[4];
	private final CustomizeScreen parent;
	private int selectedSlot = 0;
	private final TrimSelectionWidget trimSelectionWidget;
	private final ColorSelectionWidget colorSelectionWidget;
	private final HeadSelectionWidget headSelectionWidget;
	private final ModelFieldContainer modelFieldContainer;

	private final boolean nothingCustomizable;
	private final RemotePlayer player = new RemotePlayer(CLIENT.level, CLIENT.getGameProfile()) {
		@Override
		public boolean isInvisibleTo(Player player) {
			return true;
		}

		@Override
		public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {}
	};

	public ArmorTab(CustomizeScreen parent) {
		super(Component.translatable("skyblocker.customization.armor"));
		this.parent = parent;
		layout.rowSpacing(PADDING / 2).columnSpacing(PADDING);

		List<ItemStack> list = ItemUtils.getArmor(CLIENT.player);
		for (int i = 0; i < list.size(); i++) {
			ItemStack copy = list.get(i).copy();
			armor[3 - i] = copy;
			player.setItemSlot(ARMOR_SLOTS[i], copy);
		}
		while (selectedSlot < armor.length - 1 && !canEdit(armor[selectedSlot])) selectedSlot++;
		nothingCustomizable = !canEdit(armor[selectedSlot]);

		LinearLayout vertical = LinearLayout.vertical().spacing(1);
		PlayerWidget playerWidget = new PlayerWidget(0, 0, 84, 165, player);
		vertical.addChild(playerWidget);
		PieceSelectionWidget pieceSelectionWidget = new PieceSelectionWidget(0, 0);
		vertical.addChild(pieceSelectionWidget);
		layout.addChild(vertical, 0, 0, 2, 1, LayoutSettings::alignVerticallyMiddle);

		int width = 200;
		headSelectionWidget = new HeadSelectionWidget(0, 0, width, 165);
		layout.addChild(headSelectionWidget, 0, 1, 2, 1, LayoutSettings::alignVerticallyMiddle);

		LinearLayout layoutWidget = LinearLayout.horizontal().spacing(PADDING / 2);
		int containerWidth = (int) (width * (1f / 3f));
		trimSelectionWidget = new TrimSelectionWidget(0, 0, width - containerWidth - PADDING / 2, 80);
		modelFieldContainer = layoutWidget.addChild(new ModelFieldContainer(containerWidth, 80));
		layoutWidget.addChild(trimSelectionWidget);
		layoutWidget.arrangeElements();
		layout.addChild(layoutWidget, 0, 1);

		Font textRenderer = Minecraft.getInstance().font;
		colorSelectionWidget = new ColorSelectionWidget(0, 0, width, 100, textRenderer);
		layout.addChild(colorSelectionWidget, 1, 1);

		if (nothingCustomizable) {
			layout.addChild(new StringWidget(Component.translatable("skyblocker.customization.nothingCustomizable"), textRenderer), 0, 1, 2, 1, p -> p.alignVerticallyMiddle().alignHorizontallyCenter());
		}

		updateWidgets();

	}

	private static boolean canEdit(ItemStack stack) {
		boolean hasUuid = !stack.getUuid().isEmpty();
		if (stack.is(Items.PLAYER_HEAD)) return hasUuid;
		return stack.is(ItemTags.TRIMMABLE_ARMOR) && hasUuid;
	}

	private void updateWidgets() {
		if (nothingCustomizable) {
			headSelectionWidget.visible = false;
			trimSelectionWidget.visible = false;
			colorSelectionWidget.visible = false;
			modelFieldContainer.visible = false;
			return;
		}
		ItemStack item = armor[selectedSlot];
		parent.backupConfigs(item);
		boolean isPlayerHead = item.is(Items.PLAYER_HEAD);
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
			modelFieldContainer.field.setValue(string);
		} else {
			modelFieldContainer.field.setValue("");
		}
	}

	void tick() {
		player.tickCount++;
	}

	@Override
	public void doLayout(ScreenRectangle tabArea) {
		int width = Math.min(460, tabArea.width()) - PLAYER_WIDGET_WIDTH - PADDING * 3;
		headSelectionWidget.setWidth(width);
		int modelFieldWidth = (int) (width * (1 / 3f));
		trimSelectionWidget.setWidth(width - modelFieldWidth - PADDING / 2);
		modelFieldContainer.setWidth(modelFieldWidth);
		colorSelectionWidget.setWidth(width);
		super.doLayout(tabArea);
	}

	public void recreate() {
		if (colorSelectionWidget != null) colorSelectionWidget.getTimelineWidget().recreateImage();
	}

	@Override
	public void close() {
		colorSelectionWidget.close();
	}

	private class PieceSelectionWidget extends AbstractWidget {
		private static final Identifier HOTBAR_TEXTURE = SkyblockerMod.id("armor_customization_screen/mini_hotbar");
		private static final Identifier HOTBAR_SELECTION_TEXTURE = SkyblockerMod.id("hotbar_selection_full");

		private final boolean[] selectable;

		private PieceSelectionWidget(int x, int y) {
			super(x, y, 84, 24, Component.nullToEmpty(""));
			selectable = new boolean[armor.length];
			for (int i = 0; i < armor.length; i++) {
				selectable[i] = canEdit(armor[i]);
			}
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, getX() + 1, getY() + 1, 82, 22);

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
				context.renderItem(armor[i], getX() + 4 + i * 20, getY() + 4);
				if (!selectable[i] && !armor[i].isEmpty()) {
					context.renderItem(BARRIER, getX() + 4 + i * 20, getY() + 4);
				}
			}
			context.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_TEXTURE, getX() + selectedSlot * 20, getY(), 24, 24);
			this.handleCursor(context);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			double localX = click.x() - getX() - 2;
			double localY = click.y() - getY() - 2;
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
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	private class ModelFieldContainer extends AbstractContainerWidget {
		private final Component text = Component.translatable("skyblocker.customization.armor.modelOverride").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
		private final FrameLayout containerLayout;
		private final IdentifierTextField field;

		private ModelFieldContainer(int width, int height) {
			super(0, 0, width, height, Component.empty());
			containerLayout = new FrameLayout();
			field = containerLayout.addChild(new IdentifierTextField(width - 10, 20, identifier -> {
				String uuid = armor[selectedSlot].getUuid();
				if (uuid.isEmpty()) return;
				SkyblockerConfigManager.update(config -> {
					if (identifier == null) config.general.customArmorModel.remove(uuid);
					else config.general.customArmorModel.put(uuid, identifier);
				});
				colorSelectionWidget.refresh();
			}));
			containerLayout.arrangeElements();
			field.setTooltip(Tooltip.create(Component.translatable("skyblocker.customization.armor.modelOverride.tooltip")));
			field.setTooltipDelay(Duration.ofMillis(400));
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			FrameLayout.alignInDimension(getX(), getWidth(), containerLayout.getWidth(), containerLayout::setX, 0.5f);
		}

		@Override
		public void setWidth(int width) {
			super.setWidth(width);
			containerLayout.setMinWidth(width);
			containerLayout.arrangeElements();
			field.setWidth(width - 10);
			setX(getX());
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			FrameLayout.alignInDimension(getY(), getHeight(), containerLayout.getHeight(), containerLayout::setY, 0.5f);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return visible ? List.of(field) : List.of();
		}

		@Override
		protected int contentHeight() {
			return 0;
		}

		@Override
		protected double scrollRate() {
			return 0;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			if (!visible) return;
			context.blitSprite(RenderPipelines.GUI_TEXTURED,
					INNER_SPACE_TEXTURE,
					getX(),
					getY(),
					getWidth(),
					getHeight()
			);
			this.field.render(context, mouseX, mouseY, deltaTicks);
			this.drawLabel(context.textRenderer(HoveredTextEffects.NONE));
		}

		private void drawLabel(ActiveTextCollector drawer) {
			int padding = 5;
			int startY = getY() + padding;
			drawer.acceptScrollingWithDefaultCenter(text, getX() + padding, getRight() - padding, startY, startY + 9);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}
}
