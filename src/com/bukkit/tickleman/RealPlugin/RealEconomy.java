package com.bukkit.tickleman.RealPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import com.nijikokun.bukkit.iConomy.Database;
import com.nijikokun.bukkit.iConomy.iConomy;

//##################################################################################### IConomyLink
public abstract class RealEconomy extends iConomy
{

	private static Field currencyField;
	private static Method getBalanceMethod;
	private static Method setBalanceMethod;

	//---------------------------------------------------------------------------------- isInstalled
	public RealEconomy(PluginLoader pluginLoader, org.bukkit.Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
	}

	//---------------------------------------------------------------------------------- isInstalled
	public static boolean init(RealPlugin realPlugin)
	{
		// initialize links 
		currencyField = null;
		getBalanceMethod = null;
		setBalanceMethod = null;
		// load iConomy
		iConomy iConomy = (iConomy)realPlugin.getServer().getPluginManager().getPlugin("iConomy");
		boolean ok = (iConomy != null);
		// check iConomy fields and methods
		if (ok) {
			Class<? extends iConomy> iConomyClass = iConomy.getClass();
			Field dbField = null;
			try { dbField = iConomyClass.getField("database"); }
			catch (Exception e) { realPlugin.log.warning("database not found"); }
			if (dbField == null) {
				try { dbField = iConomyClass.getField("db"); }
				catch (Exception e) { realPlugin.log.warning("db not found"); }
				
			}
			if (dbField != null) {
				Database database = null;
				try { database = (Database)dbField.get(null); }
				catch (Exception e) { realPlugin.log.warning("can't get database"); }
				if (database != null) {
					try {currencyField = iConomyClass.getField("currency"); }
					catch (Exception e) { realPlugin.log.severe("currency not found"); }
					Class<? extends Database> dbClass = database.getClass(); 
					try { getBalanceMethod = dbClass.getMethod("getBalance", String.class); }
					catch (Exception e) { realPlugin.log.warning("getBalance() not found"); }
					if (getBalanceMethod == null) {
						try { getBalanceMethod = dbClass.getMethod("get_balance", String.class); }
						catch (Exception e) { realPlugin.log.severe("get_balance() not found"); }
					}
					Class<?>[] classes = { String.class, double.class };
					try { setBalanceMethod = dbClass.getMethod("setBalance", classes); }
					catch (Exception e) { realPlugin.log.warning("setBalance() not found"); }
					if (setBalanceMethod == null) {
						Class<?>[] classes2 = { String.class, int.class };
						try { setBalanceMethod = dbClass.getMethod("set_balance", classes2); }
						catch (Exception e) { realPlugin.log.severe("set_balance() not found"); }
					}
				}
			}
		}
		if (
			(currencyField == null)
			|| (getBalanceMethod == null)
			|| (setBalanceMethod == null)
		) {
			ok = false;
			realPlugin.log.severe(
				"unable to find one or several iConomy API methods."
				+ " Make sure you have version 2.x or 3.x of iConomy plugin!"
			);
		}
		if (ok) {
			realPlugin.log.info("load dependency : iConomy ok");
		}
		return ok;
	}

	//---------------------------------------------------------------------------------- getBalance
	public static double getBalance(String player)
	{
		try {
			return (Double)(getBalanceMethod.invoke(null, player));
		} catch (Exception e) {
			return 0;
		}
	}

	//---------------------------------------------------------------------------------- getBalance
	public static String getCurrency()
	{
		try {
			return (String)(currencyField.get(null));
		} catch (Exception e) {
			return "Coin";
		}
	}

	//---------------------------------------------------------------------------------- getBalance
	public static void setBalance(String player, double balance)
	{
		try {
			setBalanceMethod.invoke(null, player, balance);
		} catch (Exception e1) {
		}
	}

}
