package fr.crafter.tickleman.RealShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.crafter.tickleman.RealPlugin.RealColor;
import fr.crafter.tickleman.RealPlugin.RealTime;

//########################################################################## RealShopPlayerListener
/**
 * Handle events for all Player related events
 * @author tickleman
 */
public class RealShopPlayerListener extends PlayerListener
{

	private final RealShopPlugin plugin;
	long nextMoveCheck = 0;

	//------------------------------------------------------------------------ RealShopPlayerListener
	public RealShopPlayerListener(RealShopPlugin instance)
	{
		plugin = instance;
		
	}

	//------------------------------------------------------------------------------- onInventoryOpen
	@Override
	public void onInventoryOpen(PlayerInventoryEvent event)
	{
		// craftbukkit 440-551 : does seem to never call this sorry !
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
	public void onPlayerQuit(PlayerEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.log.warning(
				"Player " + event.getPlayer().getName() + " logs off being into a chest-shop !"
			);
			plugin.exitChest(event.getPlayer(), true);
		}
	}

}
