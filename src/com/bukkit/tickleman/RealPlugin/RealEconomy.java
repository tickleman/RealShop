package com.bukkit.tickleman.RealPlugin;

import com.nijikokun.bukkit.iConomy.iConomy;

//##################################################################################### IConomyLink
public abstract class RealEconomy
{

	//private static String iConomyVersion = "2.x";
	private static String iConomyVersion = "3.x";

	//----------------------------------------------------------------------------------- isInstalled
	public static boolean init(RealPlugin plugin)
	{
		boolean ok = (plugin.getServer().getPluginManager().getPlugin("iConomy") != null);
		if (ok) {
			plugin.log.info("load dependency : iConomy " + iConomyVersion + " ok", true);
		}
		return ok;
	}

	//---------------------------------------------------------------------------------- getBalance
	public static double getBalance(String playerName)
	{
		//return iConomy.db.get_balance(playerName);
		return iConomy.database.getBalance(playerName);
	}

	//---------------------------------------------------------------------------------- getBalance
	public static String getCurrency()
	{
		return iConomy.currency;
	}

	//---------------------------------------------------------------------------------- getBalance
	public static void setBalance(String player, double balance)
	{
		//iConomy.db.set_balance(player, (int)balance);
		iConomy.database.setBalance(player, balance);
	}

	//---------------------------------------------------------------------------------- getBalance
	public static void setBalance(String player, long balance)
	{
		setBalance(player, (double)balance);
	}

}
