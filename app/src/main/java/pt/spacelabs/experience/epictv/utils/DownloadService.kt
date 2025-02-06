package pt.spacelabs.experience.epictv.utils

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Content
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {
    private var notificationManager: NotificationManager? = null
    private val CANCEL_ACTION = "pt.spacelabs.experience.epictv.utils.CANCEL_DOWNLOAD"
    private var totalFiles = 0
    private var downloadedFiles = 0
    private var fileUrl = ""
    private var cachedBitmap: Bitmap? = null
    private var isCancelled = false;

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        registerReceiver(cancelReceiver, IntentFilter(CANCEL_ACTION), RECEIVER_NOT_EXPORTED);
    }

    private fun updateNotification(manifestName: String?, downloadedFiles: Int, totalFiles: Int) {
        val progress = ((downloadedFiles / totalFiles.toFloat()) * 100).toInt()
        if (manifestName != null) {
            showNotification(manifestName, "O teu filme está a chegar: $progress%", progress, fileUrl)
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

                val fileUrls = chunks.toTypedArray()

                totalFiles = fileUrls.size
                intent.getStringExtra("imageUrl")
                    ?.let { showNotification("Download Iniciado", "A preparar para descarregar...", 0, it) }

                fileUrl = intent.getStringExtra("imageUrl").toString()

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

                        DBHelper(this).clearConfig("downloadUnderway")

                        if(!isCancelled){
                            intent.getStringExtra("manifestName")
                                ?.let { DBHelper(this).updateMovie(it) }
                            showSuccessNotification("Download Terminado", "O teu filme está pronto para veres offline.")
                        }else{
                            intent.getStringExtra("manifestName")
                                ?.let {
                                    DBHelper(this).deleteMovieLocal(it)
                                    val chunksList = DBHelper(this).getChunksByMovieId(it)

                                    chunksList.forEach { chunkData ->
                                        deleteFile(chunkData)
                                    }
                                }
                        }

                        stopForeground(true)
                        stopSelf()
                    } catch (e: Exception) {
                        Log.e(
                                TAG,
                                "Download process failed",
                                e
                        )
                        showNotification("Download Failed", "Error occurred during download.", 0, fileUrl)
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
        if (isCancelled) return;

        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Authorization", "Bearer " + DBHelper(this@DownloadService).getConfig("token"))
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK || isCancelled) {
            connection.disconnect();
            return;
        }

        val input: InputStream = BufferedInputStream(connection.inputStream)
        val fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1)
        val output = openFileOutput(fileName, MODE_PRIVATE)

        val data = ByteArray(4096)
        var count: Int = 0
        while (!isCancelled && (input.read(data).also { count = it }) != -1) {
            output.write(data, 0, count)
        }

        output.flush()
        output.close()
        input.close()

        if (isCancelled) {
            deleteFile(fileName);
        }
    }

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isCancelled = true;
            stopForeground(true);
            notificationManager?.cancelAll();
            stopSelf();
        }
    };

    private fun showNotification(title: String, message: String, progress: Int, path: String) {
        if (isCancelled) return;

        val cancelIntent = Intent(CANCEL_ACTION)
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(100, progress, false)
            .addAction(R.drawable.icon_delete, "Cancelar", cancelPendingIntent)
            .setOngoing(true)

        if (cachedBitmap == null) {
            val futureTarget = Glide.with(this)
                .asBitmap()
                .load(Constants.contentURLPublic + path)
                .submit()

            Thread {
                try {
                    cachedBitmap = futureTarget.get()
                    Handler(Looper.getMainLooper()).post {
                        notification.setLargeIcon(cachedBitmap)
                        notificationManager?.notify(1, notification.build())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao carregar imagem com Glide", e)
                }
            }.start()
        } else {
            notification.setLargeIcon(cachedBitmap)
        }

        notificationManager?.notify(1, notification.build())

        if (!isCancelled) {
            startForeground(1, notification.build());
        }
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

    override fun onDestroy() {
        super.onDestroy();
        unregisterReceiver(cancelReceiver);
    }

    private fun showSuccessNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(2, notification)
    }

    companion object {
        private const val TAG = "DownloadService"
        private const val CHANNEL_ID = "download_channel"
    }
}