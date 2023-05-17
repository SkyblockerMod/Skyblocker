package me.xmrvizzy.skyblocker.skyblock.api;

import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class RepositoryUpdate {

    /**
     * Adds command to update repository manually from ingame.
     * <p></p>
     * TODO A button could be added to the settings menu that will trigger this command.
     */
    public static void init(){
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("updaterepository")
                        .executes(context -> {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    ItemRegistry.filesImported = false;
                                    File dir = ItemRegistry.LOCAL_ITEM_REPO_DIR.toFile();
                                    recursiveDelete(dir);
                                } catch (Exception ex) {
                                    ItemRegistry.client.player.sendMessage(
                                            Text.translatable("skyblocker.updaterepository.failed")
                                            , false
                                    );
                                    return;
                                }

                                ItemRegistry.init();
                            });

                            return 1;
                        })
                )
            )
        );

    }

    private static void recursiveDelete(File dir) {
        if (dir.isDirectory() && !Files.isSymbolicLink(dir.toPath())) {
            for (File child : dir.listFiles()) {
                recursiveDelete(child);
            }
        }
        dir.delete();
    }
}
