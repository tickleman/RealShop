package com.bukkit.tickleman.RealShop;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

//########################################################################### RealShopBlockListener
/**
 * HelloWorld block listener
 * @author tickleman
 */
public class RealShopBlockListener extends BlockListener
{

	private final RealShopPlugin plugin;

	//------------------------------------------------------------------------- RealShopBlockListener
	public RealShopBlockListener(final RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	//--------------------------------------------------------------------------------- onBlockDamage
	public void onBlockDamage(BlockDamageEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
		if (plugin.shopCommand.size() > 0) {
			Player player = event.getPlayer();
			String playerName = player.getName();
			Block block = event.getBlock();
			if (plugin.shopCommand.get(playerName) == "/shop") {
				// create / remove shop-chest
				if (block.getType().equals(Material.CHEST)) {
					plugin.registerBlockAsShop(player, block);
				} else {
					player.sendMessage(plugin.lang.tr("Chest-shop activation/desactivation cancelled"));
				}
				plugin.shopCommand.remove(playerName);
			} else if (block.getType().equals(Material.CHEST)) {
				// protects shop chests from damages
				if (plugin.shopsFile.isShop(block)) {
					event.setCancelled(true);
				}
			}
		}
	}

	//------------------------------------------------------------------------------- onBlockInteract
	public void onBlockInteract(BlockInteractEvent event)
	{
		// works only with players
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		// works only with chests
		Block block = event.getBlock();
		if (!block.getType().equals(Material.CHEST)) {
			return;
		}
		// exit previous chest, then enter new one
		Player player = (Player) event.getEntity();
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(player);
		}
		// only if chest block is a shop
		String key = block.getWorld().getName()
			+ ";" + block.getX() + ";" + block.getY() + ";" + block.getZ();
		plugin.log.debug("looking for a shop at " + key);
		if (plugin.shopsFile.shops.get(key) != null) {
			// calculate daily prices fluctuations
			if (plugin.config.dailyPricesCalculation == "true") {
				World world = block.getWorld();
				String worldName = world.getName();
				Long worldTime = world.getFullTime();
				Long lastTime = plugin.lastDayTime.get(worldName);
				if (lastTime == null) {
					lastTime = (long)0;
				}
				if (worldTime > lastTime) {
					// notice that a world begins at 6000 (6:00am),
					// so we have to translate if we want to fix the prices at midnight
					long nextTime = ((worldTime + 6000) / 24000) * 24000 + 18000;
					plugin.lastDayTime.put(worldName, nextTime);
					// daily prices calculation
					plugin.marketFile.dailyPricesCalculation(plugin.dailyLog);
				}
			}
			// enter chest
			plugin.log.debug(player.getName() + " : this is a shop !");
			plugin.enterChest(player, block);
		}
	}

	//--------------------------------------------------------------------------------- onBlockPlaced
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}
