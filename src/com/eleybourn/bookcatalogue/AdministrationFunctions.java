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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.eleybourn.bookcatalogue.ManagedTask.TaskHandler;

/**
 * 
 * This is the Administration page. It contains details about the app, links
 * to my website and email, functions to export and import books and functions to 
 * manage bookshelves.
 * 
 * @author Evan Leybourn
 */
public class AdministrationFunctions extends ActivityWithTasks {
	private static final int ACTIVITY_BOOKSHELF=1;
	private static final int ACTIVITY_FIELD_VISIBILITY=2;
	private static final int ACTIVITY_UPDATE_FROM_INTERNET=3;
	private static final int ACTIVITY_SEARCH_SOURCE=4;
	private CatalogueDBAdapter mDbHelper;
	//private int importUpdated = 0;
	//private int importCreated = 0;
	public static String fileName = Utils.EXTERNAL_FILE_PATH + "/export.csv";
	public static String UTF8 = "utf8";
	public static int BUFFER_SIZE = 8192;
	private ProgressDialog pd = null;
	private int num = 0;
	private boolean finish_after = false;

	public static final String DOAUTO = "do_auto";

	final ExportThread.ExportHandler mExportHandler = new ExportThread.ExportHandler() {
		@Override
		public void onFinish() {
		}
	};

	final ImportThread.ImportHandler mImportHandler = new ImportThread.ImportHandler() {
		@Override
		public void onFinish() {
		}
	};

	final Handler mProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			int total = msg.getData().getInt("total");
			String title = msg.getData().getString("title");
			if (total == 0) {
				pd.dismiss();
				if (finish_after == true) {
					finish();
				}
				Toast.makeText(AdministrationFunctions.this, title, Toast.LENGTH_LONG).show();
				//progressThread.setState(UpdateThumbnailsThread.STATE_DONE);
			} else {
				num += 1;
				pd.incrementProgressBy(1);
				if (title.length() > 21) {
					title = title.substring(0, 20) + "...";
				}
				pd.setMessage(title);
			}
		}
	};

	/**
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			mDbHelper = new CatalogueDBAdapter(this);
			mDbHelper.open();
			setContentView(R.layout.administration_functions);
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey("DOAUTO")) {
				try {
					if (extras.getString(DOAUTO).equals("export")) {
						finish_after = true;
						exportData();
					} else {
						throw new RuntimeException("Unsupported DOAUTO option");
					}
				} catch (NullPointerException e) {
					Logger.logError(e);
				}				
			}
			setupAdmin();
		} catch (Exception e) {
			Logger.logError(e);
		}
	}
	
	/**
	 * This function builds the Administration page in 4 sections. 
	 * 1. The button to goto the manage bookshelves activity
	 * 2. The button to export the database
	 * 3. The button to import the exported file into the database
	 * 4. The application version and link details
	 * 5. The link to paypal for donation
	 */
	public void setupAdmin() {
		/* Bookshelf Link */
		TextView bookshelf = (TextView) findViewById(R.id.bookshelf_label);
		bookshelf.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				manageBookselves();
				return;
			}
		});
		
		/* Manage Fields Link */
		TextView fields = (TextView) findViewById(R.id.fields_label);
		fields.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				manageFields();
				return;
			}
		});
		
		/* Manage Search Sources Link*/
		TextView search_sources = (TextView) findViewById(R.id.search_sources_label);
		search_sources.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				manageSearchSources();
				return;
			}
		});
		
		/* Export Link */
		TextView export = (TextView) findViewById(R.id.export_label);
		export.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exportData();
				return;
			}
		});
		
		/* Import Link */
		TextView imports = (TextView) findViewById(R.id.import_label);
		/* Hack to pass this into the class */
		final AdministrationFunctions pthis = this;
		imports.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Verify - this can be a dangerous operation
				AlertDialog alertDialog = new AlertDialog.Builder(pthis).setMessage(R.string.import_alert).create();
				alertDialog.setTitle(R.string.import_data);
				alertDialog.setIcon(android.R.drawable.ic_menu_info_details);
				alertDialog.setButton(pthis.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						importData();
						//Toast.makeText(pthis, importUpdated + " Existing, " + importCreated + " Created", Toast.LENGTH_LONG).show();
						return;
					}
				}); 
				alertDialog.setButton2(pthis.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//do nothing
						return;
					}
				}); 
				alertDialog.show();
				return;
			}
		});

		// Debug ONLY!
		/* Backup Link */
		TextView backup = (TextView) findViewById(R.id.backup_label);
		backup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDbHelper.backupDbFile();
				Toast.makeText(AdministrationFunctions.this, R.string.backup_success, Toast.LENGTH_LONG).show();
				return;
			}
		});

		/* Export Link */
		TextView thumb = (TextView) findViewById(R.id.thumb_label);
		thumb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateThumbnails();
				return;
			}
		});
	}
	
	/**
	 * Load the Bookshelf Activity
	 */
	private void manageBookselves() {
		Intent i = new Intent(this, Bookshelf.class);
		startActivityForResult(i, ACTIVITY_BOOKSHELF);
	}
	
	/**
	 * Load the Manage Field Visibility Activity
	 */
	private void manageFields() {
		Intent i = new Intent(this, FieldVisibility.class);
		startActivityForResult(i, ACTIVITY_FIELD_VISIBILITY);
	}
	
	/**
	 * Load the Manage Search Source Activity
	 */
	private void manageSearchSources() {
		Intent i = new Intent(this, SearchSource.class);
		startActivityForResult(i, ACTIVITY_SEARCH_SOURCE);
	}
	
	/**
	 * Update all (non-existent) thumbnails
	 * 
	 * There is a current limitation that restricts the search to only books with an ISBN
	 */
	private void updateThumbnails() {
		Intent i = new Intent(this, UpdateFromInternet.class);
		startActivityForResult(i, ACTIVITY_UPDATE_FROM_INTERNET);
	}


	/**
	 * Export all data to a CSV file
	 * 
	 * return void
	 */
	public void exportData() {
		ExportThread thread = new ExportThread(mTaskManager, mExportHandler, this);
		thread.start();		
	}

	/**
	 * This program reads a text file line by line and print to the console. It uses
	 * FileOutputStream to read the file.
	 * 
	 */
	public ArrayList<String> readFile() {
		ArrayList<String> importedString = new ArrayList<String>();
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), UTF8),BUFFER_SIZE);
			String line = "";
			while ((line = in.readLine()) != null) {
				importedString.add(line);
			}
			in.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, R.string.import_failed, Toast.LENGTH_LONG).show();
			Logger.logError(e);
		} catch (IOException e) {
			Toast.makeText(this, R.string.import_failed, Toast.LENGTH_LONG).show();
			Logger.logError(e);
		}
		return importedString;
	}
	
	
	/**
	 * Import all data from the CSV file
	 * 
	 * return void
	 */
	private void importData() {
		ArrayList<String> export = readFile();
		ImportThread thread = new ImportThread(mTaskManager, mImportHandler, export);
		thread.start();		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
		case ACTIVITY_BOOKSHELF:
		case ACTIVITY_FIELD_VISIBILITY:
		case ACTIVITY_UPDATE_FROM_INTERNET:
			//do nothing (yet)
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
	} 

	@Override
	protected void onPause() {
		super.onPause();
	} 
	@Override
	protected void onResume() {
		super.onResume();
	} 

	@Override
	TaskHandler getTaskHandler(ManagedTask t) {
		// If we had a task, create the progress dialog and reset the pointers.
		if (t instanceof ExportThread) {
			return mExportHandler;
		} else if (t instanceof ImportThread) {
			return mImportHandler;
		} else {
			return null;
		}
	}

}
