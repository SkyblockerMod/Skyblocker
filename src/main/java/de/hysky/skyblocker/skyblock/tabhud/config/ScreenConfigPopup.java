package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ScreenConfigPopup extends AbstractPopupScreen {

	private LinearLayout layout = LinearLayout.vertical();
	private final ScreenBuilder builder;
	private final boolean tab;
	private Checkbox checkbox;
	private Button doneButton;

	protected ScreenConfigPopup(Screen backgroundScreen, ScreenBuilder builder, boolean isTab) {
		super(Component.empty(), backgroundScreen);
		this.builder = builder;
		this.tab = isTab;
	}

	private void updateLayout() {
		clearWidgets();
		layout = LinearLayout.vertical().spacing(2);
		if (tab) {
			layout.addChild(checkbox);

			ScreenBuilder.FancyTabConfig fancyTab = builder.getConfig().fancyTab;
			if (checkbox.selected() && fancyTab != null) { // shouldn't be null if it's checked but SQUIGGLY LINE

				// TODO Translatable
				layout.addChild(Button.builder(Component.literal("Positioner: " + fancyTab.positioner), button -> {
					fancyTab.positioner = EnumUtils.cycle(fancyTab.positioner);
					button.setMessage(Component.literal("Positioner: " + fancyTab.positioner));
				}).build());

				layout.addChild(new StringWidget(Component.literal("Hidden Widgets"), font), LayoutSettings::alignHorizontallyCenter);
				LinearLayout checkboxes = LinearLayout.vertical().spacing(1);
				for (String s : PlayerListManager.getCurrentWidgets()) {
					HudWidget widget = PlayerListManager.getTabWidget(s);
					if (widget == null) continue;
					Component displayName = widget.getInformation().displayName();
					Checkbox checkboxWidget = checkboxes.addChild(Checkbox.builder(displayName, font)
							.onValueChange((box, checked) -> {
								if (checked) fancyTab.hiddenWidgets.add(widget.getId());
								else fancyTab.hiddenWidgets.remove(widget.getId());
							})
							.build());
					((CheckboxWidgetAccessor) checkboxWidget).setSelected(fancyTab.hiddenWidgets.contains(widget.getId()));
				}
				ScrollableLayout widget = layout.addChild(new ScrollableLayout(minecraft, checkboxes, 130));
				widget.setMaxHeight(130);
				widget.setMinWidth(150);
			}
		} else layout.addChild(SpacerElement.height(120));
		layout.addChild(doneButton);
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	@Override
	public void init() {
		if (tab) {
			checkbox = Checkbox.builder(Component.literal("Enable fancy tab"), font)
					.onValueChange((box, checked) -> {
						builder.getConfig().getOrCreateFancyTab().enabled = checked;
						updateLayout();
					})
					.build();
			ScreenBuilder.FancyTabConfig fancyTab = builder.getConfig().fancyTab;
			((CheckboxWidgetAccessor) checkbox).setSelected(fancyTab != null && fancyTab.enabled);
		}
		doneButton = Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build();
		updateLayout();
	}

	@Override
	public void onClose() {
		if (tab) builder.updateTabWidgetsList();
		super.onClose();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}
}
