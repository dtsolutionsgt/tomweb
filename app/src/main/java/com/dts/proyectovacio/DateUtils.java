package com.dts.proyectovacio;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class DateUtils {

	public DateUtils() {
	}

	public String sfecha(long f) {
		int vy,vm,vd;
		String s;

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+ String.valueOf(vd)+"/";} else {s=s+"0"+ String.valueOf(vd)+"/";}
		if (vm>9) { s=s+ String.valueOf(vm)+"/20";} else {s=s+"0"+ String.valueOf(vm)+"/20";}
		if (vy>9) { s=s+ String.valueOf(vy);} else {s=s+"0"+ String.valueOf(vy);}

		return s;
	}
	
	public String sfechas(long f) {
		int vy,vm,vd;
		String s;

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+ String.valueOf(vd)+"/";} else {s=s+"0"+ String.valueOf(vd)+"/";}
		if (vm>9) { s=s+ String.valueOf(vm)+"/";} else {s=s+"0"+ String.valueOf(vm)+"/";}
		if (vy>9) { s=s+ String.valueOf(vy);} else {s=s+"0"+ String.valueOf(vy);}

		return s;
	}

	public String sfechash(long f) {
		int vy,vm,vd;
		String s;

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;

		s="";
		if (vd>9) { s=s+ String.valueOf(vd)+"/";} else {s=s+"0"+ String.valueOf(vd)+"/";}
		if (vm>9) { s=s+ String.valueOf(vm);} else {s=s+"0"+ String.valueOf(vm);}

		return s;
	}

	public String shora(long vValue) {
		long h,m;
		String sh,sm;

		if (vValue==0) return "";

		h=vValue % 10000;
		m=h % 100;if (m>9) {sm= String.valueOf(m);} else {sm="0"+ String.valueOf(m);}
		h=(int) h/100;if (h>9) {sh= String.valueOf(h);} else {sh="0"+ String.valueOf(h);}

		return sh+":"+sm;
	}

	public String sfechalocal(long f) {

		if (f==0) return "";

		String s=sfecha(f);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		Date date = null;
		try {
			date = sdf.parse(s);
			s = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
		} catch (Exception e) {
			s="";
		}

		return s;
	}

	public String univfecha(long f) {
		long vy,vm,vd,m,h;
		String s;

		//yyyyMMdd hh:mm:ss

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;
		h= (int) f/100;
		m= f % 100;

		s="20";
		if (vy>9) s=s+vy; else s=s+"0"+vy; 
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;  
		s=s+" ";  
		if (h>9)  s=s+h;  else s=s+"0"+h;
		s=s+":";
		if (m>9)  s=s+m;  else s=s+"0"+m;
		s=s+":00";

		return s;
	}

	public String univfechaext(int f) {
		int vy,vm,vd;
		String s;

		//yyyyMMdd hh:mm:ss

		vy=(int) f/10000;f=f % 10000;
		vm=(int) f/100;f=f % 100;
		vd=(int) f;

		s=""+vy;
		if (vm>9) s=s+vm; else s=s+"0"+vm;
		if (vd>9) s=s+vd; else s=s+"0"+vd;
		s=vy+" "+vm+":"+vd+":00"; //#HS_20181128_1102 Agregue " "+vm+":"+vd+":00" para que devolviera la hora.

		return s;
	}


	public long fechames(long f) {
		f=(int) f/1000000;
		f=f*1000000;
		return f;
	}

	public long ffecha00(long f) {
		f=(int) f/10000;
		f=f*10000;
		return f;
	}

	public long ffecha24(long f) {
		f=(int) f/10000;
		f=f*10000+2359;
		return f;
	}

	public long cfecha(int year,int month, int day) {
		long c;
		c=year % 100;
		c=c*10000+month*100+day;
		return c*10000;
	}

	public long parsedate(long date,int hour,int min) {
		long f;
		f=date+100*hour+min;
		return f;
	}

	public int getyear(long f) {
		int vy;

		vy=(int) f/100000000;f=f % 100000000;
		vy=vy+2000;

		return vy;
	}

	public int getmonth(long f) {
		int vy,vm;

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;

		return vm;
	}

	public int getday(long f) {
		int vy,vm,vd;

		vy=(int) f/100000000;f=f % 100000000;
		vm=(int) f/1000000;f=f % 1000000;
		vd=(int) f/10000;f=f % 10000;

		return vd;
	}

	public int LastDay(int year,int month) {
		int m,y,ld;

		m=month % 2;
		if (m==1) {
			ld=31;
		} else {
			ld=30;
		}

		if (month==2) {
			ld=28;
			if (year % 4==0) {ld=29;}
		}

		return ld;

	}

	public long addDays(long f,int days){
		int cyear,cmonth,cday;

		final Calendar c = Calendar.getInstance();

		c.set(getyear(f), getmonth(f)-1, getday(f));
		c.add(Calendar.DATE, days);

		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		f=cfecha(cyear,cmonth,cday);

		return f;
	}

	public String dayweek(long f) {
		int y,m,d;

		y=getyear(f);
		m=getmonth(f)-1;
		d=getday(f);

		Calendar c = new GregorianCalendar(y,m-1,d,1,0,0);

		String dn = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

		dn = dn.substring(0,1).toUpperCase() + dn.substring(1).toLowerCase();

		return dn;
	}

	public int dayofweek(long f) {
		int y,m,d,dw;

		final Calendar c = Calendar.getInstance();

		c.set(getyear(f), getmonth(f)-1, getday(f));

		dw=c.get(Calendar.DAY_OF_WEEK);

		if (dw==1) dw=7;else dw=dw-1;

		return dw;
	}

	public String monthname(long f) {
		int y,m,d;

		y=getyear(f);
		m=getmonth(f)-1;
		d=getday(f);

		Calendar c = new GregorianCalendar(y,m,d,1,0,0);

		String dn = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

		dn = dn.substring(0,1).toUpperCase() + dn.substring(1).toLowerCase();

		return dn;
	}

	public String dayweekshort(long f) {
		int y,m,d;
	     
		if (f==0) return "";
		
		y=getyear(f);
		m=getmonth(f);
		d=getday(f);
		
		Calendar c = new GregorianCalendar(y,m-1,d,1,0,0);
			
	    String dn = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
	    
	    dn = dn.substring(0,1).toUpperCase() + dn.substring(1).toLowerCase();
	    
	    return dn;
	}
	
	public long getActDate(){
		int cyear,cmonth,cday;
		long f;		

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		f=cfecha(cyear,cmonth,cday);

		return f;
	}

	public long getActDateTime(){
		int cyear,cmonth,cday,ch,cm; 
		long f;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);

		f=cfecha(cyear,cmonth,cday);
		f=f+ch*100+cm;

		return f;
	}

	public String getActDateStr(){
		int cyear,cmonth,cday;
		long f;

		final Calendar c = Calendar.getInstance();
		cyear = c.get(Calendar.YEAR);
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);

		f=cfecha(cyear,cmonth,cday);

		return sfecha(f);
	}

	public long getCorelBase(){
		int cyear,cmonth,cday,ch,cm,cs,vd,vh;
		long f;

		final Calendar c = Calendar.getInstance();

		cyear = c.get(Calendar.YEAR);cyear=cyear % 10;
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		vd=cyear*384+cmonth*32+cday;
		vh=ch*3600+cm*60+cs;

		f=vd*100000+vh;
		f=f*100;

		return f;
	}

	public String getCorelTimeStr(){
		int cyear,cmonth,cday,ch,cm,cs,vd,vh;
		long f;
		
		Calendar c = Calendar.getInstance();

		cyear = c.get(Calendar.YEAR);cyear=cyear % 10;
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		vd=cyear*384+cmonth*32+cday;
		vh=ch*3600+cm*60+cs;

		f=vd*100000+vh;

		return ""+f;
	}

}