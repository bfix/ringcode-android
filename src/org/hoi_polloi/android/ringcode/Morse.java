
//*********************************************************************
//*   PGMID.        MORSE CODE PLAYER.                                *
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

import android.media.AudioManager;
import android.media.ToneGenerator;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Play morse code of string on built-in speaker.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class Morse {

	//=================================================================
	/*
	 * Constants:
	 */
	private static final String   KEYS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!";
	private static final String[] CODES = new String [] {
		// numbers 0 .. 9
		"-----", ".----", "..---", "...--", "....-",
		".....", "-....", "--...", "---..", "----.",
		// characters A .. Z
		".-", "-...", "-.-.", "-..", ".", "..-.", "--.",
		"....", "..", ".---", "-.-", ".-..", "--", "-.",
		"---", ".--.", "--.-", ".-.", "...", "-", "..-",
		"...-", ".--", "-..-", "-.--", "--..",
		// special characters
		"...-."
	};

	//=================================================================
	/*
	 * Attributes:
	 */
	private int dur = 100;				// (short) beep length (in ms)
	private int tone = ToneGenerator.TONE_CDMA_DIAL_TONE_LITE;
	private ToneGenerator gen = null;

	//=================================================================
	/**
	 * <p>Constructor: Instantiate new Morse code player with given
	 * speed and volume settings.</p>
	 * @param dur int - speed parameter (duration of short beep in ms)
	 * @param vol int - speaker volume (0..100)
	 */
	public Morse (int dur, int vol) {
		this.dur = dur;
		gen = new ToneGenerator (AudioManager.STREAM_ALARM, vol);
	}

	//=================================================================
	/**
	 * <p>Play string of characters in Morse code.</p>
	 * @param s String - message to be played in Morse code
	 */
	public void sendCode (String s) {
		
		// check for valid string
		if (s == null)
			return;
		
		// get length of string.
		int len = s.length();
		// process all characters...
		for (int i = 0; i < len; i++)
			sendChar (s.charAt(i));
	}

	//=================================================================
	/**
	 * <p>Play Morse code for single character.</p>
	 * @param char ch - character to be morsed
	 */
	private void sendChar (char ch) {

		// get position of char in "keys"
		int pos = KEYS.indexOf (ch);
		if (pos != -1) {

			// get code (and its length)
			String code = CODES[pos];
			int len = code.length();
			// process all characters...
			for (int i = 0; i < len; i++) {
				int pause = 0;
				switch (code.charAt(i)) {
					case '.':	gen.startTone (tone, dur);		pause = 2;	break;
					case '-':	gen.startTone (tone, 3*dur);	pause = 4;	break;
				}
				delay (pause * dur);
			}
			delay (4 * dur);
		}
	}

	//=================================================================
	/**
	 * <p>Delay thread for given number of milliseconds.</p>
	 * @param t int - number of milliseconds
	 */
	private void delay (int t) {
		try {
			Thread.sleep (t);
		} catch (Exception e) { }
	}
}
