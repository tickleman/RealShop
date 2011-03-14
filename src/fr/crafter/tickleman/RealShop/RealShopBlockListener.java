package fr.crafter.tickleman.RealShop;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

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
		Player player = event.getPlayer();
		if (player != null) {
			// exit previous chest
			if (plugin.playersInChestCounter > 0) {
				plugin.exitChest(player);
			}
			// only if block is a chest
			Block block = event.getBlock();
			if (block.getType().equals(Material.CHEST)) {
				// display shop prices
				RealShop shop = plugin.shopsFile.shopAt(block);
				if (shop != null) {
					plugin.shopPricesInfos(player, block);
					if (shop.player != player.getName()) {
						// can't damage a shop that is not yours
						event.setCancelled(true);
					}
				}
				// select chest
				plugin.selectChest(player, block, false);
			}
		}
	}

	//------------------------------------------------------------------------------- onBlockInteract
	public void onBlockInteract(BlockInteractEvent event)
	{
		// works only with players
		if (event.getEntity() instanceof Player) {
			Block block = event.getBlock();
			if (block.getType().equals(Material.CHEST)) {
				// exit previous chest
				Player player = (Player)event.getEntity();
				if (plugin.playersInChestCounter > 0) {
					plugin.exitChest(player);
				}
				// select chest
				plugin.selectChest(player, block, true);
				// only if chest block is a shop
				RealShop shop = plugin.shopsFile.shopAt(block);
				if (shop != null) {
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
					if (shop.opened) {
						// enter chest
						if (!plugin.enterChest(player, block)) {
							event.setCancelled(true);
						}
					} else {
						player.sendMessage(
							RealColor.cancel
							+ plugin.lang.tr("+owner's shop +name is closed")
							.replace("+owner", RealColor.player + shop.player + RealColor.cancel)
							.replace("+name", RealColor.shop + shop.name + RealColor.cancel)
							.replace("  ", " ")
						);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	//--------------------------------------------------------------------------------- onBlockPlaced
	public void onBlockPlace(BlockPlaceEvent event)
	{
		// exit previous chest
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}
