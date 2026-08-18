package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.LevelBarWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PlayerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.SkillsInfoBoxWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

public final class SkillsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int SPACING = 2;
	private static final int LEVEL_BAR_WIDTH = 105;
	private final List<AbstractWidget> widgets = new ArrayList<>();

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
		LinearLayout pageLayout = LinearLayout.horizontal();

		this.widgets.add(pageLayout.addChild(new RulerWidget()));

		// Player & Basic Info side
		LinearLayout leftSectionLayout = LinearLayout.vertical().spacing(SPACING);
		this.widgets.add(leftSectionLayout.addChild(new PlayerWidget(info.mainMember())));
		this.widgets.add(leftSectionLayout.addChild(new SkillsInfoBoxWidget(PlayerWidget.WIDTH, 71, info)));
		pageLayout.addChild(leftSectionLayout);

		// Spacing between left and right section
		pageLayout.addChild(SpacerElement.width(3));

		// Skills Area
		GridLayout skillsAreaLayout = new GridLayout().rowSpacing(SPACING * 2).columnSpacing(SPACING * 4);
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.COMBAT, info), 1, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.MINING, info), 2, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FARMING, info), 3, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FORAGING, info), 4, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.FISHING, info), 5, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.ENCHANTING, info), 6, 1));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.ALCHEMY, info), 1, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.TAMING, info), 2, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.HUNTING, info), 3, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.CARPENTRY, info), 4, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.RUNECRAFTING, info), 5, 2));
		this.widgets.add(skillsAreaLayout.addChild(LevelBarWidget.forSkill(LEVEL_BAR_WIDTH, Skill.SOCIAL, info), 6, 2));
		this.widgets.add(skillsAreaLayout.addChild(new LevelBarWidget(LEVEL_BAR_WIDTH), 7, 1));
		pageLayout.addChild(skillsAreaLayout, pageLayout.newCellSettings().alignVerticallyMiddle().paddingTop(-4));

		return pageLayout;
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
