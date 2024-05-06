package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class MiningLocationLabels {
    final Text name;
    final Category category;
    private final Vec3d centerPos;

    MiningLocationLabels(Category category, Text name, BlockPos pos) {
        this.name = name;
        this.category = category;
        this.centerPos = pos.toCenterPos();
    }

    public void render(WorldRenderContext context) {
        Vec3d posUp = centerPos.add(0, 1, 0);
        double distance = context.camera().getPos().distanceTo(centerPos);
        float scale = (float) (1 * (distance / 10));
        RenderHelper.renderText(context, name, posUp, scale, true);
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, scale, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);

    }


    interface Category {
        String getName();

        float[] getColorComponents();

    }

    enum dwarvenCategory implements Category {
        LAVA_SPRINGS("Lava Springs", Color.CYAN, new BlockPos(60, 197, -15)),
        CLIFFSIDE_VEINS("Cliffside Veins", Color.CYAN, new BlockPos(40, 128, 40)),
        RAMPARTS_QUARRY("Rampart's Quarry", Color.CYAN, new BlockPos(-100, 150, -20)),
        UPPER_MINES("Upper Mines", Color.CYAN, new BlockPos(-130, 174, -50)),
        ROYAL_MINES("Royal Mines", Color.CYAN, new BlockPos(130, 154, 30)),
        GLACITE_WALKER("Glacite Walker", Color.CYAN, new BlockPos(0, 128, 150));


        public final Color color;
        private final String name;
        private final float[] colorComponents;
        private final BlockPos location;

        dwarvenCategory(String name, Color color, BlockPos location) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
            this.location = location;

        }

        public BlockPos getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return name;
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public float[] getColorComponents() {
            return colorComponents;
        }

    }

    enum glaciteCategory implements Category {
        AQUAMARINE("Aquamarine", Color.BLUE, new BlockPos[]{new BlockPos(-1, 139, 437), new BlockPos(90, 151, 229), new BlockPos(56, 151, 400), new BlockPos(51, 117, 303)}),
        ONYX("Onyx", Color.BLACK, new BlockPos[]{new BlockPos(79, 119, 411), new BlockPos(-14, 132, 386), new BlockPos(18, 136, 370), new BlockPos(16, 138, 411), new BlockPos(-68, 130, 408)}),
        PERIDOT("Peridot", Color.GREEN, new BlockPos[]{new BlockPos(-61, 147, 302), new BlockPos(91, 122, 397),new BlockPos(-73, 122, 458), new BlockPos(-77, 120, 282)}),
        CITRINE("Citrine", Color.YELLOW, new BlockPos[]{new BlockPos(-104, 144, 244), new BlockPos(39, 119, 386), new BlockPos(-57, 144, 421), new BlockPos(-47, 126, 418) });

        public final Color color;
        private final String name;
        private final float[] colorComponents;
        private final BlockPos[] location;

        glaciteCategory(String name, Color color, BlockPos[] location) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
            this.location = location;

        }

        public BlockPos[] getLocations() {
            return location;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float[] getColorComponents() {
            return colorComponents;
        }

    }
}
