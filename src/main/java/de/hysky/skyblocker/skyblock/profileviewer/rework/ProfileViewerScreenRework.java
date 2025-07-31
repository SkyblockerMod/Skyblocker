package de.hysky.skyblocker.skyblock.profileviewer.rework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;
import com.mojang.util.UndashedUuid;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerNavButton;
import de.hysky.skyblocker.skyblock.profileviewer.model.ApiProfileResponse;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.ProfileUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ProfileViewerScreenRework extends Screen {
	public static Logger LOGGER = LogManager.getLogger();
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
			.create();
	public static final List<Function<ProfileLoadState.SuccessfulLoad, ProfileViewerPage>> PAGE_CONSTRUCTORS =
			new ArrayList<>();

	public ProfileViewerScreenRework() {
		super(Text.of("SkyBlocker Profile Viewer"));
		displayLoadedProfile(new ProfileLoadState.Loading());
	}

	public static Screen forPlayer(String username) {
		var screen = new ProfileViewerScreenRework();
		screen.loadProfilesFromPlayer(username);
		return screen;
	}

	//<editor-fold desc="Loading and state management">
	private CompletableFuture<ProfileLoadState> reload;
	private ProfileLoadState currentLoadState;
	private List<ProfileViewerPage> pages;
	private List<ProfileViewerNavButton> buttons;
	private List<ProfileViewerWidget.Instance> widgets;
	private int selectedIndex = 0;


	public ProfileLoadState getCurrentLoadState() {
		return currentLoadState;
	}

	public void displayLoadedProfile(ProfileLoadState profileLoadState) {
		this.currentLoadState = profileLoadState;
		this.pages = switch (profileLoadState) {
			case ProfileLoadState.Error error -> List.of(new ErrorPage(error));
			case ProfileLoadState.SuccessfulLoad successfulLoad -> PAGE_CONSTRUCTORS.stream().sorted().map(it -> it.apply(successfulLoad)).toList();
			case ProfileLoadState.Loading ignored -> List.of(new LoadingPage());
		};
		this.buttons = new ArrayList<>();
		for (int i = 0; i < pages.size(); i++) {
			var page = pages.get(i);
			buttons.add(new ProfileViewerNavButton(ignored -> setSelectedPage(selectedIndex), page.getName(), page.getIcon(), i, false));
		}
		setSelectedPage(0);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public ProfileViewerPage getSelectedPage() {
		return pages.get(selectedIndex);
	}

	public void setSelectedPage(int index) {
		this.selectedIndex = index;
		for (int i = 0; i < buttons.size(); i++) {
			buttons.get(i).setToggled(i == selectedIndex);
		}
		widgets = pages.get(selectedIndex).getWidgets();
		clearChildren();
		init();
	}

	@Override
	protected void init() {
		super.init();
		int rootX = width / 2 - GUI_WIDTH / 2;
		int rootY = height / 2 - GUI_HEIGHT / 2 + 5;
		for (var widget : widgets) {
			widget.setPositionFromRoot(rootX + 5, rootY + 7);
			addDrawableChild(widget);
		}
	}

	public CompletableFuture<ProfileLoadState> loadProfilesFromPlayer(String name) {
		if (reload != null) {
			reload.cancel(true);
		}
		this.displayLoadedProfile(new ProfileLoadState.Loading());
		return reload = ProfileUtils.fetchFullProfile(name)
				.thenApplyAsync(jsonObject -> {
					try {
						return GSON.fromJson(jsonObject, ApiProfileResponse.class);
					} catch (Exception ex) {
						try (var buffer = Files.newBufferedWriter(Path.of("last_failed_profile_response.json"))) {
							SkyblockerMod.GSON.toJson(jsonObject, buffer);
						} catch (IOException e) {
							ex.addSuppressed(e);
						}
						throw ex;
					}
				})
				.thenApplyAsync(apiProfileResponse -> apiProfileResponse
						.profiles
						.stream()
						.max(Comparator.comparing(it -> it.selected))
						.<ProfileLoadState>map(selectedProfile -> {
							var uuid = UndashedUuid.fromStringLenient(ApiUtils.name2Uuid(name));
							return new ProfileLoadState.SuccessfulLoad(
									selectedProfile,
									uuid,
									selectedProfile.members.get(uuid)
							);
						})
						.orElseGet(() -> new ProfileLoadState.Error("No profile found")))
				.exceptionally(ex -> {
					LOGGER.error("Failed to load profile of {}", name, ex);
					return new ProfileLoadState.Error(ex.getMessage());
				})
				.thenApplyAsync(load -> {
					displayLoadedProfile(load);
					return load;
				}, MinecraftClient.getInstance());
	}
	//</editor-fold>

	private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/base_plate.png");
	private static final int GUI_WIDTH = 322;
	public static final int PAGE_WIDTH = GUI_WIDTH - 10;
	private static final int GUI_HEIGHT = 180;
	public static final int PAGE_HEIGHT = GUI_HEIGHT - 10;

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		int rootX = width / 2 - GUI_WIDTH / 2;
		int rootY = height / 2 - GUI_HEIGHT / 2 + 5;

		context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		for (var button : buttons) {
			button.setX(rootX + button.getIndex() * 28 + 4);
			button.setY(rootY - 28);
			button.render(context, mouseX, mouseY, deltaTicks);
		}
		super.render(context, mouseX, mouseY, deltaTicks);
	}
}
