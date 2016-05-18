package com.stakkfactory.mediacache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielchung on 28/4/16.
 */
public class DiskCache {

    public final static String TAG = DiskCache.class.getSimpleName();

    /**
     * Default Value Count for DiskLruCache
     */
    private static final int DEFAULT_VALUE_COUNT = 1;

    /**
     * Default Index for DiskLruCache
     */
    private static final int DEFAULT_VALUE_IDX = 0;

    /**
     * Disk Cache lib from com.jakewharton.disklrucache
     * <p/>
     * Github - https://github.com/JakeWharton/DiskLruCache
     */
    DiskLruCache diskLruCache;

    /**
     * DB to store cache metadata
     */
    MetadataDBHelper metadataDBHelper;

    /**
     * @param context uas Application Context to prevent memory leakage
     * @param dirName to create or get, all dir will under com.stakkfactory.cache dir want
     * @param maxSize Maximum number of bytes this com.stakkfactory.cache should use to store
     * @throws IOException
     */
    public DiskCache(Context context, String dirName, long maxSize)
            throws IOException {
        File defaultRootDir = getDiskCacheDir(context, DiskCache.class.getName());
        File cacheDir = new File(defaultRootDir, dirName);

        this.diskLruCache = DiskLruCache.open(cacheDir, MediaCacheManager.CACHE_SCHEME_VERSION, DEFAULT_VALUE_COUNT, maxSize);
        this.metadataDBHelper = new MetadataDBHelper(context, dirName);
    }

    /**
     * Check is a media stored in cache dir
     *
     * @param endpoint as key for the media cache
     * @return
     */
    public boolean contains(String endpoint) {
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = diskLruCache.get(MediaCacheManager.Metadata.toCacheKey(endpoint));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (snapshot == null)
            return false;

        snapshot.close();
        return true;
    }

    public InputStream getInputStream(String endpoint) {
        InputStream is = null;

        DiskLruCache.Snapshot snapShot;
        try {
            snapShot = diskLruCache.get(MediaCacheManager.Metadata.toCacheKey(endpoint));

            if (snapShot != null) {
                is = snapShot.getInputStream(DEFAULT_VALUE_IDX);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return is;
    }

    public FileDescriptor getFD(String endpoint) {
        FileDescriptor fd = null;
        try {
            fd = ((FileInputStream) getInputStream(endpoint)).getFD();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fd;
    }

    public String getTempFilePath(String endpoint) {
        File temp;
        FileOutputStream output;

        String path = null;
        try {
            temp = File.createTempFile(MediaCacheManager.Metadata.toCacheKey(endpoint), endpoint.substring(endpoint.lastIndexOf(".")));
            temp.deleteOnExit();

            path = temp.getAbsolutePath();

            output = new FileOutputStream(temp);

            copyStream(getInputStream(endpoint), output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    public Bitmap getBitmap(String endpoint) {
        Bitmap bitmap = null;

        InputStream is = getInputStream(endpoint);

        if (is != null) {
            bitmap = BitmapFactory.decodeStream(is);
        }

        return bitmap;
    }

    public String getHTMLString(String endpoint) {
        return getString(endpoint);
    }

    /**
     * Store a media to cache dir
     *
     * @param mediaCache
     * @throws IOException
     */
    public void put(MediaCacheManager.MediaCache mediaCache) throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(mediaCache.getMetadata().getCacheKey());
        OutputStream os = editor.newOutputStream(DEFAULT_VALUE_IDX);

        try {
            boolean success = false;

            if (mediaCache.getMedia() instanceof Bitmap) {
                // Bitmap Image
                Bitmap bitmap = (Bitmap) mediaCache.getMedia();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

                os.flush();

                success = true;

            } else if (mediaCache.getMedia() instanceof InputStream) {
                // Gif / Video InputStream
                copyStream((InputStream) mediaCache.getMedia(), os);

                success = true;

            } else if (mediaCache.getMedia() instanceof String) {
                // HTML String
                String string = (String) mediaCache.getMedia();
                os.write(string.getBytes());

                success = true;
            }

            if (success) {
                os.close();
                editor.commit();

                metadataDBHelper.insertEntry(mediaCache.getMetadata());
            }
        } catch (IOException e) {
            editor.abort();

            throw e;
        }
    }

    /**
     * Delete all cached media from dir
     */
    public void purge() {
        final File dir = diskLruCache.getDirectory();
        final long maxSize = diskLruCache.getMaxSize();

        try {
            diskLruCache.delete();
            diskLruCache = DiskLruCache.open(dir, MediaCacheManager.CACHE_SCHEME_VERSION, DEFAULT_VALUE_COUNT, maxSize);


            metadataDBHelper.dropTable(metadataDBHelper.getWritableDatabase());
            metadataDBHelper.createTable(metadataDBHelper.getWritableDatabase());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a cache by cacheKey
     *
     * @param cacheKey
     */
    public void purge(String cacheKey) {
        try {
            if (diskLruCache.remove(cacheKey)) {
                metadataDBHelper.deleteEntry(cacheKey);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the expired cache
     */
    public void purgeExpiredCache() {
        final List<String> expiredCacheKeys = getExpiredCacheKeys();

        for (String key : expiredCacheKeys) {
            purge(key);
        }
    }

    private List<String> getAllCacheKeys() {
        return metadataDBHelper.getAllEntries();
    }

    private List<String> getExpiredCacheKeys() {
        return metadataDBHelper.getExpiredEntries();
    }

    private String getString(String key) {
        String string = null;

        DiskLruCache.Snapshot snapShot;
        try {
            snapShot = diskLruCache.get(key);

            if (snapShot != null) {
                string = snapShot.getString(DEFAULT_VALUE_IDX);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return string;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;

        // First try to use external storage
        // File location: /sdcard/Android/data/<application package>/com.stakkfactory.cache
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            // If no external sd card, use internal storage
            // File location: /data/data/<application package>/com.stakkfactory.cache
            cachePath = context.getCacheDir().getPath();
        }

        cachePath = cachePath + File.separator + uniqueName;
        Log.d(TAG, "Cache Path: " + cachePath);

        return new File(cachePath);
    }

    private void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private static class MetadataDBHelper extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = MediaCacheManager.CACHE_SCHEME_VERSION;
        public static final String DATABASE_NAME = "%s.db";

        public MetadataDBHelper(Context context, String dirName) {
            super(context, String.format(DATABASE_NAME, dirName), null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            dropTable(db);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public void createTable(SQLiteDatabase db) {
            db.execSQL(MetaDataEntry.SQL_CREATE_TABLE);
        }

        public void dropTable(SQLiteDatabase db) {
            db.execSQL(MetaDataEntry.SQL_DELETE_TABLE);
        }

        public void insertEntry(MediaCacheManager.Metadata metadata) {
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(MetaDataEntry.COLUMN_NAME_ENDPOINT, metadata.getEndpoint());
            values.put(MetaDataEntry.COLUMN_NAME_CACHE_KEY, metadata.getCacheKey());
            values.put(MetaDataEntry.COLUMN_NAME_TYPE, metadata.getType());
            values.put(MetaDataEntry.COLUMN_NAME_EXTENSION, metadata.getExtension());
            values.put(MetaDataEntry.COLUMN_NAME_CREATED_TIME, metadata.getCreatedTime());
            values.put(MetaDataEntry.COLUMN_NAME_END_TIME, metadata.getEndTime());

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    MetaDataEntry.TABLE_NAME,
                    null,
                    values);

            Log.d(TAG, "insertMetaData row: " + newRowId + " key: " + metadata.getCacheKey() +
                    " createdTime: " + metadata.getCreatedTime() + " endTime: " + metadata.getEndTime());

            db.close();
        }

        public List<String> getAllEntries() {
            List<String> entries = new ArrayList<>();

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(MetaDataEntry.SQL_FIND_ALL_ENTRIES, null);

            Log.d(TAG, "getAllEntries");

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String key = cursor.getString(0);

                    Log.d(TAG, "getAllEntries cacheKey: " + key);
                    entries.add(key);
                } while (cursor.moveToNext());
            }

            db.close();

            return entries;
        }

        public List<String> getExpiredEntries() {
            List<String> entries = new ArrayList<>();

            long now = System.currentTimeMillis();

            Log.d(TAG, "getExpiredEntries now: " + now);

            String query = String.format(MetaDataEntry.SQL_FIND_EXPIRED_ENTRIES, now);

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(query, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    String key = cursor.getString(0);

                    Log.d(TAG, "getExpiredEntries cacheKey: " + key);
                    entries.add(key);
                } while (cursor.moveToNext());
            }

            db.close();

            return entries;
        }

        public void deleteEntry(String cacheKey) {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = String.format(MetaDataEntry.SQL_DELETE_ONE_ENTRIES, cacheKey);
            db.execSQL(query);

            db.close();

        }

        static class MetaDataEntry implements BaseColumns {

            static String TABLE_NAME = MetaDataEntry.class.getSimpleName();

            static String COLUMN_NAME_ENDPOINT = "endpoint";
            static String COLUMN_NAME_CACHE_KEY = "cacheKey";
            static String COLUMN_NAME_TYPE = "type";
            static String COLUMN_NAME_EXTENSION = "extension";
            static String COLUMN_NAME_CREATED_TIME = "createdTime";
            static String COLUMN_NAME_END_TIME = "endTime";

            static final String SQL_CREATE_TABLE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            _ID + " INTEGER PRIMARY KEY, " +
                            COLUMN_NAME_ENDPOINT + " TEXT, " +
                            COLUMN_NAME_CACHE_KEY + " TEXT, " +
                            COLUMN_NAME_TYPE + " INTEGER, " +
                            COLUMN_NAME_EXTENSION + " TEXT, " +
                            COLUMN_NAME_CREATED_TIME + " LONG, " +
                            COLUMN_NAME_END_TIME + " LONG" +
                            " )";

            static final String SQL_DELETE_TABLE =
                    "DROP TABLE IF EXISTS " + TABLE_NAME;

            static final String SQL_FIND_ALL_ENTRIES =
                    "SELECT " + COLUMN_NAME_CACHE_KEY + " FROM " + TABLE_NAME;

            static final String SQL_FIND_EXPIRED_ENTRIES =
                    "SELECT " + COLUMN_NAME_CACHE_KEY + " FROM " + TABLE_NAME +
                            " WHERE %d >= " + COLUMN_NAME_END_TIME;

            static final String SQL_DELETE_ONE_ENTRIES =
                    "DELETE FROM " + TABLE_NAME +
                            "WHERE " + COLUMN_NAME_CACHE_KEY + " = '%s'";
        }
    }
}