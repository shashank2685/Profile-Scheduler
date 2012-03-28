package edu.buffalo.scheduler;

import android.database.sqlite.SQLiteDatabase;

public class UBTable {

	private static final String DATABASE_CREATE = "create table ubprofiles "
			+ "(_id integer primary key autoincrement, "
			+ "startdate  not null, " + "summary text not null,"
			+ " description text not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
