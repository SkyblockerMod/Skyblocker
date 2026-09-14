package de.hysky.skyblocker.skyblock.profileviewer2.utils.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import moe.nea.lisp.LispAst;
import moe.nea.lisp.LispData;
import moe.nea.lisp.LispParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.widgets.SkillTreeNodeWidget;
import de.hysky.skyblocker.utils.CodecUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;

public abstract sealed class SkillTreeBuilder permits HotmTreeBuilder {
	protected static final Logger LOGGER = LogUtils.getLogger();
	private static final Set<String> PERK_PROGRAM_PREFIXES = Set.of("cost", "item", "stat");
	protected static final Pattern LORE_TEMPLATE_PATTERN = Pattern.compile("\\{(?<name>[\\w\\-]+)\\}");
	public static final int MAX_SLOTS = 5;
	public static final int FREE_SLOTS = 2;

	protected SkillTreeBuilder() {
		this.init();
	}

	private void init() {
		NEURepoManager.runAsyncAfterLoad(this::loadLayout);
	}

	protected abstract void loadLayout();

	public abstract LayoutElement buildLayout(ProfileMember member, int slot);

	protected final LayoutElement buildGrid(List<Node> nodes) {
		GridLayout layout = new GridLayout();

		for (Node node : nodes) {
			SkillTreeNodeWidget widget = new SkillTreeNodeWidget(node.icon(), node.tooltip(), node.tooltipStyle());
			layout.addChild(widget, node.row(), node.column());
		}

		return layout;
	}

	/// {@return a mapping containing the perk levels & toggles for the given skill tree slot}
	protected abstract Map<String, Object> getTreeNodes(ProfileMember member, int slot);

	/// {@return the level of the player's "Heart of the X" such as the HOTM or HOTF level}
	protected abstract int getHeartOfTheXLevel(ProfileMember member);

	/// {@return the id of the "Core of the X" perk such as the COTM or COTF perk id}
	protected abstract String getCoreOfTheXId();

	/// {@return the {@code ItemStack} that is used as the perk's icon when a perk is disabled}
	protected abstract ItemStack getDisabledPerkIcon();

	/// {@return the perk's icon}
	protected final ItemStack getIcon(boolean enabled, Map<String, LispData> values) {
		if (!enabled) {
			return this.getDisabledPerkIcon();
		}

		LispData itemId = values.getOrDefault("item", LispData.LispNil.INSTANCE);
		String idPath = switch (itemId) {
			case LispData.Atom atom -> atom.getLabel();
			case LispData.LispString string -> string.getString();
			default -> "barrier";
		};

		// Lookup the item in the vanilla registry (NEU Repo does not have newer items)
		return BuiltInRegistries.ITEM.get(Identifier.withDefaultNamespace(idPath.toLowerCase(Locale.ENGLISH)))
				.map(Holder.Reference::value)
				.map(ItemStack::new)
				.orElseGet(() -> ItemUtils.getNamedPlaceholder(idPath).getStackOrThrow());
	}

	/// Compiles all the applicable LISP program strings in the {@code extraData} into programs.
	protected static Map<String, LispAst.Program> precompilePrograms(Dynamic<?> extraData) {
		Map<String, LispAst.Program> cache = new HashMap<>();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<Dynamic<?>, Dynamic<?>> allFields = (Map) extraData.asMap(Function.identity(), Function.identity());

		for (Map.Entry<Dynamic<?>, Dynamic<?>> entry : allFields.entrySet()) {
			String name = entry.getKey().asString("");
			String source = entry.getValue().asString("");

			// Only try to compile programs that start with a whitelisted prefix
			if (!name.isEmpty() && PERK_PROGRAM_PREFIXES.stream().anyMatch(prefix -> name.startsWith(prefix)) && !source.isEmpty()) {
				try {
					cache.put(name, LispParser.Companion.parse(name, source));
				} catch (Exception e) {
					LOGGER.warn("[Skyblocker Profile Viewer] Failed to compile HOTM program {}", name, e);
				}
			}
		}

		return Map.copyOf(cache);
	}

	protected record Node(ItemStack icon, List<Component> tooltip, @Nullable Identifier tooltipStyle, int row, int column) {}

	// Classes used for deserializing the NEU Repo's skill tree layouts files

	protected interface AbstractLayout {
		List<String> prelude();

		Hotx hotx();
	}

	protected record Hotx(Map<String, Perk> perks, Map<String, PowderInfo> powders) {
		protected static final Codec<Hotx> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.unboundedMap(Codec.STRING, Perk.CODEC).fieldOf("perks").forGetter(Hotx::perks),
				Codec.unboundedMap(Codec.STRING, PowderInfo.CODEC).fieldOf("powders").forGetter(Hotx::powders)
				).apply(instance, Hotx::new));
	}

	protected record Perk(String name, int x, int y, int maxLevel, LispAst.Program powder, List<LoreLine> lore, Dynamic<?> extraData, Map<String, LispAst.Program> programCache) {
		protected static final Codec<Perk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("name").forGetter(Perk::name),
				Codec.INT.fieldOf("x").forGetter(Perk::x),
				Codec.INT.fieldOf("y").forGetter(Perk::y),
				Codec.INT.fieldOf("maxLevel").forGetter(Perk::maxLevel),
				LispExtensions.PROGRAM_CODEC.fieldOf("powder").forGetter(Perk::powder),
				LoreLine.LIST_CODEC.fieldOf("lore").forGetter(Perk::lore),
				// Contains all fields of this object as a map (so that I can pull fields dynamically for the LISP functions)
				CodecUtils.MAP_PASSTHROUGH.forGetter(Perk::extraData)
				).apply(instance, Perk::new));

		private Perk(String name, int x, int y, int maxLevel, LispAst.Program powder, List<LoreLine> lore, Dynamic<?> extraData) {
			this(name, x, y, maxLevel, powder, lore, extraData, precompilePrograms(extraData));
		}
	}

	protected record LoreLine(String template, Optional<LispAst.Program> condition) {
		private static final Codec<LoreLine> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("text").forGetter(LoreLine::template),
				LispExtensions.PROGRAM_CODEC.optionalFieldOf("onlyIf").forGetter(LoreLine::condition)
				).apply(instance, LoreLine::new));
		private static final Codec<LoreLine> CODEC = Codec.withAlternative(OBJECT_CODEC, Codec.STRING, template -> new LoreLine(template, Optional.empty()));
		protected static final Codec<List<LoreLine>> LIST_CODEC = CODEC.listOf();
	}

	protected record PowderInfo(String costLine) {
		protected static final Codec<PowderInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("costLine").forGetter(PowderInfo::costLine)
				).apply(instance, PowderInfo::new));
		protected static final PowderInfo EMPTY = new PowderInfo("<lisp error>");
	}
}
