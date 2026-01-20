package de.hysky.skyblocker.object;

public enum ObjectMethodType {
	EQUALS("equals"),
	HASH_CODE("hashCode"),
	TO_STRING("toString");

	public final String methodName;

	ObjectMethodType(String methodName) {
		this.methodName = methodName;
	}
}
