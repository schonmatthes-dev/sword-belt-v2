package com.swordbelt;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import java.util.List;

public class SwordFilter {
	public static boolean isSword(ItemStack s) {
		return !s.isEmpty() && s.isIn(ItemTags.SWORDS);
	}

	/** Rang des Schwerts: 0..6 = Vanilla-Stufe, -1 = Schwert aus anderer Mod. */
	public static int tierOf(ItemStack s) {
		Identifier id = Registries.ITEM.getId(s.getItem());
		if (id.getNamespace().equals("minecraft")) {
			for (int i = 0; i < SwordBeltConfig.TIERS.size(); i++) {
				if (id.getPath().equals(SwordBeltConfig.TIERS.get(i) + "_sword")) return i;
			}
		}
		return -1;
	}

	/** Bestes Schwert, das laut Config angezeigt werden darf. */
	public static ItemStack pickBest(List<ItemStack> candidates, SwordBeltConfig cfg) {
		ItemStack best = ItemStack.EMPTY;
		int bestRank = -2;
		for (ItemStack s : candidates) {
			if (!isSword(s)) continue;
			int tier = tierOf(s);
			boolean allowed = tier >= 0
					? cfg.swords.getOrDefault(SwordBeltConfig.TIERS.get(tier), true)
					: cfg.showOtherSwords;
			if (allowed && tier > bestRank) {
				best = s;
				bestRank = tier;
			}
		}
		return best;
	}
}
