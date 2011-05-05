package fr.crafter.tickleman.RealEconomy;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//##################################################################################### IConomyLink
/**
 * This is not used anymore in 0.30
 * Back (optionally) with 0.31
 */
public abstract class iConomyLink
{

	private static String iConomyVersion = "4.x";

	public static boolean initialized = false;

	private static RealPlugin plugin;

	//------------------------------------------------------------------------------------ getBalance
	public static double getBalance(String playerName)
	{
		Account account = iConomy.getBank().getAccount(playerName);
		if (account == null) {
			plugin.log.warning("iConomy.getAccount(" + playerName + ") returned null !");
			return 0;
		} else {
			try {
				return account.getBalance();
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
			return iConomy.getBank().getCurrency();
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
		Account account = iConomy.getBank().getAccount(playerName);
		if (account == null) {
			plugin.log.warning("iConomy.getAccount(" + playerName + ") returned null !");
		} else {
			try {
				account.setBalance(Math.round(balance * 100.0) / 100.0);
				result = true;
			} catch (Exception e) {
				plugin.log.severe(
					"iConomy.setBalance(" + playerName + ", " + (Math.round(balance * 100.0) / 100.0) + ")"
					+ " crashed with this message :"
				);
				plugin.log.severe(e.getMessage());
			}
			/* BP 2011-04-12 account.save() call is deprecated :
			try {
				account.save();
			} catch (Exception e) {
				plugin.log.severe(
					"iConomy.save(" + playerName + ", " + (Math.round(balance * 100.0) / 100.0) + ")"
					+ " crashed with this message :"
				);
				plugin.log.severe(e.getMessage());
			}
			*/
		}
		return result;
	}

}
