package de.hysky.skyblocker.skyblock.profileviewer;

import org.joml.Matrix3x2fStack;

import com.google.gson.JsonObject;

import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.BackpackItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.InventoryItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.ItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.PetsInventoryItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.WardrobeInventoryItemLoader;
import de.hysky.skyblocker.skyblock.profileviewer.utils.ProfileViewerUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public class ProfileViewerTextWidget {
	private static final int ROW_GAP = 9;

	private String PROFILE_NAME = "UNKNOWN";
	private int SKYBLOCK_LEVEL = 0;
	private double PURSE = 0;
	private double BANK = 0;
	private double NETWORTH = 0;
	private List<Component> networthTooltip = List.of();

	public ProfileViewerTextWidget(JsonObject hypixelProfile, JsonObject playerProfile) {
		try {
			this.PROFILE_NAME = hypixelProfile.get("cute_name").getAsString();
			this.SKYBLOCK_LEVEL = playerProfile.getAsJsonObject("leveling").get("experience").getAsInt() / 100;
			this.PURSE = playerProfile.getAsJsonObject("currencies").get("coin_purse").getAsDouble();
			this.BANK = hypixelProfile.getAsJsonObject("banking").get("balance").getAsDouble();
		} catch (Exception ignored) {}

		this.NETWORTH = PURSE + BANK + getItemsNetworth(playerProfile);
	}

	private double getItemsNetworth(JsonObject playerProfile) {
		double value = 0;
		PriorityQueue<ItemValue> top = new PriorityQueue<>(Comparator.comparingDouble(ItemValue::price));
		try {
			JsonObject inventoryData = playerProfile.getAsJsonObject("inventory");
			if (inventoryData != null) {
				for (ItemStack stack : new InventoryItemLoader().loadItems(inventoryData)) {
					value += addItemNetworth(top, stack);
				}

				if (inventoryData.has("ender_chest_contents")) {
					for (ItemStack stack : new ItemLoader().loadItems(inventoryData.getAsJsonObject("ender_chest_contents"))) {
						value += addItemNetworth(top, stack);
					}
				}

				if (inventoryData.has("backpack_contents")) {
					for (ItemStack stack : new BackpackItemLoader().loadItems(inventoryData.getAsJsonObject("backpack_contents"))) {
						value += addItemNetworth(top, stack);
					}
				}

				if (inventoryData.has("wardrobe_contents")) {
					int activeSlot = inventoryData.get("wardrobe_equipped_slot").getAsInt();
					Set<Integer> skip = new HashSet<>();
					if (activeSlot != -1) {
						for (int i = 0; i < 4; i++) {
							int baseIndex = (activeSlot - 1) % 9;
							int page = (activeSlot - 1) / 9;
							int slotIndex = (page * 36) + (i * 9) + baseIndex;
							skip.add(slotIndex);
						}
					}

					WardrobeInventoryItemLoader loader = new WardrobeInventoryItemLoader(inventoryData);
					List<ItemStack> wardrobeItems = loader.loadItems(inventoryData.getAsJsonObject("wardrobe_contents"));
					for (int index = 0; index < wardrobeItems.size(); index++) {
						if (skip.contains(index)) continue;
						ItemStack stack = wardrobeItems.get(index);
						value += addItemNetworth(top, stack);
					}
				}

				if (inventoryData.has("bag_contents") && inventoryData.getAsJsonObject("bag_contents").has("talisman_bag")) {
					for (ItemStack stack : new ItemLoader().loadItems(inventoryData.getAsJsonObject("bag_contents").getAsJsonObject("talisman_bag"))) {
						value += addItemNetworth(top, stack);
					}
				}
			}

			for (ItemStack stack : new PetsInventoryItemLoader().loadItems(playerProfile)) {
				value += addItemNetworth(top, stack);
			}
		} catch (Exception ignored) {}

		List<ItemValue> list = new ArrayList<>(top);
		list.sort(Comparator.comparingDouble(ItemValue::price).reversed());
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(Component.literal("Top Items:").withStyle(ChatFormatting.GOLD));
		for (ItemValue iv : list) {
			tooltip.add(Component.literal(iv.name + ": ")
					.append(Component.literal(ProfileViewerUtils.numLetterFormat(iv.price)).withStyle(ChatFormatting.YELLOW)));
		}
		this.networthTooltip = tooltip;

		return value;
	}

	private double addItemNetworth(PriorityQueue<ItemValue> top, ItemStack stack) {
		double p = NetworthCalculator.getItemNetworth(stack).price();
		if (p > 0) {
			top.offer(new ItemValue(stack.getHoverName().getString(), p));
			if (top.size() > 10) top.poll();
		}
		return p;
	}

	public void render(GuiGraphics context, Font textRenderer, int root_x, int root_y, int mouseX, int mouseY) {
		// Profile Icon
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.scale(0.75f, 0.75f);
		int rootAdjustedX = (int) ((root_x) / 0.75f);
		int rootAdjustedY = (int) ((root_y) / 0.75f);
		context.renderItem(Ico.PAINTING, rootAdjustedX, rootAdjustedY);
		matrices.popMatrix();

		context.drawString(textRenderer, "§n" + PROFILE_NAME, root_x + 14, root_y + 3, CommonColors.WHITE, true);
		context.drawString(textRenderer, "§aLevel:§r " + SKYBLOCK_LEVEL, root_x + 2, root_y + 6 + ROW_GAP, CommonColors.WHITE, true);
		context.drawString(textRenderer, "§6Purse:§r " + ProfileViewerUtils.numLetterFormat(PURSE), root_x + 2, root_y + 6 + ROW_GAP * 2, CommonColors.WHITE, true);
		context.drawString(textRenderer, "§6Bank:§r " + ProfileViewerUtils.numLetterFormat(BANK), root_x + 2, root_y + 6 + ROW_GAP * 3, CommonColors.WHITE, true);
		String nwString = "§6NW:§r " + ProfileViewerUtils.numLetterFormat(NETWORTH);
		int nwX = root_x + 2;
		int nwY = root_y + 6 + ROW_GAP * 4;
		context.drawString(textRenderer, nwString, nwX, nwY, CommonColors.WHITE, true);
		if (mouseX >= nwX && mouseX <= nwX + textRenderer.width(nwString)
				&& mouseY >= nwY && mouseY <= nwY + textRenderer.lineHeight) {
			context.setComponentTooltipForNextFrame(textRenderer, networthTooltip, mouseX, mouseY);
		}
	}

	private record ItemValue(String name, double price) {}
}
