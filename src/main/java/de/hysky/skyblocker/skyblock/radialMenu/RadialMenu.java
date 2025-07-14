package de.hysky.skyblocker.skyblock.radialMenu;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class RadialMenu extends Screen implements ScreenHandlerListener {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final MenuType menuType;
	private final Int2ObjectOpenHashMap<ItemStack> options = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap syncIds = new Int2IntOpenHashMap();
	private final List<RadialButton> buttons = new ArrayList<>();
	private final GenericContainerScreenHandler handler;


	public RadialMenu(GenericContainerScreenHandler handler, MenuType type, Text title) {
		super(title);
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
		float angle = 0;
		float buttonArcSize = (float) ((2 * Math.PI) / options.size());
		clearChildren();
		for (Int2ObjectMap.Entry<ItemStack> stack : options.int2ObjectEntrySet()) {
			RadialButton newButton = new RadialButton(angle, buttonArcSize, 50, 100, stack.getValue(), t -> this.clickSlot(stack.getIntKey()), stack.getIntKey());
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

		if (this.handler == null || slotId >= this.handler.getRows() * 9 ) return;


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
		context.drawHorizontalLine(width / 2 - textWidth /2,width / 2 + textWidth / 2, height / 2, 0xFFFFFFFF);
		//render current option name
		buttons.stream().filter(button -> button.hovered).findAny().ifPresent(hovered ->
				context.drawCenteredTextWithShadow(textRenderer, hovered.getName(), width / 2, height / 2 + 2, 0xFFFFFFFF)); // + 2 to move out of way of line
	}


	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

	}

	public enum MenuType {
		YOURBAGS("your bags", null,  List.of(Items.BLACK_STAINED_GLASS_PANE)),
		YOURSKILLS("your skills", null,  List.of(Items.BLACK_STAINED_GLASS_PANE)),
		FASTTRAVEL("(fast travel)|(.* warps)", null, List.of(Items.BLACK_STAINED_GLASS_PANE)),
		SKYBLOCKMENU("skyblock menu", null,  List.of(Items.BLACK_STAINED_GLASS_PANE));

		final Pattern name;
		final List<Integer> slots;
		final List<Item> blackList;

		MenuType(String name, @Nullable List<Integer> slots, @Nullable List<Item> blackList) {
			this.name = Pattern.compile(name);
			this.slots = slots;
			this.blackList = blackList;
		}

		public boolean match(String name) {
			return false;
			//return this.name.matcher(name).matches();
		}

		public boolean itemMatches(int slotId, ItemStack stack) {
			if (blackList != null) {
				if (blackList.contains(stack.getItem())) return false;
			}
			if (slots != null) {
				if (!slots.contains(slotId)) return false;
			}

			return true;
		}
	}
}
