package de.hysky.skyblocker.config.configs;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class QuickNavigationConfig {
    @SerialEntry
    public boolean enableQuickNav = true;

    @SerialEntry
    public QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

    @SerialEntry
    public QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collections", "/collection");

    /* REGEX Explanation
     * "Pets" : simple match on letters
     * "(?: \\(\\d+\\/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
     */
    @SerialEntry
    public QuickNavItem button3 = new QuickNavItem(true, new ItemData("bone"), "Pets(:? \\(\\d+\\/\\d+\\))?", "/pets");

    /* REGEX Explanation
     * "Wardrobe" : simple match on letters
     * " \\([12]\\/2\\)" : match on the page either " (1/2)" or " (2/2)"
     */
    @SerialEntry
    public QuickNavItem button4 = new QuickNavItem(true,
            new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe \\([12]/2\\)",
            "/wardrobe");

    @SerialEntry
    public QuickNavItem button5 = new QuickNavItem(true, new ItemData("player_head", 1,
            "tag:{SkullOwner:{Id:[I;-2081424676,-57521078,-2073572414,158072763],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}}}"),
            "Sack of Sacks", "/sacks");

    /* REGEX Explanation
     * "(?:Rift )?" : optional match on the non-capturing group "Rift "
     * "Storage" : simple match on letters
     * "(?: \\([12]\\/2\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
     */
    @SerialEntry
    public QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"),
            "(?:Rift )?Storage(?: \\(1/2\\))?", "/storage");

    @SerialEntry
    public QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1,
            "tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"),
            "none", "/hub");

    @SerialEntry
    public QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1,
            "tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"),
            "none", "/warp dungeon_hub");

    @SerialEntry
    public QuickNavItem button9 = new QuickNavItem(true, new ItemData("player_head", 1,
            "tag:{SkullOwner:{Id:[I;-562285948,532499670,-1705302742,775653035],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVkZjU1NTkyNjQzMGQ1ZDc1YWRlZDIxZGQ5NjE5Yjc2YzViN2NhMmM3ZjU0MDE0NDA1MjNkNTNhOGJjZmFhYiJ9fX0=\"}]}}}"),
            "Visit prtl", "/visit prtl");

    @SerialEntry
    public QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant Item",
            "/etable");


    @SerialEntry
    public QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

    @SerialEntry
    public QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");

    public static class QuickNav {
        @SerialEntry
        public boolean enableQuickNav = true;

        @SerialEntry
        public QuickNavItem button1 = new QuickNavItem(true, new ItemData("diamond_sword"), "Your Skills", "/skills");

        @SerialEntry
        public QuickNavItem button2 = new QuickNavItem(true, new ItemData("painting"), "Collections", "/collection");

        /* REGEX Explanation
         * "Pets" : simple match on letters
         * "(?: \\(\\d+\\/\\d+\\))?" : optional match on the non-capturing group for the page in the format " ($number/$number)"
         */
        @SerialEntry
        public QuickNavItem button3 = new QuickNavItem(true, new ItemData("bone"), "Pets(:? \\(\\d+\\/\\d+\\))?", "/pets");

        /* REGEX Explanation
         * "Wardrobe" : simple match on letters
         * " \\([12]\\/2\\)" : match on the page either " (1/2)" or " (2/2)"
         */
        @SerialEntry
        public QuickNavItem button4 = new QuickNavItem(true,
                new ItemData("leather_chestplate", 1, "tag:{display:{color:8991416}}"), "Wardrobe \\([12]/2\\)",
                "/wardrobe");

        @SerialEntry
        public QuickNavItem button5 = new QuickNavItem(true, new ItemData("player_head", 1,
                "tag:{SkullOwner:{Id:[I;-2081424676,-57521078,-2073572414,158072763],Properties:{textures:[{Value:\"ewogICJ0aW1lc3RhbXAiIDogMTU5MTMxMDU4NTYwOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBhMDc3ZTI0OGQxNDI3NzJlYTgwMDg2NGY4YzU3OGI5ZDM2ODg1YjI5ZGFmODM2YjY0YTcwNjg4MmI2ZWMxMCIKICAgIH0KICB9Cn0=\"}]}}}"),
                "Sack of Sacks", "/sacks");

        /* REGEX Explanation
         * "(?:Rift )?" : optional match on the non-capturing group "Rift "
         * "Storage" : simple match on letters
         * "(?: \\([12]\\/2\\))?" : optional match on the non-capturing group " (1/2)" or " (2/2)"
         */
        @SerialEntry
        public QuickNavItem button6 = new QuickNavItem(true, new ItemData("ender_chest"),
                "(?:Rift )?Storage(?: \\(1/2\\))?", "/storage");

        @SerialEntry
        public QuickNavItem button7 = new QuickNavItem(true, new ItemData("player_head", 1,
                "tag:{SkullOwner:{Id:[I;-300151517,-631415889,-1193921967,-1821784279],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYzY2ODc0MjNkMDU3MGQ1NTZhYzUzZTA2NzZjYjU2M2JiZGQ5NzE3Y2Q4MjY5YmRlYmVkNmY2ZDRlN2JmOCJ9fX0=\"}]}}}"),
                "none", "/hub");

        @SerialEntry
        public QuickNavItem button8 = new QuickNavItem(true, new ItemData("player_head", 1,
                "tag:{SkullOwner:{Id:[I;1605800870,415127827,-1236127084,15358548],Properties:{textures:[{Value:\"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MWQ1YjI3M2ZmMGJjNTBjOTYwYjJjZDg2ZWVmMWM0MGExYjk0MDMyYWU3MWU3NTQ3NWE1NjhhODI1NzQyMSJ9fX0=\"}]}}}"),
                "none", "/warp dungeon_hub");

        @SerialEntry
        public QuickNavItem button9 = new QuickNavItem(true, new ItemData("player_head", 1,
                "tag:{SkullOwner:{Id:[I;-562285948,532499670,-1705302742,775653035],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjVkZjU1NTkyNjQzMGQ1ZDc1YWRlZDIxZGQ5NjE5Yjc2YzViN2NhMmM3ZjU0MDE0NDA1MjNkNTNhOGJjZmFhYiJ9fX0=\"}]}}}"),
                "Visit prtl", "/visit prtl");

        @SerialEntry
        public QuickNavItem button10 = new QuickNavItem(true, new ItemData("enchanting_table"), "Enchant Item",
                "/etable");


        @SerialEntry
        public QuickNavItem button11 = new QuickNavItem(true, new ItemData("anvil"), "Anvil", "/anvil");

        @SerialEntry
        public QuickNavItem button12 = new QuickNavItem(true, new ItemData("crafting_table"), "Craft Item", "/craft");
    }

    public static class QuickNavItem {
        public QuickNavItem(Boolean render, ItemData itemData, String uiTitle, String clickEvent) {
            this.render = render;
            this.item = itemData;
            this.clickEvent = clickEvent;
            this.uiTitle = uiTitle;
        }

        @SerialEntry
        public Boolean render;

        @SerialEntry
        public ItemData item;

        @SerialEntry
        public String uiTitle;

        @SerialEntry
        public String clickEvent;
    }

    public static class ItemData {
        public ItemData(String itemName, int count, String nbt) {
            this.itemName = itemName;
            this.count = count;
            this.nbt = nbt;
        }

        public ItemData(String itemName) {
            this.itemName = itemName;
            this.count = 1;
            this.nbt = "";
        }

        @SerialEntry
        public String itemName;

        @SerialEntry
        public int count;

        @SerialEntry
        public String nbt;
    }
}
