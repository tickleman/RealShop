package fr.crafter.tickleman.RealPlugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

//######################################################################################### RealLog
public class RealLog
{

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final Logger globalLog = Logger.getLogger("Minecraft");

	private final String logFile;

	private final RealPlugin plugin;

	//------------------------------------------------------------------------------------- RealLog
	public RealLog(RealPlugin plugin)
	{
		this.plugin = plugin;
		logFile = "plugins/" + plugin.name + "/" + plugin.name.toLowerCase() + ".log";
	}

	//---------------------------------------------------------------------------------------- date
	private String date()
	{
		return dateFormat.format(new Date());
	}

	//--------------------------------------------------------------------------------------- debug
	public void debug(String text)
	{
		log("DEBUG", text);
	}

	//--------------------------------------------------------------------------------------- debug
	public void debug(String text, boolean global)
	{
		log("DEBUG", text, global);
	}

	//--------------------------------------------------------------------------------------- error
	public void error(String text)
	{
		log("ERROR", text);
	}

	//--------------------------------------------------------------------------------------- error
	public void error(String text, boolean global)
	{
		log("ERROR", text, global);
	}

	//---------------------------------------------------------------------------------------- info
	public void info(String text)
	{
		log("INFO", text);
	}

	//---------------------------------------------------------------------------------------- info
	public void info(String text, boolean global)
	{
		log("INFO", text, global);
	}

	//----------------------------------------------------------------------------------------- log
	private void log(String mark, String text)
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.write(date() + " [" + mark + "] " + text + "\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			globalLog.severe(
				"[" + plugin.name + "] Could not write into log file " + logFile
				+ " file : [" + mark + "] " + text
			);
		}
	}

	//----------------------------------------------------------------------------------------- log
	private void log(String mark, String text, boolean global)
	{
		log(mark, text);
		if (global) {
			if (mark.equals("INFO")) {
				globalLog.info("[" + plugin.name + "] " + text);
			} else if (mark.equals("WARNING")) {
				globalLog.warning("[" + plugin.name + "] " + text);
			} else if (mark.equals("SEVERE")) {
				globalLog.severe("[" + plugin.name + "] " + text);
			} else if (mark.equals("ERROR")) {
				globalLog.info("[ERROR] [" + plugin.name + "] " + text);
			} else if (mark.equals("DEBUG")) {
				globalLog.info("[DEBUG] [" + plugin.name + "] " + text);
			}
			
		}
	}
	
	//-------------------------------------------------------------------------------------- severe
	public void severe(String text)
	{
		log("SEVERE", text, true);
	}

	//-------------------------------------------------------------------------------------- severe
	public void severe(String text, boolean global)
	{
		log("SEVERE", text, global);
	}

	//------------------------------------------------------------------------------------- warning
	public void warning(String text)
	{
		log("WARNING", text);
	}

	//------------------------------------------------------------------------------------- warning
	public void warning(String text, boolean global)
	{
		log("WARNING", text, global);
	}

}
