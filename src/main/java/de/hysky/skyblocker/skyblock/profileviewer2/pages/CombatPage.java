package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

public final class CombatPage implements ProfileViewerPage<LoadingInformation> {
	private final List<AbstractWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
		// Using a stone sword would be weird considering the skills page's icon
		return Ico.MADDOX_BATPHONE;
	}

	@Override
	public Component getName() {
		return Component.literal("Combat");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation data) {
		LinearLayout vertical = LinearLayout.vertical();
		vertical.addChild(new RulerWidget());
		vertical.addChild(new StringWidget(this.getName(), Minecraft.getInstance().font), l -> l.paddingLeft(18).paddingTop(18));

		vertical.visitWidgets(this.widgets::add);

		return vertical;
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
