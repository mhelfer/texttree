package com.sbj.texttree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.sbj.texttree.db.DBHelper;
import com.sbj.texttree.domain.TextTree;
import com.sbj.texttree.domain.TreeContact;

public class AddEditActivity extends ListActivity {
	
	//UI Components
	private Button addContacts;
	private EditText editTreeName;
	
	//List View Elements
	private List<Map<String, String>> contactsList = new ArrayList<Map<String, String>>(10);
	private SimpleAdapter adapter;
	private TextTree tree;

	//Constants
	private static final int PICK_CONTACT = 1;
	private static final String NAME = "name";
	private static final String NUMBER = "number";
	
	private static final int ID_DELETE = 1;
	private int mSelectedRow = 0;
	
	private DBHelper dbHelper;
	
	private static final String LOG_TAG = "AddEditActivity";
	
	private static final Comparator<Map<String,String>> BY_NAME = new Comparator<Map<String,String>>(){

		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			return lhs.get(NAME).compareTo(rhs.get(NAME));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_tree);
		
		long treeId = -1;
		Bundle extras = getIntent().getExtras(); 
		if(extras != null) {
			treeId = extras.getLong("treeId");
			handlePopulateScreen(treeId);
		}
		
		adapter = new SimpleAdapter(this, contactsList, android.R.layout.two_line_list_item, 
					new String[]{NAME, NUMBER}, new int[]{android.R.id.text1, android.R.id.text2});
		setListAdapter(adapter);
		
		//Configure the list.
		ListView listView = getListView();
        
        ActionItem deleteTree = new ActionItem(ID_DELETE, "Delete", getResources().getDrawable(R.drawable.ic_menu_delete));
        
        final QuickAction mQuickAction 	= new QuickAction(this);
        mQuickAction.addActionItem(deleteTree);
        
        //setup the action item click listener
  		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
  			@Override
  			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
				contactsList.remove(mSelectedRow);
				handleSave();
				adapter.notifyDataSetChanged();
  			}
  		});

  		listView.setOnItemClickListener(new OnItemClickListener() {
  			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  				mSelectedRow = position; //set the selected row
  				mQuickAction.show(view);
  			}
  		});
		
		//Setup 'add contacts' button handler
		addContacts = (Button) findViewById(R.id.buttonAddContacts);
		addContacts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handleAddContact();
			}
		});
		
	}
	
	/**
	 * This method will be executed in the EDIT version. It is used to populate data fields with existing data
	 */
	private void handlePopulateScreen(long id) { 
		dbHelper = new DBHelper(this);
		tree = dbHelper.get(id);
		dbHelper.cleanup();
		Map<String, String> contactsForList;
		if(tree.treeContacts != null){
			for(TreeContact contact : tree.treeContacts) {
				contactsForList = new HashMap<String,String>(2);
				contactsForList.put(NAME, contact.contactName);
				contactsForList.put(NUMBER, contact.contactPhone);
				contactsList.add(contactsForList);
			}
			Collections.sort(contactsList,BY_NAME);
		}
		
		EditText treeName = (EditText)findViewById(R.id.editTreeName);
		treeName.setText(tree.name);
	}
	
	
	/**
	 * Send the user to the contact picker to select a contact
	 */
	private void handleAddContact(){
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		intent.setType( ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE );
		startActivityForResult(intent, PICK_CONTACT);
	}
	
	
	/**
	 * Method called when user returns from the contact picker having selected a contact
	 */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
	  
		if(PICK_CONTACT == reqCode) {
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
					
				Cursor cursor =  managedQuery(contactData, new String[] {Contacts.DISPLAY_NAME, Contacts._ID}, null, null, null);
				if (cursor.moveToFirst()) {
					String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			        
			        cursor = managedQuery(Phone.CONTENT_URI, new String [] { Phone._ID, Phone.NUMBER, Phone.TYPE }, Phone._ID + " = " + contactId, null, null);
			        Map<String, String> nameNumber = null;
			        while( cursor.moveToNext() ) {
			        	String number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
			        	int type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
			        	
				        String typeLabel = (String) Phone.getTypeLabel(AddEditActivity.this.getResources(), type, "");
			        	
				        Log.d(LOG_TAG, "onActivityResult: type = " + type);
			        	Log.d(LOG_TAG, "onActivityResult: name = " + contactName);
				        Log.d(LOG_TAG, "onActivityResult: number = " + typeLabel);
				        
			        	nameNumber = new HashMap<String, String>(4);
			        	nameNumber.put(NAME, contactName + "-" + typeLabel);
			        	nameNumber.put(NUMBER, number);
			        	
			        	contactsList.add(nameNumber);
			        	handleSave();
			        	Collections.sort(contactsList,BY_NAME);
			        	adapter.notifyDataSetChanged();
			        }    
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		handleSave();
		finish();
	}
	
	private void handleSave(){
		
		editTreeName = (EditText) findViewById(R.id.editTreeName);
		
		if(tree == null && contactsList.size() == 0){
			return;
		}
		else if(tree == null){
			tree = new TextTree();
		}
		
		dbHelper = new DBHelper(this);
		//If an existing tree name and all contacts have been deleted, delete the tree
		if(editTreeName.getText().toString().equals("") && contactsList.size() == 0){
			dbHelper.deleteTree(tree.id);
			dbHelper.cleanup();
			Intent intent = new Intent(this, TextTreeActivity.class);
	    	startActivity(intent);
	    	return;
		}
		//otherwise if there are contacts but no name, use the first contact as the name
		else if(editTreeName == null || editTreeName.getText().toString().equals("")){
			tree.name = contactsList.get(0).get(NAME);
		}
		//otherwise use the name the user entered.
		else {
			tree.name = editTreeName.getText().toString();
		}
		
		
		
		List<TreeContact> contacts = new ArrayList<TreeContact>(10);
		TreeContact treeContact = null;
		for(Map<String, String> contactData : contactsList) { 
			treeContact = new TreeContact();
			treeContact.contactName = contactData.get(NAME);
			treeContact.contactPhone = contactData.get(NUMBER);
			contacts.add(treeContact);
		}
		tree.treeContacts = contacts;
		
		try{
			if(tree.id > 0) {
				dbHelper.update(tree);
			}
			else {
				tree.id = dbHelper.insert(tree);
				getIntent().putExtra("treeId", tree.id);
			} 
		} 
		catch (SQLiteConstraintException e){
			Toast toast = Toast.makeText(this, "The tree name alrady in use", Toast.LENGTH_SHORT);
			toast.show();
		}
		finally{
			dbHelper.cleanup();
		}
	}
}
