package fr.crafter.tickleman.RealEconomy;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//##################################################################################### RealEconomy
public class RealEconomy
{

	public RealAccountsFile accountsFile;
	public RealEconomyConfig config;
	private RealPlugin plugin;

	public String economyPlugin;

	//----------------------------------------------------------------------------------- RealEconomy
	public RealEconomy(RealPlugin plugin)
	{
		this.plugin = plugin;
		this.economyPlugin = "RealEconomy";
		accountsFile = new RealAccountsFile(plugin);
		config = new RealEconomyConfig(plugin);
		config.load();
	}

	//------------------------------------------------------------------------------------ getBalance
	public String getBalance(String playerName, boolean withCurrency)
	{
		Double balance = getBalance(playerName);
		if (withCurrency) {
			return balance + " " + getCurrency();
		} else {
			return balance.toString();
		}
	}

	//------------------------------------------------------------------------------------ getBalance
	public double getBalance(String playerName)
	{
		Double balance = (double)0;
		if (iConomyLink.initialized && economyPlugin.equals("iConomy")) {
			balance = iConomyLink.getBalance(playerName);
		} else if (BOSEconomyLink.initialized && economyPlugin.equals("BOSEconomy")) {
			balance = BOSEconomyLink.getBalance(playerName);
		} else {
			balance = accountsFile.accounts.get(playerName);
			if (balance == null) {
				try {
					balance = Double.parseDouble(config.initialBalance);
				} catch (Exception e) {
					balance = (double)0;
				}
			}
		}
		return Math.round(balance * 100) / 100;
	}

	//----------------------------------------------------------------------------------- getCurrency
	public String getCurrency()
	{
		if (iConomyLink.initialized && economyPlugin.equals("iConomy")) {
			return iConomyLink.getCurrency();
		} else if (BOSEconomyLink.initialized && economyPlugin.equals("BOSEconomy")) {
			return BOSEconomyLink.getCurrency();
		} else {
			return config.currency;
		}
	}

	//------------------------------------------------------------------------------------ setBalance
	public boolean setBalance(String playerName, double balance)
	{
		if (iConomyLink.initialized && economyPlugin.equals("iConomy")) {
			return iConomyLink.setBalance(playerName, balance);
		} else if (BOSEconomyLink.initialized && economyPlugin.equals("BOSEconomy")) {
			return BOSEconomyLink.setBalance(playerName, balance);
		} else {
			try {
				accountsFile.accounts.put(playerName, balance);
				accountsFile.save();
			} catch (Exception e) {
				plugin.log.severe("RealEconomy.setBalance() crashed with this message :");
				plugin.log.severe(e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++) {
					StackTraceElement el = e.getStackTrace()[i];
					plugin.log.info(
						el.getClassName() + "." + el.getMethodName()
						+ "(" + el.getFileName() + ":" + el.getLineNumber() + ")"
					);
				}
			}
			return true;
		}
	}

}
