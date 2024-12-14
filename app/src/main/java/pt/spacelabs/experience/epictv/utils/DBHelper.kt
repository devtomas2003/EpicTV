package pt.spacelabs.experience.epictv.utils;

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DBName, null, 1) {

    override fun onCreate(epicTV: SQLiteDatabase) {
        epicTV.execSQL("CREATE TABLE configs(configType TEXT PRIMARY KEY, value TEXT)")
    }

    override fun onUpgrade(epicTV: SQLiteDatabase, i: Int, i1: Int) {
        epicTV.execSQL("DROP TABLE IF EXISTS configs")
    }

    fun createConfig(configType: String?, value: String?) {
        val epicTV = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("configType", configType)
        contentValues.put("value", value)
        epicTV.insert("configs", null, contentValues)
    }

    fun getConfig(configType: String): String {
        val epicTV = this.readableDatabase
        val cursor = epicTV.rawQuery(
            "SELECT * FROM configs WHERE configType = ?",
            arrayOf(configType)
        )

        var result = "none"
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow("value"))
        }

        cursor.close()
        return result
    }

    fun updateConfig(configType: String, value: String) {
        val epicTV = this.readableDatabase
        val con = ContentValues()
        con.put("value", value)
        epicTV.update("configs", con, "configType = ?", arrayOf(configType))
    }

    fun clearConfig(configType: String) {
        val epicTV = this.writableDatabase
        epicTV.execSQL("DELETE FROM configs WHERE configType = ?", arrayOf(configType))
    }

    companion object {
        const val DBName: String = "epictv.db"
    }
}
