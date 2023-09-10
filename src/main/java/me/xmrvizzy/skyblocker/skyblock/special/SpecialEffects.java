package me.xmrvizzy.skyblocker.skyblock.special;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;

public class SpecialEffects {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialEffects.class);
	private static final Pattern DROP_PATTERN = Pattern.compile("(?:\\[[A-Z+]+\\] )?(?<player>[A-Za-z0-9_]+) unlocked (?<item>.+)!");

	public static void init() {
		ClientReceiveMessageEvents.GAME.register(SpecialEffects::displayRareDropEffect);
	}
	
	private static void displayRareDropEffect(Text message, boolean overlay) {
		//We don't check if we're in dungeons because that check doesn't work in m7 which defeats the point of this
		//It might also allow it to work with Croesus
		if (Utils.isOnSkyblock() && SkyblockerConfig.get().general.specialEffects.rareDungeonDropEffects) {
			try {
				String stringForm = message.getString();
				Matcher matcher = DROP_PATTERN.matcher(stringForm);
				
				if (matcher.matches()) {
					MinecraftClient client = MinecraftClient.getInstance();
					String player = matcher.group("player");
					
					if (player.equals(client.getSession().getUsername())) {
						ItemStack stack = getStackFromName(matcher.group("item"));
						
						if (!stack.isEmpty()) {
							if (RenderSystem.isOnRenderThread()) {
								client.particleManager.addEmitter(client.player, ParticleTypes.PORTAL, 30);
								client.gameRenderer.showFloatingItem(stack);
							} else {
								RenderSystem.recordRenderCall(() -> {
									client.particleManager.addEmitter(client.player, ParticleTypes.PORTAL, 30);
									client.gameRenderer.showFloatingItem(stack);
								});
							}
						}
					}
				}
			} catch (Exception e) { //Incase there's a regex failure or something else bad happens
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered: ", e);
			}
		}
	}
	
	private static ItemStack getStackFromName(String itemName) {
		ItemStack necronHandle = new ItemStack(Items.STICK);
		necronHandle.addEnchantment(Enchantments.PROTECTION, 1);
		
		ItemStack scroll = new ItemStack(Items.WRITABLE_BOOK);
		scroll.addEnchantment(Enchantments.PROTECTION, 1);
		
		ItemStack tier5Skull = ItemStack.EMPTY;
		ItemStack fifthStar = ItemStack.EMPTY;
		
		try {
			tier5Skull = ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;-1613868903,-527154034,-1445577520,748807544],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTEwZjlmMTA4NWQ0MDcxNDFlYjc3NjE3YTRhYmRhYWEwOGQ4YWYzM2I5NjAyMDBmZThjMTI2YzFkMTQ0NTY4MiJ9fX0=\"}]}}}}"));
			fifthStar = ItemStack.fromNbt(StringNbtReader.parse("{id:\"minecraft:player_head\",Count:1,tag:{SkullOwner:{Id:[I;1904417095,756174249,-1302927470,1407004198],Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFjODA0MjUyN2Y4MWM4ZTI5M2UyODEwMTEzNDg5ZjQzOTRjYzZlZmUxNWQxYWZhYzQzMTU3MWM3M2I2MmRjNCJ9fX0=\"}]}}}}"));
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Special Effects] Failed to parse NBT for a player head!");
		}
		
		return switch (itemName) {
			//M7
			case "Necron Dye" -> new ItemStack(Items.ORANGE_DYE);
			case "Dark Claymore" -> new ItemStack(Items.STONE_SWORD);
			case "Necron's Handle" -> necronHandle;
			case "Shiny Necron's Handle" -> necronHandle;
			case "Enchanted Book (Thunderlord VII)" -> new ItemStack(Items.ENCHANTED_BOOK);
			case "Master Skull - Tier 5" -> tier5Skull;
			case "Shadow Warp" -> scroll;
			case "Wither Shield" -> scroll;
			case "Implosion" -> scroll;
			case "Fifth Master Star" -> fifthStar;
			
			//M6
			case "Giant's Sword" -> new ItemStack(Items.IRON_SWORD);
			
			default -> ItemStack.EMPTY;
		};
	}
}
