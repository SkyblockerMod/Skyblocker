package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.Attribute;
import me.xmrvizzy.skyblocker.skyblock.HotbarSlotLock;
import me.xmrvizzy.skyblocker.skyblock.dungeon.DungeonMap;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
    private static final Identifier SLOT_LOCK = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/slot_lock.png");
    private static final Identifier BARS = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/bars.png");

    private MatrixStack hotbarMatrices;
    private int hotbarSlotIndex;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledHeight;
    @Shadow
    private int scaledWidth;

    @Shadow public abstract TextRenderer getFontRenderer();

    @ModifyVariable(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
    private Text setOverlayMessage(Text message) {
        String msg = message.getString();
        if (msg != null && Utils.isSkyblock && SkyblockerConfig.get().general.bars.enableBars)
            msg = Utils.parseActionBar(msg);
        return Text.of(msg);
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    public void renderHotbar(float f, MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isSkyblock) {
            hotbarMatrices = matrices;
            hotbarSlotIndex = 0;
        }
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    public void renderHotbarItem(int i, int j, float f, PlayerEntity player, ItemStack item, CallbackInfo ci) {
        if (Utils.isSkyblock) {
            if (HotbarSlotLock.isLocked(hotbarSlotIndex)) {
                this.client.getTextureManager().bindTexture(SLOT_LOCK);
                this.drawTexture(hotbarMatrices, i, j, 0, 0,16, 16);
            }
            hotbarSlotIndex++;
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void renderExperienceBar(MatrixStack matrices, int x, CallbackInfo ci) {
        if (Utils.isSkyblock && SkyblockerConfig.get().general.bars.enableBars)
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isDungeons && SkyblockerConfig.get().locations.dungeons.enableMap)
            DungeonMap.render(matrices);

        if (Utils.isSkyblock) {
            if (SkyblockerConfig.get().general.bars.enableBars) {
                ci.cancel();
                renderBars(matrices);
            }
            this.client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
        }
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void renderMountHealth(MatrixStack matrices, CallbackInfo ci) {
        if (Utils.isSkyblock && SkyblockerConfig.get().general.bars.enableBars)
            ci.cancel();
    }

    private void renderBars(MatrixStack matrices) {
        int left = this.scaledWidth / 2 - 91;
        int top = this.scaledHeight - 35;

        int health = (int) ((float)Attribute.HEALTH.get()/(float)Attribute.MAX_HEALTH.get() * 33.0F);
        if (health > 33) health = 33;
        int mana = (int) ((float)Attribute.MANA.get()/(float)Attribute.MAX_MANA.get() * 33.0F);
        if (mana > 33) mana = 33;
        int xp = (int) (this.client.player.experienceProgress * 33.0F);

        // Icons
        this.client.getTextureManager().bindTexture(BARS);
        this.drawTexture(matrices, left, top, 0, 0, 9, 9);
        this.drawTexture(matrices, left + 47, top, 9, 0, 7, 9);
        this.drawTexture(matrices, left + 92, top, 16, 0, 9, 9);
        this.drawTexture(matrices, left + 139, top, 25, 0, 9, 9);

        // Empty Bars
        this.drawTexture(matrices, left + 10, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 55, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 102, top + 1, 0, 9, 33, 7);
        this.drawTexture(matrices, left + 149, top + 1, 0, 9, 33, 7);

        // Progress Bars
        this.drawTexture(matrices, left + 10, top + 1, 0, 16, health, 7);
        this.drawTexture(matrices, left + 55, top + 1, 0, 23, mana, 7);
        this.drawTexture(matrices, left + 102, top + 1, 0, 30, 33, 7);
        this.drawTexture(matrices, left + 149, top + 1, 0, 37, xp, 7);

        // Progress Texts
        renderText(matrices, Attribute.HEALTH.get(), left + 11, top, 16733525);
        renderText(matrices, Attribute.MANA.get(), left + 56, top, 5636095);
        renderText(matrices, Attribute.DEFENCE.get(), left + 103, top, 12106180);
        renderText(matrices, this.client.player.experienceLevel, left + 150, top, 8453920);
    }

    private void renderText(MatrixStack matrices, int value, int left, int top, int color) {
        String text = Integer.toString(value);
        int x = left + (33 - this.getFontRenderer().getWidth(text)) / 2;
        int y = top - 3;

        this.getFontRenderer().draw(matrices, text, (float) (x + 1), (float) y, 0);
        this.getFontRenderer().draw(matrices, text, (float) (x - 1), (float) y, 0);
        this.getFontRenderer().draw(matrices, text, (float) x, (float) (y + 1), 0);
        this.getFontRenderer().draw(matrices, text, (float) x, (float) (y - 1), 0);
        this.getFontRenderer().draw(matrices, text, (float) x, (float) y, color);
    }
}