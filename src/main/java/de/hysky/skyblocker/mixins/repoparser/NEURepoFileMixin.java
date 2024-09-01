package de.hysky.skyblocker.mixins.repoparser;

import java.io.Reader;
import java.lang.reflect.Type;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.gson.Gson;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.moulberry.repo.NEURepoFile;

@Mixin(value = NEURepoFile.class, remap = false)
public class NEURepoFileMixin {

	@WrapOperation(method = "json(Lcom/google/gson/reflect/TypeToken;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/reflect/Type;)Ljava/lang/Object;"))
	private <T> T skyblocker$closeReaderTT(Gson gson, Reader reader, Type type, Operation<T> operation) throws Exception {
		try (reader) {
			return operation.call(gson, reader, type);
		} catch (Exception e) {
			throw e;
		}
	}

	@WrapOperation(method = "json(Ljava/lang/Class;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
	private <T> T skyblocker$closeReaderC(Gson gson, Reader reader, Class<T> clazz, Operation<T> operation) throws Exception {
		try (reader) {
			return operation.call(gson, reader, clazz);
		} catch (Exception e) {
			throw e;
		}
	}
}
