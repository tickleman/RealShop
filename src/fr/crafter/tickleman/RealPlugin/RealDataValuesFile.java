package fr.crafter.tickleman.RealPlugin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

//###################################################################################### PricesFile
public class RealDataValuesFile
{

	private final String fileName;

	private HashMap<String, String> names = new HashMap<String, String>();

	private final RealPlugin plugin;

	private HashMap<String, String> recipes = new HashMap<String, String>();

	//-------------------------------------------------------------------------------- DataValuesFile
	public RealDataValuesFile(final RealPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//---------------------------------------------------------------------------------------- getIds
	/**
	 * Get full id list into a "typeId:damageValue" array
	 */
	public String[] getIds()
	{
		String[] ids = new String[names.size()];
		int i = 0;
		Iterator<String> iterator = names.keySet().iterator();
		while (iterator.hasNext()) {
			ids[i++] = iterator.next();
		}
		return ids;
	}

	//---------------------------------------------------------------------------------- getIdIerator
	public Iterator<String> getIdsIterator()
	{
		return names.keySet().iterator();
	}

	//------------------------------------------------------------------------------------------- get
	/**
	 * Get name for given item type id
	 * Returns "#typeId" if no name known
	 */
	public String getName(String typeIdDamage)
	{
		String result = names.get(typeIdDamage);
		if (result == null) {
			result = "#" + typeIdDamage;
		}
		return result;
	}

	//------------------------------------------------------------------------------------------- get
	/**
	 * Get main recipe for given item type id
	 * Returns empty string if no recipe known 
	 */
	public String getRecipe(String typeIdDamage)
	{
		String result = recipes.get(typeIdDamage);
		if (result == null) {
			result = "";
		}
		return result;
	}

	//------------------------------------------------------------------------------------------ load
	/**
	 * Load data values file from hard drive
	 */
	public RealDataValuesFile load()
	{
		RealTools.renameFile(
			"plugins/" + plugin.name + "/" + fileName + ".cfg",
			"plugins/" + plugin.name + "/" + fileName + ".txt"
		);
		if (!RealTools.fileExists("plugins/" + plugin.name + "/" + fileName + ".txt")) {
			RealTools.extractDefaultFile(plugin, fileName + ".txt");
		}
		try {
			names.clear();
			recipes.clear();
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			String buffer;
			StringTokenizer line;
			String typeIdDamage;
			String typeName;
			String recipe;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, ";");
				if (line.countTokens() >= 2) {
					try {
						typeIdDamage = line.nextToken().trim();
						typeName = line.nextToken().trim();
						recipe = line.hasMoreTokens() ? line.nextToken().trim() : "";  
						names.put(typeIdDamage, typeName);
						if (!recipe.equals("")) {
							recipes.put(typeIdDamage, recipe);
						}
					} catch (Exception e) {
						// when some typeId are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.severe("Needs plugins/" + plugin.name + "/" + fileName + ".txt file");
		}
		return this;
	}

}
