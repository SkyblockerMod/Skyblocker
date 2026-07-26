package de.hysky.skyblocker.skyblock.chat.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class BeeKeeperFilter extends SimpleChatFilter {
	private static final String BEEKEEPER_BUFF_CHANGED = "^New day! Your Beekeeper buff changed!$";
	//^New buff: .+\.$ ???
	private static final String BEEKEEPER_BUFF = "^New buff: .*(Honeyhives?|Honeycomb|Critter|Trees lathered).*\\.$";
	private static final String TOGGLE_BEEKEEPER = "^You can disable this messaging by toggling BeeKeeper in your /hotf!$";

	public BeeKeeperFilter() {
		super(BEEKEEPER_BUFF_CHANGED + "|" + BEEKEEPER_BUFF + "|"+ TOGGLE_BEEKEEPER);
	}

	@Override
	protected ChatFilterResult state() {
		return !Utils.isInGalatea() && !Utils.isInTorrhusCanyon() ? SkyblockerConfigManager.get().chat.hideToggleBeeKeeper : ChatFilterResult.PASS;
	}
}
