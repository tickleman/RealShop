package com.bukkit.tickleman.RealPlugin;

import org.bukkit.plugin.java.JavaPlugin;

//###################################################################################### RealPlugin
public class RealPlugin extends JavaPlugin
{

	public String author;
	public String name;
	public String version;

	public RealTranslationFile lang;
	public RealLog log;

	//------------------------------------------------------------------------------------ RealPlugin
	public RealPlugin(String author, String name, String version)
	{
		super();
		this.author = author;
		this.name = name;
		this.version = version;
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
		lang = new RealTranslationFile(this);
		lang.load();
		// enabled
		log.info("version [" + version + "] (" + author + ") loaded", true);
	}

}
