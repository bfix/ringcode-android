
//*********************************************************************
//*   PGMID.        DEBUG HELPER CLASS.                               *
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

import java.util.Set;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


///////////////////////////////////////////////////////////////////////
/**
 * <p>Debug helper class.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class Debug {
	
	//=================================================================
	/*
	 * Constants:
	 */
	private static final String TAG = "RingCode";
	private static final boolean verbose = true;

	//=================================================================
	/**
	 * <p>Log a message.</p>
	 * @param msg String - message to be logged
	 */
	public static void log (String msg) {
		if (verbose)
			Log.d (TAG, msg);
	}
	
	//=================================================================
	/**
	 * <p>Log intent data.</p>
	 * @param intent Intent - intent to be logged
	 */
	public static void logIntent (Intent intent) {
		
		if (verbose) {
			StringBuffer buf = new StringBuffer();
			buf.append ("Intent[action=");
			buf.append (intent.getAction());
			buf.append (", categories=");
			Set<String> catList = intent.getCategories();
			if (catList == null)
				buf.append ("<none>");
			else {
				buf.append ("{");
				for (String cat : catList)
					buf.append (" " + cat);
				buf.append ("}");
			}
			buf.append (", uri=");
			Uri data = intent.getData();
			buf.append (data == null ? "<null>" : data.toString());
			buf.append (", extra=");
			Bundle b = intent.getExtras();
			if (b == null)
				buf.append ("<none>");
			else {
				buf.append ("{");
				Set<String> keyList = b.keySet();
				if (keyList != null) {
					for (String key : b.keySet()) {
						buf.append (" ");
						buf.append (key);
						buf.append ("=");
						buf.append (b.get(key));
					}
				}
				buf.append ("}");
			}
			
			Log.d (TAG, buf.toString());
		}
	}
}
