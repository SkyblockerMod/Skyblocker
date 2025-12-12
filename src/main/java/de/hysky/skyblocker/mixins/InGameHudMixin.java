package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemCooldowns;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class InGameHudMixin {
	@Unique
	private static final Supplier<Identifier> SLOT_LOCK_ICON = () -> SkyblockerConfigManager.get().general.itemProtection.slotLockStyle.tex;
	@Unique
	private static final Pattern DICER_TITLE_BLACKLIST = Pattern.compile(".+? DROP!");

	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private boolean isQuiverSlot = false;

	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V", ordinal = 0))
	public void skyblocker$renderHotbarItemLockOrBackground(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics context, @Local(ordinal = 4, name = "m") int index, @Local(ordinal = 5, name = "n") int x, @Local(ordinal = 6, name = "o") int y, @Local Player player) {
		if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(player.getInventory().getNonEquipmentItems().get(index), context, x, y);

			// slot lock
			if (HotbarSlotLock.isLocked(index)) {
				context.blit(RenderPipelines.GUI_TEXTURED, SLOT_LOCK_ICON.get(), x, y, 0, 0, 16, 16, 16, 16);
			}

			//item protection
			if (ItemProtection.isItemProtected(player.getInventory().getNonEquipmentItems().get(index))) {
				context.blit(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
			}
			isQuiverSlot = index == 8;
		}
	}

	@Unique
	private static int prevHash = 0;
	@Unique
	private static boolean prevQuiverSlot = false;

	@Unique
	private static boolean isQuiverItem(ItemStack stack) {
		int hashCode = System.identityHashCode(stack);
		if (hashCode == prevHash) {
			return prevQuiverSlot;
		}
		prevHash = hashCode;
		CustomData component = stack.get(DataComponents.CUSTOM_DATA);
		if (component == null) return false;
		CompoundTag compound = component.copyTag();
		Tag element = compound.get("quiver_arrow");
		prevQuiverSlot = element != null && (element.asBoolean().orElse(false) || element.asString().orElse("false").equals("true"));
		return prevQuiverSlot;
	}

	@WrapOperation(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
	private void skyblocker$drawQuiverAmount(GuiGraphics instance, Font textRenderer, ItemStack stack, int x, int y, Operation<Void> original) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.trueQuiverCount && isQuiverSlot && isQuiverItem(stack)) {
			String arrow = ItemUtils.getLoreLineIf(stack, s -> s.trim().startsWith("Active Arrow"));
			if (arrow == null) {
				original.call(instance, textRenderer, stack, x, y);
				return;
			}
			int i = arrow.lastIndexOf('(');
			int j = arrow.lastIndexOf(')');
			if (i == -1 || j == -1 || i > j) {
				original.call(instance, textRenderer, stack, x, y);
				return;
			}
			arrow = arrow.substring(i + 1, j);
			OptionalInt anInt = Utils.parseInt(arrow);
			if (anInt.isEmpty()) {
				original.call(instance, textRenderer, stack, x, y);
				return;
			}
			String format = Formatters.SHORT_INTEGER_NUMBERS.format(anInt.getAsInt());
			instance.renderItemDecorations(textRenderer, stack, x, y, format);
		} else {
			original.call(instance, textRenderer, stack, x, y);
		}
	}

	@WrapWithCondition(method = "renderHotbarAndDecorations", at = {
					@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"),
					@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V")
			}, require = 2)
	private boolean skyblocker$renderExperienceBar(ContextualBarRenderer bar, GuiGraphics context, DeltaTracker tickCounter) {
		return shouldShowExperienceBar();
	}

	@WrapWithCondition(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
	private boolean skyblocker$renderExperienceLevel(GuiGraphics context, Font textRenderer, int level) {
		return shouldShowExperienceBar();
	}

	@Unique
	private static boolean shouldShowExperienceBar() {
		return !(Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && FancyStatusBars.isExperienceFancyBarEnabled());
	}

	@Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V", shift = At.Shift.AFTER), cancellable = true)
	private void skyblocker$renderStatusBars(GuiGraphics context, CallbackInfo ci) {
		if (Utils.isOnSkyblock() && FancyStatusBars.render(context, minecraft)) ci.cancel();
	}

	@Inject(method = "renderHearts", at = @At(value = "HEAD"), cancellable = true)
	private void skyblocker$renderHealthBar(GuiGraphics context, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
		if (!Utils.isOnSkyblock()) return;
		if (FancyStatusBars.isEnabled() && FancyStatusBars.isHealthFancyBarEnabled()) ci.cancel();
	}

	@ModifyExpressionValue(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
	private int skyblocker$moveHealthDown(int original) {
		return Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && !FancyStatusBars.isHealthFancyBarEnabled() && FancyStatusBars.isExperienceFancyBarEnabled() ? original + 6 : original;
	}

	@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
	private static void skyblocker$renderStatusBars(GuiGraphics context, Player player, int i, int j, int k, int x, CallbackInfo ci) {
		if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled()) ci.cancel();
	}

	@Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
	private void skyblocker$renderMountHealth(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled())
			ci.cancel();
	}

	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void skyblocker$dontRenderStatusEffects(CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay) ci.cancel();
	}

	@ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackCooldownProgress(F)F"))
	private float skyblocker$modifyAttackIndicatorCooldown(float cooldownProgress) {
		if (Utils.isOnSkyblock() && minecraft.player != null) {
			ItemStack stack = minecraft.player.getMainHandItem();
			if (ItemCooldowns.isOnCooldown(stack)) {
				return ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent();
			}
		}

		return cooldownProgress;
	}

	@Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
	private void skyblocker$dicerTitlePrevent(Component title, CallbackInfo ci) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().farming.garden.dicerTitlePrevent && title != null && DICER_TITLE_BLACKLIST.matcher(title.getString()).matches()) {
			ci.cancel();
		}
	}

	@WrapWithCondition(method = "renderTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))
	private boolean skyblocker$shouldRenderHud(PlayerTabOverlay playerListHud, GuiGraphics context, int scaledWindowWidth, Scoreboard scoreboard, Objective objective) {
		return !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.shouldRenderVanilla() || Minecraft.getInstance().screen instanceof WidgetsConfigurationScreen;
	}
}
