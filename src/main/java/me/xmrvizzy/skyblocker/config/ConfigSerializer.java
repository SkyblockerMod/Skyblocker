package me.xmrvizzy.skyblocker.config;

import java.awt.Color;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.isxander.yacl3.config.v2.impl.serializer.GsonConfigSerializer;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Note: The type hierarchy adapters are copied from YACL's code
 */
class ConfigSerializer {
	static final Gson INSTANCE = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
			.registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
			.registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
			.registerTypeHierarchyAdapter(Identifier.class, new Identifier.Serializer())
			.registerTypeHierarchyAdapter(Color.class, new GsonConfigSerializer.ColorTypeAdapter())
			.registerTypeHierarchyAdapter(Item.class, new GsonConfigSerializer.ItemTypeAdapter())
			.serializeNulls()
			.setPrettyPrinting()
			.create();
}
