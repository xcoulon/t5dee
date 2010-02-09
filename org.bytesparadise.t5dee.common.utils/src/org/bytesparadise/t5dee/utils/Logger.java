package org.bytesparadise.t5dee.utils;

import org.bytesparadise.t5dee.Activator;
import org.eclipse.core.runtime.Status;

public class Logger {

	public static void error(String message, Throwable t) {
		Activator.getDefault().getLog().log(
				new Status(Status.ERROR, Activator.PLUGIN_ID, message, t));
	}

	public static void warn(String message) {
		Activator.getDefault().getLog().log(
				new Status(Status.WARNING, Activator.PLUGIN_ID, message));
	}

	public static void info(String message) {
		Activator.getDefault().getLog().log(
				new Status(Status.INFO, Activator.PLUGIN_ID, message));
	}

	public static void error(String message) {
		Activator.getDefault().getLog().log(
				new Status(Status.ERROR, Activator.PLUGIN_ID, message));
	}

}
