package de.hysky.skyblocker.compatibility;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import org.slf4j.Logger;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.utils.render.SkyblockerRenderPipelines;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;

public class IrisCompatibility {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean IRIS_ENABLED = FabricLoader.getInstance().isModLoaded("iris");
	private static final String IRIS_API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";
	private static final String IRIS_PROGRAM_CLASS = "net.irisshaders.iris.api.v0.IrisProgram";
	private static final MethodHandle GET_IRIS_API = getIrisApiHandle();
	private static final MethodHandle REGISTER_PIPELINE = getRegisterPipelineHandle();
	private static final MethodHandle GET_IRIS_PROGRAM = getIrisProgramHandle();

	/**
	 * Assigns all of the mod's needed pipelines to an iris program.
	 */
	public static void assignPipelines() {
		if (IRIS_ENABLED) {
			try {
				assignPipeline(RenderPipelines.DEBUG_FILLED_BOX, "BASIC");
				assignPipeline(SkyblockerRenderPipelines.FILLED_THROUGH_WALLS, "BASIC");
				assignPipeline(SkyblockerRenderPipelines.LINES_THROUGH_WALLS, "LINES");
				assignPipeline(SkyblockerRenderPipelines.QUADS_THROUGH_WALLS, "BASIC");
				assignPipeline(SkyblockerRenderPipelines.TEXTURE, "TEXTURED");
				assignPipeline(SkyblockerRenderPipelines.TEXTURE_THROUGH_WALLS, "TEXTURED");
				assignPipeline(SkyblockerRenderPipelines.CYLINDER, "BASIC");
				assignPipeline(SkyblockerRenderPipelines.CIRCLE, "BASIC");
				assignPipeline(SkyblockerRenderPipelines.CIRCLE_LINES, "BASIC");
			} catch (IllegalStateException ignored) {
				//The pipeline was probably already registered
			} catch (Throwable e) {
				LOGGER.error("[Skyblocker Iris Compatibility] Failed to assign pipelines.", e);
			}
		}
	}

	/**
	 * Assigns a pipeline to a given {@code IrisProgram}.
	 *
	 * @param pipeline The pipeline to be assigned.
	 * @param irisProgramName The exact name of the {@code IrisProgram} enum entry.
	 */
	private static void assignPipeline(RenderPipeline pipeline, String irisProgramName) throws Throwable {
		Objects.requireNonNull(GET_IRIS_API, "Iris API handle must be present to assign a pipeline.");
		Objects.requireNonNull(REGISTER_PIPELINE, "Iris register pipeline handle must be present to assign a pipeline.");
		Objects.requireNonNull(GET_IRIS_PROGRAM, "Iris Program handle must be present to assign a pipeline.");

		REGISTER_PIPELINE.invoke(GET_IRIS_API.invoke(), pipeline, GET_IRIS_PROGRAM.invoke(irisProgramName));
	}

	private static MethodHandle getIrisApiHandle() {
		try {
			Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodType type = MethodType.methodType(irisApiClass);

			return lookup.findStatic(irisApiClass, "getInstance", type);
		} catch (Exception e) {
			return null;
		}
	}

	private static MethodHandle getRegisterPipelineHandle() {
		try {
			Class<?> irisApiClass = Class.forName(IRIS_API_CLASS);
			Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			MethodType type = MethodType.methodType(void.class, RenderPipeline.class, irisProgramClass);

			return lookup.findVirtual(irisApiClass, "assignPipeline", type);
		} catch (Exception e) {
			return null;
		}
	}

	private static MethodHandle getIrisProgramHandle() {
		try {
			Class<?> irisProgramClass = Class.forName(IRIS_PROGRAM_CLASS);
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodType type = MethodType.methodType(Enum.class, Class.class, String.class);
			MethodHandle enumValueOf = lookup.findStatic(Enum.class, "valueOf", type);

			return MethodHandles.insertArguments(enumValueOf, 0, irisProgramClass);
		} catch (Exception e) {
			return null;
		}
	}
}
