package de.hysky.skyblocker.utils.render.text;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class GridComponent {
	/**
	 * Creates a basic grid component. When multiple of these are used in a tooltip, they will form a grid (duh) and be aligned nicely with each other
	 * <p>
	 * For example, a tooltip with:
	 * <blockquote><pre>
	 * GridComponent.of(Component.literal("very long thing:"), Component.literal("69"));
	 * GridComponent.of(Component.literal("not long:"), Component.literal("420"));
	 * </pre></blockquote>
	 * <p>
	 * Will give:
	 * <blockquote><pre>
	 * very long thing: 69
	 * not long:        420
	 * </pre></blockquote>
	 * <p>
	 * Instead of:
	 * <blockquote><pre>
	 * very long thing: 69
	 * not long: 420
	 * </pre></blockquote>
	 * <p>
	 * NOTES:
	 * <ul>
	 * <li>Do NOT try to append anything to this component. It will not work.</li>
	 * <li>This is only meant to be used in tooltips, this will not work anywhere else.</li>
	 * </ul>
	 * <p>
	 * All grid component lines in a tooltip affect each other.
	 * If you want some to be in their own little group,
	 * use the overload of this method with the group parameter
	 *
	 * @param components the columns
	 * @return a grid component
	 */
	public static Component of(Component... components) {
		return of("main", components);
	}

	public static Component of(String group, Component... components) {
		return MutableComponent.create(new Contents(group, List.of(components)));
	}

	public record Contents(String group, List<Component> components) implements ComponentContents {
		@Override
		public MapCodec<? extends ComponentContents> codec() {
			throw new UnsupportedOperationException("Cannot serialize GridComponent.Contents");
		}
	}
}
