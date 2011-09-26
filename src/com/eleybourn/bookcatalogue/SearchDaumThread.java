package com.eleybourn.bookcatalogue;

public class SearchDaumThread extends SearchThread {

	public SearchDaumThread(TaskManager manager, TaskHandler taskHandler,
			String author, String title, String isbn, boolean fetchThumbnail) {
		super(manager, taskHandler, author, title, isbn, fetchThumbnail);
	}

	@Override
	protected void onRun() {
		//
		//	Daum
		//
		this.doProgress(getString(R.string.searching_daum_books), 0);
		if (!SearchThread.isAvailable(mManager.getAppContext(), "Daum")) {
			return;
		}
		
		try {
			DaumManager.searchDaum(mIsbn, mAuthor, mTitle, mBookData, mFetchThumbnail);
			// Look for series name and clear KEY_TITLE
			checkForSeriesName();
		} catch (Exception e) {
			Logger.logError(e);
			showException(R.string.searching_daum_books, e);
		}
	}

}
