package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextMode;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GeneralConfig {
    public boolean enableTips = true;

    public boolean acceptReparty = true;

	public SpeedPresets speedPresets = new SpeedPresets();

    public Shortcuts shortcuts = new Shortcuts();

    public QuiverWarning quiverWarning = new QuiverWarning();

    public ItemList itemList = new ItemList();

    public ItemTooltip itemTooltip = new ItemTooltip();

    public ItemInfoDisplay itemInfoDisplay = new ItemInfoDisplay();

    public ItemProtection itemProtection = new ItemProtection();

    public WikiLookup wikiLookup = new WikiLookup();

    public SpecialEffects specialEffects = new SpecialEffects();

    public Hitbox hitbox = new Hitbox();

    public List<Integer> lockedSlots = new ArrayList<>();

    //maybe put this 5 somewhere else
    public ObjectOpenHashSet<String> protectedItems = new ObjectOpenHashSet<>();

    public Object2ObjectOpenHashMap<String, Text> customItemNames = new Object2ObjectOpenHashMap<>();

    public Object2IntOpenHashMap<String> customDyeColors = new Object2IntOpenHashMap<>();

    public Object2ObjectOpenHashMap<String, CustomArmorTrims.ArmorTrimId> customArmorTrims = new Object2ObjectOpenHashMap<>();

    public Object2ObjectOpenHashMap<String, CustomArmorAnimatedDyes.AnimatedDye> customAnimatedDyes = new Object2ObjectOpenHashMap<>();

	public Object2ObjectOpenHashMap<String, String> customHelmetTextures = new Object2ObjectOpenHashMap<>();

	public static class SpeedPresets {
		public boolean enableSpeedPresets = true;
	}

    public static class Shortcuts {
        public boolean enableShortcuts = true;

        public boolean enableCommandShortcuts = true;

        public boolean enableCommandArgShortcuts = true;
    }


    public static class QuiverWarning {
        public boolean enableQuiverWarning = true;

        public boolean enableQuiverWarningInDungeons = true;

        public boolean enableQuiverWarningAfterDungeon = true;
    }

    public static class ItemList {
        public boolean enableItemList = true;
    }

    public static class ItemTooltip {
        public boolean enableNPCPrice = true;

        public boolean enableMotesPrice = true;

        public boolean enableAvgBIN = true;

        public Average avg = Average.THREE_DAY;

        public boolean enableLowestBIN = true;

        public boolean enableBazaarPrice = true;

        public Craft enableCraftingCost = Craft.OFF;

        public boolean enableObtainedDate = true;

        public boolean enableMuseumInfo = true;

        public boolean enableExoticTooltip = true;

        public boolean enableAccessoriesHelper = true;

        public boolean dungeonQuality = true;

        public boolean showEssenceCost = true;

        public boolean enableEstimatedItemValue = true;

        public boolean enableStackingEnchantProgress = true;
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
        public transient boolean slotText = true;

		@Deprecated
        public transient SlotTextMode slotTextMode = SlotTextMode.ENABLED;

		@Deprecated
        public transient boolean slotTextToggled = true;

		@Deprecated
        public transient boolean attributeShardInfo = true;

		public ItemBackgroundStyle itemBackgroundStyle = ItemBackgroundStyle.SQUARE;

		public float itemBackgroundOpacity = 0.5f;

        public boolean itemRarityBackgrounds = true;

		public boolean jacobMedalBackgrounds = true;

		public boolean legacyAttributeBackgrounds = true;
    }

    public enum ItemBackgroundStyle {
        CIRCULAR(Identifier.of(SkyblockerMod.NAMESPACE, "item_background_circular")),
        SQUARE(Identifier.of(SkyblockerMod.NAMESPACE, "item_background_square"));

        public final Identifier tex;

        ItemBackgroundStyle(Identifier tex) {
            this.tex = tex;
        }

        @Override
        public String toString() {
            return I18n.translate("skyblocker.config.general.itemInfoDisplay.itemBackgroundStyle.style." + name());
        }
    }

    public static class ItemProtection {
        public SlotLockStyle slotLockStyle = SlotLockStyle.FANCY;

		public boolean displayChatNotification = true;

        public boolean protectValuableConsumables = true;
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
            return I18n.translate("skyblocker.config.general.itemProtection.slotLockStyle.style." + name());
        }
    }

    public static class WikiLookup {
        public boolean enableWikiLookup = true;

		@Deprecated
		public transient boolean officialWiki = true;
    }

    public static class SpecialEffects {
        public boolean rareDungeonDropEffects = true;

        public boolean rareDyeDropEffects = true;
    }

    public static class Hitbox {
        public boolean oldFarmlandHitbox = false;

        public boolean oldLeverHitbox = false;

		public boolean oldMushroomHitbox = false;
    }

}
