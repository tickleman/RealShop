package fr.crafter.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

//############################################################################# RealTranslationFile
public class RealTranslationFile
{

	private final String fileName;

	private HashMap<String, String> translations	= new HashMap<String, String>();

	private final JavaPlugin plugin;

	// -------------------------------------------------------------------------- RealTranslationFile
	public RealTranslationFile(final JavaPlugin plugin)
	{
		this(plugin, "en");
	}

	// -------------------------------------------------------------------------- RealTranslationFile
	public RealTranslationFile(final JavaPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	// ----------------------------------------------------------------------------------------- load
	public RealTranslationFile load()
	{
		translations.clear();
		RealTools.renameFile(
			"plugins/" + plugin.getDescription().getName() + "/" + fileName + ".lang",
			"plugins/" + plugin.getDescription().getName() + "/" + fileName + ".lang.txt"
		);
		if (!RealTools.fileExists(
			"plugins/" + plugin.getDescription().getName() + "/" + fileName + ".lang.txt"
		)) {
			RealTools.extractDefaultFile(plugin, fileName + ".lang.txt");
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
				"plugins/" + plugin.getDescription().getName() + "/" + fileName + ".lang.txt"
			));
			String buffer;
			StringTokenizer line;
			while ((buffer = reader.readLine()) != null) {
				if ((buffer.length() > 0) && (buffer.charAt(0) != '#')) {
					line = new StringTokenizer(buffer, "=");
					if (line.countTokens() >= 2) {
						String key = line.nextToken();
						String value = line.nextToken();
						if (!key.equals("") && !value.equals("")) {
							translations.put(key, value);
						}
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			if (fileName.equals("en")) {
				plugin
					.getServer()
					.getLogger()
					.log(
						Level.INFO,
						"You can create plugins/" + plugin.getDescription().getName() + "/" + fileName
							+ ".lang.txt file" + " to change texts"
					);
			} else {
				plugin
					.getServer()
					.getLogger()
					.log(
						Level.WARNING,
						"Needs plugins/" + plugin.getDescription().getName() + "/" + fileName + ".lang.txt file"
							+ " (check your language configuration)"
					);
			}
		}
		return this;
	}

	// ------------------------------------------------------------------------------------------- tr
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
