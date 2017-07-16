package pet.esens.reo_feed;

import android.*;
import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private String TAG = "MainActivty";
    final int MY_PERMISSION_REQUEST_CODE = 100;
    private BluetoothChatService mChatService = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceName = null;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private Button bt_sendData;

    /*
*   database로 데이터를 보내는 client가 하나 있어야하고,
*   데이터가 변경되었으면 bt를 핸들링하는 client가 또 하나 있어야한다.
* */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt_sendData = (Button)findViewById(R.id.bt_sendData);

        bt_sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("A");
            }
        });

        mOutStringBuffer = new StringBuffer("");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        int APIVersion = Build.VERSION.SDK_INT;
        if(APIVersion >= android.os.Build.VERSION_CODES.M){
            Log.e(TAG,"os Version code validates");
            if(checkPermission(Manifest.permission.BLUETOOTH)){
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.BLUETOOTH}, MY_PERMISSION_REQUEST_CODE);
            }
            if (checkPermission(Manifest.permission.BLUETOOTH_ADMIN)){
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.BLUETOOTH_ADMIN}, MY_PERMISSION_REQUEST_CODE);
            }
        }

        if (mBluetoothAdapter == null) {
            FragmentActivity activity = MainActivity.this;
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
                mDatabase = FirebaseDatabase.getInstance().getReference();
        String myToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"TOKEN : " + myToken);
        mDatabase.setValue("TRUE");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Log.e(TAG, "Value is :" + value);
                if(!value.equals("false")){
                    Log.e(TAG, "FEED TO LED! ");
                    connectDevice();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });


    }


    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            Log.e(TAG,"onStart()-> mChatService is null.. now call setupChat()");
            setupChat();
        }
    }

    private boolean checkPermission(String permission){
        int result=0;
        switch(permission) {
            case Manifest.permission.BLUETOOTH:
                result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);
                break;
            case Manifest.permission.BLUETOOTH_ADMIN:
                result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);
                break;
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void sendMessage(String message) {
        int a =  mChatService.getState();
        String log = String.valueOf(a);
        Log.e(TAG, log);
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "CAN'T_CONNECT", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {

            byte[] send = message.getBytes();
            mChatService.write(send);

            mOutStringBuffer.setLength(0);
            Log.e(TAG,"send Data :" + mOutStringBuffer);

            //mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

       // mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        /*mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });*/


        // Initialize the BluetoothChatService to perform bluetooth connections
        //// TODO: 2017-07-16 getActivity();
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        Log.e(TAG,"SetUp Chat END");
    }
    private void setStatus(int resId) {
        FragmentActivity activity = MainActivity.this;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
        Log.e(TAG,"setStatus1");
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = MainActivity.this;
        Log.e(TAG,"setStatus2");
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }
    private void connectDevice() {
        // Get the device MAC address
        String address = "98:D3:31:FB:72:60";
        //// TODO: 2017-07-16 상황에 따라서 secure를 true/false로 switch.
        boolean secure = true;
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        // Attempt to connect to the device
        if(device != null) mChatService.connect(device, secure);
        else Log.e(TAG,"connectDevice attemped");
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = MainActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.e(TAG,"writeMessage = " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.e(TAG,"READ_MESSAGE : " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}
