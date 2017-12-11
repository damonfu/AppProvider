package com.provider.demo.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/11.
 */

public abstract class AbstractProvider extends ContentProvider {


    public static final String TAG = "AppProvider";

    public static final boolean VERBOSE_LOGGING = Log.isLoggable(TAG, Log.VERBOSE);

    /** Set true to enable detailed transaction logging. */
    public static final boolean ENABLE_TRANSACTION_LOG = false; // Don't submit with true.

    /**
     * Duration in ms to sleep after successfully yielding the lock during a batch operation.
     */
    protected static final int SLEEP_AFTER_YIELD_DELAY = 4000;

    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;

    /**
     * Number of inserts performed in bulk to allow before yielding the transaction.
     */
    private static final int BULK_INSERTS_PER_YIELD_POINT = 50;

    /**
     * The contacts transaction that is active in this thread.
     */
    private ThreadLocal<AppTransaction> mTransactionHolder;

    /**
     * The DB helper to use for this content provider.
     */
    private SQLiteOpenHelper mDbHelper;

    /**
     * The database helper to serialize all transactions on.  If non-null, any new transaction
     * created by this provider will automatically retrieve a writable database from this helper
     * and initiate a transaction on that database.  This should be used to ensure that operations
     * across multiple databases are all blocked on a single DB lock (to prevent deadlock cases).
     *
     * Hint: It's always {@link AppDatabaseHelper}.
     *
     * TODO Change the structure to make it obvious that it's actually always set, and is the
     * {@link AppTransaction}.
     */
    private SQLiteOpenHelper mSerializeOnDbHelper;

    /**
     * The tag corresponding to the database used for serializing transactions.
     *
     * Hint: It's always the contacts db helper tag.
     *
     * See also the TODO on {@link #mSerializeOnDbHelper}.
     */
    private String mSerializeDbTag;

    /**
     * The transaction listener used with {@link #mSerializeOnDbHelper}.
     *
     * Hint: It's always {@link AppProvider}.
     *
     * See also the TODO on {@link #mSerializeOnDbHelper}.
     */
    private SQLiteTransactionListener mSerializedDbTransactionListener;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = getDatabaseHelper(context);
        mTransactionHolder = getTransactionHolder();
        return true;
    }

    public SQLiteOpenHelper getDatabaseHelper() {
        return mDbHelper;
    }

    /**
     * Specifies a database helper (and corresponding tag) to serialize all transactions on.
     *
     * See also the TODO on {@link #mSerializeOnDbHelper}.
     */
    public void setDbHelperToSerializeOn(SQLiteOpenHelper serializeOnDbHelper, String tag,
                                         SQLiteTransactionListener listener) {
        mSerializeOnDbHelper = serializeOnDbHelper;
        mSerializeDbTag = tag;
        mSerializedDbTransactionListener = listener;
    }

    public AppTransaction getCurrentTransaction() {
        return mTransactionHolder.get();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        AppTransaction transaction = startTransaction(false);
        try {
            Uri result = insertInTransaction(uri, values);
            if (result != null) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return result;
        } finally {
            endTransaction(false);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        AppTransaction transaction = startTransaction(false);
        try {
            int deleted = deleteInTransaction(uri, selection, selectionArgs);
            if (deleted > 0) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return deleted;
            /**
             * M: [ALPS00596515]in case of Low Storage, we should catch this exception.
             * otherwise the Dialer can not launch successfully, and MO is impossible @{
             */
        } catch (android.database.sqlite.SQLiteFullException ex) {
            Log.e(TAG, "[delete]catch SQLiteFullException for delete");
            return 0;
            /**
             * [ALPS00596515] @}
             */
        } finally {
            try { /** M: [ALPS00596515] low storage should catch exception */
                endTransaction(false);
                /**
                 * M: [ALPS00596515]in case of Low Storage, we should catch this exception.
                 * otherwise the Dialer can not launch successfully, and MO is impossible @{
                 */
            } catch (android.database.sqlite.SQLiteCantOpenDatabaseException ex) {
                Log.e(TAG, "[delete]catch SQLiteCantOpenDatabaseException for endTransaction");
            }
            /**
             * [ALPS00596515] @}
             */
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        AppTransaction transaction = startTransaction(false);
        try {
            int updated = updateInTransaction(uri, values, selection, selectionArgs);
            if (updated > 0) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return updated;
        } finally {
            endTransaction(false);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        AppTransaction transaction = startTransaction(true);
        int numValues = values.length;
        int opCount = 0;
        try {
            for (int i = 0; i < numValues; i++) {
                insert(uri, values[i]);
                if (++opCount >= BULK_INSERTS_PER_YIELD_POINT) {
                    opCount = 0;
                    try {
                        yield(transaction);
                    } catch (RuntimeException re) {
                        transaction.markYieldFailed();
                        throw re;
                    }
                }
            }
            transaction.markSuccessful(true);
        } finally {
            endTransaction(true);
        }
        return numValues;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        if (VERBOSE_LOGGING) {
            Log.v(TAG, "applyBatch: " + operations.size() + " ops");
        }
        int ypCount = 0;
        int opCount = 0;
        AppTransaction transaction = startTransaction(true);
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount >= MAX_OPERATIONS_PER_YIELD_POINT) {
                    throw new OperationApplicationException(
                            "Too many content provider operations between yield points. "
                                    + "The maximum number of operations per yield point is "
                                    + MAX_OPERATIONS_PER_YIELD_POINT, ypCount);
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    if (VERBOSE_LOGGING) {
                        Log.v(TAG, "applyBatch: " + opCount + " ops finished; about to yield...");
                    }
                    opCount = 0;
                    try {
                        if (yield(transaction)) {
                            ypCount++;
                        }
                    } catch (RuntimeException re) {
                        transaction.markYieldFailed();
                        throw re;
                    }
                }

                results[i] = operation.apply(this, results, i);
            }
            transaction.markSuccessful(true);
            return results;
        } finally {
            endTransaction(true);
        }
    }

    /**
     * If we are not yet already in a transaction, this starts one (on the DB to serialize on, if
     * present) and sets the thread-local transaction variable for tracking.  If we are already in
     * a transaction, this returns that transaction, and the batch parameter is ignored.
     * @param callerIsBatch Whether the caller is operating in batch mode.
     */
    private AppTransaction startTransaction(boolean callerIsBatch) {
        if (ENABLE_TRANSACTION_LOG) {
            Log.i(TAG, "startTransaction " + getClass().getSimpleName() +
                    "  callerIsBatch=" + callerIsBatch, new RuntimeException("startTransaction"));
        }
        AppTransaction transaction = mTransactionHolder.get();
        if (transaction == null) {
            transaction = new AppTransaction(callerIsBatch);
            if (mSerializeOnDbHelper != null) {
                transaction.startTransactionForDb(mSerializeOnDbHelper.getWritableDatabase(),
                        mSerializeDbTag, mSerializedDbTransactionListener);
            }
            mTransactionHolder.set(transaction);
        }
        return transaction;
    }

    /**
     * Ends the current transaction and clears out the member variable.  This does not set the
     * transaction as being successful.
     * @param callerIsBatch Whether the caller is operating in batch mode.
     */
    private void endTransaction(boolean callerIsBatch) {
        if (ENABLE_TRANSACTION_LOG) {
            Log.i(TAG, "endTransaction " + getClass().getSimpleName() +
                    "  callerIsBatch=" + callerIsBatch, new RuntimeException("endTransaction"));
        }
        AppTransaction transaction = mTransactionHolder.get();
        if (transaction != null && (!transaction.isBatch() || callerIsBatch)) {
            try {
                if (transaction.isDirty()) {
                    notifyChange();
                }
                transaction.finish(callerIsBatch);
            } finally {
                // No matter what, make sure we clear out the thread-local transaction reference.
                mTransactionHolder.set(null);
            }
        }
    }

    /**
     * Gets the database helper for this contacts provider.  This is called once, during onCreate().
     */
    protected abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    /**
     * Gets the thread-local transaction holder to use for keeping track of the transaction.  This
     * is called once, in onCreate().  If multiple classes are inheriting from this class that need
     * to be kept in sync on the same transaction, they must all return the same thread-local.
     */
    protected abstract ThreadLocal<AppTransaction> getTransactionHolder();

    protected abstract Uri insertInTransaction(Uri uri, ContentValues values);

    protected abstract int deleteInTransaction(Uri uri, String selection, String[] selectionArgs);

    protected abstract int updateInTransaction(Uri uri, ContentValues values, String selection,
                                               String[] selectionArgs);

    protected abstract boolean yield(AppTransaction transaction);

    protected abstract void notifyChange();

    /* package */ static boolean readBooleanQueryParameter(
            Uri uri, String parameter, boolean defaultValue) {

        // Manually parse the query, which is much faster than calling uri.getQueryParameter
        String query = uri.getEncodedQuery();
        if (query == null) {
            return defaultValue;
        }

        int index = query.indexOf(parameter);
        if (index == -1) {
            return defaultValue;
        }

        index += parameter.length();

        return !matchQueryParameter(query, index, "=0", false)
                && !matchQueryParameter(query, index, "=false", true);
    }

    private static boolean matchQueryParameter(
            String query, int index, String value, boolean ignoreCase) {

        int length = value.length();
        return query.regionMatches(ignoreCase, index, value, 0, length)
                && (query.length() == index + length || query.charAt(index + length) == '&');
    }

    /**
     * A fast re-implementation of {@link Uri#getQueryParameter}
     */
    /* package */ static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }

        int queryLength = query.length();
        int parameterLength = parameter.length();

        String value;
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }

            // Should match against the whole parameter instead of its suffix.
            // e.g. The parameter "param" must not be found in "some_param=val".
            if (index > 0) {
                char prevChar = query.charAt(index - 1);
                if (prevChar != '?' && prevChar != '&') { //对后缀参数进行解析
                    // With "some_param=val1&param=val2", we should find second "param" occurrence.
                    index += parameterLength;
                    continue;
                }
            }

            index += parameterLength;

            if (queryLength == index) {
                return null;
            }

            if (query.charAt(index) == '=') {
                index++;
                break;
            }
        }

        int ampIndex = query.indexOf('&', index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }

        return Uri.decode(value);
    }
}
