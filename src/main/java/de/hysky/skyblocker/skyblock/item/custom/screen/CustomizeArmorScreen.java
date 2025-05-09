package de.hysky.skyblocker.skyblock.item.custom.screen;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class CustomizeArmorScreen extends Screen {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final EquipmentSlot[] ARMOR_SLOTS = EquipmentSlot.VALUES.stream().filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new);

	private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
	//private static final ModelData PLAYER_MODEL = PlayerEntityModel.getTexturedModelData(Dilation.NONE, false);

	private final OtherClientPlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world, MinecraftClient.getInstance().getGameProfile()) {
		@Override
		public boolean isInvisibleTo(PlayerEntity player) {
			return true;
		}
		@Override
		public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {}
	};

	private final ItemStack[] armor = new ItemStack[4];
	private int selectedSlot = 0;
	private TrimSelectionWidget trimSelectionWidget;
	private ColorSelectionWidget colorSelectionWidget;

	private final Screen previousScreen;

	private final Map<String, Stuff> previousConfigs;


	@Init
	public static void initThings() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("custom").executes(context -> {
					Scheduler.queueOpenScreen(new CustomizeArmorScreen(null));
					return 1;
				}))));
		ScreenEvents.AFTER_INIT.register((client1, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof InventoryScreen inventoryScreen) {
				Screens.getButtons(inventoryScreen).add(new Button(
						((HandledScreenAccessor) inventoryScreen).getX() + 63 ,
						((HandledScreenAccessor) inventoryScreen).getY() + 10,
						inventoryScreen
				));
			}
		});
	}
	static boolean canEdit(ItemStack stack) {
		return stack.isIn(ItemTags.TRIMMABLE_ARMOR) && !ItemUtils.getItemUuid(stack).isEmpty();
	}


	private final boolean nothingCustomizable;
	protected CustomizeArmorScreen(Screen previousScreen) {
		super(Math.random() < 0.01 ? Text.translatable("skyblocker.armorCustomization.titleSecret") : Text.translatable("skyblocker.armorCustomization.title"));
		List<ItemStack> list = ItemUtils.getArmor(MinecraftClient.getInstance().player);
		for (int i = 0; i < list.size(); i++) {
			ItemStack copy = list.get(i).copy();
			armor[3 - i] = copy;
			player.equipStack(ARMOR_SLOTS[i], copy);
		}
		while (selectedSlot < armor.length - 1 && !canEdit(armor[selectedSlot])) selectedSlot++;
		this.previousScreen = previousScreen;
		nothingCustomizable = !canEdit(armor[selectedSlot]);

		ImmutableMap.Builder<String, Stuff> builder = ImmutableMap.builderWithExpectedSize(4);
		for (ItemStack stack : armor) {
			if (canEdit(stack)) {
				String uuid = ItemUtils.getItemUuid(stack);
				builder.put(uuid, new Stuff(
						SkyblockerConfigManager.get().general.customArmorTrims.containsKey(uuid) ? Optional.of(SkyblockerConfigManager.get().general.customArmorTrims.get(uuid)) : Optional.empty(),
						SkyblockerConfigManager.get().general.customDyeColors.containsKey(uuid) ? OptionalInt.of(SkyblockerConfigManager.get().general.customDyeColors.getInt(uuid)) : OptionalInt.empty(),
						SkyblockerConfigManager.get().general.customAnimatedDyes.containsKey(uuid) ? Optional.of(SkyblockerConfigManager.get().general.customAnimatedDyes.get(uuid)) : Optional.empty()
				));
			}
		}
		previousConfigs = builder.build();
	}

	@Override
	public void tick() {
		player.age++;
	}

	@Override
	protected void init() {
		super.init();
		int w = Math.min(460, width);
		int x = (width - w) / 2;

		int y = (height - 190) / 2;
		PlayerWidget playerWidget = new PlayerWidget(x + 8, y, 84, 165, player);
		addDrawableChild(playerWidget);
		PieceSelectionWidget pieceSelectionWidget = new PieceSelectionWidget(playerWidget.getX(), playerWidget.getBottom() + 1);
		addDrawableChild(pieceSelectionWidget);



		if (!nothingCustomizable) {
			trimSelectionWidget = new TrimSelectionWidget(x + 105, y, w - 105 - 5, 80);
			addDrawableChild(trimSelectionWidget);
			trimSelectionWidget.setCurrentItem(armor[selectedSlot]);

			if (colorSelectionWidget != null) colorSelectionWidget.close();
			colorSelectionWidget = new ColorSelectionWidget(trimSelectionWidget.getX(), trimSelectionWidget.getBottom() + 10, trimSelectionWidget.getWidth(), 100, textRenderer);
			addDrawableChild(colorSelectionWidget);
			colorSelectionWidget.setCurrentItem(armor[selectedSlot]);
		}

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> cancel()).position(width / 2 - 155, height - 25).build());
		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close()).position(width / 2 + 5, height - 25).build());
	}

	private void cancel() {
		previousConfigs.forEach((uuid, stuff) -> {
			stuff.armorTrimId().ifPresentOrElse(
					trim -> SkyblockerConfigManager.get().general.customArmorTrims.put(uuid, trim),
					() -> SkyblockerConfigManager.get().general.customArmorTrims.remove(uuid)
			);
			stuff.color().ifPresentOrElse(
					i -> SkyblockerConfigManager.get().general.customDyeColors.put(uuid, i),
					() -> SkyblockerConfigManager.get().general.customDyeColors.removeInt(uuid)
			);
			stuff.animatedDye().ifPresentOrElse(
					animatedDye -> SkyblockerConfigManager.get().general.customAnimatedDyes.put(uuid, animatedDye),
					() -> SkyblockerConfigManager.get().general.customAnimatedDyes.remove(uuid)
			);
		});
		close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, getTitle(), this.width / 2, 5, -1);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		super.removed();
		SkyblockerConfigManager.update(config -> {});
		if (colorSelectionWidget != null) colorSelectionWidget.close();
		// clear all the trackers cuz the color selection maybe created a bunch.
		CustomArmorAnimatedDyes.cleanTrackers();
	}

	@Override
	public void close() {
		client.setScreen(previousScreen);
	}

	private class PieceSelectionWidget extends ClickableWidget {

		private static final Identifier HOTBAR_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "armor_customization_screen/mini_hotbar");
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
				context.fill(i, getY() + 2, i + 20, getY() + 22, 0x20_FF_FF_FF);
			}

			for (int i = 0; i < armor.length; i++) {
				context.drawItem(armor[i], getX() + 4 + i * 20, getY() + 4);
				if (!selectable[i]) {
					context.drawItem(BARRIER, getX() + 4 + i * 20, getY() + 4);
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
				colorSelectionWidget.setCurrentItem(armor[selectedSlot]);
			}
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return this.active && this.visible && mouseX >= this.getX() + 2 && mouseY >= this.getY() + 2 && mouseX < this.getRight() - 2 && mouseY < this.getBottom() - 2;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}

	private record Stuff(Optional<CustomArmorTrims.ArmorTrimId> armorTrimId, OptionalInt color, Optional<CustomArmorAnimatedDyes.AnimatedDye> animatedDye) {}

	private static class Button extends ClickableWidget {

		// thanks to @yuflow
		private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "armor_customization_screen/button");

		private final Screen prevScreen;
		public Button(int x, int y, Screen screen) {
			super(x, y, 10, 10, Text.empty());
			prevScreen = screen;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, getX(), getY(), getWidth(), getHeight(), isHovered() ? 0xFFfafa96 : 0x80FFFFFF);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			MinecraftClient.getInstance().setScreen(new CustomizeArmorScreen(prevScreen));
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
	}
}
