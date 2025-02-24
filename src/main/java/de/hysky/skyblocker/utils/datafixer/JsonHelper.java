package de.hysky.skyblocker.utils.datafixer;

import java.util.Optional;
import java.util.OptionalInt;

import com.google.gson.JsonObject;

import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;

/**
 * Helper methods to assist in retrieving values nested in JSON objects.
 *
 * All methods are fully null safe, whether it be from passing a {@code null} root object or from encountering a nonexistent or null object/value.
 *
 * @author AzureAaron
 * @see <a href="https://github.com/AzureAaron/aaron-mod/blob/1.20/src/main/java/net/azureaaron/mod/utils/JsonHelper.java">Aaron's Mod's JSON Helper</a>
 */
@MethodsReturnNonnullByDefault
public class JsonHelper {

	public static OptionalInt getInt(JsonObject root, String path) {
		//If root is null
		if (root == null) return OptionalInt.empty();

		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return root.has(path) && !root.get(path).isJsonNull() ? OptionalInt.of(root.get(path).getAsInt()) : OptionalInt.empty();
		}

		String[] split = path.split("\\.");
		String propertyName = split[split.length - 1];
		String[] objects2Traverse = new String[split.length - 1];

		//Get the traversal path
		System.arraycopy(split, 0, objects2Traverse, 0, split.length - 1);

		JsonObject currentLevel = root;

		for (String objectName : objects2Traverse) {
			if (currentLevel.has(objectName) && !currentLevel.get(objectName).isJsonNull()) {
				currentLevel = currentLevel.getAsJsonObject(objectName);
			} else {
				return OptionalInt.empty();
			}
		}

		return currentLevel.has(propertyName) && !currentLevel.get(propertyName).isJsonNull() ? OptionalInt.of(currentLevel.get(propertyName).getAsInt()) : OptionalInt.empty();
	}

	public static Optional<Boolean> getBoolean(JsonObject root, String path) {
		//If root is null
		if (root == null) return Optional.empty();

		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return root.has(path) && !root.get(path).isJsonNull() ? Optional.of(root.get(path).getAsBoolean()) : Optional.empty();
		}

		String[] split = path.split("\\.");
		String propertyName = split[split.length - 1];
		String[] objects2Traverse = new String[split.length - 1];

		//Get the traversal path
		System.arraycopy(split, 0, objects2Traverse, 0, split.length - 1);

		JsonObject currentLevel = root;

		for (String objectName : objects2Traverse) {
			if (currentLevel.has(objectName) && !currentLevel.get(objectName).isJsonNull()) {
				currentLevel = currentLevel.getAsJsonObject(objectName);
			} else {
				return Optional.empty();
			}
		}

		return currentLevel.has(propertyName) && !currentLevel.get(propertyName).isJsonNull() ? Optional.of(currentLevel.get(propertyName).getAsBoolean()) : Optional.empty();
	}

	public static Optional<String> getString(JsonObject root, String path) {
		//If root is null
		if (root == null) return Optional.empty();

		//Fast path for if we just want the field itself
		if (!path.contains(".")) {
			return root.has(path) && !root.get(path).isJsonNull() ? Optional.of(root.get(path).getAsString()) : Optional.empty();
		}

		String[] split = path.split("\\.");
		String propertyName = split[split.length - 1];
		String[] objects2Traverse = new String[split.length - 1];

		//Get the traversal path
		System.arraycopy(split, 0, objects2Traverse, 0, split.length - 1);

		JsonObject currentLevel = root;

		for (String objectName : objects2Traverse) {
			if (currentLevel.has(objectName) && !currentLevel.get(objectName).isJsonNull()) {
				currentLevel = currentLevel.getAsJsonObject(objectName);
			} else {
				return Optional.empty();
			}
		}

		return currentLevel.has(propertyName) && !currentLevel.get(propertyName).isJsonNull() ? Optional.of(currentLevel.get(propertyName).getAsString()) : Optional.empty();
	}
}
