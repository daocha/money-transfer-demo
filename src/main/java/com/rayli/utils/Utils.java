package com.rayli.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class
 * 
 * @author Ray LI
 * @date 6 Feb 2020
 * @company ray@dcha.xyz
 */
public class Utils {

	public static Date parseDatabaseTime(String timestring) {
		String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			Date time = simpleDateFormat.parse(timestring);
			return time;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

}
