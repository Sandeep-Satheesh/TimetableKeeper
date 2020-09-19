package com.example.timetablekeeper;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.timetablekeeper.models.SubjectObj;
import com.example.timetablekeeper.utils.CommonUtils;
import com.example.timetablekeeper.utils.SharedPref;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.transferwise.sequencelayout.SequenceStep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class JoinMeetFromPCActivity extends Activity {
    SubjectObj obj;
    ArrayList<SequenceStep> steps;
    Handler handler = new Handler();
    Button btnResendCmd;
    CountDownTimer t = null;
    Timer timer;
    TextView tvResendSuggestion;
    TimerTask refreshTask;
    SendDesktopMessageAysncTask sendDesktopMessageAysncTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcjoin);
        setFinishOnTouchOutside(false);
        setTitle("Join Class From PC");
        if (getIntent().getStringExtra("subjDetails") != null) {
            obj = new Gson().fromJson(getIntent().getStringExtra("subjDetails"), SubjectObj.class);
            timer = new Timer(true);
            initUI();
            checkPermissions();
        }
    }

    private void initUI() {
        steps = new ArrayList<>();
        btnResendCmd = findViewById(R.id.btn_resendcmd);
        tvResendSuggestion = findViewById(R.id.tv_resendcmd_suggestion);
        tvResendSuggestion.setVisibility(View.GONE);
        btnResendCmd.setVisibility(View.GONE);

        btnResendCmd.setOnClickListener(view -> {
            btnResendCmd.setEnabled(false);
            btnResendCmd.setVisibility(View.GONE);
            tvResendSuggestion.setVisibility(View.GONE);
            refreshTask.cancel();
            if (sendDesktopMessageAysncTask != null) sendDesktopMessageAysncTask.cancel(true);
            checkPermissions();
        });
        Collections.addAll(steps, findViewById(R.id.seqstep1), findViewById(R.id.seqstep2), findViewById(R.id.seqstep3), findViewById(R.id.seqstep4), findViewById(R.id.seqstep5));
    }

    private void checkPermissions() {
        if (!CommonUtils.wifiIsConnected(getApplicationContext()))
            new AlertDialog.Builder(this)
                    .setTitle("No network")
                    .setMessage("Cannot connect to desktop without a Wi-Fi connection! The operation will now abort.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).show();

        else activateSeqStep(2);
        //step 2:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            else {
                activateSeqStep(3);
                resolveTargetComputer();
            }
        }
        else {
            activateSeqStep(3);
            resolveTargetComputer();
        }
    }

    private void resolveTargetComputer() {
        String hostIP = SharedPref.getString(getApplicationContext(), "hostIP");
        int portNo = SharedPref.getInt(getApplicationContext(), "portNo");

        if (hostIP == null || portNo == -1)
            getHostDetailsAndSendMsg();

        else {
            AlertDialog d = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Use previous successful configuration?")
                    .setMessage("The message will be sent to the last successfully computer in 6 seconds...")
                    .setPositiveButton("Send Now", (dialogInterface, i) -> {
                        sendDesktopMessageAysncTask = new SendDesktopMessageAysncTask(hostIP, portNo, obj.getZoomLink(), resultCode -> {
                            if (resultCode == 0) {
                                activateSeqStep(5);
                                Toast.makeText(getApplicationContext(), "The message was sent successfully to the desktop client.", Toast.LENGTH_SHORT).show();
                                dialogInterface.cancel();
                                finish();
                            }
                            else {
                                displayMessageAndExitActivity("An error occurred in sending the message to the desktop client. Please make sure:\n1) The desktop client is running and is listening on a port.\n2) Your network is stable.\n\nIf you think this is a bug, please feel free to report to the developer at sandeepsatheesh.official@gmail.com!\nThe screen will now close.");
                            }
                        });
                        sendDesktopMessageAysncTask.execute();
                    }).setNeutralButton("Change Computer", (dialogInterface, i) -> {
                        t.cancel();
                        getHostDetailsAndSendMsg();
                    })
                    .setNegativeButton("Cancel Send", (dialogInterface, i) -> {
                        t.cancel();
                        finish();
                    }).create();
            d.show();
            t = new CountDownTimer(6000, 1000) {

                @Override
                public void onTick(long l) {
                    d.setMessage("The message will be sent to the last successfully computer in " + l/1000 + (l/1000 == 1 ? " second..." : " seconds..."));
                }

                @Override
                public void onFinish() {
                    d.cancel();
                    sendDesktopMessageAysncTask = new SendDesktopMessageAysncTask(hostIP, portNo, obj.getZoomLink(), resultCode -> {
                        if (resultCode == 0) {
                            activateSeqStep(5);
                            Toast.makeText(getApplicationContext(), "The message was sent successfully to the desktop client.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else {
                            displayMessageAndExitActivity("An error occurred in sending the message to the desktop client. Please make sure:\n1) The desktop client is running and is listening on a port.\n2) Your network is stable.\n\nIf you think this is a bug, please feel free to report to the developer at sandeepsatheesh.official@gmail.com!\nThe screen will now close.");
                        }
                    });
                    sendDesktopMessageAysncTask.execute();
                    refreshTask = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                btnResendCmd.setEnabled(true);
                                btnResendCmd.setVisibility(View.VISIBLE);
                                tvResendSuggestion.setVisibility(View.VISIBLE);
                            });
                        }
                    };
                    timer.schedule(refreshTask, 6000);
                }
            }.start();
        }
    }

    public void activateSeqStep(final int i) {
        handler.postDelayed(() -> {
            if (i > 1) {
                SequenceStep step = steps.get(i-2);
                //step.setActive(false);
                step.setSubtitle("Done!");
                step.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Medium);
            }
            SequenceStep step = steps.get(i-1);
            step.setActive(true);
            step.setSubtitle("In Progress...");
            step.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title);
        }, 500);
    }

    private void getHostDetailsAndSendMsg() {
        View v = getLayoutInflater().inflate(R.layout.activity_qrcodescanner, null);
        CodeScannerView scannerView = v.findViewById(R.id.codescannerview);
        CodeScanner scanner = new CodeScanner(this, scannerView);
        ArrayList<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        scanner.setFormats(formats);
        scanner.setDecodeCallback(result -> {
                scanner.stopPreview();
                String hostDetails = result.getText();
                if (!hostDetails.matches("[0-9]+.[0-9]+.[0-9]+.[0-9]:[0-9]+")) {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code detected! Please try again!", Toast.LENGTH_SHORT).show();
                    scanner.startPreview();
                    return;
                }
                String hostIP = hostDetails.substring(0, hostDetails.indexOf(':'));
                if (!Patterns.IP_ADDRESS.matcher(hostIP).matches()) {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code detected! Please try again!", Toast.LENGTH_SHORT).show();
                    scanner.startPreview();
                    return;
                }
                int portNo;
                try {
                    portNo = Integer.parseInt(hostDetails.substring(hostDetails.indexOf(':') + 1));
                    SharedPref.putInt(getApplicationContext(), "portNo", portNo);
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code detected! Please try again!", Toast.LENGTH_SHORT).show();
                    scanner.startPreview();
                    return;
                }
                SharedPref.putString(getApplicationContext(), "hostIP", hostIP);
                scanner.releaseResources();
                sendDesktopMessageAysncTask = new SendDesktopMessageAysncTask(hostIP, portNo, obj.getZoomLink(), resultCode -> {
                    if (resultCode == 0) {
                        activateSeqStep(5);
                        Toast.makeText(getApplicationContext(), "The message was sent successfully to the desktop client.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                        displayMessageAndExitActivity("An error occurred in sending the message to the desktop client. Please make sure:\n1) The desktop client is running and is listening on a port.\n2) Your network is stable.\n\nIf you think this is a bug, please feel free to report to the developer at sandeepsatheesh.official@gmail.com!\nThe screen will now close.");
                    }
                });
                sendDesktopMessageAysncTask.execute();
        });
        AlertDialog d = new AlertDialog.Builder(this, R.style.Theme_TransparentDialog)
                .setView(v)
                .setCancelable(false).create();
        d.setOnCancelListener(dialogInterface ->
                new AlertDialog.Builder(JoinMeetFromPCActivity.this)
                    .setTitle("Cancel sending message?")
                    .setMessage("You can come back here to send the message again, if you wish to exit.")
                    .setPositiveButton("Yes", (dialogInterface1, i) -> finish())
                    .setNegativeButton("No", null)
                    .show());
        d.show();
        scanner.startPreview();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) return;
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateSeqStep(3);
                resolveTargetComputer();
            }
        }
    }

    private void displayMessageAndExitActivity(String s) {

    }

    public class SendDesktopMessageAysncTask extends AsyncTask<Void, Void, Void> {

        String hostName;
        int portNumber, resultCode = 0;
        String message;
        OnConnectionClosedListener listener;

        public SendDesktopMessageAysncTask(String hostName, int portNumber, String message, OnConnectionClosedListener listener) {
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.message = message;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket socket = new Socket(hostName, portNumber);
                socket.setSoTimeout(5000);
                PrintWriter op = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String statusCode = in.readLine();

                if (statusCode != null && statusCode.equals("Hi")) {
                    Log.d("SendDesktopMessage", "Received msg: " + statusCode);

                    op.println(message);
                    handler.post(() -> activateSeqStep(4));
                    statusCode = in.readLine();
                    if (statusCode != null && statusCode.equals("bye")) {
                        op.println("bye");
                        handler.post(() -> activateSeqStep(5));
                        op.close();
                        in.close();
                        socket.close();
                        Log.d("SendDesktopMessage", "Connection terminated successfully.");
                    }
                }
            } catch (IOException e) {
                resultCode = -1;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listener.onConnectionClosed(resultCode);
            /*
            if (resultCode == 0) {
                Toast.makeText(getApplicationContext(), "The message was sent successfully to the desktop client!", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "There was an error sending the message to the desktop client!", Toast.LENGTH_SHORT).show();*/
        }
    }
    public interface OnConnectionClosedListener {
        void onConnectionClosed(int resultCode);
    }
}
