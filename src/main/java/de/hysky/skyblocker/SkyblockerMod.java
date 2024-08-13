package de.hysky.skyblocker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.hysky.skyblocker.config.ConfigNullFieldsFix;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.datafixer.ConfigDataFixer;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.skyblock.dwarven.CrystalsHud;
import de.hysky.skyblocker.skyblock.dwarven.DwarvenHud;
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
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.skyblock.waypoint.*;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.chat.ChatMessageListener;
import de.hysky.skyblocker.utils.discord.DiscordRPCManager;
import de.hysky.skyblocker.utils.mayor.MayorUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.culling.OcclusionCulling;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.discord.DiscordRPCManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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
        SkyblockerConfigManager.init();
        ConfigNullFieldsFix.init(); //DO NOT INIT ANY CLASS THAT USES CONFIG FIELDS BEFORE THIS!

        containerSolverManager.init();
        statusBarTracker.init();

        Initializer.init();
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
