package com.capsmod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Keeps the list of caps (built-in + the user's downloaded .png files),
 * registers their textures and remembers which one is equipped.
 */
public final class CapManager {
	public record Cap(String name, Identifier texture, boolean builtin) {}

	private static final List<Cap> CAPS = new ArrayList<>();
	private static final int[][] BUILTIN = {
		// name is index-matched with BUILTIN_NAMES, color is ARGB
		{0xFFC62828}, {0xFF1565C0}, {0xFF2E7D32}, {0xFF212121}, {0xFFF5F5F5}, {0xFFF9A825}
	};
	private static final String[] BUILTIN_NAMES = {
		"Red Cap", "Blue Cap", "Green Cap", "Black Cap", "White Cap", "Yellow Cap"
	};

	private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("capsmod");
	private static final Path CAPS_DIR = DIR.resolve("caps");
	private static final Path CONFIG_FILE = DIR.resolve("capsmod.properties");

	private static String selected = "";
	private static boolean loaded = false;

	private CapManager() {}

	public static Path getCapsDir() {
		return CAPS_DIR;
	}

	public static List<Cap> getCaps() {
		ensureLoaded();
		return CAPS;
	}

	public static String getSelected() {
		return selected;
	}

	public static void select(String name) {
		selected = name == null ? "" : name;
		saveConfig();
	}

	/** Texture of the equipped cap, or null when no cap is equipped. */
	public static Identifier getSelectedTexture() {
		ensureLoaded();
		if (selected.isEmpty()) return null;
		for (Cap cap : CAPS) {
			if (cap.name().equals(selected)) return cap.texture();
		}
		return null;
	}

	public static void ensureLoaded() {
		if (!loaded) reload();
	}

	/** (Re)scans the caps folder and registers every texture. Must run on the render thread. */
	public static void reload() {
		TextureManager textures = MinecraftClient.getInstance().getTextureManager();

		for (Cap old : CAPS) {
			textures.destroyTexture(old.texture());
		}
		CAPS.clear();

		// Built-in caps (solid colors, generated in code, no files needed)
		for (int i = 0; i < BUILTIN_NAMES.length; i++) {
			try {
				NativeImage image = new NativeImage(64, 32, false);
				int color = BUILTIN[i][0];
				for (int x = 0; x < 64; x++) {
					for (int y = 0; y < 32; y++) {
						image.setColorArgb(x, y, shade(color, x, y));
					}
				}
				Identifier id = Identifier.of(CapsModClient.MOD_ID, "builtin/" + i);
				register(textures, id, "capsmod builtin " + i, image);
				CAPS.add(new Cap(BUILTIN_NAMES[i], id, true));
			} catch (Exception e) {
				System.err.println("[CapsMod] Could not create built-in cap " + BUILTIN_NAMES[i] + ": " + e);
			}
		}

		// The user's own caps: every .png in config/capsmod/caps/
		try {
			Files.createDirectories(CAPS_DIR);
			List<Path> files;
			try (Stream<Path> stream = Files.list(CAPS_DIR)) {
				files = stream
					.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
					.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
					.toList();
			}
			int index = 0;
			for (Path file : files) {
				String fileName = file.getFileName().toString();
				String display = fileName.substring(0, fileName.length() - 4);
				try (InputStream in = Files.newInputStream(file)) {
					NativeImage image = NativeImage.read(in);
					String safe = display.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
					Identifier id = Identifier.of(CapsModClient.MOD_ID, "custom/" + index + "_" + safe);
					register(textures, id, "capsmod custom " + fileName, image);
					CAPS.add(new Cap(display, id, false));
					index++;
				} catch (Exception e) {
					System.err.println("[CapsMod] Could not load cap " + fileName + ": " + e);
				}
			}
		} catch (IOException e) {
			System.err.println("[CapsMod] Could not read caps folder: " + e);
		}

		loaded = true;
	}

	private static void register(TextureManager textures, Identifier id, String debugName, NativeImage image) {
		textures.registerTexture(id, new NativeImageBackedTexture(() -> debugName, image));
	}

	/** Small shading so the built-in caps don't look completely flat. */
	private static int shade(int argb, int x, int y) {
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		float factor = 1.0F - ((x + y) % 4 == 0 ? 0.06F : 0.0F);
		r = Math.min(255, (int) (r * factor));
		g = Math.min(255, (int) (g * factor));
		b = Math.min(255, (int) (b * factor));
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public static void loadConfig() {
		try {
			Files.createDirectories(CAPS_DIR);
			if (Files.exists(CONFIG_FILE)) {
				Properties props = new Properties();
				try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
					props.load(in);
				}
				selected = props.getProperty("selected", "");
			}
		} catch (IOException e) {
			System.err.println("[CapsMod] Could not load config: " + e);
		}
	}

	private static void saveConfig() {
		try {
			Files.createDirectories(DIR);
			Properties props = new Properties();
			props.setProperty("selected", selected);
			try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
				props.store(out, "Caps Mod - equipped cap (empty = none)");
			}
		} catch (IOException e) {
			System.err.println("[CapsMod] Could not save config: " + e);
		}
	}
}
