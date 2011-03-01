package fr.crafter.tickleman.RealShop;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
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

	//------------------------------------------------------------------------------- onPlayerCommand
	public void onPlayerCommand(PlayerChatEvent event)
	{
		String[] cmd = event.getMessage().split(" ");
		String command = ((cmd.length > 0) ? cmd[0].toLowerCase() : "");
		// SHOP
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
		} else if (command.equals("/mny") && (plugin.config.economyPlugin == "RealEconomy")) {
			event.setCancelled(true);
			// simple /mny commands
			String param = ((cmd.length > 1) ? cmd[1].toLowerCase() : "");
			Player player = event.getPlayer();
			String playerName = player.getName();
			if (param.equals("help")) {
				// HELP
				player.sendMessage("RealEconomy help");
				player.sendMessage("/mny : tell me how many money I have in my pocket");
				player.sendMessage("/mny give <player> <amount> : give money to another player");
				player.sendMessage("/mny burn amount : burn your money");
				if (player.isOp()) {
					player.sendMessage("RealEconomy operator help");
					player.sendMessage("/mny tell <player> : tell me how many money the player has");
					player.sendMessage("/mny set <player> <balance> : sets the balance of a player");
					player.sendMessage("/mny inc <player> <amount> : increase balance of a player");
					player.sendMessage("/mny dec <player> <amount> : decrease the balance of a player");
					//player.sendMessage("/mny top [<count>] : tell the top count players");
				}
 			} else if (param.equals("")) {
 				// NO PARAM : BALANCE
 				player.sendMessage(
 					"You've got "
 					+ plugin.realEconomy.getBalance(playerName) + plugin.realEconomy.getCurrency()
 					+ " in your pocket"
 				);
 			} else if (param.equals("give")) {
 				// GIVE MONEY
 				String toPlayerName = ((cmd.length > 2) ? cmd[2] : "");
				double amount;
				try {
					amount = ((cmd.length > 3) ? Double.parseDouble(cmd[3]) : 0);
				} catch (Exception e) {
					amount = 0;
				}
				if (amount > 0) {
					if (plugin.realEconomy.getBalance(playerName) >= amount) {
						// transfer money with rollback
						if (plugin.realEconomy.setBalance(
							playerName, plugin.realEconomy.getBalance(playerName) - amount
						)) {
							if (!plugin.realEconomy.setBalance(
									toPlayerName, plugin.realEconomy.getBalance(toPlayerName) + amount
							)) {
								plugin.realEconomy.setBalance(
									playerName, plugin.realEconomy.getBalance(playerName) + amount
								);
							}
						}
						player.sendMessage(
							"You give " + amount + plugin.realEconomy.getCurrency() + " to " + toPlayerName
						);
						Player toPlayer = plugin.getServer().getPlayer(toPlayerName);
						if (toPlayer != null) {
							toPlayer.sendMessage(
								playerName + " gives you " + amount + plugin.realEconomy.getCurrency()
							);
						}
						plugin.log.info(
							playerName + " gives " + amount + plugin.realEconomy.getCurrency()
							+ " to " + toPlayerName
						);
					} else {
						player.sendMessage(
							"You don't have enough " + plugin.realEconomy.getCurrency()
						);
					}
				}
 			} else if (param.equals("burn")) {
 				double amount;
 				try {
 					amount = ((cmd.length > 3) ? Double.parseDouble(cmd[3]) : 0);
 				} catch (Exception e) {
 					amount = 0;
 				}
 				amount = Math.min(plugin.realEconomy.getBalance(playerName), amount);
 				if (amount > 0) {
					plugin.realEconomy.setBalance(
						playerName, plugin.realEconomy.getBalance(playerName) - amount
					);
					player.sendMessage(
						"You burned " + amount + plugin.realEconomy.getCurrency()
					);
 				}
 			} else if (player.isOp()) {
 				if (param.equals("tell")) {
 					String toPlayerName = ((cmd.length > 2) ? cmd[2] : "");
 					// TELL
 					player.sendMessage(
 						toPlayerName + " has got "
 						+ plugin.realEconomy.getBalance(playerName) + plugin.realEconomy.getCurrency()
 						+ " in your pocket"
 					);
 				} else if (param.equals("set")) {
 					// SET
 					String toPlayerName = ((cmd.length > 2) ? cmd[2] : "");
 					double amount;
 					try {
 						amount = ((cmd.length > 3) ? Double.parseDouble(cmd[3]) : 0);
 					} catch (Exception e) {
 						amount = 0;
 					}
					plugin.realEconomy.setBalance(toPlayerName, amount);
					player.sendMessage(
						toPlayerName + " balance set to " + amount + plugin.realEconomy.getCurrency()
					);
					Player toPlayer = plugin.getServer().getPlayer(toPlayerName);
					if (toPlayer != null) {
						toPlayer.sendMessage(
							playerName + " sets your balance to " + amount + plugin.realEconomy.getCurrency()
						);
					}
 				} else if (param.equals("inc")) {
 					// INC
 					String toPlayerName = ((cmd.length > 2) ? cmd[2] : "");
 					double amount;
 					try {
 						amount = ((cmd.length > 3) ? Double.parseDouble(cmd[3]) : 0);
 					} catch (Exception e) {
 						amount = 0;
 					}
 					plugin.realEconomy.setBalance(
 						toPlayerName, plugin.realEconomy.getBalance(toPlayerName) + amount
 					);
					player.sendMessage(
						"You increase " + toPlayerName + "'s balance of " 
						+ amount + plugin.realEconomy.getCurrency()
					);
					Player toPlayer = plugin.getServer().getPlayer(toPlayerName);
					if (toPlayer != null) {
						toPlayer.sendMessage(
							playerName + " increased your balance of "
							+ amount + plugin.realEconomy.getCurrency()
						);
					}
					plugin.log.info(
						playerName + " increases the balance of " + toPlayerName
						+ " of " + amount + plugin.realEconomy.getCurrency()
					);
 				} else if (param.equals("dec")) {
 					// DEC
 					String toPlayerName = ((cmd.length > 2) ? cmd[2] : "");
 					double amount;
 					try {
 						amount = ((cmd.length > 3) ? Double.parseDouble(cmd[3]) : 0);
 					} catch (Exception e) {
 						amount = 0;
 					}
 					amount = Math.min(plugin.realEconomy.getBalance(toPlayerName), amount);
 					plugin.realEconomy.setBalance(
 						toPlayerName, plugin.realEconomy.getBalance(toPlayerName) - amount
 					);
					player.sendMessage(
						"You decrease " + toPlayerName + "'s balance of "
						+ amount + plugin.realEconomy.getCurrency()
					);
					Player toPlayer = plugin.getServer().getPlayer(toPlayerName);
					if (toPlayer != null) {
						toPlayer.sendMessage(
							playerName + " decreased your balance of "
							+ amount + plugin.realEconomy.getCurrency()
						);
					}
					plugin.log.info(
						playerName + " decreases the balance of " + toPlayerName
						+ " of " + amount + plugin.realEconomy.getCurrency()
					);
 				} else if (param.equals("top")) {
 					// TOP
 					/*
 					int count;
 					try {
 						count = ((cmd.length > 2) ? Integer.parseInt(cmd[2]) : 0);
 					} catch (Exception e) {
 						count = 0;
 					}
 					int subCount = 0;
 					while ((count == 0) || (subCount < count)) {
 						
 					}
 					*/
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
