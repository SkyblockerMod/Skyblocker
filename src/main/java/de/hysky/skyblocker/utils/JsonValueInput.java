package de.hysky.skyblocker.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ProblemReporter;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class JsonValueInput {
	private final JsonObject input;
	private final ProblemReporter reporter;

	public JsonValueInput(ProblemReporter reporter, JsonObject input) {
		this.input = input;
		this.reporter = reporter;
	}

	public JsonValueInput(JsonObject input) {
		this(ProblemReporter.DISCARDING, input);
	}

	public <T> Optional<T> read(String name, Codec<T> codec) {
		JsonElement element = input.get(name);
		if (element == null) return Optional.empty();
		return switch (codec.parse(JsonOps.INSTANCE, element)) {
			case DataResult.Success<T> success -> Optional.of(success.value());
			case DataResult.Error<T> error -> {
				reporter.report(() -> "Failed to decode value '" + element + "' from field '" + name + "': " + error.message());
				yield error.partialValue();
			}
		};
	}

	public <T> Optional<T> read(MapCodec<T> codec) {
		return switch (JsonOps.INSTANCE.getMap(this.input).flatMap(/* lambda$read$0 */ map -> codec.decode(JsonOps.INSTANCE, map))) {
			case DataResult.Success<T> success -> Optional.of(success.value());
			case DataResult.Error<T> error -> {
				this.reporter.report(() ->  "Failed to decode from map: " + error.message());
				yield error.partialValue();
			}
		};
	}

	public OptionalInt readInt(String name) {
		Number element = readNumber(name);
		if (element == null) return OptionalInt.empty();
		return OptionalInt.of(element.intValue());
	}

	public int readIntOr(String name, int defaultValue) {
		Number element = readNumber(name);
		if (element == null) return defaultValue;
		return element.intValue();
	}

	public OptionalLong readLong(String name) {
		Number element = readNumber(name);
		if (element == null) return OptionalLong.empty();
		return OptionalLong.of(element.longValue());
	}

	public long readLongOr(String name, long defaultValue) {
		Number element = readNumber(name);
		if (element == null) return defaultValue;
		return element.longValue();
	}

	public OptionalDouble readDouble(String name) {
		Number element = readNumber(name);
		if (element == null) return OptionalDouble.empty();
		return OptionalDouble.of(element.longValue());
	}

	public double readDoubleOr(String name, double defaultValue) {
		Number element = readNumber(name);
		if (element == null) return defaultValue;
		return element.doubleValue();
	}

	public Optional<Float> readFloat(String name) {
		Number element = readNumber(name);
		if (element == null) return Optional.empty();
		return Optional.of(element.floatValue());
	}

	public float readFloatOr(String name, float defaultValue) {
		Number element = readNumber(name);
		if (element == null) return defaultValue;
		return element.floatValue();
	}

	public Optional<Boolean> readBoolean(String name) {
		JsonElement element = input.get(name);
		if (element == null) return Optional.empty();
		if (!element.isJsonPrimitive()) {
			reporter.report(expectedPrimtiveProblem(name, element));
			return Optional.empty();
		}
		return Optional.of(element.getAsBoolean());
	}

	public boolean readBooleanOr(String name, boolean defaultValue) {
		JsonElement element = input.get(name);
		if (element == null) return defaultValue;
		if (!element.isJsonPrimitive()) {
			reporter.report(expectedPrimtiveProblem(name, element));
			return defaultValue;
		}
		return element.getAsBoolean();
	}

	private @Nullable Number readNumber(String name) {
		JsonElement element = input.get(name);
		if (element == null) return null;
		if (!element.isJsonPrimitive()) {
			reporter.report(expectedPrimtiveProblem(name, element));
			return null;
		}
		try {
			return element.getAsJsonPrimitive().getAsNumber();
		} catch (NumberFormatException _) {
			reporter.report(() -> "Expected valid number for " + name + ", got " + element.getAsString() + " instead.");
			return null;
		}
	}

	private static ProblemReporter.Problem expectedPrimtiveProblem(String name, JsonElement element) {
		return () -> "Expected json primitive for " + name + ", got " + element.getClass().getSimpleName() + " instead.";
	}
}
