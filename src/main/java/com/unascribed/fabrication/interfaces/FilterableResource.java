package com.unascribed.fabrication.interfaces;

import java.io.InputStream;

public interface FilterableResource {
	interface ResourceFilter {
		InputStream apply(InputStream inputStream);
	}
	void fabrication$applyFilter(ResourceFilter filter);
}
