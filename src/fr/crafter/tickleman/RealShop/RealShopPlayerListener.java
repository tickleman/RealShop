package fr.crafter.tickleman.RealShop;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.crafter.tickleman.RealPlugin.RealColor;
import fr.crafter.tickleman.RealPlugin.RealTime;

//########################################################################## RealShopPlayerListener
/**
 * Handle events for all Player related events
 * @author tickleman
 */
public class RealShopPlayerListener extends PlayerListener
{

	long nextMoveCheck = 0;

	private final RealShopPlugin plugin;

	//------------------------------------------------------------------------ RealShopPlayerListener
	public RealShopPlayerListener(RealShopPlugin instance)
	{
		plugin = instance;
		
	}

	//------------------------------------------------------------------------------- onInventoryOpen
	@Override
	public void onInventoryOpen(PlayerInventoryEvent event)
	{
		// craftbukkit 440-740 : cratbukkit seems to never call this, sorry !
		System.out.println("onInventoryOpen");
		// exit previous chest
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer(), false);
		}
	}

	//------------------------------------------------------------------------------ onPlayerDropItem
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			// players that are into a chest-shop should not be able to throw items !
			Player player = event.getPlayer();
			RealInChestState inChestInfo = plugin.inChestStates.get(player.getName());
			if (
				(inChestInfo != null)
				&& inChestInfo.inChest
			) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(
					RealColor.cancel
					+ plugin.lang.tr("Dropping items when you are in a shop is prohibited")
				);
			}
		}
	}

	//------------------------------------------------------------------------------ onPlayerInteract
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		// exit previous chest
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer(), false);
		}
		// works only with players
    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
    	Block block = event.getClickedBlock();
			if (block.getType().equals(Material.CHEST)) {
				// exit previous chest
				Player player = event.getPlayer();
				if (plugin.playersInChestCounter > 0) {
					plugin.exitChest(player, false);
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

	//---------------------------------------------------------------------------------- onPlayerMove
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			Player player = event.getPlayer();
			if (System.currentTimeMillis() >= nextMoveCheck) {
				RealInChestState inChestInfo = plugin.inChestStates.get(player.getName());
				// shop output detection : in chest + more than 3 seconds in shop + moved 1 blocks or more
				// (last was 5 seconds and 2 blocks)
				if (
					(inChestInfo != null)
					&& inChestInfo.inChest
					&& (
						(Math.abs(player.getLocation().getX() - inChestInfo.lastX) >= 1)
						|| (Math.abs(player.getLocation().getZ()- inChestInfo.lastZ) >= 1)
					)
				) {
					if (RealTime.worldToRealTime(player.getWorld()) < (inChestInfo.enterTime + 3)) {
						inChestInfo.lastX = Math.round(player.getLocation().getX());
						inChestInfo.lastZ = Math.round(player.getLocation().getZ());
					} else {
						plugin.exitChest(player, false);
					}
				}
				nextMoveCheck = System.currentTimeMillis() + 1000; 
			}
		}
	}

	//---------------------------------------------------------------------------------- onPlayerQuit
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.log.warning(
				"Player " + event.getPlayer().getName() + " logs off being into a chest-shop !"
			);
			plugin.exitChest(event.getPlayer(), true);
		}
	}

}
