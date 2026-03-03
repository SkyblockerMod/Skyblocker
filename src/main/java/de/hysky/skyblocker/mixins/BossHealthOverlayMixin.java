package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerBossBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayMixin {
	@Final
	@Shadow
	private Minecraft minecraft;

	@Shadow
	protected abstract void drawBar(GuiGraphics context, int x, int y, BossEvent bossBar);

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void onRender(GuiGraphics context, CallbackInfo ci) {
		if (SkyblockerConfigManager.get().slayers.displayBossbar && SlayerBossBar.shouldRenderBossBar()) {
			LerpingBossEvent bossBar = SlayerBossBar.updateBossBar();

			int textWidth = this.minecraft.font.width(bossBar.getName());
			context.drawString(this.minecraft.font, bossBar.getName(), context.guiWidth() / 2 - textWidth / 2, 3, CommonColors.WHITE);

			this.drawBar(context, (context.guiWidth() / 2) - 91, 12, bossBar);

			ci.cancel();
		}
	}
}
