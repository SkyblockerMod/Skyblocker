package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Constants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class SlayerCarryCalculator {
				private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.##");

				private static final String[] SLAYER_TYPES = {"voidgloom", "revenant", "tarantula", "sven", "inferno", "riftstalker"};
				private static final String[] TIER_OPTIONS = {"t1", "t2", "t3", "t4", "t5"};

				private SlayerCarryCalculator() {
				}

				@Init
				public static void init() {
								ClientCommandRegistrationCallback.EVENT.register(SlayerCarryCalculator::registerCommands);
				}

				private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
								SuggestionProvider<FabricClientCommandSource> typeSuggestions = (context, builder) -> {
												for (String type : SLAYER_TYPES) {
																if (type.startsWith(builder.getRemainingLowerCase())) {
																				builder.suggest(type);
																}
												}
												return builder.buildFuture();
								};

								SuggestionProvider<FabricClientCommandSource> tierSuggestions = (context, builder) -> {
												for (String tier : TIER_OPTIONS) {
																if (tier.startsWith(builder.getRemainingLowerCase())) {
																				builder.suggest(tier);
																}
												}
												return builder.buildFuture();
								};

								dispatcher.register(literal(SkyblockerMod.NAMESPACE)
																.then(literal("slayercalculator")
																								.executes(SlayerCarryCalculator::showUsage)
																								.then(ClientCommandManager.argument("type", StringArgumentType.word())
																																.suggests(typeSuggestions)
																																.then(ClientCommandManager.argument("tier", StringArgumentType.word())
																																								.suggests(tierSuggestions)
																																								.then(ClientCommandManager.argument("price", StringArgumentType.word())
																																																.then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1))
																																																								.executes(ctx -> executeCalculation(ctx, 0))
																																																								.then(ClientCommandManager.argument("discount", IntegerArgumentType.integer(0, 100))
																																																																.executes(ctx -> executeCalculation(ctx, IntegerArgumentType.getInteger(ctx, "discount"))))))))));

								dispatcher.register(literal(SkyblockerMod.NAMESPACE)
																.then(literal("slayercalculator_share")
																								.then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
																																.executes(ctx -> {
																																				String message = StringArgumentType.getString(ctx, "message");
																																				Minecraft client = Minecraft.getInstance();
																																				if (client.player != null && client.player.connection != null) {
																																								client.player.connection.sendChat(message);
																																				}
																																				return 1;
																																}))));
				}

				private static int showUsage(CommandContext<FabricClientCommandSource> ctx) {
								ctx.getSource().sendFeedback(Constants.PREFIX.get()
																.append(Component.translatable("skyblocker.slayerCalculator.usage").withStyle(ChatFormatting.YELLOW)));
								ctx.getSource().sendFeedback(Component.literal("  /skyblocker slayercalculator <type> <tier> <price> <amount> [discount%]").withStyle(ChatFormatting.AQUA));
								ctx.getSource().sendFeedback(Component.literal("  ").append(Component.translatable("skyblocker.slayerCalculator.usageTypes").withStyle(ChatFormatting.GRAY)));
								ctx.getSource().sendFeedback(Component.literal("  ").append(Component.translatable("skyblocker.slayerCalculator.usageTiers").withStyle(ChatFormatting.GRAY)));
								ctx.getSource().sendFeedback(Component.literal("  ").append(Component.translatable("skyblocker.slayerCalculator.usagePrice").withStyle(ChatFormatting.GRAY)));
								return 1;
				}

				private static int executeCalculation(CommandContext<FabricClientCommandSource> ctx, int discount) {
								String typeInput = StringArgumentType.getString(ctx, "type");
								String tierInput = StringArgumentType.getString(ctx, "tier");
								String priceInput = StringArgumentType.getString(ctx, "price");
								int amount = IntegerArgumentType.getInteger(ctx, "amount");

								String normalizedType = normalizeSlayerType(typeInput);
								if (normalizedType == null) {
												ctx.getSource().sendFeedback(Constants.PREFIX.get()
																				.append(Component.translatable("skyblocker.slayerCalculator.unknownType", typeInput).withStyle(ChatFormatting.RED)));
												return 1;
								}

								int tier = parseTier(tierInput);
								if (tier < 1 || tier > 5) {
												ctx.getSource().sendFeedback(Constants.PREFIX.get()
																				.append(Component.translatable("skyblocker.slayerCalculator.invalidTier").withStyle(ChatFormatting.RED)));
												return 1;
								}

								double priceEach = parsePrice(priceInput);
								if (priceEach <= 0) {
												ctx.getSource().sendFeedback(Constants.PREFIX.get()
																				.append(Component.translatable("skyblocker.slayerCalculator.invalidPrice").withStyle(ChatFormatting.RED)));
												return 1;
								}

								double baseTotal = priceEach * amount;
								double totalPrice = discount > 0 ? baseTotal * (1 - discount / 100.0) : baseTotal;

								ctx.getSource().sendFeedback(Component.literal("====== ").withStyle(ChatFormatting.GOLD)
																.append(Component.translatable("skyblocker.slayerCalculator.header").withStyle(ChatFormatting.GOLD))
																.append(Component.literal(" ======").withStyle(ChatFormatting.GOLD)));

								ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.type").withStyle(ChatFormatting.YELLOW)
																.append(Component.literal(" " + normalizedType).withStyle(ChatFormatting.WHITE)));

								ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.tier").withStyle(ChatFormatting.YELLOW)
																.append(Component.literal(" T" + tier).withStyle(ChatFormatting.WHITE)));

								ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.amount").withStyle(ChatFormatting.YELLOW)
																.append(Component.literal(" " + amount + " bosses").withStyle(ChatFormatting.WHITE)));

								ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.priceEach").withStyle(ChatFormatting.YELLOW)
																.append(Component.literal(" " + formatPrice(priceEach)).withStyle(ChatFormatting.WHITE)));

								if (discount > 0) {
												ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.discount").withStyle(ChatFormatting.YELLOW)
																				.append(Component.literal(" " + discount + "%").withStyle(ChatFormatting.GREEN)));
								} else {
												ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.discount").withStyle(ChatFormatting.YELLOW)
																				.append(Component.literal(" ").append(Component.translatable("skyblocker.slayerCalculator.noDiscount").withStyle(ChatFormatting.GRAY))));
								}

								ctx.getSource().sendFeedback(Component.translatable("skyblocker.slayerCalculator.totalPrice").withStyle(ChatFormatting.YELLOW)
																.append(Component.literal(" " + formatPrice(totalPrice)).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));

								ctx.getSource().sendFeedback(Component.literal("==========================================").withStyle(ChatFormatting.GOLD));

								String shareMessage = "[Skyblocker] Slayer Carry: " + normalizedType + " T" + tier
																+ " x" + amount + " @ " + formatPrice(priceEach) + " each"
																+ (discount > 0 ? " (" + discount + "% off)" : "")
																+ " = " + formatPrice(totalPrice) + " total";

								MutableComponent clickText = Component.translatable("skyblocker.slayerCalculator.clickToShare")
																.withStyle(Style.EMPTY
																								.withColor(ChatFormatting.AQUA)
																								.withUnderlined(true)
																								.withClickEvent(new ClickEvent.RunCommand("/skyblocker slayercalculator_share " + shareMessage))
																								.withHoverEvent(new HoverEvent.ShowText(Component.translatable("skyblocker.slayerCalculator.clickToShareHover"))));
								ctx.getSource().sendFeedback(clickText);

								return 1;
				}

				static double parsePrice(String priceStr) {
								if (priceStr == null || priceStr.isEmpty()) {
												return 0;
								}

								priceStr = priceStr.toLowerCase(Locale.ROOT).replace(",", "").replace(" ", "");

								double multiplier = 1;
								if (priceStr.endsWith("k")) {
												multiplier = 1_000;
												priceStr = priceStr.substring(0, priceStr.length() - 1);
								} else if (priceStr.endsWith("m")) {
												multiplier = 1_000_000;
												priceStr = priceStr.substring(0, priceStr.length() - 1);
								} else if (priceStr.endsWith("b")) {
												multiplier = 1_000_000_000;
												priceStr = priceStr.substring(0, priceStr.length() - 1);
								}

								try {
												return Double.parseDouble(priceStr) * multiplier;
								} catch (NumberFormatException e) {
												return 0;
								}
				}

				static String formatPrice(double price) {
								if (price >= 1_000_000_000) {
												return PRICE_FORMAT.format(price / 1_000_000_000) + "B";
								} else if (price >= 1_000_000) {
												return PRICE_FORMAT.format(price / 1_000_000) + "M";
								} else if (price >= 1_000) {
												return PRICE_FORMAT.format(price / 1_000) + "K";
								} else {
												return PRICE_FORMAT.format(price);
								}
				}

				static int parseTier(String tierStr) {
								if (tierStr == null || tierStr.isEmpty()) {
												return -1;
								}

								tierStr = tierStr.toLowerCase(Locale.ROOT).replace("t", "").replace("tier", "");

								try {
												int tier = Integer.parseInt(tierStr);
												if (tier >= 1 && tier <= 5) {
																return tier;
												}
								} catch (NumberFormatException ignored) {
								}
								return -1;
				}

				static String normalizeSlayerType(String type) {
								if (type == null || type.isEmpty()) {
												return null;
								}

								type = type.toLowerCase(Locale.ROOT);

								if (type.contains("void") || type.contains("seraph") || type.contains("enderman")) {
												return "Voidgloom Seraph";
								} else if (type.contains("revenant") || type.contains("zombie") || type.contains("horror")) {
												return "Revenant Horror";
								} else if (type.contains("tarantula") || type.contains("spider") || type.contains("brood")) {
												return "Tarantula Broodfather";
								} else if (type.contains("sven") || type.contains("wolf") || type.contains("pack")) {
												return "Sven Packmaster";
								} else if (type.contains("inferno") || type.contains("blaze") || type.contains("demon")) {
												return "Inferno Demonlord";
								} else if (type.contains("rift") || type.contains("blood") || type.contains("vampire")) {
												return "Riftstalker Bloodfiend";
								}

								return null;
				}
}
