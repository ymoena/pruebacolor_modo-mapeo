package com.example.cesar.pruebacolor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;


public class SampleActivity extends Activity {

    // Declarando variable de tipo Intent, permite iniciar otra actividad desde ésta, y poder enviar información
    Intent sampleIntent;
    // Declarando Nombre y dirección IP, ingresados por el usuario para la conexión con CIM-NXT
    EditText editTextUserName, editTextAddress;
    Button iniciar;

    ArrayList<String> usuarios;
    ArrayList<String> address;
    String username;
    String IP;
    String TAG = "tag";

    //Función inicial que crea la actividad
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Permite que la actividad utilice la vista creada en layout/activity_samples.xml
        setContentView(R.layout.activity_sample);

        isWriteStoragePermissionGranted();
        isCameraPermissionGranted();
        isInternetPermissionGranted();

        // Inicializa el texto editable, en donde el usuario ingresará nombre y direciion IP
        iniciar = (Button) findViewById(R.id.button);

        usuarios = new ArrayList<String>();
        address = new ArrayList<String>();

        iniciar.setText("Siguiente");

        sampleIntent = new Intent(SampleActivity.this, Camara_activity.class);
        sampleIntent.putStringArrayListExtra("USERS",usuarios);
        sampleIntent.putStringArrayListExtra("ADRESSES",address);

        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(sampleIntent);

            }
        });

        /* iniciar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if(usuarios.size()<3) {
                    Log.i("opencv","dentro");
                    username = editTextUserName.getText().toString();
                    IP = editTextAddress.getText().toString();

                    if (username.equals("") || IP.equals("")) {
                        Toast.makeText(SampleActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                    }else{
                        usuarios.add(username);
                        address.add(IP); //cesar 10.3.11.166
                                         //Diego 10.3.4.41
                                         //Oscar 10.3.9.255
                    }

                    if(usuarios.size()>2){
                        iniciar.setText("Inicar Camara");
                    }
                }else{
                    editTextUserName.setVisibility(View.INVISIBLE);
                    editTextAddress.setVisibility(View.INVISIBLE);
                    sampleIntent = new Intent(SampleActivity.this, Camara_activity.class);
                    sampleIntent.putStringArrayListExtra("USERS",usuarios);
                    sampleIntent.putStringArrayListExtra("ADRESSES",address);
                    if (usuarios == null){
                        Log.i("opencv","Nullo");
                    }else{
                        Log.i("opencv","Not null");
                    }
                    startActivity(sampleIntent);
                }}
        });*/

    }

    // Función que válida los datos ingresados, e inicializa la actividad Camara_activity  cim.visionglobal.MainActivity
    public void tutorial1(View v) {
        String textUserName = editTextUserName.getText().toString();
        if (textUserName.equals("")) {
            Toast.makeText(this, "Ingrese nombre",Toast.LENGTH_LONG).show();
            return;
        }

        String textAddress = editTextAddress.getText().toString();
        if (textAddress.equals("")) {
            Toast.makeText(this, "Ingrese direccion",Toast.LENGTH_LONG).show();
            return;
        }
        // Inicializa la variable de tipo Intent
        sampleIntent = new Intent(this, Camara_activity.class);
        String Name = editTextUserName.getText().toString();
        String Address = editTextAddress.getText().toString();
        // Envia a la actividad Camara_activity, el nombre y la dirección IP
        sampleIntent.putExtra("Name", Name);
        sampleIntent.putExtra("Address", Address);
        // Inicializa la actividad Camara_activity
        startActivity(sampleIntent);
    }

    public  boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public  boolean isInternetPermissionGranted() {
        Log.v(TAG,"Inside");
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.INTERNET)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
