package me.xmrvizzy.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import me.xmrvizzy.skyblocker.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class YggdrasilServicesKeyInfoMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(YggdrasilServicesKeyInfo.class);
    @Unique
    private static final Map<String, String> REPLACEMENT_HASHMAP = new HashMap<>();
    @Unique
    private static final List<Integer> ERROR_HASH_ARRAYLIST = new ArrayList<>();

    @WrapOperation(method = "validateProperty", at = @At(value = "INVOKE", target = "Ljava/util/Base64$Decoder;decode(Ljava/lang/String;)[B", remap = false), remap = false)
    private byte[] skyblocker$replaceKnownWrongBase64(Base64.Decoder decoder, String signature, Operation<byte[]> decode) {
        try {
            return decode.call(decoder, signature.replaceAll("[^A-Za-z0-9+/=]",""));
        } catch (IllegalArgumentException e) {
            if (Utils.isOnSkyblock()) {
                if (REPLACEMENT_HASHMAP.containsKey(signature)) {
                    return decode.call(decoder, REPLACEMENT_HASHMAP.get(signature));
                }
                int signatureHashCode = signature.hashCode();
                if (!ERROR_HASH_ARRAYLIST.contains(signatureHashCode)) {
                    ERROR_HASH_ARRAYLIST.add(signatureHashCode);
                    LOGGER.warn("Failed to decode this base64 string: {}", signature);
                }
                LOGGER.warn("Failed to decode the base64 string No.{} again", ERROR_HASH_ARRAYLIST.indexOf(signatureHashCode));
            }
            throw e;
        }
    }

    static {
        REPLACEMENT_HASHMAP.put("\"ZnIHWclaHKIRt9H2puWSCaUb2OrI2YPUkdV5f0w/fAdlpllmgCGjYegWX2ThhjrcRbVwc8oravYaVo641X/33ZOmg1W9luZg2rV9Zc16dgTXkDQlJSi8bpEKK2iUMEU5oa/fb3WG3wCYM4EUbataiiizQdMKl0pG81uRDS45QSdWvowbNMvUA+tL97h7nnrNgNU01NQJntJuzXg6JD6vC5bnDyDn5nXF0/KI69xpzSDupWxs6mmd03xbU18T9XX8mlgDgg549zRIaQM2OEihCHecu42e+yjFs5TiXOhpupm3FArJ87XrfzD17fupE0cr9PehXZzJxtLtzthgjqwb5nB3QmD8UFsqhFQBU0TxswGqBQxX1CeCwYvQGOXx3jE73/79Eq6BrGmwqwYs+SOk/qcIdpfdcK2y6q4KSDiJlBbYMCryYFPEUcKmTdOE2OkGniekvzO7AriApibZvt5R/jbS2dmMmUaNKIQFSINScyBH6JV5vMmneEUbhgNBangagVoGIsVAlRNBCKrQ/mTMaIgY+jE/HU8gQnvJAKOtnYrTXLyPvhXaJ4ix+pDGFkycjj7phi1FrLVHTpEewXhx5AvuNY23m+Y9lHp2/687PDoSsryD4e1rJkFs4psmZxCDHRq4x1nmHR3ZvZeRAQgX8LytPbl+PD8N7rJ6lhNRwY4=", "ZnIHWclaHKIRt9H2puWSCaUb2OrI2YPUkdV5f0w/fAdlpllmgCGjYegWX2ThhjrcRbVwc8oravYaVo641X/33ZOmg1W9luZg2rV9Zc16dgTXkDQlJSi8bpEKK2iUMEU5oa/fb3WG3wCYM4EUbataiiizQdMKl0pG81uRDS45QSdWvowbNMvUA+tL97h7nnrNgNU01NQJntJuzXg6JD6vC5bnDyDn5nXF0/KI69xpzSDupWxs6mmd03xbU18T9XX8mlgDgg549zRIaQM2OEihCHecu42e+yjFs5TiXOhpupm3FArJ87XrfzD17fupE0cr9PehXZzJxtLtzthgjqwb5nB3QmD8UFsqhFQBU0TxswGqBQxX1CeCwYvQGOXx3jE73/79Eq6BrGmwqwYs+SOk/qcIdpfdcK2y6q4KSDiJlBbYMCryYFPEUcKmTdOE2OkGniekvzO7AriApibZvt5R/jbS2dmMmUaNKIQFSINScyBH6JV5vMmneEUbhgNBangagVoGIsVAlRNBCKrQ/mTMaIgY+jE/HU8gQnvJAKOtnYrTXLyPvhXaJ4ix+pDGFkycjj7phi1FrLVHTpEewXhx5AvuNY23m+Y9lHp2/687PDoSsryD4e1rJkFs4psmZxCDHRq4x1nmHR3ZvZeRAQgX8LytPbl+PD8N7rJ6lhNRwY4=");
    }
}