package com.example.timerservicejava;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Interface
    private TextView affichageEss;

    private Button lancementEss;

    private Button arretEss;

    // Service
    private ChronoService serviceEss;

    private boolean liaisonActiveEss = false;

    // Handler chrono
    private Handler minuterieInterface =
            new Handler(Looper.getMainLooper());

    // Connexion Service
    private final ServiceConnection connexionEss =
            new ServiceConnection() {

                @Override
                public void onServiceConnected(
                        ComponentName name,
                        IBinder service
                ) {

                    ChronoService.ServiceEss binder =
                            (ChronoService.ServiceEss)
                                    service;

                    serviceEss =
                            binder.recuperationEss();

                    liaisonActiveEss = true;

                    actualiserChronoEss();
                }

                @Override
                public void onServiceDisconnected(
                        ComponentName name
                ) {

                    liaisonActiveEss = false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Permission notifications Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    1
            );
        }

        affichageEss =
                findViewById(R.id.affichageEss);

        lancementEss =
                findViewById(R.id.lancementEss);

        arretEss =
                findViewById(R.id.arretEss);

        // Bouton démarrer
        lancementEss.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        demarrageEss();
                    }
                }
        );

        // Bouton arrêter
        arretEss.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        fermetureEss();
                    }
                }
        );
    }

    // Démarrage service
    private void demarrageEss() {

        Intent intentionEss =
                new Intent(
                        this,
                        ChronoService.class
                );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            startForegroundService(intentionEss);

        } else {

            startService(intentionEss);
        }

        bindService(
                intentionEss,
                connexionEss,
                Context.BIND_AUTO_CREATE
        );
    }

    // Arrêt service
    private void fermetureEss() {

        Intent intentionEss =
                new Intent(
                        this,
                        ChronoService.class
                );

        intentionEss.setAction("STOP_ESS");

        stopService(intentionEss);

        if (liaisonActiveEss) {

            unbindService(connexionEss);

            liaisonActiveEss = false;
        }

        affichageEss.setText("00:00");
    }

    // Mise à jour chrono interface
    private void actualiserChronoEss() {

        minuterieInterface.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        if (liaisonActiveEss
                                && serviceEss != null) {

                            int temps =
                                    serviceEss.recupererTempsEss();

                            int minutes = temps / 60;

                            int secondes = temps % 60;

                            affichageEss.setText(
                                    String.format(
                                            "%02d:%02d",
                                            minutes,
                                            secondes
                                    )
                            );
                        }

                        actualiserChronoEss();
                    }
                },
                1000
        );
    }

    @Override
    protected void onDestroy() {

        if (liaisonActiveEss) {

            unbindService(connexionEss);
        }

        super.onDestroy();
    }
}