package com.liftoff.demo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.liftoff.demo.adapters.LocationListAdapter;
import com.liftoff.demo.dao.Location;
import com.liftoff.demo.threads.SearchLocationLoader;

import java.util.List;

/**
 * Created by arindamnath on 09/12/15.
 */
public class SearchLocationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Location>> {

    private EditText searchText;
    private ListView listView;
    private LocationListAdapter locationListAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchText = (EditText) findViewById(R.id.search_location_edittext);
        listView = (ListView) findViewById(R.id.search_locations_list);
        locationListAdapter = new LocationListAdapter(this);
        listView.setAdapter(locationListAdapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching locations...");

        searchText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    progressDialog.show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    Bundle data = new Bundle();
                    data.putString("query", searchText.getText().toString());
                    getSupportLoaderManager().initLoader(0, data, SearchLocationActivity.this).forceLoad();
                    return true;
                }
                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Search can be initiated here to prompt user as they time.
                //Implementation not done because of API calls limitation
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newIntent = new Intent();
                newIntent.putExtra("lat", locationListAdapter.getItem(position).getLatitude());
                newIntent.putExtra("lng", locationListAdapter.getItem(position).getLatitude());
                newIntent.putExtra("address", locationListAdapter.getItem(position).getAddress());
                setResult(getIntent().getIntExtra("requestCode", 0), newIntent);
                finish();
            }
        });
    }

    @Override
    public Loader<List<Location>> onCreateLoader(int id, Bundle query) {
        return new SearchLocationLoader(SearchLocationActivity.this, query.getString("query"));
    }

    @Override
    public void onLoadFinished(Loader<List<Location>> loader, List<Location> data) {
        progressDialog.dismiss();
        if(data != null) {
            locationListAdapter.setData(data);
        } else {
            Toast.makeText(this, "0 matches found for the entered location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Location>> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
