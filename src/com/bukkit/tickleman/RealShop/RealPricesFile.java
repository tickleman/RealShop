package com.bukkit.tickleman.RealShop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

//###################################################################################### PricesFile
public class RealPricesFile
{

	/** margin ratio to pay workers that craft items */
	public double WORKERS_RATIO = (double)1.1;

	/** commercial ration to calculate sell price from buy price */
	public double COMMERCIAL_RATIO = (double)0.9;

	/** minimal daily price decrease (0.05 means that you can go down to 5% of last price) */
	public double MIN_DAILY_RATIO = (double)0.05;

	/** maximal daily price increase (1.95 means that you can go up to 95% of last price) */
	public double MAX_DAILY_RATIO = (double)1.95;

	/** base amount of sold / purchased items quantity used for calculation */
	public double AMOUNT_RATIO = (double)5000;

	private final RealShopPlugin plugin;
	private final String fileName;
	
	public HashMap<Integer, RealPrice> prices = new HashMap<Integer, RealPrice>();

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
		plugin.log.info("dailyPricesCalculation" + (simulation ? " SIMULATION" : " REAL"));
		// take each item id that has had a movement today, and that has a price
		Iterator<Integer> iterator = dailyLog.moves.keySet().iterator();
		while (iterator.hasNext()) {
			int typeId = iterator.next();
			// recalculate price
			RealPrice price = prices.get(typeId);
			if (price != null) {
				int amount = dailyLog.moves.get(typeId);
				double ratio;
				if (amount < 0) {
					ratio = Math.max(MIN_DAILY_RATIO, (double)1 + ((double)amount / (double)AMOUNT_RATIO));
				} else {
					ratio = Math.min(MAX_DAILY_RATIO, (double)1 + ((double)amount / (double)AMOUNT_RATIO));
				}
				String log = "- " + plugin.dataValuesFile.getName(typeId) + " :"
					+ " amount " + amount + " ratio " + ratio
					+ " OLD " + price.sell + ", " + price.buy;
				price.buy = Math.ceil(
						(double)100 * Math.max((double)0.1, price.buy * ratio)
				) / (double)100;
				price.sell = Math.floor(
						(double)100 * Math.max((double)0.1, price.buy / COMMERCIAL_RATIO)
				) / (double)100;
				log += " NEW " + price.sell + ", " + price.buy;
				plugin.log.info(log);
			} else {
				plugin.log.info("- no market price for item " + plugin.dataValuesFile.getName(typeId));
			}
		}
		if (!simulation) {
			plugin.log.info("SAVE new prices into " + fileName + ".cfg");
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
	public RealPrice fromRecipe(int typeId)
	{
		String recipe = plugin.dataValuesFile.getRecipe(typeId);
		if (recipe == "") {
			return null;
		} else {
			RealPrice price = new RealPrice();
			System.out.println("recipe for " + typeId + " is " + recipe);
			// recurse security
			recurseSecurity++;
			if (recurseSecurity > 20) {
				plugin.log.severe("Recurse security error : " + typeId);
				return null;
			} else if (recurseSecurity > 15) {
				plugin.log.warning("Recurse security warning : " + typeId);
			}
			// resQty : result quantity
			double resQty = (double)1;
			if (recipe.indexOf("=") > 0) {
				try { resQty = Double.parseDouble(recipe.split("\\=")[1]); } catch (Exception e) {}
				recipe = recipe.substring(0, recipe.indexOf("="));
			}
			System.out.println("resQty = " + resQty + ", recipe = " + recipe);
			// sum of components
			String[] sum = recipe.split("\\+");
			for (int sumIdx = 0; sumIdx < sum.length; sumIdx++) {
				String comp = sum[sumIdx];
				System.out.println("- component is " + comp);
				// mulQty : multiplier
				int mulQty = 1;
				if (comp.indexOf("*") > 0) {
					try { mulQty = Integer.parseInt(comp.split("\\*")[1]); } catch (Exception e) {}
					comp = comp.substring(0, comp.indexOf("*"));
				}
				System.out.println("  mulQty = " + mulQty);
				// divQty : divider
				int divQty = 1;
				if (comp.indexOf("/") > 0) {
					try { divQty = Integer.parseInt(comp.split("\\/")[1]); } catch (Exception e) {}
					comp = comp.substring(0, comp.indexOf("/"));
				}
				System.out.println("  divQty = " + divQty);
				// compId : component type Id
				int compId = 0;
				try { compId = Integer.parseInt(comp); } catch (Exception e) {}
				System.out.println("  comp = " + comp + ", compId = " + compId);
				// calculate price
				RealPrice compPrice = getPrice(compId);
				if (compPrice == null) {
					price = null;
					break;
				} else {
					price.buy += Math.ceil((double)100 * compPrice.getBuy() * (double)mulQty / (double)divQty) / (double)100;
					price.sell += Math.floor((double)100 * compPrice.getSell() * (double)mulQty / (double)divQty) / (double)100;
				}
				System.out.println("  sum price = buy " + price.buy + ", sell " + price.sell);
			}
			if (price != null) {
				// round final price
				System.out.println("  divide by resQty and multiply by RATIO " + WORKERS_RATIO);
				System.out.println("  divide by resQty and multiply by RATIO " + WORKERS_RATIO);
				price.buy = Math.ceil(price.buy / resQty * (double)100 * WORKERS_RATIO) / (double)100;
				price.sell = Math.floor(price.sell / resQty * (double)100 * WORKERS_RATIO) / (double)100;
				System.out.println("  RESULT PRICE = buy " + price.buy + ", sell " + price.sell);
			}
			recurseSecurity--;
			return price;
		}
	}

	//-------------------------------------------------------------------------------------- getPrice
	public RealPrice getPrice(int typeId)
	{
		RealPrice price = prices.get(typeId);
		if (price == null) {
			price = fromRecipe(typeId);
		}
		return price; 
	}

	//------------------------------------------------------------------------------------------ load
	public void load()
	{
		try {
			prices.clear();
			BufferedReader reader = new BufferedReader(
					new FileReader("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			String buffer;
			StringTokenizer line;
			int typeId;
			RealPrice price;
			while ((buffer = reader.readLine()) != null) {
				line = new StringTokenizer(buffer, ";");
				if (line.countTokens() >= 3) {
					
					try {
						typeId = Integer.parseInt(line.nextToken().trim());
						price = new RealPrice(
							Double.parseDouble(line.nextToken().trim()),
							Double.parseDouble(line.nextToken().trim())
						);
						prices.put(typeId, price);
					} catch (Exception e) {
						// when some values are not number, then ignore
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			plugin.log.severe("Needs plugins/" + plugin.name + "/" + fileName + ".cfg file");
		}
	}

	//------------------------------------------------------------------------------------------ save
	public void save()
	{
		try {
			BufferedWriter writer = new BufferedWriter(
				new FileWriter("plugins/" + plugin.name + "/" + fileName + ".cfg")
			);
			writer.write("item;buy;sell;name\n");
			Iterator<Integer> iterator = prices.keySet().iterator();
			while (iterator.hasNext()) {
				Integer typeId = iterator.next();
				RealPrice price = prices.get(typeId);
				writer.write(
					typeId + ";"
					+ price.buy + ";"
					+ price.sell + ";"
					+ plugin.dataValuesFile.getName(typeId)
					+ "\n"
				);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			plugin.log.severe("Could not save plugins/" + plugin.name + "/" + fileName + ".cfg file");
		}
	}

}
