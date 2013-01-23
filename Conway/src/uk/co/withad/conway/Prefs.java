package uk.co.withad.conway;

import static uk.co.withad.conway.Constants.*;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	public static int getCellSize(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(cellSizeKey, Integer.toString(cellSizeDefault)));
	}
	
	public static int getTickTime(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(speedKey, Integer.toString(tickTimeDefault)));
	}
	
	public static boolean getGhosting(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ghostingKey, false);
	}
}
