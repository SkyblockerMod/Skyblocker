package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.BasicInfoBoxWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.LevelBarWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PlayerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;

public final class SkillsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int SPACING = 2;
	private static final int LEVEL_BAR_WIDTH = 112;
	private final List<ProfileViewerWidget> widgets = new ArrayList<>();

	@Override
	public FlexibleItemStack getIcon() {
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
		ProfileMember member = info.member();
		LinearLayout pageLayout = LinearLayout.horizontal();

		this.widgets.add(pageLayout.addChild(new RulerWidget()));

		// Player & Basic Info side
		LinearLayout leftSectionLayout = LinearLayout.vertical().spacing(SPACING);
		this.widgets.add(leftSectionLayout.addChild(new PlayerWidget(0, 0, info.mainMember())));
		this.widgets.add(leftSectionLayout.addChild(new BasicInfoBoxWidget(0, 0, PlayerWidget.WIDTH, 71)));
		pageLayout.addChild(leftSectionLayout);

		// Spacing between left and right section
		pageLayout.addChild(SpacerElement.width(3));

		// Skills Area
		GridLayout skillsAreaLayout = new GridLayout().rowSpacing(SPACING).columnSpacing(SPACING * 4);
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.COMBAT, member), 1, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.MINING, member), 2, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FARMING, member), 3, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FORAGING, member), 4, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FISHING, member), 5, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.ENCHANTING, member), 6, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.ALCHEMY, member), 1, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.TAMING, member), 2, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.HUNTING, member), 3, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.CARPENTRY, member), 4, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.RUNECRAFTING, member), 5, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.SOCIAL, member), 6, 2));
		pageLayout.addChild(skillsAreaLayout, pageLayout.newCellSettings().alignVerticallyMiddle().paddingTop(-2));

		return pageLayout;
	}

	@Override
	public List<ProfileViewerWidget> getWidgets() {
		return this.widgets;
	}
}
