package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AttributeShardAdder extends SimpleSlotTextAdder {
	private static final Object2ObjectMap<String, String> ID_2_SHORT_NAME = new Object2ObjectOpenHashMap<>();
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"attribute_shard",
			"skyblocker.config.uiAndVisuals.slotText.attributeShard",
			"skyblocker.config.uiAndVisuals.slotText.attributeShard.@Tooltip");

	static {
		//Weapons
		ID_2_SHORT_NAME.put("arachno", "A");
		ID_2_SHORT_NAME.put("attack_speed", "AS");
		ID_2_SHORT_NAME.put("blazing", "BL");
		ID_2_SHORT_NAME.put("combo", "C");
		ID_2_SHORT_NAME.put("elite", "E");
		ID_2_SHORT_NAME.put("ender", "EN");
		ID_2_SHORT_NAME.put("ignition", "I");
		ID_2_SHORT_NAME.put("life_recovery", "HE"); //HE for Healing since LR should go to Life regen since thats more useful
		ID_2_SHORT_NAME.put("mana_steal", "MS");
		ID_2_SHORT_NAME.put("midas_touch", "MT");
		ID_2_SHORT_NAME.put("undead", "U");

		//Swords & Bows
		ID_2_SHORT_NAME.put("warrior", "W");
		ID_2_SHORT_NAME.put("deadeye", "DE");

		//Armor or Equipment
		ID_2_SHORT_NAME.put("arachno_resistance", "AR");
		ID_2_SHORT_NAME.put("blazing_resistance", "BR");
		ID_2_SHORT_NAME.put("breeze", "B");
		ID_2_SHORT_NAME.put("dominance", "D");
		ID_2_SHORT_NAME.put("ender_resistance", "ER");
		ID_2_SHORT_NAME.put("experience", "XP");
		ID_2_SHORT_NAME.put("fortitude", "F");
		ID_2_SHORT_NAME.put("life_regeneration", "LR");
		ID_2_SHORT_NAME.put("lifeline", "L");
		ID_2_SHORT_NAME.put("magic_find", "MF");
		ID_2_SHORT_NAME.put("mana_pool", "MP");
		ID_2_SHORT_NAME.put("mana_regeneration", "MR");
		ID_2_SHORT_NAME.put("mending", "VI"); //Vitality
		ID_2_SHORT_NAME.put("speed", "S");
		ID_2_SHORT_NAME.put("undead_resistance", "UR");
		ID_2_SHORT_NAME.put("veteran", "V");

		//Fishing Gear
		ID_2_SHORT_NAME.put("blazing_fortune", "BF");
		ID_2_SHORT_NAME.put("fishing_experience", "FE");
		ID_2_SHORT_NAME.put("infection", "IF");
		ID_2_SHORT_NAME.put("double_hook", "DH");
		ID_2_SHORT_NAME.put("fisherman", "FM");
		ID_2_SHORT_NAME.put("fishing_speed", "FS");
		ID_2_SHORT_NAME.put("hunter", "H");
		ID_2_SHORT_NAME.put("trophy_hunter", "TH");
	}

	public AttributeShardAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		NbtCompound customData = ItemUtils.getCustomData(stack);

		if (!ItemUtils.getItemId(stack).equals("ATTRIBUTE_SHARD")) return List.of();

		NbtCompound attributesTag = customData.getCompound("attributes");
		String[] attributes = attributesTag.getKeys().toArray(String[]::new);

		if (attributes.length != 1) return List.of();
		String attributeId = attributes[0];
		int attributeLevel = attributesTag.getInt(attributeId);
		String attributeInitials = ID_2_SHORT_NAME.getOrDefault(attributeId, "");

		return List.of(
				SlotText.bottomRight(Text.literal(String.valueOf(attributeLevel)).withColor(0xFFDDC1)),
				SlotText.topLeft(Text.literal(attributeInitials).withColor(0xCFF8F8))
		);
	}
}
