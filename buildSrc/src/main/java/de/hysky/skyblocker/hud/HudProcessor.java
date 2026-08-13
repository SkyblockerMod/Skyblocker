package de.hysky.skyblocker.hud;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hysky.skyblocker.Processor;

public class HudProcessor {

	public static void apply(ClassFile context, Map<Path, ClassModel> classes) {
		long start = System.currentTimeMillis();
		Map<ClassDesc, Integer> registeredHudWidgets = new HashMap<>();

		// Read all classes for HudWidgets
		for (Map.Entry<Path, ClassModel> entry : classes.entrySet()) {
			HudReader.readClass(entry.getValue(), registeredHudWidgets);
		}

		// Sort by priority
		List<ClassDesc> widgetClasses = new ArrayList<>(registeredHudWidgets.keySet());
		widgetClasses.sort(Comparator.comparingInt(registeredHudWidgets::get));

		// Inject Hud Widget initializers
		try {
			Path widgetsManagerClassFile = Objects.requireNonNull(Processor.findClass("WidgetManager.class"), "WidgetManager class wasn't found :(").toPath();
			byte[] classBytes = HudInjector.transformClass(context, context.parse(widgetsManagerClassFile), widgetClasses);

			Processor.writeClass(widgetsManagerClassFile, classBytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		System.out.println("Injecting widget instancing took: " + (System.currentTimeMillis() - start) + "ms");
	}
}
