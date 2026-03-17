package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.RepoDyeColors;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.OkLabColor;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class DyeSelectPopup extends AbstractPopupScreen {

	private ScrollableLayout scrollableLayout;
	private StringWidget titleWidget;
	private Button closeButton;

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

		closeButton.setPosition((width - closeButton.getWidth()) / 2, scrollableLayout.getY() + scrollableLayout.getHeight() + 20);
		titleWidget.setPosition((width - titleWidget.getWidth()) / 2, scrollableLayout.getY() - 30);
	}

	@Override
	protected void init() {
		LinearLayout layout = LinearLayout.vertical();
		layout.defaultCellSetting().alignHorizontallyCenter();

		LayoutSettings headerLayout = layout.defaultCellSetting().copy().paddingBottom(6);
		layout.addChild(new StringWidget(Component.translatable("skyblocker.customization.armor.pickDye.dyes", Component.translatable("skyblocker.customization.armor.pickDye.dyes.static")), font), headerLayout);
		RepoDyeColors.STATIC_DYES.forEach((name, hex) ->
				layout.addChild(new StaticDyeButton(
						name, hex, (button) -> this.selectStaticDye(button, hex)
				))
		);
		layout.addChild(SpacerElement.height(15));
		layout.addChild(new StringWidget(Component.translatable("skyblocker.customization.armor.pickDye.dyes", Component.translatable("skyblocker.customization.armor.pickDye.dyes.animated")), font), headerLayout);
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

		// Leave the previous method as an option in case the optimized isn't faithful enough
		// Not gonna mention it though, I don't think most people are gonna notice
		if (Minecraft.getInstance().hasShiftDown()) {
			int i = 0;
			for (int color : colors) {
				keyFrames.add(new CustomArmorAnimatedDyes.Keyframe(color, (float) i / max));
				i += 1;
				if (cycleBack && i >= max) break;
			}
		} else {
			// Optimize the keyframes by looking at the direction of the gradient in the OkLab color space
			keyFrames.add(new CustomArmorAnimatedDyes.Keyframe(colors.getFirst(), 0));
			Vec3 next = OkLabColor.linearSRGB2OkLab(
							ARGB.redFloat(colors.get(1)),
							ARGB.greenFloat(colors.get(1)),
							ARGB.blueFloat(colors.get(1)))
					.toVec3();
			Vec3 current = OkLabColor.linearSRGB2OkLab(
							ARGB.redFloat(colors.getFirst()),
							ARGB.greenFloat(colors.getFirst()),
							ARGB.blueFloat(colors.getFirst()))
					.toVec3();
			Vec3 direction = next.subtract(current).normalize();
			//double speed = current.distanceToSqr(next);
			for (int i = 1; i < max - 1; i++) {
				current = next;
				next = OkLabColor.linearSRGB2OkLab(
						ARGB.redFloat(colors.get(i + 1)),
						ARGB.greenFloat(colors.get(i + 1)),
						ARGB.blueFloat(colors.get(i + 1))).toVec3();
				Vec3 newDirection = next.subtract(current).normalize();
				// Didn't really work that well, commenting out for now
				//double newSpeed = current.distanceToSqr(next);
				//double speedRatio = speed == 0 ? Double.MAX_VALUE : (newSpeed / speed);
				if (newDirection.dot(direction) < 0.75 /*|| speedRatio < 0.5 || speedRatio > 2*/) {
					keyFrames.add(new CustomArmorAnimatedDyes.Keyframe(colors.get(i), (float) i / max));
					direction = newDirection;
					//speed = newSpeed;
				}
			}
			keyFrames.add(new CustomArmorAnimatedDyes.Keyframe(colors.get(max - 1), 1));
		}

		this.onClose();
		this.updateAnimated.accept(button, keyFrames, cycleBack);
	}


	private static class StaticDyeButton extends Button.Plain {
		private static final int TEXT_OFFSET = 10;

		String name;
		final ItemStack dyeStack;

		protected StaticDyeButton(String dyeId, int color, OnPress onPress) {
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
			guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(
					getMessage(), getX() + TEXT_MARGIN + TEXT_OFFSET + 16,
					getRight() - TEXT_MARGIN, getY() + TEXT_MARGIN, getBottom() - TEXT_MARGIN
			);
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

	public interface AnimatedDyeConsumer {
		void accept(Button button, List<CustomArmorAnimatedDyes.Keyframe> keyframes, boolean cycleBack);
	}
}
