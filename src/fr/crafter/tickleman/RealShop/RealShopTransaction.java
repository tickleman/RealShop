package fr.crafter.tickleman.RealShop;

import java.util.ArrayList;
import java.util.Iterator;

import fr.crafter.tickleman.RealPlugin.RealItemStack;
import fr.crafter.tickleman.RealPlugin.RealItemStackHashMap;

//############################################################################# RealShopTransaction
public class RealShopTransaction
{

	private RealShopPlugin plugin;

	private String playerName;
	private String shopPlayerName;
	private RealItemStackHashMap itemStackHashMap;
	private RealPricesFile pricesFile;
	private RealPricesFile marketFile;
	private double totalPrice = (double)0;
	private boolean cancelAll = false;

	public ArrayList<RealShopTransactionLine> cancelledLines = null;
	public ArrayList<RealShopTransactionLine> transactionLines = null;

	//####################################################################################### PRIVATE

	//--------------------------------------------------------------------------- RealShopTransaction
	private RealShopTransaction(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile,
		RealPricesFile marketFile
	) {
		this.plugin = plugin;
		this.playerName = playerName;
		this.shopPlayerName = shopPlayerName;
		this.itemStackHashMap = itemStackHashMap;
		this.pricesFile = pricesFile;
		this.marketFile = marketFile;
	}

	//######################################################################################## PUBLIC

	//---------------------------------------------------------------------------------------- create
	public static RealShopTransaction create(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile,
		RealPricesFile marketFile
	) {
		return new RealShopTransaction(
			plugin, playerName, shopPlayerName, itemStackHashMap, pricesFile, marketFile
		);
	}

	//--------------------------------------------------------------------------------- getTotalPrice
	public double getTotalPrice()
	{
		return totalPrice;
	}

	//----------------------------------------------------------------------------------- isCancelled
	public boolean isCancelled()
	{
		return cancelAll;
	}

	//----------------------------------------------------------------------------------- prepareBill
	public RealShopTransaction prepareBill(RealShop shop)
	{
		// Initialization
		cancelAll = false;
		cancelledLines = new ArrayList<RealShopTransactionLine>();
		transactionLines = new ArrayList<RealShopTransactionLine>();
		double totalPrice = (double)0;
		// create lines and cancelled lines
		Iterator<RealItemStack> iterator = itemStackHashMap.getContents().iterator();
		while (iterator.hasNext()) {
			RealItemStack itemStack = iterator.next();
			int amount = itemStack.getAmount();
			String typeIdDamage = itemStack.getTypeIdDamage();
			RealPrice price = pricesFile.getPrice(typeIdDamage, marketFile);
			if (price == null) {
				price = marketFile.getPrice(typeIdDamage, null);
			}
			RealShopTransactionLine transactionLine = new RealShopTransactionLine(itemStack, price);
			if (
				(price == null)
				|| ((amount > 0) && !shop.isItemBuyAllowed(typeIdDamage))
				|| ((amount < 0) && !shop.isItemSellAllowed(typeIdDamage))
				|| (!shop.getFlag("damagedItems", plugin.config.shopDamagedItems.equals("true")) && (itemStack.getDurability() != 0))
				|| (shop.getFlag("marketItemsOnly", plugin.config.shopMarketItemsOnly.equals("true")) && !plugin.marketFile.prices.containsKey(typeIdDamage))
			) {
				if (price == null) {
					transactionLine.comment = "no price";
				} else if ((amount > 0) && !shop.isItemBuyAllowed(typeIdDamage)) {
					transactionLine.comment = "buy not allowed";
				} else if ((amount < 0) && !shop.isItemSellAllowed(typeIdDamage)) {
					transactionLine.comment = "sell not allowed";
				} else if (!shop.getFlag("damagedItems", plugin.config.shopDamagedItems.equals("true")) && (itemStack.getDurability() != 0)) {
					transactionLine.comment = "damaged item";
				} else if (shop.getFlag("marketItemsOnly", plugin.config.shopMarketItemsOnly.equals("true")) && !plugin.marketFile.prices.containsKey(typeIdDamage)) {
					transactionLine.comment = "not in market";
				}
				cancelledLines.add(transactionLine);
			} else {
				transactionLines.add(transactionLine);
				totalPrice += transactionLine.getLinePrice();
			}
		}
		// if total amount exceeds available player amount, then cancel all
		this.totalPrice = Math.ceil(totalPrice * 100.0) / 100.0;
		if (
			this.totalPrice > plugin.realEconomy.getBalance(playerName)
			|| (-this.totalPrice) > plugin.realEconomy.getBalance(shopPlayerName)
		) {
			cancelAll = true;
		}
		return this;
	}

	//-------------------------------------------------------------------------------------- toString
	public String toString()
	{
		String result = "";
		{
			// header
			result += "<" + playerName + "> transaction details :\n";
		}
		String prefix = cancelAll ? "-CANCEL- " : "-VALID- ";
		{
			// cancelled lines
			Iterator<RealShopTransactionLine> iterator = cancelledLines.iterator();
			while (iterator.hasNext()) {
				RealItemStack itemStack = iterator.next();  
				result += "-CANCEL- " + plugin.dataValuesFile.getName(itemStack.getTypeIdDamage())
					+ " x" + itemStack.getAmount() + " :"
					+ " cancelled line"
					+ "\n";
			}
		}
		{
			// transaction lines
			Iterator<RealShopTransactionLine> iterator = transactionLines.iterator();
			while (iterator.hasNext()) {
				RealShopTransactionLine transactionLine = iterator.next();
				String strGain, strSide;
				if (transactionLine.getAmount() < 0) {
					strSide = "sale";
					strGain = "profit";
				} else {
					strSide = "purchase";
					strGain = "expense";
				}
				result += prefix + plugin.dataValuesFile.getName(transactionLine.getTypeIdDamage()) + ": "
					+ strSide
					+ " x" + Math.abs(transactionLine.getAmount())
					+ " price " + transactionLine.getUnitPrice() + plugin.realEconomy.getCurrency()
					+ " " + strGain + " "
					+ Math.abs(transactionLine.getLinePrice()) + plugin.realEconomy.getCurrency()
					+ "\n";
			}
		}
		{
			// footer
			result += prefix + "total price = " + totalPrice + "\n";
			if (cancelAll) {
				result += "CANCELALL !\n";
			}
		}
		return result;
	}

}
