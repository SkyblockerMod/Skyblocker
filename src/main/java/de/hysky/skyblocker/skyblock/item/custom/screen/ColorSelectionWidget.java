package de.hysky.skyblocker.skyblock.item.custom.screen;

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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ColorSelectionWidget extends ContainerWidget implements Closeable {

	private static final Identifier INNER_SPACE_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "menu_inner_space");
	private static final Text ADD_COLOR_TEXT = Text.of("Add custom color");
	private static final Text REMOVE_COLOR_TEXT = Text.of("Remove custom color");
	private final TextRenderer textRenderer;

	private final ColorPickerWidget colorPicker;
	private final RGBTextInput rgbTextInput;

	private final AnimatedDyeTimelineWidget timelineWidget;
	private final CheckboxWidget cycleBackCheckbox;
	private final Slider delaySlider;
	private final Slider speedSlider;

	private final ButtonWidget addCustomColorButton;
	private final ButtonWidget removeCustomColorButton;
	private final CheckboxWidget animatedCheckbox;
	private final TextWidget notCustomizableText;

	private ItemStack currentItem;
	private boolean animated;
	private State state = State.CANNOT_CUSTOMIZE;

	private final List<ClickableWidget> children;

	public ColorSelectionWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
		super(x, y, width, height, Text.of("ColorSelectionWidget"));
		int height1 = Math.min(2 * height / 3, width / 6);
		this.textRenderer = textRenderer;

		colorPicker = new ColorPickerWidget(x + 3, y + 3, height1 * 2, height1);
		colorPicker.setOnColorChange(this::onPickerColorChanged);
		rgbTextInput = new RGBTextInput(0, y + 3, textRenderer, true);
		rgbTextInput.setX(colorPicker.getRight() + 5);
		rgbTextInput.setOnChange(this::onTextInputColorChanged);
		timelineWidget = new AnimatedDyeTimelineWidget(getX() + 5, getBottom() - 20, getWidth() - 10, 15, this::onTimelineFrameSelected);

		addCustomColorButton = ButtonWidget.builder(ADD_COLOR_TEXT, this::onAddCustomColor).build();
		SimplePositioningWidget.setPos(addCustomColorButton, getX(), getY(), getWidth(), getHeight());
		removeCustomColorButton = ButtonWidget.builder(REMOVE_COLOR_TEXT, this::onRemoveCustomColor).width(Math.min(150, x + width - rgbTextInput.getRight() - 5)).build();

		removeCustomColorButton.setPosition(getRight() - removeCustomColorButton.getWidth() - 3, getY() + 3);

		notCustomizableText = new TextWidget(Text.literal("Cannot customize this piece's color :("), textRenderer);
		SimplePositioningWidget.setPos(notCustomizableText, getX(), getY(), getWidth(), getHeight());

		int x1 = removeCustomColorButton.getX();
		int width1 = removeCustomColorButton.getWidth();
		animatedCheckbox = CheckboxWidget.builder(Text.literal("Animated"), textRenderer)
				.pos(x1, removeCustomColorButton.getBottom() + 3)
				.maxWidth(width1 / 2)
				.callback(this::onAnimatedCheckbox)
				.build();
		cycleBackCheckbox = CheckboxWidget.builder(Text.literal("Cycle Back"), textRenderer)
				.pos(animatedCheckbox.getRight() + 1, animatedCheckbox.getY())
				.maxWidth(width1 / 2)
				.callback(this::onCycleBackCheckbox)
				.build();

		delaySlider = new Slider(x1, animatedCheckbox.getBottom() + 1, width1, 0.0f, 1.0f, f -> {
			String itemUuid = ItemUtils.getItemUuid(currentItem);
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.frames(),
					dye.cycleBack(),
					f,
					dye.speed()
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		}, Text.literal("Delay: "));
		delaySlider.setTooltip(Tooltip.of(Text.literal("The animation will be delayed by this amount in seconds.")));

		speedSlider = new Slider(x1, delaySlider.getBottom() + 1, width1, 0.01f, 2.0f, f -> {
			String itemUuid = ItemUtils.getItemUuid(currentItem);
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.frames(),
					dye.cycleBack(),
					dye.delay(),
					f
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		}, Text.literal("Speed: "));
		speedSlider.setTooltip(Tooltip.of(Text.literal("The speed of the animation in \"completions\" per second.")));


		children = ImmutableList.<ClickableWidget>builder().add(colorPicker, rgbTextInput, timelineWidget, addCustomColorButton, removeCustomColorButton, animatedCheckbox, notCustomizableText, cycleBackCheckbox, delaySlider, speedSlider).build();
	}

	private void onPickerColorChanged(int argb, boolean release) {
		rgbTextInput.setRGBColor(argb);
		if (release) timelineWidget.setColor(argb);
	}

	private void onTextInputColorChanged(int argb) {
		colorPicker.setRGBColor(argb);
		timelineWidget.setColor(argb);
	}

	private void onAddCustomColor(ButtonWidget button) {
		state = State.CUSTOMIZED;
		animated = false;
		changeVisibilities();
		SkyblockerConfigManager.get().general.customDyeColors.put(ItemUtils.getItemUuid(currentItem), -1);
		rgbTextInput.setRGBColor(-1);
		colorPicker.setRGBColor(-1);
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
		state = State.CUSTOMIZABLE;
		animated = false;
		changeVisibilities();
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		SkyblockerConfigManager.get().general.customDyeColors.removeInt(itemUuid);
		SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);
	}

	private void onAnimatedCheckbox(CheckboxWidget checkbox, boolean checked) {
		animated = checked;
		changeVisibilities();
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		if (animated) {
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, new CustomArmorAnimatedDyes.AnimatedDye(
					List.of(new CustomArmorAnimatedDyes.DyeFrame(Colors.RED, 0), new CustomArmorAnimatedDyes.DyeFrame(Colors.BLUE, 1)),
					true,
					0,
					0.6f
			));
			timelineWidget.setAnimatedDye(itemUuid);
		} else {
			SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);
		}
	}

	private void onCycleBackCheckbox(CheckboxWidget checkbox, boolean checked) {
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
		CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
				dye.frames(),
				checked,
				dye.delay(),
				dye.speed()
		);
		SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
	}

	private void changeVisibilities() {
		colorPicker.visible = state == State.CUSTOMIZED;
		rgbTextInput.visible = state == State.CUSTOMIZED;

		timelineWidget.visible = state == State.CUSTOMIZED && animated;
		cycleBackCheckbox.visible = state == State.CUSTOMIZED && animated;
		delaySlider.visible = state == State.CUSTOMIZED && animated;
		speedSlider.visible = state == State.CUSTOMIZED && animated;

		addCustomColorButton.visible = state == State.CUSTOMIZABLE;
		removeCustomColorButton.visible = state == State.CUSTOMIZED;
		animatedCheckbox.visible = state == State.CUSTOMIZED;
		notCustomizableText.visible = state == State.CANNOT_CUSTOMIZE;
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
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

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

	public void setCurrentItem(ItemStack currentItem) {
		this.currentItem = currentItem;
		String itemUuid = ItemUtils.getItemUuid(currentItem);
		if (!currentItem.isIn(ItemTags.DYEABLE)) {
			state = State.CANNOT_CUSTOMIZE;
			animated = false;
		} else if (SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(itemUuid)) {
			state = State.CUSTOMIZED;
			animated = true;
			CustomArmorAnimatedDyes.AnimatedDye animatedDye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			((CheckboxWidgetAccessor) cycleBackCheckbox).setChecked(animatedDye.cycleBack());
			delaySlider.setVal(animatedDye.delay());
			speedSlider.setVal(animatedDye.speed());
		} else if (SkyblockerConfigManager.get().general.customDyeColors.containsKey(itemUuid)) {
			state = State.CUSTOMIZED;
			animated = false;
		} else {
			state = State.CUSTOMIZABLE;
			animated = false;
		}
		changeVisibilities();
		((CheckboxWidgetAccessor) animatedCheckbox).setChecked(animated);
		if (animated) timelineWidget.setAnimatedDye(itemUuid);
	}

	private enum State {
		CANNOT_CUSTOMIZE,
		CUSTOMIZABLE,
		CUSTOMIZED
	}

	private static class Slider extends SliderWidget {
		private static final NumberFormat FORMATTER = Util.make(NumberFormat.getInstance(Locale.US), nf -> nf.setMaximumFractionDigits(3));

		private final FloatConsumer onValueChanged;
		private final float minValue;
		private final float maxValue;
		private final Text prefix;

		private boolean clicked = false;

		public Slider(int x, int y, int width, float min, float max, FloatConsumer onValueChanged, Text prefix) {
			super(x, y, width, 15, Text.empty(), min);
			this.onValueChanged = onValueChanged;
			this.minValue = min;
			this.maxValue = max;
			this.prefix = prefix;
			updateMessage();
		}

		private float trueValue() {
			return (float) (minValue + value*value * (maxValue - minValue));
		}

		@Override
		protected void updateMessage() {
			setMessage(prefix.copy().append(Text.literal(FORMATTER.format(trueValue()))));
		}

		private void setVal(float val) {
			value = Math.sqrt((val - minValue) / (maxValue - minValue));
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
			setVal(Math.clamp(trueValue() + offset, minValue, maxValue));
			onValueChanged.accept(trueValue());
			return true;
		}

		// Not using this cuz it updates every drag
		@Override
		protected void applyValue() {}
	}
}
