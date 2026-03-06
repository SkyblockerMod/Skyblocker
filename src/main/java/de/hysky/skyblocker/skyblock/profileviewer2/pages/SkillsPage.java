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
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
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
	public CompletableFuture<LayoutElement> load(LoadingInformation info) {
		return CompletableFuture.completedFuture(info)
				.thenApplyAsync(this::buildWidgets, Minecraft.getInstance());
	}

	@Override
	public LayoutElement buildWidgets(LoadingInformation info) {
		// this frame layout is only there for the RulerWidget to not move the rest of the widgets due to the spacing
		FrameLayout frameLayout = new FrameLayout();
		frameLayout.defaultChildLayoutSetting().alignHorizontallyLeft().alignVerticallyTop(); // do not center by default grrrr
		this.widgets.add(frameLayout.addChild(new RulerWidget()));
		LinearLayout linearLayout = LinearLayout.vertical().spacing(INFO_BOX_OFFSET);
		this.widgets.add(linearLayout.addChild(new PlayerWidget(0, 0, info.mainMember())));
		this.widgets.add(linearLayout.addChild(new BasicInfoBoxWidget(0, 0, PlayerWidget.WIDTH, 71)));
		frameLayout.addChild(linearLayout);
		return frameLayout;
	}

	@Override
	public List<ProfileViewerWidget> getWidgets() {
		return this.widgets;
	}
}
