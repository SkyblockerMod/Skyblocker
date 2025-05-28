package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.render.gui.RGBTextInput;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ColorSelectionWidget extends ContainerWidget implements Closeable {

	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");
	private static final Text RESET_COLOR_TEXT = Text.translatable("skyblocker.armorCustomization.resetColor");
	private static final Text CANNOT_CUSTOMIZE_COLOR_TEXT = Text.translatable("skyblocker.armorCustomization.cannotCustomizeColor");
	private static final Text ANIMATED_TEXT = Text.translatable("skyblocker.armorCustomization.animated");
	private static final Text CYCLE_BACK_TEXT = Text.translatable("skyblocker.armorCustomization.cycleBack");
	private static final Text DURATION_TOOLTIP_TEXT = Text.translatable("skyblocker.armorCustomization.durationTooltip");
	private static final Text DELAY_TOOLTIP_TEXT = Text.translatable("skyblocker.armorCustomization.delayTooltip");
	private static final String DURATION_TEXT = "skyblocker.armorCustomization.duration";
	private static final String DELAY_TEXT = "skyblocker.armorCustomization.delay";

	private final ColorPickerWidget colorPicker;
	private final RGBTextInput rgbTextInput;

	private final AnimatedDyeTimelineWidget timelineWidget;
	private final CheckboxWidget cycleBackCheckbox;
	private final Slider delaySlider;
	private final Slider durationSlider;

	private final ButtonWidget resetColorButton;
	private final CheckboxWidget animatedCheckbox;
	private final TextWidget notCustomizableText;

	private ItemStack currentItem;
	private boolean animated;
	private boolean customizable = false;

	private final List<ClickableWidget> children;

	public ColorSelectionWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
		super(x, y, width, height, Text.of("ColorSelectionWidget"));
		int height1 = Math.min(Math.min(2 * height / 3, width / 5), height - 40); // 40 is the height of slider + timeline + some padding/margin

		colorPicker = new ColorPickerWidget(x + 3, y + 3, height1 * 2, height1);
		colorPicker.setOnColorChange(this::onPickerColorChanged);
		rgbTextInput = new RGBTextInput(0, y + 3, textRenderer, true);
		rgbTextInput.setX(colorPicker.getRight() + 5);
		rgbTextInput.setOnChange(this::onTextInputColorChanged);
		timelineWidget = new AnimatedDyeTimelineWidget(getX() + 3, getBottom() - 18, getWidth() - 6, 15, this::onTimelineFrameSelected);

		resetColorButton = ButtonWidget.builder(RESET_COLOR_TEXT, this::onRemoveCustomColor).width(Math.min(150, x + width - rgbTextInput.getRight() - 5)).build();

		resetColorButton.setPosition(getRight() - resetColorButton.getWidth() - 3, getY() + 3);

		notCustomizableText = new TextWidget(CANNOT_CUSTOMIZE_COLOR_TEXT, textRenderer);
		SimplePositioningWidget.setPos(notCustomizableText, getX(), getY(), getWidth(), getHeight());

		animatedCheckbox = CheckboxWidget.builder(ANIMATED_TEXT, textRenderer)
				.pos(colorPicker.getRight() + 5, resetColorButton.getBottom())
				.maxWidth(80)
				.callback(this::onAnimatedCheckbox)
				.build();
		cycleBackCheckbox = CheckboxWidget.builder(CYCLE_BACK_TEXT, textRenderer)
				.pos(colorPicker.getRight() + 5, animatedCheckbox.getBottom() + 3)
				.maxWidth(80)
				.callback(this::onCycleBackCheckbox)
				.build();

		int sliderWidth = (int) (width * 0.35f);
		boolean vertical = getRight() - sliderWidth - 3 > Math.max(animatedCheckbox.getRight(), cycleBackCheckbox.getRight());
		int sliderY = vertical ? resetColorButton.getBottom() + 3: timelineWidget.getY() - 17;
		delaySlider = new Slider(getRight() - sliderWidth - 3, sliderY, sliderWidth, 0.0f, 2.0f, f -> {
			String itemUuid = ItemUtils.getItemUuid(currentItem);
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.keyframes(),
					dye.cycleBack(),
					f,
					dye.duration()
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		}, DELAY_TEXT, true);
		delaySlider.setTooltip(Tooltip.of(DELAY_TOOLTIP_TEXT));

		int durationX;
		int durationY;
		if (vertical) {
			durationX = delaySlider.getX();
			durationY = delaySlider.getBottom() + 3;
		} else {
			durationX = delaySlider.getX() - sliderWidth - 3;
			durationY = delaySlider.getY();
		}
		durationSlider = new Slider(durationX, durationY, sliderWidth, 0.1f, 10.0f, f -> {
			String itemUuid = ItemUtils.getItemUuid(currentItem);
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.keyframes(),
					dye.cycleBack(),
					dye.delay(),
					f
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		}, DURATION_TEXT, true);
		durationSlider.setTooltip(Tooltip.of(DURATION_TOOLTIP_TEXT));


		children = ImmutableList.<ClickableWidget>builder().add(colorPicker, rgbTextInput, timelineWidget, resetColorButton, animatedCheckbox, notCustomizableText, cycleBackCheckbox, delaySlider, durationSlider).build();
	}

	private void onPickerColorChanged(int argb, boolean release) {
		rgbTextInput.setRGBColor(argb);
		if (!animated) {
			SkyblockerConfigManager.get().general.customDyeColors.put(ItemUtils.getItemUuid(currentItem), ColorHelper.fullAlpha(argb));
		} else if (release) {
			timelineWidget.setColor(argb);
		}
	}

	private void onTextInputColorChanged(int argb) {
		colorPicker.setRGBColor(argb);
		if (animated) timelineWidget.setColor(argb);
		else SkyblockerConfigManager.get().general.customDyeColors.put(ItemUtils.getItemUuid(currentItem), ColorHelper.fullAlpha(argb));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent() || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void onTimelineFrameSelected(int color, float time) {
		rgbTextInput.setRGBColor(color);
		colorPicker.setRGBColor(color);
	}

	private void onRemoveCustomColor(ButtonWidget button) {
		animated = false;
		((CheckboxWidgetAccessor)animatedCheckbox).setChecked(false);
		changeVisibilities();

		String itemUuid = ItemUtils.getItemUuid(currentItem);
		SkyblockerConfigManager.get().general.customDyeColors.removeInt(itemUuid);
		SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);

		int color = DyedColorComponent.getColor(currentItem, -1);
		rgbTextInput.setRGBColor(color);
		colorPicker.setRGBColor(color);
	}

	private void onAnimatedCheckbox(CheckboxWidget checkbox, boolean checked) {
		animated = checked;
		changeVisibilities();
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		if (animated) {
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, new CustomArmorAnimatedDyes.AnimatedDye(
					List.of(new CustomArmorAnimatedDyes.Keyframe(Colors.RED, 0), new CustomArmorAnimatedDyes.Keyframe(Colors.BLUE, 1)),
					true,
					0,
					1.f
			));
			timelineWidget.setAnimatedDye(itemUuid);
			delaySlider.setValue(0);
			durationSlider.setValue(1);
			((CheckboxWidgetAccessor) cycleBackCheckbox).setChecked(true);
		} else {
			int color = SkyblockerConfigManager.get().general.customDyeColors.getOrDefault(itemUuid, DyedColorComponent.getColor(currentItem, -1));
			colorPicker.setRGBColor(color);
			rgbTextInput.setRGBColor(color);
			SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);
		}
	}

	private void onCycleBackCheckbox(CheckboxWidget checkbox, boolean checked) {
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
		CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
				dye.keyframes(),
				checked,
				dye.delay(),
				dye.duration()
		);
		SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
	}

	private void changeVisibilities() {
		colorPicker.visible = customizable;
		rgbTextInput.visible = customizable;

		timelineWidget.visible = customizable && animated;
		cycleBackCheckbox.visible = customizable && animated;
		delaySlider.visible = customizable && animated;
		durationSlider.visible = customizable && animated;

		resetColorButton.visible = customizable;
		animatedCheckbox.visible = customizable;
		notCustomizableText.visible = !customizable;
	}

	@Override
	public List<? extends Element> children() {
		return children;
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
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
		for (ClickableWidget child : children) {
			child.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!super.mouseClicked(mouseX, mouseY, button)) {
			setFocused(null);
			return false;
		}
		return true;
	}

	@Override
	public void close() {
		timelineWidget.close();
	}

	public void setCurrentItem(@NotNull ItemStack currentItem) {
		this.currentItem = currentItem;
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		customizable = currentItem.isIn(ItemTags.DYEABLE);
		if (!customizable) return;
		if (SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(itemUuid)) {
			animated = true;
			CustomArmorAnimatedDyes.AnimatedDye animatedDye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			((CheckboxWidgetAccessor) cycleBackCheckbox).setChecked(animatedDye.cycleBack());
			delaySlider.setValue(animatedDye.delay());
			durationSlider.setValue(animatedDye.duration());
			timelineWidget.setAnimatedDye(itemUuid);
		} else if (SkyblockerConfigManager.get().general.customDyeColors.containsKey(itemUuid)) {
			animated = false;
			int color = SkyblockerConfigManager.get().general.customDyeColors.getInt(itemUuid);
			rgbTextInput.setRGBColor(color);
			colorPicker.setRGBColor(color);
		} else {
			animated = false;
			int color = DyedColorComponent.getColor(currentItem, -1);
			rgbTextInput.setRGBColor(color);
			colorPicker.setRGBColor(color);
		}
		changeVisibilities();
		((CheckboxWidgetAccessor) animatedCheckbox).setChecked(animated);
	}

	private static class Slider extends SliderWidget {
		private static final NumberFormat FORMATTER = Util.make(NumberFormat.getInstance(Locale.US), nf -> nf.setMaximumFractionDigits(3));

		private final FloatConsumer onValueChanged;
		private final float minValue;
		private final float maxValue;
		private final String translatable;
		private final boolean linear;

		private boolean clicked = false;

		public Slider(int x, int y, int width, float min, float max, FloatConsumer onValueChanged, @Translatable String translatable, boolean linear) {
			super(x, y, width, 15, Text.empty(), 0);
			this.onValueChanged = onValueChanged;
			this.minValue = min;
			this.maxValue = max;
			this.translatable = translatable;
			this.linear = linear; // old code stuff... is always true, keeping it, can maybe be useful
			updateMessage();
		}

		private float trueValue() {
			double v = linear ? value : value*value;
			return (float) (minValue + v * (maxValue - minValue));
		}

		@Override
		protected void updateMessage() {
			setMessage(Text.translatable(translatable, FORMATTER.format(trueValue())));
		}

		private void setValue(float val) {
			float v = (val - minValue) / (maxValue - minValue);
			value = linear ? v : Math.sqrt(v);
			updateMessage();
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			super.onClick(mouseX, mouseY);
			clicked = true;
		}

		@Override
		public void onRelease(double mouseX, double mouseY) {
			super.onRelease(mouseX, mouseY);
			if (clicked) {
				onValueChanged.accept(trueValue());
				clicked = false;
			}
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (super.keyPressed(keyCode, scanCode, modifiers)) {
				onValueChanged.accept(trueValue());
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {if (verticalAmount == 0) return false;
			float offset = verticalAmount > 0 ? 0.001f : -0.001f;
			setValue(Math.clamp(trueValue() + offset, minValue, maxValue));
			onValueChanged.accept(trueValue());
			return true;
		}

		// Not using this cuz it updates every drag
		@Override
		protected void applyValue() {}
	}
}
