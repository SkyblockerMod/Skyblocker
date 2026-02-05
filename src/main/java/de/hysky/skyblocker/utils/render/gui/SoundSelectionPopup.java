package de.hysky.skyblocker.utils.render.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class SoundSelectionPopup extends AbstractPopupScreen {
	private static final Component YES_TEXT = CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN);
	private static final Component NO_TEXT = CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED);

	private final List<AbstractWidget> filteredWidgets = new ObjectArrayList<>();
	private final Consumer<@Nullable SoundEvent> onDone;
	private @Nullable SoundEvent selectedSound = null;
	private boolean advanced = false;

	private final GridLayout gridWidget = new GridLayout();
	private LinearLayout listLayout = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL);
	private @Nullable Button doneButton;
	private @Nullable ListContainer widgetsContainer;

	public SoundSelectionPopup(Screen backgroundScreen, Consumer<@Nullable SoundEvent> onDone) {
		super(Component.literal("Select Sound"), backgroundScreen);
		this.onDone = onDone;

	}

	@Override
	protected void init() {
		GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
		EditBox searchField = new EditBox(Minecraft.getInstance().font, 200, 20, Component.empty());
		searchField.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
		searchField.setResponder(this::filterSounds);
		addRenderableWidget(adder.addChild(searchField));

		CycleButton<Boolean> toggleAdvanced = CycleButton.booleanBuilder(YES_TEXT, NO_TEXT, advanced)
				.withTooltip(b -> Tooltip.create(Component.translatable("skyblocker.utils.render.gui.soundSelectionPopup.advanced.@Tooltip")))
				.create(0, 0, 100, 20, Component.translatable("skyblocker.utils.render.gui.soundSelectionPopup.advanced"), (button, value) -> {
					advanced = !advanced;
					filterSounds(searchField.getValue());
				});

		addRenderableWidget(adder.addChild(toggleAdvanced));

		widgetsContainer = new ListContainer(0, 0, 400, this.height / 2);

		addRenderableWidget(adder.addChild(widgetsContainer, 2));

		addRenderableWidget(adder.addChild(Button.builder(CommonComponents.GUI_CANCEL, b -> {
			onClose();
			onDone.accept(null);
		}).build()));
		doneButton = Button.builder(CommonComponents.GUI_DONE, b -> {
			onClose();
			onDone.accept(selectedSound);
		}).build();
		doneButton.active = false;
		addRenderableWidget(adder.addChild(doneButton));
		gridWidget.arrangeElements();
		repositionElements();
		filterSounds("");
	}

	private @Nullable Component getSoundName(SoundEvent sound) {
		String key = BuiltInRegistries.SOUND_EVENT.getKey(sound).toShortLanguageKey();
		//first check for translation
		Component translation = Component.translatableWithFallback("subtitles." + key, "null");
		if (!translation.getString().equals("null")) {
			return translation;
		}
		//convert note block sounds
		if (key.contains("note_block")) {
			String[] split = key.split("\\.");
			return Component.literal("note block " + split[split.length - 1].replace("_", " "));
		}
		if (advanced) {
			return Component.literal(key);
		} else {
			return null;
		}


	}

	private void filterSounds(String input) {
		filteredWidgets.clear();
		for (SoundEvent soundEvent : BuiltInRegistries.SOUND_EVENT) {
			Component translation = getSoundName(soundEvent);
			//filter sounds
			if (translation != null && translation.getString().toLowerCase(Locale.ENGLISH).contains(input.toLowerCase(Locale.ENGLISH))) {
				AbstractWidget widget = new SoundWidget(translation, soundEvent);
				filteredWidgets.add(widget);
			}
		}
		recreateList();
	}

	private void recreateList() {
		int listX = listLayout.getX();
		int listY = listLayout.getY();
		listLayout = new LinearLayout(0, 0, LinearLayout.Orientation.VERTICAL);
		filteredWidgets.forEach(listLayout::addChild);
		listLayout.arrangeElements();
		listLayout.setPosition(listX, listY);
		if (widgetsContainer != null) {
			widgetsContainer.refreshScrollAmount();
		}
	}

	@Override
	protected void repositionElements() {
		gridWidget.setPosition((width - gridWidget.getWidth()) / 2, (height - gridWidget.getHeight()) / 2);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, gridWidget.getX(), gridWidget.getY(), gridWidget.getWidth(), gridWidget.getHeight());
	}

	private class ListContainer extends AbstractContainerWidget {

		ListContainer(int i, int j, int k, int l) {
			super(i, j, k, l, Component.literal("List"));
		}

		@Override
		protected int contentHeight() {
			return listLayout.getHeight();
		}

		@Override
		protected double scrollRate() {
			return 15;
		}

		@Override
		public void setScrollAmount(double scrollY) {
			super.setScrollAmount(scrollY);
			listLayout.setY(getY() - (int) scrollAmount());
		}

		private boolean isVisible(AbstractWidget widget) {
			return widget.getBottom() >= getY() && widget.getY() < getBottom();
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			context.enableScissor(getX(), getY(), getRight(), getBottom());
			for (AbstractWidget widget : filteredWidgets) {
				if (isVisible(widget)) widget.render(context, mouseX, mouseY, deltaTicks);
			}
			renderScrollbar(context, mouseX, mouseY);
			context.disableScissor();
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			listLayout.setX(x);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			listLayout.setY(y);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

		}

		@Override
		public List<? extends GuiEventListener> children() {
			return filteredWidgets;
		}
	}

	private class SoundWidget extends StringWidget {
		SoundEvent sound;
		Component name;

		SoundWidget(Component name, SoundEvent sound) {
			super(name, font);
			this.name = name;
			this.sound = sound;
			this.active = true;
		}

		@Override
		public int getHeight() {
			return super.getHeight() + 10;
		}

		@Override
		public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			super.renderWidget(context, mouseX, mouseY, delta);
			if (selectedSound == sound) {
				context.fill(RenderPipelines.GUI, this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x3000FF00);
			} else if (this.isHovered) {
				context.fill(RenderPipelines.GUI, this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x20FFFFFF);
			}
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			selectedSound = sound;
			if (doneButton != null) doneButton.active = true;
			minecraft.getSoundManager().stop();
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
			super.onClick(click, doubled);
		}
	}
}
