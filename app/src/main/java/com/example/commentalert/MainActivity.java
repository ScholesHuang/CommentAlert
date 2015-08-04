package com.example.commentalert;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.commentalert.jsobject.JavaScriptInterface;
import com.example.commentalert.util.Constants;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	private String LOG_TAG = "MainActivity";
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	/***/
	


	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	WebView commentWebview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager(),this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i)).setIcon(mSectionsPagerAdapter.getPageIcon(i))
					.setTabListener(this));
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		  switch(item.getItemId())
	        {
	        case R.id.action_settings:
	        	Intent pref = new Intent(this,PrefsActivity.class);
	        	startActivity(pref);
	            break;
	        }
		
		return true;
		
		
		
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private Context context;
		
		
		public SectionsPagerAdapter(FragmentManager fm , Context context) {
			super(fm);
			this.context = context;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			final Fragment fragment = new DummySectionFragment(position,context);
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			String title = "";
			switch (position) {
			case 0:
				title = getString(R.string.title_section1).toUpperCase(l);
				break;
			case 1:
				title = getString(R.string.title_section2).toUpperCase(l);
				break;
			/*case 0:
				title = getString(R.string.title_section3).toUpperCase(l);
				break;
			*/
			}
			return title;
		}
		
		public Drawable getPageIcon(int position) {
			Locale l = Locale.getDefault();
			Drawable icon = null;
			switch (position) {
			case 0:
				icon = getResources().getDrawable(R.drawable.appstore_icon);
				break;
			case 1:
				icon = getResources().getDrawable(R.drawable.googleplay_icon);
				break;
			/*case 0:
				icon = getResources().getDrawable(R.drawable.onlineservice);
				break;
			*/
			}
			icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()); 
			return icon;
		}
		
		
		
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		
		private int position;
		final private Context context;
		
		public DummySectionFragment(int position, Context context) {
			this.position = position;
			this.context = context;
		}
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = null;
			switch (position) {
			/*case 0:
				rootView = inflater.inflate(R.layout.customer_service_chatroom,container, false);
				WebView myWebView3 = (WebView)rootView.findViewById(R.id.customer_service_webview);
				myWebView3.setWebContentsDebuggingEnabled(true);
				configureWebView(myWebView3,Constants.STORE_TYPE_APPLE);
				myWebView3.loadUrl("file:///android_asset/web_resource/wechatonweb.htm");
				break;*/
			case 0:
				rootView = inflater.inflate(R.layout.apple_store,container, false);
				WebView myWebView1 = (WebView)rootView.findViewById(R.id.app_store_webview);
				myWebView1.setWebContentsDebuggingEnabled(true);
				configureWebView(myWebView1,Constants.STORE_TYPE_APPLE);
				myWebView1.loadUrl("file:///android_asset/web_resource/appstorecomment.html");
				break;
			case 1:
				rootView = inflater.inflate(R.layout.googleplay_store,container, false);
				WebView myWebView2 = (WebView)rootView.findViewById(R.id.google_play_webview);
				myWebView2.setWebContentsDebuggingEnabled(true);
				configureWebView(myWebView2,Constants.STORE_TYPE_GOOGLEPLAY);
				myWebView2.loadUrl("file:///android_asset/web_resource/googleplaycomment.html");
				break;
			}
			
			return rootView;
		}
		private void configureWebView(WebView webView,int storeType){
			//enable Javascript
			webView.getSettings().setJavaScriptEnabled(true);
	        //loads the WebView completely zoomed out
			webView.getSettings().setLoadWithOverviewMode(true);
	         
	        //true makes the Webview have a normal viewport such as a normal desktop browser 
	        //when false the webview will have a viewport constrained to it's own dimensions
			webView.getSettings().setUseWideViewPort(true);
	         
	        //override the web client to open all links in the same webview
			webView.setWebViewClient(new MyWebViewClient());
			webView.setWebChromeClient(new MyWebChromeClient());
	        //Injects the supplied Java object into this WebView. The object is injected into the 
	        //JavaScript context of the main frame, using the supplied name. This allows the 
	        //Java object's public methods to be accessed from JavaScript.
			webView.addJavascriptInterface(new JavaScriptInterface(context,storeType), "Android");
		}
		 private class MyWebViewClient extends WebViewClient {
		     @Override
		     public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	 view.loadUrl(url);
		         return true;
		     }
		 }
		  
		 private class MyWebChromeClient extends WebChromeClient {
		      
		  //display alert message in Web View
		  @Override
		     public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
		         new AlertDialog.Builder(view.getContext())
		          .setMessage(message).setCancelable(true).show();
		         result.confirm();
		         return true;
		     }
		 
		 }
	}
	
	public static class PrefsActivity extends PreferenceActivity{

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.addPreferencesFromResource(R.xml.settings);
		}
		
		


		@Override
		public void onBuildHeaders(List<Header> target) {
			
			//loadHeadersFromResource(R.xml.settings, target);
		}


		public static class PrefFragment extends PreferenceFragment{

			@Override
			public void onCreate(Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				super.onCreate(savedInstanceState);
				//addPreferencesFromResource(R.xml.settings);
			}
		}
		
	}
	
	

}
