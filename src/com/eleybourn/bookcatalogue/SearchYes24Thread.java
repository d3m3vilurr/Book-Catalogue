package com.eleybourn.bookcatalogue;

public class SearchYes24Thread extends SearchThread {

	public SearchYes24Thread(TaskManager manager, TaskHandler taskHandler,
			String author, String title, String isbn, boolean fetchThumbnail) {
		super(manager, taskHandler, author, title, isbn, fetchThumbnail);
	}

	@Override
	protected void onRun() {
		try {
			//
			//	Yes24
			//
			doProgress(getString(R.string.searching_yes24_books), 0);

			try {
				Yes24BooksManager.searchYes24(mIsbn, mAuthor, mTitle, mBookData, mFetchThumbnail);					
			} catch (Exception e) {
				Logger.logError(e);
				showException(R.string.searching_yes24_books, e);
			}

			// Look for series name and clear KEY_TITLE
			checkForSeriesName();

		} catch (Exception e) {
			Logger.logError(e);
			showException(R.string.search_fail, e);
		}
	}

}
