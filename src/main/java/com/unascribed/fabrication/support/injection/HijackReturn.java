package com.unascribed.fabrication.support.injection;

import java.util.Optional;

public class HijackReturn {
	public static final HijackReturn TRUE = new HijackReturn(true);
	public static final HijackReturn FALSE = new HijackReturn(false);
	public static final HijackReturn OPTIONAL_EMPTY = new HijackReturn(Optional.empty());
	public Object object;

	public HijackReturn(Object object){
		this.object = object;
	}

}
