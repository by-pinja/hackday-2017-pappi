package helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.util.Log;

import com.frost.mqtttutorial.MainActivity;
import com.frost.mqtttutorial.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by wildan on 3/19/2017.
 */

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;

    MediaPlayer mp;
    Context context;

    private String serverUri;

    private String clientId;
    private String subscriptionTopic;

    private String username;
    private String password;

    public MqttHelper(Context context) {
        mp = MediaPlayer.create(context, R.raw.ipanema);

        this.context = context;

        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);

            serverUri = properties.getProperty("server_uri");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            clientId = properties.getProperty("client_id");
            subscriptionTopic = properties.getProperty("subscription_topic");

        } catch (Resources.NotFoundException e) {
            Log.e("MQTTHelper", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("MQTTHelper", "Failed to open config file.");
        }

        final Context ctx = context.getApplicationContext();
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());

                PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                assert pm != null;
                PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                wakeLock.acquire(60000);

                // prepare intent which is triggered if the
                // notification is selected
                Intent intent = new Intent(ctx, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

                // build notification
                // the addAction re-use the same intent to keep the example short
                Notification n  = new Notification.Builder(ctx)
                        .setContentTitle("ANOMALY DETECTED!!!")
                        //.setContentText("Subject")
                        .setSmallIcon(R.drawable.ic_warning_black_24dp)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true).build();
                        //.addAction(R.drawable.icon, "Call", pIntent)
                        //.addAction(R.drawable.icon, "More", pIntent)
                        //.addAction(R.drawable.icon, "And more", pIntent).build();


                NotificationManager notificationManager =
                        (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(0, n);
                startPlayer();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void startPlayer() {
        try {
            mp.prepare();
        } catch (IOException ex) {
            Log.d("Mttq", ex.getMessage());
        }
        mp.start();
    }

    public void stopPlayer() {
        if (mp.isPlaying()) {
            mp.stop();
            mp.release();
        }
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribe fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
}
