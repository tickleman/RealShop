package fr.crafter.tickleman.RealShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
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

	//------------------------------------------------------------------------------ onPlayerDropItem
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

	//---------------------------------------------------------------------------------- onPlayerMove
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			Player player = event.getPlayer();
			RealInChestState inChestInfo = plugin.inChestStates.get(player.getName());
			// shop output detection : in chest + more than 5 seconds in shop + moved 2 blocks or more
			if (
				(inChestInfo != null)
				&& inChestInfo.inChest
				&& (
					(Math.abs(player.getLocation().getX() - inChestInfo.lastX) >= 2)
					|| (Math.abs(player.getLocation().getZ()- inChestInfo.lastZ) >= 2)
				)
			) {
				if (RealTime.worldToRealTime(player.getWorld()) < (inChestInfo.enterTime + 5)) {
					inChestInfo.lastX = Math.round(player.getLocation().getX());
					inChestInfo.lastZ = Math.round(player.getLocation().getZ());
				} else {
					plugin.exitChest(player);
				}
			}
		}
	}

	//---------------------------------------------------------------------------------- onPlayerQuit
	public void onPlayerQuit(PlayerEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}
