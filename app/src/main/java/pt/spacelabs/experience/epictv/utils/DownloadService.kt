package pt.spacelabs.experience.epictv.utils

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.R
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {
    private var notificationManager: NotificationManager? = null
    private var totalFiles = 0
    private var downloadedFiles = 0

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun updateNotification(manifestName: String?, downloadedFiles: Int, totalFiles: Int) {
        val progress = ((downloadedFiles / totalFiles.toFloat()) * 100).toInt()
        if (manifestName != null) {
            showNotification(manifestName, "Descarregado: $progress%", progress)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(
                Method.GET,
                Constants.baseURL + "/offlineChunks?manifestName=" + intent.getStringExtra("manifestName") + "&isSerie=false",
                Response.Listener { response ->
            try {
                val jo = JSONObject(response)
                val chunks = mutableListOf<String>()
                var streamQuality = ""
                var movieId = ""
                val chunksArr = jo.getJSONArray("chunks")
                streamQuality = jo.getString("quality")
                movieId = jo.getString("movieId")

                for (i in 0 until chunksArr.length()) {
                    val jsonObject = chunksArr.getJSONObject(i)
                    val chunk = jsonObject.getString("chunk")
                    chunks.add(chunk)
                }

                chunks.add(intent.getStringExtra("manifestName") + ".m3u8")
                val fileUrls = chunks.toTypedArray()

                totalFiles = fileUrls.size
                showNotification("Download Started", "Downloading files...", 0)

                Thread {
                    try {
                        val dbh = DBHelper(this)
                        for (fileUrl in fileUrls) {
                            try {
                                downloadFile("https://vis-ipv-cda.epictv.spacelabs.pt/$movieId/$streamQuality/$fileUrl");
                                downloadedFiles++
                                updateNotification(intent.getStringExtra("contentName"), downloadedFiles, totalFiles)
                                intent.getStringExtra("manifestName")?.let { manifestName ->
                                    val modifiedFileUrl = if (fileUrl.length > 3) fileUrl.dropLast(3) else fileUrl
                                    dbh.createChunk(
                                        modifiedFileUrl,
                                        "nothing",
                                        manifestName,
                                        fileUrl
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(
                                        TAG,
                                        "Error downloading file: $fileUrl",
                                        e
                                )
                            }
                        }

                        showNotification("Download Complete", "All files downloaded.", 100)
                    } catch (e: Exception) {
                        Log.e(
                                TAG,
                                "Download process failed",
                                e
                        )
                        showNotification("Download Failed", "Error occurred during download.", 0)
                    } finally {
                        stopSelf()
                    }
                }.start()

            } catch (e: JSONException) {
                AlertDialog.Builder(this)
                        .setTitle("Falha de ligação")
                        .setMessage("Ocorreu um erro com a resposta do servidor!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
            }
        },
        Response.ErrorListener { error ->
            try {
                val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                val errorObject = JSONObject(errorResponse)
                if (errorObject.has("Errors")) {
                    val errors = errorObject.getJSONArray("Errors")
                    val errorMessage = errors.getString(0)
                    AlertDialog.Builder(this)
                            .setTitle("Ocorreu um erro")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                }
            } catch (e: Exception) {
                AlertDialog.Builder(this)
                        .setTitle("Falha de ligação")
                        .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
            }
        }
        ) {
            override fun getBody(): ByteArray {
                val body = JSONObject()
                return body.toString().toByteArray(Charsets.UTF_8)
            }
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@DownloadService).getConfig("token")
                headers["Authorization"] = auth
                return headers
            }
            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }
        }

        requestQueue.add(stringRequest)

        return START_STICKY
    }

    @Throws(Exception::class)
    private fun downloadFile(fileUrl: String) {
        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Authorization", "Bearer " + DBHelper(this@DownloadService).getConfig("token"))
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("Server returned HTTP " + connection.responseCode + " " + connection.responseMessage)
        }

        val input: InputStream = BufferedInputStream(connection.inputStream)
        val fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1)
        val output = openFileOutput(fileName, MODE_PRIVATE)

        val data = ByteArray(4096)
        var count: Int
        while ((input.read(data).also { count = it }) != -1) {
            output.write(data, 0, count)
        }

        output.flush()
        output.close()
        input.close()
    }

    private fun showNotification(title: String, message: String, progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        val name: CharSequence = "Transferências"
        val description = "Canal de notificações para as transferências de conteudo"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        notificationManager!!.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? {
    return null
    }

    companion object {
        private const val TAG = "DownloadService"
        private const val CHANNEL_ID = "download_channel"
    }
}