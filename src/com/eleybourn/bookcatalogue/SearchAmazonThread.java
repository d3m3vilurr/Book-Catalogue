package com.eleybourn.bookcatalogue;

public class SearchAmazonThread extends SearchThread {

	public SearchAmazonThread(TaskManager manager, TaskHandler taskHandler,
			String author, String title, String isbn, boolean fetchThumbnail) {
		super(manager, taskHandler, author, title, isbn, fetchThumbnail);
	}

	@Override
	protected void onRun() {
		//
		//	Amazon
		//
		this.doProgress(getString(R.string.searching_amazon_books), 0);
		if (!SearchThread.isAvailable(mManager.getAppContext(), "Amazon")) {
			return;
		}
		
		try {
			AmazonManager.searchAmazon(mIsbn, mAuthor, mTitle, mBookData, mFetchThumbnail);
			// Look for series name and clear KEY_TITLE
			checkForSeriesName();
		} catch (Exception e) {
			Logger.logError(e);
			showException(R.string.searching_amazon_books, e);
		}
	}

}
