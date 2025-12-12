package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.SkyblockInventoryScreen;
import de.hysky.skyblocker.skyblock.item.custom.screen.name.CustomizeNameWidget;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class ItemTab extends GridScreenTab {
	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");

	private final CustomizeScreen parentScreen;
	private final CustomizeNameWidget nameWidget;
	private final ButtonWidget glintButton;
	private final IdentifierTextField modelField;

	private ItemStack currentItem = ItemStack.EMPTY;
	private TriState glintState = TriState.DEFAULT;

	public ItemTab(CustomizeScreen parentScreen) {
		super(Text.literal("Item"));
		grid.setSpacing(5);
		this.parentScreen = parentScreen;
		glintButton = ButtonWidget.builder(Text.empty(), b -> {
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
		}).width(ButtonWidget.DEFAULT_WIDTH_SMALL).build();
		modelField = new IdentifierTextField(120, 20, identifier -> {
			String uuid = currentItem.getUuid();
			if (uuid.isEmpty()) return;
			if (identifier == null) SkyblockerConfigManager.get().general.customItemModel.remove(uuid);
			else SkyblockerConfigManager.get().general.customItemModel.put(uuid, identifier);
		});
		modelField.setPlaceholder(Text.translatable("skyblocker.customization.item.modelOverride").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
		nameWidget = new CustomizeNameWidget(parentScreen);

		grid.add(new ItemSelector(), 0, 0, 2, 1);
		grid.add(new BackgroundRenderer(), 0, 1);
		grid.add(glintButton, 0, 1, p -> p.alignRight().marginRight(3).alignTop().marginTop(3));
		grid.add(modelField, 1, 1, p -> p.alignRight().marginRight(3).alignBottom().marginBottom(3));
		grid.add(nameWidget, 2, 0, 1, 2);

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		ItemStack handStack = player.getMainHandStack();
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
		forEachChild(clickableWidget -> clickableWidget.visible = false);
		grid.add(new TextWidget(Text.translatable("skyblocker.customization.nothingCustomizable"), MinecraftClient.getInstance().textRenderer),
				0, 0, 3, 2, p -> p.alignHorizontalCenter().alignVerticalCenter());
	}

	private void setCurrentItem(ItemStack itemStack) {
		this.currentItem = itemStack;
		String uuid = currentItem.getUuid();
		boolean empty = uuid.isEmpty();
		forEachChild(clickableWidget -> clickableWidget.visible = !empty);
		if (empty) return;
		parentScreen.backupConfigs(itemStack);
		nameWidget.setItem(itemStack);
		if (SkyblockerConfigManager.get().general.customItemModel.containsKey(uuid)) {
			Identifier identifier = SkyblockerConfigManager.get().general.customItemModel.get(uuid);
			String string = identifier.toString();
			modelField.setText(string);
		} else {
			modelField.setText("");
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

	private Text getGlintText() {
		return Text.translatable("skyblocker.customization.item.glint", switch (glintState) {
			case DEFAULT -> Text.translatable("skyblocker.customization.item.glint.default");
			case TRUE -> ScreenTexts.ON;
			case FALSE -> ScreenTexts.OFF;
		});
	}

	private class ItemSelector extends ContainerWidget {
		private static final int PADDING = 3;

		private final ButtonWidget selectItemButton;
		private final DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical().spacing(5);

		private ItemSelector() {
			super(0, 20, 0, 0, Text.literal("Item Selector"));
			layout.add(EmptyWidget.ofHeight(32)); // ITEM
			selectItemButton = layout.add(ButtonWidget.builder(Text.literal("Select Item"), b ->
					MinecraftClient.getInstance().setScreen(new ItemSelectPopup(parentScreen, ItemTab.this::setCurrentItem))
			).width(ButtonWidget.DEFAULT_WIDTH_SMALL).build());
			layout.refreshPositions();
			layout.setPosition(PADDING, PADDING);
			setDimensions(layout.getWidth() + PADDING * 2, layout.getHeight() + PADDING * 2);
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
		public List<? extends Element> children() {
			return List.of(selectItemButton);
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
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
			Matrix3x2fStack matrices = context.getMatrices();
			matrices.pushMatrix();
			float x = layout.getX() + layout.getWidth() / 2f - 16;
			int y = layout.getY();
			if (mouseX >= x && mouseX < x + 32 && mouseY >= y && mouseY < y + 32) {
				context.drawTooltip(currentItem.getName(), mouseX, mouseY);
			}
			matrices.translate(x, y);
			matrices.scale(2);
			context.drawItem(currentItem, 0, 0);
			matrices.popMatrix();
			selectItemButton.render(context, mouseX, mouseY, deltaTicks);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}
	}

	private class BackgroundRenderer extends ClickableWidget {

		BackgroundRenderer() {
			super(0, 0, 0, 0, Text.empty());
			active = false;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			int x = glintButton.getX() - 3;
			int y = glintButton.getY() - 3;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
					INNER_SPACE_TEXTURE,
					x,
					y,
					modelField.getRight() + 3 - x,
					modelField.getBottom() + 3 - y
			);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
