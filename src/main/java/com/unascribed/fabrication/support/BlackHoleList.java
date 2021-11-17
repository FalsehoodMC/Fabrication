package com.unascribed.fabrication.support;

import java.util.AbstractList;

/**
 * An empty list that accepts but silently ignores attempts to modify it.
 */
public class BlackHoleList<T> extends AbstractList<T> {

	private static final BlackHoleList<Object> INSTANCE = new BlackHoleList<>();

	public static <T> BlackHoleList<T> getInstance() {
		return (BlackHoleList<T>)INSTANCE;
	}

	private BlackHoleList() {}

	@Override
	public T get(int index) {
		throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean add(Object e) {
		return false;
	}

	@Override
	public void add(int index, Object element) {
		if (index != 0) throw new ArrayIndexOutOfBoundsException(index);
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@Override
	public T set(int index, Object element) {
		throw new ArrayIndexOutOfBoundsException(index);
	}

}
