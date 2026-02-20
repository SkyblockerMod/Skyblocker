package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SkillsPage implements ProfileViewerPage<ProfileMember> {
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
		return CompletableFuture.completedFuture(info.member())
				.thenAcceptAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public void buildWidgets(ProfileMember info) {
		this.widgets.add(new RulerWidget());
	}

	@Override
	public List<ProfileViewerWidget> getWidgets() {
		return this.widgets;
	}
}
