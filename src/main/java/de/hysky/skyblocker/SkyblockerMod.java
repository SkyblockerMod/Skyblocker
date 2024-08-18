package de.hysky.skyblocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.hysky.skyblocker.config.ConfigNullFieldsFix;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.*;
import de.hysky.skyblocker.skyblock.bazaar.BazaarHelper;
import de.hysky.skyblocker.skyblock.calculators.CalculatorCommand;
import de.hysky.skyblocker.skyblock.chat.ChatRulesHandler;
import de.hysky.skyblocker.skyblock.chat.SkyblockXpMessages;
import de.hysky.skyblocker.skyblock.chocolatefactory.EggFinder;
import de.hysky.skyblocker.skyblock.chocolatefactory.TimeTowerReminder;
import de.hysky.skyblocker.skyblock.crimson.dojo.DojoManager;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.dungeon.*;
import de.hysky.skyblocker.skyblock.dungeon.device.LightsOn;
import de.hysky.skyblocker.skyblock.dungeon.device.SimonSays;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.*;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.boulder.Boulder;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretsTracker;
import de.hysky.skyblocker.skyblock.dwarven.*;
import de.hysky.skyblocker.skyblock.end.BeaconHighlighter;
import de.hysky.skyblocker.skyblock.end.EnderNodes;
import de.hysky.skyblocker.skyblock.end.TheEnd;
import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.skyblock.garden.FarmingHud;
import de.hysky.skyblocker.skyblock.garden.LowerSensitivity;
import de.hysky.skyblocker.skyblock.garden.VisitorHelper;
import de.hysky.skyblocker.skyblock.item.*;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.AccessoriesHelper;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.mayors.JerryTimer;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerScreen;
import de.hysky.skyblocker.skyblock.rift.TheRift;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;
import de.hysky.skyblocker.skyblock.shortcut.Shortcuts;
import de.hysky.skyblocker.skyblock.slayers.SlayerEntitiesGlow;
import de.hysky.skyblocker.skyblock.special.SpecialEffects;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.waypoint.*;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.chat.ChatMessageListener;
import de.hysky.skyblocker.utils.discord.DiscordRPCManager;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.ws.SkyblockerWebSocket;
import de.hysky.skyblocker.utils.ws.WsStateManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

/**
 * Main class for Skyblocker which initializes features, registers events, and
 * manages ticks. This class will be instantiated by Fabric. Do not instantiate
 * this class.
 */
public class SkyblockerMod implements ClientModInitializer {
    public static final String NAMESPACE = "skyblocker";
    public static final ModContainer SKYBLOCKER_MOD = FabricLoader.getInstance().getModContainer(NAMESPACE).orElseThrow();
    public static final String VERSION = SKYBLOCKER_MOD.getMetadata().getVersion().getFriendlyString();
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON_COMPACT = new GsonBuilder().create();
    private static SkyblockerMod INSTANCE;
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
        ConfigDataFixer.apply();
        Utils.init();
        SkyblockerConfigManager.init();
        ConfigNullFieldsFix.init(); //DO NOT INIT ANY CLASS THAT USES CONFIG FIELDS BEFORE THIS!
        SkyblockerScreen.initClass();
        ProfileViewerScreen.initClass();
        Tips.init();
        UpdateNotifications.init();
        NEURepoManager.init();
        //ImageRepoLoader.init();
        ItemRepository.init();
        SkyblockItemData.init();
        HotbarSlotLock.init();
        ItemTooltip.init();
        AccessoriesHelper.init();
        WikiLookup.init();
        Waypoints.init();
        FairySouls.init();
        Relics.init();
        MythologicalRitual.init();
        EnderNodes.init();
        OrderedWaypoints.init();
        BackpackPreview.init();
        ItemCooldowns.init();
        TabHud.init();
        GlaciteColdOverlay.init();
        DwarvenHud.init();
        CommissionLabels.init();
        CrystalsHud.init();
        FarmingHud.init();
        LowerSensitivity.init();
        CrystalsLocationsManager.init();
        WishingCompassSolver.init();
        CrystalsChestHighlighter.init();
        MetalDetector.init();
        ChatMessageListener.init();
        Shortcuts.init();
        ChatRulesHandler.init();
        SkyblockXpMessages.init();
        CalculatorCommand.init();
        DiscordRPCManager.init();
        LividColor.init();
        FishingHelper.init();
        DungeonMap.init();
        DungeonScoreHUD.init();
        DungeonManager.init();
        DungeonBlaze.init();
        Waterboard.init();
        Silverfish.init();
        IceFill.init();
        DungeonScore.init();
        SimonSays.init();
        LightsOn.init();
        PartyFinderScreen.initClass();
        ChestValue.init();
        FireFreezeStaffTimer.init();
        GuardianHealth.init();
        TheRift.init();
        TheEnd.init();
        SearchOverManager.init();
        TitleContainer.init();
        ScreenMaster.init();
        DungeonTextures.init();
        OcclusionCulling.init();
        TeleportOverlay.init();
        CustomItemNames.init();
        CustomArmorDyeColors.init();
        CustomArmorAnimatedDyes.init();
        CustomArmorTrims.init();
        TicTacToe.init();
        QuiverWarning.init();
        SpecialEffects.init();
        ItemProtection.init();
        CreeperBeams.init();
        Boulder.init();
        ThreeWeirdos.init();
        VisitorHelper.init();
        ItemRarityBackgrounds.init();
        MuseumItemCache.init();
        PetCache.init();
        SecretsTracker.init();
        ApiAuthentication.init();
        ApiUtils.init();
        SkyblockerWebSocket.init();
        WsStateManager.init();
        Debug.init();
        Kuudra.init();
        DojoManager.init();
        RenderHelper.init();
        FancyStatusBars.init();
        SkyblockInventoryScreen.initEquipment();
        EventNotifications.init();
        ContainerSolverManager.init();
        statusBarTracker.init();
        BeaconHighlighter.init();
        WarpAutocomplete.init();
        MobBoundingBoxes.init();
        EggFinder.init();
        TimeTowerReminder.init();
        SkyblockTime.init();
        JerryTimer.init();
        TooltipManager.init();
        SlotTextManager.init();
        BazaarHelper.init();
        MobGlow.init();
        MayorUtils.init();
        SlayerEntitiesGlow.init();

        Scheduler.INSTANCE.scheduleCyclic(Utils::update, 20);
        Scheduler.INSTANCE.scheduleCyclic(DiscordRPCManager::updateDataAndPresence, 200);
        Scheduler.INSTANCE.scheduleCyclic(BackpackPreview::tick, 50);
        Scheduler.INSTANCE.scheduleCyclic(DwarvenHud::update, 40);
        Scheduler.INSTANCE.scheduleCyclic(CrystalsHud::update, 40);
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
