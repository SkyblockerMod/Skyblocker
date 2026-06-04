package de.hysky.skyblocker.skyblock.profileviewer2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfile;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ApiProfileResponse;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.pages.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer2.pages.SkillsPage;
import de.hysky.skyblocker.skyblock.profileviewer2.pages.SlayersPage;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.PageTabWidget;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

// TODO should this support tab navigation?
public final class ProfileViewerScreen extends AbstractProfileViewerScreen {
	private static final Logger LOGGER = LogUtils.getLogger();
	@SuppressWarnings("unused")
	private final ApiProfileResponse apiProfileResponse;
	private final ApiProfile profile;
	private final GameProfile userProfile;
	private final ProfileMember member;
	private final long openedAt = System.currentTimeMillis();
	private final List<ProfileViewerPage<?>> pages = List.of(new SkillsPage(), new SlayersPage());
	private final Set<ProfileViewerPage<?>> loadedPages = new HashSet<>();
	private final List<PageTabWidget> tabWidgets = List.of(createPageTab(0), createPageTab(1));
	private final FrameLayout contentLayout = new FrameLayout(CONTENT_WIDTH, CONTENT_HEIGHT);
	private int selectedPageIndex;

	protected ProfileViewerScreen(ApiProfileResponse apiProfileResponse, ApiProfile profile, GameProfile userProfile, ProfileMember member) {
		super(Component.literal("Skyblocker Profile Viewer"));
		this.apiProfileResponse = apiProfileResponse;
		this.profile = profile;
		this.userProfile = userProfile;
		this.member = member;
		this.loadPages();
		this.setSelectedPage(0);
	}

	private PageTabWidget createPageTab(int index) {
		return new PageTabWidget(this.pages.get(index).getIcon(), index, this::setSelectedPage);
	}

	private LoadingInformation createLoadingInformation() {
		return new LoadingInformation(this.profile, this.userProfile, this.member);
	}

	private void loadPages() {
		LoadingInformation loadingInformation = this.createLoadingInformation();

		for (ProfileViewerPage<?> page : this.pages) {
			page.load(loadingInformation).thenAcceptAsync(layoutElement -> {
				this.contentLayout.addChild(layoutElement, l -> l.alignVerticallyTop().alignHorizontallyLeft()); // custom layout setting cuz FrameLayout centers stuff by default
				this.contentLayout.arrangeElements();
				repositionElements();
				this.loadedPages.add(page);
			}, this.minecraft).exceptionallyAsync(throwable -> {
				LOGGER.error("[Skyblocker Profile Viewer] Failed to load {} page!", page.getName().getString(), throwable);
				return null;
			}, this.minecraft);
		}
	}

	private ProfileViewerPage<?> getSelectedPage() {
		return this.pages.get(this.selectedPageIndex);
	}

	public void setSelectedPage(int index) {
		this.selectedPageIndex = Preconditions.checkPositionIndex(index, this.pages.size());

		for (PageTabWidget tabWidget : this.tabWidgets) {
			tabWidget.setSelected(this.selectedPageIndex == this.tabWidgets.indexOf(tabWidget));
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		List<ProfileViewerWidget> children = new ArrayList<>();
		children.addAll(this.getSelectedPage().getWidgets());
		children.addAll(this.tabWidgets);

		return children;
	}

	@Override
	protected void init() {
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		for (PageTabWidget widget : this.tabWidgets) {
			widget.updatePosition(this.getBackgroundX(), this.getBackgroundY());
		}
		this.contentLayout.setPosition(this.getBackgroundX() + PADDING, this.getBackgroundY() + PADDING);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// Reposition everything that is rendering
		//this.repositionElements();
		// Render the unselected buttons under the background
		this.extractTabButtons(graphics, mouseX, mouseY, a, false);
		// Render the background
		super.extractRenderState(graphics, mouseX, mouseY, a);
		// Render the selected tab on top of the background
		this.extractTabButtons(graphics, mouseX, mouseY, a, true);

		ProfileViewerPage<?> selectedPage = this.getSelectedPage();

		// Render the loaded page or some generic loading text
		if (this.loadedPages.contains(selectedPage)) {
			for (ProfileViewerWidget widget : selectedPage.getWidgets()) {
				widget.extractRenderState(graphics, mouseX, mouseY, a);
			}
		} else {
			int centreX = this.getBackgroundX() + (BACKGROUND_WIDTH / 2);
			int centreY = this.getBackgroundY() + (BACKGROUND_HEIGHT / 2);
			long timeLoadingPage = System.currentTimeMillis() - this.openedAt;
			Component pageLoadingText = Component.empty()
					.append(Component.literal("Loading the "))
					.append(selectedPage.getName())
					.append(Component.literal(" page..."));
			Component loadingDotsText = Component.literal(LoadingDotsText.get(timeLoadingPage));

			graphics.centeredText(this.font, pageLoadingText, centreX, centreY - this.font.lineHeight, CommonColors.WHITE);
			graphics.centeredText(this.font, loadingDotsText, centreX, centreY + this.font.lineHeight, CommonColors.WHITE);
		}
	}

	private void extractTabButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, boolean onlySelected) {
		for (PageTabWidget tabWidget : this.tabWidgets) {
			// We need to render the selected tab button behind the screen
			if (onlySelected && this.tabWidgets.indexOf(tabWidget) != this.selectedPageIndex) {
				continue;
			}

			tabWidget.extractRenderState(graphics, mouseX, mouseY, a);
		}
	}
}
