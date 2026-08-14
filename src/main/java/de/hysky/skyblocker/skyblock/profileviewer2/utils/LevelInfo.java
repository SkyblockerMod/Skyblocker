package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

/// @param xp the total xp
/// @param level the current level
/// @param cap information about the cap applied to the respective object (skill, dungeon class, slayer, pet, etc.)
/// @param progress information about the progress towards the next level, if there is one
public record LevelInfo(long xp, int level, Cap cap, Optional<Progress> progress) {

	public LevelInfo(long xp, int level, Cap cap, @Nullable Progress progress) {
		this(xp, level, cap, Optional.ofNullable(progress));
	}

	/// {@return whether the level is not at any maximum value, capped or not}
	public boolean isLevelNotAtAnyMaximum() {
		return this.level != this.cap.reachableMaxLevel() && this.level != this.cap.absoluteMaxLevel();
	}

	/// {@return whether the level is capped and not at its absolute max value}
	public boolean isLevelCapped() {
		return this.level == this.cap.reachableMaxLevel() && this.level != this.cap.absoluteMaxLevel();
	}

	/// {@return whether the level is at the highest value it can possibly go without a cap being imposed}
	public boolean isLevelAbsolutelyMaxed() {
		return this.level == this.cap.absoluteMaxLevel();
	}

	/// Holds information about the applicable level cap.
	///
	/// @param reachableMaxLevel the highest level that can currently be reached (due to a cap potentially being imposed)
	/// @param absoluteMaxLevel the highlight possible level that can be reached (without respect to any caps)
	public record Cap(int reachableMaxLevel, int absoluteMaxLevel) {}

	/// Holds information about the progress towards the next level.
	///
	/// @param xpProgress the XP that is going towards the next level (say 100 out of 300 needed)
	/// @param xpNeeded the XP needed to reach the next level (non-cumulative)
	public record Progress(long xpProgress, long xpNeeded) {
		/// {@return the percentage to the next level as a double from 0-1}
		public double percentageToNextLevel() {
			return  (double) this.xpProgress() / (double) this.xpNeeded();
		}
	}
}
