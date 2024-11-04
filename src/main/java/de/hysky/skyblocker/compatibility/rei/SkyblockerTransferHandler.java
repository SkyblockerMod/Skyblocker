package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;

public class SkyblockerTransferHandler implements TransferHandler {
	@Override
	public Result handle(Context context) {
		if (!(context.getDisplay() instanceof SkyblockCraftingDisplay skyblockCraftingDisplay)) return Result.createNotApplicable();

		String clickCommand = skyblockCraftingDisplay.getClickCommand();
		if (clickCommand.isEmpty()) return Result.createNotApplicable();

		if (context.isActuallyCrafting()) MessageScheduler.INSTANCE.sendMessageAfterCooldown(clickCommand, false);
		return Result.createSuccessful();
	}
}
