package com.sbj.texttree.adapter;

import java.util.List;

import net.londatiga.android.QuickAction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbj.texttree.R;
import com.sbj.texttree.domain.TextTree;

public class QuickActionListAdapter extends BaseAdapter{
	
	private LayoutInflater inflater;
	private List<TextTree> data;
	private QuickAction quickActionMenu;
	
	public QuickActionListAdapter(Context context) { 
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public TextTree getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setData(List<TextTree> data){
		this.data = data;
	}
	
	public void setQuickActionMenu(QuickAction quickActionMenu){
		this.quickActionMenu = quickActionMenu;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.quick_action_list, null);
			holder = new ViewHolder();
			holder.listText = (TextView) convertView.findViewById(R.id.t_name);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ImageView actionButton = (ImageView)convertView.findViewById(R.id.i_more);
		actionButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				quickActionMenu.show(v);
				quickActionMenu.setSelectedRow(position);
			}
		});
		
		holder.listText.setText(data.get(position).toString());
		
		return convertView;
	}
	
	static class ViewHolder {
		TextView listText;
	}

}
