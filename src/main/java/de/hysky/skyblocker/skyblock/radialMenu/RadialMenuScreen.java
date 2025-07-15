package de.hysky.skyblocker.skyblock.radialMenu;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;


public class RadialMenuScreen extends Screen implements ScreenHandlerListener {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int INTERNAL_RADIUS = 55;
	private static final int EXTERNAL_RADIUS = 110;

	private final RadialMenu menuType;
	private final Int2ObjectOpenHashMap<ItemStack> options = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap syncIds = new Int2IntOpenHashMap();
	private final List<RadialButton> buttons = new ArrayList<>();
	private final GenericContainerScreenHandler handler;


	public RadialMenuScreen(GenericContainerScreenHandler handler, RadialMenu type, Text title) {
		super(type.getTitle(title));
		menuType = type;

		options.clear();
		//Listen for slot updates
		this.handler = handler;
		handler.addListener(this);
	}

	@Override
	protected void init() {
		super.init();

		//create buttons
		buttons.clear();
		clearChildren();
		float angle = 0;
		float buttonArcSize = (float) ((2 * Math.PI) / options.size());
		List<Int2ObjectMap.Entry<ItemStack>> optionOrdered = new ArrayList<>(options.int2ObjectEntrySet().stream().toList());

		//check for back and close buttons to put at bottom
		Int2ObjectMap.Entry<ItemStack> backSlot = optionOrdered.stream().filter(option -> validName(option.getValue(), "Go Back")).findAny().orElse(null);
		Int2ObjectMap.Entry<ItemStack> closeSlot = optionOrdered.stream().filter(option -> validName(option.getValue(), "Close")).findAny().orElse(null);
		int bottom = Math.ceilDiv((optionOrdered.size() - ((closeSlot == null) ? 0 : 1) - ((backSlot == null) ? 0 : 1)), 2);

		optionOrdered.remove(backSlot);
		optionOrdered.remove(closeSlot);
		if (backSlot != null) {
			optionOrdered.add(bottom, backSlot);
		}
		if (closeSlot != null) {
			optionOrdered.add(bottom, closeSlot);
		}


		for (Int2ObjectMap.Entry<ItemStack> stack : optionOrdered) {
			RadialButton newButton = new RadialButton(angle, buttonArcSize, INTERNAL_RADIUS, EXTERNAL_RADIUS, stack.getValue(), button -> this.clickSlot(stack.getIntKey(),button), stack.getIntKey());
			buttons.add(newButton);
			addDrawableChild(newButton);
			angle += buttonArcSize;
		}

	}

	private static boolean validName(ItemStack stack, String validName) {
		Text customName = stack.getCustomName();
		if (customName == null) return false;
		return customName.getString().equals(validName);

	}

	private void clickSlot(int slotId, int button) {
		if (CLIENT.interactionManager == null || !syncIds.containsKey(slotId)) return;
		CLIENT.interactionManager.clickSlot(syncIds.get(slotId), slotId, button, SlotActionType.PICKUP, CLIENT.player);
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {

		if (this.handler == null || slotId >= this.handler.getRows() * 9) return;


		if (menuType.itemMatches(slotId, stack)) {
			options.put(slotId, stack);
			syncIds.put(slotId, handler.syncId);
			init();
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.render(context, mouseX, mouseY, deltaTicks);

		//render menu title
		context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, height / 2 - textRenderer.fontHeight, 0xFFFFFF);
		//draw separation line
		int textWidth = textRenderer.getWidth(getTitle());
		context.drawHorizontalLine(width / 2 - textWidth / 2, width / 2 + textWidth / 2, height / 2, 0xFFFFFFFF);
		//render current option name
		buttons.stream().filter(button -> button.hovered).findAny().ifPresent(hovered ->
				context.drawCenteredTextWithShadow(textRenderer, hovered.getName(), width / 2, height / 2 + 2, 0xFFFFFFFF)); // + 2 to move out of way of line
	}


	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

	}


}
