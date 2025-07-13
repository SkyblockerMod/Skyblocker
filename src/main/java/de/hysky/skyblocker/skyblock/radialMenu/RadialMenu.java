package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


public class RadialMenu extends Screen implements ScreenHandlerListener {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final MenuType menuType;
	private final Int2ObjectOpenHashMap<ItemStack> options = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap syncIds = new Int2IntOpenHashMap();
	private final List<RadialButton> buttons = new ArrayList<>();


	public RadialMenu(GenericContainerScreenHandler handler, MenuType type, Text title) {
		super(title);
		menuType = type;

		options.clear();
		//Listen for slot updates
		handler.addListener(this);
	}

	@Override
	protected void init() {
		super.init();
		//wait for all options to be loaded
		if (options.size() != menuType.slots.size()) return;

		//create buttons	System.out.println(slotId + " " + stack.getName());
		buttons.clear();
		float angle = 0;
		float buttonArcSize = (float) ((2 * Math.PI) / options.size());
		clearChildren();
		for (Int2ObjectMap.Entry<ItemStack> stack : options.int2ObjectEntrySet()) {
			RadialButton newButton = new RadialButton(angle, buttonArcSize, 50, 100, "temp", stack.getValue(), t -> this.clickSlot(stack.getIntKey()), stack.getIntKey());
			buttons.add(newButton);
			addDrawableChild(newButton);
			angle += buttonArcSize;
		}

	}

	private void clickSlot(int slotId) {
		if (CLIENT.interactionManager == null || !syncIds.containsKey(slotId)) return;
		CLIENT.interactionManager.clickSlot(syncIds.get(slotId), slotId, GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.PICKUP, CLIENT.player);
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
		if (menuType.slots.contains(slotId)) {
			options.put(slotId, stack);
			syncIds.put(slotId, handler.syncId);
			init();
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

	}

	public enum MenuType {
		YOURBAGS("your bags", List.of(19, 20, 21, 23, 24, 25, 48, 49)),
		YOURSKILLS("your skills", List.of(4, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 32, 33, 34, 48, 49)),
		SKYBLOCKMENU("skyblock menu", List.of(10, 13, 19, 20, 21, 22, 23, 24, 25, 29, 30, 31, 32, 33, 47, 48, 49, 50, 51));

		final String name;
		final List<Integer> slots;

		MenuType(String name, List<Integer> slots) {
			this.name = name;
			this.slots = slots;
		}

		public boolean match(String name) {
			//return false;
			return this.name.equals(name);
		}
	}
}
