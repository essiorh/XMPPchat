package com.example.ilia.xmppchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.ilia.xmppchat.service.listeners.IListenerService;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Created by ilia on 24.06.15.
 */
public class MyService extends Service implements IListenerService {
    public static final String LOG_TAG = "myLogs";
    private MyBinder binder = new MyBinder();
    private AbstractXMPPConnection conn2;
    private String mLogin;
    private String mPassword;
    private String mServer;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "MyService onBind");
        return new MyBinder();
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(LOG_TAG, "MyService onRebind");
    }

    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "MyService onUnbind");

        if (conn2.isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            // Send the packet (assume we have an XMPPConnection instance called "con").
            try {
                conn2.sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            conn2.disconnect();
        }
        return super.onUnbind(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        if (conn2.isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            // Send the packet (assume we have an XMPPConnection instance called "con").
            try {
                conn2.sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            conn2.disconnect();
        }
        Log.d(LOG_TAG, "MyService onDestroy");
    }

    public class MyBinder extends Binder {
        public IListenerService getService() {
            return MyService.this;
        }
    }

    @Override
    public void addConnection(String login, String password, String server) {
        // Create a connection to the jabber.org mServer.
        this.mLogin =login;
        this.mPassword =password;
        this.mServer =server;

// Create a connection to the jabber.org mServer on a specific port.


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
                    configBuilder.setUsernameAndPassword(mLogin, mPassword);
                    configBuilder.setServiceName(mServer);
                    configBuilder.setHost(mServer);

                    conn2 = new XMPPTCPConnection(configBuilder.build());
                    // Connect to the server
                    conn2.connect();
                    // Log into the server
                    conn2.login();

                    boolean autorize=conn2.isAuthenticated();
                    if (autorize) {
                        conn2.login();
                    }
                    // Create a new presence. Pass in false to indicate we're unavailable._
                    Presence presence = new Presence(Presence.Type.available);
                    presence.setStatus("Gone fishing");
                    // Send the packet (assume we have an XMPPConnection instance called "con").
                    conn2.sendStanza(presence);


                  /*  ChatMessageListener chatManagerListener = new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                            Toast.makeText(getApplicationContext(), message.getBody(), Toast.LENGTH_SHORT).show();

                        }
                    };

                            ChatManager chatmanager = ChatManager.getInstanceFor(conn2).getChatListeners(
                                    new ChatManagerListener() {
                                        @Override
                                        public void chatCreated(Chat chat, boolean createdLocally)
                                        {
                                            if (!createdLocally)
                                                chat.addMessageListener(new MyNewMessageListener());;
                                        }
                                    });
*/
                    ChatMessageListener chatMessageListener = new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message message) {
                        }
                    };
                    Chat chat = ChatManager.getInstanceFor(conn2).createChat("essiorh92@jabber.ru", chatMessageListener);


                    Message newMessage = new Message();
                    newMessage.setBody("Howdy!");

                    chat.sendMessage(newMessage);

                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
}
