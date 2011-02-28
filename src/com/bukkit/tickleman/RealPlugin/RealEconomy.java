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
			try {
				getCurrency();
			} catch (Exception e) {
				ok = false;
			}
			if (ok) {
				plugin.log.info("load dependency : iConomy " + iConomyVersion + " ok", true);
			} else {
				plugin.log.severe(
					"load dependency : iConomy was found but was not version " + iConomyVersion, true
				);
			}
		}
		return ok;
	}

	//---------------------------------------------------------------------------------- getBalance
	public static double getBalance(String playerName)
	{
		Account account = iConomy.getBank().getAccount(playerName);
		if (account == null) {
			System.out.println("[RealShop] iConomy.getAccount(" + playerName + ") returned null !");
			return 0;
		} else {
			return account.getBalance();
		}
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
		if (account == null) {
			System.out.println("[RealShop] iConomy.getAccount(" + playerName + ") returned null !");
		} else {
			account.setBalance(Math.round(balance * 100) / 100);
			account.save();
		}
	}

}
