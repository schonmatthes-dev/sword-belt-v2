package com.swordbelt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SwordBeltConfig {
	/** Reihenfolge = Rangfolge (rechts ist besser). */
	public static final List<String> TIERS = List.of("wooden", "stone", "copper", "iron", "golden", "diamond", "netherite");
	public static final List<String> TIER_NAMES = List.of("Holz", "Stein", "Kupfer", "Eisen", "Gold", "Diamant", "Netherit");

	public boolean enabled = true;
	public boolean showOnSelf = true;
	public boolean showOnOthers = true;
	public boolean showOtherSwords = true; // Schwerter aus anderen Mods
	public Map<String, Boolean> swords = new LinkedHashMap<>();

	// Feintuning der Position (Pixel bzw. Grad), relativ zum Körper
	public float offsetX = 3.0f;
	public float offsetY = 12.0f;
	public float offsetZ = -2.6f;
	public float rotX = 0f;
	public float rotY = 0f;
	public float rotZ = 135f;
	public float scale = 0.6f;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static SwordBeltConfig instance;

	public static SwordBeltConfig get() {
		if (instance == null) load();
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("swordbelt.json");
	}

	public static void load() {
		try {
			Path p = path();
			if (Files.exists(p)) instance = GSON.fromJson(Files.readString(p), SwordBeltConfig.class);
		} catch (Exception ignored) {
		}
		if (instance == null) instance = new SwordBeltConfig();
		if (instance.swords == null) instance.swords = new LinkedHashMap<>();
		for (String t : TIERS) instance.swords.putIfAbsent(t, true);
	}

	public static void save() {
		try {
			Files.writeString(path(), GSON.toJson(get()));
		} catch (Exception ignored) {
		}
	}
}
