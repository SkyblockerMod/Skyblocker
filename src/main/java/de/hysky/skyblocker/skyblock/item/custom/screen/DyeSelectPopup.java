package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.RepoDyeColors;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class DyeSelectPopup extends AbstractPopupScreen {

	private ScrollableLayout scrollableLayout;
	private StringWidget titleWidget;
	private Button closeButton;

	private int listWidth;

	private final BiConsumer<Button, Integer> updateStatic;
	private final AnimatedDyeConsumer updateAnimated;

	protected DyeSelectPopup(Screen backgroundScreen, BiConsumer<Button, Integer> updateStatic, AnimatedDyeConsumer updateAnimated) {
		super(Component.translatable("skyblocker.customization.armor.pickDye"), backgroundScreen);
		this.updateStatic = updateStatic;
		this.updateAnimated = updateAnimated;
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, scrollableLayout.getX(), scrollableLayout.getY(), scrollableLayout.getWidth(), scrollableLayout.getHeight());
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		// large GUI scales make designing screens annoying :(
		scrollableLayout.setMaxHeight(Math.min(300, (int) (height * 0.68)));
		scrollableLayout.arrangeElements();
		scrollableLayout.setPosition((width - scrollableLayout.getWidth()) / 2, (height - scrollableLayout.getHeight()) / 2);
		listWidth = scrollableLayout.getWidth();

		closeButton.setPosition((width - closeButton.getWidth()) / 2, scrollableLayout.getY() + scrollableLayout.getHeight() + 20);
		titleWidget.setPosition((width - titleWidget.getWidth()) / 2, scrollableLayout.getY() - 30);
	}

	@Override
	protected void init() {
		LinearLayout layout = LinearLayout.vertical();

		layout.addChild(new HeaderWidget(Component.translatable("skyblocker.customization.armor.pickDye.dyes", Component.translatable("skyblocker.customization.armor.pickDye.dyes.static"))));
		RepoDyeColors.STATIC_DYES.forEach((name, hex) ->
				layout.addChild(new StaticDyeButton(
						name, hex, (button) -> this.selectStaticDye(button, hex)
				))
		);
		layout.addChild(new HeaderWidget(Component.empty())); // spacer
		layout.addChild(new HeaderWidget(Component.translatable("skyblocker.customization.armor.pickDye.dyes", Component.translatable("skyblocker.customization.armor.pickDye.dyes.animated"))));
		RepoDyeColors.ANIMATED_DYES.forEach((name, colors) -> {
			if (name.startsWith("FAIRY")) return;
			layout.addChild(new AnimatedDyeColor(
					name, colors, (button) -> this.selectAnimatedDye(button, colors)
			));
		});

		scrollableLayout = new ScrollableLayout(minecraft, layout, 0);
		scrollableLayout.visitWidgets(this::addRenderableWidget);

		titleWidget = new StringWidget(Component.translatable("skyblocker.customization.armor.pickDye.title"), font);
		closeButton = Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).width(75).build();
		addRenderableWidget(titleWidget);
		addRenderableWidget(closeButton);

		super.init();
	}

	private void selectStaticDye(Button button, int dyeColor) {
		this.onClose();
		this.updateStatic.accept(button, dyeColor);
	}

	private void selectAnimatedDye(Button button, List<Integer> colors) {
		boolean cycleBack = colors.size() % 2 == 0;
		int max = cycleBack ? colors.size() / 2 : colors.size();
		List<CustomArmorAnimatedDyes.Keyframe> keyFrames = new ArrayList<>(colors.size());

		int i = 0;
		for (int color : colors) {
			keyFrames.add(new CustomArmorAnimatedDyes.Keyframe(color, (float) i / max));
			i += 1;
			if (cycleBack && i >= max) break;
		}

		this.onClose();
		this.updateAnimated.accept(button, keyFrames, cycleBack);
	}


	private static class StaticDyeButton extends Button.Plain {
		private static final int TEXT_OFFSET = 10;

		String name;
		final ItemStack dyeStack;

		public StaticDyeButton(String dyeId, int color, OnPress onPress) {
			super(0, 0, 150, 20, Component.empty(), onPress, supplier -> Component.empty());
			name = dyeId;
			NEUItem item = NEURepoManager.getItemByNeuId(dyeId);
			if (item != null) name = ChatFormatting.stripFormatting(item.getDisplayName());
			dyeStack = ItemRepository.getItemStack(dyeId, Items.BARRIER.getDefaultInstance());
			Component component = Component.literal(name).withColor(color);
			this.setMessage(component);
		}

		@Override
		protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
			this.renderDefaultSprite(guiGraphics);
			guiGraphics.renderItem(dyeStack, this.getX() + TEXT_OFFSET, this.getY() + 1);
			renderName(guiGraphics, delta);
		}

		protected void renderName(GuiGraphics guiGraphics, float f) {
			// not jank at all
			this.setX(getX() + TEXT_OFFSET);
			this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
			this.setX(getX() - TEXT_OFFSET);
		}
	}

	private static class AnimatedDyeColor extends StaticDyeButton {
		List<Component> animatedNames = new ArrayList<>();
		int index = 0;
		float lastChange = 0;

		protected AnimatedDyeColor(String dyeId, List<Integer> colors, OnPress onPress) {
			super(dyeId, colors.getFirst(), onPress);
			for (int color : colors) {
				animatedNames.add(Component.literal(name).withColor(color));
			}
		}

		@Override
		protected void renderName(GuiGraphics guiGraphics, float delta) {
			lastChange += delta;
			if (lastChange > 2) {
				lastChange = 0;
				index = Math.min(index + 1, animatedNames.size() - 1);
				setMessage(animatedNames.get(index));
				if (index == animatedNames.size() - 1) index = 0;
			}
			super.renderName(guiGraphics, delta);
		}
	}

	private class HeaderWidget extends AbstractWidget {
		private static final int BACKGROUND_TEXTURE_OFFSET = 23;
		final Component message;

		public HeaderWidget(Component component) {
			super(0, 0, 0, 15, component);
			this.message = component;
			this.width = font.width(message);
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
			guiGraphics.drawString(font, message, getX() - BACKGROUND_TEXTURE_OFFSET + (listWidth - this.width + BACKGROUND_TEXTURE_OFFSET) / 2,
					getY(), CommonColors.WHITE);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
			return false;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
	}

	public interface AnimatedDyeConsumer {
		void accept(Button button, List<CustomArmorAnimatedDyes.Keyframe> keyframes, boolean cycleBack);
	}
}
