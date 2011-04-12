package fr.crafter.tickleman.RealShop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
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
	@Override
	public void onBlockDamage(BlockDamageEvent event)
	{
		Player player = event.getPlayer();
		if (player != null) {
			// exit previous chest
			if (plugin.playersInChestCounter > 0) {
				plugin.exitChest(player, false);
			}
			// only if block is a chest
			Block block = event.getBlock();
			if (block.getType().equals(Material.CHEST)) {
				// display shop prices
				RealShop shop = plugin.shopsFile.shopAt(block);
				if (shop != null) {
					plugin.shopPricesInfos(player, block);
					if (!shop.player.equals(player.getName())) {
						// can't damage a shop that is not yours
						event.setCancelled(true);
					}
				}
				// select chest
				plugin.selectChest(player, block, false);
			}
		}
	}

	//--------------------------------------------------------------------------------- onBlockPlaced
	@Override
	public void onBlockPlace(BlockPlaceEvent event)
	{
		// exit previous chest
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer(), false);
		}
	}

}
