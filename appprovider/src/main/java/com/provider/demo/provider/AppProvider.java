package com.provider.demo.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2017/12/11.
 */

public class AppProvider extends AbstractProvider {

    private static final String TAG = "AppProvider";
    private static final String AUTHORITY = "com.provider.demo";

    private static final UriMatcher sUriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CONTACTS = 0x01;
    private static final int CONTACTS_ID = 0x02;
    private static final int CONTACTS_ID_DATA = 0x03;
    private static final int AGGREGATION_SUGGESTIONS = 0x04;

    static {
        // Contacts URI matching table
        final UriMatcher matcher = sUriMatcher;

        // DO NOT use constants such as Contacts.CONTENT_URI here.  This is the only place
        // where one can see all supported URLs at a glance, and using constants will reduce
        // readability.
        matcher.addURI(AUTHORITY, "contacts", CONTACTS);
        matcher.addURI(AUTHORITY, "contacts/#", CONTACTS_ID);
        matcher.addURI(AUTHORITY, "contacts/#/data", CONTACTS_ID_DATA);
        matcher.addURI(AUTHORITY, "contacts/#/suggestions/*", AGGREGATION_SUGGESTIONS);
    }

    private SQLiteOpenHelper mDatabaseHelper;

    private final ThreadLocal<SQLiteOpenHelper> mDbHelper =
            new ThreadLocal<>(); // ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。

    @Override
    public boolean onCreate() {
        mDatabaseHelper = getDatabaseHelper(getContext());
        mDbHelper.set(mDatabaseHelper);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    protected SQLiteOpenHelper getDatabaseHelper(Context context) {
        return AppDatabaseHelper.getInstance(context);
    }

    @Override
    protected ThreadLocal<AppTransaction> getTransactionHolder() {
        return null;
    }

    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    protected boolean yield(AppTransaction transaction) {
        return false;
    }

    @Override
    protected void notifyChange() {

    }
}
