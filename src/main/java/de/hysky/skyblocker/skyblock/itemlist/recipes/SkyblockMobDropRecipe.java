package de.hysky.skyblocker.skyblock.itemlist.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.EntityCreator;
import de.hysky.skyblocker.utils.ItemUtils;
import io.github.moulberry.repo.data.NEUMobDropRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SkyblockMobDropRecipe implements SkyblockRecipe {

    public static final Identifier IDENTIFIER = Identifier.of(SkyblockerMod.NAMESPACE, "skyblock_drops");

    private final List<ItemStack> drops = new ArrayList<>();
    private final @Nullable LivingEntity entity;

    public SkyblockMobDropRecipe(NEUMobDropRecipe mobDropRecipe) {
        for (NEUMobDropRecipe.Drop drop : mobDropRecipe.getDrops()) {
            ItemStack itemStack = SkyblockRecipe.getItemStack(drop.getDropItem());
            ArrayList<Text> texts = new ArrayList<>(ItemUtils.getLore(itemStack));
            if (drop.getChance() != null) texts.add(Text.literal(drop.getChance()));
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(texts));
            drops.add(itemStack);
        }
        if (mobDropRecipe.getRender().startsWith("@")) entity = EntityCreator.createEntity(mobDropRecipe.getRender().substring(1));
        else {
            EntityType<?> type = EntityCreator.ID_TO_TYPE.get(mobDropRecipe.getRender());
            if (type != null) entity = (LivingEntity) type.create(MinecraftClient.getInstance().world);
            else entity = null;
        }
    }

    @Override
    public List<RecipeSlot> getInputSlots(int width, int height) {
        return List.of();
    }

    @Override
    public List<RecipeSlot> getOutputSlots(int width, int height) {
        List<RecipeSlot> out = new ArrayList<>();
        int maxPerRow = (width / 2 - 4) / 18;
        for (int i = 0; i < drops.size(); i++) {
            int x = width / 2 + (i % maxPerRow) * 18;
            int y = 4 + (i / maxPerRow) * 18;
            out.add(new RecipeSlot(x, y, drops.get(i), false));
        }
        return out;
    }

    @Override
    public List<ItemStack> getInputs() {
        return List.of();
    }

    @Override
    public List<ItemStack> getOutputs() {
        return drops;
    }

    private void drawEntityNoScissors(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, LivingEntity entity) {
        float g = (float)(x1 + x2) / 2.0F;
        float h = (float)(y1 + y2) / 2.0F;
        float i = (float)Math.atan((g - mouseX) / 40.0F);
        float j = (float)Math.atan((h - mouseY) / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf2 = new Quaternionf().rotateX(j * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf2);
        float k = entity.bodyYaw;
        float l = entity.getYaw();
        float m = entity.getPitch();
        float n = entity.prevHeadYaw;
        float o = entity.headYaw;
        entity.bodyYaw = 180.0F + i * 20.0F;
        entity.setYaw(180.0F + i * 40.0F);
        entity.setPitch(-j * 20.0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        float p = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() / 2.0F + f * p, 0.0F);
        float q = (float)size / p;
        InventoryScreen.drawEntity(context, g, h, q, vector3f, quaternionf, quaternionf2, entity);
        entity.bodyYaw = k;
        entity.setYaw(l);
        entity.setPitch(m);
        entity.prevHeadYaw = n;
        entity.headYaw = o;
    }

    @Override
    public void render(DrawContext context, int width, int height, double mouseX, double mouseY) {
        if (entity == null) return;

        drawEntityNoScissors(
                context, 4, 4, height - 4, width / 2 - 4,
                24, 0.0625F, (int) mouseX, (int) mouseY, entity);
    }

    @Override
    public Text getExtraText() {
        return Text.empty();
    }

    @Override
    public Identifier getCategoryIdentifier() {
        return IDENTIFIER;
    }
}
