package com.zhangshushu.BigWord;

import java.util.Stack;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangshushu.BigWord.SimpleWikiHelper.ApiException;
import com.zhangshushu.BigWord.SimpleWikiHelper.ParseException;

public class BigWord extends Activity implements View.OnClickListener {
	private static final String TAG = "BigWord";
	
	private String mWord;
	private Button mSearchButton;
	private WebView mWebView;
    private EditText mWordText;
    
    private Stack<String> mHistory = new Stack<String>();

    private String mEntryTitle;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mSearchButton = (Button) findViewById(R.id.search);
        mSearchButton.setOnClickListener(this);
        
        mWordText = (EditText) findViewById(R.id.word);
        mWordText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                	mWord = mWordText.getText().toString();
                	startNavigating(mWord, true);
                    // Perform action on key press
                    //Toast.makeText(BigWord.this, mWordText.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
        
        mWebView = (WebView) findViewById(R.id.webview);
        ExtendedWikiHelper.prepareUserAgent(this);
        
        //android.os.Debug.startMethodTracing("calc");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.bigword, menu);
        return true;
    }    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.lookup_search: {
                onSearchRequested();
                return true;
            }*/
        }
        return false;
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
    	
        @Override
        protected void onProgressUpdate(String... args) {
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
    
    public void onClick(View v) {
        mWord = mWordText.getText().toString();
        Log.v(TAG, "word=" + mWord);
        startNavigating(mWord, true);
    }   
    
    @Override
    public void onDestroy() {
      //android.os.Debug.stopMethodTracing();
      super.onDestroy();
    }
    
}