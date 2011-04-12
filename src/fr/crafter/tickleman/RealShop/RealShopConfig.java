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
	public double workForceRatio = (double)1.1;
	public double buySellRatio = (double).95;
	public double minDailyRatio = (double).05;
	public double maxDailyRatio = (double)1.95;
	public double amountRatio = (double)5000;
	public double minItemPrice = (double).1;
	public double maxItemPrice = (double)99999;

	//-------------------------------------------------------------------------------- RealShopConfig
	public RealShopConfig(final RealShopPlugin plugin)
	{
		super(plugin);
	}

	//------------------------------------------------------------------------------------------ load
	@Override
	public RealShopConfig load()
	{
		super.load();
		if (
			!economyPlugin.equals("RealEconomy")
			&& !economyPlugin.equals("iConomy")
			&& !economyPlugin.equals("BOSEconomy")
		) {
			plugin.log.warning(
				"unknown economyPlugin " + economyPlugin + " was set to RealEconomy instead", true
			);
			economyPlugin = "RealEconomy";
		}
		return this;
	}

	//------------------------------------------------------------------------------------- loadValue
	@Override
	protected boolean loadValue(String key, String value)
	{
		if (super.loadValue(key, value)) {
			return true;
		}
		if (key.equals("dailyPricesCalculation")) { dailyPricesCalculation = value; return true; }
		if (key.equals("economyPlugin")) { economyPlugin = value; return true; }
		if (key.equals("shopDamagedItems")) { shopDamagedItems = value; return true; }
		if (key.equals("shopInfiniteBuy")) { shopInfiniteBuy = value; return true; }
		if (key.equals("shopInfiniteSell")) { shopInfiniteSell = value; return true; }
		if (key.equals("shopMarketItemsOnly")) { shopMarketItemsOnly = value; return true; }
		if (key.equals("shopOpOnly")) { shopOpOnly = value; return true; }
		if (key.equals("workForceRatio")) {
			try {
				workForceRatio = Double.parseDouble(value);
			} catch (Exception e) {
				workForceRatio = (double)1.1;
			}
			return true;
		}
		if (key.equals("buySellRatio")) {
			try {
				buySellRatio = Double.parseDouble(value);
			} catch (Exception e) {
				buySellRatio = (double).95;
			}
			return true;
		}
		if (key.equals("minDailyRatio")) {
			try {
				minDailyRatio = Double.parseDouble(value);
			} catch (Exception e) {
				minDailyRatio = (double).05;
			}
			return true;
		}
		if (key.equals("maxDailyRatio")) {
			try {
				maxDailyRatio = Double.parseDouble(value);
			} catch (Exception e) {
				maxDailyRatio = (double)1.95;
			}
			return true;
		}
		if (key.equals("amountRatio")) {
			try {
				amountRatio = Double.parseDouble(value);
			} catch (Exception e) {
				amountRatio = (double)5000;
			}
			return true;
		}
		if (key.equals("minItemPrice")) {
			try {
				minItemPrice = Double.parseDouble(value);
			} catch (Exception e) {
				minItemPrice = (double).1;
			}
			return true;
		}
		if (key.equals("maxItemPrice")) {
			try {
				maxItemPrice = Double.parseDouble(value);
			} catch (Exception e) {
				maxItemPrice = (double)99999;
			}
			return true;
		}
		return false;
	}

	//------------------------------------------------------------------------------------------ save
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
		saveValue(writer, "workForceRatio");
		saveValue(writer, "buySellRatio");
		saveValue(writer, "minDailyRatio");
		saveValue(writer, "maxDailyRatio");
		saveValue(writer, "amountRatio");
		saveValue(writer, "minItemPrice");
		saveValue(writer, "maxItemPrice");
	}

}
