
//*********************************************************************
//*   PGMID.        MORSE CODE NOTIFICATION THREAD.                   *
//*   AUTHOR.       BERND R. FIX   >Y<                                *
//*   DATE WRITTEN. 10/08/07.                                         *
//*   COPYRIGHT.    (C) BY BERND R. FIX. ALL RIGHTS RESERVED.         *
//*                 LICENSED MATERIAL - PROGRAM PROPERTY OF THE       *
//*                 AUTHOR. REFER TO COPYRIGHT INSTRUCTIONS.          *
//*   REMARKS.      REVISION HISTORY AT END OF FILE.                  *
//*********************************************************************

package org.hoi_polloi.android.ringcode;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Morse code notification thread.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class MorseNotifier extends Thread {

	//=================================================================
	/*
	 * Attributes:
	 */
	private Morse beeper = null;
	private String code = null;
	private boolean running = false;

	//=================================================================
	/**
	 * <p>Constructor: Instantiate new notification thread with given
	 * speed and volume settings.</p>
	 * @param dur int - speed parameter (duration of short beep in ms)
	 * @param vol int - speaker volume (0..100)
	 */
	public MorseNotifier (int dur, int vol) {
		beeper = new Morse (dur, vol);
	}

	//=================================================================
	/**
	 * <p>Kick-off notification thread with given morse code.</p>
	 * @param code String - morse code notification
	 */
	public void begin (String code) {
		if (code == null)
			return;
		this.code = code;
		start();
	}

	//=================================================================
	/**
	 * <p>Execute thread.
	 */
	public void run () {
		running = true;
		delay (2000);
		while (running) {
			beeper.sendCode (code);
			delay (5000);
		}
	}

	//=================================================================
	/**
	 * <p>Delay notification thread for given time.</p>
	 * @param t int - delay in milliseconds
	 */
	private void delay (int t) {
		try {
			Thread.sleep (t);
		} catch (InterruptedException e) { }
	}

	//=================================================================
	/**
	 * <p>Stop notification thread.</p>
	 */
	public void terminate () {
		running = false;
	}
}
