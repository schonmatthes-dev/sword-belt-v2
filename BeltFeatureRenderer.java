package com.swordbelt.client;

import com.swordbelt.SwordBeltConfig;
import net.minecraft.client.model.PlayerEntityModel;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class BeltFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	public BeltFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
					   PlayerEntityRenderState state, float limbAngle, float limbDistance) {
		SwordBeltConfig c = SwordBeltConfig.get();
		for (ItemRenderState item : ((BeltStateAccess) state).swordbelt$items()) {
			if (item.isEmpty()) continue;
			matrices.push();
			// dem Körper folgen (inkl. Schleichen)
			this.getContextModel().body.applyTransform(matrices);
			matrices.translate(c.offsetX / 16f, c.offsetY / 16f, c.offsetZ / 16f);
			// Y/X-Spiegelung des Entity-Renderers aufheben
			matrices.scale(-1f, -1f, 1f);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotY));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.rotX));
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.rotZ));
			matrices.scale(c.scale, c.scale, c.scale);
			item.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
			matrices.pop();
		}
	}
}
