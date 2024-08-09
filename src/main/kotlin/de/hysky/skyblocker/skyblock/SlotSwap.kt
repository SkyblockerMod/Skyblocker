package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.utils.CoroutineUtils
import de.hysky.skyblocker.utils.KtUtil.sendSkyblockerMessage
import de.hysky.skyblocker.utils.render.RenderHelper
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.math.Vec2f
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW

typealias Point = Vec2f

object SlotSwap {
	private val slotMap get() = config.slotSquareMap

	private val config get() = SkyblockerConfigManager.get().general.slotSwap

	//This field is used instead of the field in MinecraftClient, so we don't have to type check the current screen every time.
	private var currentScreen: InventoryScreen? = null
	private var slotAtKeyPress: Int? = null
	private val configureKeybinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.skyblocker.slotSwapConfigure", GLFW.GLFW_KEY_L, "key.categories.skyblocker"))
	private val resetKeybinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.skyblocker.slotSwapReset", GLFW.GLFW_KEY_UNKNOWN, "key.categories.skyblocker"))

	//Self-resetting state for the reset keybinding to make sure the user wants to reset by requiring the key to be pressed twice within `delay` ms
	private var shouldResetOnNextKey = false
	private var waitJob: Job? = null
	private const val WAIT_TIME = 500L

	private const val SLOT_SIZE = 16
	private const val HALF_SLOT_SIZE = SLOT_SIZE / 2

	//These 2 aren't configurable, just because
	private const val CONFIGURING_SOURCE_SLOT_COLOR = 0xFF993AFF.toInt()
	private const val CONFIGURING_TARGET_SLOT_COLOR = 0xFFFF9214.toInt()
	private const val LINE_WIDTH = 2f
	private const val OFFSET_TO_CENTER = 1f // This is needed because SlotSquares' x and y values are 1 less than what they should be for being visually in the center.

	@JvmStatic
	fun init() {
		ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
			//Since this event is called for each screen, we can just check the config enabled status here once and for all
			if (screen !is InventoryScreen || !config.enableSlotSwap) return@register
			currentScreen = screen
			ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, scancode, _ ->
				onKeyPress(screen, key, scancode)
			}
			ScreenKeyboardEvents.afterKeyRelease(screen).register { _, key, scancode, _ ->
				onKeyRelease(client, screen, key, scancode)
			}
			ScreenEvents.afterRender(screen).register { _, drawContext, _, _, _ ->
				render(screen, drawContext)
			}
			ScreenMouseEvents.allowMouseClick(screen).register { _, _, _, button ->
				onMouseClick(client, screen, button)
			}
			ScreenEvents.remove(screen).register {
				currentScreen = null
			}
		}
	}

	private fun onMouseClick(client: MinecraftClient, screen: InventoryScreen, button: Int): Boolean {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT)) return true

		val entry: IntIntImmutablePair = (screen as HandledScreenAccessor).focusedSlot?.id?.let { id ->
			slotMap.firstOrNull { pair -> pair.keyInt() == id || pair.valueInt() == id }
		} ?: return true

		if (isHotbarSlot(entry.keyInt())) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.valueInt(), entry.keyInt() - 36, SlotActionType.SWAP, client.player)
			return false
		} else if (isHotbarSlot(entry.valueInt())) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.keyInt(), entry.valueInt() - 36, SlotActionType.SWAP, client.player)
			return false
		}
		return true
	}

	private fun onKeyPress(screen: InventoryScreen, key: Int, scancode: Int) {
		if (!configureKeybinding.matchesKey(key, scancode) || slotAtKeyPress != null) return
		slotAtKeyPress = screen.getFocusedSlotId()
	}

	private fun onKeyRelease(client: MinecraftClient, screen: InventoryScreen, key: Int, scancode: Int) {
		when {
			configureKeybinding.matchesKey(key, scancode) -> {
				val slotAtKeyRelease = screen.getFocusedSlotId()
				if (slotAtKeyPress != null && slotAtKeyRelease != null && slotAtKeyPress != slotAtKeyRelease
					&& (isHotbarSlot(slotAtKeyPress!!) || isHotbarSlot(slotAtKeyRelease))
				) { // At least one slot has to be in the hotbar
					addSlotMapping(slotAtKeyPress!!, slotAtKeyRelease)
					client.player?.sendSkyblockerMessage(Text.translatable("skyblocker.slotSwap.add", slotAtKeyPress!!, slotAtKeyRelease))
				}
				slotAtKeyPress = null
			}

			resetKeybinding.matchesKey(key, scancode) -> {
				if (shouldResetOnNextKey) {
					client.player?.sendSkyblockerMessage(Text.translatable("skyblocker.slotSwap.reset"))
					reset()
					return
				}
				shouldResetOnNextKey = true
				waitJob = CoroutineUtils.globalJob.launch {
					delay(WAIT_TIME)
					shouldResetOnNextKey = false //Resets back to the initial state after 500ms, but if the keybinding is pressed again then this timeout is canceled and the reset happens
				}
			}
		}
	}

	private fun render(screen: InventoryScreen, drawContext: DrawContext) {
		//Slot mappings
		for (entry in slotMap) {
			renderRectanglesAndLine(drawContext, entry.keyInt(), entry.valueInt(), config.sourceSlotColor.rgb, config.targetSlotColor.rgb)
		}
		//Configuring state
		if (slotAtKeyPress == null) return
		val currentSlot = screen.getFocusedSlotId()
		renderRectanglesAndLine(drawContext, slotAtKeyPress!!, currentSlot, CONFIGURING_SOURCE_SLOT_COLOR, CONFIGURING_TARGET_SLOT_COLOR)
	}

	private fun InventoryScreen.getFocusedSlotId() = (this as HandledScreenAccessor).focusedSlot?.let { slot -> if (isSlotValid(slot.id)) slot.id else null }

	private fun renderRectanglesAndLine(drawContext: DrawContext, slotId1: Int, slotId2: Int?, sourceColor: Int, targetColor: Int) {
		val pos1 = getPosFromSlotId(slotId1) ?: return
		drawContext.drawBorder(pos1.x, pos1.y, SLOT_SIZE, SLOT_SIZE, sourceColor) // Source slot
		if (slotId1 == slotId2 || slotId2 == null) return
		val pos2 = getPosFromSlotId(slotId2) ?: return
		drawContext.drawBorder(pos2.x, pos2.y, SLOT_SIZE, SLOT_SIZE, targetColor) // Target slot

		//Draws a line between the two slots' centers only between the rectangles
		RenderHelper.renderLine(getCenterFromPos(pos1), getCenterFromPos(pos2), sourceColor, targetColor, LINE_WIDTH)
		//Todo: Find out the intersection point of the line and the square and render from that instead of the center as it looks ugly
		//Disclaimer: Maths is hard
	}

	private fun reset() {
		slotMap.clear()
		SkyblockerConfigManager.save()
		slotAtKeyPress = null
		waitJob?.cancel()
		waitJob = null
		shouldResetOnNextKey = false
	}

	private fun isSlotValid(slotId: Int) = slotId in PlayerScreenHandler.EQUIPMENT_START..<PlayerScreenHandler.HOTBAR_END

	private fun isHotbarSlot(slotId: Int) = slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END

	private fun addSlotMapping(source: Int, aimed: Int) {
		//Remove all mappings that are related to these keys
		slotMap.removeAll { pair -> pair.keyInt() == source || pair.valueInt() == source || pair.keyInt() == aimed || pair.valueInt() == aimed }
		slotMap += IntIntImmutablePair.of(source, aimed)
		SkyblockerConfigManager.save()
	}

	private fun getPosFromSlotId(slotId: Int) = currentScreen?.screenHandler?.getSlot(slotId)?.let { slot ->
		(currentScreen as HandledScreenAccessor).let { screen ->
			Vector2i(slot.x + screen.x, slot.y + screen.y)
		}
	}

	private fun getCenterFromPos(pos: Vector2i) = Point(pos.x + HALF_SLOT_SIZE + OFFSET_TO_CENTER, pos.y + HALF_SLOT_SIZE + OFFSET_TO_CENTER)
}
