package de.hysky.skyblocker.skyblock.fancybars;

import java.awt.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum StatusBarType implements StringRepresentable {
	HEALTH("health", BarPositioner.BarAnchor.HOTBAR_TOP, 0, new Color[]{new Color(255, 0, 0), new Color(255, 220, 0)}, true, true, new Color(255, 85, 85), Component.translatable("skyblocker.bars.config.health")),
	INTELLIGENCE("intelligence", BarPositioner.BarAnchor.HOTBAR_TOP, 0, new Color[]{new Color(0, 255, 255), new Color(180, 0, 255)}, true, true, new Color(85, 255, 255), Component.translatable("skyblocker.bars.config.intelligence")),
	DEFENSE("defense", BarPositioner.BarAnchor.HOTBAR_RIGHT, 0, new Color[]{new Color(255, 255, 255)}, false, false, new Color(185, 185, 185), Component.translatable("skyblocker.bars.config.defense")),
	EXPERIENCE("experience", BarPositioner.BarAnchor.HOTBAR_TOP, 1, new Color[]{new Color(100, 230, 70)}, false, false, new Color(128, 255, 32), Component.translatable("skyblocker.bars.config.experience")),
	SPEED("speed", BarPositioner.BarAnchor.HOTBAR_RIGHT, 0, new Color[]{new Color(255, 255, 255)}, false, true, new Color(185, 185, 185), Component.translatable("skyblocker.bars.config.speed")),
	AIR("air", BarPositioner.BarAnchor.HOTBAR_RIGHT, 1, new Color[]{new Color(135, 206, 250)}, false, true, new Color(150, 230, 255), Component.translatable("skyblocker.bars.config.air"));

	private final String id;
	private final BarPositioner.BarAnchor defaultAnchor;
	private final int defaultGridY;
	private final Color[] colors;
	private final boolean hasOverflow;
	private final boolean hasMax;
	private final @Nullable Color textColor;
	private final Component name;

	StatusBarType(String id, BarPositioner.BarAnchor defaultAnchor, int defaultGridY, Color[] colors, boolean hasOverflow, boolean hasMax, @Nullable Color textColor, Component name) {
		this.id = id;
		this.defaultAnchor = defaultAnchor;
		this.defaultGridY = defaultGridY;
		this.colors = colors;
		this.hasOverflow = hasOverflow;
		this.hasMax = hasMax;
		this.textColor = textColor;
		this.name = name;
	}

	public static StatusBarType from(String id) {
		for (StatusBarType type : values()) {
			if (type.id.equals(id)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown status bar type: " + id);
	}

	@Override
	public String getSerializedName() {
		return id;
	}

	public BarPositioner.BarAnchor getDefaultAnchor() {
		return defaultAnchor;
	}

	public int getDefaultGridY() {
		return defaultGridY;
	}

	public Color[] getColors() {
		return colors;
	}

	public boolean hasOverflow() {
		return hasOverflow;
	}

	public boolean hasMax() {
		return hasMax;
	}

	public @Nullable Color getTextColor() {
		return textColor;
	}

	public Component getName() {
		return name;
	}

	public StatusBar newStatusBar() {
		return switch (this) {
			case INTELLIGENCE -> new StatusBar.ManaStatusBar(this);
			case EXPERIENCE -> new StatusBar.ExperienceStatusBar(this);
			default -> new StatusBar(this);
		};
	}
}
