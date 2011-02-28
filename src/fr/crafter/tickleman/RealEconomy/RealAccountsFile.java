package fr.crafter.tickleman.RealEconomy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import fr.crafter.tickleman.RealPlugin.RealPlugin;

//################################################################################### RealShopsFile
public class RealAccountsFile
{

	private final RealPlugin plugin;
	private final String fileName = "accounts";

	/** Accounts list : "playerName" => (double)balance */
	public HashMap<String, Double> accounts = new HashMap<String, Double>();

	//--------------------------------------------------------------------------------- RealShopsFile
	public RealAccountsFile(final RealPlugin plugin)
	{
		this.plugin = plugin;
	}

	//------------------------------------------------------------------------------------------ load
	public void load()
	{
		try {
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			String buffer;
			StringTokenizer line;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, ";");
				if (line.countTokens() >= 2) {
					try {
						String playerName = line.nextToken().trim();
						Double balance = Double.parseDouble(line.nextToken().trim());
						accounts.put(playerName, balance);
					} catch (Exception e) {
						// when some values are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.warning(
				"Needs plugins/" + plugin.name + "/" + fileName + ".cfg file (will auto-create)"
			);
			save();
		}
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			Iterator<String> iterator = accounts.keySet().iterator();
			while (iterator.hasNext()) {
				String playerName = iterator.next();
				writer.write(
					playerName + ";"
					+ accounts.get(playerName) + "\n"
				);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe("Could not save plugins/" + plugin.name + "/" + fileName + ".cfg file");
		}
	}

}
