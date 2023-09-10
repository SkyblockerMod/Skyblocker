package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import me.xmrvizzy.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class YggdrasilServicesKeyInfoMixin {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private static final Map<String, String> REPLACEMENT_HASHMAP = Map.of();
    @Unique
    private static final List<Integer> ERROR_HASH_ARRAYLIST = new ArrayList<>();

    @WrapOperation(method = "validateProperty", at = @At(value = "INVOKE", target = "Ljava/util/Base64$Decoder;decode(Ljava/lang/String;)[B", remap = false), remap = false)
    private byte[] skyblocker$replaceKnownWrongBase64(Base64.Decoder decoder, String signature, Operation<byte[]> decode) {
        try {
            return decode.call(decoder, signature.replaceAll("[^A-Za-z0-9+/=]", ""));
        } catch (IllegalArgumentException e) {
            if (Utils.isOnSkyblock()) {
                if (REPLACEMENT_HASHMAP.containsKey(signature)) {
                    return decode.call(decoder, REPLACEMENT_HASHMAP.get(signature));
                }
                int signatureHashCode = signature.hashCode();
                if (!ERROR_HASH_ARRAYLIST.contains(signatureHashCode)) {
                    ERROR_HASH_ARRAYLIST.add(signatureHashCode);
                    LOGGER.warn("[Skyblocker Base64 Fixer] Failed to decode base64 string No.{}: {}", ERROR_HASH_ARRAYLIST.size() - 1, signature);
                } else {
                    LOGGER.warn("[Skyblocker Base64 Fixer] Failed to decode the base64 string No.{} again", ERROR_HASH_ARRAYLIST.indexOf(signatureHashCode));
                }
            }
            throw e;
        }
    }
}