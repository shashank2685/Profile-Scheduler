package edu.buffalo.scheduler;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;

public class Profiles {

	public final static String M12 = "h:mm aa";
    public final static String M24 = "kk:mm";
    
    
    public static void enableProfile(
            final Context context, final int id, boolean enabled) {
        ProfileDbAdapter helper = ProfileDbAdapter.factory(context);
        Cursor cursor = helper.fetchProfile(id);
        Profile profile = new Profile(cursor);
        profile.enabled = enabled;
        helper.updateProfile(id, profile);
        // TODO
        // set next alert.
    }
    
    public static void saveProfile(Context context, Profile profile) {
    	ProfileDbAdapter helper = ProfileDbAdapter.factory(context);
    	helper.updateProfile(profile.id, profile);
    }
    
    public static void addProfile(Context context, Profile profile) {
    	
    	ProfileDbAdapter helper = ProfileDbAdapter.factory(context);
    	profile.id = (int)helper.createProfile(profile);
    	
    }
    
    public static Profile getProfile(Context context, int id) {
    	ProfileDbAdapter helper = ProfileDbAdapter.factory(context);
    	Cursor cursor = helper.fetchProfile(id);
    	Profile profile = new Profile(cursor);
    	cursor.close();
    	return profile;
    }
    
    public static void deleteProfile(Context context, int id) {
    	ProfileDbAdapter helper = ProfileDbAdapter.factory(context);
    	helper.deleteProfile(id);
    }
    
	static Calendar calculateAlarm(int hour, int minute,
            Profile.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    static String formatTime(final Context context, int hour, int minute,
                             Profile.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }
    
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String)DateFormat.format(format, c);
    }
    
    static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
}
