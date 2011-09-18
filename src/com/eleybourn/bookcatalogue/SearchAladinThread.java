package com.eleybourn.bookcatalogue;

public class SearchAladinThread extends SearchThread {

	public SearchAladinThread(TaskManager manager, TaskHandler taskHandler,
			String author, String title, String isbn, boolean fetchThumbnail) {
		super(manager, taskHandler, author, title, isbn, fetchThumbnail);
	}

	@Override
	protected void onRun() {
		//
		//	Amazon
		//
		this.doProgress(getString(R.string.searching_aladin_books), 0);

		try {
			AladinManager.searchAladin(mIsbn, mAuthor, mTitle, mBookData, mFetchThumbnail);
			// Look for series name and clear KEY_TITLE
			checkForSeriesName();
		} catch (Exception e) {
			Logger.logError(e);
			showException(R.string.searching_aladin_books, e);
		}
	}

}
