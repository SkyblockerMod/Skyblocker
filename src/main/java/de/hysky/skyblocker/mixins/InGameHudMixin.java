package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import de.hysky.skyblocker.SkyblockerMod;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final Supplier<Identifier> SLOT_LOCK_ICON = () -> SkyblockerConfigManager.get().general.itemProtection.slotLockStyle.tex;
    @Unique
    private static final Pattern DICER_TITLE_BLACKLIST = Pattern.compile(".+? DROP!");

    @Unique
    private final FancyStatusBars statusBars = new FancyStatusBars();

    @Shadow
    @Final
    private MinecraftClient client;

	@Unique
	private boolean isQuiverSlot = false;

	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V", ordinal = 0))
    public void skyblocker$renderHotbarItemLockOrBackground(CallbackInfo ci, @Local(argsOnly = true) DrawContext context, @Local(ordinal = 4, name = "m") int index, @Local(ordinal = 5, name = "n") int x, @Local(ordinal = 6, name = "o") int y, @Local PlayerEntity player, @Share(namespace = SkyblockerMod.NAMESPACE, value = "slotIndex") LocalIntRef ref) {
        if (Utils.isOnSkyblock()) {
			ItemBackgroundManager.drawBackgrounds(player.getInventory().getMainStacks().get(index), context, x, y);

			// slot lock
            if (HotbarSlotLock.isLocked(index)) {
                context.drawTexture(RenderLayer::getGuiTextured, SLOT_LOCK_ICON.get(), x, y, 0, 0, 16, 16, 16, 16);
            }

            //item protection
            if (ItemProtection.isItemProtected(player.getInventory().getMainStacks().get(index))) {
                context.drawTexture(RenderLayer::getGuiTextured, ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
            }
			isQuiverSlot = index == 8;
        }
		ref.set(index);
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
		System.out.println("math'd");
		prevHash = hashCode;
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component == null) return false;
		NbtCompound compound = component.copyNbt();
		NbtElement element = compound.get("quiver_arrow");
		prevQuiverSlot = element != null && (element.asBoolean().orElse(false) || element.asString().orElse("false").equals("true"));
		return prevQuiverSlot;
	}

	@WrapOperation(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;II)V"))
	private void skyblocker$drawQuiverAmount(DrawContext instance, TextRenderer textRenderer, ItemStack stack, int x, int y, Operation<Void> original) {
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
			instance.drawStackOverlay(textRenderer, stack, x, y, format);
		} else {
			original.call(instance, textRenderer, stack, x, y);
		}
	}

    @Inject(method = { "renderExperienceBar", "renderExperienceLevel" }, at = @At("HEAD"), cancellable = true, require = 2)
    private void skyblocker$renderExperienceBar(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && FancyStatusBars.isExperienceFancyBarVisible())
            ci.cancel();
    }

    @Inject(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V", shift = At.Shift.AFTER), cancellable = true)
    private void skyblocker$renderStatusBars(DrawContext context, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && statusBars.render(context, context.getScaledWindowWidth(), context.getScaledWindowHeight())) ci.cancel();
    }

    @Inject(method = "renderHealthBar", at = @At(value = "HEAD"), cancellable = true)
    private void skyblocker$renderHealthBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        if (!Utils.isOnSkyblock()) return;
        if (FancyStatusBars.isEnabled() && FancyStatusBars.isHealthFancyBarVisible()) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowHeight()I"))
    private int skyblocker$moveHealthDown(int original) {
        return Utils.isOnSkyblock() && FancyStatusBars.isEnabled() && !FancyStatusBars.isHealthFancyBarVisible() && FancyStatusBars.isExperienceFancyBarVisible() ? original + 6 : original;
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void skyblocker$renderStatusBars(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled()) ci.cancel();
    }

    @Inject(method = "renderMountHealth", at = @At("HEAD"), cancellable = true)
    private void skyblocker$renderMountHealth(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && FancyStatusBars.isEnabled())
            ci.cancel();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void skyblocker$dontRenderStatusEffects(CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.hideStatusEffectOverlay) ci.cancel();
    }

    @ModifyExpressionValue(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    private float skyblocker$modifyAttackIndicatorCooldown(float cooldownProgress) {
        if (Utils.isOnSkyblock() && client.player != null) {
            ItemStack stack = client.player.getMainHandStack();
            if (ItemCooldowns.isOnCooldown(stack)) {
                return ItemCooldowns.getItemCooldownEntry(stack).getRemainingCooldownPercent();
            }
        }

        return cooldownProgress;
    }

    @Inject(method = "setTitle", at = @At("HEAD"), cancellable = true)
    private void skyblocker$dicerTitlePrevent(Text title, CallbackInfo ci) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().farming.garden.dicerTitlePrevent && title != null && DICER_TITLE_BLACKLIST.matcher(title.getString()).matches()) {
            ci.cancel();
        }
    }

	@WrapWithCondition(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
	private boolean skyblocker$shouldRenderHud(PlayerListHud playerListHud, DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective) {
		return !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.shouldRenderVanilla() || MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen;
	}
}
