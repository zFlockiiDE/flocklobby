package ovh.fedox.flocklobby.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.mineacademy.fo.menu.Menu;
import org.mineacademy.fo.menu.button.Button;
import org.mineacademy.fo.menu.button.annotation.Position;
import org.mineacademy.fo.menu.model.ItemCreator;
import org.mineacademy.fo.remain.CompMaterial;
import ovh.fedox.flockapi.util.GuiUtil;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * PromptMenu.java - A prompt menu for choosing yes or no or custom options
 * <p>
 * Created on 3/24/2025 at 5:51 PM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

public class PromptMenu extends Menu {

	private final String NUMBER_ONE = "http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530";
	private final String NUMBER_TWO = "http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847";

	@Position(9 + 2)
	private final Button LEFT_BUTTON;

	@Position(9 + 4)
	private final Button INFORMATION;

	@Position(9 + 6)
	private final Button RIGHT_BUTTON;

	public PromptMenu(Consumer<Object> result, String[] information) {
		this(result, information, true, "&aJa", "&cNein");
	}

	public PromptMenu(Consumer<Object> result, String[] information, boolean isYesNo, String option1, String option2) {

		if (isYesNo) setTitle("Wähle eine Option");
		else setTitle("Wähle eine Option");

		setSize(9 * 3);

		LEFT_BUTTON = new Button() {
			@Override
			public void onClickedInMenu(Player player, Menu menu, ClickType click) {
				if (isYesNo) result.accept(true);
				else result.accept(1);
			}

			@Override
			public ItemStack getItem() {
				if (isYesNo) {
					return ItemCreator.of(CompMaterial.LIME_DYE).name("&aJa").make();
				} else {
					return ItemCreator.of(CompMaterial.PLAYER_HEAD)
							.skullUrl(NUMBER_ONE)
							.name(option1)
							.make();
				}
			}
		};

		INFORMATION = new Button() {
			@Override
			public void onClickedInMenu(Player player, Menu menu, ClickType click) {
			}

			@Override
			public ItemStack getItem() {
				return ItemCreator.of(CompMaterial.NETHER_STAR, "&fInformationen", information).glow(true).make();
			}
		};

		RIGHT_BUTTON = new Button() {
			@Override
			public void onClickedInMenu(Player player, Menu menu, ClickType click) {
				if (isYesNo) result.accept(false);
				else result.accept(2);
			}

			@Override
			public ItemStack getItem() {
				if (isYesNo) {
					return ItemCreator.of(CompMaterial.RED_DYE).name("&cNein").make();
				} else {
					return ItemCreator.of(CompMaterial.PLAYER_HEAD)
							.skullUrl(NUMBER_TWO)
							.name(option2)
							.make();
				}
			}
		};
	}

	@Override
	public ItemStack getItemAt(int slot) {
		int[] cornerSlots = GuiUtil.getCornerSlots(3);

		for (int cornerSlot : cornerSlots) {
			if (slot == cornerSlot) {
				return ItemCreator.of(CompMaterial.IRON_BARS).name("&7").make();
			}
		}

		if (slot != LEFT_BUTTON.getSlot() && slot != INFORMATION.getSlot() && slot != RIGHT_BUTTON.getSlot() && Arrays.stream(cornerSlots).noneMatch(i -> i == slot)) {
			int random = (int) (Math.random() * 2);

			if (random == 0) {
				return ItemCreator.of(CompMaterial.LIME_STAINED_GLASS_PANE).name("&7").make();
			} else {
				return ItemCreator.of(CompMaterial.GREEN_STAINED_GLASS_PANE).name("&7").make();
			}
		}

		return null;
	}

	@Override
	protected void onButtonClick(Player player, int slot, InventoryAction action, ClickType click, Button button) {
		player.closeInventory();
		super.onButtonClick(player, slot, action, click, button);
	}
}
