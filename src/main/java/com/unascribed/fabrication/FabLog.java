package com.unascribed.fabrication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;

public class FabLog {

	private static final Logger log = LogManager.getLogger("Fabrication");
	
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

	public static void debug(String message) {
		log.debug("[Fabrication] "+message);
	}

	public static void debug(String message, Throwable t) {
		log.debug("[Fabrication] "+message, t);
	}

	public static void error(String message) {
		log.error("[Fabrication] "+message);
	}

	public static void error(String message, Throwable t) {
		log.error("[Fabrication] "+message, t);
	}

	public static void fatal(String message) {
		log.fatal("[Fabrication] "+message);
	}

	public static void fatal(String message, Throwable t) {
		log.fatal("[Fabrication] "+message, t);
	}

	public static void info(String message) {
		log.info("[Fabrication] "+message);
	}

	public static void info(String message, Throwable t) {
		log.info("[Fabrication] "+message, t);
	}

	public static void trace(String message) {
		log.trace("[Fabrication] "+message);
	}

	public static void trace(String message, Throwable t) {
		log.trace("[Fabrication] "+message, t);
	}

	public static void warn(String message) {
		if (warnings != -1) warnings++;
		log.warn("[Fabrication] "+message);
	}

	public static void warn(String message, Throwable t) {
		if (warnings != -1) warnings++;
		log.warn("[Fabrication] "+message, t);
	}

	public static void timeAndCountWarnings(String prefix, Runnable r) {
		Stopwatch sw = Stopwatch.createStarted();
		startCountingWarnings();
		r.run();
		int w = stopCountingWarnings();
		info(prefix+" done in "+sw+" with "+w+" warning"+(w == 1 ? "" : "s"));
	}

}
