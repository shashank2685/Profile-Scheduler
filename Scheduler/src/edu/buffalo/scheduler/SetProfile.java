package edu.buffalo.scheduler;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;


public class SetProfile extends PreferenceActivity 
implements TimePickerDialog.OnTimeSetListener,
Preference.OnPreferenceChangeListener {

	private EditTextPreference mLabel;
	private CheckBoxPreference mEnabledPref;
	private Preference mStartTimePref;
	private Preference mEndTimePref;
	private Preference clickedTimePref;
	private CheckBoxPreference mVibratePref;
	private CheckBoxPreference mSilentPref;
	private RepeatPreference mRepeatPref;

	private int     mId;
	private int     mStartHour;
	private int     mStartMinutes;
	private int 	mEndHour;
	private int 	mEndMinutes;
	private boolean mTimePickerCancelled;
	private Profile   mOriginalProfile;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Override the default content view.
		setContentView(R.layout.set_profile);

		addPreferencesFromResource(R.xml.profile_prefs);

		mLabel = (EditTextPreference) findPreference("label");
		mLabel.setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference p,
							Object newValue) {
						String val = (String) newValue;
						// Set the summary based on the new label.
						p.setSummary(val);
						//Put this in to the database
						if (val != null && !val.equals(mLabel.getText())) {
							// Call through to the generic listener.
							return SetProfile.this.onPreferenceChange(p,
									newValue);
						}
						return true;
					}
				});
		mEnabledPref = (CheckBoxPreference) findPreference("enabled");
		mEnabledPref.setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference p,
							Object newValue) {
						return SetProfile.this.onPreferenceChange(p, newValue);
					}
				});
		mStartTimePref = findPreference("starttime");
		mEndTimePref = findPreference("endtime");
		mVibratePref = (CheckBoxPreference) findPreference("vibrate");
		mVibratePref.setOnPreferenceChangeListener(this);
		mSilentPref = (CheckBoxPreference) findPreference("silent");
		mRepeatPref = (RepeatPreference) findPreference("setRepeat");
		mRepeatPref.setOnPreferenceChangeListener(this);

		Intent i = getIntent();
		Profile profile;

		mId = i.getIntExtra(ProfileDbAdapter.KEY_ROWID,-1);
		if (mId == -1) {
			// No alarm id means create a new alarm.
			profile = new Profile();
		} else {
			/* load alarm details from database */
			profile = Profiles.getProfile(this, mId);
			// Bad alarm, bail to avoid a NPE.
			if (profile == null) {
				finish();
				return;
			}
		}
		mOriginalProfile = profile;
		updatePrefs(mOriginalProfile);

		getListView().setItemsCanFocus(true);

		Button b = (Button) findViewById(R.id.profile_save);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveProfile();
				finish();
			}
		});

		final Button revert = (Button) findViewById(R.id.profile_revert);
		revert.setEnabled(false);
		revert.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int newId = mId;
				updatePrefs(mOriginalProfile);

				if (mOriginalProfile.id == -1) {
					Profiles.deleteProfile(SetProfile.this, mId);
				} else {
					saveProfile();
				}
				revert.setEnabled(false);
			}
		});

		b = (Button) findViewById(R.id.profile_delete);
		if (mId == -1) {
			b.setEnabled(false);
		} else {
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					deleteAlarm();
				}
			});
		}

		// The last thing we do is pop the time picker if this is a new alarm.
		if (mId == -1) {
			// Assume the user hit cancel
			mTimePickerCancelled = true;
			clickedTimePref = mStartTimePref;
			showTimePicker();
		}
	}

	// Used to post runnables asynchronously.
	private static final Handler sHandler = new Handler();

	public boolean onPreferenceChange(final Preference p, Object newValue) {
		// Asynchronously save the alarm since this method is called _before_
		// the value of the preference has changed.
		sHandler.post(new Runnable() {
			public void run() {
				// Editing any preference (except enable) enables the alarm.
				if (p != mEnabledPref) {
					mEnabledPref.setChecked(true);
				}
				saveProfileAndEnableRevert();
			}
		});
		return true;
	}

	private long saveProfileAndEnableRevert() {
		// Enable "Revert" to go back to the original Alarm.
		final Button revert = (Button) findViewById(R.id.profile_revert);
		revert.setEnabled(true);
		return saveProfile();
	}

	private long saveProfile() {
		Profile profile = new Profile();
		profile.id = mId;
		profile.enabled = mEnabledPref.isChecked();
		profile.starthour = mStartHour;
		profile.startminutes = mStartMinutes;
		profile.endhour = mEndHour;
		profile.endminutes = mEndMinutes;
		profile.repeatPref = mRepeatPref.getDaysOfWeek();
		profile.vibrate = mVibratePref.isChecked();
		profile.label = mLabel.getText();
		profile.silent = mSilentPref.isChecked();

		long time = 0;
		if (profile.id == -1) {
			Profiles.addProfile(this, profile);
			mId = profile.id;
		} else {
			Profiles.saveProfile(this, profile);
		}
		return time;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if (preference == mStartTimePref || preference == mEndTimePref) {
			clickedTimePref = preference;
			showTimePicker();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// onTimeSet is called when the user clicks "Set"
		mTimePickerCancelled = false;
		if ( clickedTimePref == mStartTimePref ) {
			mStartHour = hourOfDay;
			mStartMinutes = minute;
			updateTime(mStartTimePref);
		}

		if ( clickedTimePref == mEndTimePref) {
			mEndHour = hourOfDay;
			mEndMinutes = minute;
			updateTime(mEndTimePref);
		}

		mEnabledPref.setChecked(true);
		saveProfileAndEnableRevert();
	}

	@Override
	public void onBackPressed() {
		if (!mTimePickerCancelled) {
			saveProfile();
		}
		finish();
	}

	private void showTimePicker() {
		if ( clickedTimePref == mStartTimePref) {
			new TimePickerDialog(this, this, mStartHour, mStartMinutes,
					DateFormat.is24HourFormat(this)).show();
		} else {
			new TimePickerDialog(this, this, mEndHour, mEndMinutes,
					DateFormat.is24HourFormat(this)).show();

		}
	}

	private void deleteAlarm() {
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.delete_profile))
		.setMessage(getString(R.string.delete_profile_confirm))
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface d, int w) {
				Profiles.deleteProfile(SetProfile.this, mId);
				finish();
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.show();
	}

	private void updatePrefs(Profile profile) {
		mId = profile.id;
		mEnabledPref.setChecked(profile.enabled);
		mLabel.setText(profile.label);
		mLabel.setSummary(profile.label);
		mStartHour = profile.starthour;
		mStartMinutes = profile.startminutes;
		mEndHour = profile.endhour;
		mEndMinutes = profile.endminutes;
		mRepeatPref.setDaysOfWeek(profile.repeatPref);
		mVibratePref.setChecked(profile.vibrate);
		mSilentPref.setChecked(profile.silent);
		// Give the alert uri to the preference.

		updateTime();
	}

	private void updateTime(Preference preference) {
		if ( preference == mStartTimePref)
			mStartTimePref.setSummary(Profiles.formatTime(this, mStartHour, mStartMinutes, mRepeatPref.getDaysOfWeek()));
		if (preference == mEndTimePref)
			mEndTimePref.setSummary(Profiles.formatTime(this, mEndHour, mEndMinutes, mRepeatPref.getDaysOfWeek()));
	}

	private void updateTime() {

		mStartTimePref.setSummary(Profiles.formatTime(this, mStartHour, mStartMinutes, mRepeatPref.getDaysOfWeek()));
		mEndTimePref.setSummary(Profiles.formatTime(this, mEndHour, mEndMinutes, mRepeatPref.getDaysOfWeek()));
	}

}
