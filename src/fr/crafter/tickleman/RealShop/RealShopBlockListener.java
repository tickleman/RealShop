package fr.crafter.tickleman.RealShop;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import fr.crafter.tickleman.RealPlugin.RealChest;
import fr.crafter.tickleman.RealPlugin.RealColor;

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
		Player player = event.getPlayer();
		if (player != null) {
			Block block = event.getBlock();
			if (plugin.shopCommand.size() > 0) {
				String playerName = player.getName();
				String command = plugin.shopCommand.get(playerName);
				if ((command != null) && command.substring(0, 5).equals("/shop")) {
					// create / remove chest-shop
					if (block.getType().equals(Material.CHEST)) {
						if (command.equals("/shop")) {
							event.setCancelled(true);
							plugin.registerBlockAsShop(player, block);
						} else if (plugin.shopsFile.isShop(block)) {
							event.setCancelled(true);
							Block neighbor = RealChest.scanForNeighborChest(
								block.getWorld(), block.getX(), block.getY(), block.getZ()
							);
							if (neighbor != null) plugin.log.debug("neighbor found");
							if (command.substring(0, 9).equals("/shop buy")) {
								plugin.shopAddBuy(player, block, command, false);
								if (neighbor != null) plugin.shopAddBuy(player, neighbor, command, true);
							} else if (command.substring(0, 10).equals("/shop sell")) {
								plugin.shopAddSell(player, block, command, false);
								if (neighbor != null) plugin.shopAddSell(player, neighbor, command, true);
							} else if (command.substring(0, 10).equals("/shop xbuy")) {
								plugin.shopExclBuy(player, block, command, false);
								if (neighbor != null) plugin.shopExclBuy(player, neighbor, command, true);
							} else if (command.substring(0, 11).equals("/shop xsell")) {
								plugin.shopExclSell(player, block, command, false);
								if (neighbor != null) plugin.shopExclSell(player, neighbor, command, true);
							} else if (command.substring(0, 10).equals("/shop give")) {
								plugin.shopGive(player, block, command, false);
								if (neighbor != null) plugin.shopGive(player, neighbor, command, true);
							}
						}
					} else {
						player.sendMessage(RealColor.cancel + plugin.lang.tr("Shop-chest command cancelled"));
					}
					plugin.shopCommand.remove(playerName);
				} else if (
					block.getType().equals(Material.CHEST)
					&& plugin.shopsFile.isShop(block)
				) {
					// protects shop chests from damages
					event.setCancelled(true);
				}
			} else if (
				block.getType().equals(Material.CHEST)
				&& plugin.shopsFile.isShop(block)
			) {
				// display shop current prices and protect from damages
				plugin.shopPricesInfos(player, block);
				event.setCancelled(true);
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
		if (plugin.shopsFile.shops.get(key) != null) {
			// calculate daily prices fluctuations
			if (plugin.config.dailyPricesCalculation.equals("true")) {
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
			if (!plugin.enterChest(player, block)) {
				event.setCancelled(true);
			}
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
