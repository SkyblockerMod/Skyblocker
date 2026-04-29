package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.concurrent.TimeUnit;

@RegisterWidget
public class PetWidget extends TabHudWidget {
	private static final MutableComponent TITLE = Component.literal("Pet").withStyle(ChatFormatting.YELLOW,
			ChatFormatting.BOLD);
	/// Exists since a user reported (along with a spark profile) that retrieving the pet icon took a significant amount
	/// of CPU time and caused lag spikes.
	///
	/// See: <a href="https://canary.discord.com/channels/997079228510117908/1489752282948964483">SkyHanni Discord</a>
	private static final LoadingCache<String, FlexibleItemStack> PET_ICON_CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public FlexibleItemStack load(String key) throws Exception {
					return getIcon(key);
				}
			});

	private String prevString = "";
	private FlexibleItemStack icon = Ico.BONE;

	public PetWidget() {
		super("Pet", TITLE, ChatFormatting.YELLOW.getColor());
	}

	@Override
	protected void updateContent(PlayerListManager.Widget widget) {
		for (Component line : widget.lines()) {
			String string = line.getString();
			if (string.contains("[") && string.contains("]")) {
				String[] split = string.split("]", 2);
				if (split.length < 2) {
					addElement(new PlainTextElement(line));
					continue;
				}

				String petName = split[1].replace("✦", "").trim();
				if (!petName.equals(this.prevString)) {
					this.icon = PET_ICON_CACHE.getUnchecked(petName);
					this.prevString = petName;
				}
				addElement(Elements.iconTextComponent(this.icon, line));

			} else addElement(new PlainTextElement(line));
		}
	}

	private static FlexibleItemStack getIcon(String petName) {
		// With the cache this should be decently performant, but further optimization is likely
		// possible in some manner so this remains open to that
		return ItemRepository.getItemsStream().filter(stack -> {
			String string1 = stack.get(DataComponents.CUSTOM_NAME).getString();
			if (!string1.contains("]")) return false;
			String trim = string1.split("]")[1].trim();
			return trim.equals(petName);
		}).findFirst().orElse(Ico.BONE);
	}
}
