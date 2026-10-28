package com.swordbelt;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

/** Server -> Client: welche Schwerter ein Spieler im Inventar hat. */
public record BeltSyncPayload(int entityId, List<ItemStack> swords) implements CustomPayload {
	public static final Id<BeltSyncPayload> ID = new Id<>(Identifier.of("swordbelt", "sync"));
	public static final PacketCodec<RegistryByteBuf, BeltSyncPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, BeltSyncPayload::entityId,
			ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), BeltSyncPayload::swords,
			BeltSyncPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
