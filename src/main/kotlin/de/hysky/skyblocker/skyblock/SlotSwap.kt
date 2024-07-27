package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.config.SkyblockerConfigManager
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.util.CoroutineUtil
import de.hysky.skyblocker.util.KtUtil.sendSkyblockerMessage
import de.hysky.skyblocker.utils.render.RenderHelper
import dev.isxander.yacl3.config.v2.api.SerialEntry
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
import org.lwjgl.glfw.GLFW

typealias Point = Vec2f

object SlotSwap {
	private val slotMap: MutableList<Pair<SlotSquare?, SlotSquare?>> get() = config.slotSquareMap

	private val config get() = SkyblockerConfigManager.get().general.slotSwap

	private var slotAtKeyPress: SlotSquare? = null
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
		}
	}

	private fun onMouseClick(client: MinecraftClient, screen: InventoryScreen, button: Int): Boolean {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !InputUtil.isKeyPressed(client.window.handle, GLFW.GLFW_KEY_LEFT_SHIFT)) return true

		val entry = (screen as HandledScreenAccessor).focusedSlot?.id?.let { id ->
			slotMap.firstOrNull { (key, value) -> key?.slotId == id || value?.slotId == id }
		} ?: return true //If the slot is not in the mapping, then we don't care

		if (entry.first == null || entry.second == null) return true
		if (isHotbarSlot(entry.first!!.slotId)) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.second!!.slotId, entry.first!!.slotId - 36, SlotActionType.SWAP, client.player)
			return false
		} else if (isHotbarSlot(entry.second!!.slotId)) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.first!!.slotId, entry.second!!.slotId - 36, SlotActionType.SWAP, client.player)
			return false
		}
		return true
	}

	private fun onKeyPress(screen: InventoryScreen, key: Int, scancode: Int) {
		if (!configureKeybinding.matchesKey(key, scancode) || slotAtKeyPress != null) return
		slotAtKeyPress = screen.getSquareFromFocusedSlot()
	}

	private fun onKeyRelease(client: MinecraftClient, screen: InventoryScreen, key: Int, scancode: Int) {
		when {
			configureKeybinding.matchesKey(key, scancode) -> {
				val slotAtKeyRelease = screen.getSquareFromFocusedSlot()
				if (slotAtKeyPress != null && slotAtKeyRelease != null && slotAtKeyPress != slotAtKeyRelease
					&& (isHotbarSlot(slotAtKeyPress!!.slotId) || isHotbarSlot(slotAtKeyRelease.slotId))) { // At least one slot has to be in the hotbar
					addSlotMapping(slotAtKeyPress!!, slotAtKeyRelease)
					client.player?.sendSkyblockerMessage(Text.translatable("skyblocker.slotSwap.add", slotAtKeyPress!!.slotId, slotAtKeyRelease.slotId))
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
				waitJob = CoroutineUtil.globalJob.launch {
					delay(WAIT_TIME)
					shouldResetOnNextKey = false //Resets back to the initial state after 500ms, but if the keybinding is pressed again then this timeout is canceled and the reset happens
				}
			}
		}
	}

	private fun render(screen: InventoryScreen, drawContext: DrawContext) {
		//Slot mappings
		for ((key, value) in slotMap) {
			if (key == null || value == null) continue
			renderRectanglesAndLine(drawContext, key, value, config.sourceSlotColor.rgb, config.targetSlotColor.rgb)
		}
		//Configuring state
		if (slotAtKeyPress == null) return
		val currentSlot = screen.getSquareFromFocusedSlot()
		renderRectanglesAndLine(drawContext, slotAtKeyPress!!, currentSlot, CONFIGURING_SOURCE_SLOT_COLOR, CONFIGURING_TARGET_SLOT_COLOR)
	}

	private fun InventoryScreen.getSquareFromFocusedSlot() = (this as HandledScreenAccessor).focusedSlot?.let { slot -> if (isSlotValid(slot.id)) SlotSquare(x + slot.x, y + slot.y, slot.id) else null }

	private fun renderRectanglesAndLine(drawContext: DrawContext, slot1: SlotSquare, slot2: SlotSquare?, sourceColor: Int, targetColor: Int) {
		drawContext.drawBorder(slot1.x, slot1.y, SLOT_SIZE, SLOT_SIZE, sourceColor) // Source slot
		if (slot2 == null || slot1 == slot2) return
		drawContext.drawBorder(slot2.x, slot2.y, SLOT_SIZE, SLOT_SIZE, targetColor) // Target slot

		//Draws a line between the two slots' centers only between the rectangles
		RenderHelper.renderLine(slot1.center, slot2.center, sourceColor, targetColor, LINE_WIDTH)
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

	private fun isSlotValid(slotId: Int) = slotId in PlayerScreenHandler.INVENTORY_START..<PlayerScreenHandler.HOTBAR_END

	private fun isHotbarSlot(slotId: Int) = slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END

	private fun addSlotMapping(source: SlotSquare, aimed: SlotSquare) {
		//Remove all mappings that are related to these keys
		slotMap.removeAll { (key, value) ->  key == source || value == source || key == aimed || value == aimed }
		slotMap += source to aimed
		SkyblockerConfigManager.save()
	}

	@JvmRecord
	data class SlotSquare(@SerialEntry val x: Int, @SerialEntry val y: Int, @SerialEntry val slotId: Int) {
		val center get() = Point(x + HALF_SLOT_SIZE + OFFSET_TO_CENTER, y + HALF_SLOT_SIZE + OFFSET_TO_CENTER)
	}
}
