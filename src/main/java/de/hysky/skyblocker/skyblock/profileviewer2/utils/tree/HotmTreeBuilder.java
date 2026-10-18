package de.hysky.skyblocker.skyblock.profileviewer2.utils.tree;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import moe.nea.lisp.CoreBindings;
import moe.nea.lisp.LispData;
import moe.nea.lisp.LispExecutionContext;
import moe.nea.lisp.LispParser;
import moe.nea.lisp.StackFrame;
import moe.nea.lisp.bind.AutoBinder;
import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.PrimitiveTypeUtils;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.SkyBlockTooltipStyles;

public final class HotmTreeBuilder extends SkillTreeBuilder {
	public static final HotmTreeBuilder INSTANCE = new HotmTreeBuilder();
	private @Nullable Layout layout;
	private @Nullable LispExecutionContext lispContext;

	private HotmTreeBuilder() {}

	@Init
	public static void initClass() {}

	@Override
	protected void loadLayout() {
		try (InputStream stream = NEURepoManager.file("constants/hotmlayout.json").stream()) {
			String content = new String(stream.readAllBytes());
			Layout layout = Layout.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(content)).getOrThrow();

			// Ensure that the field is updated on the main thread
			CompletableFuture.runAsync(() -> this.updateLayout(layout), Minecraft.getInstance());
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to load HOTM layout.", e);
		}
	}

	private void updateLayout(Layout layout) {
		// Update layout
		this.layout = layout;

		try {
			this.updateLispContext(layout);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profile Viewer] Failed to update LISP context.", e);
		}
	}

	private void updateLispContext(Layout layout) {
		// Initialize LISP context
		LispExecutionContext lec = new LispExecutionContext();
		lec.setupStandardBindings();

		// Bind the extra methods we need
		new AutoBinder().bindTo(new LispExtensions(), lec.getRootStackFrame());

		// Bind the prelude functions
		for (int i = 0; i < layout.prelude().size(); i++) {
			String name = "hotmlayout:prelude:" + i;

			lec.executeProgram(lec.getRootStackFrame(), LispParser.Companion.parse(name, layout.prelude().get(i)));
		}

		this.lispContext = lec;
	}

	@Override
	public LayoutElement buildLayout(ProfileMember member, int slot) {
		if (this.layout == null || this.lispContext == null) {
			return new StringWidget(Component.literal("Error loading HOTM layout :("), Minecraft.getInstance().font);
		}

		List<Node> nodes = this.layout.hotm().perks().entrySet().stream()
				.map(entry -> this.buildNode(entry.getKey(), entry.getValue(), member, slot))
				.toList();

		return this.buildGrid(nodes);
	}

	private Node buildNode(String id, Perk perk, ProfileMember member, int slot) {
		Map<String, Object> tree = this.getTreeNodes(member, slot);

		int level = PrimitiveTypeUtils.coerceInt(tree.getOrDefault(id, 0));
		boolean enabled = PrimitiveTypeUtils.coerceBoolean(tree.getOrDefault("toggle_" + id, true));
		boolean isMaxLevel = level == perk.maxLevel();

		int hotm = this.getHeartOfTheXLevel(member);
		int cotm = PrimitiveTypeUtils.coerceInt(tree.getOrDefault(this.getCoreOfTheXId(), 0));

		Pair<Map<String, LispData>, StackFrame> valuesAndBindings = this.calculatePerkValues(perk, level, hotm, cotm);

		ItemStack icon = this.getIcon(enabled, valuesAndBindings.left());
		List<Component> tooltip = this.getTooltip(perk, id, level, enabled, valuesAndBindings.left(), valuesAndBindings.right(), this.layout.hotm().powders());
		Identifier tooltipStyle = isMaxLevel ? SkyBlockTooltipStyles.LEGENDARY : null;
		int row = perk.y();
		int column = perk.x();

		return new Node(icon, tooltip, tooltipStyle, row, column);
	}

	// Note: when lifting into superclass rename cotm to cotx
	/// @param cotm the Core of the Mountain level
	private Pair<Map<String, LispData>, StackFrame> calculatePerkValues(Perk perk, int level, int hotm, int cotm) {
		StackFrame bindings = this.lispContext.genBindings();
		bindings.setValueLocal("hotm", new LispData.LispNumber(hotm));
		bindings.setValueLocal("potm", new LispData.LispNumber(cotm));
		bindings.setValueLocal("level", new LispData.LispNumber(level == 0 ? 1d : level));
		bindings.setValueLocal("maxLevel", new LispData.LispNumber(perk.maxLevel()));
		bindings.setValueLocal("level0", new LispData.LispNumber(level));

		Map<String, LispData> values = perk.programCache().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> this.lispContext.executeProgram(bindings.fork(), entry.getValue())));

		return Pair.of(values, bindings);
	}

	@Override
	protected Map<String, Object> getTreeNodes(ProfileMember member, int slot) {
		return  member.skillTree.nodes.getMiningNode(slot);
	}

	@Override
	protected int getHeartOfTheXLevel(ProfileMember member) {
		return member.skillTree.experience.getHotmLevel().level();
	}

	@Override
	protected String getCoreOfTheXId() {
		return "core_of_the_mountain";
	}

	@Override
	protected ItemStack getDisabledPerkIcon() {
		return Ico.REDSTONE_BLOCK.getStackOrThrow();
	}

	private List<Component> getTooltip(Perk perk, String id, int level, boolean enabled, Map<String, LispData> values, StackFrame bindings, Map<String, PowderInfo> powders) {
		boolean maxLevel = perk.maxLevel() == level;
		List<Component> tooltip = new ArrayList<>();

		// Title
		ChatFormatting nameFormatting = enabled && level != 0 ? (maxLevel ? ChatFormatting.GREEN : ChatFormatting.YELLOW) : ChatFormatting.RED;
		tooltip.add(Component.literal(perk.name()).withStyle(nameFormatting));

		// Level - only show if the perk has more than 1 level
		if (perk.maxLevel() > 1) {
			if (maxLevel) {
				tooltip.add(Component.literal("Level " + perk.maxLevel()).withStyle(ChatFormatting.GRAY));
			} else {
				Component levelText = Component.empty()
						.append(Component.literal("Level " + level).withStyle(ChatFormatting.GRAY))
						.append(Component.literal("/" + perk.maxLevel()).withStyle(ChatFormatting.DARK_GRAY));
				tooltip.add(levelText);
			}
		}

		// Space between top and middle parts
		tooltip.add(Component.empty());

		// The part of the tooltip driven by the LISP stuff
		List<String> dynamicTooltip = new ArrayList<>();

		// Perk Lore
		{
			List<String> applicableLines = perk.lore().stream()
					.filter(line -> line.condition().isPresent() ? CoreBindings.INSTANCE.isTruthy(this.lispContext.executeProgram(bindings.fork(), line.condition().get())) : true)
					.map(LoreLine::template)
					.toList();
			dynamicTooltip.addAll(applicableLines);
		}

		// Powder
		powder: {
			// Hide powder line for locked or maxed perks
			if (level == 0 || maxLevel) {
				break powder;
			}

			// This will separate the cost line from the perk lore
			dynamicTooltip.add("");

			StackFrame powderBindings = bindings.fork();
			for (String powder : powders.keySet()) {
				powderBindings.setValueLocal(powder, new LispData.LispString(powder));
			}

			String powderType = this.lispContext.executeProgram(powderBindings, perk.powder()) instanceof LispData.LispString string ? string.getString() : "";
			String costLine = powders.getOrDefault(powderType, PowderInfo.EMPTY).costLine();

			dynamicTooltip.add(costLine);
		}

		// Fill in the templates inside the dynamic lore (e.g. {cost}, {stat}, etc.)
		dynamicTooltip.replaceAll(line -> LORE_TEMPLATE_PATTERN.matcher(line).replaceAll(match -> {
			String name = match.group("name");

			if (name != null) {
				return switch (values.get(name)) {
					case LispData.LispString string -> string.getString();
					case LispData.LispNumber number -> {
						double value = name.equals("cost") ? Math.floor(number.getValue()) : number.getValue();
						yield Formatters.DOUBLE_NUMBERS.format(value);
					}
					case null, default -> "<lisp error - " + name + ">";
				};
			}

			return line;
		}));

		// Add dynamic tooltip to main
		for (String line : dynamicTooltip) {
			tooltip.add(Component.literal(line));
		}

		// Add unlocked/enabled/disabled text
		if (level > 0) {
			tooltip.add(Component.empty());

			if (enabled) {
				boolean isCotm = id.equals(this.getCoreOfTheXId());
				tooltip.add(Component.literal(isCotm ? "UNLOCKED" : "ENABLED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
			} else {
				tooltip.add(Component.literal("DISABLED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
			}

		}

		return List.copyOf(tooltip);
	}

	private record Layout(List<String> prelude, Hotx hotm) implements AbstractLayout {
		private static final Codec<Layout> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.listOf().fieldOf("prelude").forGetter(Layout::prelude),
				Hotx.CODEC.fieldOf("hotm").forGetter(Layout::hotm)
				).apply(instance, Layout::new));

		@Override
		public Hotx hotx() {
			return this.hotm();
		}
	}
}
