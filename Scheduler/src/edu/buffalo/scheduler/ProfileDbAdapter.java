package edu.buffalo.scheduler;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ProfileDbAdapter {

	// Database fields
		public static final String KEY_ROWID = "_id";
		public static final String KEY_ENABLED = "enabled";
		public static final String KEY_STARTHOUR = "starthour";
		public static final String KEY_STARTMIN = "startminutes";
		public static final String KEY_STARTTIME = "starttime";
		public static final String KEY_ENDHOUR = "endhour";
		public static final String KEY_ENDMIN = "endminutes";
		public static final String KEY_ENDTIME = "endtime";
		public static final String KEY_VIBRATE = "vibrate";
		public static final String KEY_SILENT = "silent";
		public static final String KEY_REPEAT = "repeat";
		public static final String KEY_LABEL = "label";
		
		
		public static final int KEY_ROWID_INDEX = 0;
		public static final int KEY_ENABLED_INDEX = 1;
		public static final int KEY_STARTHOUR_INDEX = 2;
		public static final int KEY_STARTMIN_INDEX = 3;
		public static final int KEY_STARTTIME_INDEX = 4;
		public static final int KEY_ENDHOUR_INDEX = 5;
		public static final int KEY_ENDMIN_INDEX = 6;
		public static final int KEY_ENDTIME_INDEX = 7;
		public static final int KEY_VIBRATE_INDEX = 8;
		public static final int KEY_SILENT_INDEX = 9;
		public static final int KEY_REPEAT_INDEX = 10;
		public static final int KEY_LABEL_INDEX = 11;
		
		private static final String DB_TABLE = "profiletable";
		
		static final String[] PROFILE_QUERY_COLUMNS = { KEY_ROWID, KEY_ENABLED, KEY_STARTHOUR, 
			                                           KEY_STARTMIN, KEY_STARTTIME, KEY_ENDHOUR, 
			                                           KEY_ENDMIN, KEY_ENDTIME, KEY_VIBRATE, 
			                                           KEY_SILENT, KEY_REPEAT, KEY_LABEL };
		static ProfileDbAdapter self = null;
		
		private Context context;
		private SQLiteDatabase db;
		private ProfileDatabaseHelper dbHelper;

		private ProfileDbAdapter(Context context) {
			dbHelper = new ProfileDatabaseHelper(context);
			db = dbHelper.getWritableDatabase();
			this.context = context;
		}

		public static ProfileDbAdapter factory(Context context) {
		
			if (self == null)
				self = new ProfileDbAdapter(context);
			
			return self;
		}
		
		public ProfileDbAdapter open() throws SQLException {
			dbHelper = new ProfileDatabaseHelper(context);
			db = dbHelper.getWritableDatabase();
			return this;
		}

		public void close() {
			dbHelper.close();
		}

		/**
		 * Create a new todo If the todo is successfully created return the new
		 * rowId for that note, otherwise return a -1 to indicate failure.
		 */
		public long createProfile(Profile profile) {
			ContentValues values = createContentValues(profile);

			return db.insert(DB_TABLE, null, values);
		}

		/**
		 * Update the todo
		 */
		public boolean updateProfile(long rowId, Profile profile) {
			ContentValues values = createContentValues(profile);
			
			return db.update(DB_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
		}

		/**
		 * Deletes todo
		 */
		public boolean deleteProfile(long rowId) {
			return db.delete(DB_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
		}

		/**
		 * Return a Cursor over the list of all todo in the database
		 * 
		 * @return Cursor over all notes
		 */
		public Cursor fetchAllProfiles() {
			Cursor retValue = null;
			try {
			retValue = db.query(DB_TABLE, PROFILE_QUERY_COLUMNS, null, null, null, null, null);
			} catch ( Exception e) {
				String temp = "hello";
			}
			return retValue;
		}

		/**
		 * Return a Cursor positioned at the defined todo
		 */
		public Cursor fetchProfile(long rowId) throws SQLException {
			Cursor mCursor = db.query(true, DB_TABLE, PROFILE_QUERY_COLUMNS, KEY_ROWID + "="
					+ rowId, null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}

		private ContentValues createContentValues(Profile profile) {
			ContentValues values = new ContentValues();
			values.put(KEY_ENABLED, profile.enabled ? 1 : 0);
			values.put(KEY_ENDHOUR, profile.endhour);
			values.put(KEY_ENDMIN, profile.endminutes);
			values.put(KEY_ENDTIME, profile.end_time);
			values.put(KEY_LABEL, profile.label);
			values.put(KEY_REPEAT, profile.repeatPref.getCoded());
			values.put(KEY_SILENT, profile.silent ? 1 : 0);
			values.put(KEY_STARTHOUR, profile.starthour);
			values.put(KEY_STARTMIN, profile.startminutes);
			values.put(KEY_STARTTIME, profile.start_time);
			values.put(KEY_VIBRATE, profile.vibrate ? 1 : 0);
			return values;
		}
}
