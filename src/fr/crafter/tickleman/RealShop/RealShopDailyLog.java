package fr.crafter.tickleman.RealShop;

import java.util.HashMap;
import java.util.Iterator;

//################################################################################ RealShopDailyLog
public class RealShopDailyLog
{

	private final RealShopPlugin plugin;
	public HashMap<Integer, Integer> moves = new HashMap<Integer, Integer>();

	//------------------------------------------------------------------------------ RealShopDailyLog
	public RealShopDailyLog(final RealShopPlugin plugin)
	{
		this.plugin = plugin;
	}

	//------------------------------------------------------------------------------------------- add
	/**
	 * Add an amount of an item typeId to daily moves
	 * positive amount for buy, negative amount for sell
	 */
	public void add(final int typeId, final int amount)
	{
		Integer balance = moves.get(typeId);
		if (amount != 0) {
			if (balance == null) {
				moves.put(typeId, amount);
			} else if ((balance + amount) == 0) {
				moves.remove(typeId);
			} else {
				moves.put(typeId, balance + amount);
			}
		}
	}

	//-------------------------------------------------------------------------------- addTransaction
	/**
	 * Add all amounts from a validated transaction
	 */
	public void addTransaction(final RealShopTransaction transaction)
	{
		if (!transaction.isCanceled()) {
			Iterator<RealShopTransactionLine> iterator = transaction.transactionLines.iterator();
			while (iterator.hasNext()) {
				RealShopTransactionLine item = iterator.next();
				add(item.getTypeId(), item.getAmount());
			}
		}
	}

	//----------------------------------------------------------------------------------------- reset
	/**
	 * Resets moves log
	 */
	public void reset()
	{
		moves.clear();
	}

	//-------------------------------------------------------------------------------------- toString
	public String toString()
	{
		String result = "RealShopDailyLog status\n";
		Iterator<Integer> iterator = moves.keySet().iterator();
		while (iterator.hasNext()) {
			int typeId = iterator.next();
			int amount = moves.get(typeId);
			result += "- " + plugin.dataValuesFile.getName(typeId) + " x" + amount + "\n";
		}
		return result;
	}

}
