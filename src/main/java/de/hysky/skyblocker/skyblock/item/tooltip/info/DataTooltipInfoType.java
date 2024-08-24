package de.hysky.skyblocker.skyblock.item.tooltip.info;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

public interface DataTooltipInfoType<T> extends TooltipInfoType, Runnable {
	/**
	 * @return whether the data should be downloaded (if there is any)
	 */
	boolean isDataEnabled();

	@Nullable
	T getData();

	Codec<T> getCodec();

	/**
	 * Checks if the data has the given member name and sends a warning message if data is null.
	 *
	 * @param memberName the member name to check
	 * @return whether the data has the given member name or not
	 */
	boolean hasOrNullWarning(String memberName);

	/**
	 * Downloads the data if it is enabled.
	 *
	 * @param futureList the list to add the future to
	 */
	default void downloadIfEnabled(List<CompletableFuture<Void>> futureList) {
		if (isDataEnabled()) {
			download(futureList);
		}
	}

	/**
	 * Downloads the data.
	 *
	 * @param futureList the list to add the future to
	 */
	default void download(List<CompletableFuture<Void>> futureList) {
		futureList.add(CompletableFuture.runAsync(this));
	}

	/**
	 * Downloads the data.
	 */
	@Override
	void run();
}