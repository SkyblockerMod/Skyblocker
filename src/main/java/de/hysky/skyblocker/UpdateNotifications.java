package de.hysky.skyblocker;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.game.minecraft.McVersionLookup;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.UnaryOperator;

public class UpdateNotifications {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/skyblocker-liap/version?loaders=[%22fabric%22]&include_changelog=false";
	private static final Version MOD_VERSION = SkyblockerMod.SKYBLOCKER_MOD.getMetadata().getVersion();
	private static final Path CONFIG_PATH = SkyblockerMod.CONFIG_DIR.resolve("update_notifications_v2.json");
	@VisibleForTesting
	protected static final Comparator<Version> VERSION_COMPARATOR = Version::compareTo;
	@VisibleForTesting
	protected static final Codec<SemanticVersion> SEMANTIC_VERSION_CODEC = Codec.STRING.comapFlatMap(UpdateNotifications::parseVersion, SemanticVersion::toString);
	@VisibleForTesting
	protected static final Codec<SemanticVersion> MINECRAFT_VERSION_CODEC = Codec.STRING.comapFlatMap(UpdateNotifications::parseMinecraftVersion, SemanticVersion::toString);
	private static final SystemToast.SystemToastId TOAST_TYPE = new SystemToast.SystemToastId(10000L);
	// The config is in a separate file and uses ProfiledData to prevent modpacks/people who share configs from providing default or wrong configurations to others.
	private static final ProfiledData<Config> CONFIG = new ProfiledData<>(CONFIG_PATH, Config.CODEC);
	private static boolean sentUpdateNotification;
	private static @Nullable CompletableFuture<Void> loaded;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> loaded = CONFIG.init());
		SkyblockEvents.JOIN.register(() -> Objects.requireNonNull(loaded).thenRunAsync(UpdateNotifications::tryCheckForNewVersion, Minecraft.getInstance()));
	}

	private static void tryCheckForNewVersion() {
		if (getConfig().enabled() && !sentUpdateNotification) {
			// Wait a minute since when you join Skyblock there's usually a bunch of chat messages that pop up
			// so that this doesn't get buried
			Scheduler.INSTANCE.schedule(UpdateNotifications::checkForNewVersion, 60 * 20);
			Scheduler.INSTANCE.schedule(UpdateNotifications::introduceNewUpdate, 60 * 20);
		}
	}

	private static void checkForNewVersion() {
		CompletableFuture.runAsync(() -> {
			try {
				// The cast would only fail because someone compiled the mod with a non-compliant version
				SemanticVersion currentModVersion = (SemanticVersion) MOD_VERSION;
				SemanticVersion currentMinecraftVersion = parseMinecraftVersion(SharedConstants.getCurrentVersion().id()).getOrThrow();
				String response = Http.sendGetRequest(VERSIONS_URL);
				List<MrVersion> mrVersions = MrVersion.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getOrThrow();

				// Set it to true now so that we don't keep re-checking if the data should be discarded
				sentUpdateNotification = true;

				Optional<MrVersion> optimalVersion = getOptimalVersion(currentModVersion, currentMinecraftVersion, mrVersions);

				if (optimalVersion.isPresent() && !shouldDiscard(currentModVersion, optimalVersion.get().version())) {
					MrVersion newVersion = optimalVersion.get();
					String downloadLink = "https://modrinth.com/mod/skyblocker-liap/version/" + newVersion.id();
					Component versionText = Component.literal(newVersion.name()).withStyle(style -> style
							.applyFormat(ChatFormatting.GRAY)
							.withUnderlined(true)
							.withClickEvent(new ClickEvent.OpenUrl(URI.create(downloadLink))));

					MINECRAFT.execute(() -> {
						if (MINECRAFT.player == null) {
							return;
						}

						MINECRAFT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.updateNotifications.newUpdateMessage", versionText)), false);
						SystemToast.add(MINECRAFT.getToastManager(), TOAST_TYPE, Component.translatable("skyblocker.updateNotifications.newUpdateToast.title"), Component.translatableEscape("skyblocker.updateNotifications.newUpdateToast.description", newVersion.version()));
					});
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Update Notifications] Failed to determine if an update is available or not!", e);
			}
		}, Executors.newVirtualThreadPerTaskExecutor());
	}

	private static void introduceNewUpdate() {
		try {
			Optional<SemanticVersion> newestVersionUsed = getConfig().newestVersionUsed();
			SemanticVersion currentModVersion = (SemanticVersion) MOD_VERSION;
			boolean shouldIntroduceUpdate = newestVersionUsed.isEmpty() || VERSION_COMPARATOR.compare(currentModVersion, newestVersionUsed.get()) > 0;

			if (!shouldIntroduceUpdate) {
				return;
			}

			String versionTagString = String.format(Locale.ENGLISH, "v%d.%d.%d", currentModVersion.getVersionComponent(0), currentModVersion.getVersionComponent(1), currentModVersion.getVersionComponent(2));
			Component configureText = Component.translatable("skyblocker.updateNotifications.configureNewVersionHere").withStyle(style -> style
					.applyFormat(ChatFormatting.GRAY)
					.withUnderlined(true)
					.withClickEvent(new ClickEvent.RunCommand("/skyblocker config " + versionTagString)));
			Component newVersionText = Component.translatable("skyblocker.updateNotifications.configureNewVersion", SkyblockerMod.VERSION, configureText);

			MINECRAFT.player.displayClientMessage(Constants.PREFIX.get().append(newVersionText), false);
			setConfig(config -> config.withNewestVersionUsed(currentModVersion));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Update Notifications] Failed to introduce the new update!", e);
		}
	}

	/**
	 * {@return the optimal version for the user to update to}.
	 *
	 * <p> If the latest Skyblocker version is available for the currently used Minecraft version, then that version is suggested.
	 * If the latest Skyblocker version does not support the currently used Minecraft version then the version suggested will be for the
	 * newest version of Minecraft that Skyblocker supports to encourage updating.
	 */
	@VisibleForTesting
	protected static Optional<MrVersion> getOptimalVersion(SemanticVersion currentModVersion, SemanticVersion currentMinecraftVersion, List<MrVersion> mrVersions) {
		Config config = Debug.isTestEnvironment() ? Config.DEFAULT : getConfig();

		// Find newer versions of the mod that align with the preferred release channels
		List<MrVersion> eligibleModVersions = mrVersions.stream()
				.filter(releaseVersion -> config.encompassingChannels().contains(releaseVersion.channel()))
				.filter(releaseVersion -> VERSION_COMPARATOR.compare(releaseVersion.version(), currentModVersion) > 0)
				.toList();

		// If there is no newer version of the mod then exit
		if (eligibleModVersions.isEmpty()) {
			return Optional.empty();
		}

		// Find what the newest available version number is
		SemanticVersion newestModVersion = Collections.max(eligibleModVersions, Comparator.comparing(MrVersion::version)).version();

		// Finds a release whose version number matches the newest version available and is available for the current Minecraft version
		Optional<MrVersion> latestModVersionForCurrentMinecraftVersion = eligibleModVersions.stream()
				.filter(releaseVersion -> VERSION_COMPARATOR.compare(releaseVersion.version(), newestModVersion) == 0)
				.filter(releaseVersion -> releaseVersion.minecraftVersions().stream().anyMatch(version -> VERSION_COMPARATOR.compare(version, currentMinecraftVersion) == 0))
				.findFirst();

		if (latestModVersionForCurrentMinecraftVersion.isPresent()) {
			return latestModVersionForCurrentMinecraftVersion;
		}

		// Finds a release whose version number matches the newest version available and is available for the newest Minecraft version that the mod supports
		Optional<MrVersion> latestModVersionForNewestMinecraftVersion = eligibleModVersions.stream()
				.filter(releaseVersion -> VERSION_COMPARATOR.compare(releaseVersion.version(), newestModVersion) == 0)
				.max(Comparator.comparing(MrVersion::newestMinecraftVersionSupported, VERSION_COMPARATOR));

		return latestModVersionForNewestMinecraftVersion;
	}

	private static DataResult<SemanticVersion> parseVersion(String version) {
		String formattedVersion = version.transform(s -> s.charAt(0) == 'v' ? s.substring(1) : s);

		try {
			return DataResult.success(SemanticVersion.parse(formattedVersion));
		} catch (VersionParsingException e) {
			return DataResult.error(() -> "Failed to parse semantic version from string: " + formattedVersion);
		}
	}

	private static DataResult<SemanticVersion> parseMinecraftVersion(String minecraftVersion) {
		String normalized = McVersionLookup.normalizeVersion(minecraftVersion, McVersionLookup.getRelease(minecraftVersion));

		return parseVersion(normalized);
	}

	private static boolean isUnofficialAlphaOrBeta(SemanticVersion version) {
		return switch (version.getPrereleaseKey().orElse("")) {
			case String s when s.startsWith("alpha") -> s.substring(5).charAt(0) == '-';
			case String s when s.startsWith("beta") -> s.substring(4).charAt(0) == '-';

			default -> false;
		};
	}

	/**
	 * Since our "unofficial" betas and alphas (from actions) take after the latest release number we want to discard them from the checker
	 * if the current version is "unofficial" and the major, minor, and patch versions match.
	 */
	@VisibleForTesting
	protected static boolean shouldDiscard(SemanticVersion currentVersion, SemanticVersion latestVersion) {
		if (isUnofficialAlphaOrBeta(currentVersion)) {
			// We will expect all 3 components to be present

			int currentMajor = currentVersion.getVersionComponent(0);
			int currentMinor = currentVersion.getVersionComponent(1);
			int currentPatch = currentVersion.getVersionComponent(2);

			int latestMajor = latestVersion.getVersionComponent(0);
			int latestMinor = latestVersion.getVersionComponent(1);
			int latestPatch = latestVersion.getVersionComponent(2);

			return currentMajor == latestMajor && currentMinor == latestMinor && currentPatch == latestPatch;
		}

		return false;
	}

	public static Config getConfig() {
		return CONFIG.computeIfAbsent(Utils.getUuid(), "", () -> Config.DEFAULT);
	}

	public static void setConfig(UnaryOperator<Config> updater) {
		CONFIG.put(Utils.getUuid(), "", updater.apply(getConfig()));
	}

	public record Config(boolean enabled, Channel channel, Optional<SemanticVersion> newestVersionUsed) {
		// Set default channel to alpha since most people probably want whatever the latest version is
		// and we work hard to polish all of our releases.
		public static final Config DEFAULT = new Config(true, Channel.ALPHA, Optional.empty());
		private static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("enabled").forGetter(Config::enabled),
				Channel.CODEC.fieldOf("channel").forGetter(Config::channel),
				SEMANTIC_VERSION_CODEC.optionalFieldOf("newestVersionUsed").forGetter(Config::newestVersionUsed))
				.apply(instance, Config::new));

		private List<Channel> encompassingChannels() {
			return switch (this.channel) {
				case BETA -> List.of(Channel.RELEASE, Channel.BETA);
				case ALPHA -> List.of(Channel.values());

				default -> List.of(this.channel);
			};
		}

		public Config withEnabled(boolean newEnabled) {
			return new Config(newEnabled, this.channel, this.newestVersionUsed);
		}

		public Config withChannel(Channel newChannel) {
			return new Config(this.enabled, newChannel, this.newestVersionUsed);
		}

		private Config withNewestVersionUsed(SemanticVersion newNewestVersionUsed) {
			return new Config(this.enabled, this.channel, Optional.of(newNewestVersionUsed));
		}
	}

	/**
	 * Represents a mod release from the Modrinth API.
	 *
	 * @param id                the id of the release
	 * @param name              the name of the release
	 * @param version           the version number of this release
	 * @param minecraftVersions the versions of Minecraft this release supports
	 * @param channel           the release channel
	 */
	@VisibleForTesting
	protected record MrVersion(String id, String name, SemanticVersion version, List<SemanticVersion> minecraftVersions, Channel channel) {
		private static final Codec<MrVersion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(MrVersion::id),
				Codec.STRING.fieldOf("name").forGetter(MrVersion::name),
				SEMANTIC_VERSION_CODEC.fieldOf("version_number").forGetter(MrVersion::version),
				MINECRAFT_VERSION_CODEC.listOf().fieldOf("game_versions").forGetter(MrVersion::minecraftVersions),
				Channel.CODEC.fieldOf("version_type").forGetter(MrVersion::channel))
				.apply(instance, MrVersion::new));
		private static final Codec<List<MrVersion>> LIST_CODEC = CODEC.listOf();

		private SemanticVersion newestMinecraftVersionSupported() {
			return this.minecraftVersions().stream()
					.max(VERSION_COMPARATOR)
					.orElseThrow();
		}
	}

	public enum Channel implements StringRepresentable {
		RELEASE,
		BETA,
		ALPHA;

		private static final Codec<Channel> CODEC = StringRepresentable.fromValues(Channel::values);

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.general.updateNotifications.updateChannel.channel." + name());
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}
