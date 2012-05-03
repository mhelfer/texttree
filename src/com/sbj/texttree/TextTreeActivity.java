package com.sbj.texttree;

import java.util.Collections;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sbj.texttree.adapter.QuickActionListAdapter;
import com.sbj.texttree.db.DBHelper;
import com.sbj.texttree.domain.TextTree;

public class TextTreeActivity extends ListActivity {
	
	//UI Controls
	private Button createTree;
	private QuickActionListAdapter adapter;
	
	//Domain & Persistence
	private DBHelper dbHelper;
	private List<TextTree> trees;
	
	//Constants
	private static final String BUNDLE_TREE_ID = "treeId";
    private static final int ID_DELETE = 1;
	private static final int ID_EDIT = 2;
	private static final int ID_SEND = 3;
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
         
        //Query database for existing lists.
        dbHelper = new DBHelper(this);
        trees = dbHelper.getAll();
        dbHelper.cleanup();
        
        //Setup the QuickActionMenu
        final ActionItem deleteTree = new ActionItem(ID_DELETE, "Delete", getResources().getDrawable(R.drawable.ic_menu_delete));
		final ActionItem editITree = new ActionItem(ID_EDIT, "Edit", getResources().getDrawable(R.drawable.ic_menu_edit));
		final ActionItem sendToTree = new ActionItem(ID_SEND, "Send", getResources().getDrawable(R.drawable.ic_menu_share));
        
        final QuickAction quickAction = new QuickAction(this);
        quickAction.addActionItem(deleteTree);
		quickAction.addActionItem(editITree);
		quickAction.addActionItem(sendToTree);
		
		//setup the QuickAction item click listener
		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(final QuickAction quickAction,final int pos, int actionId) {
				
				if (actionId == ID_DELETE) {
					Log.i("TextTree.TextTreeActivity","Delete item selected on row " + quickAction.getSelectedRow());
					
					AlertDialog.Builder deleteConfirm = new AlertDialog.Builder(TextTreeActivity.this);
					final TextTree treeItem = trees.get(quickAction.getSelectedRow());
					deleteConfirm.setTitle(R.string.labelConfirmDeleteTreeTitle)
						.setMessage(getString(R.string.labelConfirmDeleteTreeText, treeItem.name))
						.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
							//If positive delete list.
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dbHelper = new DBHelper(TextTreeActivity.this);
								dbHelper.deleteTree(treeItem.id);
								dbHelper.cleanup();
								trees.remove(quickAction.getSelectedRow());
								adapter.notifyDataSetChanged();
							}
						})
						.setNegativeButton(R.string.negative, null);
					deleteConfirm.create().show();
					
				} else if(actionId == ID_EDIT){
					Log.i("TextTree.TextTreeActivity", "Edit item selected on row "+ quickAction.getSelectedRow());
					
					Intent intent = new Intent(TextTreeActivity.this, AddEditActivity.class);
					intent.putExtra(BUNDLE_TREE_ID, trees.get(quickAction.getSelectedRow()).id);
					startActivity(intent);
					
				} else if(actionId == ID_SEND){
					Log.i("TextTree.TextTreeActivity", "Send item selected on row "+ quickAction.getSelectedRow());
					
					Intent intent = new Intent(TextTreeActivity.this, SendActivity.class);
					intent.putExtra(BUNDLE_TREE_ID, trees.get(quickAction.getSelectedRow()).id);
					startActivity(intent);
				}
			}
		});
		
		
		//If we found lists in the database add them to the list adapter.
        if(trees!= null) { 
        	Collections.sort(trees);
        	adapter = new QuickActionListAdapter(this);
        	adapter.setData(trees);
        	adapter.setQuickActionMenu(quickAction);
        	setListAdapter(adapter);
        }
        
        //Initialize list view
        final ListView listView = getListView();
        TextView empty = (TextView)listView.getEmptyView();
        empty.setText(R.string.labelEmptyTreeList);
        
        //Allow for clicking
        listView.setClickable(true);
        Log.i("TextTreeActivity", "count = " + listView.getCount());
		
        //Clicking directly on the list item sends the user to the send functionality.
        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(TextTreeActivity.this, SendActivity.class);
				intent.putExtra(BUNDLE_TREE_ID, trees.get(position).id);
				startActivity(intent);
			}
        	
        });
        
        //Long press will show the quick action menu.
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					quickAction.show(view);
					quickAction.setSelectedRow(position);
					return true;
			}
        });
		
        
        createTree = (Button)findViewById(R.id.createButton);
        createTree.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TextTreeActivity.this, AddEditActivity.class);
		    	startActivity(intent);
			}
		});
    }
    
    /**
     * Refreshes the home page when users presses back. 
     * Should figure out a better way to exit the application. This prevents them from 
     * backing out. The user has to press Home.
     */
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(this, TextTreeActivity.class);
    	startActivity(intent);	
	}
}