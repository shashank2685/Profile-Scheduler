package edu.buffalo.scheduler;


import android.database.sqlite.SQLiteDatabase;


public class ProfileTable {

	private static final String DATABASE_CREATE = "create table profiletable "
			+ "(_id integer primary key autoincrement, "
			+ "enabled integer not null, " 
			+ "starthour integer not null, "
			+ "startminutes integer not null, "
			+ "starttime integer not null, "
			+ "endhour integer not null, "
			+ "endminutes integer not null, "
			+ "endtime integer not null, "
			+ "vibrate integer not null, "
			+ "silent integer not null, "
			+ "repeat integer not null, "
			+ "label text );";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
