package fr.crafter.tickleman.RealEconomy;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//##################################################################################### RealEconomy
public class RealEconomy
{

	private RealAccountsFile accountsFile;
	private RealEconomyConfig config;

	//----------------------------------------------------------------------------------- RealEconomy
	public RealEconomy(RealPlugin plugin)
	{
		accountsFile = new RealAccountsFile(plugin);
		config = new RealEconomyConfig(plugin);
	}

	//------------------------------------------------------------------------------------ getBalance
	public double getBalance(String playerName)
	{
		Double balance = accountsFile.accounts.get(playerName);
		if (balance == null) {
			return config.initialBalance;
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
	public void setBalance(String playerName, double balance)
	{
		accountsFile.accounts.put(playerName, balance);
		accountsFile.save();
	}

}
