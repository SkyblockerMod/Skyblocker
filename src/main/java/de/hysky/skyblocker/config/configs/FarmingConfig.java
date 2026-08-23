package de.hysky.skyblocker.config.configs;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringRepresentable;

public class FarmingConfig {
	@Deprecated
	public transient FarmingHud farmingHud = new FarmingHud();

	public PestHighlighter pestHighlighter = new PestHighlighter();

	public MouseLock mouseLock = new MouseLock();

	public PlotsWidget plotsWidget = new PlotsWidget();

	public VisitorHelper visitorHelper = new VisitorHelper();

	public Greenhouse greenhouse = new Greenhouse();

	public static class Greenhouse {
		public boolean enabled = true;

		public boolean showMutationSlot = true;
	}

	public static class PestHighlighter {
		public boolean enabled = true;

		public boolean vinylHighlighter = true;

		public boolean contestHighlighter = true;

		public boolean enableStereoHarmonyHelperForContest = true;
	}

	public static class MouseLock {
		public boolean lockMouseTool = false;

		public boolean lockMouseGroundOnly = false;
	}

	public static class PlotsWidget {
		public boolean enabled = true;

		public boolean closeScreenOnPlotClick = false;

		public int x = 0;

		public int y = 0;
	}

	public static class VisitorHelper {
		public boolean enabled = true;

		public boolean showInGardenOnly = true;

		public boolean showInStacks = false;
	}

	public static class FarmingHud {
		@Deprecated
		public transient boolean enabled = true;

		@Deprecated
		public transient boolean counter = true;

		@Deprecated
		public transient boolean coins = true;

		@Deprecated
		public transient Type type = Type.BOTH;

		@Deprecated
		public transient boolean includeSeedsPrice = true;

		@Deprecated
		public transient boolean experience = true;
	}

	public enum Type implements StringRepresentable {
		BOTH,
		NPC,
		BAZAAR;

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.config.farming.farmingHud.type." + name());
		}
	}
}
