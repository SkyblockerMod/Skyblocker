package me.xmrvizzy.skyblocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.*;
import me.xmrvizzy.skyblocker.skyblock.item.ItemCooldowns;
import me.xmrvizzy.skyblocker.skyblock.dungeon.*;
import me.xmrvizzy.skyblocker.skyblock.dungeon.secrets.DungeonSecrets;
import me.xmrvizzy.skyblocker.skyblock.dwarven.DwarvenHud;
import me.xmrvizzy.skyblocker.skyblock.item.*;
import me.xmrvizzy.skyblocker.skyblock.itemlist.ItemRegistry;
import me.xmrvizzy.skyblocker.skyblock.quicknav.QuickNav;
import me.xmrvizzy.skyblocker.skyblock.rift.TheRift;
import me.xmrvizzy.skyblocker.skyblock.shortcut.Shortcuts;
import me.xmrvizzy.skyblocker.skyblock.special.SpecialEffects;
import me.xmrvizzy.skyblocker.skyblock.spidersden.Relics;
import me.xmrvizzy.skyblocker.skyblock.tabhud.TabHud;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.utils.NEURepo;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.chat.ChatMessageListener;
import me.xmrvizzy.skyblocker.utils.discord.DiscordRPCManager;
import me.xmrvizzy.skyblocker.utils.render.culling.OcclusionCulling;
import me.xmrvizzy.skyblocker.utils.render.gui.ContainerSolverManager;
import me.xmrvizzy.skyblocker.utils.render.title.TitleContainer;
import me.xmrvizzy.skyblocker.utils.scheduler.MessageScheduler;
import me.xmrvizzy.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

/**
 * Main class for Skyblocker which initializes features, registers events, and
 * manages ticks. This class will be instantiated by Fabric. Do not instantiate
 * this class.
 */
public class SkyblockerMod implements ClientModInitializer {
    public static final String VERSION = FabricLoader.getInstance().getModContainer("skyblocker").get().getMetadata().getVersion().getFriendlyString();
    public static final String NAMESPACE = "skyblocker";
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SkyblockerMod INSTANCE;
    public final ContainerSolverManager containerSolverManager = new ContainerSolverManager();
    public final StatusBarTracker statusBarTracker = new StatusBarTracker();

    /**
     * Do not instantiate this class. Use {@link #getInstance()} instead.
     */
    @Deprecated
    public SkyblockerMod() {
        INSTANCE = this;
    }

    public static SkyblockerMod getInstance() {
        return INSTANCE;
    }

    /**
     * Register {@link #tick(MinecraftClient)} to
     * {@link ClientTickEvents#END_CLIENT_TICK}, initialize all features, and
     * schedule tick events.
     */
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        Utils.init();
        HotbarSlotLock.init();
        SkyblockerConfig.init();
        PriceInfoTooltip.init();
        WikiLookup.init();
        ItemRegistry.init();
        NEURepo.init();
        FairySouls.init();
        Relics.init();
        BackpackPreview.init();
        QuickNav.init();
        ItemCooldowns.init();
        DwarvenHud.init();
        ChatMessageListener.init();
        Shortcuts.init();
        DiscordRPCManager.init();
        LividColor.init();
        FishingHelper.init();
        TabHud.init();
        DungeonMap.init();
        DungeonSecrets.init();
        DungeonBlaze.init();
        DungeonChestProfit.init();
        TheRift.init();
        TitleContainer.init();
        ScreenMaster.init();
        OcclusionCulling.init();
        TeleportOverlay.init();
        CustomItemNames.init();
        CustomArmorDyeColors.init();
        CustomArmorTrims.init();
        TicTacToe.init();
        QuiverWarning.init();
        SpecialEffects.init();
        containerSolverManager.init();
        statusBarTracker.init();
        Scheduler.INSTANCE.scheduleCyclic(Utils::update, 20);
        Scheduler.INSTANCE.scheduleCyclic(DiscordRPCManager::updateDataAndPresence, 100);
        Scheduler.INSTANCE.scheduleCyclic(TicTacToe::tick, 4);
        Scheduler.INSTANCE.scheduleCyclic(LividColor::update, 10);
        Scheduler.INSTANCE.scheduleCyclic(BackpackPreview::tick, 50);
        Scheduler.INSTANCE.scheduleCyclic(DwarvenHud::update, 40);
        Scheduler.INSTANCE.scheduleCyclic(PlayerListMgr::updateList, 20);
    }

    /**
     * Ticks the scheduler. Called once at the end of every client tick through
     * {@link ClientTickEvents#END_CLIENT_TICK}.
     *
     * @param client the Minecraft client.
     */
    public void tick(MinecraftClient client) {
        Scheduler.INSTANCE.tick();
        MessageScheduler.INSTANCE.tick();
    }
}
