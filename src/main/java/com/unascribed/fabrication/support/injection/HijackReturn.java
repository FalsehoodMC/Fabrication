package com.unascribed.fabrication.support.injection;

public class HijackReturn {
	public static final HijackReturn empty = new HijackReturn();

	public Object object;
	public boolean cancel;

	public HijackReturn(Object object){
		this.object = object;
		this.cancel = true;
	}

	HijackReturn(){
		this.cancel = false;
	}

}
