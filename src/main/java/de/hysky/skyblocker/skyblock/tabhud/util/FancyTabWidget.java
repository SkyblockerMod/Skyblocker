package de.hysky.skyblocker.skyblock.tabhud.util;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.config.option.EnumOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.FloatOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.CenteredWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.TopAlignedWidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@RegisterWidget
public class FancyTabWidget extends HudWidget {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final String ID = "fancy_tab";

	private final List<HudWidget> widgets = new ObjectArrayList<>();
	private Positioner positioner = Positioner.CENTERED;
	private float maxHeight = 0.8f;

	private String @NotNull [] hiddenWidgets = new String[0];

	public FancyTabWidget() {
		super(new Information(ID, Text.literal("Fancy Tab")));
		PlayerListManager.registerTabListener(this::update);
	}

	@Override
	protected void renderWidget(DrawContext context, float delta) {
		updatePositions();
		for (HudWidget widget : widgets) {
			widget.render(context, delta);
		}
		ScreenBuilder.markPositionsDirty(); // since make fucked with positioning
	}

	private void update() {
		widgets.clear();
		for (String s : PlayerListManager.WIDGET_MAP.keySet()) {
			HudWidget hudWidget = PlayerListManager.HANDLED_TAB_WIDGETS.get(s);
			// this really should not happen but it somehow did and I don't know why.
			if (hudWidget == null) {
				LOGGER.warn("Couldn't find HudWidget for {}", s);
				continue;
			}
			if (ArrayUtils.contains(hiddenWidgets, hudWidget.getId())) continue;
			widgets.add(hudWidget);
		}
	}

	@Override
	protected void renderWidgetConfig(DrawContext context, float delta) {
		updatePositions();
		for (HudWidget widget : widgets) {
			widget.renderConfig(context, delta);
		}
		ScreenBuilder.markPositionsDirty();
	}

	private void updatePositions() {
		// TODO global scale option
		MinecraftClient client = MinecraftClient.getInstance();
		WidgetPositioner widgetPositioner = positioner.getNewPositioner(maxHeight, client.getWindow().getScaledHeight());
		widgets.forEach(widgetPositioner::positionWidget);
		Vector2i widthAndHeight = widgetPositioner.finalizePositioning();
		w = widthAndHeight.x;
		h = widthAndHeight.y;
	}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		// TODO translatable
		options.add(new EnumOption<>(Positioner.class, "positioner", Text.literal("Positioner"), () -> positioner, v -> positioner = v, Positioner.CENTERED));
		options.add(new FloatOption("max_height", Text.literal("Max Height"), () -> maxHeight, v -> maxHeight = v, 0.8f));
	}

	@Override
	public void getPerScreenOptions(List<WidgetOption<?>> options) {
		super.getPerScreenOptions(options);
		options.add(new HiddenWidgetsOption());
	}

	public enum Positioner implements StringIdentifiable {
		TOP(TopAlignedWidgetPositioner::new),
		CENTERED(CenteredWidgetPositioner::new);

		private final BiFunction<Float, Integer, WidgetPositioner> function;

		Positioner(BiFunction<Float, Integer, WidgetPositioner> widgetPositionerSupplier) {
			function = widgetPositionerSupplier;
		}

		public WidgetPositioner getNewPositioner(float maxHeight, int screenHeight) {
			return function.apply(maxHeight, screenHeight);
		}

		@Override
		public String asString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	private class HiddenWidgetsOption implements WidgetOption<String[]> {

		@Override
		public String @NotNull [] getValue() {
			return hiddenWidgets;
		}

		@Override
		public void setValue(String @NotNull [] value) {
			hiddenWidgets = value;
		}

		@Override
		public String getId() {
			return "hidden_widgets";
		}

		@Override
		public @NotNull JsonElement toJson() {
			return Codec.STRING.listOf().encodeStart(JsonOps.INSTANCE, List.of(hiddenWidgets)).getOrThrow();
		}

		@Override
		public void fromJson(@NotNull JsonElement json) {
			hiddenWidgets = Codec.STRING.listOf().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst().toArray(String[]::new);
		}

		@Override
		public @NotNull ClickableWidget createNewWidget(WidgetConfig config) {
			return ButtonWidget.builder(
					Text.literal("Edit shown widgets"),
					button -> config.openPopup(SelectWidgetsPopup::new)
					).build();
		}
	}

	private class SelectWidgetsPopup extends AbstractPopupScreen {

		private final Set<String> hiddenWidgetsMutable = new ObjectOpenHashSet<>(FancyTabWidget.this.hiddenWidgets);
		private GridWidget layout;

		protected SelectWidgetsPopup(Screen backgroundScreen) {
			super(Text.literal("Select widgets to hide"), backgroundScreen);
		}

		@Override
		protected void init() {
			layout = new GridWidget().setRowSpacing(2);
			GridWidget.Adder adder = layout.createAdder(2);
			adder.add(new TextWidget(Text.literal("Select widgets to hide."), textRenderer).alignCenter(), 2, layout.getMainPositioner().copy().alignHorizontalCenter());
			adder.add(new WidgetsList(200, 200, hiddenWidgetsMutable, hiddenWidgetsMutable::remove, hiddenWidgetsMutable::add), 2);
			adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> close()).width(ButtonWidget.DEFAULT_WIDTH_SMALL).build());
			adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
				hiddenWidgets = hiddenWidgetsMutable.toArray(String[]::new);
				update();
				close();
			}).width(ButtonWidget.DEFAULT_WIDTH_SMALL).build());
			layout.forEachChild(this::addDrawableChild);
			layout.refreshPositions();
			refreshWidgetPositions();
		}

		@Override
		public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
			super.renderBackground(context, mouseX, mouseY, delta);
			drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
		}

		@Override
		protected void refreshWidgetPositions() {
			layout.setX(width / 2 - layout.getWidth() / 2);
			layout.setY(height / 2 - layout.getHeight() / 2);
		}
	}

	private static class WidgetsList extends ElementListWidget<WidgetEntry> {

		private final int rowWidth;

		private WidgetsList(int width, int height, Collection<String> hidden, Consumer<String> unhide, Consumer<String> hide) {
			super(MinecraftClient.getInstance(), width, height, 0, 20);
			int tempRowWidth = 0;
			for (String s : PlayerListManager.WIDGET_MAP.keySet()) {
				HudWidget widget = PlayerListManager.HANDLED_TAB_WIDGETS.get(s);
				WidgetEntry entry = new WidgetEntry(widget.getInformation().displayName(), getWidth() - 15, hidden.contains(widget.getId()), (checkbox, checked) -> {
					if (checked) hide.accept(widget.getId());
					else unhide.accept(widget.getId());
				});
				addEntry(entry);
				tempRowWidth = Math.max(entry.checkbox.getWidth(), tempRowWidth);
			}
			rowWidth = tempRowWidth;
		}

		@Override
		public int getRowWidth() {
			return rowWidth;
		}

		@Override
		protected void drawMenuListBackground(DrawContext context) {}

		@Override
		protected void drawHeaderAndFooterSeparators(DrawContext context) {}
	}

	private static class WidgetEntry extends ElementListWidget.Entry<WidgetEntry> {

		private final CheckboxWidget checkbox;

		private WidgetEntry(Text name, int maxWidth, boolean checked, CheckboxWidget.Callback callback) {
			checkbox = CheckboxWidget.builder(name, MinecraftClient.getInstance().textRenderer)
					.callback(callback)
					.checked(checked)
					.maxWidth(maxWidth)
					.build();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(checkbox);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(checkbox);
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			checkbox.setPosition(x, y + (entryHeight - checkbox.getHeight()) / 2);
			checkbox.render(context, mouseX, mouseY, tickProgress);
		}
	}
}
