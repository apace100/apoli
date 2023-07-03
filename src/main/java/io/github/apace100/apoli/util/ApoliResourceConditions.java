package io.github.apace100.apoli.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Set;

public class ApoliResourceConditions {

	public static final Identifier ANY_NAMESPACE_LOADED = Apoli.identifier("any_namespace_loaded");

	public static final Identifier ALL_NAMESPACES_LOADED = Apoli.identifier("all_namespaces_loaded");

	public static boolean namespacesLoaded(JsonObject jsonObject, Set<String> namespaces, boolean and) {

		JsonArray jsonArray = JsonHelper.getArray(jsonObject, "namespaces");
		for (JsonElement jsonElement : jsonArray) {
			if (jsonElement.isJsonPrimitive()) {
				if (namespaces.contains(jsonElement.getAsString()) != and) {
					return !and;
				}
			} else {
				throw new JsonParseException("Invalid " + jsonElement + " entry: expected a JSON string!");
			}
		}

		return and;

	}

	public static boolean test(Identifier id, JsonObject jsonObject) {

		try {
			JsonArray conditions = JsonHelper.getArray(jsonObject, ResourceConditions.CONDITIONS_KEY, null);
			if (conditions == null) {
				return true;
			} else {
				return ResourceConditions.conditionsMatch(conditions, true);
			}
		} catch (RuntimeException e) {
			Apoli.LOGGER.error("There was a problem parsing the resource condition(s) of power file " + id + " (skipping): " + e.getMessage());
			return false;
		}

	}

}
