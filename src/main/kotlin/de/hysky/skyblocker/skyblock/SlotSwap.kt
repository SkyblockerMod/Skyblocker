package de.hysky.skyblocker.skyblock

import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor
import de.hysky.skyblocker.util.CoroutineUtil
import de.hysky.skyblocker.utils.Constants
import de.hysky.skyblocker.utils.render.RenderHelper
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import org.slf4j.LoggerFactory

typealias Point = Vec2f

object SlotSwap {
	private const val INVENTORY_SLOT_COUNT = 36
	private val keys = arrayOfNulls<SlotSquare>(INVENTORY_SLOT_COUNT)
	private val values = arrayOfNulls<SlotSquare>(INVENTORY_SLOT_COUNT)
	private val slotMap = Object2ObjectArrayMap<SlotSquare?, SlotSquare?>(keys, values, INVENTORY_SLOT_COUNT)
	private val reversedSlotMap = Object2ObjectArrayMap<SlotSquare?, SlotSquare?>(values, keys, INVENTORY_SLOT_COUNT)

	private val logger = LoggerFactory.getLogger("Skyblocker Slot Swap")
	private var slotAtKeyPress: SlotSquare? = null
	private val configureKeybinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.skyblocker.slotSwapConfigure", GLFW.GLFW_KEY_L, "key.categories.skyblocker"))
	private val resetKeybinding: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.skyblocker.slotSwapReset", GLFW.GLFW_KEY_UNKNOWN, "key.categories.skyblocker"))

	//Self-resetting state for the reset keybinding to make sure the user wants to reset by requiring the key to be pressed twice within `delay` ms
	private var shouldResetOnNextKey = false
	private var waitJob: Job? = null
	private val mutex = Mutex() //Idk if this actually fixes anything or if I've used it correctly or not
	private const val WAIT_TIME = 500L

	private const val SLOT_SIZE = 16
	private const val HALF_SLOT_SIZE = SLOT_SIZE / 2
	private const val SOURCE_SLOT_COLOR = 0xFFE86DFF.toInt()
	private const val TARGET_SLOT_COLOR = 0xFFFFBB00.toInt()
	private const val CONFIGURING_SOURCE_SLOT_COLOR = 0xFF993AFF.toInt()
	private const val CONFIGURING_TARGET_SLOT_COLOR = 0xFFFF9214.toInt()

	@JvmStatic
	fun init() {
		ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
			if (screen !is InventoryScreen) return@register
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
			slotMap.entries.firstOrNull { entry -> entry.key?.slotId == id }
				?: reversedSlotMap.entries.firstOrNull { entry -> entry.key?.slotId == id }
		}
		if (entry?.key == null || entry.value == null) return true
		if (entry.key!!.slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.value!!.slotId, entry.key!!.slotId -  36, SlotActionType.SWAP, client.player)
			return false
		} else if (entry.value!!.slotId in PlayerScreenHandler.HOTBAR_START..<PlayerScreenHandler.HOTBAR_END) {
			client.interactionManager?.clickSlot(screen.screenHandler.syncId, entry.key!!.slotId, entry.value!!.slotId - 36, SlotActionType.SWAP, client.player)
			return false
		}
		return true
	}

	private fun onKeyPress(screen: InventoryScreen, key: Int, scancode: Int) {
		if (!configureKeybinding.matchesKey(key, scancode) || slotAtKeyPress != null) return
		slotAtKeyPress = (screen as HandledScreenAccessor).focusedSlot?.let { slot -> if (isSlotValid(slot.id)) SlotSquare(screen.x + slot.x, screen.y + slot.y, slot.id) else null }
	}

	private fun onKeyRelease(client: MinecraftClient, screen: InventoryScreen, key: Int, scancode: Int) {
		when {
			configureKeybinding.matchesKey(key, scancode) -> {
				val slotAtKeyRelease = (screen as HandledScreenAccessor).focusedSlot?.let { slot -> if (isSlotValid(slot.id)) SlotSquare(screen.x + slot.x, screen.y + slot.y, slot.id) else null }
				if (slotAtKeyPress != null && slotAtKeyRelease != null && slotAtKeyPress != slotAtKeyRelease) {
					addSlotMapping(slotAtKeyPress!!, slotAtKeyRelease)
					client.player?.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slotSwap.add", slotAtKeyPress!!.slotId, slotAtKeyRelease.slotId)), false)
				}
				slotAtKeyPress = null
			}
			resetKeybinding.matchesKey(key, scancode) -> {
				if (shouldResetOnNextKey) {
					client.player?.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.slotSwap.reset")), false)
					reset()
				}
				else {
					shouldResetOnNextKey = true
					waitJob = CoroutineUtil.globalJob.launch {
						delay(WAIT_TIME)
						mutex.withLock {
							shouldResetOnNextKey = false //Resets back to the initial state after 500ms, but if the keybinding is pressed again then this timeout is canceled and the reset happens
						}
					}
				}
			}
		}
	}

	private fun render(screen: InventoryScreen, drawContext: DrawContext) {
		for (entry in slotMap) {
			if (entry.key == null || entry.value == null) continue
			renderRectanglesAndLine(drawContext, entry.key!!, entry.value!!, SOURCE_SLOT_COLOR, TARGET_SLOT_COLOR)
		}
		if (slotAtKeyPress == null) return
		val currentSlot = (screen as HandledScreenAccessor).focusedSlot?.let { slot -> if (isSlotValid(slot.id)) SlotSquare(screen.x + slot.x, screen.y + slot.y, slot.id) else null }
		renderRectanglesAndLine(drawContext, slotAtKeyPress!!, currentSlot, CONFIGURING_SOURCE_SLOT_COLOR, CONFIGURING_TARGET_SLOT_COLOR)
	}

	private fun renderRectanglesAndLine(drawContext: DrawContext, slot1: SlotSquare, slot2: SlotSquare?, sourceColor: Int, targetColor: Int) {
		drawContext.drawBorder(slot1.x1, slot1.y1, SLOT_SIZE, SLOT_SIZE, sourceColor) // Source slot
		if (slot2 == null || slot1 == slot2) return
		drawContext.drawBorder(slot2.x1, slot2.y1, SLOT_SIZE, SLOT_SIZE, targetColor) // Target slot

		//Draws a line between the two slots' centers only between the rectangles
		RenderHelper.renderLine(slot1.center.add(1f), slot2.center.add(1f), sourceColor, targetColor, 2f)
		//Todo: Find out the intersection point of the line and the square and render from that instead of the center as it looks ugly
		//Disclaimer: Maths is hard
	}

	private fun reset() {
		slotMap.clear()
		slotAtKeyPress = null
		waitJob?.cancel()
		waitJob = null
		shouldResetOnNextKey = false
	}

	private fun isSlotValid(slotId: Int) = slotId in PlayerScreenHandler.INVENTORY_START until PlayerScreenHandler.HOTBAR_END

	private fun addSlotMapping(source: SlotSquare, aimed: SlotSquare) {
		//Remove all mappings that are related to these keys
		slotMap.remove(source)
		slotMap.remove(aimed)
		reversedSlotMap.remove(source)
		reversedSlotMap.remove(aimed)

		slotMap[source] = aimed
		reversedSlotMap[aimed] = source
	}

	private data class SlotSquare(val x1: Int, val y1: Int, val slotId: Int) {
		val center = Point((x1 + HALF_SLOT_SIZE).toFloat(), (y1 + HALF_SLOT_SIZE).toFloat())

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as SlotSquare

			return slotId == other.slotId
		}

		override fun hashCode() = slotId
	}
}
