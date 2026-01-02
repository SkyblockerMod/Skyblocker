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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class LeapOverlay extends Screen implements ContainerListener {
	public static final String TITLE = "Spirit Leap";
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Identifier BUTTON = SkyblockerMod.id("button/button");
	private static final Identifier BUTTON_HIGHLIGHTED = SkyblockerMod.id("button/button_highlighted");
	private static final Supplier<DungeonsConfig.SpiritLeapOverlay> CONFIG = () -> SkyblockerConfigManager.get().dungeons.leapOverlay;
	private static final int BUTTON_SPACING = 8;
	private static final int BUTTON_WIDTH = 130;
	private static final int BUTTON_HEIGHT = 50;
	private final ChestMenu handler;
	private final SortedSet<PlayerReference> references = new TreeSet<>();
	private @Nullable UUID hovered;

	public LeapOverlay(ChestMenu handler) {
		super(Component.literal("Skyblocker Leap Overlay"));
		this.handler = handler;

		//Listen for slot updates
		handler.addSlotListener(this);
	}

	public static boolean shouldShowMap() {
		return DungeonManager.isClearingDungeon() && CONFIG.get().showMap;
	}

	@Override
	protected void init() {
		LinearLayout layout = LinearLayout.vertical();
		layout.spacing(32).defaultCellSetting().alignHorizontallyCenter();

		if (shouldShowMap()) layout.addChild(new MapWidget(0, 0));

		GridLayout gridWidget = new GridLayout().spacing(BUTTON_SPACING);
		RowHelper adder = gridWidget.createRowHelper(2);
		for (PlayerReference reference : references) {
			adder.addChild(new PlayerButton(0, 0, (int) (BUTTON_WIDTH * CONFIG.get().scale), (int) (BUTTON_HEIGHT * CONFIG.get().scale), reference));
		}
		layout.addChild(gridWidget);

		layout.arrangeElements();
		FrameLayout.centerInRectangle(layout, 0, 0, this.width, this.height);
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
		int containerSlots = this.handler.getRowCount() * 9;

		if (slotId < containerSlots && stack.is(Items.PLAYER_HEAD) && stack.has(DataComponents.PROFILE)) {
			ResolvableProfile profile = stack.get(DataComponents.PROFILE);
			UUID uuid = profile.partialProfile().id();
			//We take the name from the item because the name from the profile component can leave out _ characters for some reason?
			String name = stack.getHoverName().getString();
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(name);
			PlayerStatus status = switch (ItemUtils.getConcatenatedLore(stack).toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("dead") -> PlayerStatus.DEAD;
				case String s when s.contains("offline") -> PlayerStatus.OFFLINE;
				default -> null;
			};

			updateReference(new PlayerReference(uuid, name, dungeonClass, status, handler.containerId, slotId));
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu handler, int property, int value) {}

	private void updateReference(PlayerReference reference) {
		references.remove(reference);
		references.add(reference);
		rebuildWidgets();
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (super.keyPressed(input)) {
			return true;
		} else if (this.minecraft.options.keyInventory.matches(input)) {
			this.onClose();
			return true;
		} else if (CONFIG.get().leapKeybinds) {
			return switch (input.key()) {
				case GLFW.GLFW_KEY_1 -> leapToPlayer(0);
				case GLFW.GLFW_KEY_2 -> leapToPlayer(1);
				case GLFW.GLFW_KEY_3 -> leapToPlayer(2);
				case GLFW.GLFW_KEY_4 -> leapToPlayer(3);
				default -> false;
			};
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

		if (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved()) {
			this.minecraft.player.closeContainer();
		}
	}

	@Override
	public void onClose() {
		this.minecraft.player.closeContainer();
		super.onClose();
	}

	@Override
	public void removed() {
		if (this.minecraft != null && this.minecraft.player != null) {
			this.handler.removed(this.minecraft.player);
			this.handler.removeSlotListener(this);
		}
	}

	public class MapWidget extends AbstractWidget {
		public MapWidget(int x, int y) {
			super(x, y, (int) (128 * CONFIG.get().scale), (int) (128 * CONFIG.get().scale), Component.translatable("skyblocker.config.dungeons.map.fancyMap"));
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			LeapOverlay.this.hovered = DungeonMap.render(context, getX(), getY(), CONFIG.get().scale, true, mouseX - getX(), mouseY - getY(), getChildAt(mouseX, mouseY).filter(PlayerButton.class::isInstance).map(PlayerButton.class::cast).map(p -> p.reference.uuid()).orElse(null));
			HudHelper.drawBorder(context, getX(), getY(), (int) (128 * CONFIG.get().scale), (int) (128 * CONFIG.get().scale), CommonColors.WHITE);
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			if (LeapOverlay.this.hovered == null) return;

			assert minecraft != null && minecraft.player != null && minecraft.gameMode != null;
			references.stream()
					.filter(ref -> ref.uuid().equals(LeapOverlay.this.hovered))
					.findAny()
					.ifPresent(PlayerReference::clickSlot);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {}
	}

	private class PlayerButton extends Button {
		private static final int BORDER_THICKNESS = 2;
		private static final int HEAD_SIZE = 24;
		private final PlayerReference reference;

		private PlayerButton(int x, int y, int width, int height, PlayerReference reference) {
			super(x, y, width, height, Component.empty(), b -> {}, ts -> Component.empty());
			this.reference = reference;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			Identifier texture = this.isHoveredOrFocused() || reference.uuid().equals(LeapOverlay.this.hovered) ? BUTTON_HIGHLIGHTED : BUTTON;
			context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

			Matrix3x2fStack matrices = context.pose();
			float scale = CONFIG.get().scale;
			int baseX = this.getX() + BORDER_THICKNESS;
			int centreX = this.getX() + (this.getWidth() >> 1);
			int centreY = this.getY() + (this.getHeight() >> 1);
			int halfFontHeight = (int) (CLIENT.font.lineHeight * scale) >> 1;

			//Draw Player Head
			HudHelper.drawPlayerHead(context, baseX + 4, centreY - ((int) (HEAD_SIZE * scale) >> 1), (int) (HEAD_SIZE * scale), reference.uuid());

			//Draw class as heading
			matrices.pushMatrix();
			matrices.translate(centreX, this.getY() + halfFontHeight);
			matrices.scale(scale, scale);
			context.drawCenteredString(CLIENT.font, reference.dungeonClass().displayName(), 0, 0, reference.dungeonClass().color());
			matrices.popMatrix();

			//Draw name next to head
			matrices.pushMatrix();
			matrices.translate(baseX + HEAD_SIZE * scale + 8, centreY - halfFontHeight);
			matrices.scale(scale, scale);
			context.drawString(CLIENT.font, Component.literal(reference.name()), 0, 0, CommonColors.WHITE);
			matrices.popMatrix();

			if (reference.status() != null) {
				//Text
				matrices.pushMatrix();
				matrices.translate(centreX, this.getY() + this.getHeight() - (halfFontHeight * 3));
				matrices.scale(scale, scale);
				context.drawCenteredString(CLIENT.font, reference.status().text.get(), 0, 0, CommonColors.WHITE);
				matrices.popMatrix();

				//Overlay
				context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), reference.status().overlayColor);
			}
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
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
		public int compareTo(LeapOverlay.PlayerReference o) {
			return COMPARATOR.compare(this, o);
		}

		private void clickSlot() {
			CLIENT.gameMode.handleInventoryMouseClick(this.syncId(), this.slotId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, ClickType.PICKUP, CLIENT.player);
			if (CONFIG.get().enableLeapMessage) {
				MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + Constants.PREFIX.get().getString() + CONFIG.get().leapMessage.replaceAll("\\[name]", this.name), true);
			}
		}
	}

	private enum PlayerStatus {
		DEAD(() -> Component.translatable("text.skyblocker.dead").withColor(CommonColors.RED), ARGB.color(64, CommonColors.SOFT_RED)),
		OFFLINE(() -> Component.translatable("text.skyblocker.offline").withColor(CommonColors.GRAY), ARGB.color(64, CommonColors.LIGHT_GRAY));

		private final Supplier<Component> text;
		private final int overlayColor;

		PlayerStatus(Supplier<Component> text, int overlayColor) {
			this.text = text;
			this.overlayColor = overlayColor;
		}
	}
}
