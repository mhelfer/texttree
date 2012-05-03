package com.sbj.texttree.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sbj.texttree.domain.TextTree;
import com.sbj.texttree.domain.TreeContact;


public class DBHelper  {
	
	private static final int DB_VERSION = 3;
	private static final String DB_NAME = "pt_data";
	private static final String DB_TABLE_TEXT_TREES = "text_trees";
	private static final String DB_TABLE_TREE_CONTACTS = "tree_contacts";
	private static final String CLASSNAME = DBHelper.class.getSimpleName();
	
	private static final String COL_TREE_NAME = "name";
	private static final String COL_CONTACT_NAME = "contact_name";
	private static final String COL_CONTACT_PHONE = "contact_phone";
	private static final String COL_TREE_ID = "tree_id";
	private static final String[] TREE_COLS = new String[] {"_id", COL_TREE_NAME};
	private static final String[] CONTACT_COLS = new String[] {"_id", COL_CONTACT_NAME, COL_CONTACT_PHONE, COL_TREE_ID};
		
	
	private SQLiteDatabase db;
	private final DBOpenHelper dbOpenHelper;
	
	
	public DBHelper(Context context) { 
		dbOpenHelper = new DBOpenHelper(context, DB_NAME, DB_VERSION);
		establishDb();
	}
	
	private void establishDb() { 
		if(db == null) {
			db = dbOpenHelper.getWritableDatabase();
		}
	}
	
	public void cleanup() { 
		if(db != null) { 
			db.close();
			db = null;
		}
	}
	
	
	/**
	 * Inserts a tree into the database.
	 * @param textTree
	 * @return
	 */
	public long insert(TextTree textTree) {
		ContentValues values = new ContentValues();
		values.put("name", textTree.name);
		
		db.beginTransaction();
		
		long id = db.insertOrThrow(DBHelper.DB_TABLE_TEXT_TREES, null, values);
		insertContacts(textTree.treeContacts, id);
		db.setTransactionSuccessful();
		db.endTransaction();
		return id;
	}
	
	/**
	 * Updates a tree in the database. 
	 * 
	 * Since its unclear if records were added or removed, all records are removed and then readded.
	 * 
	 * @param textTree the tree to be updated
	 */
	public void update(TextTree textTree) {
		ContentValues values = new ContentValues();
		values.put(COL_TREE_NAME, textTree.name);
		
		Log.i("DBHelper", "textTree.id = " + textTree.id);
		
		db.beginTransaction();
		
		db.update(DBHelper.DB_TABLE_TEXT_TREES, values, "_id=" + textTree.id, null);
		deleteContactsForTree(textTree.id);
		insertContacts(textTree.treeContacts, textTree.id);
		db.setTransactionSuccessful();
		
		db.endTransaction();
	}
	
	
	/**
	 * Inserts a list of contacts into the tree for a given id.
	 * 
	 * @param contacts the list of contacts to be inserted
	 * @param id the id the contacts will be associated with
	 */
	public void insertContacts(List<TreeContact> contacts, long id){
		ContentValues values = new ContentValues();
		
		for(TreeContact contact : contacts) {
			values = new ContentValues();
			
			values.put(COL_CONTACT_NAME, contact.contactName);
			values.put(COL_CONTACT_PHONE, contact.contactPhone);
			values.put(COL_TREE_ID, id);
			db.insert(DBHelper.DB_TABLE_TREE_CONTACTS, null, values);
		}
	}
	
	/**
	 * Deletes a tree from the database
	 * @param id the id of the tree to delete
	 */
	public void deleteTree(long id){
		db.delete(DBHelper.DB_TABLE_TEXT_TREES, "_id=" + id, null);
	}
	
	/**
	 * Deletes all contacts for an id
	 * @param id the id to be deleted.
	 */
	public void deleteContactsForTree(long id) { 
		db.delete(DBHelper.DB_TABLE_TREE_CONTACTS, COL_TREE_ID + "=" + id, null);
	}
	
	
	/**
	 * Returns a tree for a given id
	 * 
	 * @param id the id of the tree to be retrieved.
	 * @return
	 */
	public TextTree get(long id) { 
		Cursor treeCursor = null;
		Cursor contactCursor = null;
		TextTree tree = null;
		TreeContact contact = null;
		List<TreeContact> contacts;
		
		try {
			treeCursor = db.query(true, DBHelper.DB_TABLE_TEXT_TREES, DBHelper.TREE_COLS, "_id = " + id, null, null, null, null, null);
			if(treeCursor.getCount() > 0) {
				treeCursor.moveToFirst();
				tree = new TextTree();
				tree.id = treeCursor.getLong(0);
				tree.name = treeCursor.getString(1);
			
			
				contactCursor = db.query(true, DBHelper.DB_TABLE_TREE_CONTACTS, DBHelper.CONTACT_COLS, COL_TREE_ID + " =" + id, null, null, null, null, null);
				if(contactCursor.getCount() > 0) { 
					contacts = new ArrayList<TreeContact>();
					while(contactCursor.moveToNext()) { 
						contact = new TreeContact();
						contact.id = contactCursor.getLong(0);
						contact.contactName = contactCursor.getString(1);
						contact.contactPhone = contactCursor.getString(2);
						contacts.add(contact);
					}
					tree.treeContacts = contacts;
				}
			}
		} catch (SQLException e){
			Log.e("TEXT TREE GET", DBHelper.CLASSNAME, e);
		} finally {
			if (treeCursor != null && !treeCursor.isClosed()) {
	        	treeCursor.close();
	        }
			if (contactCursor != null && !contactCursor.isClosed()) {
	        	contactCursor.close();
	        }
		}
		
		return tree;
	}
	
	/**
	 * Returns all the trees in the database
	 * @return
	 */
	public List<TextTree> getAll() { 
		Cursor cursor = null;
		Cursor contactCursor = null;
		TextTree tree = null;
		List<TextTree> trees = new ArrayList<TextTree>();
		TreeContact contact = null;
		List<TreeContact> contacts;
		
		try {
			cursor = db.query(true, DBHelper.DB_TABLE_TEXT_TREES, DBHelper.TREE_COLS, null, null, null, null, null, null);
			if(cursor.getCount() > 0) {
				while( cursor.moveToNext() ) {
					tree = new TextTree();
					tree.id = cursor.getLong(0);
					tree.name = cursor.getString(1);
					
					
					contactCursor = db.query(true, DBHelper.DB_TABLE_TREE_CONTACTS, DBHelper.CONTACT_COLS, COL_TREE_ID + "=" + tree.id, null, null, null, null, null);
					if(contactCursor.getCount() > 0) { 
						contacts = new ArrayList<TreeContact>();
						while(contactCursor.moveToNext()) { 
							contact = new TreeContact();
							contact.id = contactCursor.getLong(0);
							contact.contactName = contactCursor.getString(1);
							contact.contactPhone = contactCursor.getString(2);
							contacts.add(contact);
						}
						tree.treeContacts = contacts;
					}
					trees.add(tree);
				}
			}
		} catch (SQLException e){
			Log.e("TEXT TREE GET ALL", DBHelper.CLASSNAME, e);
		} finally {
	        if (cursor != null && !cursor.isClosed()) {
	            cursor.close();
	        }
	        if (contactCursor != null && !contactCursor.isClosed()) {
	        	contactCursor.close();
	        }
		}
		
		return trees;
	}
	
	private static class DBOpenHelper extends SQLiteOpenHelper {
		private static final String DB_CREATE_TABLE_TREE = 
				"CREATE TABLE " + DBHelper.DB_TABLE_TEXT_TREES
		        + " (_id INTEGER PRIMARY KEY, " 
		        + COL_TREE_NAME + " TEXT NOT NULL);";
		
		private static final String DB_CREATE_TABLE_TREE_CONTACT = 
				"CREATE TABLE " + DBHelper.DB_TABLE_TREE_CONTACTS
				+ "(_id INTEGER PRIMARY KEY"
				+ ", " + COL_CONTACT_NAME + " TEXT NOT NULL"
				+ ", " + COL_CONTACT_PHONE + " TEXT NOT NULL"
				+ ", " + COL_TREE_ID + " INTEGER NOT NULL"
				+ ", FOREIGN KEY("+COL_TREE_ID+") REFERENCES phone_trees(_id) ON DELETE CASCADE);";
		
		public DBOpenHelper(Context context, String dbName, int version) {
		    super(context, DBHelper.DB_NAME, null, DBHelper.DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DBOpenHelper.DB_CREATE_TABLE_TREE);
				db.execSQL(DBOpenHelper.DB_CREATE_TABLE_TREE_CONTACT);
			} catch (SQLException e){
				Log.e("TEXT TREE CREATE TABLE", DBHelper.CLASSNAME, e);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS" + DBHelper.DB_NAME);
			onCreate(db);
		}	
	}

}
