package com.capsmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/** The Caps menu: pick a built-in cap or one of your own downloaded caps. */
public class CapsScreen extends Screen {
	private static final int PER_PAGE = 6;

	private final Screen parent;
	private int page = 0;

	public CapsScreen(Screen parent) {
		super(Text.translatable("screen.capsmod.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		List<CapManager.Cap> caps = CapManager.getCaps();
		int cx = this.width / 2;
		int top = 40;

		int pages = Math.max(1, (caps.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.max(0, Math.min(page, pages - 1));
		int start = page * PER_PAGE;

		for (int i = 0; i < PER_PAGE && start + i < caps.size(); i++) {
			CapManager.Cap cap = caps.get(start + i);
			boolean equipped = cap.name().equals(CapManager.getSelected());
			String prefix = equipped ? "[x] " : "[  ] ";
			String suffix = cap.builtin() ? "" : "  (custom)";
			this.addDrawableChild(
				ButtonWidget.builder(Text.literal(prefix + cap.name() + suffix), button -> {
					CapManager.select(cap.name());
					this.clearAndInit();
				}).dimensions(cx - 100, top + i * 24, 200, 20).build()
			);
		}

		int y = top + PER_PAGE * 24 + 6;

		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.prev"), button -> {
				page--;
				this.clearAndInit();
			}).dimensions(cx - 100, y, 98, 20).build()
		).active = page > 0;

		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.next"), button -> {
				page++;
				this.clearAndInit();
			}).dimensions(cx + 2, y, 98, 20).build()
		).active = page < pages - 1;

		y += 24;
		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.no_cap"), button -> {
				CapManager.select("");
				this.clearAndInit();
			}).dimensions(cx - 100, y, 98, 20).build()
		);
		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.open_folder"), button -> {
				try {
					Files.createDirectories(CapManager.getCapsDir());
				} catch (IOException ignored) {
				}
				Util.getOperatingSystem().open(CapManager.getCapsDir().toFile());
			}).dimensions(cx + 2, y, 98, 20).build()
		);

		y += 24;
		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.reload"), button -> {
				CapManager.reload();
				this.clearAndInit();
			}).dimensions(cx - 100, y, 98, 20).build()
		);
		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("screen.capsmod.done"), button -> this.close())
				.dimensions(cx + 2, y, 98, 20).build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int cx = this.width / 2;
		int pages = Math.max(1, (CapManager.getCaps().size() + PER_PAGE - 1) / PER_PAGE);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 15, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.literal((page + 1) + " / " + pages), cx, 28, 0xFFAAAAAA);

		int hintY = this.height - 36;
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.translatable("screen.capsmod.hint"), cx, hintY, 0xFFAAAAAA);
		context.drawCenteredTextWithShadow(this.textRenderer,
			Text.translatable("screen.capsmod.hint2"), cx, hintY + 12, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}
}
