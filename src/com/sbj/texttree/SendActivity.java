package com.sbj.texttree;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sbj.texttree.db.DBHelper;
import com.sbj.texttree.domain.TextTree;
import com.sbj.texttree.domain.TreeContact;

public class SendActivity extends ListActivity {

	private DBHelper dbHelper;
	private ArrayAdapter<TreeContact> adapter;
	
	private Button send;
	private TextTree tree = null;
	private TextView message;
	private TextView charCount;
	
	private static final char CONTACT_DELIMITER = ',';
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_message);
		
		Bundle extras = getIntent().getExtras(); 
		if(extras != null) {
			getTreeForBundle(extras.getLong("treeId"));
		}
		
		adapter = new ArrayAdapter<TreeContact>(this,android.R.layout.simple_list_item_checked, tree.treeContacts);
		setListAdapter(adapter);
		
		final ListView contactList = getListView();
		for(int i = 0; i< contactList.getCount(); i++) { 
			contactList.setItemChecked(i, true);
		}
		
		send = (Button) findViewById(R.id.buttonSend);
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				StringBuilder builder = new StringBuilder();
				EditText message = (EditText)findViewById(R.id.message);
				if(message.getText() != null && message.getText().toString().trim().length() > 0) {
					
					for(int i = 0; i< contactList.getCount(); i++){
						if(contactList.isItemChecked(i)){ 
							TreeContact contact = (TreeContact)contactList.getItemAtPosition(i);
							builder.append(contact.contactPhone);
							builder.append(CONTACT_DELIMITER);
						}
					}
					
					if(builder.charAt(builder.length()-1) == CONTACT_DELIMITER) {
						builder.deleteCharAt(builder.length()-1);
					}
					
					Uri uri = Uri.parse("smsto:" + builder.toString());
					Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
					intent.putExtra("sms_body", message.getText().toString());
					startActivity(intent);
				}
				else{
					Toast.makeText(SendActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		charCount = (TextView)findViewById(R.id.charLimit);
		
		message = (TextView)findViewById(R.id.message);
		message.addTextChangedListener(new TextWatcher(){
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//no-op
			}

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	           charCount.setText(String.valueOf(160-s.length()));
	        }

	        public void afterTextChanged(Editable s) {
	        	//no-op
	        }
			
		});
		
		TextView title = (TextView)findViewById(R.id.title);
		title.setText(tree.name);
		
	}
	
	
	private TextTree getTreeForBundle(long treeId) {
		dbHelper = new DBHelper(this);
		tree = dbHelper.get(treeId);
		dbHelper.cleanup();
		
		return tree;
	}
	
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
}
