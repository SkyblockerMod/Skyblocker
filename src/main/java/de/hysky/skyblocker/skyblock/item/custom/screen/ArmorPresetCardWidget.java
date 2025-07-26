package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreset;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreviewStorage;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPresets;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenTexts;

import java.util.List;

import net.minecraft.util.Identifier;

public class ArmorPresetCardWidget extends ClickableWidget {
	public static final int WIDTH = 90;
	public static final int HEIGHT = 183;
	private static final EquipmentSlot[] ARMOR_SLOTS = EquipmentSlot.VALUES.stream()
			.filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
			.toArray(EquipmentSlot[]::new);
	private static List<ItemStack> BASE_ARMOR;

	private final ArmorPreset preset;
	private static final Identifier DELETE_ICON_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/pending_invite/reject.png");
	private static final int DELETE_SIZE = 16;
	private final PlayerWidget widget;
	private final Runnable onApply;
	private final Runnable refresh;

	public ArmorPresetCardWidget(ArmorPreset preset, Runnable onApply, Runnable refresh) {
		super(0, 0, WIDTH, HEIGHT, Text.empty());
		this.preset = preset;
		this.onApply = onApply;
		this.refresh = refresh;
		OtherClientPlayerEntity player = new OtherClientPlayerEntity(MinecraftClient.getInstance().world,
				MinecraftClient.getInstance().getGameProfile()) {
			@Override
			public boolean isInvisibleTo(PlayerEntity player) {
				return true;
			}

			@Override
			public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {}
		};
		ArmorPreset.Piece[] pieces = new ArmorPreset.Piece[]{preset.helmet(), preset.chestplate(), preset.leggings(), preset.boots()};
		if (BASE_ARMOR == null) {
			BASE_ARMOR = ItemUtils.getArmor(MinecraftClient.getInstance().player);
		}
		var baseArmor = BASE_ARMOR;
		for (int i = 0; i < pieces.length && i < baseArmor.size(); i++) {
			ArmorPreset.Piece p = pieces[pieces.length - 1 - i];
			ItemStack stack = baseArmor.get(i).copy();
			String uuid = java.util.UUID.randomUUID().toString();
			ItemUtils.setItemUuid(stack, uuid);
			if (p.trim() != null)
				ArmorPreviewStorage.TEMP_TRIMS.put(uuid, new CustomArmorTrims.ArmorTrimId(
						Identifier.of(p.trim().material()),
						Identifier.of(p.trim().pattern())));
			if (p.dye() != null) ArmorPreviewStorage.TEMP_DYE_COLORS.put(uuid, p.dye());
			if (p.animation() != null) ArmorPreviewStorage.TEMP_ANIMATED_DYES.put(uuid, p.animation());
			if (p.texture() != null) ArmorPreviewStorage.TEMP_HELMET_TEXTURES.put(uuid, p.texture());
			player.equipStack(ARMOR_SLOTS[i], stack);
		}
		widget = new PlayerWidget(getX() + 3, getY() + 3, WIDTH - 6, HEIGHT - 18, player);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		widget.setPosition(getX() + 3, getY() + 3);
		widget.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of(preset.name()),
				getX() + getWidth() / 2, getY() + getHeight() - 10, 0xFFFFFF);
		context.drawTexture(RenderLayer::getGuiTextured, DELETE_ICON_TEXTURE,
				getX() + getWidth() - DELETE_SIZE - 3, getY() + 3,
				0, 0, DELETE_SIZE, DELETE_SIZE, 16, 16);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (mouseX >= getX() + getWidth() - DELETE_SIZE - 3 && mouseX <= getX() + getWidth() - 6 + DELETE_SIZE &&
				mouseY >= getY() + 2 && mouseY <= getY() + 3 + DELETE_SIZE) {
			MinecraftClient client = MinecraftClient.getInstance();
			Screen parent = client.currentScreen;
			if (parent != null) {
				client.setScreen(new ConfirmScreen(confirmed -> {
					if (confirmed) {
						ArmorPresets.getInstance().removePreset(preset);
						refresh.run();
					}
					client.setScreen(parent);
				},
						Text.translatable("skyblocker.armorPresets.deleteQuestion"),
						Text.translatable("skyblocker.armorPresets.deleteWarning", preset.name()),
						Text.translatable("selectServer.deleteButton"), ScreenTexts.CANCEL));
			}
			return;
		}
		if (mouseY >= getY() + getHeight() - 12) {
			MinecraftClient client = MinecraftClient.getInstance();
			Screen parent = client.currentScreen;
			if (parent != null)
				client.setScreen(new ArmorPresetRenamePopup(parent, preset, refresh));
			return;
		}
		onApply.run();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	/**
	 * Remove temporary config entries created for preview rendering.
	 */
	public static void clearTempData() {
		ArmorPreviewStorage.clear();
		BASE_ARMOR = null;
	}
}
