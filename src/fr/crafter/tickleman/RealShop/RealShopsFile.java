package fr.crafter.tickleman.RealShop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.block.Block;

import fr.crafter.tickleman.RealPlugin.RealTools;

//################################################################################### RealShopsFile
public class RealShopsFile
{

	private final RealShopPlugin plugin;
	private final String fileName = "shops";

	/** Shops list : "world;x;y;z" => RealShop */
	public HashMap<String, RealShop> shops = new HashMap<String, RealShop>();

	//--------------------------------------------------------------------------------- RealShopsFile
	public RealShopsFile(final RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	//---------------------------------------------------------------------------------------- isShop
	public boolean isShop(Block block)
	{
		return (shopAt(block) != null);
	}

	//---------------------------------------------------------------------------------------- shopAt
	public RealShop shopAt(Block block)
	{
		return shopAt(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}

	//---------------------------------------------------------------------------------------- shopAt
	public RealShop shopAt(World world, int x, int y, int z)
	{
		return shopAt(world.getName(), x, y, z); 
	}

	//---------------------------------------------------------------------------------------- shopAt
	public RealShop shopAt(String world, int x, int y, int z)
	{
		return shopAt(world + ";" + x + ";" + y + ";" + z);
	}

	//---------------------------------------------------------------------------------------- shopAt
	public RealShop shopAt(String key)
	{
		return shops.get(key);
	}

	//------------------------------------------------------------------------------------------ load
	public void load()
	{
		RealTools.renameFile(
			"plugins/" + plugin.name + "/" + fileName + ".cfg",
			"plugins/" + plugin.name + "/" + fileName + ".txt"
		);
		try {
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			String buffer;
			while ((buffer = reader.readLine()) != null) {
				String[] line = buffer.split(";");
				if (line.length > 4) {
					try {
						String world = line[0].trim();
						Integer posX = Integer.parseInt(line[1].trim());
						Integer posY = Integer.parseInt(line[2].trim());
						Integer posZ = Integer.parseInt(line[3].trim());
						String player = line[4].trim();
						String key = world + ";" + posX + ";" + posY + ";" + posZ;
						RealShop shop = new RealShop(world, posX, posY, posZ, player);
						if (line.length > 5) {
							shop.buyOnly = RealShop.csvToHashMap(line[5].trim());
						}
						if (line.length > 6) {
							shop.sellOnly = RealShop.csvToHashMap(line[6].trim());
						}
						if (line.length > 7) {
							shop.buyExclude = RealShop.csvToHashMap(line[7].trim());
						}
						if (line.length > 8) {
							shop.sellExclude = RealShop.csvToHashMap(line[8].trim());
						}
						if (line.length > 9) {
							shop.name = line[9].trim();
						}
						shops.put(key, shop);
					} catch (Exception e) {
						// when some values are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.warning(
				"Needs plugins/" + plugin.name + "/" + fileName + ".txt file (will auto-create)"
			);
		}
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			Iterator<RealShop> iterator = shops.values().iterator();
			while (iterator.hasNext()) {
				RealShop shop = iterator.next();
				writer.write(
					shop.world + ";"
					+ shop.posX + ";"
					+ shop.posY + ";"
					+ shop.posZ + ";"
					+ shop.player + ";"
					+ RealShop.HashMapToCsv(shop.buyOnly) + ";"
					+ RealShop.HashMapToCsv(shop.sellOnly) + ";"
					+ RealShop.HashMapToCsv(shop.buyExclude) + ";"
					+ RealShop.HashMapToCsv(shop.sellExclude) + ";"
					+ shop.name
					+ "\n"
				);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe("Could not save plugins/" + plugin.name + "/" + fileName + ".txt file");
		}
	}

}
