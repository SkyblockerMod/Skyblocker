package de.hysky.skyblocker.utils.waypoint;

import de.hysky.skyblocker.utils.Utils;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;

public class ProfileAwareWaypoint extends Waypoint {
	public final Set<String> foundProfiles = new HashSet<>();
	private final float[] missingColor;
	private final float[] foundColor;

	public ProfileAwareWaypoint(BlockPos pos, Supplier<Type> typeSupplier, float[] missingColor, float[] foundColor) {
		super(pos, typeSupplier, null);
		this.missingColor = missingColor;
		this.foundColor = foundColor;
	}

	@Override
	public boolean shouldRender() {
		return !foundProfiles.contains(Utils.getProfile());
	}

	@Override
	public void setFound() {
		foundProfiles.add(Utils.getProfile());
	}

	public void setFound(String profile) {
		foundProfiles.add(profile);
	}

	@Override
	public void setMissing() {
		foundProfiles.remove(Utils.getProfile());
	}

	@Override
	public float[] getRenderColorComponents() {
		return foundProfiles.contains(Utils.getProfile()) ? foundColor : missingColor;
	}
}
