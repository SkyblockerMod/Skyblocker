package de.hysky.skyblocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.*;
import de.hysky.skyblocker.skyblock.waypoint.MythologicalRitual;
import de.hysky.skyblocker.skyblock.dungeon.*;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonSecrets;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretsTracker;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud;
import de.hysky.skyblocker.skyblock.item.*;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.rift.TheRift;
import de.hysky.skyblocker.skyblock.shortcut.Shortcuts;
import de.hysky.skyblocker.skyblock.special.SpecialEffects;
import de.hysky.skyblocker.skyblock.waypoint.Relics;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.waypoint.FairySouls;
import de.hysky.skyblocker.utils.ApiUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatMessageListener;
import de.hysky.skyblocker.utils.discord.DiscordRPCManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.render.gui.ContainerSolverManager;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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
    public static final String VERSION = FabricLoader.getInstance().getModContainer("skyblocker").orElseThrow().getMetadata().getVersion().getFriendlyString();
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
        SkyblockerConfigManager.init();
        NEURepoManager.init();
        ItemRepository.init();
        HotbarSlotLock.init();
        ItemTooltip.init();
        WikiLookup.init();
        FairySouls.init();
        Relics.init();
        MythologicalRitual.init();
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
        ChestValue.init();
        FireFreezeStaffTimer.init();
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
        ItemProtection.init();
        CreeperBeams.init();
        ItemRarityBackgrounds.init();
        MuseumItemCache.init();
        SecretsTracker.init();
        ApiUtils.init();
        Debug.init();
        RenderHelper.init();
        containerSolverManager.init();
        statusBarTracker.init();
        Scheduler.INSTANCE.scheduleCyclic(Utils::update, 20);
        Scheduler.INSTANCE.scheduleCyclic(DiscordRPCManager::updateDataAndPresence, 200);
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
