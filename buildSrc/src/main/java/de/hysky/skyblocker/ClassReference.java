package de.hysky.skyblocker;

/**
 * Record representing a reference to some class.
 *
 * @param className  The class' name according to {@link Class#getName()} where '.' are replaced with '/' (e.g. 'de.hysky.skyblocker.SkyblockerMod').
 * @param descriptor The class' descriptor (e.g. 'Lde/hysky/skyblocker/SkyblockerMod;').
 */
public record ClassReference(String className, String descriptor) {

	public ClassReference(String className) {
		this(className, "L" + className + ";");
	}
}
