package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterWidget
public class ItemPickupWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int ITEM_LIFE_TIME = 3000;
	private static final int LOBBY_CHANGE_DELAY = 3000;
	private static final String SACKS_MESSAGE_START = "[Sacks]";

	private static ItemPickupWidget instance;

	long lastLobbyChange;
	Object2ObjectOpenHashMap<String, changeData> addedCount = new Object2ObjectOpenHashMap<>();
	Object2ObjectOpenHashMap<String, changeData> removedCount = new Object2ObjectOpenHashMap<>();


	public ItemPickupWidget() {
		super(Text.literal("Items"), Formatting.BLUE.getColorValue(), "item_pickup");
		instance = this;

		ClientReceiveMessageEvents.GAME.register((text, bl) -> instance.onChatMessage(text, bl));
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> lastLobbyChange = System.currentTimeMillis());
	}

	/**
	 * Retrieves a cached ItemStack or fetches it if not already cached.
	 */
	private static ItemStack getItem(String itemName) {
		return NEURepoManager.NEU_REPO.getItems().getItems()
				.values().stream()
				.filter(item -> Formatting.strip(item.getDisplayName()).equals(itemName))
				.findFirst()
				.map(NEUItem::getSkyblockItemId)
				.map(ItemRepository::getItemStack)
				.orElse(new ItemStack(Items.AIR));
	}

	private void onChatMessage(Text message, boolean b) {

		if (!Formatting.strip(message.getString()).startsWith(SACKS_MESSAGE_START)) return;
		HoverEvent hoverEvent = message.getSiblings().getFirst().getStyle().getHoverEvent();
		if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) return;
		String hoverMessage = ((HoverEvent.ShowText) hoverEvent).value().getString();

		Pattern changeRegex = Pattern.compile("([+-])([\\d,]+) (.+) \\((.+)\\)");
		Matcher matcher = changeRegex.matcher(hoverMessage);
		while (matcher.find()) {

			ItemStack item = getItem(matcher.group(3));
			//positive
			int existingCount = 0;
			if (matcher.group(1).equals("+")) {
				if (addedCount.containsKey(item.getNeuName())) {
					existingCount = addedCount.get(item.getNeuName()).amount;
				}
				addedCount.put(item.getNeuName(), new changeData(item, existingCount + Formatters.parseNumber(matcher.group(2)).intValue(), System.currentTimeMillis()));
			}
			//negative
			else if (matcher.group(1).equals("-")) {
				if (removedCount.containsKey(item.getNeuName())) {
					existingCount = removedCount.get(item.getNeuName()).amount;
				}
				removedCount.put(item.getNeuName(), new changeData(item, existingCount - Formatters.parseNumber(matcher.group(2)).intValue(), System.currentTimeMillis()));

			}

		}

	}

	public static ItemPickupWidget getInstance() {
		return instance;
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.BONE, "Bone ", Formatting.GREEN, "+64");
			return;
		}
		//add each diff item to widget
		//add positive changes
		for (String item : addedCount.keySet()) {
			changeData entry = addedCount.get(item);
			//check the item has not expired
			if (entry.lastChange + ITEM_LIFE_TIME < System.currentTimeMillis()) {
				addedCount.remove(item);
				continue;
			}

			addSimpleIcoText(entry.item, entry.item.getName().getString() + ":", Formatting.GREEN, "+" + entry.amount);


		}
		//add negative changes
		for (String item : removedCount.keySet()) {
			changeData entry = removedCount.get(item);
			//check the item has not expired
			if (entry.lastChange + ITEM_LIFE_TIME < System.currentTimeMillis()) {
				removedCount.remove(item);
				continue;
			}
			addSimpleIcoText(entry.item, entry.item.getName().getString() + ":", Formatting.RED, "" + entry.amount);
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.values());
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		//todo
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return true;//todo
	}

	/**
	 * Checks current inventory with last known inventory
	 */
	private void updateInventory() {
		if (CLIENT.player == null) return;
		List<ItemStack> newInventory = CLIENT.player.getInventory().getMainStacks();
	}

	public void onItemPickup(ScreenHandlerSlotUpdateS2CPacket packet) {
		//if just changed lobby don't read item as this is just going to be all the players items
		if (lastLobbyChange + LOBBY_CHANGE_DELAY > System.currentTimeMillis() || CLIENT.player == null) return;

		//get the slot and stack from the packet
		ItemStack newStack = packet.getStack();
		int slot = packet.getSlot();
		int convertedSlot;
		if (slot < 9) return;
		if (slot >= 36 && slot < 45) {
			convertedSlot = slot - 36;
		} else {
			convertedSlot = slot;
		}
		ItemStack oldStack = CLIENT.player.getInventory().getMainStacks().get(convertedSlot);


		//if it's new


		int existingCount = 0;
		int countDiff = newStack.getCount() - oldStack.getCount();

		//if item being removed
		if (newStack.getItem() == Items.AIR) {
			if (oldStack.getItem() == Items.AIR) {
				return;
			}

			if (removedCount.containsKey(oldStack.getNeuName())) {
				existingCount = removedCount.get(oldStack.getNeuName()).amount;
			}
			removedCount.put(oldStack.getNeuName(), new changeData(oldStack, existingCount - oldStack.getCount(), System.currentTimeMillis()));
			return;
		}


		if (countDiff > 0) {
			//add to diff
			if (addedCount.containsKey(newStack.getNeuName())) {
				existingCount = addedCount.get(newStack.getNeuName()).amount;
			}
			addedCount.put(newStack.getNeuName(), new changeData(newStack, existingCount + countDiff, System.currentTimeMillis()));
		} else if (countDiff < 0) {
			if (removedCount.containsKey(newStack.getNeuName())) {
				existingCount = removedCount.get(newStack.getNeuName()).amount;
			}
			removedCount.put(newStack.getNeuName(), new changeData(newStack, existingCount + countDiff, System.currentTimeMillis()));
		}


	}

	private record changeData(ItemStack item, int amount, long lastChange) {

	}
}
