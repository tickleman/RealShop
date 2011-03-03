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
	private double totalPrice = (double)0;
	private boolean cancelAll = false;

	public ArrayList<RealItemStack> canceledLines = null;
	public ArrayList<RealShopTransactionLine> transactionLines = null;

	//##################################################################################### PRIVATE

	//------------------------------------------------------------------------- RealShopTransaction
	private RealShopTransaction(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile
	) {
		this.plugin = plugin;
		this.playerName = playerName;
		this.shopPlayerName = shopPlayerName;
		this.itemStackHashMap = itemStackHashMap;
		this.pricesFile = pricesFile;
	}

	//###################################################################################### PUBLIC

	//-------------------------------------------------------------------------------------- create
	public static RealShopTransaction create(
		RealShopPlugin plugin,
		String playerName,
		String shopPlayerName,
		RealItemStackHashMap itemStackHashMap,
		RealPricesFile pricesFile
	) {
		return new RealShopTransaction(
			plugin,
			playerName,
			shopPlayerName,
			itemStackHashMap,
			pricesFile
		);
	}

	//------------------------------------------------------------------------------- getTotalPrice
	public double getTotalPrice()
	{
		return totalPrice;
	}

	//---------------------------------------------------------------------------------- isCanceled
	public boolean isCanceled()
	{
		return cancelAll;
	}

	//--------------------------------------------------------------------------------- prepareBill
	public RealShopTransaction prepareBill(RealShop shop)
	{
		// Initialization
		cancelAll = false;
		canceledLines = new ArrayList<RealItemStack>();
		transactionLines = new ArrayList<RealShopTransactionLine>();
		double totalPrice = (double)0;
		// create lines and canceled lines
		Iterator<RealItemStack> iterator = itemStackHashMap.getContents().iterator();
		while (iterator.hasNext()) {
			RealItemStack itemStack = iterator.next();
			int amount = itemStack.getAmount();
			int typeId = itemStack.getTypeId();
			RealPrice price = pricesFile.getPrice(typeId);
			if (
				(price == null)
				|| ((amount > 0) && !shop.isItemBuyAllowed(typeId))
				|| ((amount < 0) && !shop.isItemSellAllowed(typeId))
				|| ((plugin.config.shopDamagedItems.equals("false")) && (itemStack.getDurability() != 0))
				|| ((plugin.config.shopMarketItemsOnly.equals("true")) && !plugin.marketFile.prices.containsKey(typeId))
			) {
				canceledLines.add(itemStack);
			} else {
				RealShopTransactionLine transactionLine = new RealShopTransactionLine(
						itemStack, price
				); 
				transactionLines.add(transactionLine);
				totalPrice += transactionLine.getLinePrice();
			}
		}
		// if total amount exceeds available player amount, then cancel all
		this.totalPrice = Math.ceil(totalPrice * (double)100) / (double)100;
		if (
			this.totalPrice > plugin.realEconomy.getBalance(playerName)
			|| (-this.totalPrice) > plugin.realEconomy.getBalance(shopPlayerName)
		) {
			cancelAll = true;
		}
		return this;
	}

	//------------------------------------------------------------------------------------ toString
	public String toString()
	{
		String result = "";
		{
			// header
			result += "<" + playerName + "> transaction details :\n";
		}
		String prefix = cancelAll ? "-CANCEL- " : "-VALID- ";
		{
			// canceled lines
			Iterator<RealItemStack> iterator = canceledLines.iterator();
			while (iterator.hasNext()) {
				RealItemStack itemStack = iterator.next();  
				result += "-CANCEL- " + plugin.dataValuesFile.getName(itemStack.getTypeId())
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
				result += prefix + plugin.dataValuesFile.getName(transactionLine.getTypeId()) + ": "
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
