package fr.crafter.tickleman.RealShop;

import fr.crafter.tickleman.RealPlugin.RealItemStack;

//######################################################################### RealShopTransactionLine
public class RealShopTransactionLine extends RealItemStack 
{

	public String comment; 

	private double unitPrice;

	//--------------------------------------------------------------------- RealShopTransactionLine
	public RealShopTransactionLine(RealItemStack itemStack, RealPrice price)
	{
		super(itemStack.getTypeId(), itemStack.getAmount(), itemStack.getDurability());
		if (price == null) {
			unitPrice = 0;
		} else {
			unitPrice = ((getAmount() < 0) ? price.getSell() : price.getBuy());
		}
	}

	//-------------------------------------------------------------------------------- getLinePrice
	public double getLinePrice()
	{
		return Math.ceil((double)100 * getUnitPrice() * (double)getAmount()) / (double)100;
	}

	//-------------------------------------------------------------------------------- getUnitPrice
	public double getUnitPrice()
	{
		return unitPrice;
	}

}
