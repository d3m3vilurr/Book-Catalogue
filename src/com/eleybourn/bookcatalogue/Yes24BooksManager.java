package com.eleybourn.bookcatalogue;

import java.net.URL;

import android.os.Bundle;

// TODO: Get editions via: http://www.yes24.com/searchcorner/Result?query=8925822733

public class Yes24BooksManager {

	static public void searchYes24(String mIsbn, String author, String title, Bundle bookData, boolean fetchThumbnail) {
		//replace spaces with %20
		author = author.replace(" ", "%20");
		title = title.replace(" ", "%20");

		String host = "http://www.yes24.com";
		String path = host + "/searchcorner/Result";
		if (mIsbn.equals("")) {
			path += "?query=" + title + "&mstr_query=" + author + "";
		} else {
			path += "?query=" + mIsbn;
		}
		URL url;
		SearchYes24BooksHandler handler = new SearchYes24BooksHandler(path);
		
		int count = 0;
		count = handler.getCount();
		if (count > 0) {
			String id = handler.getId();
			new SearchYes24BooksEntryHandler(host + id, bookData, fetchThumbnail);
		}
		return;
	}
	
}
