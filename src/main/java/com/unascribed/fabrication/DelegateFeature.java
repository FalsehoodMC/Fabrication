package com.unascribed.fabrication;

import com.unascribed.fabrication.support.Feature;

public class DelegateFeature implements Feature {

	private final Feature delegate;
	
	public DelegateFeature(String className) {
		try {
			delegate = (Feature)Class.forName(className).getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void apply() {
		delegate.apply();
	}

	@Override
	public boolean undo() {
		return delegate.undo();
	}

	@Override
	public String getConfigKey() {
		return delegate.getConfigKey();
	}

}
