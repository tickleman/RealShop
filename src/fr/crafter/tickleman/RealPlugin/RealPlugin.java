package fr.crafter.tickleman.RealPlugin;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

//###################################################################################### RealPlugin
public class RealPlugin extends JavaPlugin
{

	/** Plugin identification (use it for your logs !) */
	public String author = "tickleman";
	public String name = "RealPlugin";
	public String version = "0.01";

	/** Translation links */
	public RealTranslationFile lang;
	public String language = "en";

	/** Use this log object to log, as the plugin name will automatically be added */
	public RealLog log;

	//------------------------------------------------------------------------------------ RealPlugin
	public RealPlugin()
	{
		super();
		log = new RealLog(this);
	}

	//------------------------------------------------------------------------------------- onDisable
	@Override
	public void onDisable()
	{
		// disable language file
		lang = null;
		// disabled
		log.info("version [" + version + "] (" + author + ") un-loaded", true);
	}

	//-------------------------------------------------------------------------------------- onEnable
	@Override
	public void onEnable()
	{
		// plugin information
		PluginDescriptionFile pdfFile = getDescription();
		author = pdfFile.getAuthors().get(0);
		name = pdfFile.getName();
		version = pdfFile.getVersion();
		// read language file
		lang = new RealTranslationFile(this, language);
		lang.load();
		// enabled
		log.info("version [" + version + "] (" + author + ") loaded", true);
	}

}
