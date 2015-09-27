package com.danilov.aircontrol.activity;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.aircontrol.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ControlActivity extends BaseActivity {

    private static final String CONNECTION_THREAD = "CONNECTION_THREAD";

    private Handler connectionHandler = null;
    private HandlerThread connectionThread = null;

    private ImageButton reconnectButton;
    private ImageButton backButton;
    private ImageButton stateOkButton;
    private ImageButton stateReloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        connectionThread = new HandlerThread(CONNECTION_THREAD) {
            @Override
            protected void onLooperPrepared() {
                connectionHandler = new Handler(connectionThread.getLooper()); //explicitly because I want so
                init();
            }
        };
        connectionThread.start();
        reconnectButton = view(R.id.reconnect);
        backButton = view(R.id.back);
        stateReloading = view(R.id.status_reloading);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        stateOkButton = view(R.id.status_ok);
        reconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                startConnection();
            }
        });
    }

    private void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ControlListener listener = new ControlListener();
                view(R.id.btn1).setOnTouchListener(listener);
                view(R.id.btn2).setOnTouchListener(listener);
                view(R.id.btn3).setOnTouchListener(listener);
                view(R.id.btn4).setOnTouchListener(listener);
                view(R.id.btn5).setOnTouchListener(listener);
                view(R.id.btn6).setOnTouchListener(listener);
                view(R.id.btn7).setOnTouchListener(listener);
                view(R.id.btn8).setOnTouchListener(listener);
                view(R.id.btn9).setOnTouchListener(listener);
                view(R.id.btn10).setOnTouchListener(listener);
                view(R.id.btn11).setOnTouchListener(listener);
                view(R.id.btn12).setOnTouchListener(listener);
                System.out.println("connection is " + connection);
                if (connection == null) {
                    startConnection();
                }

            }
        });
    }

    private class ControlListener implements View.OnTouchListener {


        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            int id = v.getId();
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    sendMessage(getMessageById(id), true);
                    return true;
                case MotionEvent.ACTION_UP:
                    sendMessage(getMessageById(id), false);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        }

    }

    private Connection connection = null;

    private void sendMessage(final String message, final boolean isPressed) {
        connectionHandler.post(new Runnable() {
            @Override
            public void run() {
                if (connection != null) {
                    connection.write(message + (isPressed ? "0" : "1"));
                }
            }
        });
    }

    private synchronized void startConnection() {
        if (connectionHandler != null) {
            connection = new Connection();
            onConnecting();
            final Connection curConnection = connection;
            connectionHandler.post(new Runnable() {
                @Override
                public void run() {
                    Resources resources = getResources();
                    InetSocketAddress inetSocketAddress = new InetSocketAddress(getString(R.string.server_ip), resources.getInteger(R.integer.server_port));
                    Socket connectionSocket = new Socket();
                    try {
                        connectionSocket.connect(inetSocketAddress, resources.getInteger(R.integer.connection_timeout));
                    } catch (IOException e) {
                        curConnection.setWriter(null);
                        curConnection.setConnected(false);
                        onError();
                    }
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(connectionSocket.getOutputStream());
                        PrintWriter printWriter = new PrintWriter(writer, true);
                        curConnection.setWriter(printWriter);
                        curConnection.setConnected(true);
                        curConnection.onSocketConnected(connectionSocket);
                        onConnected();
                    } catch (IOException e) {
                        curConnection.setWriter(null);
                        curConnection.setConnected(false);
                        onError();
                    }
                }
            });
        }
    }

    private class Connection {

        private boolean connected = false;

        private PrintWriter writer = null;

        private Socket connectionSocket = null;

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(final boolean connected) {
            this.connected = connected;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public void setWriter(final PrintWriter writer) {
            this.writer = writer;
        }

        private void write(final String message) {
            if (writer != null) {
                writer.write(message);
                boolean hasError = writer.checkError();
                if (hasError) {
                    onError();
                }
            }
        }

        boolean shouldClose = false;

        public void onSocketConnected(final Socket socket) {
            connectionSocket = socket;
            if (shouldClose) {
                close();
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        try {
                            inputStream = connectionSocket.getInputStream();
                            InputStreamReader reader = new InputStreamReader(inputStream);
                            char[] buffer = new char[1024];
                            int read = -1;
                            while ((read = reader.read(buffer)) != -1) {
                                //doin nothin
                            }
                        } catch (IOException e) {
                            onError();
                        }
                    }
                }.start();
            }
        }

        private void close() {
            shouldClose = true;
            try {
                if (connectionSocket != null) {
                    write(getString(R.string.final_message));
                    connectionSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void onError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateOkButton.setVisibility(View.GONE);
                stateReloading.setVisibility(View.GONE);
                stopReloadAnim();
                reconnectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onConnecting() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateReloading.setVisibility(View.VISIBLE);
                startReloadAnim();
                stateOkButton.setVisibility(View.GONE);
                reconnectButton.setVisibility(View.GONE);
            }
        });

    }

    private void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateOkButton.setVisibility(View.VISIBLE);
                reconnectButton.setVisibility(View.GONE);
                stopReloadAnim();
                stateReloading.setVisibility(View.GONE);
            }
        });
    }

    private ObjectAnimator reloadingAnimator = null;

    private void startReloadAnim() {
        stateReloading.setRotationX(stateReloading.getWidth() / 2);
        stateReloading.setRotationY(stateReloading.getHeight() / 2);
        reloadingAnimator = ObjectAnimator.ofFloat(stateReloading, "rotation", 0f, 360f);
        reloadingAnimator.setRepeatMode(ObjectAnimator.RESTART);
        reloadingAnimator.setRepeatCount(1000);
        reloadingAnimator.setDuration(2000);
        reloadingAnimator.start();
    }

    private void stopReloadAnim() {
        if (reloadingAnimator != null) {
            reloadingAnimator.cancel();
        }
    }

    private void closeConnection() {
        final Connection conn = connection;
        connection = null;
        connectionHandler.post(new Runnable() {
            @Override
            public void run() {
                conn.close();
            }
        });
    }

    @Override
    protected void onPause() {
        closeConnection();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onresume: connection is " + connection);
        if (connection != null) {
            return;
        }
        startConnection();
    }

    private String getMessageById(final int id) {
        String message = "";
        switch (id) {
            case R.id.btn1:
                message = "0xAA1";
                break;
            case R.id.btn2:
                message = "0xAA0";
                break;
            case R.id.btn3:
                message = "0xCC1";
                break;
            case R.id.btn4:
                message = "0xCC0";
                break;
            case R.id.btn5:
                message = "0xAB1";
                break;
            case R.id.btn6:
                message = "0xAB0";
                break;
            case R.id.btn7:
                message = "0xCD1";
                break;
            case R.id.btn8:
                message = "0xCD0";
                break;
            case R.id.btn9:
                message = "0xBB1";
                break;
            case R.id.btn10:
                message = "0xBB0";
                break;
            case R.id.btn11:
                message = "0xDD1";
                break;
            case R.id.btn12:
                message = "0xDD0";
                break;
            default:
                break;
        }
        return message;
    }

}
