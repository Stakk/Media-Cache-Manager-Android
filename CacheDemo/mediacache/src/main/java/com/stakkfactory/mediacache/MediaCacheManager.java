package com.stakkfactory.mediacache;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielchung on 27/4/16.
 */
public class MediaCacheManager {

    public final static String TAG = MediaCacheManager.class.getSimpleName();

    /**
     * Cache Manager data scheme version
     */
    static final int CACHE_SCHEME_VERSION = 1;

    /**
     * Default 1 YEAR TTL of the cached media (in ms)
     */
    private static final long DEFAULT_MAX_TTL = 365 * 24 * 60 * 60 * 1000;

    /**
     * The list of com.stakkfactory.cache directory in used
     */
    private static final List<String> usedDirNames = new ArrayList<>();

    private static boolean sHaveInit = false;


    public static synchronized void init(Context context) {
        sHaveInit = true;

        Log.d(TAG, "init");
        SharedPreferences preferences = context.getSharedPreferences(MediaCacheManager.class.getSimpleName(), Context.MODE_PRIVATE);

        int schemeVersion = preferences.getInt("CACHE_SCHEME_VERSION", 0);
        if (schemeVersion < CACHE_SCHEME_VERSION) {
            Log.d(TAG, "Scheme Version Updated. Purge all cache dir.");
            // Scheme Version Updated
            // Purge all cache dir

            DirIndexDBHelper dirIndexDBHelper = new DirIndexDBHelper(context);
            List<DirIndexDBHelper.Index> indexs = dirIndexDBHelper.getAllDirName();

            DiskCache diskCache;
            for (DirIndexDBHelper.Index index : indexs) {
                try {
                    diskCache = new DiskCache(context.getApplicationContext(), index.dirName, index.dirSize);

                    diskCache.purge();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            preferences.edit().putInt("CACHE_SCHEME_VERSION", CACHE_SCHEME_VERSION).commit();
        } else {
            Log.d(TAG, "Scheme Version Not Change. Check any expired cache.");
            // Scheme Version Not Change
            // Check any expired cache

            DirIndexDBHelper dirIndexDBHelper = new DirIndexDBHelper(context);
            List<DirIndexDBHelper.Index> indexs = dirIndexDBHelper.getAllDirName();

            DiskCache diskCache;
            for (DirIndexDBHelper.Index index : indexs) {
                try {
                    diskCache = new DiskCache(context.getApplicationContext(), index.dirName, index.dirSize);

                    diskCache.purgeExpiredCache();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param context use Application Context to prevent memory leakage
     * @param dirName want to create or get
     * @param maxSize of bytes this cache dir should use to store
     * @throws IOException
     */
    public static synchronized DiskCache openCache(Context context, String dirName, long maxSize)
            throws IOException {

        Log.d(TAG, "openCache");
        if (!sHaveInit) {
            Log.d(TAG, "!sHaveInit");
            init(context);
        }

        if (usedDirNames.contains(dirName)) {
            throw new IllegalStateException("Cache dir " + dirName + " was used before.");
        }

        usedDirNames.add(dirName);

        // Add an index if not exists
        DirIndexDBHelper dirIndexDBHelper = new DirIndexDBHelper(context);
        if (!dirIndexDBHelper.isDirExists(dirName)) {
            DirIndexDBHelper.Index index = new DirIndexDBHelper.Index();
            index.dirName = dirName;
            index.dirSize = maxSize;

            dirIndexDBHelper.addDir(index);
        }

        return new DiskCache(context.getApplicationContext(), dirName, maxSize);
    }

    /**
     * Generic Class for Cache object with metadata
     *
     * @param <T> Media Resource Generic Type. Eg. String, Bitmap, Drawable, InputStream...
     */
    public static class MediaCache<T> {

        /**
         * Cache Manager support media type
         */
        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_GIF = 2;
        public static final int MEDIA_TYPE_VIDEO = 3;
        public static final int MEDIA_TYPE_HTML = 4;

        private final T media;
        private final Metadata metadata;

        public MediaCache(T media, Metadata metadata) {
            if (metadata == null) {
                throw new IllegalArgumentException("MediaCache metadata can not be null.");
            }

            int mediaType = metadata.getType();
            if (!(mediaType == MEDIA_TYPE_IMAGE && media instanceof Bitmap) &&
                    !(mediaType == MEDIA_TYPE_GIF && media instanceof InputStream) &&
                    !(mediaType == MEDIA_TYPE_VIDEO && media instanceof InputStream) &&
                    !(mediaType == MEDIA_TYPE_HTML && media instanceof String)) {

                throw new IllegalArgumentException("MediaCache mediaType and media instance not match.");
            }

            this.media = media;
            this.metadata = metadata;
        }

        public T getMedia() {
            return media;
        }

        public Metadata getMetadata() {
            return metadata;
        }
    }

    public static class Metadata {

        private String endpoint;
        private String cacheKey;
        private int type;
        private String extension;
        private long createdTime;
        private long endTime;

        /**
         * Metadata of the media cache
         *
         * @param endpoint  url or path of the media
         * @param mediaType media cache type
         * @param extension file extension of the media
         */

        public Metadata(String endpoint, int mediaType, String extension) {
            this(endpoint, mediaType, extension, DEFAULT_MAX_TTL);
        }

        public Metadata(String endpoint, int mediaType, String extension, long ttl) {
            this.endpoint = endpoint;
            this.cacheKey = toCacheKey(endpoint);
            this.type = mediaType;
            this.extension = extension;
            this.createdTime = System.currentTimeMillis();
            this.endTime = createdTime + ttl;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getCacheKey() {
            return cacheKey;
        }

        public int getType() {
            return type;
        }

        public String getExtension() {
            return extension;
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public static String toCacheKey(String key) {
            return md5(key);
        }

        private static String md5(String s) {
            try {
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(s.getBytes("UTF-8"));
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1, digest);
                return bigInt.toString(16);
            } catch (NoSuchAlgorithmException e) {
                throw new AssertionError();
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError();
            }
        }
    }

    private static class DirIndexDBHelper extends SQLiteOpenHelper {

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "DirIndex.db";

        public DirIndexDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
            db.execSQL(IndexEntry.SQL_CREATE_TABLE);
        }

        public void dropTable(SQLiteDatabase db) {
            db.execSQL(IndexEntry.SQL_DELETE_TABLE);
        }

        public void addDir(Index index) {
            SQLiteDatabase db = this.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(IndexEntry.COLUMN_NAME_DIR_NAME, index.dirName);
            values.put(IndexEntry.COLUMN_NAME_DIR_SIZE, index.dirSize);

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    IndexEntry.TABLE_NAME,
                    null,
                    values);

            db.close();
        }

        public boolean isDirExists(String dirName) {
            Log.d(TAG, "isDirExists: " + dirName);

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(String.format(IndexEntry.SQL_CONTAINS_DIR, dirName), null);

            int count = cursor.getCount();

            db.close();

            return count > 0;
        }

        public List<Index> getAllDirName() {
            List<Index> indexs = new ArrayList<>();

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(IndexEntry.SQL_FIND_ALL_DIR, null);

            Log.d(TAG, "getAllDirName");

            Index index;
            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    index = new Index();
                    index.dirName = cursor.getString(cursor.getColumnIndex(IndexEntry.COLUMN_NAME_DIR_NAME));
                    index.dirSize = cursor.getLong(cursor.getColumnIndex(IndexEntry.COLUMN_NAME_DIR_SIZE));

                    Log.d(TAG, "getAllDirName dirName: " + index.dirName + " dirSize: " + index.dirSize);
                    indexs.add(index);
                } while (cursor.moveToNext());
            }

            db.close();

            return indexs;
        }

        static class Index {

            String dirName;
            long dirSize;
        }

        static class IndexEntry implements BaseColumns {

            static String TABLE_NAME = IndexEntry.class.getSimpleName();

            static final String COLUMN_NAME_DIR_NAME = "dirName";
            static final String COLUMN_NAME_DIR_SIZE = "dirSize";

            static final String SQL_CREATE_TABLE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            _ID + " INTEGER PRIMARY KEY, " +
                            COLUMN_NAME_DIR_NAME + " TEXT, " +
                            COLUMN_NAME_DIR_SIZE + " LONG" +
                            " )";

            static final String SQL_DELETE_TABLE =
                    "DROP TABLE IF EXISTS " + TABLE_NAME;

            static final String SQL_FIND_ALL_DIR =
                    "SELECT * FROM " + TABLE_NAME;

            static final String SQL_CONTAINS_DIR =
                    "SELECT * FROM " + TABLE_NAME +
                            " WHERE " + COLUMN_NAME_DIR_NAME + " = '%s'";
        }
    }
}
