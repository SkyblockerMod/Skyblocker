package de.hysky.skyblocker.config.configs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.SlotSwap.SlotSquare;
import de.hysky.skyblocker.skyblock.item.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextMode;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static de.hysky.skyblocker.skyblock.SlotSwap.INVENTORY_SLOT_COUNT;

public class GeneralConfig {
    @SerialEntry
    public boolean enableTips = true;

    @SerialEntry
    public boolean acceptReparty = true;

    @SerialEntry
    public Shortcuts shortcuts = new Shortcuts();

    @SerialEntry
    public QuiverWarning quiverWarning = new QuiverWarning();

    @SerialEntry
    public ItemList itemList = new ItemList();

    @SerialEntry
    public ItemTooltip itemTooltip = new ItemTooltip();

    @SerialEntry
    public ItemInfoDisplay itemInfoDisplay = new ItemInfoDisplay();

    @SerialEntry
    public ItemProtection itemProtection = new ItemProtection();

    @SerialEntry
    public WikiLookup wikiLookup = new WikiLookup();

    @SerialEntry
    public SpecialEffects specialEffects = new SpecialEffects();

    @SerialEntry
    public Hitbox hitbox = new Hitbox();

    @SerialEntry
    public List<Integer> lockedSlots = new ArrayList<>();

    @SerialEntry
    public SlotSwap slotSwap = new SlotSwap();

    //maybe put this 5 somewhere else
    @SerialEntry
    public ObjectOpenHashSet<String> protectedItems = new ObjectOpenHashSet<>();

    @SerialEntry
    public Object2ObjectOpenHashMap<String, Text> customItemNames = new Object2ObjectOpenHashMap<>();

    @SerialEntry
    public Object2IntOpenHashMap<String> customDyeColors = new Object2IntOpenHashMap<>();

    @SerialEntry
    public Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customArmorTrims = new Object2ObjectOpenHashMap<>();

    @SerialEntry
    public Object2ObjectOpenHashMap<String, CustomArmorAnimatedDyes.AnimatedDye> customAnimatedDyes = new Object2ObjectOpenHashMap<>();

    public static class Shortcuts {
        @SerialEntry
        public boolean enableShortcuts = true;

        @SerialEntry
        public boolean enableCommandShortcuts = true;

        @SerialEntry
        public boolean enableCommandArgShortcuts = true;
    }

    public static class QuiverWarning {
        @SerialEntry
        public boolean enableQuiverWarning = true;

        @SerialEntry
        public boolean enableQuiverWarningInDungeons = true;

        @SerialEntry
        public boolean enableQuiverWarningAfterDungeon = true;
    }

    public static class ItemList {
        @SerialEntry
        public boolean enableItemList = true;
    }

    public static class ItemTooltip {
        @SerialEntry
        public boolean enableNPCPrice = true;

        @SerialEntry
        public boolean enableMotesPrice = true;

        @SerialEntry
        public boolean enableAvgBIN = true;

        @SerialEntry
        public Average avg = Average.THREE_DAY;

        @SerialEntry
        public boolean enableLowestBIN = true;

        @SerialEntry
        public boolean enableBazaarPrice = true;

        @SerialEntry
        public Craft enableCraftingCost = Craft.OFF;

        @SerialEntry
        public boolean enableObtainedDate = true;

        @SerialEntry
        public boolean enableMuseumInfo = true;

        @SerialEntry
        public boolean enableExoticTooltip = true;

        @SerialEntry
        public boolean enableAccessoriesHelper = true;

        @SerialEntry
        public boolean dungeonQuality = true;

        @SerialEntry
        public boolean showEssenceCost = false;
    }

    public enum Average {
        ONE_DAY, THREE_DAY, BOTH;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.general.itemTooltip.avg." + name());
        }
    }

    public enum Craft {
        SELL_ORDER, BUY_ORDER, OFF;

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.general.itemTooltip.craft." + name());
        }

        public String getOrder() {
            return switch (this) {
                case SELL_ORDER -> "sellPrice";
                case BUY_ORDER -> "buyPrice";
                case OFF -> null;
            };
        }
    }

    public static class ItemInfoDisplay {
        @Deprecated
        @SerialEntry
        public boolean slotText = true;

        @SerialEntry
        public SlotTextMode slotTextMode = SlotTextMode.ENABLED;

        @SerialEntry
        public boolean slotTextToggled = true;

        @SerialEntry
        public boolean attributeShardInfo = true;

        @SerialEntry
        public boolean itemRarityBackgrounds = false;

        @SerialEntry
        public RarityBackgroundStyle itemRarityBackgroundStyle = RarityBackgroundStyle.CIRCULAR;

        @SerialEntry
        public float itemRarityBackgroundsOpacity = 1f;
    }

    public enum RarityBackgroundStyle {
        CIRCULAR(Identifier.of(SkyblockerMod.NAMESPACE, "item_rarity_background_circular")),
        SQUARE(Identifier.of(SkyblockerMod.NAMESPACE, "item_rarity_background_square"));

        public final Identifier tex;

        RarityBackgroundStyle(Identifier tex) {
            this.tex = tex;
        }

        @Override
        public String toString() {
            return switch (this) {
                case CIRCULAR -> "Circular";
                case SQUARE -> "Square";
            };
        }
    }

    public static class ItemProtection {
        @SerialEntry
        public SlotLockStyle slotLockStyle = SlotLockStyle.FANCY;
    }

    public enum SlotLockStyle {
        CLASSIC(Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png")),
        FANCY(Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/fancy_slot_lock.png"));

        public final Identifier tex;

        SlotLockStyle(Identifier tex) {
            this.tex = tex;
        }

        @Override
        public String toString() {
            return switch (this) {
                case CLASSIC -> "Classic";
                case FANCY -> "FANCY";
            };
        }
    }

    public static class WikiLookup {
        @SerialEntry
        public boolean enableWikiLookup = true;

        @SerialEntry
        public boolean officialWiki = true;
    }

    public static class SpecialEffects {
        @SerialEntry
        public boolean rareDungeonDropEffects = true;
    }

    public static class Hitbox {
        @SerialEntry
        public boolean oldFarmlandHitbox = false;

        @SerialEntry
        public boolean oldLeverHitbox = false;
    }

    public static class SlotSwap {
        @SerialEntry
        public boolean enableSlotSwap = true;
        @SerialEntry
        public BiMap<@Nullable SlotSquare, @Nullable SlotSquare> slotSquareMap = HashBiMap.create(INVENTORY_SLOT_COUNT);
        @SerialEntry
        public Color sourceSlotColor = new Color(0xE86DFF);
        @SerialEntry
        public Color targetSlotColor = new Color(0xFFBB00);
    }
}
