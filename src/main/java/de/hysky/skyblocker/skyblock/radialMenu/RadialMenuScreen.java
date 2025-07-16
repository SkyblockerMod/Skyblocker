package de.hysky.skyblocker.skyblock.radialMenu;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
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
	private final Text parentName;

	public RadialMenuScreen(GenericContainerScreenHandler handler, RadialMenu type, Text title) {
		super(type.getTitle(title));
		menuType = type;

		this.parentName = title;


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

		//check for back and close buttons to put in the middle of the list so they appear at the bottom
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

		//create all needed radial buttons clockwise from top
		for (Int2ObjectMap.Entry<ItemStack> stack : optionOrdered) {
			RadialButton newButton = new RadialButton(angle, buttonArcSize, INTERNAL_RADIUS, EXTERNAL_RADIUS, stack.getValue(), this::clickSlot, stack.getIntKey());
			buttons.add(newButton);
			addDrawableChild(newButton);
			angle += buttonArcSize;
		}

		//add button to temperately disable menu
		addDrawableChild(ButtonWidget.builder(Text.translatable("skyblocker.config.uiAndVisuals.radialMenu.hideButton"), this::hide)
				.tooltip(Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.radialMenu.hideButton.@Tooltip")))
				.position(width - 50, height - 25)
				.size(40, 15)
				.build());

	}

	private void hide(ButtonWidget button) {
		CLIENT.setScreen(new GenericContainerScreen(handler, CLIENT.player.getInventory(), parentName));
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
				context.drawCenteredTextWithShadow(textRenderer, hovered.getName() == null ? "Error" : hovered.getName(), width / 2, height / 2 + 2, 0xFFFFFFFF)); // + 2 to move out of way of line. Skyhanni seams to sometimes give us a null value
	}


	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

	}

	@Override
	public void close() {
		CLIENT.player.closeHandledScreen();
		super.close();
	}


}
