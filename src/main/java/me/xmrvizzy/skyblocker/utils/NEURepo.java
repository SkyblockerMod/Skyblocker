package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NEURepo {
    private static final Logger LOGGER = LoggerFactory.getLogger(NEURepo.class);
    public static final String REMOTE_REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git";
    public static final Path LOCAL_REPO_DIR = SkyblockerMod.CONFIG_DIR.resolve("item-repo");
    private static final CompletableFuture<Void> REPO_INITIALIZED = initRepository();

    /**
     * Adds command to update repository manually from ingame.
     * <p></p>
     * TODO A button could be added to the settings menu that will trigger this command.
     */
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
                        .then(ClientCommandManager.literal("updaterepository").executes(context -> {
                            deleteAndDownloadRepository();
                            return 1;
                        }))));
    }

    public static CompletableFuture<Void> initRepository() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (Files.isDirectory(NEURepo.LOCAL_REPO_DIR)) {
                    try (Git localRepo = Git.open(NEURepo.LOCAL_REPO_DIR.toFile())) {
                        localRepo.pull().setRebase(true).call();
                        LOGGER.info("[Skyblocker] NEU Repository Updated");
                    }
                } else {
                    Git.cloneRepository().setURI(REMOTE_REPO_URL).setDirectory(NEURepo.LOCAL_REPO_DIR.toFile()).setBranchesToClone(List.of("refs/heads/master")).setBranch("refs/heads/master").call().close();
                    LOGGER.info("[Skyblocker] NEU Repository Downloaded");
                }
            } catch (RepositoryNotFoundException e) {
                LOGGER.warn("Local NEU Repository not found or corrupted, downloading new one", e);
                deleteAndDownloadRepository();
            } catch (Exception e) {
                LOGGER.error("Encountered unknown exception while initializing NEU Repository", e);
            }
        });
    }

    public static void deleteAndDownloadRepository() {
        CompletableFuture.runAsync(() -> {
            try {
                ItemRegistry.filesImported = false;
                File dir = NEURepo.LOCAL_REPO_DIR.toFile();
                recursiveDelete(dir);
            } catch (Exception ex) {
                if (MinecraftClient.getInstance().player != null)
                    MinecraftClient.getInstance().player.sendMessage(Text.translatable("skyblocker.updaterepository.failed"), false);
                return;
            }
            initRepository();
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void recursiveDelete(File dir) {
        File[] children;
        if (dir.isDirectory() && !Files.isSymbolicLink(dir.toPath()) && (children = dir.listFiles()) != null) {
            for (File child : children) {
                recursiveDelete(child);
            }
        }
        dir.delete();
    }

    public static CompletableFuture<Void> runAsyncAfterLoad(Runnable runnable) {
        return REPO_INITIALIZED.thenRunAsync(runnable);
    }
}
