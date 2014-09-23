
//*********************************************************************
//*   PGMID.        DETECT INCOMING CALLS AND TRIGGER NOTIFICATION.   *
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Detect incoming phone calls and trigger notification if
 * phone number assigned to a Morse code in the repository..</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class RingDetector extends BroadcastReceiver {

	//=================================================================
	/**
	 * <p>Morse code notification thread.</p>
	 */
	private static MorseNotifier notifier = null;

	//=================================================================
	/**
	 * <p>Broadcast message received.</p>
	 * @param context Context - associated context
	 * @param intent Intent - message parameters (action,category,data)
	 */
	@Override
	public void onReceive (Context context, Intent intent) {

		// get data associated with incoming call
		Bundle bundle = intent.getExtras ();
		if (bundle == null) {
			Debug.log ("No data for incoming call");
			return;
		}
		String state  = bundle.getString ("state");
		String number = bundle.getString ("incoming_number");

		// decide on state:
		if ("RINGING".equals (state)) {
			// INCOMING CALL

			// only use ringcode if ringer is silent
			AudioManager am = (AudioManager) context.getSystemService (Context.AUDIO_SERVICE);
			if (am == null) {
				Debug.log ("No AudioManager available");
				return;
			}
			int rm = am.getRingerMode();
			if (rm == AudioManager.RINGER_MODE_NORMAL)
				return;
			
			// only if we are activated
			if (!Setup.isActivated())
				return;

			// find number in assignments
			number = PhoneNumber.normalize (number);
			Cursor c = context.getContentResolver().query (
				Repository.NumberCode.CONTENT_URI,
				new String[] { Repository.NumberCode.NUMBER, Repository.NumberCode.CODE },
				null, null, null
			);
			
			// get code from associated assignment
			String code = null;
			try {
				c.moveToFirst();
				do {
					// check for matching phone numbers.
					String list_number = PhoneNumber.normalize (c.getString (0));
					String list_code = c.getString (1);
					if (PhoneNumber.isSame (number, list_number)) {
						code = list_code;
						break;
					}
				} while (c.moveToNext());
			}
			catch (Exception e) {
				Debug.log ("Failed: lookup number in assignment");
			}
			finally {
				// close query on exit
				c.close();
			}

			// quit if no assignment is found
			if (code == null)
				return;

			// run notification loop.
			if (notifier == null) {
				int volume = Setup.getVolume();
				int speed = Setup.getSpeed();
				notifier = new MorseNotifier (speed, volume);
			}
			notifier.begin (code);
		}
		else {
			// Not INCOMING state terminates any running notification
			if (notifier != null) {
				notifier.terminate();
				notifier = null;
			}
		}
	}
}
