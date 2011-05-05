package fr.crafter.tickleman.RealEconomy;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

import fr.crafter.tickleman.RealPlugin.RealPlugin;


//##################################################################################### IConomyLink
public abstract class iConomyLink
{

	private static String iConomyVersion = "5.x";

	public static boolean initialized = false;

	private static RealPlugin plugin;

	//---------------------------------------------------------------------------------------- format
	public static String format(double amount)
	{
		return iConomy.format(amount);
	}

	//------------------------------------------------------------------------------------ getBalance
	public static double getBalance(String playerName)
	{
		Account account = iConomy.getAccount(playerName);
		Holdings holding = ((account != null) ? account.getHoldings() : null);
		if (account == null || holding == null) {
			plugin.log.warning("iConomy.getAccount(" + playerName + ") returned null !");
			return 0;
		} else {
			try {
				return holding.balance();
			} catch (Exception e) {
				plugin.log.severe("iConomy.getBalance() crashed with this message :");
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
	}

	//----------------------------------------------------------------------------------- getCurrency
	public static String getCurrency()
	{
		try {
			// TODO found where to get currency from iConomy
			return "Coin";
		} catch (Exception e) {
			plugin.log.severe("iConomy.getCurency() crashed with this message :");
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
		iConomyLink.plugin = plugin;
		boolean ok = (plugin.getServer().getPluginManager().getPlugin("iConomy") != null);
		if (ok) {
			try {
				getCurrency();
			} catch (Exception e) {
				ok = false;
			}
			if (ok) {
				plugin.log.info("load dependency : iConomy " + iConomyVersion + " ok", true);
				initialized = true;
			} else {
				plugin.log.severe(
					"load dependency : iConomy was found but was not version " + iConomyVersion, true
				);
			}
		}
		return ok;
	}

	//------------------------------------------------------------------------------------ setBalance
	public static boolean setBalance(String playerName, double balance)
	{
		boolean result = false;
		Account account = iConomy.getAccount(playerName);
		Holdings holding = ((account != null) ? account.getHoldings() : null);
		if (account == null || holding == null) {
			plugin.log.warning("iConomy.getAccount(" + playerName + ") returned null !");
		} else {
			try {
				holding.set(Math.round(balance * 100.0) / 100.0);
				result = true;
			} catch (Exception e) {
				plugin.log.severe(
					"iConomy.setBalance(" + playerName + ", " + (Math.round(balance * 100.0) / 100.0) + ")"
					+ " crashed with this message :"
				);
				plugin.log.severe(e.getMessage());
			}
		}
		return result;
	}

}
