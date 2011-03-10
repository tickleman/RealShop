package fr.crafter.tickleman.RealShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.crafter.tickleman.RealPlugin.RealTime;

//########################################################################## RealShopPlayerListener
/**
 * Handle events for all Player related events
 * @author tickleman
 */
public class RealShopPlayerListener extends PlayerListener
{

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
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

	//------------------------------------------------------------------------------ onPlayerDropItem
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			// players that are into a shop-chest should not be able to throw items !
			Player player = event.getPlayer();
			RealInChestState inChestInfo = plugin.inChestStates.get(player.getName());
			if (
				(inChestInfo != null)
				&& inChestInfo.inChest
			) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("You're not allowed to drop items !!!");
			}
		}
	}

	//---------------------------------------------------------------------------------- onPlayerMove
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			Player player = event.getPlayer();
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
					plugin.exitChest(player);
				}
			}
		}
	}

	//---------------------------------------------------------------------------------- onPlayerQuit
	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}
