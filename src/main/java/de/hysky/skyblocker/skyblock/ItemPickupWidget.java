package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterWidget
public class ItemPickupWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int LOBBY_CHANGE_DELAY = 60;
	private static final String SACKS_MESSAGE_START = "[Sacks]";
	private static final Pattern CHANGE_REGEX = Pattern.compile("([+-])([\\d,]+) (.+) \\((.+)\\)");

	private static ItemPickupWidget instance;

	private boolean changingLobby;

	private final Object2ObjectOpenHashMap<String, ChangeData> addedCount = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<String, ChangeData> removedCount = new Object2ObjectOpenHashMap<>();

	public ItemPickupWidget() {
		super(Text.literal("Items"), Formatting.AQUA.getColorValue(), "Item Pickup");
		instance = this;

		ClientReceiveMessageEvents.GAME.register(instance::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> changingLobby = true);
		// Make changingLobby true for a short period while the player loads into a new lobby and their items are loading
		SkyblockEvents.LOCATION_CHANGE.register(location -> Scheduler.INSTANCE.schedule(() -> changingLobby = false, LOBBY_CHANGE_DELAY));
	}

	public static ItemPickupWidget getInstance() {
		return instance;
	}

	/**
	 * Searches the NEU REPO for the item linked to the name
	 */
	private static ItemStack getItem(String itemName) {
		if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return new ItemStack(Items.BARRIER);
		return NEURepoManager.NEU_REPO.getItems().getItems()
				.values().stream()
				.filter(item -> Formatting.strip(item.getDisplayName()).equals(itemName))
				.findFirst()
				.map(NEUItem::getSkyblockItemId)
				.map(ItemRepository::getItemStack)
				.orElse(new ItemStack(Items.BARRIER));
	}

	/**
	 * Checks chat messages for a stack update message, then finds the items linked to it
	 */
	private void onChatMessage(Text message, boolean overlay) {
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
				addedCount.put(item.getNeuName(), new ChangeData(item, existingCount + Formatters.parseNumber(matcher.group(2)).intValue(), System.currentTimeMillis()));
			}
			//negative
			else if (matcher.group(1).equals("-")) {
				if (removedCount.containsKey(item.getNeuName())) {
					existingCount = removedCount.get(item.getNeuName()).amount;
				}
				removedCount.put(item.getNeuName(), new ChangeData(item, existingCount - Formatters.parseNumber(matcher.group(2)).intValue(), System.currentTimeMillis()));
			}
		}
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.BONE, "Bone ", Formatting.GREEN, "+64");
			return;
		}
		//add each diff item to the widget
		//add positive changes
		for (String item : addedCount.keySet()) {
			ChangeData entry = addedCount.get(item);
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				addedCount.remove(item);
				continue;
			}
			addSimpleIcoText(entry.item, itemName, Formatting.GREEN, Formatters.DIFF_NUMBERS.format(entry.amount));
		}
		//add negative changes
		for (String item : removedCount.keySet()) {
			ChangeData entry = removedCount.get(item);
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				removedCount.remove(item);
				continue;
			}
			addSimpleIcoText(entry.item, itemName, Formatting.RED, Formatters.DIFF_NUMBERS.format(entry.amount));
		}
	}

	/**
	 * Checks if the ChangeData has expired and if not, returns the item name for the entry
	 *
	 * @param entry ChangeData to check
	 * @return formatted name from ChangeData
	 */
	private String checkNextItem(ChangeData entry) {
		//check the item has not expired
		if (entry.lastChange + SkyblockerConfigManager.get().uiAndVisuals.itemPickup.lifeTime * 1000L < System.currentTimeMillis()) {
			return null;
		}
		//return the formatted name for the item based on user settings
		return SkyblockerConfigManager.get().uiAndVisuals.itemPickup.showItemName ? entry.item.getName().getString() + " " : " ";
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
		return ALL_LOCATIONS;
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
	 * When the client receives a slot change packet, see what has changed in the inventory and add to the counts
	 */
	public void onItemPickup(int slot, ItemStack newStack) {
		//if just changed a lobby, don't read item as this is just going to be all the player's items
		if (changingLobby || CLIENT.player == null) return;
		//make sure there is not an inventory open
		if (CLIENT.currentScreen != null) return;

		//if the slot is below 9, it is a slot that we do not care about
		//if the slot is equals to or above 45, it is not in the player's inventory
		if (slot < 9 || slot >= 45) return;
		//hotbar slots are at the end of the ids instead of at the start like in the inventory main stacks, so we convert to that indexing
		if (slot >= 36) {
			slot = slot - 36;
		}
		//find what used to be in the slot
		ItemStack oldStack = CLIENT.player.getInventory().getMainStacks().get(slot);

		//work out the number of items changed
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
			removedCount.put(oldStack.getNeuName(), new ChangeData(oldStack, existingCount - oldStack.getCount(), System.currentTimeMillis()));
			return;
		}

		//if there are more items than before
		if (countDiff > 0) {
			//see if there is already a change for this type of item
			if (addedCount.containsKey(newStack.getNeuName())) {
				existingCount = addedCount.get(newStack.getNeuName()).amount;
			}
			addedCount.put(newStack.getNeuName(), new ChangeData(newStack, existingCount + countDiff, System.currentTimeMillis()));

		}
		//if there are fewer items than before
		else if (countDiff < 0) {
			//see if there is already a change for this type of item
			if (removedCount.containsKey(newStack.getNeuName())) {
				existingCount = removedCount.get(newStack.getNeuName()).amount;
			}
			removedCount.put(newStack.getNeuName(), new ChangeData(newStack, existingCount + countDiff, System.currentTimeMillis()));
		}
	}

	private record ChangeData(ItemStack item, int amount, long lastChange) {}
}
