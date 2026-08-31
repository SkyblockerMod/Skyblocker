package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.ProblemReporter;

public class JsonValueOutput {
	private final JsonObject output;
	private final ProblemReporter reporter;

	public JsonValueOutput(ProblemReporter reporter, JsonObject output) {
		this.output = output;
		this.reporter = reporter;
	}

	public JsonValueOutput(JsonObject output) {
		this(ProblemReporter.DISCARDING, output);
	}

	public <T> void write(final String name, final Codec<T> codec, final T value) {
		switch (codec.encodeStart(JsonOps.INSTANCE, value)) {
			case DataResult.Success<JsonElement> success:
				this.output.add(name, success.value());
				break;
			case DataResult.Error<JsonElement> error:
				this.reporter.report(() -> "Failed to encode '" + value + "' into field '" + name + "': " + error);
				error.partialValue().ifPresent(/* lambda$store$0 */ partial -> this.output.add(name, partial));
				break;
			default:
				throw new MatchException(null, null);
		}
	}

	public void writeNumber(String field, Number value) {
		output.addProperty(field, value);
	}

	public void writeBool(String field, boolean value) {
		output.addProperty(field, value);
	}

	public void writeString(String field, String value) {
		output.addProperty(field, value);
	}
}
