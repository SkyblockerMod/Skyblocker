package de.hysky.skyblocker.skyblock.profileviewer.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Leveling {
	public int experience;
	@SerializedName("highest_pet_score")
	public int highestPetScore;
	@SerializedName("mining_fiesta_ores_mined")
	public long miningFiestaOresMined;
	@SerializedName("fishing_festival_sharks_killed")
	public int fishingFestivalSharksKilled;
	public boolean migrated;
	@SerializedName("migrated_completions_2")
	public boolean migratedCompletions2;
	@SerializedName("category_expanded")
	public boolean categoryExpanded;
	@SerializedName("claimed_talisman")
	public boolean claimedTalisman;

	public Map<String, Integer> completions;
	@SerializedName("bop_bonus")
	public String bookOfProgressionBonus;
	@SerializedName("emblem_unlocks")
	public List<String> emblemUnlocks = new ArrayList<>();
	@SerializedName("last_viewed_tasks")
	public List<String> lastViewedTasks = new ArrayList<>();
	@SerializedName("completed_tasks")
	public List<String> completedTasks = new ArrayList<>();
	@SerializedName("task_sort")
	public String taskSort;
}
