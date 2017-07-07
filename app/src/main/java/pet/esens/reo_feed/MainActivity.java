package pet.esens.reo_feed;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    //FEED TO LED
                    Log.e(TAG, "FEED TO LED! ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read value.", databaseError.toException());
            }
        });
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
}
