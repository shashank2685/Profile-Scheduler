package edu.buffalo.scheduler;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;


public class Profile implements Parcelable {


	public static final Parcelable.Creator<Profile> CREATOR
	= new Parcelable.Creator<Profile>() {
		public Profile createFromParcel(Parcel p) {
			return new Profile(p);
		}

		public Profile[] newArray(int size) {
			return new Profile[size];
		}
	};
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(starthour);
		p.writeInt(startminutes);
		p.writeInt(repeatPref.getCoded());
		p.writeLong(start_time);
		p.writeInt(vibrate ? 1 : 0);
		p.writeString(label);
		p.writeInt(silent ? 1 : 0);
	}


	public int id;
	public boolean enabled;
	public int starthour;
	public int startminutes;
	public int endhour;
	public int endminutes;
	public long start_time;
	public long end_time;
	public DaysOfWeek repeatPref;
	public boolean vibrate;
	public boolean silent;
	public String label;

	public Profile(Parcel p) {
	
		id = p.readInt();
		enabled = p.readInt() == 1;
		starthour = p.readInt();
		startminutes = p.readInt();
		repeatPref = new DaysOfWeek(p.readInt());
		start_time = p.readInt();
		vibrate = p.readInt() == 1;
		label = p.readString();
		silent = p.readInt() == 1;
	}
	
	public Profile(Cursor cursor) {

		id = cursor.getInt(ProfileDbAdapter.KEY_ROWID_INDEX);
		enabled = (cursor.getInt(ProfileDbAdapter.KEY_ENABLED_INDEX) == 1)? true : false;
		starthour = cursor.getInt(ProfileDbAdapter.KEY_STARTHOUR_INDEX);
		startminutes = cursor.getInt(ProfileDbAdapter.KEY_STARTMIN_INDEX);
		endhour = cursor.getInt( ProfileDbAdapter.KEY_ENDHOUR_INDEX);
		endminutes = cursor.getInt( ProfileDbAdapter.KEY_ENDMIN_INDEX);
		start_time = cursor.getLong(ProfileDbAdapter.KEY_STARTTIME_INDEX);
		end_time = cursor.getLong( ProfileDbAdapter.KEY_ENDTIME_INDEX);
		repeatPref = new DaysOfWeek(cursor.getInt( ProfileDbAdapter.KEY_REPEAT_INDEX));
		vibrate = (cursor.getInt(ProfileDbAdapter.KEY_VIBRATE_INDEX) == 1)? true : false;
		silent = (cursor.getInt(ProfileDbAdapter.KEY_SILENT_INDEX) == 1)? true : false;
		label = cursor.getString(ProfileDbAdapter.KEY_LABEL_INDEX);

	}

	public Profile() {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		starthour = c.get(Calendar.HOUR_OF_DAY);
		startminutes = c.get(Calendar.MINUTE);
		endhour = c.get(Calendar.HOUR_OF_DAY);;
		endminutes = c.get(Calendar.MINUTE);
		vibrate = true;
		repeatPref = new DaysOfWeek(0);

	}

	/*
	 * Days of week code as a single int.
	 * 0x00: no day
	 * 0x01: Monday
	 * 0x02: Tuesday
	 * 0x04: Wednesday
	 * 0x08: Thursday
	 * 0x10: Friday
	 * 0x20: Saturday
	 * 0x40: Sunday
	 */
	static final class DaysOfWeek {

		private static int[] DAY_MAP = new int[] {
			Calendar.MONDAY,
			Calendar.TUESDAY,
			Calendar.WEDNESDAY,
			Calendar.THURSDAY,
			Calendar.FRIDAY,
			Calendar.SATURDAY,
			Calendar.SUNDAY,
		};

		// Bitmask of all repeating days
		private int mDays;

		DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ?
						context.getText(R.string.never).toString() : "";
			}

			// every day
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1) dayCount++;
				days >>= 1;
			}

			// short or long form?
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] dayList = (dayCount > 1) ?
					dfs.getShortWeekdays() :
						dfs.getWeekdays();

					// selected days
					for (int i = 0; i < 7; i++) {
						if ((mDays & (1 << i)) != 0) {
							ret.append(dayList[DAY_MAP[i]]);
							dayCount -= 1;
							if (dayCount > 0) ret.append(
									context.getText(R.string.day_concat));
						}
					}
					return ret.toString();
		}

		private boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

		public void set(DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public int getCoded() {
			return mDays;
		}

		// Returns days of week encoded in an array of booleans.
		public boolean[] getBooleanArray() {
			boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

		/**
		 * returns number of days from today until next alarm
		 * @param c must be set to today
		 */
		public int getNextAlarm(Calendar c) {
			if (mDays == 0) {
				return -1;
			}

			int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

			int day = 0;
			int dayCount = 0;
			for (; dayCount < 7; dayCount++) {
				day = (today + dayCount) % 7;
				if (isSet(day)) {
					break;
				}
			}
			return dayCount;
		}
	}

}

