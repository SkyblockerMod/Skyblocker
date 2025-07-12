package de.hysky.skyblocker.compatibility.rei;

import de.hysky.skyblocker.compatibility.rei.info.SkyblockInfoCategory;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUForgeRecipe;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SkyblockTransferHandler implements TransferHandler {
	// This determines if the plus button should be active
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

		// Not applicable if it has 0 recipes, or no crafting/forge recipe
		String neuId = itemStack.getNeuName();
		if (!NEURepoManager.getRecipes().containsKey(neuId) || NEURepoManager.getRecipes().get(neuId).stream().noneMatch(recipe -> recipe instanceof NEUCraftingRecipe || recipe instanceof NEUForgeRecipe))
			return ApplicabilityResult.createApplicableWithError(Text.translatable("skyblocker.rei.transfer.noRecipe"));

		return ApplicabilityResult.createApplicable();

	}

	// This is run every tick
	// When the plus button is pressed context.isActuallyCrafting() becomes true
	@Override
	public Result handle(Context context) {
		EntryIngredient ingredient = context.getDisplay().getOutputEntries().getFirst();
		EntryStack<?> entryStack = ingredient.getFirst();
		if (!(entryStack.getValue() instanceof ItemStack itemStack)) return Result.createNotApplicable();
		if (!context.isActuallyCrafting()) return Result.createSuccessful();

		String skyblockId = itemStack.getSkyblockId();
		MessageScheduler.INSTANCE.sendMessageAfterCooldown("/viewrecipe " + skyblockId, false);
		// TODO: The user must look in chat to see if /viewrecipe fails, maybe add a check if it has been a few ticks after pressing
		//  (and the menu hasn't closed), return an Error result.
		return Result.createSuccessful();
	}
}
