package com.bukkit.tickleman.RealShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

//########################################################################## RealShopPlayerListener
/**
 * Handle events for all Player related events
 * @author tickleman
 */
public class RealShopPlayerListener extends PlayerListener
{

	private final RealShopPlugin plugin;

	//---------------------------------------------------------------------- RealShopPlayerListener
	public RealShopPlayerListener(RealShopPlugin instance)
	{
		plugin = instance;
	}

	//----------------------------------------------------------------------------- onPlayerCommand
	public void onPlayerCommand(PlayerChatEvent event)
	{
		String[] cmd = event.getMessage().split(" ");
		String command = ((cmd.length > 0) ? cmd[0].toLowerCase() : "");
		if (command.equals("/shop")) {
			// /shop
			Player player = event.getPlayer();
			String param = ((cmd.length > 1) ? cmd[1].toLowerCase() : "");
			if (player.isOp()) {
				// operator events
				event.setCancelled(true);
				if (param.equals("check")) {
					// /shop check : display info about RealShop
					plugin.pluginInfos(player);
				} else if (param.equals("prices")) {
					// /shop log : show transactions log (summary) of the day
					plugin.pluginInfosPrices(player);
				} else if (param.equals("simul")) {
					// /shop simul : simulate new prices using last prices and transactions log
					plugin.marketFile.dailyPricesCalculation(plugin.dailyLog, true);
				} else if (param.equals("daily")) {
					// /shop daily : calculate and save new prices using last prices and transactions log
					plugin.marketFile.dailyPricesCalculation(plugin.dailyLog);
				} else if (param.equals("log")) {
					plugin.pluginInfosDailyLog(player);
				} else if (param.equals("")) {
					// /shop without parameter : simply create/remove a shop
					String playerName = player.getName();
					if (plugin.shopCommand.get(playerName) == null) {
						player.sendMessage(plugin.lang.tr("Click on the chest-shop to activate/desactivate"));
						plugin.log.info("[PLAYER_COMMAND] " + playerName + ": /shop");
						plugin.shopCommand.put(playerName, "/shop");
					} else {
						player.sendMessage(plugin.lang.tr("Chest-shop activation/desactivation cancelled"));
						plugin.shopCommand.remove(playerName);
					}
				} else {
					event.setCancelled(false);
				}
			}
		}
	}

	//---------------------------------------------------------------------------- onPlayerDropItem
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

	//-------------------------------------------------------------------------------- onPlayerMove
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			Player player = event.getPlayer();
			RealInChestState inChestInfo = plugin.inChestStates.get(player.getName());
			if (
					(inChestInfo != null)
					&& inChestInfo.inChest && (
							(Math.round(player.getLocation().getX()) != inChestInfo.lastX)
							|| (Math.round(player.getLocation().getZ()) != inChestInfo.lastZ)
					)
			) {
				plugin.exitChest(player);
			}
		}
	}

	//-------------------------------------------------------------------------------- onPlayerQuit
	public void onPlayerQuit(PlayerEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}

/*

##### COMMAND EXAMPLES TO GET INFO / ADD / REMOVE ITEMS 

System.out.println("command is " + command);
// /i DEBUG : INFO
if (
	(command.charAt(0) == '/')
	&& (command.charAt(1) == 'i')
) {
	RealInventory inventory = RealInventory.create(event.getPlayer());
	System.out.println(inventory.toString());
	event.setCancelled(true);
}
// /a <typeId> <amount> DEBUG : ADD ITEM	
if (
	(command.charAt(0) == '/')
	&& (command.charAt(1) == 'a')
) {
	int typeId = Integer.parseInt(cmd.nextToken());
	int amount = Integer.parseInt(cmd.nextToken());
	if ((typeId > 0) && (amount > 0)) {
		System.out.println(
			"add item " + typeId + " x" + amount + " => "
			+ RealInventory.create(event.getPlayer()).add(typeId, amount)
		);
		
	}
	event.setCancelled(true);
}
// /r <typeId> <amount> DEBUG : REMOVE ITEM
if (
	(command.charAt(0) == '/')
	&& (command.charAt(1) == 'r')
) {
	int typeId = Integer.parseInt(cmd.nextToken());
	int amount = Integer.parseInt(cmd.nextToken());
	if ((typeId > 0) && (amount > 0)) {
		
		System.out.println(
			"remove item " + typeId + " x" + amount + " => "
			+ RealInventory.create(event.getPlayer()).remove(typeId, amount)
		);
	}
	event.setCancelled(true);
}
*/
