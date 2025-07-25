package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreset;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArmorPresetCardWidget extends ClickableWidget {
    public static final int WIDTH = 90;
    public static final int HEIGHT = 183;
    private static final EquipmentSlot[] ARMOR_SLOTS = EquipmentSlot.VALUES.stream()
            .filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .toArray(EquipmentSlot[]::new);

    private final ArmorPreset preset;
    private final PlayerWidget widget;
    private final Runnable onApply;

    public ArmorPresetCardWidget(ArmorPreset preset, Runnable onApply) {
        super(0, 0, WIDTH, HEIGHT, Text.empty());
        this.preset = preset;
        this.onApply = onApply;
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
        var baseArmor = ItemUtils.getArmor(MinecraftClient.getInstance().player);
        for (int i = 0; i < pieces.length && i < baseArmor.size(); i++) {
            ArmorPreset.Piece p = pieces[pieces.length - 1 - i];
            ItemStack stack = baseArmor.get(i).copy();
            String uuid = java.util.UUID.randomUUID().toString();
            ItemUtils.setItemUuid(stack, uuid);
            var cfg = SkyblockerConfigManager.get().general;
            if (p.trim() != null)
                cfg.customArmorTrims.put(uuid, new CustomArmorTrims.ArmorTrimId(
                        Identifier.of(p.trim().material()),
                        Identifier.of(p.trim().pattern())));
            if (p.dye() != null) cfg.customDyeColors.put(uuid, p.dye());
            if (p.animation() != null) cfg.customAnimatedDyes.put(uuid, p.animation());
            if (p.texture() != null) cfg.customHelmetTextures.put(uuid, p.texture());
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
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onApply.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
