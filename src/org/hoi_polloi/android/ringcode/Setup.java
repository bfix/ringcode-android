
//*********************************************************************
//*   PGMID.        RINGCODE CONFIGURATION ACTIVITY.                  *
//*   AUTHOR.       BERND R. FIX   >Y<                                *
//*   DATE WRITTEN. 10/08/07.                                         *
//*   COPYRIGHT.    (C) BY BERND R. FIX. ALL RIGHTS RESERVED.         *
//*                 LICENSED MATERIAL - PROGRAM PROPERTY OF THE       *
//*                 AUTHOR. REFER TO COPYRIGHT INSTRUCTIONS.          *
//*   REMARKS.      REVISION HISTORY AT END OF FILE.                  *
//*********************************************************************

package org.hoi_polloi.android.ringcode;

///////////////////////////////////////////////////////////////////////
//Import external declarations

import org.hoi_polloi.android.ringcode.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Configuration activity.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class Setup extends Activity {

	//=================================================================
	/**
	 * <p>Name of preference settings (persistent).</p>
	 */
	private static final String PREFS = "RingCode_Prefs";

	//=================================================================
	/*
	 * Attributes:
	 */
	private static boolean	active	= true;		// use ringcodes?
	private static int		volume	= 100;		// speaker volume (0..100)
	private static int		speed	= 100;		// duration of short beep in ms
	
	//=================================================================
	/**
	 * <p>Constructor: Instantiate configuration activity.</p>
	 * @param savedInstanceState Bundle - saved activity state
	 */
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setContentView (R.layout.config);

		// get preference data
		SharedPreferences settings = getSharedPreferences (PREFS, 0);
		active = settings.getBoolean ("active", false);
		volume = settings.getInt ("volume", 100);
		speed = settings.getInt ("speed", 100);

		// pass to parameters to associated views
		((CheckBox) findViewById (R.id.cfg_active)).setChecked (active);
		((SeekBar) findViewById (R.id.cfg_volume)).setProgress (volume);
		((SeekBar) findViewById (R.id.cfg_speed)).setProgress (250-speed);
	}

	//=================================================================
	/**
	 * <p>Handle clicks on parameter views.</p>
	 * @param view View - clicked configuration setting
	 */
	public void onClick (View view) {

		switch (view.getId()) {

		// play morse code once
			case R.id.cfg_play: {
				volume = ((SeekBar) findViewById (R.id.cfg_volume)).getProgress();
				speed = 250 - ((SeekBar) findViewById (R.id.cfg_speed)).getProgress();
				Morse beeper = new Morse (speed, volume);
				beeper.sendCode ("!");
			} break;
		}
	}

	//=================================================================
	/**
	 * <p>Save configuration settings on termination.</p>
	 */
	@Override
	protected void onStop () {
		super.onStop();

		// save configuration in preference setting
		SharedPreferences settings = getSharedPreferences (PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();

		volume = ((SeekBar) findViewById (R.id.cfg_volume)).getProgress();
		speed = 250 - ((SeekBar) findViewById (R.id.cfg_speed)).getProgress();
		active = ((CheckBox) findViewById (R.id.cfg_active)).isChecked();
		
		editor.putBoolean ("active", active);
		editor.putInt ("volume", volume);
		editor.putInt ("speed", speed);
		editor.commit();
	}

	//=================================================================
	//	Getter methods
	//=================================================================
	/**
	 * <p>Check for active ringcode notification.</p>
	 * @return boolean - ringcode notification is active?
	 */
	public static boolean isActivated () {
		return active;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Get speaker volume.</p>
	 * @return int - speaker volume (0..100)
	 */
	public static int getVolume() {
		return volume;
	}
	//-----------------------------------------------------------------
	/**
	 * <p>Get morse speed.</p>
	 * @return int - duration of short beep in ms
	 */
	public static int getSpeed() {
		return speed;
	}
}
