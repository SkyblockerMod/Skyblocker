package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.demonwav.mcdev.annotations.Translatable;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.mixins.accessors.EntityRenderManagerAccessor;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.io.Closeable;
import java.util.List;
import java.util.stream.Stream;

public class ColorSelectionWidget extends ContainerWidget implements Closeable {
	private static final int PADDING = 3;

	private static final Identifier INNER_SPACE_TEXTURE = SkyblockerMod.id("menu_inner_space");
	private static final Text RESET_COLOR_TEXT = Text.translatable("skyblocker.customization.armor.resetColor");
	private static final Text CANNOT_CUSTOMIZE_COLOR_TEXT = Text.translatable("skyblocker.customization.armor.cannotCustomizeColor");
	private static final Text ANIMATED_TEXT = Text.translatable("skyblocker.customization.armor.animated");
	private static final Text CYCLE_BACK_TEXT = Text.translatable("skyblocker.customization.armor.cycleBack");
	private static final Text DURATION_TOOLTIP_TEXT = Text.translatable("skyblocker.customization.armor.durationTooltip");
	private static final Text DELAY_TOOLTIP_TEXT = Text.translatable("skyblocker.customization.armor.delayTooltip");
	private static final String DURATION_TEXT = "skyblocker.customization.armor.duration";
	private static final String DELAY_TEXT = "skyblocker.customization.armor.delay";

	private final ColorPickerWidget colorPicker;
	private final ARGBTextInput argbTextInput;

	private final AnimatedDyeTimelineWidget timelineWidget;
	private final CheckboxWidget cycleBackCheckbox;
	private final Slider delaySlider;
	private final Slider durationSlider;

	private final ButtonWidget resetColorButton;
	private final CheckboxWidget animatedCheckbox;
	private final TextWidget notCustomizableText;

	private final SimplePositioningWidget layout;

	private ItemStack currentItem;
	private boolean animated;
	private boolean customizable = false;

	private final List<ClickableWidget> children;

	public ColorSelectionWidget(int x, int y, int width, int height, TextRenderer textRenderer) {
		super(x, y, width, height, Text.of("ColorSelectionWidget"));
		int height1 = Math.min(Math.min(2 * height / 3, width / 5), height - 40); // 40 is the height of slider + timeline + some padding/margin

		colorPicker = new ColorPickerWidget(0, 0, height1 * 2, height1);
		colorPicker.setOnColorChange(this::onPickerColorChanged);
		argbTextInput = new ARGBTextInput(0, 0, textRenderer, true);
		argbTextInput.setOnChange(this::onTextInputColorChanged);
		timelineWidget = new AnimatedDyeTimelineWidget(0, 0, getWidth() - 6, 15, this::onTimelineFrameSelected);

		resetColorButton = ButtonWidget.builder(RESET_COLOR_TEXT, this::onRemoveCustomColor).width(Math.min(150, x + width - argbTextInput.getRight() - 5)).build();

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
		delaySlider = new Slider(0, 0, sliderWidth, 0.0f, 2.0f, 0.02f, true, DELAY_TEXT, f -> {
			String itemUuid = currentItem.getUuid();
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.keyframes(),
					dye.cycleBack(),
					f,
					dye.duration()
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		});
		delaySlider.setTooltip(Tooltip.of(DELAY_TOOLTIP_TEXT));

		durationSlider = new Slider(0, 0, sliderWidth, 0.1f, 10.0f, 0.1f, true, DURATION_TEXT, f -> {
			String itemUuid = currentItem.getUuid();
			CustomArmorAnimatedDyes.AnimatedDye dye = SkyblockerConfigManager.get().general.customAnimatedDyes.get(itemUuid);
			CustomArmorAnimatedDyes.AnimatedDye newDye = new CustomArmorAnimatedDyes.AnimatedDye(
					dye.keyframes(),
					dye.cycleBack(),
					dye.delay(),
					f
			);
			SkyblockerConfigManager.get().general.customAnimatedDyes.put(itemUuid, newDye);
		});
		durationSlider.setTooltip(Tooltip.of(DURATION_TOOLTIP_TEXT));

		children = List.of(colorPicker, argbTextInput, timelineWidget, resetColorButton, animatedCheckbox, notCustomizableText, cycleBackCheckbox, delaySlider, durationSlider);
		int w = getWidth() - PADDING * 2;
		int h = getHeight() - PADDING * 2;
		layout = new SimplePositioningWidget(w, h);
		layout.add(timelineWidget, Positioner::alignBottom);

		GridWidget grid = new GridWidget().setSpacing(3);
		grid.add(argbTextInput, 0, 1);
		grid.add(resetColorButton, 0, 2, 1, 3, Positioner::alignRight);
		grid.add(animatedCheckbox, 1, 1, 1, 2);
		grid.add(delaySlider, 1, 3, 1, 2, Positioner::alignRight);
		grid.add(cycleBackCheckbox, 2, 1, 1, 2);
		grid.add(durationSlider, 2, 3, 1, 2, Positioner::alignRight);
		grid.add(colorPicker, 0, 0, 3, 1);
		layout.add(grid, Positioner::alignTop);
		layout.add(notCustomizableText);
		updateWidgetDimensions();
	}

	private void updateWidgetDimensions() {
		int w = getWidth() - PADDING * 2;
		int h = getHeight() - PADDING * 2;
		timelineWidget.setWidth(w);
		colorPicker.setHeight(Math.min(h - timelineWidget.getHeight() - 5, w / 3 / 2));
		colorPicker.setWidth(colorPicker.getHeight() * 2);
		delaySlider.setWidth((int) (w * 0.35f));
		durationSlider.setWidth((int) (w * 0.35f));
		layout.refreshPositions();
		layout.setPosition(getX() + PADDING, getY() + PADDING);
		width = layout.getWidth() + PADDING * 2;
		height = layout.getHeight() + PADDING * 2;
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
	public void setWidth(int width) {
		super.setWidth(width);
		updateWidgetDimensions();
	}

	public AnimatedDyeTimelineWidget getTimelineWidget() {
		return timelineWidget;
	}

	private void onPickerColorChanged(int argb, boolean release) {
		argbTextInput.setARGBColor(argb);
		if (!animated) {
			SkyblockerConfigManager.get().general.customDyeColors.put(currentItem.getUuid(), ColorHelper.fullAlpha(argb));
		} else if (release) {
			timelineWidget.setColor(argb);
		}
	}

	private void onTextInputColorChanged(int argb) {
		colorPicker.setARGBColor(argb);
		if (animated) timelineWidget.setColor(argb);
		else SkyblockerConfigManager.get().general.customDyeColors.put(currentItem.getUuid(), ColorHelper.fullAlpha(argb));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent() || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private void onTimelineFrameSelected(int color, float time) {
		argbTextInput.setARGBColor(color);
		colorPicker.setARGBColor(color);
	}

	private void onRemoveCustomColor(ButtonWidget button) {
		animated = false;
		((CheckboxWidgetAccessor) animatedCheckbox).setChecked(false);
		changeVisibilities();

		String itemUuid = currentItem.getUuid();
		SkyblockerConfigManager.get().general.customDyeColors.removeInt(itemUuid);
		SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);

		int color = DyedColorComponent.getColor(currentItem, -1);
		argbTextInput.setARGBColor(color);
		colorPicker.setARGBColor(color);
	}

	private void onAnimatedCheckbox(CheckboxWidget checkbox, boolean checked) {
		animated = checked;
		changeVisibilities();
		String itemUuid = currentItem.getUuid();
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
			colorPicker.setARGBColor(color);
			argbTextInput.setARGBColor(color);
			SkyblockerConfigManager.get().general.customAnimatedDyes.remove(itemUuid);
		}
	}

	private void onCycleBackCheckbox(CheckboxWidget checkbox, boolean checked) {
		String itemUuid = currentItem.getUuid();
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
		argbTextInput.visible = customizable;

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
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, INNER_SPACE_TEXTURE, getX(), getY(), getWidth(), getHeight());
		for (ClickableWidget child : children) {
			child.render(context, mouseX, mouseY, delta);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (!super.mouseClicked(click, doubled)) {
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
		refresh();
	}

	public void refresh() {
		String itemUuid = currentItem.getUuid();
		RegistryKey<EquipmentAsset> key = null;
		if (SkyblockerConfigManager.get().general.customArmorModel.containsKey(itemUuid)) {
			key = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, SkyblockerConfigManager.get().general.customArmorModel.get(itemUuid));
		} else if (currentItem.contains(DataComponentTypes.EQUIPPABLE)) {
			EquippableComponent component = currentItem.get(DataComponentTypes.EQUIPPABLE);
			key = component.assetId().orElse(null);
		}
		if (key == null) customizable = false;
		else {
			EquipmentModel model = ((EntityRenderManagerAccessor) MinecraftClient.getInstance().getEntityRenderDispatcher()).getEquipmentModelLoader().get(key);
			customizable = Stream.of(EquipmentModel.LayerType.HUMANOID, EquipmentModel.LayerType.HUMANOID_LEGGINGS, EquipmentModel.LayerType.WINGS)
					.flatMap(l -> model.getLayers(l).stream())
					.anyMatch(layer -> layer.dyeable().isPresent());
		}
		if (!customizable) {
			animated = false;
			((CheckboxWidgetAccessor) animatedCheckbox).setChecked(false);
			changeVisibilities();
			return;
		}
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
			argbTextInput.setARGBColor(color);
			colorPicker.setARGBColor(color);
		} else {
			animated = false;
			int color = DyedColorComponent.getColor(currentItem, -1);
			argbTextInput.setARGBColor(color);
			colorPicker.setARGBColor(color);
		}
		changeVisibilities();
		((CheckboxWidgetAccessor) animatedCheckbox).setChecked(animated);
	}

	private static class Slider extends SliderWidget {
		private final float minValue;
		private final float maxValue;
		private final float step;
		private final boolean linear;
		private final String translatable;
		private final FloatConsumer onValueChanged;

		private boolean clicked = false;

		private Slider(int x, int y, int width, float min, float max, float step, boolean linear, @Translatable String translatable, FloatConsumer onValueChanged) {
			super(x, y, width, 15, Text.empty(), 0);
			if (min >= max || step <= 0 || step > (max - min)) throw new IllegalArgumentException("Invalid slider parameters: min=" + min + ", max=" + max + ", step=" + step);
			this.minValue = min;
			this.maxValue = max;
			this.step = step;
			this.linear = linear; // old code stuff... is always true, keeping it, can maybe be useful
			this.translatable = translatable;
			this.onValueChanged = onValueChanged;
			updateMessage();
		}

		private float trueValue() {
			double v = linear ? value : value * value;
			return roundToStep(v * (maxValue - minValue));
		}

		@Override
		protected void updateMessage() {
			setMessage(Text.translatable(translatable, Formatters.DOUBLE_NUMBERS.format(trueValue())));
		}

		private void setValue(float val) {
			float v = (val - minValue) / (maxValue - minValue);
			value = linear ? v : Math.sqrt(v);
			updateMessage();
		}

		@Override
		public void onClick(Click click, boolean doubled) {
			super.onClick(click, doubled);
			clicked = true;
		}

		@Override
		public void onRelease(Click click) {
			super.onRelease(click);
			if (clicked) {
				onValueChanged.accept(trueValue());
				clicked = false;
			}
		}

		@Override
		public boolean keyPressed(KeyInput input) {
			if (super.keyPressed(input)) {
				onValueChanged.accept(trueValue());
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (verticalAmount == 0) return false;
			float offset = verticalAmount > 0 ? step : -step;
			setValue(Math.clamp(trueValue() + offset, minValue, maxValue));
			onValueChanged.accept(trueValue());
			return true;
		}

		// Not using this cuz it updates every drag
		@Override
		protected void applyValue() {}

		private float roundToStep(double value) {
			return Math.clamp(minValue + step * Math.round(value / step), minValue, maxValue);
		}
	}
}
