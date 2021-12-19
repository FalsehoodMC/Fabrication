package com.unascribed.fabrication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.SpecialEligibility;

import com.google.common.base.Stopwatch;

public class FabLog {

	private static final Logger log = LogManager.getLogger(MixinConfigPlugin.isMet(SpecialEligibility.FORGE) ? "Forgery" : "Fabrication");

	// Fabric uses vanilla's log config, which doesn't print log tags, so we need to add our own prefix
	// Forge modifies the log config to include log tags among other things, so the prefix is redundant there
	private static final boolean PREFIX = MixinConfigPlugin.isMet(SpecialEligibility.NOT_FORGE);
	// Forge has a debug log that includes, well, debug-level messages
	// Fabric uses vanilla's log config which simply ignores debug messages
	private static final boolean FAKE_DEBUG = Boolean.getBoolean("fabrication.debug");

	// for (limited, one-level) reentrancy
	private static int warningsOld = -1;
	private static int warnings = 0;

	public static void startCountingWarnings() {
		if (warnings != -1) {
			warningsOld = warnings;
		}
		warnings = 0;
	}

	public static int stopCountingWarnings() {
		int w = warnings;
		if (warningsOld != -1) {
			warnings = warningsOld;
			warningsOld = -1;
		} else {
			warnings = -1;
		}
		return w;
	}

	private static String prefix(String message) {
		return PREFIX ? "["+log.getName()+"] "+message : message;
	}

	public static void debug(String message) {
		if (FAKE_DEBUG) { info(message); return; }
		log.debug(prefix(message));
	}

	public static void debug(String message, Throwable t) {
		if (FAKE_DEBUG) { info(message, t); return; }
		log.debug(prefix(message), t);
	}

	public static void error(String message) {
		log.error(prefix(message));
	}

	public static void error(String message, Throwable t) {
		log.error(prefix(message), t);
	}

	public static void fatal(String message) {
		log.fatal(prefix(message));
	}

	public static void fatal(String message, Throwable t) {
		log.fatal(prefix(message), t);
	}

	public static void info(String message) {
		log.info(prefix(message));
	}

	public static void info(String message, Throwable t) {
		log.info(prefix(message), t);
	}

	public static void trace(String message) {
		log.trace(prefix(message));
	}

	public static void trace(String message, Throwable t) {
		log.trace(prefix(message), t);
	}

	public static void warn(String message) {
		if (warnings != -1) warnings++;
		log.warn(prefix(message));
	}

	public static void warn(String message, Throwable t) {
		if (warnings != -1) warnings++;
		log.warn(prefix(message), t);
	}

	public static void timeAndCountWarnings(String prefix, Runnable r) {
		Stopwatch sw = Stopwatch.createStarted();
		startCountingWarnings();
		r.run();
		int w = stopCountingWarnings();
		info(prefix+" done in "+sw+" with "+w+" warning"+(w == 1 ? "" : "s"));
	}

}
