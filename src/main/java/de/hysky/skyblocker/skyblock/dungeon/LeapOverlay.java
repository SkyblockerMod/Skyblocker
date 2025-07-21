package de.hysky.skyblocker.skyblock.dungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.realms.util.RealmsUtil;
import net.minecraft.client.gui.widget.GridWidget.Adder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
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

public class LeapOverlay extends Screen implements ScreenHandlerListener {
	public static final String TITLE = "Spirit Leap";
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Identifier BUTTON = Identifier.of(SkyblockerMod.NAMESPACE, "button/button");
	private static final Identifier BUTTON_HIGHLIGHTED = Identifier.of(SkyblockerMod.NAMESPACE, "button/button_highlighted");
	private static final int BUTTON_SPACING = 8;
	private static final float SCALE = 1.5f;
	private static final int BUTTON_WIDTH = (int) (130f * SCALE);
	private static final int BUTTON_HEIGHT = (int) (50f * SCALE);
	/**
	 * Compares first by class name then by player name.
	 */
	private static final Comparator<PlayerReference> COMPARATOR = Comparator.<PlayerReference, String>comparing(ref -> ref.dungeonClass().displayName())
			.thenComparing(PlayerReference::name);
	private final GenericContainerScreenHandler handler;
	private final List<PlayerReference> references = new ArrayList<>();

	public LeapOverlay(GenericContainerScreenHandler handler) {
		super(Text.literal("Skyblocker Leap Overlay"));
		this.handler = handler;
		this.client = CLIENT; //Stops an NPE due to items being sent (and calling clearAndInit) before the main init method can initialize this field

		//Listen for slot updates
		handler.addListener(this);
	}

	@Override
	protected void init() {
		GridWidget gridWidget = new GridWidget();
		gridWidget.setSpacing(BUTTON_SPACING);

		Adder adder = gridWidget.createAdder(2);

		for (PlayerReference reference : references) {
			adder.add(new PlayerButton(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT, reference));
		}

		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0.5f);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
		int containerSlots = this.handler.getRows() * 9;

		if (slotId < containerSlots && stack.isOf(Items.PLAYER_HEAD) && stack.contains(DataComponentTypes.PROFILE)) {
			ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);

			//All heads in the leap menu have the id property set
			if (profile.id().isEmpty()) return;

			UUID uuid = profile.id().get();
			//We take the name from the item because the name from the profile component can leave out _ characters for some reason?
			String name = stack.getName().getString();
			DungeonClass dungeonClass = DungeonPlayerManager.getClassFromPlayer(name);
			PlayerStatus status = switch (ItemUtils.getConcatenatedLore(stack).toLowerCase(Locale.ENGLISH)) {
				case String s when s.contains("dead") -> PlayerStatus.DEAD;
				case String s when s.contains("offline") -> PlayerStatus.OFFLINE;
				default -> null;
			};

			PlayerReference reference = new PlayerReference(uuid, name, dungeonClass, status, handler.syncId, slotId);
			tryInsertReference(reference);
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

	/**
	 * Inserts the {@code reference} into the list if it doesn't exist or updates current value then updates the screen.
	 */
	private void tryInsertReference(PlayerReference reference) {
		Optional<PlayerReference> existing = references.stream()
				.filter(ref -> ref.uuid().equals(reference.uuid()))
				.findAny();

		if (existing.isEmpty()) {
			references.add(reference);
			references.sort(COMPARATOR);

			this.clearAndInit();
		} else if (!existing.get().equals(reference)) {
			references.remove(existing.get());
			references.add(reference);
			references.sort(COMPARATOR);

			this.clearAndInit();
		}
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

	private static class PlayerButton extends ButtonWidget {
		private static final int BORDER_THICKNESS = 2;
		private static final int HEAD_SIZE = 32;
		private final PlayerReference reference;

		private PlayerButton(int x, int y, int width, int height, PlayerReference reference) {
			super(x, y, width, height, Text.empty(), b -> {}, ts -> Text.empty());
			this.reference = reference;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			Identifier texture = this.isSelected() ? BUTTON_HIGHLIGHTED : BUTTON;
			context.drawGuiTexture(RenderLayer::getGuiTextured, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

			int baseX = this.getX() + BORDER_THICKNESS;
			int centreX = this.getX() + (this.getWidth() >> 1);
			int centreY = this.getY() + (this.getHeight() >> 1);

			//Draw Player Head
			RealmsUtil.drawPlayerHead(context, baseX + 4, centreY - (HEAD_SIZE >> 1), HEAD_SIZE, reference.uuid());

			MatrixStack matrices = context.getMatrices();
			int halfFontHeight = (int) (CLIENT.textRenderer.fontHeight * SCALE) >> 1;

			//Draw class as heading
			matrices.push();
			matrices.translate(centreX, this.getY() + halfFontHeight, 0f);
			matrices.scale(SCALE, SCALE, 1f);
			context.drawCenteredTextWithShadow(CLIENT.textRenderer, reference.dungeonClass().displayName(), 0, 0, ColorHelper.fullAlpha(reference.dungeonClass().color()));
			matrices.pop();

			//Draw name next to head
			matrices.push();
			matrices.translate(baseX + HEAD_SIZE + 8, centreY - halfFontHeight, 0f);
			matrices.scale(SCALE, SCALE, 1f);
			context.drawTextWithShadow(CLIENT.textRenderer, Text.literal(reference.name()), 0, 0, Colors.WHITE);
			matrices.pop();

			if (reference.status() != null) {
				//Text
				matrices.push();
				matrices.translate(centreX, this.getY() + this.getHeight() - (halfFontHeight * 3), 0f);
				matrices.scale(SCALE, SCALE, 1f);
				context.drawCenteredTextWithShadow(CLIENT.textRenderer, reference.status().text.get(), 0, 0, Colors.WHITE);
				matrices.pop();

				//Overlay
				matrices.push();
				matrices.scale(1f, 1f, 1f);
				context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), reference.status().overlayColor);
				matrices.pop();
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			CLIENT.interactionManager.clickSlot(reference.syncId(), reference.slotId(), GLFW.GLFW_MOUSE_BUTTON_LEFT, SlotActionType.PICKUP, CLIENT.player);
		}
	}

	private record PlayerReference(UUID uuid, String name, DungeonClass dungeonClass, @Nullable PlayerStatus status, int syncId, int slotId) {}

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
