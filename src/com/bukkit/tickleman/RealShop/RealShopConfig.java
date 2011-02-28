package com.bukkit.tickleman.RealShop;

import java.io.BufferedWriter;

import com.bukkit.tickleman.RealPlugin.RealConfig;

//################################################################################## RealShopConfig
public class RealShopConfig extends RealConfig
{

	/** Default configuration values (if not in file) */
	public String dailyPricesCalculation = "false"; 

	//------------------------------------------------------------------------------ RealShopConfig
	public RealShopConfig(final RealShopPlugin plugin)
	{
		super(plugin);
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
	}

}
