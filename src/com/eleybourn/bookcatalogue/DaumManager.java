package com.eleybourn.bookcatalogue;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.os.Bundle;

public class DaumManager {
	/**
	 * 
	 * This searches the amazon REST site based on a specific isbn. It proxies through lgsolutions.com.au
	 * due to amazon not support mobile devices
	 * 
	 * @param mIsbn The ISBN to search for
	 * @return The book array
	 */
	static public void searchDaum(String mIsbn, String mAuthor, String mTitle, Bundle bookData, boolean fetchThumbnail) {
		//replace spaces with %20
		mAuthor = mAuthor.replace(" ", "%20");
		mTitle = mTitle.replace(" ", "%20");
		
		String path = "http://apis.daum.net/search/book?apikey=DAUM_SEARCH_DEMO_APIKEY&output=xml";
		if (mIsbn.equals("")) {
			String keyword;
			if (mAuthor.equals("")) {
				keyword = mTitle;
			} else if (mTitle.equals("")) {
				keyword = mAuthor;
			} else {
				keyword = mTitle + "%20" + mAuthor;
			}		
			path += "&sort=accu&q=" + keyword;
		} else {
			path += "&searchType=isbn&q=" + mIsbn;
		}
		URL url;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		SearchDaumHandler handler = new SearchDaumHandler(bookData, fetchThumbnail);

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
