package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.TestTextWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SlayersPage implements ProfileViewerPage<LoadingInformation> {
	private final List<ProfileViewerWidget> widgets = new ArrayList<>();

	@Override
	public ItemStack getIcon() {
		return Ico.MADDOX_BATPHONE;
	}

	@Override
	public Component getName() {
		return Component.literal("Slayers");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation data) {
		LinearLayout vertical = LinearLayout.vertical();
		this.widgets.add(vertical.addChild(new RulerWidget()));
		this.widgets.add(vertical.addChild(new TestTextWidget(this.getName()), l -> l.paddingLeft(18).paddingTop(18)));
		return vertical;
	}

	@Override
	public List<ProfileViewerWidget> getWidgets() {
		return this.widgets;
	}
}
