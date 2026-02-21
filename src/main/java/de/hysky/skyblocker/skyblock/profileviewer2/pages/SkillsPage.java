package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.BasicInfoBoxWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PlayerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SkillsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int INFO_BOX_OFFSET = 2;
	private final List<ProfileViewerWidget> widgets = new ArrayList<>();

	@Override
	public ItemStack getIcon() {
		return Ico.IRON_SWORD;
	}

	@Override
	public Component getName() {
		return Component.literal("Skills");
	}

	@Override
	public CompletableFuture<Void> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenAcceptAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public void buildWidgets(LoadingInformation info) {
		this.widgets.add(new RulerWidget());
		this.widgets.add(new PlayerWidget(0, 0, info.mainMember()));
		this.widgets.add(new BasicInfoBoxWidget(0, PlayerWidget.HEIGHT + INFO_BOX_OFFSET, PlayerWidget.WIDTH, 71));
	}

	@Override
	public List<ProfileViewerWidget> getWidgets() {
		return this.widgets;
	}
}
