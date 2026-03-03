package de.hysky.skyblocker.skyblock.profileviewer2.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Levelling {
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
	public Map<String, Integer> completions = Map.of();
	@SerializedName("bop_bonus")
	public String bookOfProgressionBonus = "";
	@SerializedName("selected_symbol")
	public String selectedEmblem = "";
	@SerializedName("emblem_unlocks")
	public List<String> emblemUnlocks = List.of();
	@SerializedName("last_viewed_tasks")
	public List<String> lastViewedTasks = List.of();
	@SerializedName("completed_tasks")
	public List<String> completedTasks = List.of();
	@SerializedName("task_sort")
	public String taskSort = "";
}
