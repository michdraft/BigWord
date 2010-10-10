package com.zhangshushu.BigWord;

import java.util.Stack;

import com.zhangshushu.BigWord.SimpleWikiHelper.ApiException;
import com.zhangshushu.BigWord.SimpleWikiHelper.ParseException;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class BigWord extends Activity {
	private static final String TAG = "BigWord";
	
    private Stack<String> mHistory = new Stack<String>();
    private WebView mWebView;

    private String mEntryTitle;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final Intent intent = getIntent();
        final String action = intent.getAction();
        Log.v(TAG, "action=" + action);

        mWebView = (WebView) findViewById(R.id.webview);

        ExtendedWikiHelper.prepareUserAgent(this);
        
        if (Intent.ACTION_SEARCH.equals(action)) {
        	String query = intent.getStringExtra(SearchManager.QUERY);
        	Log.v(TAG, "query=" + query);
        	startNavigating(query, true);
        }
        else {
        	Log.v(TAG, "other intents");
            onSearchRequested();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bigword, menu);
        return true;
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lookup_search: {
                onSearchRequested();
                return true;
            }
        }
        return false;
    }

    /**
     * Set the title for the current entry.
     */
    protected void setEntryTitle(String entryText) {
        mEntryTitle = entryText;
        //mTitle.setText(mEntryTitle);
    }

    /**
     * Set the content for the current entry. This will update our
     * {@link WebView} to show the requested content.
     */
    protected void setEntryContent(String entryContent) {
        mWebView.loadDataWithBaseURL(ExtendedWikiHelper.WIKI_AUTHORITY, entryContent,
                ExtendedWikiHelper.MIME_TYPE, ExtendedWikiHelper.ENCODING, null);
    }
    
    private void startNavigating(String word, boolean pushHistory) {
        // Push any current word onto the history stack
        if (!TextUtils.isEmpty(mEntryTitle) && pushHistory) {
            mHistory.add(mEntryTitle);
        }

        // Start lookup for new word in background
        new LookupTask().execute(word);
    }

    private class LookupTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            String query = args[0];
            String parsedText = null;

            try {
                // If query word is null, assume request for random word
                if (query == null) {
                    query = ExtendedWikiHelper.getRandomWord();
                }

                if (query != null) {
                    // Push our requested word to the title bar
                    publishProgress(query);
                    String wikiText = ExtendedWikiHelper.getPageContent(query, true);
                    parsedText = ExtendedWikiHelper.formatWikiText(wikiText);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Problem making wiktionary request", e);
            } catch (ParseException e) {
                Log.e(TAG, "Problem making wiktionary request", e);
            }

            if (parsedText == null) {
                parsedText = getString(R.string.empty_result);
            }

            return parsedText;
        }
    	
        /**
         * Our progress update pushes a title bar update.
         */
        @Override
        protected void onProgressUpdate(String... args) {
            String searchWord = args[0];
            setEntryTitle(searchWord);
        }

        /**
         * When finished, push the newly-found entry content into our
         * {@link WebView} and hide the {@link ProgressBar}.
         */
        @Override
        protected void onPostExecute(String parsedText) {
            //mTitleBar.startAnimation(mSlideOut);
            //mProgress.setVisibility(View.INVISIBLE);

            setEntryContent(parsedText);
        }   	
    }
    
    
    
    
    
}