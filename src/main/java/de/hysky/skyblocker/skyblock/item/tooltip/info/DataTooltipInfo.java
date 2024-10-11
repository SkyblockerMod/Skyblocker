package de.hysky.skyblocker.skyblock.item.tooltip.info;

import java.net.http.HttpHeaders;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import de.hysky.skyblocker.config.configs.GeneralConfig;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.Http;

public final class DataTooltipInfo<T> extends SimpleTooltipInfo implements DataTooltipInfoType<T> {
	private final String address;
	private final Codec<T> codec;
	@Nullable
	private T data;
	private final boolean cacheable;
	private long hash;
	private final BiPredicate<T, String> contains;
	private final Predicate<GeneralConfig.ItemTooltip> dataEnabled;
	@Nullable
	private final Consumer<T>[] callbacks;

	@SafeVarargs
	protected DataTooltipInfo(String address, Codec<T> codec, boolean cacheable, BiPredicate<T, String> contains, Predicate<GeneralConfig.ItemTooltip> tooltipEnabled, Predicate<GeneralConfig.ItemTooltip> dataEnabled, Consumer<T>... callbacks) {
		super(tooltipEnabled);

		this.address = address;
		this.codec = codec;
		this.cacheable = cacheable;
		this.contains = contains;
		this.dataEnabled = dataEnabled;
		this.callbacks = callbacks;
	}

	@Override
	public boolean isDataEnabled() {		
		return dataEnabled.test(ItemTooltip.config);
	}

	@Override
	@Nullable
	public T getData() {
		return data;
	}

	@Override
	public Codec<T> getCodec() {
		return codec;
	}

	@Override
	public boolean hasOrNullWarning(String memberName) {
		if (data == null) {
			ItemTooltip.nullWarning();

			return false;
		} else {
			return contains.test(data, memberName);
		}
	}

	@Override
	public void run() {
		try {
			if (cacheable) {
				HttpHeaders headers = Http.sendHeadRequest(address);
				long hash = Http.getEtag(headers).hashCode() + Http.getLastModified(headers).hashCode();
				if (this.hash == hash) return;
				else this.hash = hash;
			}

			String response = Http.sendGetRequest(address);

			if (response.trim().startsWith("<!DOCTYPE") || response.trim().startsWith("<html")) {
				ItemTooltip.LOGGER.warn("[Skyblocker] Received HTML content for {}. Expected JSON.", this.address);
				return;
			}

			data = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(response)).getOrThrow();

			if (callbacks != null) {
				for (Consumer<T> callback : callbacks) {
					callback.accept(data);
				}
			}
		} catch (Exception e) {
			ItemTooltip.LOGGER.warn("[Skyblocker] Failed to download {} data!", this.address, e);
		}
	}
}
