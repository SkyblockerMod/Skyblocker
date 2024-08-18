package de.hysky.skyblocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Http;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public class UpdateNotifications {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final String BASE_URL = "https://api.modrinth.com/v2/project/y6DuFGwJ/version?loaders=[%22fabric%22]&game_versions=";
	private static final Version MOD_VERSION = SkyblockerMod.SKYBLOCKER_MOD.getMetadata().getVersion();
	private static final String MC_VERSION = SharedConstants.getGameVersion().getId();
	private static final Path CONFIG_PATH = SkyblockerMod.CONFIG_DIR.resolve("update_notifications.json");
	@VisibleForTesting
	protected static final Comparator<Version> COMPARATOR = Version::compareTo;
	@VisibleForTesting
	protected static final Codec<SemanticVersion> SEM_VER_CODEC = Codec.STRING.comapFlatMap(UpdateNotifications::parseVersion, SemanticVersion::toString);
	private static final SystemToast.Type TOAST_TYPE = new SystemToast.Type(10000L);

	public static Config config = Config.DEFAULT;
	private static boolean sentUpdateNotification;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> loadConfig());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
		SkyblockEvents.JOIN.register(() -> {
			if (config.enabled() && !sentUpdateNotification) checkForNewVersion();
		});
	}

	private static void loadConfig() {
		CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
				return Config.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
			} catch (NoSuchFileException ignored) {
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Update Notifications] Failed to load config!", e);
			}

			return Config.DEFAULT;
		}).thenAccept(loadedConfig -> config = loadedConfig);
	}

	private static void saveConfig() {
		try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
			SkyblockerMod.GSON.toJson(Config.CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow(), writer);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Update Notifications] Failed to save config :(", e);
		}
	}

	private static void checkForNewVersion() {
		CompletableFuture.runAsync(() -> {
			try {
				SemanticVersion version = (SemanticVersion) MOD_VERSION; //Would only fail because someone changed it themselves
				String response = Http.sendGetRequest(BASE_URL + "[%22" + MC_VERSION + "%22]");
				List<MrVersion> mrVersions = MrVersion.LIST_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getOrThrow();

				//Set it to true now so that we don't keep re-checking if the data should be discarded
				sentUpdateNotification = true;

				Optional<MrVersion> newestVersion = mrVersions.stream()
						.filter(ver -> Arrays.stream(config.includedChannels()).anyMatch(channel -> channel == ver.channel()))
						.filter(mrv -> COMPARATOR.compare(mrv.version(), version) > 0)
						.max(Comparator.comparing(MrVersion::version, COMPARATOR));

				if (newestVersion.isPresent() && CLIENT.player != null && !shouldDiscard(version, newestVersion.get().version())) {
					MrVersion newVersion = newestVersion.get();
					String downloadLink = "https://modrinth.com/mod/skyblocker-liap/version/" + newVersion.id();
					Text versionText = Text.literal(newVersion.name()).styled(style -> style
							.withFormatting(Formatting.GRAY)
							.withUnderline(true)
							.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink)));

					CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.updateNotifications.newUpdateMessage", versionText)));
					SystemToast.add(CLIENT.getToastManager(), TOAST_TYPE, Text.translatable("skyblocker.updateNotifications.newUpdateToast.title"), Text.stringifiedTranslatable("skyblocker.updateNotifications.newUpdateToast.description", newVersion.version()));
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Update Notifications] Failed to determine if an update is available or not!", e);
			}
		});
	}

	private static DataResult<SemanticVersion> parseVersion(String version) {
		String formattedVersion = switch (version) {
			case String s when s.charAt(0) == 'v' -> s.substring(1);

			default -> version;
		};

		try {
			return DataResult.success(SemanticVersion.parse(formattedVersion));
		} catch (VersionParsingException e) {
			return DataResult.error(() -> "Failed to parse semantic version from string: " + formattedVersion);
		}
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
			//We will expect all 3 components to be present

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

	public record Config(boolean enabled, Channel channel) {
		public static final Config DEFAULT = new Config(true, Channel.RELEASE);
		private static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("enabled").forGetter(Config::enabled),
				Channel.CODEC.fieldOf("channel").forGetter(Config::channel))
				.apply(instance, Config::new));

		private Channel[] includedChannels() {
			return switch (this.channel) {
				case BETA -> new Channel[] { Channel.RELEASE, Channel.BETA };
				case ALPHA -> Channel.values();

				default -> new Channel[] { this.channel };
			};
		}

		public Config withEnabled(boolean newEnabled) {
			return new Config(newEnabled, this.channel);
		}

		public Config withChannel(Channel newChannel) {
			return new Config(this.enabled, newChannel);
		}
	}

	private record MrVersion(String id, String name, SemanticVersion version, Channel channel) {
		private static final Codec<MrVersion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("id").forGetter(MrVersion::id),
				Codec.STRING.fieldOf("name").forGetter(MrVersion::name),
				SEM_VER_CODEC.fieldOf("version_number").forGetter(MrVersion::version),
				Channel.CODEC.fieldOf("version_type").forGetter(MrVersion::channel))
				.apply(instance, MrVersion::new));
		private static final Codec<List<MrVersion>> LIST_CODEC = CODEC.listOf();
	}

	public enum Channel implements StringIdentifiable {
		RELEASE,
		BETA,
		ALPHA;

		private static final Codec<Channel> CODEC = StringIdentifiable.createBasicCodec(Channel::values);

		@Override
		public String toString() {
			return I18n.translate("skyblocker.config.general.updateChannel.channel." + name());
		}

		@Override
		public String asString() {
			return name().toLowerCase();
		}
	}
}
