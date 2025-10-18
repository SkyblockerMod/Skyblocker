package de.hysky.skyblocker.skyblock.fancybars;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public enum StatusBarType implements StringIdentifiable {
	HEALTH("health", BarPositioner.BarAnchor.HOTBAR_TOP, 0, new Color[]{new Color(255, 0, 0), new Color(255, 220, 0)}, true, true, new Color(255, 85, 85), Text.translatable("skyblocker.bars.config.health")),
	INTELLIGENCE("intelligence", BarPositioner.BarAnchor.HOTBAR_TOP, 0, new Color[]{new Color(0, 255, 255), new Color(180, 0, 255)}, true, true, new Color(85, 255, 255), Text.translatable("skyblocker.bars.config.intelligence")),
	DEFENSE("defense", BarPositioner.BarAnchor.HOTBAR_RIGHT, 0, new Color[]{new Color(255, 255, 255)}, false, false, new Color(185, 185, 185), Text.translatable("skyblocker.bars.config.defense")),
	EXPERIENCE("experience", BarPositioner.BarAnchor.HOTBAR_TOP, 1, new Color[]{new Color(100, 230, 70)}, false, false, new Color(128, 255, 32), Text.translatable("skyblocker.bars.config.experience")),
	SPEED("speed", BarPositioner.BarAnchor.HOTBAR_RIGHT, 0, new Color[]{new Color(255, 255, 255)}, false, true, new Color(185, 185, 185), Text.translatable("skyblocker.bars.config.speed")),
	AIR("air", BarPositioner.BarAnchor.HOTBAR_RIGHT, 1, new Color[]{new Color(135, 206, 250)}, false, true, new Color(150, 230, 255), Text.translatable("skyblocker.bars.config.air"));

	private final String id;
	private final BarPositioner.BarAnchor defaultAnchor;
	private final int defaultGridY;
	private final Color[] colors;
	private final boolean hasOverflow;
	private final boolean hasMax;
	@Nullable
	private final Color textColor;
	private final Text name;

	StatusBarType(String id, BarPositioner.BarAnchor defaultAnchor, int defaultGridY, Color[] colors, boolean hasOverflow, boolean hasMax, @Nullable Color textColor, Text name) {
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
	public String asString() {
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

	public Text getName() {
		return name;
	}

	public StatusBar newStatusBar() {
		if (this == INTELLIGENCE) return new StatusBar.ManaStatusBar(this);
		return new StatusBar(this);
	}
}
