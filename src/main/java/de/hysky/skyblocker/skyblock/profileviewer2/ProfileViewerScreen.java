package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfileResponse;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.pages.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer2.pages.SkillsPage;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

// TODO should this support tab navigation?
// TODO add generic LoadingPage to be displayed when pv pages are still loading
public final class ProfileViewerScreen extends AbstractProfileViewerScreen {
	private final ApiProfileResponse apiProfileResponse;
	private final ApiProfile profile;
	private final GameProfile userProfile;
	private final ProfileMember member;
	private final List<ProfileViewerPage<?>> pages = List.of(new SkillsPage());
	private final Set<ProfileViewerPage<?>> loadedPages = new HashSet<>();
	private int selectedPageIndex = 0;

	protected ProfileViewerScreen(ApiProfileResponse apiProfileResponse, ApiProfile profile, GameProfile userProfile, ProfileMember member) {
		super(Component.literal("Skyblocker Profile Viewer"));
		this.apiProfileResponse = apiProfileResponse;
		this.profile = profile;
		this.userProfile = userProfile;
		this.member = member;
		this.loadPages();
	}

	private LoadingInformation createLoadingInformation() {
		return new LoadingInformation(this.profile, this.userProfile, this.member);
	}

	private void loadPages() {
		LoadingInformation loadingInformation = this.createLoadingInformation();

		for (ProfileViewerPage<?> page : this.pages) {
			page.load(loadingInformation).thenRunAsync(() -> this.loadedPages.add(page), this.minecraft);
		}
	}

	private ProfileViewerPage<?> getSelectedPage() {
		return this.pages.get(this.selectedPageIndex);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.getSelectedPage().getWidgets();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);

		graphics.pose().pushMatrix();
		graphics.pose().translate(this.getBackgroundX(), this.getBackgroundY());

		for (ProfileViewerWidget widget : this.getSelectedPage().getWidgets()) {
			widget.render(graphics, mouseX, mouseY, a);
		}

		graphics.pose().popMatrix();

		int middleX = graphics.guiWidth() / 2;
		int middleY = graphics.guiHeight() / 2;
		graphics.drawCenteredString(this.font, "The calm before the storm.", middleX, middleY, CommonColors.WHITE);
		graphics.drawCenteredString(this.font, this.userProfile.name() + "'s profile " + this.apiProfileResponse.getSelectedProfile().cuteName + "?", middleX, middleY + this.font.lineHeight, CommonColors.WHITE);
	}
}
