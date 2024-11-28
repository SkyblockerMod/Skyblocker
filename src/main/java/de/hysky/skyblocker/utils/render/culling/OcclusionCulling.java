package de.hysky.skyblocker.utils.render.culling;

import de.hysky.skyblocker.annotations.Init;

public class OcclusionCulling {
	private static final int TRACING_DISTANCE = 128;
	private static OcclusionCuller regularCuller = null;
	private static OcclusionCuller reducedCuller = null;

	/**
	 * Initializes the occlusion culling instances
	 */
	@Init
	public static void init() {
		regularCuller = new OcclusionCuller(TRACING_DISTANCE, new WorldProvider(), 2);
		reducedCuller = new OcclusionCuller(TRACING_DISTANCE, new ReducedWorldProvider(), 0);
	}

	public static OcclusionCuller getRegularCuller() {
		return regularCuller;
	}

	public static OcclusionCuller getReducedCuller() {
		return reducedCuller;
	}
}
