package com.bukkit.tickleman.RealShop;

import com.bukkit.tickleman.RealPlugin.RealItemStack;

//######################################################################### RealShopTransactionLine
public class RealShopTransactionLine extends RealItemStack 
{

	private double unitPrice;

	//--------------------------------------------------------------------- RealShopTransactionLine
	public RealShopTransactionLine(RealItemStack itemStack, RealPrice price)
	{
		super(itemStack.getTypeId(), itemStack.getAmount(), itemStack.getDurability());
		unitPrice = ((getAmount() < 0) ? price.getSell() : price.getBuy());
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
