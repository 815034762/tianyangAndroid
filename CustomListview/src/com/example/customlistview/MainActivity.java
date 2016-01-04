package com.example.customlistview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.customlistview.CustomListView.onFreshListener;


public class MainActivity extends Activity implements onFreshListener{

	CustomListView listView;
	MyAdapter adapter;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        adapter = new MyAdapter(MainActivity.this);
        listView = (CustomListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnFreshListeners(this);
        listView.setOnFreshListeners(this);
        
        listView.setTotalResutl(30);
        findViewById(R.id.btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				listView.freshOnComplete();
			}
		});
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onfresh() {

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				int a = adapter.getCounts();
				a = a + 3;
				adapter.setCount(a);
				listView.freshOnComplete();
				adapter.notifyDataSetChanged();
			}
		}, 2000);
	}


	@Override
	public void onloadMore() {

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				int a = adapter.getCounts();
				a = a + 3;
				adapter.setCount(a);
				listView.loadMoreComplete();
				adapter.notifyDataSetChanged();
			}
		}, 2000);
	}
}