package fr.crafter.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

//###################################################################################### RealConfig
public class RealConfig
{

	private final RealPlugin plugin;
	private final String fileName;

	public String language = "en"; 

	//---------------------------------------------------------------------------------- RealConfig
	public RealConfig(final RealPlugin plugin)
	{
		this(plugin, "config");
	}

	//---------------------------------------------------------------------------------- RealConfig
	public RealConfig(final RealPlugin plugin, String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//---------------------------------------------------------------------------------------- load
	public void load()
	{
		try {
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			String buffer;
			StringTokenizer line;
			while ((buffer = reader.readLine()) != null) {
				if (buffer.charAt(0) != '#') {
					line = new StringTokenizer(buffer, "=");
					if (line.countTokens() >= 2) {
						String key = line.nextToken().trim();
						String value = line.nextToken().trim();
						try {
							getClass().getField(key).set(this, value);
						} catch (Exception e) {
							plugin.log.warning(
								"[" + plugin.name + "] ignore configuration option "
								+ key + " (unknown)"
							);
						}
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.warning(
				"[" + plugin.name + "] Needs plugins/" + plugin.name + "/"
				+ fileName + ".cfg file (will auto-create)"
			);
			save();
		}
		plugin.language = language;
	}

	//---------------------------------------------------------------------------------------- save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			this.saveValues(writer);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe(
				"[" + plugin.name + "]"
				+ " Could not save plugins/" + plugin.name + "/" + fileName + ".cfg file"
			);
		}
	}

	//---------------------------------------------------------------------------------------- save
	protected void saveValue(BufferedWriter writer, String field)
	{
		try {
			writer.write(field + "=" + getClass().getField(field).get(this) + "\n");
		} catch (Exception e) {
			plugin.log.severe(
				"[" + plugin.name + "] config field " + field + " does not exist,"
				+ " could not save value into " + fileName + ".cfg file"
			);
		}
	}

	//---------------------------------------------------------------------------------------- save
	protected void saveValues(BufferedWriter writer)
	{
		saveValue(writer, "language");
	}

}