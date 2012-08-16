package org.ligi.fast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ligi.fast.util.FileHelper;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

/**
 * The main Activity for this App
 * 
 * @author Marcus -ligi- Büschleb
 * 
 *         License GPLv3
 */
public class SearchActivity extends SherlockActivity {

	private List<AppInfo> pkgAppsListTemp;
		
	private AppInfoAdapter mAdapter;

	private File index_file;
	private String new_index="";
	private String old_index="";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_search);

	
		pkgAppsListTemp = new ArrayList<AppInfo>();

		index_file = new File(getCacheDir(), "index2.csv");

		
		
		try {
			old_index = FileHelper.file2String(index_file);
			String[] lines = old_index.split("\n");
			Log.i("FAST","new index:"+old_index);
			for (String line : lines) {
				if (line.length()>0)
					pkgAppsListTemp.add(new AppInfo(this, line));
			}
		} catch (Exception e) {

		}

		mAdapter = new AppInfoAdapter(this,pkgAppsListTemp);
		
		if (pkgAppsListTemp.size() == 0) 			
			new BaseAppGatherAsyncTask(this) {

				private LoadingDialog mLoadingDialog;

				@Override
				protected void onPreExecute() {
					mLoadingDialog = new LoadingDialog(SearchActivity.this);
					mLoadingDialog.show();
				}

				@Override
				protected void onProgressUpdate(AppInfo... values) {
					super.onProgressUpdate(values);

					mLoadingDialog.setIcon(values[0].getIcon());
					mLoadingDialog.setText(values[0].getLabel());

					pkgAppsListTemp.add(values[0]);
					new_index+=values[0].toCacheString()+"\n";
				}

				@Override
				protected void onPostExecute(Void result) {
					mLoadingDialog.dismiss();
					super.onPostExecute(result);
					process_new_index();
				}

			}.execute();

			
		GridView app_list = (GridView) findViewById(R.id.listView);

		disableOverScoll(app_list);

		
		app_list.setAdapter(mAdapter);
	
		getSupportActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO
						| ActionBar.DISPLAY_SHOW_HOME);

		EditText search_et = new EditText(this);
		search_et.setHint("Enter Query");

		search_et.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				mAdapter.setActQuery(s.toString().toLowerCase());
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});
		getSupportActionBar().setCustomView(search_et);
		// getSupportActionBar().set

		app_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				try {
					mAdapter.getAtPosition(pos).getIntent();
				} catch (ActivityNotFoundException e) {
					// e.g. uninstalled while app running - TODO should refresh
					// list
				}
			}

		});

		super.onCreate(savedInstanceState);

	}

	/**
	 * takes the temp apps list as the new all apps index
	 */
	private void process_new_index()  {
		mAdapter.setAllAppsList(pkgAppsListTemp);
		if (!new_index.equals(old_index)) {
			try {
				FileOutputStream fos=new FileOutputStream( index_file);
				fos.write(new_index.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
		
			} catch (IOException e) {
				
			}
		}
	}

	@TargetApi(9)
	// we do a check for SDK Version here - all good
	private void disableOverScoll(GridView gridView) {
		if (Build.VERSION.SDK_INT >= 9)
			gridView.setOverScrollMode(View.OVER_SCROLL_NEVER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_search, menu);
		return true;
	}

	

}
