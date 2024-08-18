package de.hysky.skyblocker;

import java.util.Comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JavaOps;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

public class UpdateNotificationsTest {
	private final Comparator<Version> COMPARATOR = UpdateNotifications.COMPARATOR;
	private final Codec<SemanticVersion> SEM_VER_CODEC = UpdateNotifications.SEM_VER_CODEC;
	private final SemanticVersion LATEST_VERSION = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.22.0+1.21").getOrThrow();

	@BeforeAll
	public static void setupEnvironment() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void testLatestAgainstRegular() {
		SemanticVersion regular = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.21.1+1.21").getOrThrow();

		//Requires that the latest be newer than this normal release version
		Assertions.assertTrue(COMPARATOR.compare(LATEST_VERSION, regular) > 0);
	}

	@Test
	void testLatestAgainstBeta() {
		SemanticVersion beta = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.22.0-beta.1+1.21").getOrThrow();

		//Requires that the latest be newer than the beta
		Assertions.assertTrue(COMPARATOR.compare(LATEST_VERSION, beta) > 0);
	}

	@Test
	void testLatestAgainstAlpha() {
		SemanticVersion alpha = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.22.0-alpha.1+1.21").getOrThrow();

		//Requires that the latest be newer than the alpha
		Assertions.assertTrue(COMPARATOR.compare(LATEST_VERSION, alpha) > 0);
	}

	@Test
	void testLatestAgainstOldAlpha() {
		SemanticVersion oldAlpha = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.21.1-alpha-pr-888-afc81df+1.21").getOrThrow();

		//Requires the alpha is older than the latest
		Assertions.assertEquals(COMPARATOR.compare(oldAlpha, LATEST_VERSION), -1);
	}

	@Test
	void testThatTheCurrentAlphaAgainstLatestShouldBeDiscarded() {
		SemanticVersion currentAlpha = SEM_VER_CODEC.parse(JavaOps.INSTANCE, "1.22.0-alpha-pr-908-fe7d89a+1.21").getOrThrow();

		//Requires that the current alpha be discarded against the latest version
		Assertions.assertTrue(UpdateNotifications.shouldDiscard(currentAlpha, LATEST_VERSION));
	}
}
