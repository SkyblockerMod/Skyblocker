package de.hysky.skyblocker;

/**
 * Record representing some reference to a method.
 *
 * @param className  The name of the class the method is in. See {@link ClassReference#className()}.
 * @param methodName The name of the method (e.g. 'main').
 * @param descriptor The descriptor of the method (e.g. '(IJ)Z', '(Lnet/java/lang/Long;IZ)Ljava/lang/String;').
 * @param itf        Whether this method belongs to an interface.
 */
public record MethodReference(String className, String methodName, String descriptor, boolean itf) {

	public boolean matches(String methodName, String descriptor) {
		return this.methodName.equals(methodName) && this.descriptor.equals(descriptor);
	}
}
