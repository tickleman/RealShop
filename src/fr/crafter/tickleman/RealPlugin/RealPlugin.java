package fr.crafter.tickleman.RealPlugin;

import org.bukkit.plugin.java.JavaPlugin;

//###################################################################################### RealPlugin
public class RealPlugin extends JavaPlugin
{

	/** Plugin identification (use it for your logs !) */
	public String author;
	public String name;
	public String version;

	/** Translation links */
	public RealTranslationFile lang;
	public String language = "en";

	/** Use this log object to log, as the plugin name will automatically be added */
	public RealLog log;

	//------------------------------------------------------------------------------------ RealPlugin
	public RealPlugin(String author, String name, String version)
	{
		super();
		this.author = author;
		this.name = name;
		this.version = version;
		RealTools.mkDir("plugins/" + name);
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
		// read language file
		lang = new RealTranslationFile(this, language);
		lang.load();
		// enabled
		log.info("version [" + version + "] (" + author + ") loaded", true);
	}

}
