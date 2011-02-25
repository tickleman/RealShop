package com.bukkit.tickleman.RealPlugin;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

//###################################################################################### RealPlugin
public class RealPlugin extends JavaPlugin
{

	protected final String author = "Tickleman";
	protected final String name = "RealPlugin";
	protected final String version = "0.1";

	public RealTranslationFile lang;
	public final Logger log;
	public RealLog realLog;

	//----------------------------------------------------------------------------------- onDisable
	@Override
	public void onDisable() {
		realLog = null;
		log.info("[" + name + "] version [" + version + "] (" + author + ") un-loaded");
	}

	//------------------------------------------------------------------------------------ onEnable
	@Override
	public void onEnable()
	{
		// read language file
		lang = new RealTranslationFile(this);
		lang.load();
		// construct realLog
		realLog = new RealLog(this);
		// enabled
		log.info("[" + name + " ] version [" + version + "] (" + author + ") loaded");
	}

}
