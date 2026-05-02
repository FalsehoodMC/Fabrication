package com.unascribed.fabrication.util.forgery_nonsense;

import com.unascribed.fabrication.interfaces.ResupplyingFurnaceCart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ForgeryFurnaceCartResupplying implements Predicate {
	//have this static in a mixin and forge will crap
	public static ThreadLocal<List<ResupplyingFurnaceCart>> fabrication$fmr$lastCart = new ThreadLocal<>();
	public static ForgeryFurnaceCartResupplying INSTANCE = new ForgeryFurnaceCartResupplying();
	@Override
	public boolean test(Object entity) {
		if(entity instanceof ResupplyingFurnaceCart) {
			List<ResupplyingFurnaceCart> list = ForgeryFurnaceCartResupplying.fabrication$fmr$lastCart.get();
			if (list == null) ForgeryFurnaceCartResupplying.fabrication$fmr$lastCart.set(list = new ArrayList<>());
			list.add((ResupplyingFurnaceCart) entity);
		}
		return true;
	}
}
