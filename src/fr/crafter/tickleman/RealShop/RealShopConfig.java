package fr.crafter.tickleman.RealShop;

import java.io.BufferedWriter;

import fr.crafter.tickleman.RealPlugin.RealConfig;

//################################################################################## RealShopConfig
public class RealShopConfig extends RealConfig
{

	/** Default configuration values (if not in file) */
	public double amountRatio = 5000.0;
	public double buySellRatio = .95;
	public String dailyPricesCalculation = "false";
	public String economyPlugin = "RealEconomy";
	public double maxDailyRatio = 1.95;
	public double maxItemPrice = 99999.0;
	public double minDailyRatio = .05;
	public double minItemPrice = .1;
	public String permissionsPlugin = "none";
	public String shopDamagedItems = "true";
	public String shopInfiniteBuy = "false";
	public String shopInfiniteSell = "false";
	public String shopMarketItemsOnly = "false";
	public String shopOpOnly = "false";
	public double workForceRatio = 1.1;

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
		if (
			!permissionsPlugin.equals("none")
			&& !permissionsPlugin.equals("Permissions")
		) {
			plugin.log.warning(
				"unknown permissionsPlugin " + permissionsPlugin + " was set to none instead", true
			);
			permissionsPlugin = "none";
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
		if (key.equals("amountRatio")) {
			try {
				amountRatio = Double.parseDouble(value);
			} catch (Exception e) {
				amountRatio = 5000.0;
			}
			return true;
		}
		if (key.equals("buySellRatio")) {
			try {
				buySellRatio = Double.parseDouble(value);
			} catch (Exception e) {
				buySellRatio = .95;
			}
			return true;
		}
		if (key.equals("dailyPricesCalculation")) {
			dailyPricesCalculation = value;
			return true;
		}
		if (key.equals("economyPlugin")) {
			economyPlugin = value;
			return true;
		}
		if (key.equals("maxDailyRatio")) {
			try {
				maxDailyRatio = Double.parseDouble(value);
			} catch (Exception e) {
				maxDailyRatio = 1.95;
			}
			return true;
		}
		if (key.equals("maxItemPrice")) {
			try {
				maxItemPrice = Double.parseDouble(value);
			} catch (Exception e) {
				maxItemPrice = 99999.0;
			}
			return true;
		}
		if (key.equals("minDailyRatio")) {
			try {
				minDailyRatio = Double.parseDouble(value);
			} catch (Exception e) {
				minDailyRatio = .05;
			}
			return true;
		}
		if (key.equals("minItemPrice")) {
			try {
				minItemPrice = Double.parseDouble(value);
			} catch (Exception e) {
				minItemPrice = .1;
			}
			return true;
		}
		if (key.equals("permissionsPlugin")) {
			permissionsPlugin = value;
			return true;
		}
		if (key.equals("shopDamagedItems")) {
			shopDamagedItems = value;
			return true;
		}
		if (key.equals("shopInfiniteBuy")) {
			shopInfiniteBuy = value;
			return true;
		}
		if (key.equals("shopInfiniteSell")) {
			shopInfiniteSell = value;
			return true;
		}
		if (key.equals("shopMarketItemsOnly")) {
			shopMarketItemsOnly = value;
			return true;
		}
		if (key.equals("shopOpOnly")) {
			shopOpOnly = value;
			return true;
		}
		if (key.equals("workForceRatio")) {
			try {
				workForceRatio = Double.parseDouble(value);
			} catch (Exception e) {
				workForceRatio = 1.1;
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
		saveValue(writer, "amountRatio");
		saveValue(writer, "buySellRatio");
		saveValue(writer, "dailyPricesCalculation");
		saveValue(writer, "economyPlugin");
		saveValue(writer, "maxDailyRatio");
		saveValue(writer, "maxItemPrice");
		saveValue(writer, "minDailyRatio");
		saveValue(writer, "minItemPrice");
		saveValue(writer, "permissionsPlugin");
		saveValue(writer, "shopDamagedItems");
		saveValue(writer, "shopInfiniteBuy");
		saveValue(writer, "shopInfiniteSell");
		saveValue(writer, "shopMarketItemsOnly");
		saveValue(writer, "shopOpOnly");
		saveValue(writer, "workForceRatio");
	}

}
