package com.unascribed.fabrication.util;

import java.util.Optional;

import com.google.common.base.Objects;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Allows referencing registry items that may not have been registered yet in an efficient manner.
 */
public class Resolvable<T> {

	private static final ThreadLocal<Resolvable> MAP_KEY = ThreadLocal.withInitial(() -> new Resolvable<>(null, null, null));

	private Identifier id;
	private Registry<T> registry;

	private Optional<T> resolved;

	private final boolean resolvedImmediately;

	private Resolvable(Identifier id, Registry<T> registry, Optional<T> resolved) {
		this.id = id;
		this.registry = registry;
		this.resolved = resolved;
		this.resolvedImmediately = resolved != null && resolved.isPresent();
	}

	public Identifier getId() {
		return id;
	}

	public Optional<T> get() {
		if (resolved == null) throw new IllegalStateException("Cannot resolve a mapKey resolvable");
		if (!resolved.isPresent()) {
			resolved = registry.getOrEmpty(id);
		}
		return resolved;
	}

	public T getOrNull() {
		return get().orElse(null);
	}

	public boolean isResolved() {
		if (resolved == null) throw new IllegalStateException("Cannot resolve a mapKey resolvable");
		return resolved.isPresent();
	}

	public boolean wasResolvedImmediately() {
		if (resolved == null) throw new IllegalStateException("Cannot resolve a mapKey resolvable");
		return resolvedImmediately;
	}

	public static <T> Resolvable<T> of(Identifier id, Registry<T> registry) {
		return new Resolvable<>(id, registry, registry.getOrEmpty(id));
	}

	public static <T> Resolvable<T> mapKey(T obj, Registry<T> registry) {
		return mapKey(registry.getId(obj), registry);
	}

	public static <T> Resolvable<T> mapKey(Identifier id, Registry<T> registry) {
		Resolvable<T> r = MAP_KEY.get();
		r.id = id;
		r.registry = registry;
		return r;
	}

	@Override
	public String toString() {
		if (resolved == null) return "Resolvable(MapKey){id="+id+"}";
		return "Resolvable{id="+id+",resolved="+isResolved()+",resolvedImmediately="+resolvedImmediately+"}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((registry == null) ? 0 : registry.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Resolvable<?> other = (Resolvable<?>) obj;
		if (!Objects.equal(id, other.id)) return false;
		if (registry != other.registry) return false;
		return true;
	}

}
