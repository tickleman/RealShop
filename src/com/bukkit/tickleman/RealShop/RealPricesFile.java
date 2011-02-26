package com.bukkit.tickleman.RealShop;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

//###################################################################################### PricesFile
public class RealPricesFile
{

	public double RATIO = (double)1.1;

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

	//------------------------------------------------------------------------------------- dailyCalc
	/**
	 * Daily price calculation
	 * Takes care of :
	 * - the last day transactions log
	 * - the last items price
	 */
	public void dailyCalc(RealShopDailyLog dailyLog)
	{
		plugin.log.info("dailyCalc simulation");
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
					ratio = Math.max((double)0.05, (double)1 + ((double)amount / (double)5000));
				} else {
					ratio = Math.min((double)1.95, (double)1 + ((double)amount / (double)5000));
				}
				String log = "- " + plugin.dataValuesFile.getName(typeId) + " :"
					+ " amount " + amount + " ratio " + ratio
					+ " OLD " + price.sell + ", " + price.buy;
				price.sell = Math.round(
					Math.max((double)0.1, price.sell * ratio * (double)100)
				) / (double)100;
				price.buy = Math.round(
					Math.max((double)0.1, price.sell * 0.9 * (double)100)
				) / (double)100;
				log += " NEW " + price.sell + ", " + price.buy;
				plugin.log.info(log);
			}
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
			int resQty = 1;
			if (recipe.indexOf("=") > 0) {
				try { resQty = Integer.parseInt(recipe.split("\\=")[1]); } catch (Exception e) {}
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
					price.buy += compPrice.getBuy() * mulQty / divQty;
					price.sell += compPrice.getSell() * mulQty / divQty;
				}
				System.out.println("  sum price = buy " + price.buy+ ", sell " + price.sell);
			}
			if (price != null) {
				// round final price
				System.out.println("  divide by resQty and multiply by RATIO " + RATIO);
				System.out.println("  divide by resQty and multiply by RATIO " + RATIO);
				price.buy = Math.round(price.buy / resQty * 100 * RATIO) / 100;
				price.sell = Math.round(price.sell / resQty * 100 * RATIO) / 100;
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

}
