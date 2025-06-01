package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterWidget
public class ItemPickupWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int LOBBY_CHANGE_DELAY = 3000;
	private static final String SACKS_MESSAGE_START = "[Sacks]";
	private static final Pattern CHANGE_REGEX = Pattern.compile("([+-])([\\d,]+) (.+) \\((.+)\\)");

	private static ItemPickupWidget instance;

	long lastLobbyChange;
	Object2ObjectOpenHashMap<String, changeData> addedCount = new Object2ObjectOpenHashMap<>();
	Object2ObjectOpenHashMap<String, changeData> removedCount = new Object2ObjectOpenHashMap<>();


	public ItemPickupWidget() {
		super(Text.literal("Items"), Formatting.AQUA.getColorValue(), "Item Pickup");
		instance = this;

		ClientReceiveMessageEvents.GAME.register((text, bl) -> instance.onChatMessage(text, bl));
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> lastLobbyChange = System.currentTimeMillis());
	}

	/**
	 * Searches the NEU REPO for the item linked to the name
	 */
	private static ItemStack getItem(String itemName) {
		return NEURepoManager.NEU_REPO.getItems().getItems()
				.values().stream()
				.filter(item -> Formatting.strip(item.getDisplayName()).equals(itemName))
				.findFirst()
				.map(NEUItem::getSkyblockItemId)
				.map(ItemRepository::getItemStack)
				.orElse(new ItemStack(Items.BARRIER));
	}

	/**
	 * Checks chat messages for a stack update message then finds the items linked to it
	 * @param message message
	 * @param b overlay
	 */
	private void onChatMessage(Text message, boolean b) {
		if (!Formatting.strip(message.getString()).startsWith(SACKS_MESSAGE_START)) return;
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemPickup.sackNotifications) return;
		HoverEvent hoverEvent = message.getSiblings().getFirst().getStyle().getHoverEvent();
		if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) return;
		String hoverMessage = ((HoverEvent.ShowText) hoverEvent).value().getString();

		Matcher matcher = CHANGE_REGEX.matcher(hoverMessage);
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
	protected boolean shouldUpdateBeforeRendering() {
		return true;
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
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				addedCount.remove(item);
				continue;
			}
			addSimpleIcoText(entry.item, itemName, Formatting.GREEN, "+" + entry.amount);


		}
		//add negative changes
		for (String item : removedCount.keySet()) {
			changeData entry = removedCount.get(item);
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				removedCount.remove(item);
				continue;
			}
			addSimpleIcoText(entry.item, itemName, Formatting.RED, "" + entry.amount);
		}
	}

	/**
	 * Checks if the changeData has expired and if not returns the item name for the entry
	 * @param entry changeData to check
	 * @return formated name from changeData
	 */
	private String checkNextItem(changeData entry) {
		//check the item has not expired
		if (entry.lastChange + SkyblockerConfigManager.get().uiAndVisuals.itemPickup.lifeTime * 1000L < System.currentTimeMillis()) {
			return null;
		}
		//return the formated name for the item based on user settings
		return  SkyblockerConfigManager.get().uiAndVisuals.itemPickup.showItemName ?  entry.item.getName().getString() + " " : " ";
	}

	@Override
	public boolean shouldRender(Location location) {
		if (super.shouldRender(location)) {
			//render if enabled
			if (SkyblockerConfigManager.get().uiAndVisuals.itemPickup.enabled) {
				//render if there are items in history
				return !addedCount.isEmpty() || !removedCount.isEmpty();
			}
		}
		return false;
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.values());
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		SkyblockerConfigManager.get().uiAndVisuals.itemPickup.enabled = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return SkyblockerConfigManager.get().uiAndVisuals.itemPickup.enabled;
	}

	/**
	 * When the client receives a slot change packet see what has changed in the inventory and add the to the counts
	 * @param packet slot change packet
	 */
	public void onItemPickup(ScreenHandlerSlotUpdateS2CPacket packet) {
		//if just changed lobby don't read item as this is just going to be all the players items
		if (lastLobbyChange + LOBBY_CHANGE_DELAY > System.currentTimeMillis() || CLIENT.player == null) return;

		//get the slot and stack from the packet
		ItemStack newStack = packet.getStack();
		int slot = packet.getSlot();
		//if the slot is below 9 it is a slot that we do not care about
		if (slot < 9) return;
		//hotbar slots are at the end of the ids instead of at the start like in the inventory main stacks so we convert to that indexing
		if (slot >= 36 && slot < 45) {
			slot = slot - 36;
		}
		//find what use to be in the slot
		ItemStack oldStack = CLIENT.player.getInventory().getMainStacks().get(slot);

		//work out what amount of items has changed
		int existingCount = 0;
		int countDiff = newStack.getCount() - oldStack.getCount();

		//if item being removed completely
		if (newStack.getItem() == Items.AIR) {
			// don't count air being changed somehow
			if (oldStack.getItem() == Items.AIR) {
				return;
			}

			if (removedCount.containsKey(oldStack.getNeuName())) {
				existingCount = removedCount.get(oldStack.getNeuName()).amount;
			}
			removedCount.put(oldStack.getNeuName(), new changeData(oldStack, existingCount - oldStack.getCount(), System.currentTimeMillis()));
			return;
		}

		//if there are more items than before
		if (countDiff > 0) {
			//see if there is already a change for this type of item
			if (addedCount.containsKey(newStack.getNeuName())) {
				existingCount = addedCount.get(newStack.getNeuName()).amount;
			}
			addedCount.put(newStack.getNeuName(), new changeData(newStack, existingCount + countDiff, System.currentTimeMillis()));

		}
		//if there are fewer items than before
		else if (countDiff < 0) {
			//see if there is already a change for this type of item
			if (removedCount.containsKey(newStack.getNeuName())) {
				existingCount = removedCount.get(newStack.getNeuName()).amount;
			}
			removedCount.put(newStack.getNeuName(), new changeData(newStack, existingCount + countDiff, System.currentTimeMillis()));
		}
	}

	private record changeData(ItemStack item, int amount, long lastChange) {
	}
}
