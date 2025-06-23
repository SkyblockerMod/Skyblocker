package de.hysky.skyblocker.config;

import de.hysky.skyblocker.config.configs.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class SkyblockerConfig {
    @SerialEntry
    public int version = SkyblockerConfigManager.CONFIG_VERSION;

    @SerialEntry
    public GeneralConfig general = new GeneralConfig();

    @SerialEntry
    public UIAndVisualsConfig uiAndVisuals = new UIAndVisualsConfig();

    @SerialEntry
    public HelperConfig helpers = new HelperConfig();

    @SerialEntry
    public DungeonsConfig dungeons = new DungeonsConfig();

    @SerialEntry
    public ForagingConfig foraging = new ForagingConfig();

    @SerialEntry
    public CrimsonIsleConfig crimsonIsle = new CrimsonIsleConfig();

    @SerialEntry
    public MiningConfig mining = new MiningConfig();

    @SerialEntry
    public FarmingConfig farming = new FarmingConfig();

    @SerialEntry
    public HuntingConfig hunting = new HuntingConfig();

    @SerialEntry
    public OtherLocationsConfig otherLocations = new OtherLocationsConfig();

    @SerialEntry
    public SlayersConfig slayers = new SlayersConfig();

    @SerialEntry
    public ChatConfig chat = new ChatConfig();

    @SerialEntry
    public QuickNavigationConfig quickNav = new QuickNavigationConfig();

    @SerialEntry
    public EventNotificationsConfig eventNotifications = new EventNotificationsConfig();

    @SerialEntry
    public MiscConfig misc = new MiscConfig();

    @SerialEntry
    public DebugConfig debug = new DebugConfig();
}
