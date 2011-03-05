package fr.crafter.tickleman.RealShop;

import java.io.BufferedWriter;

import fr.crafter.tickleman.RealPlugin.RealConfig;

//################################################################################## RealShopConfig
public class RealShopConfig extends RealConfig
{

	/** Default configuration values (if not in file) */
	public String dailyPricesCalculation = "false";
	public String economyPlugin = "RealEconomy";
	public String shopDamagedItems = "true";
	public String shopInfiniteBuy = "false";
	public String shopInfiniteSell = "false";
	public String shopMarketItemsOnly = "false";
	public String shopOpOnly = "false";

	//------------------------------------------------------------------------------ RealShopConfig
	public RealShopConfig(final RealShopPlugin plugin)
	{
		super(plugin);
	}

	//------------------------------------------------------------------------------------- loadValue
	protected boolean loadValue(String key, String value)
	{
		if (key.equals("dailyPricesCalculation")) { dailyPricesCalculation = value; return true; }
		if (key.equals("economyPlugin")) { economyPlugin = value; return true; }
		if (key.equals("shopDamagedItems")) { shopDamagedItems = value; return true; }
		if (key.equals("shopInfiniteBuy")) { shopInfiniteBuy = value; return true; }
		if (key.equals("shopInfiniteSell")) { shopInfiniteSell = value; return true; }
		if (key.equals("shopMarketItemsOnly")) { shopMarketItemsOnly = value; return true; }
		if (key.equals("shopOpOnly")) { shopOpOnly = value; return true; }
		if (!economyPlugin.equals("RealEconomy") && !economyPlugin.equals("iConomy")) {
			plugin.log.warning(
				"unknown economyPlugin " + economyPlugin + " was set to RealEconomy instead", true
			);
			economyPlugin = "RealEconomy";
		}
		return false;
	}

	//---------------------------------------------------------------------------------------- save
	/*
	 * Save values. Override original to add my own configuration values.
	 */
	@Override
	protected void saveValues(BufferedWriter writer)
	{
		super.saveValues(writer);
		saveValue(writer, "dailyPricesCalculation");
		saveValue(writer, "economyPlugin");
		saveValue(writer, "shopDamagedItems");
		saveValue(writer, "shopInfiniteBuy");
		saveValue(writer, "shopInfiniteSell");
		saveValue(writer, "shopMarketItemsOnly");
		saveValue(writer, "shopOpOnly");
	}

}
