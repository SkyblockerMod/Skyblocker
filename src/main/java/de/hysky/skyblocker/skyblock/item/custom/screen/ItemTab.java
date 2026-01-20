package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.CustomizeNameWidget;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class ItemTab extends GridLayoutTab {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");

	private final CustomizeScreen parentScreen;
	private final CustomizeNameWidget nameWidget;
	private final Button glintButton;
	private final IdentifierTextField modelField;

	private ItemStack currentItem = ItemStack.EMPTY;
	private TriState glintState = TriState.DEFAULT;

	public ItemTab(CustomizeScreen parentScreen) {
		super(Component.translatable("skyblocker.customization.item"));
		layout.spacing(5);
		this.parentScreen = parentScreen;
		glintButton = Button.builder(Component.empty(), b -> {
			TriState[] states = TriState.values();
			glintState = states[(glintState.ordinal() + 1) % states.length];
			b.setMessage(getGlintText());
			String uuid = currentItem.getUuid();
			Object2BooleanMap<String> customGlint = SkyblockerConfigManager.get().general.customGlint;
			switch (glintState) {
				case DEFAULT -> customGlint.removeBoolean(uuid);
				case TRUE -> customGlint.put(uuid, true);
				case FALSE -> customGlint.put(uuid, false);
			}
		}).width(Button.SMALL_WIDTH).build();
		modelField = new IdentifierTextField(120, 20, identifier -> {
			String uuid = currentItem.getUuid();
			if (uuid.isEmpty()) return;
			if (identifier == null) SkyblockerConfigManager.get().general.customItemModel.remove(uuid);
			else SkyblockerConfigManager.get().general.customItemModel.put(uuid, identifier);
		});
		modelField.setHint(Component.translatable("skyblocker.customization.item.modelOverride").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
		nameWidget = new CustomizeNameWidget(parentScreen);

		layout.addChild(new ItemSelector(), 0, 0, 2, 1);
		layout.addChild(new BackgroundRenderer(), 0, 1);
		layout.addChild(glintButton, 0, 1, p -> p.alignHorizontallyRight().paddingRight(3).alignVerticallyTop().paddingTop(3));
		layout.addChild(modelField, 1, 1, p -> p.alignHorizontallyRight().paddingRight(3).alignVerticallyBottom().paddingBottom(3));
		layout.addChild(nameWidget, 2, 0, 1, 2);

		LocalPlayer player = Minecraft.getInstance().player;
		ItemStack handStack = player.getMainHandItem();
		if (!handStack.getUuid().isEmpty()) {
			setCurrentItem(handStack);
			return;
		}
		for (ItemStack stack : (Utils.isInTheRift() ? SkyblockInventoryScreen.equipment_rift : SkyblockInventoryScreen.equipment)) {
			if (!stack.getUuid().isEmpty()) {
				setCurrentItem(stack);
				return;
			}
		}
		for (ItemStack stack : player.getInventory()) {
			if (!stack.getUuid().isEmpty()) {
				setCurrentItem(stack);
				return;
			}
		}
		visitChildren(clickableWidget -> clickableWidget.visible = false);
		layout.addChild(new StringWidget(Component.translatable("skyblocker.customization.nothingCustomizable"), Minecraft.getInstance().font),
				0, 0, 3, 2, p -> p.alignHorizontallyCenter().alignVerticallyMiddle());
	}

	private void setCurrentItem(ItemStack itemStack) {
		this.currentItem = itemStack;
		String uuid = currentItem.getUuid();
		boolean empty = uuid.isEmpty();
		visitChildren(clickableWidget -> clickableWidget.visible = !empty);
		if (empty) return;
		parentScreen.backupConfigs(itemStack);
		nameWidget.setItem(itemStack);
		if (SkyblockerConfigManager.get().general.customItemModel.containsKey(uuid)) {
			Identifier identifier = SkyblockerConfigManager.get().general.customItemModel.get(uuid);
			String string = identifier.toString();
			modelField.setValue(string);
		} else {
			modelField.setValue("");
		}

		// glint
		Object2BooleanMap<String> customGlint = SkyblockerConfigManager.get().general.customGlint;
		String itemUuid = itemStack.getUuid();
		if (customGlint.containsKey(itemUuid)) {
			glintState = customGlint.getBoolean(itemUuid) ? TriState.TRUE : TriState.FALSE;
		} else {
			glintState = TriState.DEFAULT;
		}
		glintButton.setMessage(getGlintText());
	}

	private Component getGlintText() {
		return Component.translatable("skyblocker.customization.item.glint", switch (glintState) {
			case DEFAULT -> Component.translatable("skyblocker.customization.item.glint.default");
			case TRUE -> CommonComponents.OPTION_ON;
			case FALSE -> CommonComponents.OPTION_OFF;
		});
	}

	private class ItemSelector extends AbstractContainerWidget {
		private static final int PADDING = 3;

		private final Button selectItemButton;
		private final LinearLayout layout = LinearLayout.vertical().spacing(5);

		private ItemSelector() {
			super(0, 20, 0, 0, Component.literal("Item Selector"));
			layout.addChild(SpacerElement.height(32)); // ITEM
			selectItemButton = layout.addChild(Button.builder(Component.literal("Select Item"), b ->
					Minecraft.getInstance().setScreen(new ItemSelectPopup(parentScreen, ItemTab.this::setCurrentItem))
			).width(Button.SMALL_WIDTH).build());
			layout.arrangeElements();
			layout.setPosition(PADDING, PADDING);
			setSize(layout.getWidth() + PADDING * 2, layout.getHeight() + PADDING * 2);
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			layout.setX(getX() + PADDING);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			layout.setY(getY() + PADDING);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(selectItemButton);
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
			context.blitSprite(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
			Matrix3x2fStack matrices = context.pose();
			matrices.pushMatrix();
			float x = layout.getX() + layout.getWidth() / 2f - 16;
			int y = layout.getY();
			if (mouseX >= x && mouseX < x + 32 && mouseY >= y && mouseY < y + 32) {
				context.setTooltipForNextFrame(currentItem.getHoverName(), mouseX, mouseY);
			}
			matrices.translate(x, y);
			matrices.scale(2);
			context.renderItem(currentItem, 0, 0);
			matrices.popMatrix();
			selectItemButton.render(context, mouseX, mouseY, deltaTicks);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}
	}

	private class BackgroundRenderer extends AbstractWidget {

		BackgroundRenderer() {
			super(0, 0, 0, 0, Component.empty());
			active = false;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			int x = glintButton.getX() - 3;
			int y = glintButton.getY() - 3;
			context.blitSprite(RenderPipelines.GUI_TEXTURED,
					INNER_SPACE_TEXTURE,
					x,
					y,
					modelField.getRight() + 3 - x,
					modelField.getBottom() + 3 - y
			);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}
}
