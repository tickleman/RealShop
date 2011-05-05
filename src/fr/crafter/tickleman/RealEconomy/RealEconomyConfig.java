package fr.crafter.tickleman.RealEconomy;

import java.io.BufferedWriter;

import fr.crafter.tickleman.RealPlugin.RealConfig;
import fr.crafter.tickleman.RealPlugin.RealPlugin;

//################################################################################## RealShopConfig
public class RealEconomyConfig extends RealConfig
{

	/** Default configuration values (if not in file) */

	public String currency = "Coin"; 

	public String initialBalance = "100";

	//----------------------------------------------------------------------------- RealEconomyConfig
	public RealEconomyConfig(final RealPlugin plugin)
	{
		super(plugin, "economy");
		language = plugin.language;
	}

	//------------------------------------------------------------------------------------- loadValue
	protected boolean loadValue(String key, String value)
	{
		if (key.equals("currency")) { currency = value; return true; }
		if (key.equals("initialBalance")) { initialBalance = value; return true; }
		if (key.equals("language")) { language = value; return true; }
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
		saveValue(writer, "currency");
		saveValue(writer, "initialBalance");
	}

}
