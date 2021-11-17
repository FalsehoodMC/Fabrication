package com.unascribed.fabrication.logic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.unascribed.fabrication.FabricationMod;

import com.google.common.collect.Maps;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class PingPrivacyPersistentState extends PersistentState {

	private static final String name = FabricationMod.MOD_NAME_LOWER+"_ping_privacy";

	private final Map<InetAddress, Long> knownIps = Maps.newHashMap();
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();

	public PingPrivacyPersistentState() {
		super(name);
	}

	public static PingPrivacyPersistentState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(PingPrivacyPersistentState::new, name);
	}

	public void addKnownIp(InetAddress addr) {
		try {
			rwl.writeLock().lock();
			knownIps.put(addr, System.currentTimeMillis());
		} finally {
			rwl.writeLock().unlock();
		}
		markDirty();
	}

	public boolean isKnownAndRecent(InetAddress addr) {
		try {
			rwl.readLock().lock();
			return isRecent(knownIps.getOrDefault(addr, 0L));
		} finally {
			rwl.readLock().unlock();
		}
	}

	private boolean isRecent(long time) {
		return System.currentTimeMillis()-time < TimeUnit.DAYS.toMillis(7);
	}

	@Override
	public void fromTag(NbtCompound tag) {
		knownIps.clear();
		NbtList li = tag.getList("KnownIPs", NbtType.COMPOUND);
		for (int i = 0; i < li.size(); i++) {
			NbtCompound c = li.getCompound(i);
			long time = c.getLong("Time");
			if (!isRecent(time)) {
				// don't load it, it'll get dropped on next save
				continue;
			}
			InetAddress addr;
			try {
				addr = InetAddress.getByAddress(c.getByteArray("IP"));
			} catch (UnknownHostException e) {
				// ????????
				continue;
			}
			knownIps.put(addr, time);
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		NbtList li = new NbtList();
		for (Map.Entry<InetAddress, Long> en : knownIps.entrySet()) {
			NbtCompound c = new NbtCompound();
			c.putByteArray("IP", en.getKey().getAddress());
			c.putLong("Time", en.getValue());
			li.add(c);
		}
		tag.put("KnownIPs", li);
		return tag;
	}

}
