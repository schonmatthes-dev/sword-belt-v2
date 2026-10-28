package com.swordbelt.client;

import com.swordbelt.BeltSyncPayload;
import com.swordbelt.SwordBeltConfig;
import com.swordbelt.SwordFilter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SwordBeltClient implements ClientModInitializer {
	/** Vom Server gemeldete Schwerter anderer Spieler (EntityId -> Schwerter). */
	private static final Map<Integer, List<ItemStack>> SYNCED = new ConcurrentHashMap<>();
	private static KeyBinding openKey;

	@Override
	public void onInitializeClient() {
		SwordBeltConfig.load();

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
			if (renderer instanceof PlayerEntityRenderer pr) helper.register(new BeltFeatureRenderer(pr));
		});

		ClientPlayNetworking.registerGlobalReceiver(BeltSyncPayload.ID,
				(payload, context) -> SYNCED.put(payload.entityId(), payload.swords()));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SYNCED.clear());

		openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.swordbelt.open",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
				KeyBinding.Category.create(Identifier.of("swordbelt", "main"))));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.wasPressed()) client.setScreen(new SwordBeltScreen(client.currentScreen));
		});
	}

	/** Wird pro Frame für jeden Spieler aufgerufen und füllt die anzuzeigenden Schwerter. */
	public static void fill(AbstractClientPlayerEntity player, List<ItemRenderState> out) {
		SwordBeltConfig cfg = SwordBeltConfig.get();
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!cfg.enabled || player.isSpectator() || player.isInvisible()) return;

		boolean self = player == mc.player;
		List<ItemStack> candidates = new ArrayList<>();
		if (self) {
			if (!cfg.showOnSelf) return;
			PlayerInventory inv = player.getInventory();
			for (int i = 0; i < 36; i++) {
				if (i == inv.getSelectedSlot()) continue; // Schwert in der Hand nicht doppelt anzeigen
				candidates.add(inv.getStack(i));
			}
		} else {
			if (!cfg.showOnOthers) return;
			List<ItemStack> synced = SYNCED.get(player.getId());
			if (synced == null) return; // Server ohne Mod -> Inventar unbekannt
			candidates.addAll(synced);
			ItemStack held = player.getMainHandStack();
			for (int i = 0; i < candidates.size(); i++) {
				if (ItemStack.areItemsEqual(candidates.get(i), held)) {
					candidates.remove(i);
					break;
				}
			}
		}

		ItemStack best = SwordFilter.pickBest(candidates, cfg);
		if (best.isEmpty()) return;
		ItemRenderState state = new ItemRenderState();
		mc.getItemModelManager().updateForLivingEntity(state, best, ItemDisplayContext.FIXED, player);
		out.add(state);
	}
}
