package de.hysky.skyblocker.compatibility;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

public class IrisCompatibility {
	public static void assignPipelines() {
		if (FabricLoader.getInstance().isModLoaded("iris")) {
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.FILLED, IrisProgram.BASIC);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.FILLED_THROUGH_WALLS, IrisProgram.BASIC);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.LINES_THROUGH_WALLS, IrisProgram.LINES);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.QUADS_THROUGH_WALLS, IrisProgram.BASIC);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.TEXTURE, IrisProgram.TEXTURED);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS, IrisProgram.TEXTURED);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.CYLINDER, IrisProgram.BASIC);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.CIRCLE, IrisProgram.BASIC);
			IrisApi.getInstance().assignPipeline(SkyblockerRenderPipelines.CIRCLE_LINES, IrisProgram.BASIC);
		}
	}
}
