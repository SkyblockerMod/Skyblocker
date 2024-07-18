package de.hysky.skyblocker.skyblock.itemlist;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.mixins.CreeperEntityAccessor;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EntityCreator {

    public static final Map<String, EntityType<?>> ID_TO_TYPE = Map.ofEntries(
            Map.entry("Zombie", EntityType.ZOMBIE),
            Map.entry("Chicken", EntityType.CHICKEN),
            Map.entry("Slime", EntityType.SLIME),
            Map.entry("Wolf", EntityType.WOLF),
            Map.entry("Skeleton", EntityType.SKELETON),
            Map.entry("Creeper", EntityType.CREEPER),
            Map.entry("Ocelot", EntityType.OCELOT),
            Map.entry("Blaze", EntityType.BLAZE),
            Map.entry("Rabbit", EntityType.RABBIT),
            Map.entry("Sheep", EntityType.SHEEP),
            Map.entry("Horse", EntityType.HORSE),
            Map.entry("Eisengolem", EntityType.IRON_GOLEM),
            Map.entry("Silverfish", EntityType.SILVERFISH),
            Map.entry("Witch", EntityType.WITCH),
            Map.entry("Endermite", EntityType.ENDERMITE),
            Map.entry("Snowman", EntityType.SNOW_GOLEM),
            Map.entry("Villager", EntityType.VILLAGER),
            Map.entry("Guardian", EntityType.GUARDIAN),
            Map.entry("ArmorStand", EntityType.ARMOR_STAND),
            Map.entry("Squid", EntityType.SQUID),
            Map.entry("Bat", EntityType.BAT),
            Map.entry("Spider", EntityType.SPIDER),
            Map.entry("CaveSpider", EntityType.CAVE_SPIDER),
            Map.entry("Pigman", EntityType.ZOMBIFIED_PIGLIN),
            Map.entry("Ghast", EntityType.GHAST),
            Map.entry("MagmaCube", EntityType.MAGMA_CUBE),
            Map.entry("Wither", EntityType.WITHER),
            Map.entry("Enderman", EntityType.ENDERMAN),
            Map.entry("Mooshroom", EntityType.MOOSHROOM),
            Map.entry("WitherSkeleton", EntityType.WITHER_SKELETON),
            Map.entry("Cow", EntityType.COW),
            Map.entry("Dragon", EntityType.ENDER_DRAGON),
            Map.entry("Pig", EntityType.PIG),
            Map.entry("Giant", EntityType.GIANT)
    );

    public static @Nullable LivingEntity createEntity(String jsonFilePath) {
        JsonObject object;
        try (BufferedReader reader = Files.newBufferedReader(NEURepoManager.NEU_REPO.getBaseFolder().resolve(jsonFilePath.replace("neurepo:", "")))) {
            object = SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
        } catch (IOException ignored) {
            return null;
        }

        return createEntityFromJson(object);
    }

    public static @Nullable LivingEntity createEntityFromJson(JsonObject object) {
        LivingEntity entity;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return null;
        if ("Player".equals(object.get("entity").getAsString())) {
            entity = new EditablePlayer(client.world);
        } else {
            EntityType<?> type = ID_TO_TYPE.get(object.get("entity").getAsString());
            if (type == null) return null;
            entity = (LivingEntity) type.create(client.world);
        }
        if (entity == null) return null;
        if (object.has("modifiers")) {
            for (JsonElement jsonElement : object.getAsJsonArray("modifiers")) {
                JsonObject modifier = jsonElement.getAsJsonObject();
                switch (modifier.get("type").getAsString()) {
                    case "age" -> makeEntitySmol(entity, modifier);
                    case "equipment" -> addEquipment(entity, modifier);
                    case "riding" -> riding(entity, modifier);
                    case "charged" -> {
                        if (entity instanceof CreeperEntity creeper) creeper.getDataTracker().set(CreeperEntityAccessor.getCharged(), true);
                    }
                    case "name" -> {
                        entity.setCustomName(Text.literal(modifier.get("name").getAsString()));
                        entity.setCustomNameVisible(true);
                    }
                    case "witherdata" -> wither(entity, modifier);
                    case "invisible" -> entity.setInvisible(!modifier.has("invisible") || modifier.get("invisible").getAsBoolean());
                    case "horse" -> {
                        LivingEntity horsey = horsey(entity, modifier);
                        if (horsey != null) entity = horsey;
                    }
                    case "playerdata" -> player(entity, modifier);
                    default -> {}
                }
            }
        }
        return entity;
    }

    public static void makeEntitySmol(LivingEntity entity, JsonObject modifier) {
        if (modifier.has("isBaby") && modifier.get("isBaby").getAsBoolean() && entity instanceof MobEntity mob) {
            mob.setBaby(true);
        }
    }

    private static ItemStack createLeatherThingy(Item item, String color) {
        return new ItemStack(RegistryEntry.of(item), 1, ComponentChanges.builder().add(DataComponentTypes.DYED_COLOR, new DyedColorComponent(Integer.parseInt(color, 16), false)).build());
    }

    public static void addEquipment(LivingEntity entity, JsonObject modifier) {
        Map<String, EquipmentSlot> fields = Map.of(
                "helmet", EquipmentSlot.HEAD,
                "chestplate", EquipmentSlot.CHEST,
                "leggings", EquipmentSlot.LEGS,
                "feet", EquipmentSlot.FEET,
                "hand", EquipmentSlot.MAINHAND
        );

        for (Map.Entry<String, EquipmentSlot> entry : fields.entrySet()) {
            if (!modifier.has(entry.getKey())) continue;
            String s = modifier.get(entry.getKey()).getAsString();
            if (s.contains("#")) {
                String[] split = s.split("#", 2);
                ItemStack toEquip = switch (split[0]) {
                    case "SKULL" -> new ItemStack(RegistryEntry.of(Items.PLAYER_HEAD), 1, ComponentChanges.builder().add(
                            DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.empty(), ItemUtils.propertyMapWithTexture(split[1]))).build());
                    case "LEATHER_HELMET" -> createLeatherThingy(Items.LEATHER_HELMET, split[1]);
                    case "LEATHER_CHESTPLATE" -> createLeatherThingy(Items.LEATHER_CHESTPLATE, split[1]);
                    case "LEATHER_LEGGINGS" -> createLeatherThingy(Items.LEATHER_LEGGINGS, split[1]);
                    case "LEATHER_BOOTS" -> createLeatherThingy(Items.LEATHER_BOOTS, split[1]);
                    case null, default -> ItemStack.EMPTY;
                };
                entity.equipStack(entry.getValue(), toEquip);
            } else {
                NEUItem id = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(s);
                if (id == null) continue;
                ItemStack stack = ItemStackBuilder.fromNEUItem(id);
                entity.equipStack(entry.getValue(), stack);
            }
        }
    }

    public static void riding(LivingEntity entity, JsonObject modifier) {
        LivingEntity entityFromJson = createEntityFromJson(modifier);
        if (entityFromJson == null) return;
        entity.startRiding(entityFromJson, true);
    }

    public static void wither(LivingEntity entity, JsonObject modifier) {
        if (!(entity instanceof WitherEntity wither)) return;
        if (modifier.has("tiny") && modifier.get("tiny").getAsBoolean()) {
            wither.setInvulTimer(800);
        } else wither.setInvulTimer(0);
        if (modifier.has("armored") && modifier.get("armored").getAsBoolean()) {
            wither.setHealth(1);
        } else wither.setHealth(9999999);

    }

    public static LivingEntity horsey(LivingEntity entity, JsonObject modifier) {
        if (!(entity instanceof AbstractHorseEntity horse)) return null;
        if (modifier.has("kind")) {
            final ClientWorld world = MinecraftClient.getInstance().world;
            switch (modifier.get("kind").getAsString()) {
                case "skeleton" -> horse = EntityType.SKELETON_HORSE.create(world);
                case "zombie" -> horse = EntityType.ZOMBIE_HORSE.create(world);
                case "donkey" -> horse = EntityType.DONKEY.create(world);
                case "mule" -> horse = EntityType.MULE.create(world);
            }
        }
        if (horse == null) return null;
        if (modifier.has("armor")) {
            if (modifier.isJsonNull()) horse.equipStack(EquipmentSlot.BODY, null);
            else horse.equipStack(EquipmentSlot.BODY, switch (modifier.get("armor").getAsString()) {
                case "diamond" -> new ItemStack(Items.DIAMOND_HORSE_ARMOR);
                case "gold" -> new ItemStack(Items.GOLDEN_HORSE_ARMOR);
                case "iron" -> new ItemStack(Items.IRON_HORSE_ARMOR);
                case null, default -> ItemStack.EMPTY;
            });
        }
        if (modifier.has("saddled") && modifier.get("saddled").getAsBoolean()) {
            horse.saddle(Items.SADDLE.getDefaultStack(), null);
        }
        return entity;
    }

    public static void player(LivingEntity entity, JsonObject modifier) {
        if (!(entity instanceof EditablePlayer player)) return;
        if (modifier.has("cape")) {
            player.setCape(Identifier.of(modifier.get("cape").getAsString()));
        }
        if (modifier.has("skin")) {
            player.setSkin(Identifier.of(modifier.get("skin").getAsString()));
        }
        if (modifier.has("slim")) {
            player.setSlim(modifier.get("slim").getAsBoolean());
        }
        if (modifier.has("parts")) {
            JsonElement jsonElement = modifier.get("parts");
            if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
                player.setSecondLayer((byte) (jsonElement.getAsBoolean() ? -1 : 0));
            } else {
                int bits = 0;
                JsonObject obj = jsonElement.getAsJsonObject();
                for (Map.Entry<String, JsonElement> part : obj.entrySet()) {
                    PlayerModelPart modelPart = PlayerModelPart.valueOf(part.getKey());
                    if (part.getValue().getAsBoolean()) {
                        bits |= modelPart.getBitFlag();
                    } else {
                        bits &= ~modelPart.getBitFlag();
                    }
                }
                player.setSecondLayer((byte) bits);
            }

        }

    }

    private static class EditablePlayer extends AbstractClientPlayerEntity {

        private SkinTextures textures = DefaultSkinHelper.getSkinTextures(getGameProfile());

        public EditablePlayer(ClientWorld world) {
            super(world, new GameProfile(UUID.randomUUID(), ""));
            setCustomName(Text.empty());
        }

        public void setCape(Identifier cape) {
            textures = new SkinTextures(
                    textures.texture(),
                    textures.textureUrl(),
                    cape,
                    textures.elytraTexture(),
                    textures.model(),
                    textures.secure());
        }

        public void setSlim(boolean slim) {
            textures = new SkinTextures(
                    textures.texture(),
                    textures.textureUrl(),
                    textures.capeTexture(),
                    textures.elytraTexture(),
                    slim ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE,
                    textures.secure());
        }

        public void setSkin(Identifier skin) {
            textures = new SkinTextures(
                    skin,
                    textures.textureUrl(),
                    textures.capeTexture(),
                    textures.elytraTexture(),
                    textures.model(),
                    textures.secure());
        }

        public void setSecondLayer(byte bytes) {
            getDataTracker().set(PLAYER_MODEL_PARTS, bytes);
        }

        @Override
        public SkinTextures getSkinTextures() {
            return textures;
        }

        @Override
        public Text getName() {
            return getCustomName();
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }
    }
}
