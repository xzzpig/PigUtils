package com.github.xzzpig.pigutils.logger;

import com.github.xzzpig.pigutils.annotation.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class LogPrinter {

	private static Map<String, LogPrinter> map = new HashMap<>();

	public static void addPrinter(@NotNull LogPrinter printer) {
		map.put(printer.getName(), printer);
	}

	public static LogPrinter getPrinter(String str) {
		if (map.containsKey(str))
			return map.get(str);
		try {
			Class<?> clazz = Class.forName(str);
			if (LogPrinter.class.isAssignableFrom(clazz)) {
				return (LogPrinter) clazz.newInstance();
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
		}
		return null;
	}

	public LogPrinter() {
	}

	public abstract String getName();

	public abstract void print(String log);
}
