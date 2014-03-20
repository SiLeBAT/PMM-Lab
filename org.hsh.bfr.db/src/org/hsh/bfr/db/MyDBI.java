package org.hsh.bfr.db;

import java.util.HashMap;
import java.util.LinkedHashMap;


public abstract class MyDBI {
	public abstract LinkedHashMap<String, MyTable> getAllTables();
	public abstract void recreateTriggers();
	public abstract void updateCheck(final String fromVersion, final String toVersion);
	public abstract boolean isReadOnly();
	public abstract HashMap<String, String> getProbableSAs();
	public abstract String getPath4FirstDB();
	public abstract String getDBVersion();
	public abstract LinkedHashMap<Object, String> getHashMap(final String key);

	public MyTable getTable(String tableName) {
		if (getAllTables().containsKey(tableName)) return getAllTables().get(tableName);
		else return null;
	}
}
