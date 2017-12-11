package com.provider.demo.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

/**
 * Created by Administrator on 2017/12/11.
 */

public class AppDatabaseHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contacts2.db";
    private static AppDatabaseHelper sSingleton;

    public interface Tables {
        public static final String ACCOUNTS = "accounts";
        public static final String CONTACTS = "contacts";
        public static final String RAW_CONTACTS = "raw_contacts";
        public static final String MIMETYPES = "mimetypes";
        public static final String DATA = "data";
    }

    public interface AccountsColumns extends BaseColumns {
        String CONCRETE_ID = Tables.ACCOUNTS + "." + BaseColumns._ID;

        String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;
        String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
        String DATA_SET = ContactsContract.RawContacts.DATA_SET;

        String CONCRETE_ACCOUNT_NAME = Tables.ACCOUNTS + "." + ACCOUNT_NAME;
        String CONCRETE_ACCOUNT_TYPE = Tables.ACCOUNTS + "." + ACCOUNT_TYPE;
        String CONCRETE_DATA_SET = Tables.ACCOUNTS + "." + DATA_SET;
    }

    public interface ContactsColumns {
        public static final String LAST_STATUS_UPDATE_ID = "status_update_id";

        public static final String CONCRETE_ID = Tables.CONTACTS + "." + BaseColumns._ID;

        public static final String CONCRETE_PHOTO_FILE_ID = Tables.CONTACTS + "."
                + ContactsContract.Contacts.PHOTO_FILE_ID;
        public static final String CONCRETE_TIMES_CONTACTED = Tables.CONTACTS + "."
                + ContactsContract.Contacts.TIMES_CONTACTED;
        public static final String CONCRETE_LAST_TIME_CONTACTED = Tables.CONTACTS + "."
                + ContactsContract.Contacts.LAST_TIME_CONTACTED;
        public static final String CONCRETE_STARRED = Tables.CONTACTS + "." + ContactsContract.Contacts.STARRED;
        public static final String CONCRETE_PINNED = Tables.CONTACTS + "." + ContactsContract.Contacts.PINNED;
        public static final String CONCRETE_CUSTOM_RINGTONE = Tables.CONTACTS + "."
                + ContactsContract.Contacts.CUSTOM_RINGTONE;
        public static final String CONCRETE_SEND_TO_VOICEMAIL = Tables.CONTACTS + "."
                + ContactsContract.Contacts.SEND_TO_VOICEMAIL;
        public static final String CONCRETE_LOOKUP_KEY = Tables.CONTACTS + "."
                + ContactsContract.Contacts.LOOKUP_KEY;
        public static final String CONCRETE_CONTACT_LAST_UPDATED_TIMESTAMP = Tables.CONTACTS + "."
                + ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP;
        public static final String PHONEBOOK_LABEL_PRIMARY = "phonebook_label";
        public static final String PHONEBOOK_BUCKET_PRIMARY = "phonebook_bucket";
        public static final String PHONEBOOK_LABEL_ALTERNATIVE = "phonebook_label_alt";
        public static final String PHONEBOOK_BUCKET_ALTERNATIVE = "phonebook_bucket_alt";
    }

    public interface RawContactsColumns {
        public static final String CONCRETE_ID =
                Tables.RAW_CONTACTS + "." + BaseColumns._ID;

        public static final String ACCOUNT_ID = "account_id";
        public static final String CONCRETE_ACCOUNT_ID = Tables.RAW_CONTACTS + "." + ACCOUNT_ID;
        public static final String CONCRETE_SOURCE_ID =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SOURCE_ID;
        public static final String CONCRETE_BACKUP_ID =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.BACKUP_ID;
        public static final String CONCRETE_VERSION =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.VERSION;
        public static final String CONCRETE_DIRTY =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.DIRTY;
        public static final String CONCRETE_DELETED =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.DELETED;
        public static final String CONCRETE_SYNC1 =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SYNC1;
        public static final String CONCRETE_SYNC2 =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SYNC2;
        public static final String CONCRETE_SYNC3 =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SYNC3;
        public static final String CONCRETE_SYNC4 =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SYNC4;
        public static final String CONCRETE_CUSTOM_RINGTONE =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.CUSTOM_RINGTONE;
        public static final String CONCRETE_SEND_TO_VOICEMAIL =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.SEND_TO_VOICEMAIL;
        public static final String CONCRETE_LAST_TIME_CONTACTED =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.LAST_TIME_CONTACTED;
        public static final String CONCRETE_TIMES_CONTACTED =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.TIMES_CONTACTED;
        public static final String CONCRETE_STARRED =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.STARRED;
        public static final String CONCRETE_PINNED =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.PINNED;

        public static final String DISPLAY_NAME = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY;
        public static final String DISPLAY_NAME_SOURCE = ContactsContract.RawContacts.DISPLAY_NAME_SOURCE;
        public static final String AGGREGATION_NEEDED = "aggregation_needed";

        public static final String CONCRETE_DISPLAY_NAME =
                Tables.RAW_CONTACTS + "." + DISPLAY_NAME;
        public static final String CONCRETE_CONTACT_ID =
                Tables.RAW_CONTACTS + "." + ContactsContract.RawContacts.CONTACT_ID;
        public static final String PHONEBOOK_LABEL_PRIMARY =
                ContactsColumns.PHONEBOOK_LABEL_PRIMARY;
        public static final String PHONEBOOK_BUCKET_PRIMARY =
                ContactsColumns.PHONEBOOK_BUCKET_PRIMARY;
        public static final String PHONEBOOK_LABEL_ALTERNATIVE =
                ContactsColumns.PHONEBOOK_LABEL_ALTERNATIVE;
        public static final String PHONEBOOK_BUCKET_ALTERNATIVE =
                ContactsColumns.PHONEBOOK_BUCKET_ALTERNATIVE;

        /**
         * This column is no longer used, but we keep it in the table so an upgraded database
         * will look the same as a new database. This reduces the chance of OEMs adding a second
         * column with the same name.
         */
        public static final String NAME_VERIFIED_OBSOLETE = "name_verified";
    }

    public interface MimetypesColumns {
        public static final String _ID = BaseColumns._ID;
        public static final String MIMETYPE = "mimetype";
    }

    public interface DataColumns {
        public static final String MIMETYPE_ID = "mimetype_id";
    }

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        if (sSingleton == null) {
            sSingleton = new AppDatabaseHelper(context, DATABASE_NAME);
        }
        return sSingleton;
    }

    public AppDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //数据库不存在时调用, 可看源码
        db.execSQL("CREATE TABLE " + Tables.ACCOUNTS + " (" +
                AccountsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                AccountsColumns.ACCOUNT_NAME + " TEXT, " +
                AccountsColumns.ACCOUNT_TYPE + " TEXT, " +
                AccountsColumns.DATA_SET + " TEXT " +
                ");");
        // One row per group of contacts corresponding to the same person
        db.execSQL("CREATE TABLE " + Tables.CONTACTS + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContactsContract.Contacts.NAME_RAW_CONTACT_ID + " INTEGER REFERENCES raw_contacts(_id)," +
                ContactsContract.Contacts.CUSTOM_RINGTONE + " TEXT," +
                ContactsContract.Contacts.SEND_TO_VOICEMAIL + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Contacts.TIMES_CONTACTED + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Contacts.LAST_TIME_CONTACTED + " INTEGER," +
                ContactsContract.Contacts.STARRED + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Contacts.PINNED + " INTEGER NOT NULL DEFAULT " + ContactsContract.PinnedPositions.UNPINNED + "," +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Contacts.LOOKUP_KEY + " TEXT," +
                ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + " INTEGER" +
                ");");

        // Raw_contacts table
        db.execSQL("CREATE TABLE " + Tables.RAW_CONTACTS + " (" +
                ContactsContract.RawContacts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RawContactsColumns.ACCOUNT_ID + " INTEGER REFERENCES " +
                Tables.ACCOUNTS + "(" + AccountsColumns._ID + ")," +
                ContactsContract.RawContacts.SOURCE_ID + " TEXT," +
                ContactsContract.RawContacts.BACKUP_ID + " TEXT," +
                ContactsContract.RawContacts.RAW_CONTACT_IS_READ_ONLY + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.VERSION + " INTEGER NOT NULL DEFAULT 1," +
                ContactsContract.RawContacts.DIRTY + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.DELETED + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.CONTACT_ID + " INTEGER REFERENCES contacts(_id)," +
                ContactsContract.RawContacts.AGGREGATION_MODE + " INTEGER NOT NULL DEFAULT " +
                ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT + "," +
                RawContactsColumns.AGGREGATION_NEEDED + " INTEGER NOT NULL DEFAULT 1," +
                ContactsContract.RawContacts.CUSTOM_RINGTONE + " TEXT," +
                ContactsContract.RawContacts.SEND_TO_VOICEMAIL + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.TIMES_CONTACTED + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.LAST_TIME_CONTACTED + " INTEGER," +
                ContactsContract.RawContacts.STARRED + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.PINNED + " INTEGER NOT NULL DEFAULT "  + ContactsContract.PinnedPositions.UNPINNED +
                "," + ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " TEXT," +
                ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE + " TEXT," +
                ContactsContract.RawContacts.DISPLAY_NAME_SOURCE + " INTEGER NOT NULL DEFAULT " +
                ContactsContract.DisplayNameSources.UNDEFINED + "," +
                ContactsContract.RawContacts.PHONETIC_NAME + " TEXT," +
                ContactsContract.RawContacts.PHONETIC_NAME_STYLE + " TEXT," +
                RawContactsColumns.PHONEBOOK_LABEL_PRIMARY + " TEXT," +
                RawContactsColumns.PHONEBOOK_BUCKET_PRIMARY + " INTEGER," +
                RawContactsColumns.PHONEBOOK_LABEL_ALTERNATIVE + " TEXT," +
                RawContactsColumns.PHONEBOOK_BUCKET_ALTERNATIVE + " INTEGER," +
                RawContactsColumns.NAME_VERIFIED_OBSOLETE + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.RawContacts.SYNC1 + " TEXT, " +
                ContactsContract.RawContacts.SYNC2 + " TEXT, " +
                ContactsContract.RawContacts.SYNC3 + " TEXT, " +
                ContactsContract.RawContacts.SYNC4 + " TEXT " +
                ");");

        // Mimetype mapping table
        db.execSQL("CREATE TABLE " + Tables.MIMETYPES + " (" +
                MimetypesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MimetypesColumns.MIMETYPE + " TEXT NOT NULL" +
                ");");

        // Public generic data table
        db.execSQL("CREATE TABLE " + Tables.DATA + " (" +
                ContactsContract.Data._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DataColumns.MIMETYPE_ID + " INTEGER REFERENCES mimetype(_id) NOT NULL," +
                ContactsContract.Data.RAW_CONTACT_ID + " INTEGER REFERENCES raw_contacts(_id) NOT NULL," +
                ContactsContract.Data.IS_READ_ONLY + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Data.IS_PRIMARY + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Data.IS_SUPER_PRIMARY + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Data.DATA_VERSION + " INTEGER NOT NULL DEFAULT 0," +
                ContactsContract.Data.DATA1 + " TEXT," +
                ContactsContract.Data.DATA2 + " TEXT," +
                ContactsContract.Data.DATA3 + " TEXT," +
                ContactsContract.Data.DATA4 + " TEXT," +
                ContactsContract.Data.DATA5 + " TEXT," +
                ContactsContract.Data.DATA6 + " TEXT," +
                ContactsContract.Data.DATA7 + " TEXT," +
                ContactsContract.Data.DATA8 + " TEXT," +
                ContactsContract.Data.DATA9 + " TEXT," +
                ContactsContract.Data.DATA10 + " TEXT," +
                ContactsContract.Data.DATA11 + " TEXT," +
                ContactsContract.Data.DATA12 + " TEXT," +
                ContactsContract.Data.DATA13 + " TEXT," +
                ContactsContract.Data.DATA14 + " TEXT," +
                ContactsContract.Data.DATA15 + " TEXT," +
                ContactsContract.Data.SYNC1 + " TEXT, " +
                ContactsContract.Data.SYNC2 + " TEXT, " +
                ContactsContract.Data.SYNC3 + " TEXT, " +
                ContactsContract.Data.SYNC4 + " TEXT " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  //数据库升级时调用

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db); //数据库被打开后调用，发生在onCreate,onUpgrade,onDowngrade之后，可对数据库在修改之前进行检查
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) { //数据库降级
        super.onDowngrade(db, oldVersion, newVersion); //一般不建议对数据库进行降级处理
    }
}
