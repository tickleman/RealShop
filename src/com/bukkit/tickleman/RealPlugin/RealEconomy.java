package com.bukkit.tickleman.RealPlugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

//##################################################################################### IConomyLink
public abstract class RealEconomy
{

	private static String iConomyVersion = "4.x";

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
		return iConomy.getBank().getAccount(playerName).getBalance();
	}

	//---------------------------------------------------------------------------------- getBalance
	public static String getCurrency()
	{
		return iConomy.getBank().getCurrency();
	}

	//---------------------------------------------------------------------------------- getBalance
	public static void setBalance(String playerName, double balance)
	{
		Account account = iConomy.getBank().getAccount(playerName); 
		account.setBalance(balance);
		account.save();
	}

}
