package fr.crafter.tickleman.RealShop;

import java.util.HashMap;
import java.util.Iterator;

import fr.crafter.tickleman.RealPlugin.RealLog;

//################################################################################ RealShopDailyLog
public class RealShopDailyLog
{

	public HashMap<String, Integer> moves = new HashMap<String, Integer>();

	private final RealShopPlugin plugin;

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
	public void add(final String typeIdDamage, final int amount)
	{
		Integer balance = moves.get(typeIdDamage);
		if (amount != 0) {
			if (balance == null) {
				moves.put(typeIdDamage, amount);
			} else if ((balance + amount) == 0) {
				moves.remove(typeIdDamage);
			} else {
				moves.put(typeIdDamage, balance + amount);
			}
		}
	}

	//-------------------------------------------------------------------------------- addTransaction
	/**
	 * Add all amounts from a validated transaction
	 */
	public void addTransaction(final RealShopTransaction transaction)
	{
		if (!transaction.isCancelled()) {
			Iterator<RealShopTransactionLine> iterator = transaction.transactionLines.iterator();
			while (iterator.hasNext()) {
				RealShopTransactionLine item = iterator.next();
				String typeIdDamage = item.getTypeIdDurability();
				add(typeIdDamage, item.getAmount());
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

	//----------------------------------------------------------------------------------------- toLog
	public void toLog(RealLog log)
	{
		log.info("RealShopDailyLog status");
		for (String typeIdDamage : moves.keySet()) {
			int amount = moves.get(typeIdDamage);
			log.info(
				"- " + typeIdDamage + "(" + plugin.dataValuesFile.getName(typeIdDamage) + ") x" + amount
			);
		}
	}

}
