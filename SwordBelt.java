package com.swordbelt;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Server-Teil: Clients kennen das Inventar anderer Spieler nicht.
 * Darum meldet der Server (wenn die Mod dort installiert ist) die Schwerter an alle Spieler in der Nähe.
 */
public class SwordBelt implements ModInitializer {
	private static final Map<UUID, String> LAST = new ConcurrentHashMap<>();

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(BeltSyncPayload.ID, BeltSyncPayload.CODEC);

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 10 != 0) return;
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				List<ItemStack> swords = collect(p);
				String key = swords.stream().map(s -> Registries.ITEM.getId(s.getItem()).toString())
						.collect(Collectors.joining(","));
				if (!key.equals(LAST.put(p.getUuid(), key))) {
					BeltSyncPayload payload = new BeltSyncPayload(p.getId(), swords);
					for (ServerPlayerEntity t : PlayerLookup.tracking(p)) send(t, payload);
				}
			}
		});

		EntityTrackingEvents.START_TRACKING.register((entity, viewer) -> {
			if (entity instanceof ServerPlayerEntity sp) send(viewer, new BeltSyncPayload(sp.getId(), collect(sp)));
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST.remove(handler.getPlayer().getUuid()));
	}

	private static void send(ServerPlayerEntity to, BeltSyncPayload payload) {
		if (ServerPlayNetworking.canSend(to, BeltSyncPayload.ID)) ServerPlayNetworking.send(to, payload);
	}

	private static List<ItemStack> collect(ServerPlayerEntity p) {
		List<ItemStack> out = new ArrayList<>();
		PlayerInventory inv = p.getInventory();
		for (int i = 0; i < Math.min(36, inv.size()) && out.size() < 12; i++) {
			ItemStack s = inv.getStack(i);
			if (SwordFilter.isSword(s)) out.add(s.copyWithCount(1));
		}
		return out;
	}
}
