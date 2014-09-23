
//*********************************************************************
//*   PGMID.        PHONE NUMBER UTILITY CLASS.                       *
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
 * <p>Phone number utility class.</p>
 *
 * @author Bernd R. Fix   >Y<
 * @version 1.0
 */
public class PhoneNumber {

	//=================================================================
	/**
	 * <p>Normalize (compacts) phone number for comparison.</p>
	 * @param number String - phone number
	 * @return String - normalized phone number
	 */
	public static String normalize (String number) {
		
		StringBuffer buf = new StringBuffer();

		// handle number prefix:
		// 	- strip leading '0' or '+' from numbers
		if (number.startsWith ("00")) {
			number = number.substring (2);
		}
		else if (number.startsWith ("+")) {
			number = number.substring (1);
		}
		else if (number.startsWith ("0")) {
			number = number.substring (1);
		}
		
		// strip non-digit characters
		int count = number.length();
		boolean init = true;
		for (int n = 0; n < count; n++) {
			char ch = number.charAt (n);
			if (!Character.isDigit(ch) || (init && ch == '0'))
				continue;
			buf.append (ch);
			init = false;
		}
		return buf.toString();
	}

	//=================================================================
	/**
	 * <p>Compare two normalized phone numbers.</p>
	 * @param num1 String
	 * @param num2 String
	 * @return boolean - phone numbers match
	 */
	public static boolean isSame (String num1, String num2) {
		
		if (num1.endsWith (num2))
			return true;
		if (num2.endsWith (num1))
			return true;
		return false;
	}
}
