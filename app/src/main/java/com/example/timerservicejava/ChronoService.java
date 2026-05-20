package com.example.timerservicejava;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChronoService extends Service {

    // Liaison Activity-Service
    private final IBinder liaisonEss = new ServiceEss();

    // Variables personnalisées
    private int tempsEss = 0;

    private boolean executionEss = false;

    private ScheduledExecutorService minuterieEss;

    private NotificationManager notificationEss;

    private static final int identifiantEss = 202;

    // Binder personnalisé
    public class ServiceEss extends Binder {

        public ChronoService recuperationEss() {

            return ChronoService.this;
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        notificationEss =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);

        canalEss();
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {

        String actionEss =
                intent != null
                        ? intent.getAction()
                        : null;

        // Arrêt du service
        if ("STOP_ESS".equals(actionEss)) {

            stopSelf();

            return START_NOT_STICKY;
        }

        // Démarrage du chrono
        if (!executionEss) {

            executionEss = true;

            startForeground(
                    identifiantEss,
                    creationEss()
            );

            lancementEss();
        }

        return START_STICKY;
    }

    // Lancement chrono
    private void lancementEss() {

        minuterieEss =
                Executors.newSingleThreadScheduledExecutor();

        minuterieEss.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {

                        tempsEss++;

                        actualisationEss();
                    }
                },
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    // Canal notification
    private void canalEss() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel canal =
                    new NotificationChannel(
                            "canal_ess",
                            "Service ESS",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            canal.setDescription(
                    "Notification Chronomètre ESS"
            );

            notificationEss.createNotificationChannel(canal);
        }
    }

    // Création notification
    private Notification creationEss() {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        "canal_ess"
                )

                        .setSmallIcon(
                                R.mipmap.ic_launcher
                        )

                        .setContentTitle(
                                "Chronomètre ESS actif"
                        )

                        .setContentText(
                                "Temps : "
                                        + formatEss(tempsEss)
                        )

                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )

                        .setOngoing(true)

                        .setAutoCancel(false);

        return builder.build();
    }

    // Mise à jour notification
    private void actualisationEss() {

        notificationEss.notify(
                identifiantEss,
                creationEss()
        );
    }

    // Retour temps Activity
    public int recupererTempsEss() {

        return tempsEss;
    }

    // Format temps
    private String formatEss(int valeurEss) {

        int minuteEss = valeurEss / 60;

        int secondeEss = valeurEss % 60;

        return String.format(
                "%02d:%02d",
                minuteEss,
                secondeEss
        );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return liaisonEss;
    }

    @Override
    public void onDestroy() {

        executionEss = false;

        if (minuterieEss != null) {

            minuterieEss.shutdown();
        }

        stopForeground(true);

        super.onDestroy();
    }
}