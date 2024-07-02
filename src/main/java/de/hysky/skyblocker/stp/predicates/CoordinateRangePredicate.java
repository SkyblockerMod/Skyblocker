package de.hysky.skyblocker.stp.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.stp.SkyblockerPredicateType;
import de.hysky.skyblocker.stp.SkyblockerPredicateTypes;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record CoordinateRangePredicate(BlockPos pos1, BlockPos pos2) implements SkyblockerTexturePredicate {
	public static final Identifier ID = Identifier.of(SkyblockerMod.NAMESPACE, "coordinate_range");
	public static final MapCodec<CoordinateRangePredicate> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockPos.CODEC.fieldOf("pos1").forGetter(CoordinateRangePredicate::pos1),
			BlockPos.CODEC.fieldOf("pos2").forGetter(CoordinateRangePredicate::pos2))
			.apply(instance, CoordinateRangePredicate::new));
	public static final Codec<CoordinateRangePredicate> CODEC = MAP_CODEC.codec();

	@Override
	public boolean test(ItemStack stack) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		if (player != null) {
			BlockPos pos = player.getBlockPos();

			return RenderHelper.pointIsInArea(pos.getX(), pos.getY(), pos.getZ(), pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
		}

		return false;
	}

	@Override
	public SkyblockerPredicateType<?> getType() {
		return SkyblockerPredicateTypes.COORDINATE_RANGE;
	}

	@Override
	public boolean itemStackDependent() {
		return false;
	}
}
