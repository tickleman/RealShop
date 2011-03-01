package fr.crafter.tickleman.RealEconomy;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//##################################################################################### RealEconomy
public class RealEconomy
{

	private RealAccountsFile accountsFile;
	private RealEconomyConfig config;
	private RealPlugin plugin;

	public String economyPlugin = "RealEconomy";

	//----------------------------------------------------------------------------------- RealEconomy
	public RealEconomy(RealPlugin plugin)
	{
		this.plugin = plugin;
		accountsFile = new RealAccountsFile(plugin);
		config = new RealEconomyConfig(plugin);
		config.load();
	}

	//------------------------------------------------------------------------------------ getBalance
	public double getBalance(String playerName)
	{
		Double balance = accountsFile.accounts.get(playerName);
		if (balance == null) {
			try {
				return Double.parseDouble(config.initialBalance);
			} catch (Exception e) {
				return 0;
			}
		} else {
			return balance;
		}
	}

	//----------------------------------------------------------------------------------- getCurrency
	public String getCurrency()
	{
		return config.currency;
	}

	//------------------------------------------------------------------------------------ setBalance
	public boolean setBalance(String playerName, double balance)
	{
		if (economyPlugin == "iConomy") {
			return iConomyLink.setBalance(playerName, balance);
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
