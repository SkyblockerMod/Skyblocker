package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import de.hysky.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Base64;
import java.util.Map;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class YggdrasilServicesKeyInfoMixin {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private static final Map<String, String> REPLACEMENT_MAP = Map.of();
    @Unique
    private static final IntList ERRONEUS_SIGNATURE_HASHES = new IntArrayList();

    @WrapOperation(method = "validateProperty", at = @At(value = "INVOKE", target = "Ljava/util/Base64$Decoder;decode(Ljava/lang/String;)[B", remap = false), remap = false)
    private byte[] skyblocker$replaceKnownWrongBase64(Base64.Decoder decoder, String signature, Operation<byte[]> decode) {
        try {
            return decode.call(decoder, signature);
        } catch (IllegalArgumentException e) {
            try {
                return decode.call(decoder, signature.replaceAll("[^A-Za-z0-9+/=]", ""));
            } catch (IllegalArgumentException e2) {
                if (Utils.isOnSkyblock()) {
                    if (REPLACEMENT_MAP.containsKey(signature)) {
                        return decode.call(decoder, REPLACEMENT_MAP.get(signature));
                    }
                    int signatureHashCode = signature.hashCode();
                    if (!ERRONEUS_SIGNATURE_HASHES.contains(signatureHashCode)) {
                    	ERRONEUS_SIGNATURE_HASHES.add(signatureHashCode);
                        LOGGER.warn("[Skyblocker Base64 Fixer] Failed to decode base64 string No.{}: {}", ERRONEUS_SIGNATURE_HASHES.size() - 1, signature);
                    } else {
                        LOGGER.warn("[Skyblocker Base64 Fixer] Failed to decode the base64 string No.{} again", ERRONEUS_SIGNATURE_HASHES.indexOf(signatureHashCode));
                    }
                }
            }
            throw e;
        }
    }

    @WrapWithCondition(method = "validateProperty", remap = false, at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean skyblocker$dontLogFailedSignatureValidations(Logger logger, String message, Object property, Object exception) {
        return !Utils.isOnHypixel();
    }
}