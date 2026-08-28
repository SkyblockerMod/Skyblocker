package de.hysky.skyblocker.skyblock;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.SeparatorElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.JsonValueInput;
import de.hysky.skyblocker.utils.JsonValueOutput;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

@RegisterWidget
public class ItemPickupWidget extends ElementBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int LOBBY_CHANGE_DELAY = 60;
	private static final String SACKS_MESSAGE_START = "[Sacks]";
	private static final Pattern CHANGE_REGEX = Pattern.compile("([+-])([\\d,]+) (.+) \\((.+)\\)");

	private static @Nullable ItemPickupWidget instance;

	private boolean changingLobby;

	private boolean sackNotifications;
	private boolean splitNotifications;
	private boolean showItemName = true;
	private float lifetime = 3;

	private final Object2ObjectOpenHashMap<String, ChangeData> addedCount = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<String, ChangeData> removedCount = new Object2ObjectOpenHashMap<>();

	private final Object2ObjectOpenHashMap<String, ChangeData> addedSackCount = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<String, ChangeData> removedSackCount = new Object2ObjectOpenHashMap<>();

	public ItemPickupWidget() {
		super(Component.literal("Items"), ChatFormatting.AQUA.getColor(), new Information("item_pickup", Component.literal("Item Pickup")));
		instance = this;

		ClientReceiveMessageEvents.ALLOW_GAME.register(instance::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> changingLobby = true);
		// Make changingLobby true for a short period while the player loads into a new lobby and their items are loading
		SkyblockEvents.LOCATION_CHANGE.register(_ -> Scheduler.INSTANCE.schedule(() -> changingLobby = false, LOBBY_CHANGE_DELAY));
	}

	public static ItemPickupWidget getInstance() {
		return Objects.requireNonNull(instance, "ItemPickupWidget not initialized");
	}

	/**
	 * Searches the NEU REPO for the item linked to the name
	 */
	private static FlexibleItemStack getItem(String itemName) {
		if (NEURepoManager.isLoading() || !ItemRepository.filesImported()) return ItemUtils.getNamedPlaceholder(itemName);
		return NEURepoManager.getItemByName(itemName)
				.stream()
				.filter(Objects::nonNull)
				.findFirst()
				.map(NEUItem::getSkyblockItemId)
				.map(ItemRepository::getItemStack)
				.orElseGet(() -> ItemUtils.getNamedPlaceholder(itemName));
	}

	/**
	 * Checks chat messages for a stack update message, then finds the items linked to it
	 */
	@SuppressWarnings("SameReturnValue")
	private boolean onChatMessage(Component message, boolean overlay) {
		if (!ChatFormatting.stripFormatting(message.getString()).startsWith(SACKS_MESSAGE_START)) return true;
		if (!sackNotifications) return true;
		HoverEvent hoverEvent = message.getSiblings().getFirst().getStyle().getHoverEvent();
		if (hoverEvent == null || hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) return true;
		String hoverMessage = ((HoverEvent.ShowText) hoverEvent).value().getString();
		boolean split = splitNotifications;

		Matcher matcher = CHANGE_REGEX.matcher(ChatFormatting.stripFormatting(hoverMessage));
		while (matcher.find()) {

			ItemStack item = getItem(matcher.group(3)).getStackOrThrow();
			int count = Formatters.parseNumber(matcher.group(2)).intValue();
			//positive
			if (matcher.group(1).equals("+")) updateCount(split ? addedSackCount : addedCount, item, count);
			//negative
			else if (matcher.group(1).equals("-")) updateCount(split ? removedSackCount : removedCount, item, count);
		}

		return true;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		//add each diff item to the widget
		//add positive changes
		for (String item : addedCount.keySet()) {
			ChangeData entry = addedCount.get(item);
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				addedCount.remove(item);
				continue;
			}
			if (entry.item.isEmpty()) continue;
			addSimpleIcoText(new FlexibleItemStack(entry.item), itemName, ChatFormatting.GREEN, Formatters.DIFF_NUMBERS.format(entry.amount));
		}
		//add negative changes
		for (String item : removedCount.keySet()) {
			ChangeData entry = removedCount.get(item);
			String itemName = checkNextItem(entry);
			if (itemName == null) {
				removedCount.remove(item);
				continue;
			}
			if (entry.item.isEmpty()) continue;
			addSimpleIcoText(new FlexibleItemStack(entry.item), itemName, ChatFormatting.RED, Formatters.DIFF_NUMBERS.format(entry.amount));
		}
		if (splitNotifications && !(this.addedSackCount.isEmpty() && this.removedSackCount.isEmpty())) {
			this.addElement(new SeparatorElement(Component.nullToEmpty("Sacks")));
			for (String item : addedSackCount.keySet()) {
				ChangeData entry = addedSackCount.get(item);
				String itemName = checkNextItem(entry);
				if (itemName == null) {
					addedSackCount.remove(item);
					continue;
				}
				addSimpleIcoText(new FlexibleItemStack(entry.item), itemName, ChatFormatting.GREEN, Formatters.DIFF_NUMBERS.format(entry.amount));
			}
			for (String item : removedSackCount.keySet()) {
				ChangeData entry = removedSackCount.get(item);
				String itemName = checkNextItem(entry);
				if (itemName == null) {
					removedSackCount.remove(item);
					continue;
				}
				addSimpleIcoText(new FlexibleItemStack(entry.item), itemName, ChatFormatting.RED, Formatters.DIFF_NUMBERS.format(entry.amount));
			}
		}
	}

	@Override
	protected void updateConfigContent(ElementCollector collector) {
		collector.addSimpleIcoText(Ico.BONE, "Bone ", ChatFormatting.GREEN, "+64");
		if (sackNotifications) {
			if (splitNotifications) {
				collector.addElement(new SeparatorElement(Component.nullToEmpty("Sacks")));
			}
			collector.addSimpleIcoText(Ico.BONE, "Enchanted Bone ", ChatFormatting.GREEN, "+1");
		}
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.yesNoButton(Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.sackNotifications"), b -> sackNotifications = b, sackNotifications, Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.sackNotifications.@Tooltip"));
		collector.yesNoButton(Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.splitSack"), b -> splitNotifications = b, splitNotifications, Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.splitSack.@Tooltip"));
		collector.yesNoButton(Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.showItemName"), b -> showItemName = b, showItemName, Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.showItemName.@Tooltip"));
		collector.slider(Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.lifeTime"), d -> lifetime = (float) d, lifetime, 0.3, 10).tooltip(Component.translatable("skyblocker.config.uiAndVisuals.itemPickup.lifeTime.@Tooltip"));
	}

	@Override
	public void load(JsonValueInput input) {
		super.load(input);
		sackNotifications = input.readBooleanOr("sack_notifications", false);
		splitNotifications = input.readBooleanOr("split_sack", false);
		showItemName = input.readBooleanOr("show_item_name", true);
		lifetime = input.readFloatOr("lifetime", 3);
	}

	@Override
	public void save(JsonValueOutput output) {
		super.save(output);
		output.writeBool("sack_notifications", sackNotifications);
		output.writeBool("split_sack", splitNotifications);
		output.writeBool("show_item_name", showItemName);
		output.writeNumber("lifetime", lifetime);
	}

	/**
	 * Checks if the ChangeData has expired and if not, returns the item name for the entry
	 *
	 * @param entry ChangeData to check
	 * @return formatted name from ChangeData
	 */
	private @Nullable String checkNextItem(ChangeData entry) {
		//check the item has not expired
		if (entry.lastChange + lifetime * 1000L < System.currentTimeMillis()) {
			return null;
		}
		//return the formatted name for the item based on user settings
		return showItemName ? entry.item.getHoverName().getString() + " " : " ";
	}

	@Override
	public boolean shouldRender() {
		return !addedCount.isEmpty() || !removedCount.isEmpty() || !addedSackCount.isEmpty() || !removedSackCount.isEmpty();
	}

	/**
	 * When the client receives a slot change packet, see what has changed in the inventory and add to the counts
	 */
	public void onItemPickup(int slot, ItemStack newStack) {
		//if just changed a lobby, don't read item as this is just going to be all the player's items
		if (changingLobby || CLIENT.player == null) return;
		//make sure there is not an inventory open
		if (CLIENT.screen != null) return;

		//if the slot is below 9, it is a slot that we do not care about
		//if the slot is equals to or above 45, it is not in the player's inventory
		if (slot < 9 || slot >= 45) return;
		//hotbar slots are at the end of the ids instead of at the start like in the inventory main stacks, so we convert to that indexing
		if (slot >= 36) {
			slot = slot - 36;
		}
		if (slot == 8) return; // Ignore skyblock menu/quiver slot
		//find what used to be in the slot
		ItemStack oldStack = CLIENT.player.getInventory().getNonEquipmentItems().get(slot);

		//work out the number of items changed
		int countDiff = newStack.getCount() - oldStack.getCount();

		//if item being removed completely
		if (newStack.getItem() == Items.AIR) {
			// don't count air being changed somehow
			if (oldStack.getItem() == Items.AIR) {
				return;
			}

			updateCount(removedCount, oldStack, oldStack.getCount());
			return;
		}

		newStack = newStack.copy();
		//if there are more items than before
		if (countDiff > 0) updateCount(addedCount, newStack, countDiff);
		//if there are fewer items than before
		else if (countDiff < 0) updateCount(removedCount, newStack, countDiff);
	}

	private void updateCount(Object2ObjectOpenHashMap<String, ChangeData> map, ItemStack stack, int count) {
		String neuId = stack.getNeuName();
		if (neuId.isEmpty()) neuId = stack.getHoverName().toString();
		map.compute(neuId, (_, existing) -> {
			int existingCount = existing == null ? 0 : existing.amount;
			return new ChangeData(stack, existingCount + count, System.currentTimeMillis());
		});
	}

	private record ChangeData(ItemStack item, int amount, long lastChange) {}
}
