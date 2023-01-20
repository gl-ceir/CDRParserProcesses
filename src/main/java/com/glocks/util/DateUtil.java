package com.glocks.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DateUtil {

	private final static String NUMERIC_STRING = "0123456789";

	public static String nextDate(int nextdateDay, String format) {
		if(Objects.isNull(format)) {
			format = "yyyy-MM-dd";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.DAY_OF_MONTH, nextdateDay);  
		String newDate = sdf.format(cal.getTime());  

		return newDate;

	}

	public Date formatChanger(LocalDateTime localDateTime ) throws ParseException {

		String dmyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(localDateTime);
		java.util.Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dmyFormat);

		return date;
	}
	
	public LocalDate parseDate(String date) {
		return LocalDate.parse(date);
	}
	
	public void compareDateWithToday() {
		
	}
	public static java.sql.Date getSqlDate(String date,String format)
	{
		java.sql.Date sqlDate = null;
		try {
		SimpleDateFormat sdf1 = new SimpleDateFormat(format);
		java.util.Date dateUtil = sdf1.parse(date);
		sqlDate = new java.sql.Date(dateUtil.getTime()); 
		}
		catch(Exception exp){
			exp.printStackTrace();
		}
		return sqlDate;
	}
	public static void main(String[] args) {
		String testdate = "04-01";
		
		 // System.out.println("DateUtil.nextDate(27, \"yyyy-MM-dd\") " + DateUtil.nextDate(-27, "yyyy-MM-dd"));
		String currentYear = DateUtil.nextDate(0, "yyyy-MM-dd").split("-")[0];
		String finalDate = currentYear + "-" + testdate;
		 // System.out.println("finalDate :" + finalDate);
		
		// Compare date
		// if final date is greater then today -> stringToDate() -> Compare date()
		// 	year = currentyear - 1;
		// 	check date = year + "-" + testdate
		// else
		//    
		
		// if( )
		
		
		
	} 

}
