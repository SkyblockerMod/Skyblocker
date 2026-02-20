package de.hysky.skyblocker.skyblock.profileviewer2.pages;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.ProfileViewerWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public sealed interface ProfileViewerPage<T> permits SkillsPage, SlayersPage {
	/**
	 * {@return the icon of the page}
	 */
	ItemStack getIcon();

	/**
	 * {@return the name of the page}
	 */
	Component getName();

	/**
	 * {@return a {@link CompletableFuture} that succeeds once the page has built its widgets}
	 *
	 * <p>Allows pages to optionally construct additional state off-thread before building widgets.
	 */
	CompletableFuture<Void> load(LoadingInformation info);

	/**
	 * Builds the widgets of this page, the {@code data} comes from the result of {@link #load(LoadingInformation)}.
	 */
	void buildWidgets(T data);

	/**
	 * {@return the widgets this page is composed of}
	 */
	List<ProfileViewerWidget> getWidgets();
}
