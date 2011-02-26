package com.bukkit.tickleman.RealShop;

import java.io.BufferedWriter;

import com.bukkit.tickleman.RealPlugin.RealConfig;

//################################################################################## RealShopConfig
public class RealShopConfig extends RealConfig
{

	public String dailyPricesCalculation = "false"; 

	//------------------------------------------------------------------------------ RealShopConfig
	public RealShopConfig(final RealShopPlugin plugin)
	{
		super(plugin);
	}

	//---------------------------------------------------------------------------------------- save
	@Override
	protected void saveValues(BufferedWriter writer)
	{
		super.saveValues(writer);
		saveValue(writer, "dailyPricesCalculation");
	}

}
