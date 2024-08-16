package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.intellij.lang.annotations.Language;

public class QuickNavigationConfig {
    @SerialEntry
    public boolean enableQuickNav = true;

    @SerialEntry
    public QuickNavItem button1 = new QuickNavItem(new ItemData(Items.DIAMOND_SWORD), "Your Skills", "/skills", "Skills");

    @SerialEntry
    public QuickNavItem button2 = new QuickNavItem(new ItemData(Items.PAINTING), "Collections", "/collection", "Collections");

    /* REGEX Explanation
     * "Pets" : simple match on letters
     * "(?: \\(\\d+/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
     */
    @SerialEntry
    public QuickNavItem button3 = new QuickNavItem(new ItemData(Items.BONE), "Pets(?: \\(\\d+/\\d+\\))?", "/pets", "Pets");

    /* REGEX Explanation
     * "Wardrobe" : simple match on letters
     * " \\([12]/2\\)" : match on the page either " (1/2)" or " (2/2)"
     */
    @SerialEntry
    public QuickNavItem button4 = new QuickNavItem(new ItemData(Items.LEATHER_CHESTPLATE, "[minecraft:dyed_color={rgb:8991416}]"), "Wardrobe \\([12]/2\\)", "/wardrobe", "Wardrobe");

    @SerialEntry
    public QuickNavItem button5 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={id:[I;-2081424676,-57521078,-2073572414,158072763],name:\"\",properties:[{name:\"textures\",value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}]"), "Sack of Sacks", "/sacks", "Sacks");

    @SerialEntry
    public QuickNavItem button6 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={name:\"5da6bec64bd942bc\",id:[I;1571208902,1272529596,-1566400349,-679283814],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0=\"}]}]"), "Accessory Bag(?: \\(\\d/\\d\\))?", "/accessories", "Accessories");

    /* REGEX Explanation
     * "(?:Rift )?" : optional match on the non-capturing group "Rift "
     * "Storage" : simple match on letters
     * "(?: \\(\\d/\\d\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
     */
    @SerialEntry
    public QuickNavItem button7 = new QuickNavItem(new ItemData(Items.ENDER_CHEST), "(?:Rift )?Storage(?: \\(\\d/\\d\\))?", "/storage", "Storage");

    @SerialEntry
    public QuickNavItem button8 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={name:\"421a8ef40eff47f4\",id:[I;1109036788,251611124,-2126904485,-130621758],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0=\"}]}]"), "/is", "Home");

    @SerialEntry
    public QuickNavItem button9 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={name:\"e30e30d02878417c\",id:[I;-485609264,678969724,-1929747597,-718202427],properties:[{name:\"textures\",value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ4ODBkMmMxZTdiODZlODc1MjJlMjA4ODI2NTZmNDViYWZkNDJmOTQ5MzJiMmM1ZTBkNmVjYWE0OTBjYjRjIn19fQ==\"}]}]"), "/warp garden", "Garden");

    @SerialEntry
    public QuickNavItem button10 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={id:[I;-300151517,-631415889,-1193921967,-1821784279],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}]"), "/hub", "Skyblock Hub");

    @SerialEntry
    public QuickNavItem button11 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={id:[I;1605800870,415127827,-1236127084,15358548],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}]"), "/warp dungeon_hub", "Dungeons Hub");

    @SerialEntry
    public QuickNavItem button12 = new QuickNavItem(new ItemData(Items.GOLD_BLOCK), "Auction House", "/ah", "Auction House");

    @SerialEntry
    public QuickNavItem button13 = new QuickNavItem(new ItemData(Items.PLAYER_HEAD, "[minecraft:profile={id:[I;-562285948,532499670,-1705302742,775653035],name:\"\",properties:[{name:\"textures\",value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZlMmRjZGE0MWVjM2FmZjhhZjUwZjI3MmVjMmUwNmE4ZjUwOWUwZjgwN2YyMzU1YTFmNWEzM2MxYjY2ZTliNCJ9fX0=\"}]}]"), "Bazaar .*", "/bz", "Bazaar");

    @SerialEntry
    public QuickNavItem button14 = new QuickNavItem(new ItemData(Items.CRAFTING_TABLE), "Craft Item", "/craft", "Crafting Table");

    public static class QuickNavItem {
        /**
         * Default constructor or else gson skips initialization.
         */
        @SuppressWarnings("unused")
		private QuickNavItem() {}

        public QuickNavItem(ItemData itemData, String clickEvent, String tooltip) {
            this(itemData, "none", clickEvent, tooltip);
        }

        public QuickNavItem(ItemData itemData, @Language("RegExp") String uiTitle, String clickEvent, String tooltip) {
            this.itemData = itemData;
            this.uiTitle = uiTitle;
            this.clickEvent = clickEvent;
            this.tooltip = tooltip;
        }

        @SerialEntry
        public boolean render = true;

        @SerialEntry
        public ItemData itemData;

        @SerialEntry
        public String uiTitle;

        @SerialEntry
        public String tooltip = "";

        @SerialEntry
        public String clickEvent;
    }

    public static class ItemData {
        public ItemData(Item item) {
            this(item, "[]");
        }

        public ItemData(Item item, String components) {
            this.item = item;
            this.components = components;
        }

        @SerialEntry
        public Item item;

        @SerialEntry
        public int count = 1;

        @SerialEntry
        public String components;
    }
}
