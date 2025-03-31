package ovh.fedox.flocklobby.listener;


import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mineacademy.fo.annotation.AutoRegister;

/**
 * ProtectionListener.java - Protects the server from griefing
 * <p>
 * Created on 3/31/2025 at 7:36 AM by Fedox.
 * Copyright © 2025 Fedox. All rights reserved.
 */

@AutoRegister
public final class ProtectionListener implements Listener {

	/**
	 * Cancel all entity damage
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onEntity(EntityDamageEvent e) {
		e.setCancelled(true);
	}

	/**
	 * Cancel all entity damage by entity
	 *
	 * @param e the event
	 */
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		e.setCancelled(true);
	}

	/**
	 * Cancel all food level changes
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onFood(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	/**
	 * Cancel all entity damage by block
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onEntityBlock(EntityDamageByBlockEvent e) {
		e.setCancelled(true);
	}

	/**
	 * Cancel all block break events
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE || !e.getPlayer().hasPermission("flock.team"));
	}

	/**
	 * Cancel all block break events
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		e.setCancelled(e.getPlayer().getGameMode() != GameMode.CREATIVE || !e.getPlayer().hasPermission("flock.team"));
	}

	/**
	 * Cancel all item drops
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		e.setCancelled(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE));
	}

	/**
	 * Cancel some block interactions
	 *
	 * @param e the event
	 */
	@EventHandler
	public void onWheatInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock().getType().equals(Material.LEGACY_SOIL)) {
			e.setCancelled(true);
		}
	}

	/**
	 * Cancel all mob spawns
	 *
	 * @param event the event
	 */
	@EventHandler
	public void onMobSpawn(CreatureSpawnEvent event) {
		event.setCancelled(true);
	}
}
