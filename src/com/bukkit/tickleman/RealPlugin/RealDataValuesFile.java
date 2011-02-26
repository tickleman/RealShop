package com.bukkit.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

//###################################################################################### PricesFile
public class RealDataValuesFile
{

	private final RealPlugin plugin;
	private final String fileName;
	private HashMap<Integer, String> names = new HashMap<Integer, String>();
	private HashMap<Integer, String> recipes = new HashMap<Integer, String>();

	//------------------------------------------------------------------------------ DataValuesFile
	public RealDataValuesFile(final RealPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//---------------------------------------------------------------------------------------- load
	/**
	 * Load data values file from disk
	 */
	public void load()
	{
		try {
			names.clear();
			recipes.clear();
			BufferedReader reader = new BufferedReader(
					new FileReader("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			String buffer;
			StringTokenizer line;
			int typeId;
			String typeName;
			String recipe;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, ";");
				if (line.countTokens() >= 2) {
					try {
						typeId = Integer.parseInt(line.nextToken().trim());
						typeName = line.nextToken().trim();
						recipe = line.hasMoreTokens() ? line.nextToken().trim() : "";  
						names.put(typeId, typeName);
						if (recipe != "") {
							recipes.put(typeId, recipe);
						}
					} catch (Exception e) {
						// when some typeId are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.severe("Needs plugins/" + plugin.name + "/" + fileName + ".cfg file");
		}
	}

	//-------------------------------------------------------------------------------------- getIds
	/**
	 * Get full id list into an integer array
	 */
	public int[] getIds()
	{
		int[] ids = new int[names.size()];
		int i = 0;
		Iterator<Integer> iterator = names.keySet().iterator();
		while (iterator.hasNext()) {
			ids[i++] = iterator.next();
		}
		return ids;
	}

	//----------------------------------------------------------------------------------------- get
	/**
	 * Get name for given item type id
	 * Returns "#typeId" if no name known
	 */
	public String getName(Integer typeId)
	{
		String result = names.get(typeId);
		if (result == null) {
			result = "#" + typeId.toString();
		}
		return result;
	}

	//----------------------------------------------------------------------------------------- get
	/**
	 * Get main recipe for given item type id
	 * Returns empty string if no recipe known 
	 */
	public String getRecipe(Integer typeId)
	{
		String result = recipes.get(typeId);
		if (result == null) {
			result = "";
		}
		return result;
	}

}
