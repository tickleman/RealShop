package com.bukkit.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

//######################################################################### RealShopTranslationFile
public class RealTranslationFile
{

	private final RealPlugin plugin;
	private final String fileName;

	private HashMap<String, String> translations = new HashMap<String, String>();

	//--------------------------------------------------------------------- RealShopTranslationFile
	public RealTranslationFile(final RealPlugin plugin)
	{
		this(plugin, "en");
	}
	
	//--------------------------------------------------------------------- RealShopTranslationFile
	public RealTranslationFile(final RealPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//---------------------------------------------------------------------------------------- load
	public void load()
	{
		translations.clear();
		try {
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".lang")
			);
			String buffer;
			StringTokenizer line;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, "=");
				if (line.countTokens() >= 2) {
					String key = line.nextToken().trim();
					String value = line.nextToken().trim();
					if ((key != "") && (value != "")) {
						translations.put(key, value);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			if (fileName.equals("en")) {
				plugin.log.info(
					"[RealShop] You could create plugins/RealShop/" + fileName + ".lang file"
					+ " to change texts"
				);
			} else {
				plugin.log.warning(
					"[RealShop] Needs plugins/RealShop/" + fileName + ".lang file"
					+ " (check your language configuration)"
				);
			}
		}
	}

	//------------------------------------------------------------------------------------------ tr
	public String tr(String text)
	{
		String translated = translations.get(text);
		if ((translated == null) || (translated == "")) {
			return text;
		} else {
			return translated;
		}
	}

}
