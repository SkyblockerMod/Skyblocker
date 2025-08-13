package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.widget.GridWidget.Adder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Supplier;

public class LeapOverlay extends Screen implements ScreenHandlerListener {
	public static final String TITLE = "Spirit Leap";
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Identifier BUTTON = Identifier.of(SkyblockerMod.NAMESPACE, "button/button");
	private static final Identifier BUTTON_HIGHLIGHTED = Identifier.of(SkyblockerMod.NAMESPACE, "button/button_highlighted");
	private static final Supplier<DungeonsConfig.SpiritLeapOverlay> CONFIG = () -> SkyblockerConfigManager.get().dungeons.leapOverlay;
	private static final int BUTTON_SPACING = 8;
	private static final int BUTTON_WIDTH = 130;
	private static final int BUTTON_HEIGHT = 50;
	private final GenericContainerScreenHandler handler;
	private final SortedSet<PlayerReference> references = new TreeSet<>();
	@Nullable
	private UUID hovered;

	public LeapOverlay(GenericContainerScreenHandler handler) {
		super(Text.literal("Skyblocker Leap Overlay"));
		this.handler = handler;
		this.client = CLIENT; //Stops an NPE due to items being sent (and calling clearAndInit) before the main init method can initialize this field

		//Listen for slot updates
		handler.addListener(this);
	}

	public static boolean shouldShowMap() {
		return DungeonManager.isClearingDungeon() && CONFIG.get().showMap;
	}

	@Override
	protected void init() {
		DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
		layout.spacing(32).getMainPositioner().alignHorizontalCenter();

		if (shouldShowMap()) layout.add(new MapWidget(0, 0));

		GridWidget gridWidget = new GridWidget().setSpacing(BUTTON_SPACING);
		Adder adder = gridWidget.createAdder(2);
		for (PlayerReference reference : references) {
			adder.add(new PlayerButton(0, 0, (int) (BUTTON_WIDTH * CONFIG.get().scale), (int) (BUTTON_HEIGHT * CONFIG.get().scale), reference));
		}
		layout.add(gridWidget);

		layout.refreshPositions();
		SimplePositioningWidget.setPos(layout, 0, 0, this.width, this.height);
		layout.forEachChild(this::addDrawableChild);
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
		int containerSlots = this.handler.getRows() * 9;

		if (slotId < containerSlots && stack.isOf(Items.PLAYER_HEAD) && stack.contains(DataComponentTypes.PROFILE)) {
			ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);

			//All heads in the leap menu have the id property set
			if (profile.uuid().isEmpty()) return;

			UUID uuid = profile.uuid().get();
			//We take the name from the item because the name from the profile component can leave out _ characters for some reason?
			String name = stack.getName().getString();
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(name);
			PlayerStatus status = switch (ItemUtils.getConcatenatedLore(stack).toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("dead") -> PlayerStatus.DEAD;
				case String s when s.contains("offline") -> PlayerStatus.OFFLINE;
				default -> null;
			};

			updateReference(new PlayerReference(uuid, name, dungeonClass, status, handler.syncId, slotId));
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

	private void updateReference(PlayerReference reference) {
		references.remove(reference);
		references.add(reference);
		clearAndInit();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		} else if (CONFIG.get().leapKeybinds) {
			boolean result = switch (keyCode) {
				case GLFW.GLFW_KEY_1 -> leapToPlayer(0);
				case GLFW.GLFW_KEY_2 -> leapToPlayer(1);
				case GLFW.GLFW_KEY_3 -> leapToPlayer(2);
				case GLFW.GLFW_KEY_4 -> leapToPlayer(3);

				default -> false;
			};

			if (result) return true;
		}
		return false;
	}

	private boolean leapToPlayer(int index) {
		PlayerReference[] players = references.toArray(PlayerReference[]::new);

		if (players.length > 0 && index < players.length) {
			players[index].clickSlot();
			return true;
		}

		return false;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.client.player.isAlive() || this.client.player.isRemoved()) {
			this.client.player.closeHandledScreen();
		}
	}

	@Override
	public void close() {
		this.client.player.closeHandledScreen();
		super.close();
	}

	@Override
	public void removed() {
		if (this.client != null && this.client.player != null) {
			this.handler.onClosed(this.client.player);
			this.handler.removeListener(this);
		}
	}

	public class MapWidget extends ClickableWidget {
		public MapWidget(int x, int y) {
			super(x, y, (int) (128 * CONFIG.get().scale), (int) (128 * CONFIG.get().scale), Text.translatable("skyblocker.config.dungeons.map.fancyMap"));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			LeapOverlay.this.hovered = DungeonMap.render(context, getX(), getY(), CONFIG.get().scale, true, mouseX - getX(), mouseY - getY(), hoveredElement(mouseX, mouseY).filter(PlayerButton.class::isInstance).map(PlayerButton.class::cast).map(p -> p.reference.uuid()).orElse(null));
			context.drawBorder(getX(), getY(), (int) (128 * CONFIG.get().scale), (int) (128 * CONFIG.get().scale), Colors.WHITE);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			if (LeapOverlay.this.hovered == null) return;

			assert client != null && client.player != null && client.interactionManager != null;
			references.stream()
					.filter(ref -> ref.uuid().equals(LeapOverlay.this.hovered))
					.findAny()
					.ifPresent(PlayerReference::clickSlot);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private class PlayerButton extends ButtonWidget {
		private static final int BORDER_THICKNESS = 2;
		private static final int HEAD_SIZE = 24;
		private final PlayerReference reference;

		private PlayerButton(int x, int y, int width, int height, PlayerReference reference) {
			super(x, y, width, height, Text.empty(), b -> {}, ts -> Text.empty());
			this.reference = reference;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			Identifier texture = this.isSelected() || reference.uuid().equals(LeapOverlay.this.hovered) ? BUTTON_HIGHLIGHTED : BUTTON;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

			Matrix3x2fStack matrices = context.getMatrices();
			float scale = CONFIG.get().scale;
			int baseX = this.getX() + BORDER_THICKNESS;
			int centreX = this.getX() + (this.getWidth() >> 1);
			int centreY = this.getY() + (this.getHeight() >> 1);
			int halfFontHeight = (int) (CLIENT.textRenderer.fontHeight * scale) >> 1;

			//Draw Player Head
			HudHelper.drawPlayerHead(context, baseX + 4, centreY - ((int) (HEAD_SIZE * scale) >> 1), (int) (HEAD_SIZE * scale), reference.uuid());

			//Draw class as heading
			matrices.pushMatrix();
			matrices.translate(centreX, this.getY() + halfFontHeight);
			matrices.scale(scale, scale);
			context.drawCenteredTextWithShadow(CLIENT.textRenderer, reference.dungeonClass().displayName(), 0, 0, reference.dungeonClass().color());
			matrices.popMatrix();

			//Draw name next to head
			matrices.pushMatrix();
			matrices.translate(baseX + HEAD_SIZE * scale + 8, centreY - halfFontHeight);
			matrices.scale(scale, scale);
			context.drawTextWithShadow(CLIENT.textRenderer, Text.literal(reference.name()), 0, 0, Colors.WHITE);
			matrices.popMatrix();

			if (reference.status() != null) {
				//Text
				matrices.pushMatrix();
				matrices.translate(centreX, this.getY() + this.getHeight() - (halfFontHeight * 3));
				matrices.scale(scale, scale);
				context.drawCenteredTextWithShadow(CLIENT.textRenderer, reference.status().text.get(), 0, 0, Colors.WHITE);
				matrices.popMatrix();

				//Overlay
				context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), reference.status().overlayColor);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			reference.clickSlot();
		}
	}

	private record PlayerReference(UUID uuid, String name, DungeonClass dungeonClass, @Nullable PlayerStatus status, int syncId, int slotId) implements Comparable<PlayerReference> {
		/**
		 * Compares first by class name then by player name.
		 */
		private static final Comparator<PlayerReference> COMPARATOR = Comparator.<PlayerReference, String>comparing(ref -> ref.dungeonClass().displayName()).thenComparing(PlayerReference::name);

		@Override
		public boolean equals(Object obj) {
			return obj instanceof PlayerReference playerRef && uuid.equals(playerRef.uuid);
		}

		@Override
		public int hashCode() {
			return uuid.hashCode();
		}

		@Override
		public int compareTo(@NotNull LeapOverlay.PlayerReference o) {
			return COMPARATOR.compare(this, o);
		}

		private void clickSlot() {
			CLIENT.interactionManager.clickSlot(this.syncId(), this.slotId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.PICKUP, CLIENT.player);
			if (CONFIG.get().enableLeapMessage) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + CONFIG.get().leapMessage.replaceAll("\\[name]", this.name), false);
			}
		}
	}

	private enum PlayerStatus {
		DEAD(() -> Text.translatable("text.skyblocker.dead").withColor(Colors.RED), ColorHelper.withAlpha(64, Colors.LIGHT_RED)),
		OFFLINE(() -> Text.translatable("text.skyblocker.offline").withColor(Colors.GRAY), ColorHelper.withAlpha(64, Colors.LIGHT_GRAY));

		private final Supplier<Text> text;
		private final int overlayColor;

		PlayerStatus(Supplier<Text> text, int overlayColor) {
			this.text = text;
			this.overlayColor = overlayColor;
		}
	}
}
