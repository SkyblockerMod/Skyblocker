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
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.DungeonLevelBarWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PlayerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.RulerWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.SkillLevelBarWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.SkillsInfoBoxWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.SkyBlockLevelBarWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;

public final class SkillsPage implements ProfileViewerPage<LoadingInformation> {
	private static final int SPACING = 2;
	protected static final int LEVEL_BAR_WIDTH = 105;
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

		pageLayout.addChild(new RulerWidget());

		// Player & Basic Info side
		LinearLayout leftSectionLayout = LinearLayout.vertical().spacing(SPACING);
		leftSectionLayout.addChild(new PlayerWidget(info.mainMember()));
		leftSectionLayout.addChild(new SkillsInfoBoxWidget(PlayerWidget.WIDTH, 71, info));
		pageLayout.addChild(leftSectionLayout);

		// Spacing between left and right section
		pageLayout.addChild(SpacerElement.width(3));

		// Skills area
		GridLayout skillsAreaLayout = new GridLayout().rowSpacing(SPACING * 2).columnSpacing(SPACING * 4);
		GridLayout.RowHelper skillsAreaRowHelper = skillsAreaLayout.createRowHelper(2);

		// Add level bars
		skillsAreaRowHelper.addChild(SkyBlockLevelBarWidget.create(LEVEL_BAR_WIDTH, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.COMBAT, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.ALCHEMY, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.MINING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.TAMING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.FARMING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.HUNTING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.FORAGING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.CARPENTRY, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.FISHING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.RUNECRAFTING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.ENCHANTING, info));
		skillsAreaRowHelper.addChild(SkillLevelBarWidget.create(LEVEL_BAR_WIDTH, Skill.SOCIAL, info));
		skillsAreaRowHelper.addChild(DungeonLevelBarWidget.createCatacombs(LEVEL_BAR_WIDTH, info));

		pageLayout.addChild(skillsAreaLayout, pageLayout.newCellSettings().alignVerticallyMiddle());
		pageLayout.visitWidgets(this.widgets::add);

		return pageLayout;
	}

	@Override
	public List<AbstractWidget> getWidgets() {
		return this.widgets;
	}
}
