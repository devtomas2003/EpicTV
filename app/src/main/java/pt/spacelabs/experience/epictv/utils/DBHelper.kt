package pt.spacelabs.experience.epictv.utils;

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import pt.spacelabs.experience.epictv.entitys.Content

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DBName, null, 1) {

    override fun onCreate(epicTV: SQLiteDatabase) {
        epicTV.execSQL("CREATE TABLE configs(configType TEXT PRIMARY KEY, value TEXT)")
        epicTV.execSQL("CREATE TABLE offlinePlayback(id TEXT PRIMARY KEY, episodeId TEXT, movieId TEXT, chunk TEXT)")
        epicTV.execSQL("CREATE TABLE movies(id TEXT PRIMARY KEY, name TEXT, time INTEGER, description TEXT, poster TEXT, isDone INTEGER, isActive INTEGER)")
    }

    override fun onUpgrade(epicTV: SQLiteDatabase, i: Int, i1: Int) {
        epicTV.execSQL("DROP TABLE IF EXISTS configs")
        epicTV.execSQL("DROP TABLE IF EXISTS offlinePlayback")
        epicTV.execSQL("DROP TABLE IF EXISTS movies")
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

    fun createChunk(id: String?, episodeId: String?, movieId: String?, chunk: String?) {
        val epicTV = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("episodeId", episodeId)
        contentValues.put("movieId", movieId)
        contentValues.put("chunk", chunk)
        epicTV.insert("offlinePlayback", null, contentValues)
    }

    fun createMovie(id: String?, name: String?, time: Int?, description: String?, poster: String?, isDone: Int?, isActive: Boolean?) {
        val epicTV = this.writableDatabase
        val contentValues = ContentValues()
        var isact = 0

        if(isActive == true){
            isact = 1
        }

        contentValues.put("id", id)
        contentValues.put("name", name)
        contentValues.put("time", time)
        contentValues.put("description", description)
        contentValues.put("poster", poster)
        contentValues.put("isDone", isDone)
        contentValues.put("isActive", isact)
        epicTV.insert("movies", null, contentValues)
    }

    fun getMovies(showActive: Boolean): MutableList<Content> {
        val movies = mutableListOf<Content>()
        var query = ""

        if(showActive){
            query = "SELECT id, name, time, description, poster FROM movies WHERE isDone = 1 AND isActive = 1"
        }else{
            query = "SELECT id, name, time, description, poster FROM movies WHERE isDone = 1"
        }

        val cursor: Cursor = this.writableDatabase.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val time = cursor.getInt(cursor.getColumnIndexOrThrow("time"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val poster = cursor.getString(cursor.getColumnIndexOrThrow("poster"))
                movies.add(Content(id, poster, name, time, description, true))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return movies
    }

    fun checkIfIsAvailableOffline(movieId: String): Boolean {
        val epicTV = this.readableDatabase
        val cursor = epicTV.rawQuery(
            "SELECT isDone FROM movies WHERE id = ?",
            arrayOf(movieId)
        )

        var result = false
        if (cursor.moveToFirst()) {
            if(cursor.getInt(cursor.getColumnIndexOrThrow("isDone")) == 1){
                result = true
            }
        }

        cursor.close()
        return result
    }

    fun updateMovie(movieId: String) {
        val epicTV = this.readableDatabase
        val con = ContentValues()
        con.put("isDone", 1)
        epicTV.update("movies", con, "id = ?", arrayOf(movieId))
    }

    fun updateMovieData(movieId: String?, name: String?, time: Int?, description: String?, poster: String?, isActive: Boolean?) {
        val epicTV = this.readableDatabase
        val con = ContentValues()
        var isact = 0

        if(isActive == true){
            isact = 1
        }

        con.put("name", name)
        con.put("time", time)
        con.put("description", description)
        con.put("poster", poster)
        con.put("isActive", isact)

        epicTV.update("movies", con, "id = ?", arrayOf(movieId))
    }

    fun getChunksByMovieId(movieId: String): List<String> {
        val playbacks = mutableListOf<String>()
        val query = "SELECT chunk FROM offlinePlayback WHERE movieId = ?"
        val cursor: Cursor = this.readableDatabase.rawQuery(query, arrayOf(movieId))

        if (cursor.moveToFirst()) {
            do {
                val chunk = cursor.getString(cursor.getColumnIndexOrThrow("chunk"))
                playbacks.add(chunk)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return playbacks
    }

    fun deleteMovieLocal(movieId: String){
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM movies WHERE id = ?", arrayOf(movieId))
        leanifyDB.execSQL("DELETE FROM offlinePlayback WHERE movieId = ?", arrayOf(movieId))
    }

    fun deleteMovie(movieId: String){
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM movies WHERE id = ?", arrayOf(movieId))
    }

    fun deleteMovieChunks(movieId: String){
        val leanifyDB = this.writableDatabase
        leanifyDB.execSQL("DELETE FROM offlinePlayback WHERE movieId = ?", arrayOf(movieId))
    }

    companion object {
        const val DBName: String = "epictv.db"
    }
}
