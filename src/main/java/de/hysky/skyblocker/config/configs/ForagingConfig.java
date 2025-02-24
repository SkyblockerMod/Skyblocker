package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ForagingConfig {

	@SerialEntry
	public Hunting hunting = new Hunting();

	public static class Hunting {

	}
}
