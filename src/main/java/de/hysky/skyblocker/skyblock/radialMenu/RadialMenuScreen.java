package de.hysky.skyblocker.skyblock.radialMenu;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.navigation.NavigationDirection;
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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;


public class RadialMenuScreen extends Screen implements ScreenHandlerListener {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int INTERNAL_RADIUS = 55;
	private static final int EXTERNAL_RADIUS = 110;
	private static long NAVIGATION_DIRECTION_COOLDOWN_DELAY = 500;

	private final RadialMenu menuType;
	private final Int2ObjectOpenHashMap<ItemStack> options = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap syncIds = new Int2IntOpenHashMap();
	private final List<RadialButton> buttons = new ArrayList<>();
	private float buttonArcSize;
	private int buttonsHoveredIndex = -1;
	private NavigationDirection lastNavigationDirectionInput = null;
	private long navigationDirectionLastTime;
	private int navigationDirection = 1;
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
		int index = 0;
		buttonArcSize = (float) ((2 * Math.PI) / options.size());
		List<Int2ObjectMap.Entry<ItemStack>> optionOrdered = new ArrayList<>(options.int2ObjectEntrySet().stream().toList());

		//find all navigation buttons for the menu
		String[] navigationNames = menuType.getNavigationItemNames();
		List<Int2ObjectMap.Entry<ItemStack>> navigationEntries = new ArrayList<>();
		for (String navigationName : navigationNames) {
			optionOrdered.stream().filter(option -> validName(option.getValue(), navigationName)).findAny().ifPresent(navigationEntries::add);
		}

		//remove then re add entries at bottom on menu
		int bottom = Math.ceilDiv((optionOrdered.size() - navigationEntries.size()), 2);
		for (Int2ObjectMap.Entry<ItemStack> entry : navigationEntries) {
			optionOrdered.remove(entry);
		}
		for (Int2ObjectMap.Entry<ItemStack> entry : navigationEntries) {
			optionOrdered.add(bottom, entry);
		}

		//create all needed radial buttons clockwise from top
		for (Int2ObjectMap.Entry<ItemStack> stack : optionOrdered) {
			RadialButton newButton = new RadialButton(angle, buttonArcSize, INTERNAL_RADIUS, EXTERNAL_RADIUS, stack.getValue(), getButtonHovered(index), stack.getIntKey());
			buttons.add(newButton);
			addDrawableChild(newButton);
			angle += buttonArcSize;
			index++;
		}

		//add button to temperately disable menu
		addDrawableChild(ButtonWidget.builder(Text.translatable("skyblocker.config.uiAndVisuals.radialMenu.hideButton"), this::hide)
				.tooltip(Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.radialMenu.hideButton.@Tooltip")))
				.position(width - 50, height - 25)
				.size(40, 15)
				.build());

	}

	private BooleanSupplier getButtonHovered(int index) {
		return () -> buttonsHoveredIndex == index;
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		super.mouseMoved(mouseX, mouseY);
		if (CLIENT.currentScreen == null) return;
		float actualX = (float) (mouseX * 2) - CLIENT.currentScreen.width;
		float actualY = (float) (mouseY * 2) - CLIENT.currentScreen.height;

		//return if over hide button
		if (actualX > CLIENT.currentScreen.width - 100 && actualY > CLIENT.currentScreen.height - 50) {
			buttonsHoveredIndex = -1;
			return;
		}

		//get angle of mouse and adjust to use same starting point and direction as buttons and see if its within bounds
		double angle = -Math.atan2(actualX, actualY) + Math.PI;
		buttonsHoveredIndex = (int) (angle / buttonArcSize);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (buttonsHoveredIndex != -1 && buttonsHoveredIndex < buttons.size()) {
			this.clickSlot(buttons.get(buttonsHoveredIndex).getLinkedSlot(), button);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch (keyCode) {
			case GLFW.GLFW_KEY_RIGHT -> this.navigateDirection(NavigationDirection.RIGHT);
			case GLFW.GLFW_KEY_LEFT -> this.navigateDirection(NavigationDirection.LEFT);
			case GLFW.GLFW_KEY_DOWN-> this.navigateDirection(NavigationDirection.DOWN);
			case GLFW.GLFW_KEY_UP -> this.navigateDirection(NavigationDirection.UP);
			case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_SPACE -> this.clickSlot();
			default -> {
				if (CLIENT.options.forwardKey.matchesKey(keyCode, scanCode)) this.navigateDirection(NavigationDirection.UP);
				else if (CLIENT.options.backKey.matchesKey(keyCode, scanCode)) this.navigateDirection(NavigationDirection.DOWN);
				else if (CLIENT.options.leftKey.matchesKey(keyCode, scanCode)) this.navigateDirection(NavigationDirection.LEFT);
				else if (CLIENT.options.rightKey.matchesKey(keyCode, scanCode)) this.navigateDirection(NavigationDirection.RIGHT);
				else return super.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		return false;
	}

	/**
	 * Changes the {@link RadialMenuScreen#buttonsHoveredIndex} towards the given direction
	 * @param direction navigated direction
	 */
	private void navigateDirection(NavigationDirection direction) {
		//if there is no current hovered index start at slot in direction
		boolean skipUpdate = buttonsHoveredIndex == -1;
		if (buttonsHoveredIndex == -1) {
			buttonsHoveredIndex = switch (direction) {
				case UP -> 0;
				case DOWN -> buttons.size() / 2;
				case LEFT -> (int) (buttons.size() * 0.75);
				case RIGHT -> (int) (buttons.size() * 0.25);
			};
		}
		//calculate new direction based on current angle. then we keep this direction while the button is pressed
		if (lastNavigationDirectionInput != direction || System.currentTimeMillis() > navigationDirectionLastTime + NAVIGATION_DIRECTION_COOLDOWN_DELAY) {
			lastNavigationDirectionInput = direction;
			float angle = buttonsHoveredIndex * buttonArcSize;
			navigationDirection = switch (direction) {
				case UP -> (angle > Math.PI) ? +1 : -1;
				case DOWN -> (angle > Math.PI) ? -1 : +1;
				case LEFT -> (angle > Math.PI / 2 && angle < Math.PI * 1.5) ? +1 : -1;
				case RIGHT -> (angle > Math.PI / 2 && angle < Math.PI * 1.5) ? -1 : +1;
			};
		}
		// update hovered index
		if (skipUpdate) return;
		buttonsHoveredIndex += navigationDirection;
		if (buttonsHoveredIndex < 0) {
			buttonsHoveredIndex = buttons.size() - 1;
		}
		if (buttonsHoveredIndex >= buttons.size()) {
			buttonsHoveredIndex = 0;
		}
		navigationDirectionLastTime = System.currentTimeMillis();
	}


	private void hide(ButtonWidget button) {
		if (CLIENT.player == null) return;
		CLIENT.setScreen(new GenericContainerScreen(handler, CLIENT.player.getInventory(), parentName));
	}

	/**
	 * Checks if an items custom name matches a string
	 * @param stack item to check name of
	 * @param validName string to compare
	 * @return if they match. {@code false} if custom name not present.
	 */
	private static boolean validName(ItemStack stack, String validName) {
		Text customName = stack.getCustomName();
		if (customName == null) return false;
		return customName.getString().equals(validName);

	}

	private void clickSlot() {
		if (buttonsHoveredIndex != -1 && buttonsHoveredIndex < buttons.size()) {
			clickSlot(buttons.get(buttonsHoveredIndex).getLinkedSlot(), 0);
		}
	}

	private void clickSlot(int slotId, int button) {
		if (CLIENT.interactionManager == null || !syncIds.containsKey(slotId)) return;
		CLIENT.interactionManager.clickSlot(syncIds.get(slotId), slotId, menuType.remapClickSlotButton(button, slotId), SlotActionType.PICKUP, CLIENT.player);
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
		if (buttonsHoveredIndex != -1 && buttonsHoveredIndex < buttons.size()) {
			context.drawCenteredTextWithShadow(textRenderer, buttons.get(buttonsHoveredIndex).getName(), width / 2, height / 2 + 2, 0xFFFFFFFF); // + 2 to move out of way of line.
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

	@Override
	public void close() {
		if (CLIENT.player != null) {
			CLIENT.player.closeHandledScreen();
		}
		super.close();
	}
}
