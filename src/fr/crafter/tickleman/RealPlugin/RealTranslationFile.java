package fr.crafter.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

//############################################################################# RealTranslationFile
public class RealTranslationFile
{

	private final String fileName;

	private HashMap<String, String> translations = new HashMap<String, String>();

	private final RealPlugin plugin;

	//--------------------------------------------------------------------------- RealTranslationFile
	public RealTranslationFile(final RealPlugin plugin)
	{
		this(plugin, "en");
	}
	
	//--------------------------------------------------------------------------- RealTranslationFile
	public RealTranslationFile(final RealPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//------------------------------------------------------------------------------------------ load
	public RealTranslationFile load()
	{
		translations.clear();
		RealTools.renameFile(
			"plugins/" + plugin.name + "/" + fileName + ".lang",
			"plugins/" + plugin.name + "/" + fileName + ".lang.txt"
		);
		if (!RealTools.fileExists("plugins/" + plugin.name + "/" + fileName + ".lang.txt")) {
			RealTools.extractDefaultFile(plugin, fileName + ".lang.txt");
		}
		try {
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".lang.txt")
			);
			String buffer;
			StringTokenizer line;
			while ((buffer = reader.readLine()) != null) {
				if ((buffer.length() > 0) && (buffer.charAt(0) != '#')) {
					line = new StringTokenizer(buffer, "=");
					if (line.countTokens() >= 2) {
						String key = line.nextToken().trim();
						String value = line.nextToken().trim();
						if (!key.equals("") && !value.equals("")) {
							translations.put(key, value);
						}
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			if (fileName.equals("en")) {
				plugin.log.info(
					"You can create plugins/" + plugin.name + "/" + fileName + ".lang.txt file"
					+ " to change texts"
				);
			} else {
				plugin.log.warning(
					"Needs plugins/" + plugin.name + "/" + fileName + ".lang.txt file"
					+ " (check your language configuration)"
				);
			}
		}
		return this;
	}

	//-------------------------------------------------------------------------------------------- tr
	public String tr(String text)
	{
		String translated = translations.get(text);
		if ((translated == null) || (translated.equals(""))) {
			return text;
		} else {
			return translated;
		}
	}

}
