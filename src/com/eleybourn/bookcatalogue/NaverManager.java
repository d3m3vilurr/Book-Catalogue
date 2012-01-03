package com.eleybourn.bookcatalogue;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.content.SharedPreferences;
import android.os.Bundle;

public class NaverManager {
	/**
	 * 
	 * This searches the amazon REST site based on a specific isbn. It proxies through lgsolutions.com.au
	 * due to amazon not support mobile devices
	 * 
	 * @param mIsbn The ISBN to search for
	 * @return The book array
	 */
	/** Name of preference that contains the dev key for the user */
	public static final String NAVER_DEVKEY_PREF_NAME = "naver_devkey";
	
	static public void searchNaver(String key, String mIsbn, String mAuthor, String mTitle, Bundle bookData, boolean fetchThumbnail) {
		//replace spaces with %20
		mAuthor = mAuthor.replace(" ", "%20");
		mTitle = mTitle.replace(" ", "%20");
		
		String path = "http://openapi.naver.com/search?target=book&display=3&start=1";
		path += "&key=" + key;
		if (mIsbn.equals("")) {
			String keyword;
			if (mAuthor.equals("")) {
				keyword = mTitle;
			} else if (mTitle.equals("")) {
				keyword = mAuthor;
			} else {
				keyword = mTitle + "%20" + mAuthor;
			}		
			path += "&query=" + keyword;
		} else {
			path += "&query=" + mIsbn;
		}
		URL url;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		SearchNaverHandler handler = new SearchNaverHandler(bookData, fetchThumbnail);

		try {
			url = new URL(path);
			parser = factory.newSAXParser();
			// We can't Toast anything here, so let exceptions fall through.
			parser.parse(Utils.getInputStream(url), handler);
		} catch (MalformedURLException e) {
			Logger.logError(e);
		} catch (ParserConfigurationException e) {
			Logger.logError(e);
		} catch (SAXException e) {
			Logger.logError(e);
		} catch (Exception e) {
			Logger.logError(e);
		}
		return;
	}
}
