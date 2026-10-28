package com.swordbelt.client;

import com.swordbelt.SwordBeltConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SwordBeltScreen extends Screen {
	private final Screen parent;

	public SwordBeltScreen(Screen parent) {
		super(Text.literal("Sword Belt - Einstellungen"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		SwordBeltConfig c = SwordBeltConfig.get();
		int left = width / 2 - 205, right = width / 2 + 5;

		addDrawableChild(new TextWidget(0, 12, width, 9, title, textRenderer).alignCenter());

		int y = 32;
		toggle(left, y, "Mod aktiv", c.enabled, v -> c.enabled = v);
		toggle(right, y, "Bei mir selbst", c.showOnSelf, v -> c.showOnSelf = v);
		y += 24;
		toggle(left, y, "Bei anderen Spielern", c.showOnOthers, v -> c.showOnOthers = v);
		toggle(right, y, "Schwerter aus anderen Mods", c.showOtherSwords, v -> c.showOtherSwords = v);

		y += 36;
		for (int i = 0; i < SwordBeltConfig.TIERS.size(); i++) {
			String id = SwordBeltConfig.TIERS.get(i);
			int x = (i % 2 == 0) ? left : right;
			toggle(x, y + (i / 2) * 24, SwordBeltConfig.TIER_NAMES.get(i) + "schwert",
					c.swords.getOrDefault(id, true), v -> c.swords.put(id, v));
		}

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 - 100, height - 28, 200, 20).build());
	}

	private void toggle(int x, int y, String label, boolean initial, Consumer<Boolean> setter) {
		addDrawableChild(CyclingButtonWidget.onOffBuilder(initial)
				.build(x, y, 200, 20, Text.literal(label), (button, value) -> setter.accept(value)));
	}

	@Override
	public void close() {
		SwordBeltConfig.save();
		client.setScreen(parent);
	}
}
