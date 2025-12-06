package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.Positioner;
import net.minecraft.client.gui.widget.ScrollableLayoutWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ScreenConfigPopup extends AbstractPopupScreen {

	private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
	private final ScreenBuilder builder;
	private final boolean tab;
	private CheckboxWidget checkbox;
	private ButtonWidget doneButton;

	protected ScreenConfigPopup(Screen backgroundScreen, ScreenBuilder builder, boolean isTab) {
		super(Text.empty(), backgroundScreen);
		this.builder = builder;
		this.tab = isTab;
	}

	private void updateLayout() {
		clearChildren();
		layout = DirectionalLayoutWidget.vertical().spacing(2);
		if (tab) {
			layout.add(checkbox);

			ScreenBuilder.FancyTabConfig fancyTab = builder.getConfig().fancyTab;
			if (checkbox.isChecked() && fancyTab != null) { // shouldn't be null if it's checked but SQUIGGLY LINE

				// TODO Translatable
				layout.add(ButtonWidget.builder(Text.literal("Positioner: " + fancyTab.positioner), button -> {
					fancyTab.positioner = EnumUtils.cycle(fancyTab.positioner);
					button.setMessage(Text.literal("Positioner: " + fancyTab.positioner));
				}).build());

				layout.add(new TextWidget(Text.literal("Hidden Widgets"), textRenderer), Positioner::alignHorizontalCenter);
				DirectionalLayoutWidget checkboxes = DirectionalLayoutWidget.vertical().spacing(1);
				for (String s : PlayerListManager.getCurrentWidgets()) {
					HudWidget widget = PlayerListManager.getTabWidget(s);
					if (widget == null) continue;
					Text displayName = widget.getInformation().displayName();
					CheckboxWidget checkboxWidget = checkboxes.add(CheckboxWidget.builder(displayName, textRenderer)
							.callback((box, checked) -> {
								if (checked) fancyTab.hiddenWidgets.add(widget.getId());
								else fancyTab.hiddenWidgets.remove(widget.getId());
							})
							.build());
					((CheckboxWidgetAccessor) checkboxWidget).setChecked(fancyTab.hiddenWidgets.contains(widget.getId()));
				}
				ScrollableLayoutWidget widget = layout.add(new ScrollableLayoutWidget(client, checkboxes, 130));
				widget.setHeight(130);
				widget.setWidth(150);
			}
		} else layout.add(EmptyWidget.ofHeight(120));
		layout.add(doneButton);
		layout.forEachChild(this::addDrawableChild);
		refreshWidgetPositions();
	}

	@Override
	public void init() {
		if (tab) {
			checkbox = CheckboxWidget.builder(Text.literal("Enable fancy tab"), textRenderer)
					.callback((box, checked) -> {
						builder.getConfig().getOrCreateFancyTab().enabled = checked;
						updateLayout();
					})
					.build();
			ScreenBuilder.FancyTabConfig fancyTab = builder.getConfig().fancyTab;
			((CheckboxWidgetAccessor) checkbox).setChecked(fancyTab != null && fancyTab.enabled);
		}
		doneButton = ButtonWidget.builder(ScreenTexts.DONE, b -> close()).build();
		updateLayout();
	}

	@Override
	public void close() {
		if (tab) builder.updateTabWidgetsList();
		super.close();
	}

	@Override
	protected void refreshWidgetPositions() {
		super.refreshWidgetPositions();
		layout.refreshPositions();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}
}
