package fr.crafter.tickleman.RealShop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import fr.crafter.tickleman.RealPlugin.RealDataValuesFile;
import fr.crafter.tickleman.RealPlugin.RealItemStack;
import fr.crafter.tickleman.RealPlugin.RealTools;

//###################################################################################### PricesFile
public class RealPricesFile
{

	/** stored file name */
	private final String fileName;
	
	/** master plugin */
	private final RealShopPlugin plugin;

	/** prices list : typeId[:damageId] => RealPrice(buy, sell) */
	public HashMap<String, RealPrice> prices = new HashMap<String, RealPrice>();

	/** anti-recurse security flag for recipes */
	private int recurseSecurity = 0;
	
	//-------------------------------------------------------------------------------- RealPricesFile
	public RealPricesFile(final RealShopPlugin plugin, final String fileName)
	{
		this.plugin = plugin;
		this.fileName = fileName;
	}

	//------------------------------------------------------------------------ dailyPricesCalculation
	public void dailyPricesCalculation(RealShopDailyLog dailyLog)
	{
		dailyPricesCalculation(dailyLog, false);
	}

	//------------------------------------------------------------------------ dailyPricesCalculation
	/**
	 * Daily price calculation
	 * Takes care of :
	 * - the last day transactions log
	 * - the last items price
	 */
	public void dailyPricesCalculation(RealShopDailyLog dailyLog, boolean simulation)
	{
		plugin.log.info("dailyPricesCalculation (" + (simulation ? "SIMULATION" : "FOR REAL") + ")");
		// take each item id that has had a movement today, and that has a price
		Iterator<String> iterator = dailyLog.moves.keySet().iterator();
		while (iterator.hasNext()) {
			String typeIdDamage = iterator.next();
			// recalculate price
			RealPrice price = prices.get(typeIdDamage);
			if (price != null) {
				int amount = dailyLog.moves.get(typeIdDamage);
				double ratio;
				if (amount < 0) {
					ratio = Math.max(
						plugin.config.minDailyRatio,
						(double)1 + ((double)amount / plugin.config.amountRatio)
					);
				} else {
					ratio = Math.min(
						plugin.config.maxDailyRatio,
						(double)1 + ((double)amount / plugin.config.amountRatio)
					);
				}
				String log = "- "
					+ typeIdDamage + "(" + plugin.dataValuesFile.getName(typeIdDamage) + ") :"
					+ " amount " + amount + " ratio " + ratio
					+ " OLD " + price.buy + ", " + price.sell;
				price.buy = Math.ceil(
					(double)100 * Math.min(plugin.config.maxItemPrice, Math.max(
						plugin.config.minItemPrice, price.buy * ratio
					))
				) / (double)100;
				price.sell = Math.floor(
					(double)100 * Math.min(plugin.config.maxItemPrice, Math.max(
						plugin.config.minItemPrice, price.buy * plugin.config.buySellRatio
					))
				) / (double)100;
				log += " NEW " + price.buy + ", " + price.sell;
				plugin.log.info(log);
			} else {
				plugin.log.info("- no market price for item " + plugin.dataValuesFile.getName(typeIdDamage));
			}
		}
		if (!simulation) {
			plugin.log.info("- SAVE new prices into " + fileName + ".txt");
			save();
		}
	}

	//------------------------------------------------------------------------------------ fromRecipe
	/**
	 * Calculate Price using crafting recipes
	 * - returns null if no price for any component
	 * - recurse if necessary
	 * recipe format : typeId[*mulQty][/divQty][+...][=resQty]
	 * recipe samples :
	 * - stick (typeId=280) : 5*2=4 : 2 wooden planks gives you 4 sticks
	 * - diamond hoe (typeId=293) : 280*2+264*2 : 2 sticks and 2 diamonds give you 1 diamond hoe
	 */
	public RealPrice fromRecipe(String typeIdDamage, RealPricesFile marketFile)
	{
		String recipe = plugin.dataValuesFile.getRecipe(typeIdDamage);
		if (recipe.equals("")) {
			return null;
		} else {
			RealPrice price = new RealPrice();
			// recurse security
			recurseSecurity++;
			if (recurseSecurity > 20) {
				plugin.log.severe("Recurse security error : " + typeIdDamage);
				return null;
			} else if (recurseSecurity > 15) {
				plugin.log.warning("Recurse security warning : " + typeIdDamage);
			}
			// resQty : result quantity
			double resQty = (double)1;
			if (recipe.indexOf("=") > 0) {
				try { resQty = Double.parseDouble(recipe.split("\\=")[1]); } catch (Exception e) {}
				recipe = recipe.substring(0, recipe.indexOf("="));
			}
			// sum of components
			String[] sum = recipe.split("\\+");
			for (int sumIdx = 0; sumIdx < sum.length; sumIdx++) {
				String comp = sum[sumIdx];
				// mulQty : multiplier
				int mulQty = 1;
				if (comp.indexOf("*") > 0) {
					try { mulQty = Integer.parseInt(comp.split("\\*")[1]); } catch (Exception e) {}
					comp = comp.substring(0, comp.indexOf("*"));
				}
				// divQty : divider
				int divQty = 1;
				if (comp.indexOf("/") > 0) {
					try { divQty = Integer.parseInt(comp.split("\\/")[1]); } catch (Exception e) {}
					comp = comp.substring(0, comp.indexOf("/"));
				}
				// compId : component type Id
				String compId = "0";
				try { compId = comp; } catch (Exception e) {}
				// calculate price
				RealPrice compPrice = getPrice(compId, marketFile);
				if (compPrice == null) {
					price = null;
					break;
				} else {
					price.buy += Math.ceil(
						(double)100 * compPrice.getBuy() * (double)mulQty / (double)divQty
					) / (double)100;
					price.sell += Math.floor(
						(double)100 * compPrice.getSell() * (double)mulQty / (double)divQty
					) / (double)100;
				}
			}
			if (price != null) {
				// round final price
				price.buy = Math.ceil(price.buy / resQty * (double)100 * plugin.config.workForceRatio)
					/ (double)100;
				price.sell = Math.floor(price.sell / resQty * (double)100 * plugin.config.workForceRatio)
					/ (double)100;
			}
			recurseSecurity--;
			return price;
		}
	}

	//-------------------------------------------------------------------------------------- getPrice
	public RealPrice getPrice(String typeIdDamage, RealPricesFile marketFile)
	{
		return getPrice(typeIdDamage, marketFile, true);
	}

	//-------------------------------------------------------------------------------------- getPrice
	public RealPrice getPrice(String typeIdDamage, RealPricesFile marketFile, boolean recipe)
	{
		RealPrice price = prices.get(typeIdDamage);
		if (price == null) {
			if (marketFile != null) {
				// market file price
				price = marketFile.getPrice(typeIdDamage, null, false);
			}
			if ((price == null) && recipe) {
				// recipe price
				price = fromRecipe(typeIdDamage, marketFile);
			}
			if ((price == null) && typeIdDamage.contains(":")) {
				// item without damage code price
				Integer typeId = Integer.parseInt(typeIdDamage.split(":")[0]);
				Short damage = Short.parseShort(typeIdDamage.split(":")[1]);
				price = getPrice(typeId.toString(), marketFile, recipe);
				if (price != null) {
					// apply a ratio from the damage amount
					try {
						price.damagedBuy = Math.max(
							0, price.buy - (price.buy * damage / RealItemStack.typeIdMaxDamage(typeId))
						);
					} catch(Exception e) {
					}
					try {
						price.damagedSell = Math.max(
							0, price.sell - (price.sell * damage / RealItemStack.typeIdMaxDamage(typeId))
						);
					} catch(Exception e) {
					}
				}
			}
		}
		return price; 
	}

	//------------------------------------------------------------------------------------------ load
	public RealPricesFile load()
	{
		boolean willSave = false;
		RealTools.renameFile(
			"plugins/" + plugin.name + "/" + fileName + ".cfg",
			"plugins/" + plugin.name + "/" + fileName + ".txt"
		);
		if (
			(fileName.equals("market"))
			&& !RealTools.fileExists("plugins/" + plugin.name + "/" + fileName + ".txt")
		) {
			RealTools.extractDefaultFile(plugin, fileName + ".txt");
			willSave = true;
		}
		try {
			prices.clear();
			BufferedReader reader = new BufferedReader(
				new FileReader("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			String buffer;
			StringTokenizer line;
			String typeIdDamage;
			RealPrice price;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, ";");
				if (line.countTokens() >= 3) {
					try {
						typeIdDamage = line.nextToken().trim();
						price = new RealPrice(
							Double.parseDouble(line.nextToken().trim()),
							Double.parseDouble(line.nextToken().trim())
						);
						prices.put(typeIdDamage, price);
					} catch (Exception e) {
						// when some values are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			if (fileName.equals("market")) {
				plugin.log.severe("Needs plugins/" + plugin.name + "/" + fileName + ".txt file");
			}
		}
		if (willSave) {
			save();
		}
		return this;
	}

	//--------------------------------------------------------------------------- playerHasPricesFile
	public static boolean playerHasPricesFile(RealShopPlugin plugin, String player)
	{
		return RealTools.fileExists("plugins/" + plugin.name + "/" + player + ".prices.txt");
	}

	//------------------------------------------------------------------------------ playerPricesFile
	public static RealPricesFile playerPricesFile(
		RealShopPlugin plugin, String player, RealPricesFile defaultFile
	) {
		if (playerHasPricesFile(plugin, player) || (defaultFile == null)) {
			return new RealPricesFile(plugin, player + ".prices").load();
		} else {
			return defaultFile;
		}
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".txt")
			);
			writer.write("#item:dm;buy;sell;name\n");
			Iterator<String> iterator = prices.keySet().iterator();
			while (iterator.hasNext()) {
				String typeIdDamage = iterator.next();
				RealPrice price = prices.get(typeIdDamage);
				writer.write(
					typeIdDamage + ";"
					+ price.buy + ";"
					+ price.sell + ";"
					+ plugin.dataValuesFile.getName(typeIdDamage)
					+ "\n"
				);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe("Could not save plugins/" + plugin.name + "/" + fileName + ".txt file");
		}
		// Save all current values (including calculated prices) into currentValues.txt
		if (fileName.equals("market")) {
			try {
				RealDataValuesFile dataValues = new RealDataValuesFile(plugin, "dataValues").load();
				BufferedWriter writer = new BufferedWriter(
					new FileWriter("plugins/" + plugin.name + "/currentValues.txt")
				);
				writer.write("#item:dm;buy;sell;name\n");
				for (String typeIdDamage : dataValues.getIds()) {
					RealPrice price = getPrice(typeIdDamage, null);
					if (price != null) {
						writer.write(
							typeIdDamage + ";"
							+ price.buy + ";"
							+ price.sell + ";"
							+ plugin.dataValuesFile.getName(typeIdDamage)
							+ "\n"
						);
					} else {
						writer.write(
							typeIdDamage + ";0;0;"
							+ plugin.dataValuesFile.getName(typeIdDamage)
							+ "\n"
						);
					}
				}
				plugin.log.debug("END");
				writer.flush();
				writer.close();
			} catch (Exception e) {
				plugin.log.error("Could not save plugins/" + plugin.name + "/dataValues.txt file");
				plugin.log.error(e.getMessage());
				plugin.log.error(e.getStackTrace().toString());
			}
		}
	}

}
