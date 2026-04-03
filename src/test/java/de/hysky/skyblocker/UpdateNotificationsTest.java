package de.hysky.skyblocker;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;

import de.hysky.skyblocker.UpdateNotifications.MrVersion;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class UpdateNotificationsTest {
	private final Comparator<Version> versionComparator = UpdateNotifications.VERSION_COMPARATOR;
	private final Codec<SemanticVersion> semanticVersionCodec = UpdateNotifications.SEMANTIC_VERSION_CODEC;
	private final Codec<SemanticVersion> minecraftVersionCodec = UpdateNotifications.MINECRAFT_VERSION_CODEC;
	private final SemanticVersion latestVersion = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.22.0+1.21").getOrThrow();

	@BeforeAll
	public static void setupEnvironment() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void testLatestAgainstRegular() {
		SemanticVersion regular = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.21.1+1.21").getOrThrow();

		//Requires that the latest be newer than this normal release version
		Assertions.assertTrue(versionComparator.compare(latestVersion, regular) > 0);
	}

	@Test
	void testLatestAgainstBeta() {
		SemanticVersion beta = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.22.0-beta.1+1.21").getOrThrow();

		//Requires that the latest be newer than the beta
		Assertions.assertTrue(versionComparator.compare(latestVersion, beta) > 0);
	}

	@Test
	void testLatestAgainstAlpha() {
		SemanticVersion alpha = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.22.0-alpha.1+1.21").getOrThrow();

		//Requires that the latest be newer than the alpha
		Assertions.assertTrue(versionComparator.compare(latestVersion, alpha) > 0);
	}

	@Test
	void testLatestAgainstOldAlpha() {
		SemanticVersion oldAlpha = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.21.1-alpha-pr-888-afc81df+1.21").getOrThrow();

		//Requires the alpha is older than the latest
		Assertions.assertEquals(versionComparator.compare(oldAlpha, latestVersion), -1);
	}

	@Test
	void testThatTheCurrentAlphaAgainstLatestShouldBeDiscarded() {
		SemanticVersion currentAlpha = semanticVersionCodec.parse(JavaOps.INSTANCE, "1.22.0-alpha-pr-908-fe7d89a+1.21").getOrThrow();

		//Requires that the current alpha be discarded against the latest version
		Assertions.assertTrue(UpdateNotifications.shouldDiscard(currentAlpha, latestVersion));
	}

	@Test
	void testOptimalVersionSelection() {
		SemanticVersion mc1_21_8 = minecraftVersionCodec.parse(JavaOps.INSTANCE, "1.21.8").getOrThrow();
		SemanticVersion mc1_21_10 = minecraftVersionCodec.parse(JavaOps.INSTANCE, "1.21.10").getOrThrow();
		SemanticVersion mc1_21_11 = minecraftVersionCodec.parse(JavaOps.INSTANCE, "1.21.11").getOrThrow();
		SemanticVersion sv6_0_0 = semanticVersionCodec.parse(JavaOps.INSTANCE, "6.0.0").getOrThrow();
		SemanticVersion sv6_0_2 = semanticVersionCodec.parse(JavaOps.INSTANCE, "6.0.2").getOrThrow();

		MrVersion skb6_0_2_mc1_21_11 = new MrVersion("example", "Skyblocker 6.0.2 for 1.21.11", sv6_0_2, List.of(mc1_21_11), UpdateNotifications.Channel.RELEASE);
		MrVersion skb6_0_2_mc1_21_10 = new MrVersion("example", "Skyblocker 6.0.2 for 1.21.10", sv6_0_2, List.of(mc1_21_10), UpdateNotifications.Channel.RELEASE);
		List<MrVersion> testMrVersions = List.of(skb6_0_2_mc1_21_11, skb6_0_2_mc1_21_10);

		// If we're on 1.21.11 it is expected that it will prompt us to install 6.0.2 for 1.21.11
		Assertions.assertEquals(UpdateNotifications.getOptimalVersion(sv6_0_0, mc1_21_11, testMrVersions).get(), skb6_0_2_mc1_21_11);

		// If we're on 1.21.10 it is expected that it will prompt us to install 6.0.2 for 1.21.10
		Assertions.assertEquals(UpdateNotifications.getOptimalVersion(sv6_0_0, mc1_21_10, testMrVersions).get(), skb6_0_2_mc1_21_10);

		// If we're on 1.21.8 it is expected that it will prompt us to install 6.0.2 for 1.21.11
		Assertions.assertEquals(UpdateNotifications.getOptimalVersion(sv6_0_0, mc1_21_8, testMrVersions).get(), skb6_0_2_mc1_21_11);

		// If we're on the latest version (6.0.2 here) we should not be prompted to update whether we're on 1.21.11 or 1.21.10
		Assertions.assertTrue(UpdateNotifications.getOptimalVersion(sv6_0_2, mc1_21_11, testMrVersions).isEmpty());
		Assertions.assertTrue(UpdateNotifications.getOptimalVersion(sv6_0_2, mc1_21_10, testMrVersions).isEmpty());
	}
}
