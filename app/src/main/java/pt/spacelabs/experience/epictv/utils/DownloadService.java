package pt.spacelabs.experience.epictv.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private static final String CHANNEL_ID = "download_channel";
    private NotificationManager notificationManager;
    private int totalFiles = 0;
    private int downloadedFiles = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String[] fileUrls = {
                "data00.ts", "data01.ts", "data02.ts", "data03.ts", "data04.ts", "data05.ts", "data06.ts", "data07.ts", "data08.ts", "data09.ts",
                "data10.ts", "data11.ts", "data12.ts", "data13.ts", "data14.ts", "data15.ts", "data16.ts", "data17.ts", "data18.ts", "data19.ts",
                "data20.ts", "data21.ts", "data22.ts", "data23.ts", "data24.ts", "data25.ts", "data26.ts", "data27.ts", "data28.ts", "data29.ts",
                "data30.ts", "data31.ts", "data32.ts", "data33.ts", "data34.ts", "data35.ts", "data36.ts", "data37.ts", "data38.ts", "data39.ts",
                "data40.ts", "data41.ts", "data42.ts", "data43.ts", "data44.ts", "data45.ts", "data46.ts", "data47.ts", "data48.ts", "data49.ts",
                "data50.ts", "data51.ts", "data52.ts", "data53.ts", "data54.ts", "data55.ts", "data56.ts", "data57.ts", "data58.ts", "data59.ts",
                "data60.ts", "data61.ts", "data62.ts", "data63.ts", "data64.ts", "data65.ts", "data66.ts", "data67.ts", "data68.ts", "data69.ts",
                "data70.ts", "data71.ts", "data72.ts", "data73.ts", "data74.ts", "data75.ts", "data76.ts", "data77.ts", "data78.ts", "data79.ts",
                "data80.ts", "data81.ts", "data82.ts", "data83.ts", "data84.ts", "data85.ts", "data86.ts", "data87.ts", "data88.ts", "data89.ts",
                "data90.ts", "data91.ts", "data92.ts", "data93.ts", "data94.ts", "data95.ts", "data96.ts", "data97.ts", "data98.ts", "data99.ts",
                "data100.ts", "data101.ts", "data102.ts", "data103.ts", "data104.ts", "data105.ts", "data106.ts", "data107.ts", "data108.ts", "data109.ts",
                "data110.ts", "data111.ts", "data112.ts", "data113.ts", "data114.ts", "data115.ts", "data116.ts", "data117.ts", "data118.ts", "data119.ts",
                "data120.ts", "data121.ts", "data122.ts", "data123.ts", "data124.ts", "data125.ts", "data126.ts", "data127.ts", "data128.ts", "data129.ts",
                "data130.ts", "data131.ts", "data132.ts", "data133.ts", "data134.ts", "data135.ts", "data136.ts", "data137.ts", "data138.ts", "data139.ts",
                "data140.ts", "data141.ts", "data142.ts", "data143.ts", "data144.ts", "data145.ts", "data146.ts", "data147.ts", "data148.ts", "data149.ts",
                "data150.ts", "data151.ts", "data152.ts", "data153.ts", "data154.ts", "data155.ts", "data156.ts", "data157.ts", "data158.ts", "data159.ts",
                "data160.ts", "data161.ts", "data162.ts", "data163.ts", "data164.ts", "data165.ts", "data166.ts", "data167.ts", "data168.ts", "data169.ts",
                "data170.ts", "data171.ts", "data172.ts", "data173.ts", "data174.ts", "data175.ts", "data176.ts", "data177.ts", "data178.ts", "data179.ts",
                "data180.ts", "data181.ts", "data182.ts", "data183.ts", "data184.ts", "data185.ts", "data186.ts", "data187.ts", "data188.ts", "data189.ts",
                "data190.ts", "data191.ts", "data192.ts", "data193.ts", "data194.ts", "data195.ts", "data196.ts", "data197.ts", "data198.ts", "data199.ts",
                "data200.ts", "data201.ts", "data202.ts", "data203.ts", "data204.ts", "data205.ts", "data206.ts", "data207.ts", "data208.ts", "data209.ts",
                "data210.ts", "data211.ts", "data212.ts", "data213.ts", "data214.ts", "data215.ts", "data216.ts", "data217.ts", "data218.ts", "data219.ts",
                "data220.ts", "data221.ts", "data222.ts", "data223.ts", "data224.ts", "data225.ts", "data226.ts", "data227.ts", "data228.ts", "data229.ts",
                "data230.ts", "data231.ts", "data232.ts", "data233.ts", "data234.ts", "data235.ts", "data236.ts", "data237.ts", "data238.ts", "data239.ts",
                "data240.ts", "data241.ts", "data242.ts", "data243.ts", "data244.ts", "data245.ts", "data246.ts", "data247.ts", "data248.ts", "data249.ts",
                "data250.ts", "data251.ts", "data252.ts", "data253.ts", "data254.ts", "data255.ts", "data256.ts", "data257.ts", "data258.ts", "data259.ts",
                "data260.ts", "data261.ts", "data262.ts", "data263.ts", "data264.ts", "data265.ts", "data266.ts", "data267.ts", "data268.ts", "data269.ts",
                "data270.ts", "data271.ts", "data272.ts", "data273.ts", "data274.ts", "data275.ts", "data276.ts", "data277.ts", "data278.ts", "data279.ts",
                "data280.ts", "data281.ts", "data282.ts", "data283.ts", "data284.ts", "data285.ts", "data286.ts", "data287.ts", "data288.ts", "data289.ts",
                "data290.ts", "data291.ts", "data292.ts", "data293.ts", "data294.ts", "data295.ts", "data296.ts", "data297.ts", "data298.ts", "data299.ts",
                "data300.ts", "data301.ts", "data302.ts", "data303.ts", "data304.ts", "data305.ts", "data306.ts", "data307.ts", "data308.ts", "data309.ts",
                "data310.ts", "data311.ts", "data312.ts", "data313.ts", "data314.ts", "data315.ts", "data316.ts", "data317.ts", "data318.ts", "data319.ts",
                "data320.ts", "data321.ts", "data322.ts", "data323.ts", "data324.ts", "data325.ts", "data326.ts", "data327.ts", "data328.ts", "data329.ts",
                "data330.ts", "data331.ts", "data332.ts", "data333.ts", "data334.ts", "data335.ts", "data336.ts", "data337.ts", "data338.ts", "data339.ts",
                "data340.ts", "data341.ts", "data342.ts", "data343.ts", "data344.ts", "data345.ts", "data346.ts", "data347.ts", "data348.ts", "data349.ts",
                "data350.ts", "data351.ts", "data352.ts", "data353.ts", "data354.ts", "data355.ts", "data356.ts", "data357.ts", "data358.ts", "data359.ts",
                "data360.ts", "data361.ts", "data362.ts", "data363.ts", "data364.ts", "data365.ts", "data366.ts", "data367.ts", "data368.ts", "data369.ts",
                "data370.ts", "data371.ts", "data372.ts", "data373.ts", "data374.ts", "data375.ts", "data376.ts", "data377.ts", "data378.ts", "data379.ts",
                "data380.ts", "data381.ts", "data382.ts", "data383.ts", "data384.ts", "data385.ts", "data386.ts", "data387.ts", "data388.ts", "data389.ts",
                "data390.ts", "data391.ts", "data392.ts", "data393.ts", "data394.ts", "data395.ts", "data396.ts", "data397.ts", "data398.ts", "data399.ts",
                "data400.ts", "data401.ts", "data402.ts", "data403.ts", "data404.ts", "data405.ts", "data406.ts", "data407.ts", "data408.ts", "data409.ts",
                "data410.ts", "data411.ts", "data412.ts", "data413.ts", "data414.ts", "data415.ts", "data416.ts", "data417.ts", "data418.ts", "data419.ts",
                "data420.ts", "data421.ts", "data422.ts", "data423.ts", "data424.ts", "data425.ts", "data426.ts", "data427.ts", "data428.ts", "data429.ts",
                "data430.ts", "data431.ts", "data432.ts", "data433.ts", "data434.ts", "data435.ts", "data436.ts", "data437.ts", "data438.ts", "data439.ts",
                "data440.ts", "data441.ts", "data442.ts", "data443.ts", "data444.ts", "data445.ts", "data446.ts", "data447.ts", "data448.ts", "data449.ts",
                "data450.ts", "data451.ts", "data452.ts", "data453.ts", "data454.ts", "data455.ts", "data456.ts", "data457.ts", "data458.ts", "data459.ts",
                "data460.ts", "data461.ts", "data462.ts", "data463.ts", "data464.ts", "data465.ts", "data466.ts", "data467.ts", "data468.ts", "data469.ts",
                "data470.ts", "data471.ts", "data472.ts", "data473.ts", "data474.ts", "data475.ts", "data476.ts", "data477.ts", "data478.ts", "data479.ts",
                "data480.ts", "data481.ts", "data482.ts", "data483.ts", "data484.ts", "data485.ts", "data486.ts", "data487.ts", "data488.ts", "data489.ts",
                "data490.ts", "data491.ts", "data492.ts", "data493.ts", "data494.ts", "data495.ts", "data496.ts", "data497.ts", "data498.ts", "data499.ts",
                "data500.ts", "data501.ts", "data502.ts", "data503.ts", "data504.ts", "data505.ts", "data506.ts", "data507.ts", "data508.ts", "data509.ts",
                "data510.ts", "data511.ts", "data512.ts", "data513.ts", "data514.ts", "data515.ts", "data516.ts", "data517.ts", "data518.ts", "data519.ts",
                "data520.ts", "data521.ts", "data522.ts", "data523.ts", "data524.ts", "data525.ts", "data526.ts", "data527.ts", "data528.ts", "data529.ts",
                "data530.ts", "data531.ts", "data532.ts", "data533.ts", "data534.ts", "data535.ts", "data536.ts", "data537.ts", "data538.ts", "data539.ts",
                "data540.ts", "data541.ts", "data542.ts", "data543.ts", "data544.ts", "data545.ts", "data546.ts", "data547.ts", "data548.ts", "data549.ts",
                "data550.ts", "data551.ts", "data552.ts", "data553.ts", "data554.ts", "data555.ts", "data556.ts", "data557.ts", "data558.ts", "data559.ts",
                "data560.ts", "data561.ts", "data562.ts", "data563.ts", "data564.ts", "data565.ts", "data566.ts", "data567.ts", "data568.ts", "data569.ts",
                "data570.ts", "data571.ts", "data572.ts", "data573.ts", "data574.ts", "data575.ts", "data576.ts", "data577.ts", "data578.ts", "data579.ts",
                "data580.ts", "data581.ts", "data582.ts", "data583.ts", "data584.ts", "data585.ts", "data586.ts", "data587.ts", "data588.ts", "data589.ts",
                "data590.ts", "data591.ts", "data592.ts", "data593.ts", "data594.ts", "data595.ts", "data596.ts", "data597.ts", "data598.ts", "data599.ts",
                "data600.ts", "data601.ts", "data602.ts", "data603.ts", "data604.ts", "data605.ts", "data606.ts", "data607.ts", "data608.ts", "data609.ts",
                "data610.ts", "data611.ts", "data612.ts", "data613.ts", "data614.ts", "data615.ts", "data616.ts", "data617.ts", "data618.ts", "data619.ts",
                "data620.ts", "data621.ts", "data622.ts", "data623.ts", "data624.ts", "data625.ts", "data626.ts", "data627.ts", "data628.ts", "data629.ts",
                "data630.ts", "data631.ts", "data632.ts", "data633.ts", "data634.ts", "data635.ts", "data636.ts", "data637.ts", "data638.ts", "data639.ts",
                "data640.ts", "data641.ts", "data642.ts", "data643.ts", "data644.ts", "data645.ts", "data646.ts", "data647.ts", "data648.ts", "data649.ts",
                "data650.ts", "data651.ts", "data652.ts", "data653.ts", "data654.ts", "data655.ts", "data656.ts", "data657.ts", "data658.ts", "data659.ts",
                "data660.ts", "data661.ts", "data662.ts", "data663.ts", "data664.ts", "data665.ts", "data666.ts", "data667.ts", "data668.ts", "data669.ts",
                "data670.ts", "data671.ts", "data672.ts", "data673.ts", "data674.ts", "data675.ts", "data676.ts", "data677.ts", "data678.ts", "data679.ts",
                "data680.ts", "data681.ts", "data682.ts", "data683.ts", "data684.ts", "data685.ts", "data686.ts", "data687.ts", "data688.ts", "data689.ts",
                "data690.ts", "data691.ts", "data692.ts", "data693.ts", "data694.ts", "data695.ts", "data696.ts", "data697.ts", "data698.ts", "data699.ts",
                "data700.ts", "data701.ts", "data702.ts", "data703.ts", "data704.ts", "data705.ts", "data706.ts", "data707.ts", "data708.ts", "data709.ts",
                "data710.ts", "data711.ts", "data712.ts", "data713.ts", "data714.ts", "data715.ts", "data716.ts", "data717.ts", "data718.ts", "data719.ts",
                "data720.ts", "data721.ts", "data722.ts", "data723.ts", "data724.ts", "data725.ts", "data726.ts", "data727.ts", "data728.ts", "data729.ts",
                "data730.ts", "data731.ts", "data732.ts", "data733.ts", "data734.ts", "data735.ts", "data736.ts", "data737.ts", "data738.ts", "data739.ts",
                "data740.ts", "data741.ts", "data742.ts", "data743.ts", "data744.ts", "data745.ts", "data746.ts", "data747.ts", "data748.ts", "data749.ts",
                "data750.ts", "data751.ts", "data752.ts", "data753.ts", "data754.ts", "data755.ts", "data756.ts", "data757.ts", "data758.ts", "data759.ts",
                "data760.ts", "data761.ts", "data762.ts", "data763.ts", "data764.ts", "data765.ts", "data766.ts", "data767.ts", "data768.ts", "data769.ts",
                "data770.ts", "data771.ts", "data772.ts", "data773.ts", "data774.ts", "data775.ts", "data776.ts", "data777.ts", "data778.ts", "data779.ts",
                "data780.ts", "data781.ts", "data782.ts", "data783.ts", "data784.ts", "data785.ts", "data786.ts", "data787.ts", "data788.ts", "data789.ts",
                "data790.ts", "data791.ts", "data792.ts", "data793.ts", "data794.ts", "data795.ts", "data796.ts", "data797.ts", "data798.ts", "data799.ts",
                "data800.ts", "data801.ts", "data802.ts", "data803.ts", "data804.ts", "data805.ts", "data806.ts", "data807.ts", "data808.ts", "data809.ts",
                "data810.ts", "data811.ts", "data812.ts", "data813.ts", "data814.ts", "data815.ts", "data816.ts", "data817.ts", "stream_0.m3u8" };
        totalFiles = fileUrls.length;
        showNotification("Download Started", "Downloading files...", 0);

        new Thread(() -> {
            try {
                for (String fileUrl : fileUrls) {
                    try {
                        downloadFile("https://vis-ipv-cda.epictv.spacelabs.pt/spider/stream_0/" + fileUrl);
                        downloadedFiles++;
                        updateNotification(downloadedFiles, totalFiles);
                    } catch (Exception e) {
                        Log.e(TAG, "Error downloading file: " + fileUrl, e);
                    }
                }
                DBHelper dbh = new DBHelper(this);
                //dbh.createOfflinePlayback("spider");
                showNotification("Download Complete", "All files downloaded.", 100);
            } catch (Exception e) {
                Log.e(TAG, "Download process failed", e);
                showNotification("Download Failed", "Error occurred during download.", 0);
            } finally {
                stopSelf();
            }
        }).start();

        return START_STICKY;
    }

    private void downloadFile(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }

        InputStream input = new BufferedInputStream(connection.getInputStream());
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        FileOutputStream output = openFileOutput(fileName, MODE_PRIVATE);

        byte[] data = new byte[4096];
        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();
    }

    private void showNotification(String title, String message, int progress) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false)
                .setOngoing(true)
                .build();

        startForeground(1, notification);
    }

    private void updateNotification(int downloadedFiles, int totalFiles) {
        int progress = (int) ((downloadedFiles / (float) totalFiles) * 100);
        showNotification("Homem Aranha", "Descarregado: " + progress + "%", progress);
    }

    private void createNotificationChannel() {
            CharSequence name = "Transferências";
            String description = "Canal de notificações para as transferências de conteudo";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}