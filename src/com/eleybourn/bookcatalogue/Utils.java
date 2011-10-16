/*
 * @copyright 2011 Philip Warner
 * @license GNU General Public License
 * 
 * This file is part of Book Catalogue.
 *
 * Book Catalogue is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Book Catalogue is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Book Catalogue.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.eleybourn.bookcatalogue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

public class Utils {
	private static String UTF8 = "utf8";
	private static int BUFFER_SIZE = 8192;

	// Used for date parsing
	static SimpleDateFormat mDateSqlSdf = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat mDate1Sdf = new SimpleDateFormat("dd-MMM-yyyy");
	static SimpleDateFormat mDate2Sdf = new SimpleDateFormat("dd-MMM-yy");
	static SimpleDateFormat mDateUSSdf = new SimpleDateFormat("MM-dd-yyyy");
	static SimpleDateFormat mDateEngSdf = new SimpleDateFormat("dd-MM-yyyy");
	static DateFormat mDateDispSdf = DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);

	public static final String APP_NAME = "Book Catalogue";
	public static final String LOCATION = "bookCatalogue";
	public static final String DATABASE_NAME = "book_catalogue";
	public static final boolean USE_LT = true;
	public static final boolean USE_BARCODE = true;
	//public static final String APP_NAME = "DVD Catalogue";
	//public static final String LOCATION = "dvdCatalogue";
	//public static final String DATABASE_NAME = "dvd_catalogue";
	//public static final boolean USE_LT = false;
	//public static final boolean USE_BARCODE = false;
	//public static final String APP_NAME = "CD Catalogue";
	//public static final String LOCATION = "cdCatalogue";
	//public static final String DATABASE_NAME = "cd_catalogue";
	//public static final boolean USE_LT = true;
	//public static final boolean USE_BARCODE = false;

	public static final String EXTERNAL_FILE_PATH = Environment.getExternalStorageDirectory() + "/" + LOCATION;
	public static final String ERRORLOG_FILE = EXTERNAL_FILE_PATH + "/error.log";

	public static String toSqlDate(Date d) {
		return mDateSqlSdf.format(d);
	}
	public static String toPrettyDate(Date d) {
		return mDateDispSdf.format(d);		
	}

	public static Date parseDate(String s) {
		SimpleDateFormat[] formats = new SimpleDateFormat[] {
				mDateSqlSdf,
				mDate1Sdf,
				mDate2Sdf,
				mDateUSSdf,
				mDateEngSdf};
		Date d;
		for ( SimpleDateFormat sdf : formats ) {
			try {
				// Parse as SQL/ANSI date
				d = sdf.parse(s);
				return d;
			} catch (Exception e) {
				// Ignore 
			}			
		}
		// All SDFs failed, try one more...
		try {
			java.text.DateFormat df = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT);
			d = df.parse(s);
			return d;
		} catch (Exception e) {
			// Ignore 
		}			
		return null;
	}

	private static ArrayUtils<Author> mAuthorUtils = null;
	private static ArrayUtils<Series> mSeriesUtils = null;

	static public ArrayUtils<Author> getAuthorUtils() {
		if (mAuthorUtils == null) {
			mAuthorUtils = new ArrayUtils<Author>(new Utils.Factory<Author>(){
				@Override
				public Author get(String source) {
					return new Author(source);
				}});			
		}
		return mAuthorUtils;
	}

	static public ArrayUtils<Series> getSeriesUtils() {
		if (mSeriesUtils == null) {
			mSeriesUtils = new ArrayUtils<Series>(new Utils.Factory<Series>(){
				@Override
				public Series get(String source) {
					return new Series(source);
				}});
		}
		return mSeriesUtils;
	}

	/**
	 * Check if the sdcard is writable
	 * 
	 * @return	success or failure
	 */
	static public boolean sdCardWritable() {
		/* Test write to the SDCard */
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(EXTERNAL_FILE_PATH + "/.nomedia"), UTF8), BUFFER_SIZE);
			out.write("");
			out.close();
			return true;
		} catch (IOException e) {
			return false;
		}		
	}

	/**
	 * Encode a string by 'escaping' all instances of: '|', '\', \r, \n. The
	 * escape char is '\'.
	 * 
	 * This is used to build text lists separated by the passed delimiter.
	 * 
	 * @param s			String to convert
	 * @param delim		The list delimiter to encode (if found).
	 * 
	 * @return		Converted string
	 */
	static String encodeListItem(String s, char delim) {
		StringBuilder ns = new StringBuilder();
		for (int i = 0; i < s.length(); i++){
		    char c = s.charAt(i);        
		    switch (c) {
		    case '\\':
		    	ns.append("\\\\");
		    	break;
		    case '\r':
		    	ns.append("\\r");
		    	break;
		    case '\n':
		    	ns.append("\\n");
		    	break;
		    default:
		    	if (c == delim)
		    		ns.append("\\");
		    	ns.append(c);
		    }
		}
		return ns.toString();
	}

	/**
	 * Encode a list of strings by 'escaping' all instances of: delim, '\', \r, \n. The
	 * escape char is '\'.
	 * 
	 * This is used to build text lists separated by 'delim'.
	 * 
	 * @param s		String to convert
	 * @return		Converted string
	 */
	static String encodeList(ArrayList<String> sa, char delim) {
		StringBuilder ns = new StringBuilder();
		Iterator<String> si = sa.iterator();
		if (si.hasNext()) {
			ns.append(encodeListItem(si.next(), delim));
			while (si.hasNext()) {
				ns.append(delim);
				ns.append(encodeListItem(si.next(), delim));
			}
		}
		return ns.toString();
	}

	public interface Factory<T> {
		T get(String source);
	}

	static public class ArrayUtils<T> {

		Factory<T> mFactory;

		ArrayUtils(Factory<T> factory) {
			mFactory = factory;
		}

		private T get(String source) {
			return mFactory.get(source);
		}
		/**
		 * Encode a list of strings by 'escaping' all instances of: delim, '\', \r, \n. The
		 * escape char is '\'.
		 * 
		 * This is used to build text lists separated by 'delim'.
		 * 
		 * @param s		String to convert
		 * @return		Converted string
		 */
		String encodeList(ArrayList<T> sa, char delim) {
			Iterator<T> si = sa.iterator();
			return encodeList(si, delim);
		}

		private String encodeList(Iterator<T> si, char delim) {
			StringBuilder ns = new StringBuilder();
			if (si.hasNext()) {
				ns.append(encodeListItem(si.next().toString(), delim));
				while (si.hasNext()) {
					ns.append(delim);
					ns.append(encodeListItem(si.next().toString(), delim));
				}
			}
			return ns.toString();
		}
		
		/**
		 * Decode a text list separated by '|' and encoded by encodeListItem.
		 * 
		 * @param s		String representing the list
		 * @return		Array of strings resulting from list
		 */
		ArrayList<T> decodeList(String s, char delim, boolean allowBlank) {
			StringBuilder ns = new StringBuilder();
			ArrayList<T> list = new ArrayList<T>();
			if (s == null)
				return list;

			boolean inEsc = false;
			for (int i = 0; i < s.length(); i++){
			    char c = s.charAt(i);
			    if (inEsc) {
			    	switch(c) {
				    case '\\':
			    		ns.append(c);
				    	break;		    	
				    case 'r':
			    		ns.append('\r');
				    	break;		    	
				    case 't':
			    		ns.append('\t');
				    	break;		    	
				    case 'n':
			    		ns.append('\n');
				    	break;		    	
				    default:
				    	ns.append(c);
				    	break;
			    	}
		    		inEsc = false;
			    } else {
				    switch (c) {
				    case '\\':
			    		inEsc = true;
				    	break;
				    default:
				    	if (c == delim) {
				    		String source = ns.toString();
				    		if (allowBlank || source.length() > 0)
						    	list.add(get(source));
					    	ns.setLength(0);
					    	break;
				    	} else {
					    	ns.append(c);
					    	break;
				    	}
				    }
			    }
			}
			// It's important to send back even an empty item.
    		String source = ns.toString();
    		if (allowBlank || source.length() > 0)
		    	list.add(get(source));
			return list;
		}
	}

	/**
	 * Decode a text list separated by '|' and encoded by encodeListItem.
	 * 
	 * @param s		String representing the list
	 * @return		Array of strings resulting from list
	 */
	static ArrayList<String> decodeList(String s, char delim) {
		StringBuilder ns = new StringBuilder();
		ArrayList<String> list = new java.util.ArrayList<String>();
		boolean inEsc = false;
		for (int i = 0; i < s.length(); i++){
		    char c = s.charAt(i);
		    if (inEsc) {
		    	switch(c) {
			    case '\\':
		    		ns.append(c);
			    	break;		    	
			    case 'r':
		    		ns.append('\r');
			    	break;		    	
			    case 't':
		    		ns.append('\t');
			    	break;		    	
			    case 'n':
		    		ns.append('\n');
			    	break;		    	
			    default:
			    	ns.append(c);
			    	break;
		    	}
	    		inEsc = false;
		    } else {
			    switch (c) {
			    case '\\':
		    		inEsc = true;
			    	break;
			    default:
			    	if (c == delim) {
				    	list.add(ns.toString());
				    	ns.setLength(0);
				    	break;
			    	} else {
				    	ns.append(c);
				    	break;
			    	}
			    }
		    }
		}
		// It's important to send back even an empty item.
    	list.add(ns.toString());
		return list;
	}

	/**
	 * Add the current text data to the collection if not present, otherwise 
	 * append the data as a list.
	 * 
	 * @param key	Key for data to add
	 */
	static public void appendOrAdd(Bundle values, String key, String value) {
		String s = Utils.encodeListItem(value, '|');
		if (!values.containsKey(key) || values.getString(key).length() == 0) {
			values.putString(key, s);
		} else {
			String curr = values.getString(key);
			values.putString(key, curr + "|" + s);
		}
	}

	/**
	 * Given a URL, get an image and save to a file, optionally appending a suffic to the file.
	 * 
	 * @param urlText			Image file URL
	 * @param filenameSuffix	Suffix to add
	 *
	 * @return	Downloaded filespec
	 */
	static public String saveThumbnailFromUrl(String urlText, String filenameSuffix) {
		URL u;
		try {
			u = new URL(urlText);
		} catch (MalformedURLException e) {
			Logger.logError(e);
			return "";
		}
		HttpURLConnection c;
		InputStream in = null;
		try {
			c = (HttpURLConnection) u.openConnection();
			c.setConnectTimeout(30000);
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			in = c.getInputStream();
		} catch (IOException e) {
			Logger.logError(e);
			return "";
		}

		String filename = "";
		FileOutputStream f = null;
		try {
			filename = CatalogueDBAdapter.fetchThumbnailFilename(0, true, filenameSuffix);
			f = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			Logger.logError(e);
			return "";
		}
		
		try {
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ( (len1 = in.read(buffer)) > 0 ) {
				f.write(buffer,0, len1);
			}
			f.close();
		} catch (IOException e) {
			Logger.logError(e);
			return "";
		}
		return filename;
	}

	/**
	 * Utility routine to get the data from a URL. Makes sure timeout is set to avoid application
	 * stalling.
	 * 
	 * @param url		URL to retrieve
	 * @return
	 * @throws UnknownHostException 
	 */
	static public InputStream getInputStream(URL url) throws UnknownHostException {
		
		synchronized (url) {
			
			int retries = 3;
			while (true) {
				try {
					java.net.URLConnection conn = url.openConnection();
					conn.setConnectTimeout(30000);
					return conn.getInputStream();
				} catch (java.net.UnknownHostException e) {
					Logger.logError(e);
					retries--;
					if (retries-- == 0)
						throw e;
					try { Thread.sleep(500); } catch(Exception junk) {};
				} catch (Exception e) {
					Logger.logError(e);
					throw new RuntimeException(e);
				}			
			}
		}
	}
	
	/*
	 *@return boolean return true if the application can access the internet
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * If there is a '__thumbnails' key, pick the largest image, rename it
	 * and delete the others. Finally, remove the key.
	 * 
	 * @param result	Book data
	 */
	static public void cleanupThumbnails(Bundle result) {
    	if (result.containsKey("__thumbnail")) {
    		long best = -1;
    		int bestFile = -1;

    		// Parse the list
    		ArrayList<String> files = Utils.decodeList(result.getString("__thumbnail"), '|');

    		// Just read the image files to get file size
    		BitmapFactory.Options opt = new BitmapFactory.Options();
    		opt.inJustDecodeBounds = true;

    		// Scan, finding biggest
    		for(int i = 0; i < files.size(); i++) {
    			String filespec = files.get(i);
	    		File file = new File(filespec);
	    		if (file.exists()) {
		    	    BitmapFactory.decodeFile( filespec, opt );
		    	    // If no size info, assume file bad and skip
		    	    if ( opt.outHeight > 0 && opt.outWidth > 0 ) {
		    	    	long size = opt.outHeight * opt.outWidth;
		    	    	if (size > best) {
		    	    		best = size;
		    	    		bestFile = i;
		    	    	}
		    	    }	    		
	    		}
    		}

    		// Delete all but the best one. Note there *may* be no best one,
    		// so all would be deleted. We do this first in case the list 
    		// contains a file with the same name as the target of our
    		// rename.
    		for(int i = 0; i < files.size(); i++) {
    			if (i != bestFile) {
		    		File file = new File(files.get(i));
		    		file.delete();
    			}
    		}
    		// Get the best file (if present) and rename it.
			if (bestFile >= 0) {
	    		File file = new File(files.get(bestFile));
	    		file.renameTo(CatalogueDBAdapter.fetchThumbnail(0));
			}
    		// Finally, cleanup the data
    		result.remove("__thumbnail");
    		result.putBoolean(CatalogueDBAdapter.KEY_THUMBNAIL, true);
    	}
	}

	/**
	 * Convert text at specified key to proper case.
	 * 
	 * @param values
	 * @param key
	 */
	public static void doProperCase(Bundle values, String key) {
		if (!values.containsKey(key))
			return;
		values.putString(key, properCase(values.getString(key)));
	}

	public static String properCase(String inputString) {
		StringBuilder ff = new StringBuilder(); 
		String outputString;
		int wordnum = 0;

		try {
			for(String f: inputString.split(" ")) {
				if(ff.length() > 0) { 
					ff.append(" "); 
				} 
				wordnum++;
				String word = f.toLowerCase();
	
				if (word.substring(0,1).matches("[\"\\(\\./\\\\,]")) {
					wordnum = 1;
					ff.append(word.substring(0,1));
					word = word.substring(1,word.length());
				}
	
				/* Do not convert 1st char to uppercase in the following situations */
				if (wordnum > 1 && word.matches("a|to|at|the|in|and|is|von|de|le")) {
					ff.append(word);
					continue;
				} 
				try {
					if (word.substring(0,2).equals("mc")) {
						ff.append(word.substring(0,1).toUpperCase());
						ff.append(word.substring(1,2));
						ff.append(word.substring(2,3).toUpperCase());
						ff.append(word.substring(3,word.length()));
						continue;
					}
				} catch (StringIndexOutOfBoundsException e) {
					// do nothing and continue;
				}
	
				try {
					if (word.substring(0,3).equals("mac")) {
						ff.append(word.substring(0,1).toUpperCase());
						ff.append(word.substring(1,3));
						ff.append(word.substring(3,4).toUpperCase());
						ff.append(word.substring(4,word.length()));
						continue;
					}
				} catch (StringIndexOutOfBoundsException e) {
					// do nothing and continue;
				}
	
				try {
					ff.append(word.substring(0,1).toUpperCase());
					ff.append(word.substring(1,word.length()));
				} catch (StringIndexOutOfBoundsException e) {
					ff.append(word);
				}
			}
	
			/* output */ 
			outputString = ff.toString();
		} catch (StringIndexOutOfBoundsException e) {
			//empty string - do nothing
			outputString = inputString;
		}
		return outputString;
	}

	/**
	 * Check if passed bundle contains a non-blank string at key k.
	 * 
	 * @param b			Bundle to check
	 * @param key		Key to check for
	 * @return			Present/absent
	 */
	public static boolean isNonBlankString(Bundle b, String key) {
		try {
			if (b.containsKey(key)) {
				String s = b.getString(key);
				return (s != null && s.length() > 0);
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Join the passed array of strings, with 'delim' between them.
	 * 
	 * @param sa		Array of strings to join
	 * @param delim		Delimiter to place between entries
	 * 
	 * @return			The joined strings
	 */
	static String join(String[] sa, String delim) {
		// Simple case, return empty string
		if (sa.length <= 0)
			return "";

		// Initialize with first
		StringBuilder buf = new StringBuilder(sa[0]);

		if (sa.length > 1) {
			// If more than one, loop appending delim then string.
			for(int i = 1; i < sa.length; i++) {
				buf.append(delim);
				buf.append(sa[i]);
			}
		}
		// Return result
		return buf.toString();
	}

	/**
	 * Get a value from a bundle and convert to a long.
	 * 
	 * @param b		Bundle
	 * @param key	Key in bundle
	 * 
	 * @return		Result
	 */
	public static long getAsLong(Bundle b, String key) {
		Object o = b.get(key);
		if (o instanceof Long) {
			return (Long) o;
		} else if (o instanceof String) {
			return Long.parseLong((String)o);
		} else if (o instanceof Integer) {
			return ((Integer)o).longValue();
		} else {
			throw new RuntimeException("Not a long value");
		}
	}

	/**
	 * Get a value from a bundle and convert to a long.
	 * 
	 * @param b		Bundle
	 * @param key	Key in bundle
	 * 
	 * @return		Result
	 */
	public static String getAsString(Bundle b, String key) {
		Object o = b.get(key);
		return o.toString();
	}

	public interface ItemWithIdFixup {
		long fixupId(CatalogueDBAdapter db);
		long getId();
		boolean isUniqueById();
	}

	/**
	 * Passed a list of Objects, remove duplicates based on the toString result.
	 * 
	 * TODO Add author_aliases table to allow further pruning (eg. Joe Haldeman == Jow W Haldeman).
	 * TODO Add series_aliases table to allow further pruning (eg. 'Amber Series' <==> 'Amber').
	 * 
	 * @param db		Database connection to lookup IDs
	 * @param list		List to clean up
	 */
	public static <T extends ItemWithIdFixup> void pruneList(CatalogueDBAdapter db, ArrayList<T> list) {
		Hashtable<String,Boolean> names = new Hashtable<String,Boolean>();
		Hashtable<Long,Boolean> ids = new Hashtable<Long,Boolean>();

		// We have to go forwards through the list because 'first item' is important,
		// but we also can't delete things as we traverse if we are going forward. So
		// we build a list of items to delete.
		ArrayList<Integer> toDelete = new ArrayList<Integer>();

		for(int i = 0; i < list.size(); i++) {
			T item = list.get(i);
			Long id = item.fixupId(db);
			String name = item.toString().trim().toUpperCase();
			
			// Series special case - same name different series number.
			// This means different series positions will have the same ID but will have
			// different names; so ItemWithIdFixup contains the 'isUniqueById()' method.
			if (ids.containsKey(id) && !names.containsKey(name) && !item.isUniqueById()) {
				ids.put(id, true);
				names.put(name, true);
			} else if (names.containsKey(name) || (id != 0 && ids.containsKey(id))) {
				toDelete.add(i);
			} else {
				ids.put(id, true);
				names.put(name, true);
			}
		}
		for(int i = toDelete.size() - 1; i >= 0; i--)
			list.remove(toDelete.get(i).intValue());
	}

	/**
	 * Convert a array of objects to a string.
	 * 
	 * @param <T>
	 * @param a		Array
	 * @return		Resulting string
	 */
	public static <T> String ArrayToString(ArrayList<T> a) {
		String details = "";

		for (T i : a) {
			if (details.length() > 0)
				details += "|";
			details += Utils.encodeListItem(i.toString(), '|');			
		}
		return details;
	}
	
	// TODO: Make sure all URL getters use this if possible.
	static public void parseUrlOutput(String path, SAXParserFactory factory, DefaultHandler handler) {
		SAXParser parser;
		URL url;

		try {
			url = new URL(path);
			parser = factory.newSAXParser();
			parser.parse(Utils.getInputStream(url), handler);
			// Dont bother catching general exceptions, they will be caught by the caller.
		} catch (MalformedURLException e) {
			String s = "unknown";
			try { s = e.getMessage(); } catch (Exception e2) {};
			Logger.logError(e, s);
		} catch (ParserConfigurationException e) {
			String s = "unknown";
			try { s = e.getMessage(); } catch (Exception e2) {};
			Logger.logError(e, s);
		} catch (SAXException e) {
			String s = e.getMessage(); // "unknown";
			try { s = e.getMessage(); } catch (Exception e2) {};
			Logger.logError(e, s);
		} catch (java.io.IOException e) {
			String s = "unknown";
			try { s = e.getMessage(); } catch (Exception e2) {};
			Logger.logError(e, s);
		}
	}
	
	public static Bitmap fetchFileIntoImageView(File file, ImageView destView, int maxWidth, int maxHeight, boolean exact) {
		// Get the file, if it exists. Otherwise set 'help' icon and exit.
		if (!file.exists()) {
			if (destView != null)
				destView.setImageResource(android.R.drawable.ic_menu_help);
			return null;
		}
		
		Bitmap bm = null;					// resultant Bitmap (which we will return) 
		String filename = file.getPath();	// Full file spec
		
		// Read the file to get file size
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile( filename, opt );
		
		// If no size info, or a single pixel, assume file bad and set the 'alert' icon
		if ( opt.outHeight <= 0 || opt.outWidth <= 0 || (opt.outHeight== 1 && opt.outWidth == 1) ) {
			if (destView != null)
				destView.setImageResource(android.R.drawable.ic_dialog_alert);
			return null;
		}
		
		// Next time we don't just want the bounds, we want the file
		opt.inJustDecodeBounds = false;
		
		// Work out how to scale the file to fit in required bbox
		float widthRatio = (float)maxWidth / opt.outWidth; 
		float heightRatio = (float)maxHeight / opt.outHeight;
		
		// Work out scale so that it fit exactly
		float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
		
		// Note that inSampleSize seems to ALWAYS be forced to a power of 2, no matter what we
		// specify, so we just work with powers of 2.
		int idealSampleSize = (int)Math.ceil(1/ratio); // This is the sample size we want to use
		// Get the nearest *bigger* power of 2.
		int samplePow2 = (int)Math.ceil(Math.log(idealSampleSize)/Math.log(2));
		
		try {
			if (exact) {
				// Create one bigger than needed and scale it; this is an attempt to improve quality.
				opt.inSampleSize = samplePow2 / 2;
				if (opt.inSampleSize < 1)
					opt.inSampleSize = 1;
				
				bm = BitmapFactory.decodeFile( filename, opt );
				android.graphics.Matrix matrix = new android.graphics.Matrix();
				// Fixup ratio based on new sample size and scale it.
				ratio = ratio / (1.0f / opt.inSampleSize);
				matrix.postScale(ratio, ratio);
				bm = Bitmap.createBitmap(bm, 0, 0, opt.outWidth, opt.outHeight, matrix, true); 
			} else {
				// Use a scale that will make image *no larger than* the desired size
				if (ratio < 1.0f)
					opt.inSampleSize = samplePow2;
				bm = BitmapFactory.decodeFile( filename, opt );
			}
		} catch (OutOfMemoryError e) {
			return null;
		}
		
		// Set ImageView and return bitmap
		if (destView != null)
			destView.setImageBitmap(bm);
		return bm;
	}
	
	public static void showLtAlertIfNecessary(Context context, boolean always, String suffix) {
		if (USE_LT) {
			LibraryThingManager ltm = new LibraryThingManager(context);
			if (!ltm.isAvailable())
				StandardDialogs.needLibraryThingAlert(context, always, suffix);		
		}
	}

	private static String[] mPurgeableFilePrefixes = new String[]{Utils.LOCATION + "DbUpgrade", Utils.LOCATION + "DbExport", "error.log", "tmp"};
	private static String[] mDebugFilePrefixes = new String[]{Utils.LOCATION + "DbUpgrade", Utils.LOCATION + "DbExport", "error.log", "export.csv"};

	/**
	 * Collect and send debug info to a support email address. 
	 * 
	 * THIS SHOULD NOT BE A PUBLICALLY AVAILABLE MAINING LIST OR FORUM!
	 * 
	 * @param context
	 * @param dbHelper
	 */
	public static void sendDebugInfo(Context context, CatalogueDBAdapter dbHelper) {
		// Create a temp DB copy.
		String tmpName = Utils.LOCATION + "DbExport-tmp.db";
		dbHelper.backupDbFile(tmpName);
		File dbFile = new File(Utils.EXTERNAL_FILE_PATH + "/" + tmpName);
		dbFile.deleteOnExit();
		// setup the mail message
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, context.getString(R.string.debug_email).split(";"));
		String subject = "[" + context.getString(R.string.app_name) + "] " + context.getString(R.string.debug_subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.debug_body));
		//has to be an ArrayList
		ArrayList<Uri> uris = new ArrayList<Uri>();
		//convert from paths to Android friendly Parcelable Uri's
		ArrayList<String> files = new ArrayList<String>();
		
		// Find all files of interest to send
		File dir = new File(Utils.EXTERNAL_FILE_PATH);
		try {
			for (String name : dir.list()) {
				boolean send = false;
				for(String prefix : mDebugFilePrefixes)
					if (name.startsWith(prefix)) {
						send = true;
						break;
					}
				if (send)
					files.add(name);
			}
			
			// Build the attachment list
			for (String file : files)
			{
				File fileIn = new File(Utils.EXTERNAL_FILE_PATH + "/" + file);
				if (fileIn.exists() && fileIn.length() > 0) {
					Uri u = Uri.fromFile(fileIn);
					uris.add(u);
				}
			}
			// Send it, if there are any files to send.
			if (uris.size() == 0) {
				Toast.makeText(context, R.string.no_debug_info, Toast.LENGTH_LONG).show();
			} else {
				emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
				context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));        	
			}
		} catch (NullPointerException e) {
			Logger.logError(e);
			Toast.makeText(context, R.string.export_failed_sdcard, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Cleanup any purgeable files.
	 */
	public static void cleanupFiles() {
		if (Utils.sdCardWritable()) {
	        File dir = new File(Utils.EXTERNAL_FILE_PATH);
	        for (String name : dir.list()) {
	        	boolean purge = false;
	        	for(String prefix : mPurgeableFilePrefixes)
	        		if (name.startsWith(prefix)) {
	        			purge = true;
	        			break;
	        		}
	        	if (purge)
		        	try {
		        		File file = new File(Utils.EXTERNAL_FILE_PATH + "/" + name);
			        	file.delete();
		        	} catch (Exception e) {        		
		        	}
	        }
		}
	}

	/**
	 * Get the total size of purgeable files.
	 * @return	size, in bytes
	 */
	public static long cleanupFilesTotalSize() {
		if (!Utils.sdCardWritable())
			return 0;

		long totalSize = 0;

		File dir = new File(Utils.EXTERNAL_FILE_PATH);
        for (String name : dir.list()) {
        	boolean purge = false;
        	for(String prefix : mPurgeableFilePrefixes)
        		if (name.startsWith(prefix)) {
        			purge = true;
        			break;
        		}
        	if (purge)
	        	try {
	        		File file = new File(Utils.EXTERNAL_FILE_PATH + "/" + name);
	        		totalSize += file.length();
	        	} catch (Exception e) {        		
	        	}
        }
        return totalSize;
	}
}

