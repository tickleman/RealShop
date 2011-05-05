package fr.crafter.tickleman.RealPlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

//###################################################################################### RealPlugin
public class RealPlugin extends JavaPlugin
{

	public String author;

	public RealTranslationFile lang;

	public String language = "en";

	public RealLog log;

	public String name;

	public String version;

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

	//--------------------------------------------------------------------------------- hasPermission
	public boolean hasPermission(Player player, String permission)
	{
		return player.isOp();
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
