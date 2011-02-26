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

	//------------------------------------------------------------------------ RealShopPlayerListener
	public RealShopPlayerListener(RealShopPlugin instance)
	{
		plugin = instance;
	}

	//------------------------------------------------------------------------------- onPlayerCommand
	public void onPlayerCommand(PlayerChatEvent event)
	{
		String[] cmd = event.getMessage().split(" ");
		String command = ((cmd.length > 0) ? cmd[0].toLowerCase() : "");
		if (command.equals("/shop")) {
			event.setCancelled(true);
			// /shop
			Player player = event.getPlayer();
			String param = ((cmd.length > 1) ? cmd[1].toLowerCase() : "");
			// ALL PLAYERS
			if (param.equals("")) {
				// /shop without parameter : simply create/remove a shop
				String playerName = player.getName();
				if (plugin.shopCommand.get(playerName) == null) {
					plugin.log.info("[PLAYER_COMMAND] " + playerName + ": /shop");
					plugin.shopCommand.put(playerName, "/shop");
					player.sendMessage(plugin.lang.tr("Click on the shop-chest to activate/desactivate"));
				} else {
					plugin.shopCommand.remove(playerName);
					player.sendMessage(plugin.lang.tr("Shop-chest activation/desactivation cancelled"));
				}
			} else if (param.equals("buy")) {
				// /shop buy : give the list of item typeIds that players can buy into the shop
				String playerName = player.getName();
				String param2 = (cmd.length > 2) ? cmd[2] : "";
				plugin.shopCommand.put(playerName, "/shop " + param + " " + param2);
				player.sendMessage(plugin.lang.tr("Click on the shop-chest to add buy items"));
			} else if (param.equals("sell")) {
				// /shop sell : give the list of item typeIds that players can sell into the shop
				String playerName = player.getName();
				String param2 = (cmd.length > 2) ? cmd[2] : "";
				plugin.shopCommand.put(playerName, "/shop " + param + " " + param2);
				player.sendMessage(plugin.lang.tr("Click on the shop-chest to add sell items"));
			} else if (param.equals("xbuy")) {
				// /shop xbuy : give the list of item typeIds that players cannot buy into the shop
				String playerName = player.getName();
				String param2 = (cmd.length > 2) ? cmd[2] : "";
				plugin.shopCommand.put(playerName, "/shop " + param + " " + param2);
				player.sendMessage(plugin.lang.tr("Click on the shop-chest to exclude buy items"));
			} else if (param.equals("xsell")) {
				// /shop xsell : give the list of item typeIds that players cannot sell into the shop
				String playerName = player.getName();
				String param2 = (cmd.length > 2) ? cmd[2] : "";
				plugin.shopCommand.put(playerName, "/shop " + param + " " + param2);
				player.sendMessage(plugin.lang.tr("Click on the shop-chest to exclude sell items"));
			} else if (player.isOp()) {
				// OPERATORS ONLY
				if (param.equals("check")) {
					// /shop check : display info about RealShop
					plugin.pluginInfos(player);
				} else if (param.equals("prices")) {
					// /shop log : show transactions log (summary) of the day
					plugin.pluginInfosPrices(player);
				} else if (param.equals("simul")) {
					// /shop simul : simulate new prices using last prices and transactions log
					plugin.marketFile.dailyPricesCalculation(plugin.dailyLog, true);
					player.sendMessage(plugin.lang.tr("Daily prices calculation simulation is into the realshop.log file"));
				} else if (param.equals("daily")) {
					// /shop daily : calculate and save new prices using last prices and transactions log
					plugin.marketFile.dailyPricesCalculation(plugin.dailyLog);
					player.sendMessage(plugin.lang.tr("Real daily prices calculation log is into the realshop.log file"));
				} else if (param.equals("log")) {
					// /shop log : log daily movements
					plugin.pluginInfosDailyLog(player);
					player.sendMessage(plugin.lang.tr("Daily log was dumped into the realshop.log file"));
				} else {
					event.setCancelled(false);
				}
			} else {
				event.setCancelled(false);
			}
		}
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

	//---------------------------------------------------------------------------------- onPlayerQuit
	public void onPlayerQuit(PlayerEvent event)
	{
		if (plugin.playersInChestCounter > 0) {
			plugin.exitChest(event.getPlayer());
		}
	}

}
