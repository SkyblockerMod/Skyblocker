package de.hysky.skyblocker.skyblock.itemlist;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockCraftingRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockForgeRecipe;
import de.hysky.skyblocker.skyblock.itemlist.recipes.SkyblockRecipe;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUCraftingRecipe;
import io.github.moulberry.repo.data.NEUForgeRecipe;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.NEURecipe;
import io.github.moulberry.repo.util.NEUId;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemRepository {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ItemRepository.class);

	private static final List<ItemStack> items = new ArrayList<>();
	private static final Map<String, ItemStack> itemsMap = new HashMap<>();
	private static final List<SkyblockRecipe> recipes = new ArrayList<>();
	private static final HashMap<String, @NEUId String> bazaarStocks = new HashMap<>();
	/**
	 * Store callbacks so we can execute them each time the item repository
	 * finishes loading.
	 */
	private static final List<AfterImportTask> afterImportTasks = new CopyOnWriteArrayList<>();

	private record AfterImportTask(Runnable runnable, boolean async) {}

	/**
	 * Consumers must check this field when accessing `items` and `itemsMap`, or else thread safety is not guaranteed.
	 */
	private static boolean itemsImported = false;
	/**
	 * Consumers must check this field when accessing `recipes`, or else thread safety is not guaranteed.
	 */
	private static boolean filesImported = false;

	@Init
	public static void init() {
		NEURepoManager.runAsyncAfterLoad(ItemStackBuilder::loadPetNums);
		NEURepoManager.runAsyncAfterLoad(ItemRepository::importItemFiles);
		NEURepoManager.runAsyncAfterLoad(ItemRepository::loadBazaarStocks);
		runAsyncAfterImport(ItemRepository::handleRecipeSynchronization);
		SkyblockEvents.JOIN.register(ItemRepository::handleRecipeSynchronization);
	}

	/**
	 * Load the recipes manually because Hypixel doesn't send any vanilla recipes to the client.
	 * This also reloads REI to include the Skyblock items when the items are done loading.
	 */
	private static void handleRecipeSynchronization() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.getNetworkHandler() == null) return;

		SynchronizeRecipesS2CPacket packet = new SynchronizeRecipesS2CPacket(Map.of(), CuttingRecipeDisplay.Grouping.empty());
		try {
			client.execute(() -> client.getNetworkHandler().onSynchronizeRecipes(packet));
		} catch (Exception e) {
			LOGGER.info("[Skyblocker Item Repo] recipe sync error", e);
		}
	}

	private static void importItemFiles() {
		itemsImported = false;
		filesImported = false;

		items.clear();
		itemsMap.clear();
		recipes.clear();

		NEURepoManager.forEachItem(ItemRepository::loadItem);
		items.sort(Comparator.<ItemStack, String>comparing(stack -> ItemUtils.getItemId(stack).replaceAll(".\\d+$", ""))
				.thenComparingInt(stack -> ItemUtils.getItemId(stack).length())
				.thenComparing(ItemUtils::getItemId)
		);
		itemsImported = true;

		NEURepoManager.forEachItem(ItemRepository::loadRecipes);
		filesImported = true;

		afterImportTasks.forEach(task -> {
			if (task.async) {
				CompletableFuture.runAsync(task.runnable).exceptionally(e -> {
					LOGGER.error("[Skyblocker Item Repo Loader] Encountered unknown exception while running after import tasks", e);
					return null;
				});
			} else {
				try {
					task.runnable.run();
				} catch (Exception e) {
					LOGGER.error("[Skyblocker Item Repo Loader] Encountered unknown exception while running after import tasks", e);
				}
			}
		});
	}

	private static void loadItem(NEUItem item) {
		try {
			ItemStack stack = ItemStackBuilder.fromNEUItem(item);
			StackOverlays.applyOverlay(item, stack);

			if (stack.isOf(Items.ENCHANTED_BOOK) && ItemUtils.getItemId(stack).contains(";")) {
				ItemUtils.getCustomData(stack).putString("id", "ENCHANTED_BOOK");
			}

			items.add(stack);
			itemsMap.put(item.getSkyblockItemId(), stack);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Item Repo Loader] Failed to load item, please report this! Skyblock Id: {}", item.getSkyblockItemId(), e);
		}
	}

	private static void loadRecipes(NEUItem item) {
		item.getRecipes().stream().map(ItemRepository::toSkyblockRecipe).filter(Objects::nonNull).forEach(recipes::add);
	}

	private static void loadBazaarStocks() {
		bazaarStocks.clear();
		NEURepoManager.getConstants().getBazaarStocks().getStocks().forEach((String neuId, String skyblockId) -> bazaarStocks.put(skyblockId, neuId));
	}

	public static String getWikiLink(String neuId, boolean useOfficial) {
		NEUItem item = NEURepoManager.getItemByNeuId(neuId);
		if (item == null || item.getInfo() == null || item.getInfo().isEmpty()) {
			return null;
		}

		List<String> info = item.getInfo();
		String wikiLink0 = info.getFirst();
		String wikiLink1 = info.size() > 1 ? info.get(1) : "";
		String wikiDomain = getWikiLink(useOfficial);
		if (wikiLink0.startsWith(wikiDomain)) {
			return wikiLink0;
		} else if (wikiLink1.startsWith(wikiDomain)) {
			return wikiLink1;
		}
		return null;
	}

	public static String getWikiLink(boolean useOfficial) {
		return useOfficial ? "https://wiki.hypixel.net" : "https://hypixel-skyblock.fandom.com";
	}

	public static List<SkyblockRecipe> getRecipesAndUsages(ItemStack stack) {
		return Stream.concat(getRecipes(stack), getUsages(stack)).toList();
	}

	public static boolean filesImported() {
		return filesImported;
	}

	public static List<ItemStack> getItems() {
		return itemsImported ? items : List.of();
	}

	public static Stream<ItemStack> getItemsStream() {
		return itemsImported ? items.stream() : Stream.empty();
	}

	public static Map<String, @NEUId String> getBazaarStocks() {
		// This is not protected by `filesImported` because it is loaded asynchronously separately from `items`, `itemsMap`, and `recipes`.
		return bazaarStocks;
	}

	/**
	 * @param neuId the NEU item id gotten through {@link NEUItem#getSkyblockItemId()}, {@link ItemStack#getNeuName()}, or {@link ItemUtils#getNeuId(ItemStack) ItemTooltip#getNeuName(String, String)}
	 */
	@Nullable
	public static ItemStack getItemStack(String neuId) {
		return itemsImported ? itemsMap.get(neuId) : null;
	}

	/**
	 * @param neuId the NEU item id gotten through {@link NEUItem#getSkyblockItemId()}, {@link ItemStack#getNeuName()}, or {@link ItemUtils#getNeuId(ItemStack) ItemTooltip#getNeuName(String, String)}
	 */
	public static Supplier<ItemStack> getItemStackSupplier(String neuId) {
		return () -> itemsMap.get(neuId);
	}

	public static Stream<SkyblockRecipe> getRecipesStream() {
		return filesImported ? recipes.stream() : Stream.empty();
	}

	public static Stream<SkyblockRecipe> getRecipes(ItemStack stack) {
		return NEURepoManager.getRecipes().getOrDefault(stack.getNeuName(), Set.of()).stream().map(ItemRepository::toSkyblockRecipe).filter(Objects::nonNull);
	}

	public static Stream<SkyblockRecipe> getUsages(ItemStack stack) {
		return NEURepoManager.getUsages().getOrDefault(stack.getNeuName(), Set.of()).stream().map(ItemRepository::toSkyblockRecipe).filter(Objects::nonNull);
	}

	private static SkyblockRecipe toSkyblockRecipe(NEURecipe neuRecipe) {
		return switch (neuRecipe) {
			case NEUCraftingRecipe craftingRecipe -> new SkyblockCraftingRecipe(craftingRecipe);
			case NEUForgeRecipe forgeRecipe -> new SkyblockForgeRecipe(forgeRecipe);
			case null, default -> null;
		};
	}

	/**
	 * Runs the given runnable after the item repository has finished loading.
	 * If the repository is already loaded the runnable is executed immediately.
	 *
	 * @param runnable the runnable to run
	 */
	public static void runAsyncAfterImport(Runnable runnable) {
		runAfterImport(runnable, true);
	}

	/**
	 * Runs the given runnable after the item repository has finished loading.
	 * If the repository is already loaded the runnable is executed immediately.
	 *
	 * @param runnable the runnable to run
	 * @param async    whether to run the runnable asynchronously
	 */
	public static void runAfterImport(Runnable runnable, boolean async) {
		if (filesImported) {
			if (async) {
				CompletableFuture.runAsync(runnable).exceptionally(e -> {
					LOGGER.error("[Skyblocker Item Repo Loader] Encountered unknown exception while running after import task", e);
					return null;
				});
			} else {
				try {
					runnable.run();
				} catch (Exception e) {
					LOGGER.error("[Skyblocker Item Repo Loader] Encountered unknown exception while running after import task", e);
				}
			}
		}
		afterImportTasks.add(new AfterImportTask(runnable, async));
	}
}
