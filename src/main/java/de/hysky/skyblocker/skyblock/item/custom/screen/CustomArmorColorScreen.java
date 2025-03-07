package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class CustomArmorColorScreen extends Screen {

	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
	//private static final ModelData PLAYER_MODEL = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);

	private final OtherClientPlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, MinecraftClient.getInstance().getGameProfile()) {
		@Override
		public boolean isInvisibleTo(PlayerEntity player) {
			return true;
		}
	};

	private final ItemStack[] armor = new ItemStack[4];
	private int selectedSlot = 0;
	private TrimSelectionWidget trimSelectionWidget;

	@Init
	public static void initCommand() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("teehee").executes(context -> {
					Scheduler.queueOpenScreen(new CustomArmorColorScreen());
					return 1;
				}))));
	}

	static boolean canEdit(ItemStack stack) {
		return stack.getItem() instanceof ArmorItem && !ItemUtils.getItemUuid(stack).isEmpty();
	}

	protected CustomArmorColorScreen() {
		super(Text.literal("Pimp My Armor"));
		DefaultedList<ItemStack> list = MinecraftClient.getInstance().player.getInventory().armor;
		for (int i = 0; i < list.size(); i++) {
			ItemStack copy = list.get(i).copy();
			armor[3 - i] = copy;
			player.getInventory().armor.set(i, copy);
		}
		while (selectedSlot < armor.length - 1 && !canEdit(armor[selectedSlot])) selectedSlot++;
	}

	@Override
	public void tick() {
		player.age++;
	}

	@Override
	protected void init() {
		super.init();
		addDrawableChild(new PlayerWidget(5, 40, 90, 165, player));
		addDrawableChild(new PieceSelectionWidget(5 + 3, 40 + 165 + 1));
		trimSelectionWidget = new TrimSelectionWidget(105, 30, width - 105 - 5, 90);
		addDrawableChild(trimSelectionWidget);
		trimSelectionWidget.setCurrentItem(armor[selectedSlot]);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, getTitle(), this.width / 2, 10, -1);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		SkyblockerConfigManager.save();
	}

	private class PieceSelectionWidget extends ClickableWidget {

		private static final Identifier HOTBAR_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "mini_hotbar");
		private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "hotbar_selection_full");

		private final boolean[] selectable;

		public PieceSelectionWidget(int x, int y) {
			super(x, y, 84, 24, Text.of(""));
			selectable = new boolean[armor.length];
			for (int i = 0; i < armor.length; i++) {
				selectable[i] = canEdit(armor[i]);
			}
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_TEXTURE, getX() + 1, getY() + 1, 82, 22);

			int hoveredSlot = -1;
			int localX = mouseX - getX() - 2;
			int localY = mouseY - getY() - 2;
			if (localY >= 0 && localY < 20) {
				hoveredSlot = localX / 20 >= armor.length ? -1 : localX / 20;
			}

			if (hoveredSlot >= 0 && selectable[hoveredSlot]) {
				int i = getX() + 2 + hoveredSlot * 20;
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				context.fill(i, getY() + 2, i + 20, getY() + 22, 0x20_FF_FF_FF);
				RenderSystem.disableBlend();
			}

			for (int i = 0; i < armor.length; i++) {
				context.drawItem(armor[i], getX() + 4 + i * 20, getY() + 4);
				if (!selectable[i]) {
					RenderSystem.disableDepthTest();
					context.drawItem(BARRIER, getX() + 4 + i * 20, getY() + 4);
					RenderSystem.enableDepthTest();
				}
			}
			context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_SELECTION_TEXTURE, getX() + selectedSlot * 20, getY(), 24, 24);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			double localX = mouseX - getX() - 2;
			double localY = mouseY - getY() - 2;
			if (localY < 0 || localY >= 20) return;
			int i = (int) (localX / 20);
			if (i < 0 || i >= armor.length || !selectable[i]) return;
			if (i != selectedSlot) {
				selectedSlot = i;
				trimSelectionWidget.setCurrentItem(armor[selectedSlot]);
			}
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return this.active && this.visible && mouseX >= this.getX() + 2 && mouseY >= this.getY() + 2 && mouseX < this.getRight() - 2 && mouseY < this.getBottom() - 2;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}
	}
}
