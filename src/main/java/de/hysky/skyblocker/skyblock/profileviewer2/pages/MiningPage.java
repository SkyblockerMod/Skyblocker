package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.tree.HotmTreeBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

public final class MiningPage implements ProfileViewerPage<LoadingInformation> {
	private List<AbstractWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
		return Ico.STONE_PICKAXE;
	}

	@Override
	public Component getName() {
		return Component.literal("Mining");
	}

	@Override
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation info) {
		ProfileMember member = info.member();
		LinearLayout pageLayout = LinearLayout.vertical();

		LayoutElement hotmTree = HotmTreeBuilder.INSTANCE.buildLayout(member, member.skillTree.selectedSkillTreeSlot.mining);
		pageLayout.addChild(hotmTree);

		// Add all widgets
		pageLayout.visitWidgets(this.widgets::add);

		return pageLayout;
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
