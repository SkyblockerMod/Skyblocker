package me.xmrvizzy.skyblocker.config;

import java.awt.Color;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.isxander.yacl3.config.GsonConfigInstance.ColorTypeAdapter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

class ConfigSerializer {
	static final Gson INSTANCE = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
			.registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
			.registerTypeHierarchyAdapter(Identifier.class, new Identifier.Serializer())
			.registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
			.serializeNulls()
			.setPrettyPrinting()
			.create();
}
