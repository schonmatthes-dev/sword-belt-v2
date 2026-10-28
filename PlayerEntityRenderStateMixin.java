package com.swordbelt.mixin;

import com.swordbelt.client.BeltStateAccess;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements BeltStateAccess {
	@Unique
	private final List<ItemRenderState> swordbelt$items = new ArrayList<>();

	@Override
	public List<ItemRenderState> swordbelt$items() {
		return swordbelt$items;
	}
}
