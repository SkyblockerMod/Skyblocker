package de.hysky.skyblocker.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import io.github.moulberry.repo.NEUConstants;
import io.github.moulberry.repo.NEURecipeCache;
import io.github.moulberry.repo.NEURepoFile;
import io.github.moulberry.repo.NEURepository;
import io.github.moulberry.repo.data.NEUItem;
import io.github.moulberry.repo.data.NEURecipe;
import io.github.moulberry.repo.util.NEUId;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.function.Consumers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Initializes the NEU repo, which contains item metadata and fairy souls location data. Clones the repo if it does not exist and checks for updates. Use {@link #runAsyncAfterLoad(Runnable)} to run code after the repo is initialized.
 */
public class NEURepoManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(NEURepoManager.class);
	private static final String REMOTE_REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git";
	/**
	 * @see #NEU_REPO
	 */
	private static final Path LOCAL_REPO_DIR = SkyblockerMod.CONFIG_DIR.resolve("item-repo"); // TODO rename to NotEnoughUpdates-REPO
	/**
	 * @see #isLoading()
	 */
	private static CompletableFuture<Void> REPO_LOADING = loadRepository().thenAccept(Consumers.nop());
	/**
	 * Use public methods instead of this field.
	 *
	 * @see #getItemByName(String)
	 * @see #getConstants()
	 */
	private static final NEURepository NEU_REPO = NEURepository.of(LOCAL_REPO_DIR);
	/**
	 * @see #getRecipes()
	 * @see #getUsages()
	 */
	private static final NEURecipeCache RECIPE_CACHE = NEURecipeCache.forRepo(NEU_REPO);
	/**
	 * Store after load runnables so we can execute them after each time the repository is (re)loaded.
	 */
	private static final List<Runnable> afterLoadTasks = new ArrayList<>();
	/**
	 * A cache containing NEUItems indexed by their display name.
	 */
	private static Multimap<String, NEUItem> nameToNEUItem = HashMultimap.create();

	/**
	 * Adds command to update the repository manually from ingame.
	 * <p></p>
	 * TODO A button could be added to the settings menu that will trigger this command.
	 */
	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("updateRepository").executes(context -> {
							deleteAndDownloadRepository(context.getSource().getPlayer());
							return Command.SINGLE_SUCCESS;
						}))
				)
		);
		runAsyncAfterLoad(NEURepoManager::loadNameToNEUItemMap); // Loads the NEUItem name cache after the repository is loaded.
	}


	public static boolean isLoading() {
		return REPO_LOADING != null && !REPO_LOADING.isDone();
	}

	private static CompletableFuture<Boolean> loadRepository() {
		return CompletableFuture.supplyAsync(() -> {
			boolean success = true;
			try {
				if (Files.isDirectory(NEURepoManager.LOCAL_REPO_DIR)) {
					try (Git localRepo = Git.open(NEURepoManager.LOCAL_REPO_DIR.toFile())) {
						boolean stashed = localRepo.stashCreate().call() != null;
						localRepo.fetch()
								.setRefSpecs("+refs/heads/master:refs/remotes/origin/master")
								.setThin(true)
								.setDepth(1)
								.call();
						Ref ref = localRepo.reset()
								.setRef("refs/remotes/origin/master")
								.setMode(ResetCommand.ResetType.HARD)
								.disableRefLog(true)
								.call();
						if (stashed) {
							localRepo.stashApply()
									.setContentMergeStrategy(ContentMergeStrategy.THEIRS)
									.call();
							localRepo.stashDrop().call();
							LOGGER.info("[Skyblocker NEU Repo] Auto stash has been applied to the NEU Repository");
						}
						LOGGER.info("[Skyblocker NEU Repo] NEU Repository was updated to {}", ref.getObjectId().getName());
					}
				} else {
					Git.cloneRepository()
							.setURI(REMOTE_REPO_URL)
							.setDirectory(NEURepoManager.LOCAL_REPO_DIR.toFile())
							.setBranchesToClone(List.of("refs/heads/master"))
							.setBranch("refs/heads/master")
							.setDepth(1)
							.call().close();
					LOGGER.info("[Skyblocker NEU Repo] NEU Repository Downloaded");
				}
			} catch (TransportException e) {
				LOGGER.error("[Skyblocker NEU Repo] Transport operation failed. Most likely unable to connect to the remote NEU repo on github", e);
				success = false;
			} catch (GitAPIException | RepositoryNotFoundException e) {
				LOGGER.warn("[Skyblocker NEU Repo] Local NEU Repository not found or corrupted, downloading new one", e);
				Scheduler.INSTANCE.schedule(() -> deleteAndDownloadRepositoryInternal(MinecraftClient.getInstance().player), 1);
				success = false;
			} catch (Exception e) {
				LOGGER.error("[Skyblocker NEU Repo] Encountered unknown exception while downloading NEU Repository", e);
				success = false;
			}

			try {
				NEU_REPO.reload();
			} catch (Exception e) {
				LOGGER.error("[Skyblocker NEU Repo] Encountered unknown exception while loading NEU Repository", e);
				success = false;
			}
			return success;
		}).thenApplyAsync(success -> {
			CompletableFuture.allOf(afterLoadTasks.stream().map(CompletableFuture::runAsync).toArray(CompletableFuture[]::new)).exceptionally(e -> {
				LOGGER.error("[Skyblocker NEU Repo] Encountered unknown exception while running after load tasks", e);
				return null;
			});
			return success;
		});
	}

	/**
	 * Caches NEUItems by display name using the NEU repository.
	 */
	private static void loadNameToNEUItemMap() {
		nameToNEUItem = NEU_REPO.getItems()
				.getItems()
				.values()
				.stream()
				.collect(Multimaps.toMultimap(item -> Formatting.strip(item.getDisplayName()), Function.identity(), HashMultimap::create));
	}

	/**
	 * Differs from {@link #deleteAndDownloadRepositoryInternal(PlayerEntity)} in that this method checks if the repository is currently loading to prevent spamming the command.
	 */
	private static void deleteAndDownloadRepository(PlayerEntity player) {
		if (isLoading()) {
			sendMessage(player, Text.translatable("skyblocker.updateRepository.loading"));
			return;
		}
		deleteAndDownloadRepositoryInternal(player);
	}

	private static void deleteAndDownloadRepositoryInternal(PlayerEntity player) {
		Function<Runnable, CompletableFuture<Void>> runner = isLoading() ? REPO_LOADING::thenRunAsync : CompletableFuture::runAsync;
		REPO_LOADING = runner.apply(() -> {
			sendMessage(player, Text.translatable("skyblocker.updateRepository.start"));
			try {
				FileUtils.recursiveDelete(NEURepoManager.LOCAL_REPO_DIR);
				sendMessage(player, Text.translatable("skyblocker.updateRepository.deleted"));
				sendMessage(player, Text.translatable(loadRepository().join() ? "skyblocker.updateRepository.success" : "skyblocker.updateRepository.failed"));
			} catch (Exception e) {
				LOGGER.error("[Skyblocker NEU Repo] Encountered unknown exception while deleting the NEU repo", e);
				sendMessage(player, Text.translatable("skyblocker.updateRepository.error"));
			}
		});
	}

	private static void sendMessage(PlayerEntity player, Text text) {
		if (player != null) {
			player.sendMessage(Constants.PREFIX.get().append(text), false);
		} else {
			LOGGER.info("[Skyblocker NEU Repo] {}", text.getString());
		}
	}

	/**
	 * Runs the given runnable after the NEU repo is initialized.
	 *
	 * @param runnable the runnable to run
	 * @return a completable future of the given runnable
	 */
	public static CompletableFuture<Void> runAsyncAfterLoad(Runnable runnable) {
		return REPO_LOADING.thenRunAsync(runnable).exceptionally(e -> {
			LOGGER.error("[Skyblocker NEU Repo] Encountered unknown exception while running after load task", e);
			return null;
		}).thenRun(() -> afterLoadTasks.add(runnable)); // Add to the list after so it doesn't get executed twice.
	}

	public static void forEachItem(Consumer<NEUItem> consumer) {
		NEU_REPO.getItems().getItems().values().forEach(consumer);
	}

	public static @Nullable NEUItem getItemByNeuId(String neuId) {
		return NEU_REPO.getItems().getItemBySkyblockId(neuId);
	}

	/**
	 * Gets the {@link NEUItem} by display name.
	 */
	public static Collection<NEUItem> getItemByName(String displayName) {
		return nameToNEUItem.get(displayName);
	}

	public static NEUConstants getConstants() {
		return NEU_REPO.getConstants();
	}

	public static @Nullable NEURepoFile file(@NotNull String path) {
		return NEU_REPO.file(path);
	}

	public static Map<@NEUId String, Set<NEURecipe>> getRecipes() {
		return RECIPE_CACHE.getRecipes();
	}

	public static Map<@NEUId String, Set<NEURecipe>> getUsages() {
		return RECIPE_CACHE.getUsages();
	}
}
