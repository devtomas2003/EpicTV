package pt.spacelabs.experience.epictv.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBName = "epictv.db";

    public DBHelper(Context context) {
        super(context, DBName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase epictvDB) {
        epictvDB.execSQL("CREATE TABLE offlinePlayback(contentId TEXT PRIMARY KEY, downloadDone INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase epictvDB, int i, int i1) {
        epictvDB.execSQL("DROP TABLE IF EXISTS offlinePlayback");
    }

    public void createOfflinePlayback(String contentId){
        SQLiteDatabase epictvDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("contentId", contentId);
        contentValues.put("downloadDone", 1);
        long result = epictvDB.insert("offlinePlayback", null, contentValues);
    }

    public boolean checkPlaybackPresence(String contentId){
        SQLiteDatabase epictvDB = this.getReadableDatabase();
        Cursor cursor = epictvDB.rawQuery("SELECT * FROM offlinePlayback WHERE contentId = ?", new String[] { contentId });

        if(cursor.getCount() == 1){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void clearOfflinePlaybackCache(String contentId){
        SQLiteDatabase epictvDB = this.getWritableDatabase();
        epictvDB.execSQL("DELETE FROM offlinePlayback WHERE contentId = ?", new String[] { contentId });
    }
}
