package de.hysky.skyblocker.config;

import de.hysky.skyblocker.config.configs.*;

public class SkyblockerConfig {
	public int version = SkyblockerConfigManager.CONFIG_VERSION;

	public GeneralConfig general = new GeneralConfig();

	public UIAndVisualsConfig uiAndVisuals = new UIAndVisualsConfig();

	public HelperConfig helpers = new HelperConfig();

	public DungeonsConfig dungeons = new DungeonsConfig();

	public ForagingConfig foraging = new ForagingConfig();

	public CrimsonIsleConfig crimsonIsle = new CrimsonIsleConfig();

	public MiningConfig mining = new MiningConfig();

	public FarmingConfig farming = new FarmingConfig();

	public HuntingConfig hunting = new HuntingConfig();

	public OtherLocationsConfig otherLocations = new OtherLocationsConfig();

	public SlayersConfig slayers = new SlayersConfig();

	public ChatConfig chat = new ChatConfig();

	public QuickNavigationConfig quickNav = new QuickNavigationConfig();

	public EventNotificationsConfig eventNotifications = new EventNotificationsConfig();

	public MiscConfig misc = new MiscConfig();

	public DebugConfig debug = new DebugConfig();
}
