/*
 * @copyright 2010 Evan Leybourn
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Bundle;

/** 
 * An XML handler for the Naver return 
 * 
 * An example response looks like;
 * <rss version="2.0">
 *   <channel>
 *     <title>
 *       Naver Open API - book ::'Harry Potter and the Sorcerer's Stone'
 *     </title>
 *     <link>http://search.naver.com</link>
 *     <description>Naver Search Result</description>
 *     <lastBuildDate>Tue, 03 Jan 2012 22:25:28 +0900</lastBuildDate>
 *     <total>80</total>
 *     <start>1</start>
 *     <display>1</display>
 *     <item>
 *       <title>
 *         <strong>Harry Potter and the Sorcerer's Stone</strong> - Audio CD 7장
 *       </title>
 *       <link>
 *         http://openapi.naver.com/l?AAAC2LQQrCMBAAX5NeAiVNIq2HHAQpHoU+QDbdLSlqU9dVyO+tUJjDMDCvD3EJF2Au+ppFiDUsqCWRHjKPxMTKtm89SF6okrJSiDnfqzuVMHVj46O3DRo4mhinSM6b9tCABTQIVWKaQhJZlTsp22/833qBL3E95uceMO5yQxKYH/WatqOPMyp3tt53rvsB+Y6Sy6cAAAA=
 *       </link>
 *       <image>
 *       http://bookthumb.phinf.naver.net/cover/002/448/00244838.jpg?type=m1
 *       </image>
 *       <author>조앤 K. 롤링</author>
 *       <price>65400</price>
 *       <discount>43480</discount>
 *       <publisher>RandomHouse</publisher>
 *       <pubdate>20001201</pubdate>
 *       <isbn>0807281956 9780807281956</isbn>
 *       <description>
 *         <strong>HARRY POTTER</strong> HAS NO IDEA HOW FAMOUS HE IS.... TO <strong>THE</strong> UNIQUE CURRICULUM <strong>AND</strong> COLORFUL FACULTY AT HIS UNUSUAL SCHOOL, <strong>HARRY</strong> FINDS...
 *       </description>
 *     </item>
 *   </channel>
 * </rss>
 */
public class SearchNaverHandler extends DefaultHandler {
	private Bundle mBookData;
	private StringBuilder mBuilder;
	private String mThumbnailUrl = "";
	private int mThumbnailSize = -1;
	private static boolean mFetchThumbnail;
	
	/* How many results found */
	public int count = 0;
	/* A flag to identify if we are in the correct node */
	private boolean entry = false;
	private boolean image = false;
	private boolean done = false;
	
	/* The XML element names */
	public static String ID = "id";
	public static String TOTALRESULTS = "display";
	public static String ENTRY = "item";
	public static String URL = "link";
	public static String AUTHOR = "author";
	public static String TITLE = "title";
	public static String ISBN = "isbn";
	//public static String ISBNOLD = "isbn";
	public static String DATE_PUBLISHED = "pubdate";
	public static String PUBLISHER = "publisher";
	public static String IMAGE = "image";
	public static String DESCRIPTION = "description";
	//public static String GENRE = "category";

	SearchNaverHandler(Bundle bookData, boolean fetchThumbnail) {
		mBookData = bookData;
		mFetchThumbnail = fetchThumbnail;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		mBuilder.append(ch, start, length);
	}

	/**
	 * Add the current characters to the book collection if not already present.
	 * 
	 * @param key	Key for data to add
	 */
	private void addIfNotPresent(String key) {
		String value = mBuilder.toString().replaceAll("&lt;.?b&gt;", "").replaceAll("<.?b>", "").trim();
		addIfNotPresent(key, value);
	}
	
	private void addIfNotPresent(String key, String value) {
		if (!mBookData.containsKey(key) || mBookData.getString(key).length() == 0) {
			mBookData.putString(key, value);
		}		
	}

	/**
	 * Add the current characters to the book collection if not already present.
	 * 
	 * @param key	Key for data to add
	 * @param value	Value to compare to; if present but equal to this, it will be overwritten
	 */
	private void addIfNotPresentOrEqual(String key, String value) {
		if (!mBookData.containsKey(key) || mBookData.getString(key).length() == 0 || mBookData.getString(key).equals(value)) {
			mBookData.putString(key, mBuilder.toString().replaceAll("&lt;.?b&gt;", "").replaceAll("<.?b>", "").trim());
		}		
	}

	/**
	 * 	Overridden method to get the best thumbnail, if present.
	 */
	@Override
	public void endDocument() {
		if (mFetchThumbnail && mThumbnailUrl.length() > 0) {
			String filename = Utils.saveThumbnailFromUrl(mThumbnailUrl, "_AM");
			if (filename.length() > 0)
				Utils.appendOrAdd(mBookData, "__thumbnail", filename);			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 * 
	 * Populate the class variables for each appropriate element. 
	 * Also download the thumbnail and store in a tmp location
	 */
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
		try {
			if (localName.equalsIgnoreCase(TOTALRESULTS)){
				count = Integer.parseInt(mBuilder.toString().trim());
			} else if (localName.equalsIgnoreCase(ENTRY)){
				done = true;
				entry = false;
			} else if (entry == true) {
				String string = mBuilder.toString().replaceAll("&lt;.?strong&gt;", "").replaceAll("<.?strong>", "").trim();
//				if (localName.equalsIgnoreCase(URL)) {
//				}
				if (localName.equalsIgnoreCase(AUTHOR)){
					Utils.appendOrAdd(mBookData, CatalogueDBAdapter.KEY_AUTHOR_DETAILS, string);
				} else if (localName.equalsIgnoreCase(TITLE)){
					addIfNotPresent(CatalogueDBAdapter.KEY_TITLE);
				} else if (localName.equalsIgnoreCase(ISBN)){
					for (String sub:string.split(" ")) {
						if (!mBookData.containsKey(CatalogueDBAdapter.KEY_ISBN) 
								|| mBookData.getString(CatalogueDBAdapter.KEY_ISBN).length() < sub.length()) {
							mBookData.putString(CatalogueDBAdapter.KEY_ISBN, sub);
						}						
					}
				} else if (localName.equalsIgnoreCase(PUBLISHER)){
					addIfNotPresent(CatalogueDBAdapter.KEY_PUBLISHER);
				} else if (localName.equalsIgnoreCase(DATE_PUBLISHED)){
					String date = string.substring(0, 4) + "-" + string.substring(4, 6) + "-" + string.substring(6);
					addIfNotPresent(CatalogueDBAdapter.KEY_DATE_PUBLISHED, date);
//				} else if (localName.equalsIgnoreCase(PAGES)){
//					addIfNotPresentOrEqual(CatalogueDBAdapter.KEY_PAGES, "0");
				} else if (localName.equalsIgnoreCase(DESCRIPTION)){
					addIfNotPresent(CatalogueDBAdapter.KEY_DESCRIPTION);
//				} else if (localName.equalsIgnoreCase(GENRE)) {
//					mBookData.putString(CatalogueDBAdapter.KEY_GENRE, string);
				} else if (localName.equalsIgnoreCase(IMAGE)) {
					mThumbnailUrl = string;
				}
			}
			mBuilder.setLength(0);			
		} catch (Exception e) {
			Logger.logError(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 * 
	 * Start the XML document and the StringBuilder object
	 */
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		mBuilder = new StringBuilder();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 * 
	 * Start each XML element. Specifically identify when we are in the item element and set the appropriate flag.
	 */
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (done == false && localName.equalsIgnoreCase(ENTRY)){
			entry = true;
		}
	}
}
 
