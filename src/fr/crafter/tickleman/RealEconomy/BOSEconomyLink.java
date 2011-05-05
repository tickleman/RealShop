package fr.crafter.tickleman.RealEconomy;

import org.bukkit.plugin.Plugin;

import cosine.boseconomy.BOSEconomy;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

public abstract class BOSEconomyLink
{

	private static BOSEconomy economy;

	public static boolean initialized = false;

	private static RealPlugin plugin;
	
	//---------------------------------------------------------------------------------------- format
	public static String format(Double amount)
	{
		return amount.toString() + " " + BOSEconomyLink.getCurrency();
	}

	//------------------------------------------------------------------------------------ getBalance
	public static double getBalance(String playerName)
	{
		try {
			return BOSEconomyLink.economy.getPlayerMoney(playerName);
		} catch (Exception e) {
			plugin.log.severe("BOSEconomy.getPlayerMoney() crashed with this message :");
			plugin.log.severe(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				StackTraceElement el = e.getStackTrace()[i];
				plugin.log.info(
					el.getClassName() + "." + el.getMethodName()
					+ "(" + el.getFileName() + ":" + el.getLineNumber() + ")"
				);
			}
			return 0;
		}
	}

	//----------------------------------------------------------------------------------- getCurrency
	public static String getCurrency()
	{
		try {
			return BOSEconomyLink.economy.getMoneyName();
		} catch(Exception e) {
			plugin.log.severe("BOSEconomy.getCurency() crashed with this message :");
			plugin.log.severe(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				StackTraceElement el = e.getStackTrace()[i];
				plugin.log.info(
					el.getClassName() + "." + el.getMethodName()
					+ "(" + el.getFileName() + ":" + el.getLineNumber() + ")"
				);
			}
			return "Coin";
		}
	}

	//------------------------------------------------------------------------------------------ init
	public static boolean init(RealPlugin plugin)
	{
		BOSEconomyLink.plugin = plugin;
		// load BOSEconomy plugin
		Plugin temp = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
		if (temp != null) {
			BOSEconomyLink.economy = (BOSEconomy)plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
		} else {
			BOSEconomyLink.economy = null;
		}
		// ok
		boolean ok = (economy != null);
		if (ok) {
			try {
				getCurrency();
			} catch (Exception e) {
				ok = false;
			}
			if (ok) {
				plugin.log.info("load dependency : BOSEconomy ok", true);
				initialized = true;
			} else {
				plugin.log.severe(
					"load dependency : BOSEconomy was found but was not a compatible version", true
				);
			}
		}
		return ok;
	}

	//------------------------------------------------------------------------------------ setBalance
	public static boolean setBalance(String playerName, double balance)
	{
		try {
			BOSEconomyLink.economy.setPlayerMoney(playerName, (int)Math.round(balance), false);
			return true;
		} catch (Exception e) {
			plugin.log.severe("BOSEconomy.setPlayerMoney() crashed with this message :");
			plugin.log.severe(e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				StackTraceElement el = e.getStackTrace()[i];
				plugin.log.info(
					el.getClassName() + "." + el.getMethodName()
					+ "(" + el.getFileName() + ":" + el.getLineNumber() + ")"
				);
			}
			return false;
		}
	}

}
