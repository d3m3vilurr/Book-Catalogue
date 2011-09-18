package com.eleybourn.bookcatalogue;

import android.os.Bundle;

//TODO: Get editions via: http://m.aladin.co.kr/m/msearch.aspx?SearchTarget=All&SearchWord=059035342X

public class AladinManager {
	
	static public void searchAladin(String mIsbn, String mAuthor, String mTitle, Bundle bookData, boolean fetchThumbnail) {
		//replace spaces with %20
		mAuthor = mAuthor.replace(" ", "%20");
		mTitle = mTitle.replace(" ", "%20");
		String keyword;
		if (mIsbn.equals("")) {
			if (mAuthor.equals("")) {
				keyword = mTitle;
			} else if (mTitle.equals("")) {
				keyword = mAuthor;
			} else {
				keyword = mTitle + "%20" + mAuthor;
			}			
		} else {
			keyword = mIsbn;
		}
		String path;
		String id = "";
		path = "http://m.aladin.co.kr/m/msearch.aspx?SearchTarget=All"
			 + "&SearchWord=" + keyword;
		SearchAladinBooksHandler handler = new SearchAladinBooksHandler(path);
		int count = handler.getCount();
		if (count > 0) {
			id = handler.getId();
		}
		new SearchAladinBooksEntryHandler(id, bookData, fetchThumbnail);
		addIfNotPresent(bookData, CatalogueDBAdapter.KEY_DATE_PUBLISHED, handler.getDatePublished());
	}
	
	private static void addIfNotPresent(Bundle bookData, String key, String value) {
		if (!bookData.containsKey(key) || bookData.getString(key).length() == 0) {
			bookData.putString(key, value);
		}		
	}
	
}
