package com.eleybourn.bookcatalogue;

import android.content.SharedPreferences;

public class SearchNaverThread extends SearchThread {

	public SearchNaverThread(TaskManager manager, TaskHandler taskHandler,
			String author, String title, String isbn, boolean fetchThumbnail) {
		super(manager, taskHandler, author, title, isbn, fetchThumbnail);
	}

	@Override
	protected void onRun() {
		//
		//	Naver
		//
		this.doProgress(getString(R.string.searching_naver_books), 0);
		if (!isAvailable()) {
			return;
		}
		
		try {
			NaverManager.searchNaver(getDevKey(), mIsbn, mAuthor, mTitle, mBookData, mFetchThumbnail);
			// Look for series name and clear KEY_TITLE
			checkForSeriesName();
		} catch (Exception e) {
			Logger.logError(e);
			showException(R.string.searching_naver_books, e);
		}
	}
	
	public boolean isAvailable() {
		return getDevKey().length() > 0 && SearchThread.isAvailable(mManager.getAppContext(), "Naver");
	}

	private String getDevKey() {
		SharedPreferences prefs = mManager.getAppContext().getSharedPreferences("bookCatalogue", android.content.Context.MODE_PRIVATE);
		return prefs.getString(NaverManager.NAVER_DEVKEY_PREF_NAME, "");
	}
}
