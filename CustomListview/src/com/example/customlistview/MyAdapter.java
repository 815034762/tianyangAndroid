package com.example.customlistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {

	private Context contxt;
	private int count = 5;
	
	public MyAdapter(Context mContext)
	{
		this.contxt = mContext;
	}
	
	@Override
	public int getCount() {
		return count;
	}

	public int getCounts() {
		
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
		}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View convertview, ViewGroup arg2) {
		
		ViewHolder holder;
		
		if(null == convertview)
		{
			holder = new ViewHolder();
			convertview = LayoutInflater.from(contxt).inflate(R.layout.item, null);
			holder.tv = (TextView)convertview.findViewById(R.id.tv);
			convertview.setTag(holder);
		}else
		{
		 holder = (ViewHolder) convertview.getTag();
		}
		
		return convertview;
	}

	class ViewHolder {
		
		TextView tv;
		
	}
}
