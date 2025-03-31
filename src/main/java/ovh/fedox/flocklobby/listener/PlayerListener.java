package ovh.fedox.flocklobby.listener;


import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.annotation.AutoRegister;
import org.mineacademy.fo.remain.CompSound;
import ovh.fedox.flocklobby.settings.Setting;
import ovh.fedox.flocklobby.util.LocationUtil;

import java.util.Calendar;

/**
 * PlayerListener.java - Handles player events
 * <p>
 * Created on 3/31/2025 at 7:23 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@AutoRegister
public final class PlayerListener implements Listener {

	/**
	 * Set player's game mode to Adventure, set health to 20, set level to current year, set exp to 0, add blindness effect, teleport player to spawn, play firework sound and spawn firework
	 *
	 * @param event PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		int year = Calendar.getInstance().get(Calendar.YEAR);

		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealthScale(20);
		player.setLevel(year);
		player.setExp(0);
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 35, 1, false, false));

		Common.runLater(() -> {
			player.teleport(LocationUtil.stringToLocation(Setting.SPAWN_LOCATION));
			CompSound.ENTITY_FIREWORK_ROCKET_TWINKLE.play(player, 0.4f, 1.5f);
			CompSound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR.play(player, 0.4f, 1.5f);

			Common.runLater(10, () -> {
				Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);

				FireworkMeta meta = firework.getFireworkMeta();
				meta.addEffect(FireworkEffect.builder().withColor(Color.LIME).withFade(Color.GREEN).trail(true).build());
				meta.setPower(1);

				firework.setFireworkMeta(meta);
			});

		});
	}

	/**
	 * Cancel right-clicking on ArmorStands
	 *
	 * @param e PlayerInteractAtEntityEvent
	 */
	@EventHandler
	public void onManipulate(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof ArmorStand) {
			e.setCancelled(true);
		}
	}

	/**
	 * Teleport player back to spawn if they fall below the minimum Y value
	 *
	 * @param event PlayerMoveEvent
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) { // Yes, i know this Event is heavy, but it's fine for now
		Player player = event.getPlayer();

		if (player.getLocation().getY() < Setting.MIN_Y) {
			player.teleport(LocationUtil.stringToLocation(Setting.SPAWN_LOCATION));
		}
	}

}
