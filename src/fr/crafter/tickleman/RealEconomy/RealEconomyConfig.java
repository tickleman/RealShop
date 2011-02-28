package fr.crafter.tickleman.RealEconomy;

import java.io.BufferedWriter;

import fr.crafter.tickleman.RealPlugin.RealConfig;
import fr.crafter.tickleman.RealPlugin.RealPlugin;

//################################################################################## RealShopConfig
public class RealEconomyConfig extends RealConfig
{

	/** Default configuration values (if not in file) */
	public String currency = "Coin"; 
	public Double initialBalance = (double)100;

	//----------------------------------------------------------------------------- RealEconomyConfig
	public RealEconomyConfig(final RealPlugin plugin)
	{
		super(plugin, "economy");
		language = plugin.language;
	}

	//------------------------------------------------------------------------------------------ save
	/*
	 * Save values. Override original to add my own configuration values.
	 */
	@Override
	protected void saveValues(BufferedWriter writer)
	{
		super.saveValues(writer);
		saveValue(writer, "currency");
		saveValue(writer, "initialBalance");
	}

}
