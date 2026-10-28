package com.capsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Caps Mod - client-side only. Nothing is sent over the network, so it works
 * in singleplayer (offline) and on any server (online).
 */
public class CapsModClient implements ClientModInitializer {
	public static final String MOD_ID = "capsmod";

	public static KeyBinding openKey;

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void onInitializeClient() {
		CapManager.loadConfig();

		// Keyboard shortcut (default: K). Can be changed in Options > Controls.
		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
		openKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.capsmod.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, category)
		);

		// Cap model + renderer
		EntityModelLayerRegistry.registerModelLayer(CapFeatureRenderer.LAYER, CapFeatureRenderer::createModelData);
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerEntityRenderer) {
				((LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper) registrationHelper)
					.register(new CapFeatureRenderer((PlayerEntityRenderer) entityRenderer, context));
			}
		});

		// Open the GUI when the key is pressed
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new CapsScreen(null));
				}
			}
		});
	}
}
