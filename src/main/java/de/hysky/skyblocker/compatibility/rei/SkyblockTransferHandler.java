package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.compatibility.rei.info.SkyblockInfoCategory;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SkyblockTransferHandler implements TransferHandler {
	private static final int MAX_FAIL_COUNT = 5;

	private String FAILED_ITEM;
	private int FAIL_COUNT;

	@Override
	public ApplicabilityResult checkApplicable(Context context) {
		// Only work on our displays
		CategoryIdentifier<?> identifier = context.getDisplay().getCategoryIdentifier();
		if (identifier != CategoryIdentifier.of(SkyblockCraftingRecipe.IDENTIFIER) && identifier != CategoryIdentifier.of(SkyblockInfoCategory.IDENTIFIER))
			return ApplicabilityResult.createNotApplicable();

		EntryIngredient ingredient = context.getDisplay().getOutputEntries().getFirst();
		EntryStack<?> entryStack = ingredient.getFirst();
		if (!(entryStack.getValue() instanceof ItemStack itemStack))
			return ApplicabilityResult.createNotApplicable();

		// Not applicable if it has 0 recipes, or no crafting recipe
		String neuId = itemStack.getNeuName();
		if (!NEURepoManager.getRecipes().containsKey(neuId) || NEURepoManager.getRecipes().get(neuId).stream().noneMatch(recipe -> recipe instanceof NEUCraftingRecipe))
			return ApplicabilityResult.createApplicableWithError(Text.translatable("skyblocker.rei.transfer.noRecipe"));

		return ApplicabilityResult.createApplicable();

	}

	private boolean hasFailed(String skyblockId) {
		if (skyblockId.equals(FAILED_ITEM)) {
			if (FAIL_COUNT < MAX_FAIL_COUNT) {
				FAIL_COUNT += 1;
				return true;
			}

			FAILED_ITEM = "";
			FAIL_COUNT = 0;
		}
		return false;
	}

	private void checkScreen(String skyblockId) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (!(currentScreen instanceof GenericContainerScreen)) {
			FAIL_COUNT = 0;
			FAILED_ITEM = skyblockId;
		}
	}

	@Override
	public Result handle(Context context) {
		EntryIngredient ingredient = context.getDisplay().getOutputEntries().getFirst();
		EntryStack<?> entryStack = ingredient.getFirst();
		if (!(entryStack.getValue() instanceof ItemStack itemStack)) return Result.createNotApplicable();

		String skyblockId = itemStack.getSkyblockId();
		if (hasFailed(skyblockId)) return Result.createFailed(Text.translatable("skyblocker.rei.transfer.failed"));
		if (!context.isActuallyCrafting()) return Result.createSuccessful();

		MessageScheduler.INSTANCE.sendMessageAfterCooldown("/viewrecipe " + skyblockId, false);
		Scheduler.INSTANCE.schedule(() -> checkScreen(skyblockId), 5);
		return Result.createSuccessful();
	}
}
