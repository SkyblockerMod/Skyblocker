package de.hysky.skyblocker.config;

import de.hysky.skyblocker.config.configs.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class SkyblockerConfig {
	@SerialEntry
	public int version = 2;

	@SerialEntry
	public GeneralConfig general = new GeneralConfig();

	@SerialEntry
	public UiAndVisualsConfig uiAndVisuals = new UiAndVisualsConfig();

	@SerialEntry
	public HelperConfig helper = new HelperConfig();

	@SerialEntry
	public DungeonsConfig dungeons = new DungeonsConfig();

	@SerialEntry
	public CrimsonIsleConfig crimsonIsle = new CrimsonIsleConfig();

	@SerialEntry
	public MiningConfig mining = new MiningConfig();

	@SerialEntry
	public FarmingConfig farming = new FarmingConfig();

	@SerialEntry
	public OtherLocationsConfig otherLocations = new OtherLocationsConfig();

	@SerialEntry
	public SlayerConfig slayer = new SlayerConfig();

	@SerialEntry
	public MessagesConfig messages = new MessagesConfig();

	@SerialEntry
	public QuickNavConfig quickNav = new QuickNavConfig();

	@SerialEntry
	public MiscConfig misc = new MiscConfig();
}
