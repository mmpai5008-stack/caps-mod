package com.capsmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Draws the equipped cap on top of the local player's head.
 * Texture layout: 64x32 (crown at 0,0 - brim at 0,16). See README.
 */
public class CapFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(Identifier.of(CapsModClient.MOD_ID, "cap"), "main");

	private final ModelPart cap;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public CapFeatureRenderer(FeatureRendererContext context, EntityRendererFactory.Context factoryContext) {
		super(context);
		this.cap = factoryContext.getEntityModels().getModelPart(LAYER).getChild("cap");
	}

	public static TexturedModelData createModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild(
			"cap",
			ModelPartBuilder.create()
				// crown: sits over the top of the head
				.uv(0, 0).cuboid(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F)
				// brim: sticks out of the front
				.uv(0, 16).cuboid(-4.5F, -6.0F, -8.5F, 9.0F, 1.0F, 4.0F),
			ModelTransform.NONE
		);
		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
					   PlayerEntityRenderState state, float limbAngle, float limbDistance) {
		if (state.invisible) return;

		// Client-side only: other players cannot see your cap, so we only draw it on you.
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || state.id != client.player.getId()) return;

		Identifier texture = CapManager.getSelectedTexture();
		if (texture == null) return;

		matrices.push();
		this.getContextModel().head.rotate(matrices); // follow head movement
		queue.submitModelPart(
			this.cap,
			matrices,
			RenderLayer.getEntityCutoutNoCull(texture),
			light,
			OverlayTexture.DEFAULT_UV,
			null
		);
		matrices.pop();
	}
}
