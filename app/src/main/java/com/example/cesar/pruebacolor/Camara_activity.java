package com.example.cesar.pruebacolor;


import android.Manifest;
import android.accounts.AbstractAccountAuthenticator;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import static android.R.attr.name;
import static android.R.attr.y;
import static java.lang.Thread.currentThread;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.inRange;
import static org.opencv.core.Core.sqrt;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Camara_activity extends Activity implements CvCameraViewListener2, OnTouchListener {
    // Para identificar los LOG, (un log se usa para imprimir mensajes)
    private static final String TAG = "opencv";
    // Puesto escucha del servidor CIM-NXT
    static final int SocketServerPORT = 8080;
    // Variable que extiende de JavaCameraView
    private MainActivity mOpenCvCameraView;
    private static final String TAGLOG = "firebase-db";
    TextView colorv, colora, colorr, colorn, dato, mapeos;
    int counter = 0;


    // Declara la conexión con CIM-NXT
    //ChatClientThread chatClientThread = null;
    // Intent para Iniciar y recibir la información de la actividad SamplesActivity
    private Intent intent;
    Bitmap bitmap;
    Mat mRgba;
    int i=0;
    int j=1;
    Bitmap bmp1;
    double cm1point3;

    ArrayList<String> usuarios;
    ArrayList<String> ips;

    Point Rhead,Rleg1,Rleg2;
    Point Ghead,Gleg1,Gleg2;
    Point Bhead,Bleg1,Bleg2;
    Point objetivo;

    Robot Rrobot,Brobot,Grobot;
    Coordenadas coordenadas;

    ArrayList<Integer> radios = new ArrayList<Integer>();

    private int x;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference root = database.getReference().getRoot().child("Coordenadas");
    DatabaseReference historial = database.getReference().getRoot().child("Historial");

    Date date = new Date();
    DateFormat fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    final String fecha = fechaHora.format(date);
    DatabaseReference nuevafecha = historial.child(fecha);

    //Para almacenar circulo "head" en Firebase

    DatabaseReference datorx = database.getReference().getRoot().child("Punto").child("Rojo").child("RobotRx");
    DatabaseReference datory = database.getReference().getRoot().child("Punto").child("Rojo").child("RobotRy");
    DatabaseReference datordx = database.getReference().getRoot().child("Punto").child("Rojo").child("RobotRdx");
    DatabaseReference datordy = database.getReference().getRoot().child("Punto").child("Rojo").child("RobotRdy");

    DatabaseReference datoax = database.getReference().getRoot().child("Punto").child("Azul").child("RobotAx");
    DatabaseReference datoay = database.getReference().getRoot().child("Punto").child("Azul").child("RobotAy");
    DatabaseReference datoadx = database.getReference().getRoot().child("Punto").child("Azul").child("RobotAdx");
    DatabaseReference datoady = database.getReference().getRoot().child("Punto").child("Azul").child("RobotAdy");

    DatabaseReference datovx = database.getReference().getRoot().child("Punto").child("Verde").child("RobotVx");
    DatabaseReference datovy = database.getReference().getRoot().child("Punto").child("Verde").child("RobotVy");
    DatabaseReference datovdx = database.getReference().getRoot().child("Punto").child("Verde").child("RobotVdx");
    DatabaseReference datovdy = database.getReference().getRoot().child("Punto").child("Verde").child("RobotVdy");


    DatabaseReference datonx = database.getReference().getRoot().child("Punto").child("Negro").child("RobotNx");
    DatabaseReference datony = database.getReference().getRoot().child("Punto").child("Negro").child("RobotNy");


//Colores
    DatabaseReference azul_color = database.getReference().getRoot().child("Punto").child("Azul");
    DatabaseReference verde_color = database.getReference().getRoot().child("Punto").child("Verde");
    DatabaseReference rojo_color = database.getReference().getRoot().child("Punto").child("Rojo");
    DatabaseReference negro_color = database.getReference().getRoot().child("Punto").child("Negro");






    static{ System.loadLibrary("opencv_java"); }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Camara_activity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Camara_activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Recibe los datos para la conexión con CIM-NXT
        intent = getIntent();

        usuarios = new ArrayList<String>();
        ips = new ArrayList<String>();

        Rrobot = new Robot();
        Grobot = new Robot();
        Brobot = new Robot();

        coordenadas = new Coordenadas();

        Log.i("Info","Conexiones creadas");
        // Permite que la actividad utilice la vista creada en layout/activity_main.xml
        setContentView(R.layout.activity_main);

        final Button IniciarCamara = (Button) findViewById(R.id.iniciar);
        final Button DetenerCamara = (Button) findViewById(R.id.detener);

        IniciarCamara.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                IniciarCamara.setVisibility(View.INVISIBLE);
                DetenerCamara.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Se ha iniciado la captura de información", Toast.LENGTH_SHORT).show();
                //FirebaseDatabase.getInstance().goOnline();
                runThread();
            }
        });

        DetenerCamara.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                IniciarCamara.setVisibility(View.VISIBLE);
                DetenerCamara.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Se ha detenido la captura de información", Toast.LENGTH_SHORT).show();
               // FirebaseDatabase.getInstance().goOffline();
                System.exit(0);
               // finish();


            }
        });

        mOpenCvCameraView = (MainActivity) findViewById(R.id.java_camera_view);
        //mOpenCvCameraView.setMaxFrameSize(640,480);
        mOpenCvCameraView.setMinimumHeight(720);
        mOpenCvCameraView.setMinimumWidth(1280);
        mOpenCvCameraView.setMaxFrameSize(1280,720);
        // Hace visible la cámara

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        // La camara empieza a escuchar
        mOpenCvCameraView.setCvCameraViewListener(this);
    }



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {

        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
        //historial.setValue(null);
        
    }



    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }


    // Función que se ejecuta cuando se toca la pantalla con la cámara activa
    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Capturar y guardar una fotografía
        /*Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/cim_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();*/
        // Incia ciclo de mapeo
       // Toast.makeText(this, "Se ha iniciado la captura de información", Toast.LENGTH_SHORT).show();
        //runThread();
        return false;
    }



    // Permite que se mapee constantemente con la cámara activa, sin necesidad de tomar fotografías
    public void runThread()
    {

        new Thread()
        {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    mapear();
                }
            };
            public void run() {
                while (i++ < 1000) {//Mapea hasta 1000 veces
                    try {

                        runOnUiThread(runnable);
                        Thread.sleep(2000);//Mapea cada 2 segundo

                    } catch (InterruptedException e) {
                        currentThread().interrupt();
                    }
                }
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //Función que mapea lo que se captura por la cámara en tiempo real
    public void mapear(){

        //bitmap = Bitmap.createBitmap(mOpenCvCameraView.getWidth()/4,mOpenCvCameraView.getHeight()/4, Bitmap.Config.ARGB_8888);
        try {

            Point Rojo, Azul,Verde;
            final HashMap<String, String> actual = new HashMap<>();
            final HashMap<String, Integer> rojo = new HashMap<>();
            final HashMap<String, Integer> verde = new HashMap<>();
            final HashMap<String, Integer> azul = new HashMap<>();
            final HashMap<String, Integer> negro = new HashMap<>();


            Date date = new Date();
            DateFormat hora = new SimpleDateFormat("HH:mm:ss");

            final String houra = hora.format(date);
            actual.put("Hora", houra);

            // Inicializa bitmap, formato que permite conocer los valores de cada pixel
            bitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);


            //Log.i("info",""+bitmap.getWidth()); //800
            //Log.i("info",""+bitmap.getHeight()); //480

            //Recolectar las cordenadas de 3 circulos del mismo color usando el radio como punto de ref

            double minDist = 10;

            // min and max radii (set these values as you desire)
            double dp = 1;
            int param1 = 100;
            int param2 = 25;

            int minRadius = 4;
            int maxRadius = 40;

            final DatabaseReference negro_bd = FirebaseDatabase.getInstance().getReference().child("Punto").child("Negro");

            // param1 = gradient value used to handle edge detection
            // param2 = Accumulator threshold entre mas pequeño el valor hay mas probabilidad que se detecten falsos positivos;

             //originalmente 20

            ArrayList<Point> centrosR = new ArrayList<Point>();
            ArrayList<Point> centrosG = new ArrayList<Point>();
            ArrayList<Point> centrosB = new ArrayList<Point>();
            ArrayList<Point> centrosN = new ArrayList<Point>();

            Mat hsv_image = new Mat(bitmap.getWidth(),bitmap.getHeight(),CvType.CV_8UC3);
            cvtColor(mRgba,hsv_image, Imgproc.COLOR_RGB2HSV);

            Mat red_mask_upper_range = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
            Mat red_mask_lower_range = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
            Mat blue_mask = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
            Mat green_mask = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);
            Mat black_mask = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC1);

            Mat result = new Mat(bitmap.getWidth(),bitmap.getHeight(),CvType.CV_8UC1);
            Mat circles = new Mat(bitmap.getWidth(),bitmap.getHeight(),CvType.CV_8UC3);

            inRange(hsv_image, new Scalar(0, 50, 50), new Scalar(10, 255, 255), red_mask_lower_range);
            inRange(hsv_image, new Scalar(160, 50, 50), new Scalar(179, 255, 255), red_mask_upper_range);
            //inRange(hsv_image, new Scalar(100, 50, 50), new Scalar(130, 255, 255), blue_mask);

            inRange(hsv_image, new Scalar(110, 150, 150), new Scalar(130, 255, 255), blue_mask);

            inRange(hsv_image, new Scalar(38, 50, 50), new Scalar(80, 255, 255),green_mask);
            //inRange(hsv_image, new Scalar(0, 0, 0), new Scalar(180, 255, 50),black_mask);

            inRange(hsv_image, new Scalar(0, 0, 0), new Scalar(180, 255, 30),black_mask);

            bmp1= Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
            Log.i("Size",bitmap.getHeight()+" "+bitmap.getWidth());

            //Seguimiento Rojo

            addWeighted(red_mask_lower_range, 1.0, red_mask_upper_range, 1.0, 0.0,result);
            Imgproc.erode(result, result, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            Imgproc.dilate(result, result, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            GaussianBlur(result, result, new Size(9, 9), 2, 2);
            Imgproc.HoughCircles(result,circles,Imgproc.CV_HOUGH_GRADIENT,dp,minDist,param1,param2,minRadius,maxRadius);
            centrosR = Circles(circles);

            //erode(result,result,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            //dilate(result,result,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));


            /*Utils.matToBitmap(result,bitmap);
            Save(bitmap);*/

            //Seguimiento Azul
            Imgproc.erode(blue_mask, blue_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            Imgproc.dilate(blue_mask, blue_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            GaussianBlur(blue_mask, result, new Size(9, 9), 2, 2);
            Imgproc.HoughCircles(result,circles,Imgproc.CV_HOUGH_GRADIENT,dp,minDist,param1,param2,minRadius,maxRadius);
            centrosB = Circles(circles);

            //Seguimiento Verde
            Imgproc.erode(green_mask, green_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            Imgproc.dilate(green_mask, green_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            GaussianBlur(green_mask, result, new Size(9, 9), 2, 2);
            Imgproc.HoughCircles(result,circles,Imgproc.CV_HOUGH_GRADIENT,dp,minDist,param1,param2,minRadius,maxRadius);
            centrosG = Circles(circles);

            /*Utils.matToBitmap(mRgba,bmp1);
            Save(bmp1);*/

            //Seguimiento Amarillo
            Imgproc.erode(black_mask, black_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            Imgproc.dilate(black_mask, black_mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
            GaussianBlur(black_mask, result, new Size(9, 9), 2, 2);
            Imgproc.HoughCircles(result,circles,Imgproc.CV_HOUGH_GRADIENT,dp,minDist,param1,param2,minRadius,maxRadius);
            centrosN = Circles(circles);

            /*Utils.matToBitmap(mRgba,bitmap);

            Save(bitmap);*/

            //Si encontramos toodo if()
            colorv = (TextView) findViewById(R.id.colorv);
            colora = (TextView) findViewById(R.id.colora);
            colorr = (TextView) findViewById(R.id.colorr);
            colorn = (TextView) findViewById(R.id.colorn);
            dato = (TextView) findViewById(R.id.dato);
            mapeos = (TextView) findViewById(R.id.mapeos);

             negro_bd.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String valory = dataSnapshot.child("RobotNx").getValue().toString();
                    String valorx = dataSnapshot.child("RobotNy").getValue().toString();
                    dato.setText("x:"+valorx+" y:"+valory);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAGLOG, "Error!", databaseError.toException());
                }
            });

            if(centrosR.size() > 0){
                //Log.i("Info","Circulos rojos encontrados:"+" "+centrosR.size());
                colorr.setTextColor(Color.RED);
                colorr.setText("colorR:"+centrosR.size());
                                    if(centrosR.size() == 3 && centrosN.size() == 1){
                                        ActualizarRojo(centrosR,centrosN, actual, rojo);
                                    }

            }else{
                colorr.setTextColor(Color.GRAY);
                colorr.setText("colorR");

                //datorx.setValue(0);
                //datory.setValue(0);
                //datordx.setValue(0);
                //datordy.setValue(0);

                rojo.put("RobotRx", 0);
                rojo.put("RobotRy", 0);
                rojo.put("RobotRdx", 0);
                rojo.put("RobotRdy", 0);
                rojo_color.setValue(rojo);

                //actual.put("RobotRx", "0");
                //actual.put("RobotRy", "0");
                //actual.put("RobotRdx", "0");
                //actual.put("RobotRdy", "0");
            }

            if(centrosG.size() > 0){
                //Log.i("Info","Circulos verdes encontrados:"+" "+centrosG.size());
                colorv.setTextColor(Color.GREEN);
                colorv.setText("colorV:"+centrosG.size());

                if(centrosG.size() == 3 && centrosN.size() == 1){
                    ActualizarVerde(centrosG,centrosN, actual, verde);
                }


            }else{
                colorv.setTextColor(Color.GRAY);
                colorv.setText("colorV");

                //datovx.setValue(0);
                //datovy.setValue(0);
                //datovdx.setValue(0);
                //datovdy.setValue(0);


                verde.put("RobotVx", 0);
                verde.put("RobotVy", 0);
                verde.put("RobotVdx", 0);
                verde.put("RobotVdy", 0);
                verde_color.setValue(verde);

                //actual.put("RobotVx", "0");
                //actual.put("RobotVy", "0");
                //actual.put("RobotVdx", "0");
                //actual.put("RobotVdy", "0");
            }


            if(centrosB.size() > 0){
                //Log.i("Info","Circulos azules encontrados:"+" "+centrosB.size());
                colora.setTextColor(Color.BLUE);
                colora.setText("colorA:"+centrosB.size());

                if(centrosB.size() == 3 && centrosN.size() == 1){
                    ActualizarAzul(centrosB,centrosN, actual, azul);
                }

            }else{
                colora.setTextColor(Color.GRAY);
                colora.setText("colorA");

                //datoax.setValue(0);
                //datoay.setValue(0);
                //datoadx.setValue(0);
                //datoady.setValue(0);


                azul.put("RobotAx", 0);
                azul.put("RobotAy", 0);
                azul.put("RobotAdx", 0);
                azul.put("RobotAdy", 0);
                azul_color.setValue(azul);

                //actual.put("RobotAx", "0");
                //actual.put("RobotAy", "0");
                //actual.put("RobotAdx", "0");
                //actual.put("RobotAdy", "0");

            }

            if(centrosN.size() > 0){
                //Log.i("Info","Circulos negros encontrados:"+" "+centrosN.size());
                colorn.setTextColor(Color.BLACK);
                colorn.setText("colorN:"+centrosN.size());
                            if(centrosN.size()==1){
                                actual.put("RobotNx", Double.toString(centrosN.get(0).x));
                                actual.put("RobotNy",Double.toString(centrosN.get(0).y));

                                negro.put("RobotNx", (int) centrosN.get(0).x);
                                negro.put("RobotNy", (int)(centrosN.get(0).y));
                                negro_color.setValue(negro);
                            }
            }else{
                colorn.setTextColor(Color.GRAY);
                colorn.setText("colorN");

                //datonx.setValue(0);
                //datony.setValue(0);

                negro.put("RobotNx", 0);
                negro.put("RobotNy", 0);
                negro_color.setValue(negro);

                //actual.put("RobotNx", "0");
                //actual.put("RobotNy", "0");
            }

            if(centrosR.size() == 3 && centrosG.size() == 3 && centrosB.size() == 3 && centrosN.size() == 1){
                cm1point3 = radios.get(9);
                Log.i("Info","Circulos encontrados");
              //  Instrucciones(centrosR,centrosG,centrosB,centrosN);
            }

            Log.i("Info","Circulos no encontrados");

            //actual.put("rsvp", "j");
            //actual.put("name", "pepe");
            //actual.put("event", "popa");
            nuevafecha.child(String.valueOf(houra)).setValue(actual);
            //nuevafecha.push().setValue(actual);
            mapeos.setText("mapeos:"+j);
            j++;

        }catch(Exception ex){
            Log.e("ERROR",ex.getMessage());
        }
    }

    void ActualizarRojo(ArrayList<Point> radiosR, ArrayList<Point> radiosY, HashMap<String, String> bd, HashMap<String, Integer> color){
        //Tomar en cuenta que 3 circulos del mismo color representan un robot en el espacio x,y

        Log.i("info","Intrucciones");
        objetivo = radiosY.get(0);
        double highest = 0;
        Rhead = radiosR.get(0);
        int distancia01,distancia02,distancia12;
        Point PuntoMedio = new Point(x,y);


        //armar Robot Rojo

        distancia01 = Distancia(radiosR.get(0),radiosR.get(1));
        distancia02 = Distancia(radiosR.get(0),radiosR.get(2));
        distancia12 = Distancia(radiosR.get(1),radiosR.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Rhead = radiosR.get(0);
            Rleg1 = radiosR.get(1);
            Rleg2 = radiosR.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Rhead = radiosR.get(1);
            Rleg1 = radiosR.get(0);
            Rleg2 = radiosR.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Rhead = radiosR.get(2);
            Rleg1 = radiosR.get(1);
            Rleg2 = radiosR.get(0);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        Log.i("Check","antes de armarlos robots");

        if (Rhead== null || Rleg1==null || Rleg2 == null){
            Log.i("Check","RNulo");
        }

        //datorx.setValue(Rhead.x);
        //datory.setValue(Rhead.y);

        //datordx.setValue(PuntoMedio.x);
        //datordy.setValue(PuntoMedio.y);

        //datordx.setValue(Rleg1.x);
        //datordy.setValue(Rleg1.y);

        //datonx.setValue(radiosY.get(0).x);
        //datony.setValue(radiosY.get(0).y);

        bd.put("RobotRx", Double.toString(Rhead.x));
        bd.put("RobotRy", Double.toString(Rhead.y));
        bd.put("RobotRdx", Double.toString(PuntoMedio.x));
        bd.put("RobotRdy", Double.toString(PuntoMedio.y));

        color.put("RobotRx", (int) Rhead.x);
        color.put("RobotRy", (int) Rhead.y);
        color.put("RobotRdx", (int) PuntoMedio.x);
        color.put("RobotRdy", (int) PuntoMedio.y);

        rojo_color.setValue(color);

        Log.i("Info","Rojo actualizado");

    }
    void ActualizarAzul(ArrayList<Point> radiosB, ArrayList<Point> radiosY, HashMap<String, String> bd, HashMap<String, Integer> color){

        //Tomar en cuenta que 3 circulos del mismo color representan un robot en el espacio x,y

        Log.i("info","Intrucciones");
        objetivo = radiosY.get(0);
        double highest = 0;
        double distancia01,distancia02,distancia12;
        Point PuntoMedio = new Point(x,y);

        //armar robot Azul

        distancia01 = Distancia(radiosB.get(0),radiosB.get(1));
        distancia02 = Distancia(radiosB.get(0),radiosB.get(2));
        distancia12 = Distancia(radiosB.get(1),radiosB.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Bhead = radiosB.get(0);
            Bleg1 = radiosB.get(1);
            Bleg2 = radiosB.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Bhead = radiosB.get(1);
            Bleg1 = radiosB.get(0);
            Bleg2 = radiosB.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Bhead = radiosB.get(2);
            Bleg1 = radiosB.get(1);
            Bleg2 = radiosB.get(0);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if (Bhead== null || Bleg1==null || Bleg2 == null){
            Log.i("Check","BNulo");
        }


        //datoax.setValue(Bhead.x);
        //datoay.setValue(Bhead.y);
        //datoadx.setValue(Bleg1.x);
        //datoady.setValue(Bleg1.y);

        //datonx.setValue(radiosY.get(0).x);
        //datony.setValue(radiosY.get(0).y);


        bd.put("RobotAx", Double.toString(Bhead.x));
        bd.put("RobotAy", Double.toString(Bhead.y));
        bd.put("RobotAdx", Double.toString(PuntoMedio.x));
        bd.put("RobotAdy", Double.toString(PuntoMedio.y));

        color.put("RobotAx", (int)(Bhead.x));
        color.put("RobotAy", (int)(Bhead.y));
        color.put("RobotAdx", (int)(PuntoMedio.x));
        color.put("RobotAdy", (int)(PuntoMedio.y));

        azul_color.setValue(color);

        Log.i("Info","Azul actualizado");

    }
    void ActualizarVerde(ArrayList<Point> radiosG, ArrayList<Point> radiosY, HashMap<String, String> bd, HashMap<String, Integer> color){

        //Tomar en cuenta que 3 circulos del mismo color representan un robot en el espacio x,y

        Log.i("info","Intrucciones");
        objetivo = radiosY.get(0);
        double highest = 0;
        double distancia01,distancia02,distancia12;
        Point PuntoMedio = new Point(x,y);

        //Armar Verde

        distancia01 = Distancia(radiosG.get(0),radiosG.get(1));
        distancia02 = Distancia(radiosG.get(0),radiosG.get(2));
        distancia12 = Distancia(radiosG.get(1),radiosG.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Ghead = radiosG.get(0);
            Gleg1 = radiosG.get(1);
            Gleg2 = radiosG.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Ghead = radiosG.get(1);
            Gleg1 = radiosG.get(0);
            Gleg2 = radiosG.get(2);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Ghead = radiosG.get(2);
            Gleg1 = radiosG.get(1);
            Gleg2 = radiosG.get(0);
            PuntoMedio = punto_medio(Rleg1, Rleg2);
        }

        if (Ghead== null || Gleg1==null || Gleg2 == null){
            Log.i("Check","GNulo");
        }

        //datovx.setValue(Ghead.x);
        //datovy.setValue(Ghead.y);
        //datovdx.setValue(Gleg1.x);
        //datovdy.setValue(Gleg1.y);

        //datonx.setValue(radiosY.get(0).x);
        //datony.setValue(radiosY.get(0).y);

        bd.put("RobotVx", Double.toString(Ghead.x));
        bd.put("RobotVy", Double.toString(Ghead.y));
        bd.put("RobotVdx", Double.toString(PuntoMedio.x));
        bd.put("RobotVdy", Double.toString(PuntoMedio.y));

        color.put("RobotVx", (int)(Ghead.x));
        color.put("RobotVy", (int)(Ghead.y));
        color.put("RobotVdx", (int)(PuntoMedio.x));
        color.put("RobotVdy",(int)(PuntoMedio.y));

        verde_color.setValue(color);

    }


    ArrayList<Point> Circles(Mat circulos){
        ArrayList<Point> coordenadas = new ArrayList<Point>();

        int numberOfCircles = (circulos.rows() == 0) ? 0 : circulos.cols();

        for (int i=0; i<numberOfCircles; i++) {


            //get the circle details, circleCoordinates[0, 1, 2] = (x,y,r)
            //(x,y) are the coordinates of the circle's center
            double[] circleCoordinates = circulos.get(0, i);


            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];

            Point center = new Point(x, y);
            coordenadas.add(center);

            int radius = (int) circleCoordinates[2];
            radios.add(radius);

            //Funcion de opencv dibuja en mat un circulo de color (0,255,0) o verde de grosor 4
            Core.circle(mRgba, center, radius, new Scalar(0, 255, 0), 4);
            //Log.i("Info","Circulo dibujado");

            //circle's center outline
            //Core.rectangle(mRgba, new Point(x - 5, y - 5), new Point(x + 5, y + 5), new Scalar(0, 128, 255), -1);
        }

        return coordenadas;
    }
    void Instrucciones(ArrayList<Point> radiosR,ArrayList<Point> radiosG,ArrayList<Point> radiosB,ArrayList<Point> radiosY){
        //Tomar en cuenta que 3 circulos del mismo color representan un robot en el espacio x,y

        Log.i("info","Intrucciones");
        objetivo = radiosY.get(0);
        double highest = 0;
        Rhead = radiosR.get(0);
        double distancia01,distancia02,distancia12;

        //armar Robot Rojo

        distancia01 = Distancia(radiosR.get(0),radiosR.get(1));
        distancia02 = Distancia(radiosR.get(0),radiosR.get(2));
        distancia12 = Distancia(radiosR.get(1),radiosR.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Rhead = radiosR.get(0);
            Rleg1 = radiosR.get(1);
            Rleg2 = radiosR.get(2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Rhead = radiosR.get(1);
            Rleg1 = radiosR.get(0);
            Rleg2 = radiosR.get(2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Rhead = radiosR.get(0);
            Rleg1 = radiosR.get(1);
            Rleg2 = radiosR.get(2);
        }

        //armar robot Azul

        distancia01 = Distancia(radiosB.get(0),radiosB.get(1));
        distancia02 = Distancia(radiosB.get(0),radiosB.get(2));
        distancia12 = Distancia(radiosB.get(1),radiosB.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Bhead = radiosB.get(0);
            Bleg1 = radiosB.get(1);
            Bleg2 = radiosB.get(2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Bhead = radiosB.get(1);
            Bleg1 = radiosB.get(0);
            Bleg2 = radiosB.get(2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Bhead = radiosB.get(0);
            Bleg1 = radiosB.get(1);
            Bleg2 = radiosB.get(2);
        }

        //Armar Verde

        distancia01 = Distancia(radiosG.get(0),radiosG.get(1));
        distancia02 = Distancia(radiosG.get(0),radiosG.get(2));
        distancia12 = Distancia(radiosG.get(1),radiosG.get(2));

        if((distancia01+distancia02)>(distancia01+distancia12) && (distancia01+distancia02)>(distancia02+distancia12)){
            Ghead = radiosG.get(0);
            Gleg1 = radiosG.get(1);
            Gleg2 = radiosG.get(2);
        }

        if((distancia01+distancia12)>(distancia12+distancia02) && (distancia01+distancia12)>(distancia02+distancia01)){
            Ghead = radiosG.get(1);
            Gleg1 = radiosG.get(0);
            Gleg2 = radiosG.get(2);
        }

        if((distancia12+distancia02)>(distancia12+distancia01) && (distancia12+distancia02)>(distancia01+distancia02)){
            Ghead = radiosG.get(0);
            Gleg1 = radiosG.get(1);
            Gleg2 = radiosG.get(2);
        }

        Log.i("Check","antes de armarlos robots");

        if (Rhead== null || Rleg1==null || Rleg2 == null){
            Log.i("Check","RNulo");
        }
        if (Bhead== null || Bleg1==null || Bleg2 == null){
            Log.i("Check","BNulo");
        }
        if (Ghead== null || Gleg1==null || Gleg2 == null){
            Log.i("Check","GNulo");
        }

        Rrobot.Asignacion(Rhead,Rleg1,Rleg2);
        Grobot.Asignacion(Ghead,Gleg1,Gleg2);
        Brobot.Asignacion(Bhead,Bleg1,Bleg2);

        coordenadas.UpdateR1((int)Rrobot.head.x,(int)Rrobot.head.y);
        coordenadas.UpdateR2((int)Grobot.head.x,(int)Grobot.head.y);
        coordenadas.UpdateR3((int)Brobot.head.x,(int)Brobot.head.y);

        //root.push().setValue(coordenadas);
        //root2.push().setValue(coordenadas);

        Log.i("Check","despues de armarlos robots");

        String uno = Integer.toString(Decidir(Rrobot,Grobot,Brobot));
        String dos = Integer.toString(Decidir(Grobot,Rrobot,Brobot));
        String tres = Integer.toString(Decidir(Brobot,Rrobot,Grobot));

        Log.i("info",uno);
        Log.i("info",dos);
        Log.i("info",tres);
        Log.i("info",""+radiosY.get(0).x+" "+radiosY.get(0).y);

        /*conexion.BuilMsj(uno);
        conexion.BuilMsj(dos);
        conexion.BuilMsj(tres);
        conexion.SendSynchroMsj();
*/
        //Tomar en cuenta las colisiones entre si y la rapidez con que se concretan las instrucciones
    }
    int Calibracion(Robot roboto){

        double distLO,distRO,distHO;
        double angle;
        double distBC_H,distBC_O;

        distLO = Distancia(roboto.legI,objetivo);
        distRO = Distancia(roboto.legD,objetivo);
        distHO = Distancia(roboto.head,objetivo);

        distBC_O = Distancia(roboto.botcenter,objetivo);
        distBC_H = Distancia(roboto.botcenter,roboto.head);

        if(distHO<distRO && distHO<distLO){
            angle = Math.toDegrees(Math.acos(((distBC_H*distBC_H) + (distBC_O*distBC_O) - (distHO*distHO))/(2*distBC_O*distBC_H)));
            if(angle<8){
                return 1; //forward
            }else{
                //rotate
                if(distLO<distRO){
                    return 3;
                }else{
                    return 4;
                }
            }
        }else{
            return 3; //rotate
        }

    }
    void Colision(Robot robot1,Robot robot2,Robot robot3){
        //si la parte delantera choca un auto
        //son 4 for pero siempre son 144 comparaciones por auto asi que no importa

        for(i=0;i<robot1.Body.size();i++){
            for (int j=0;j<3;j++){
                for(int k=0;k<robot2.Body.size();k++){
                    for (int l=0;l<3;l++){
                        if(Distancia(robot1.Body.get(i).get(j).punto,robot2.Body.get(k).get(l).punto)<Conversor(0.001)){
                            robot1.Body.get(i).get(j).colision = true;
                            if(robot1.Body.get(i).get(j).punto == robot1.topL){
                                robot1.colision = true;
                            }

                            if(robot1.Body.get(i).get(j).punto == robot1.topC){
                                robot1.colision = true;
                            }

                            if(robot1.Body.get(i).get(j).punto == robot1.topR){
                                robot1.colision = true;
                            }
                        }
                    }
                }
            }
        }

        for(i=0;i<robot1.Body.size();i++){
            for (int j=0;j<3;j++){
                for(int k=0;k<robot3.Body.size();k++){
                    for (int l=0;l<3;l++){
                        if(Distancia(robot1.Body.get(i).get(j).punto,robot3.Body.get(k).get(l).punto)<0.001){
                            robot1.Body.get(i).get(j).colision = true;
                            if(robot1.Body.get(i).get(j).punto == robot1.topL){
                                robot1.colision = true;
                            }

                            if(robot1.Body.get(i).get(j).punto == robot1.topC){
                                robot1.colision = true;
                            }

                            if(robot1.Body.get(i).get(j).punto == robot1.topR){
                                robot1.colision = true;
                            }
                        }
                    }
                }
            }
        }

    }
    int Corregir(Robot robot1,Robot robot2,Robot robot3){
        int leftTouched = 0,rightTouched = 0,topTouched = 0,bottomTouched = 0;
        double determinante;

        for(int i=0;i<3;i++){
            if(robot1.Adelante.get(i).colision){
                topTouched++;
            }
        }
        for(int i=0;i<3;i++){
            if(robot1.Derecha.get(i).colision){
                rightTouched++;
            }
        }
        for(int i=0;i<3;i++){
            if(robot1.Atras.get(i).colision){
                bottomTouched++;
            }
        }
        for(int i=0;i<3;i++){
            if(robot1.Izquierda.get(i).colision){
                leftTouched++;
            }
        }

        //Si toda la parte delantera esta por chocar con uno o mas robots, este debe girar hacia el lado
        //que este mas vacio
        if((robot1.Adelante.get(0).colision && !robot1.Adelante.get(1).colision && !robot1.Adelante.get(2).colision) ||
                (robot1.Adelante.get(0).colision && robot1.Adelante.get(1).colision && !robot1.Adelante.get(2).colision)){
            return 4; //girar a la derecha
        }
        if((robot1.Adelante.get(2).colision && !robot1.Adelante.get(1).colision && !robot1.Adelante.get(0).colision) ||
                (robot1.Adelante.get(2).colision && robot1.Adelante.get(1).colision && !robot1.Adelante.get(0).colision)){
            return 3;//girar a la izquierda
        }

        if(robot1.Adelante.get(1).colision && !robot1.Adelante.get(0).colision && !robot1.Adelante.get(2).colision){
            determinante = ((robot2.center.x-robot1.botcenter.x)*(robot1.head.y-robot1.botcenter.y)) - ((robot2.center.y-robot1.botcenter.y)*(robot1.head.x-robot1.botcenter.x));

            if(Distancia(robot1.center,robot2.center)>Distancia(robot1.center,robot3.center)){
                //girar en sentido del robot2
                if(determinante<0){
                    return 3;//girar a la izquierda
                }else{
                    return 4;//girar a la derecha
                }
            }
            if(Distancia(robot1.center,robot3.center)>Distancia(robot1.center,robot2.center)){
                //girar en sentido del robot3
                if(determinante<0){
                    return 4;//girar a la derecha
                }else{
                    return 3;//girar a la izquierda
                }
            }
        }

        if(!robot1.Adelante.get(0).colision && !robot1.Adelante.get(1).colision && !robot1.Adelante.get(2).colision && robot1.avanzar<4){
            robot1.avanzar++;
            return 1;
        }
        if(robot1.avanzar>3){
            robot1.colision = false;
            robot1.avanzar = 0;
        }

        if (Distancia(robot1.head,objetivo)<Conversor(5)){
            robot1.stopped = true;
        }

        return 0;

    }
    int Decidir(Robot robot1,Robot robot2,Robot robot3){
        Colision(robot1,robot2,robot3);
        if(!robot1.stopped){
            if(!robot1.colision){
                return Calibracion(robot1);
            }else{
                return Corregir(robot1,robot2,robot3);
            }
        }else {
            return 0;
        }
    }
    int Distancia(Point P1, Point P2){
        double c1,c2;
        int dist;

        c1 = Math.abs(P2.x-P1.x);
        c2 = Math.abs(P2.y-P1.y);

         dist = (int) Math.sqrt((c1*c1)+(c2*c2));

        return dist;
    }
    int Conversor(double medida){
        int pixeles;

        pixeles = (int)((medida*cm1point3)/0.013); //medida del radio del circulo de papel;

        return pixeles;
    }
    void Save(Bitmap bmp) {
        String path2 = Environment.getExternalStorageDirectory().getAbsoluteFile().toString();
        OutputStream fOut = null;
        File file = new File(path2, "Deteccion" + counter + ".png"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        counter++;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        try {
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    class Robot{
        Point head;
        Point leg1,legD;
        Point leg2,legI;
        Point center;
        Point botcenter;

        Point topL,topC,topR;
        Point leftT,leftC,leftB;
        Point rightT,rightC,rightB;
        Point bottomL,bottomC,bottomR;

        Point auxTopL,auxTopR;
        Point auxLeftC,auxRightC;

        ArrayList<Pair> Adelante;
        ArrayList<Pair> Izquierda;
        ArrayList<Pair> Derecha;
        ArrayList<Pair> Atras;

        ArrayList<ArrayList<Pair>> Body;

        boolean colision;
        boolean stopped;

        int avanzar;
        int distancia;

        Robot(){


        }

        void Asignacion(Point headd, Point legg1,Point legg2){
            //determinar derecha e izquierda
            //y si es igual?

            Log.i("Check","Ensamblar");

            head = headd;
            leg1 = legg1;
            leg2 = legg2;

            Log.i("Check","Ensamblar 1.25");

            double ybcenter = (double)((int)(Math.abs(leg1.y-leg2.y)/2));
            double xbcenter = (double)((int)(Math.abs(leg1.x-leg2.x)/2));

            Log.i("Check","Ensamblar 1.5");

            botcenter = new Point(xbcenter,ybcenter);

            double xcenter = (int) (Math.abs(head.x-botcenter.x)/2);
            double ycenter = (int) (Math.abs(head.y-botcenter.y)/2);

            center = new Point(xcenter,ycenter);

            Log.i("Check","Ensamblar 2");

            distancia = Conversor(0.03);
            avanzar = 0;

            colision = false;
            stopped = false;

            double d = ((leg1.x-botcenter.x)*(head.y-botcenter.y)) - ((leg1.y-botcenter.y)*(head.x-botcenter.x));

            Log.i("Check","Ensamblar 3");

            if(d<0){
                legI = leg1;
                legD = leg2;
            }else{
                legD = leg1;
                legI = leg2;
            }

            auxRightC = new Point(center.x+(legD.x-botcenter.x),center.y+(legD.y-botcenter.y));
            auxTopR = new Point(head.x+(auxRightC.x-center.x),head.y+(auxRightC.y-center.y));
            auxLeftC = new Point(center.x+(legI.x-botcenter.x),center.y+(legI.y-botcenter.y));
            auxTopL = new Point(head.x+(auxLeftC.x-center.x),head.y+(auxLeftC.y-center.y));

            topC = Normalizacion(center,head);
            bottomC = Normalizacion(center,botcenter);

            leftC = Normalizacion(center,auxLeftC);
            rightC = Normalizacion(center,auxRightC);

            topR = new Point((topC.x+(rightC.x-center.x))-((topC.x+(rightC.x-center.x))/5),(topC.y+(rightC.y-center.y)-(((topC.y+(rightC.y-center.y)))/5)));
            topL = new Point((topC.x+(leftC.x-center.x))-((topC.x+(leftC.x-center.x))/5),(topC.y+(leftC.y-center.y))-((topC.y+(leftC.y-center.y))/5));

            bottomL = new Point((bottomC.x+(leftC.x-center.x))-((bottomC.x+(leftC.x-center.x))/5),(bottomC.y+(leftC.y-center.y))-((bottomC.y+(leftC.y-center.y))/5));
            bottomR = new Point((bottomC.x+(rightC.x-center.x))-((bottomC.x+(rightC.x-center.x))/5),(bottomC.y+(rightC.y-center.y))-((bottomC.y+(rightC.y-center.y))/5));

            leftT = new Point((leftC.x+(topC.x-center.x))-((leftC.x+(topC.x-center.x))/5),(leftC.y+(topC.y-center.y))-((leftC.y+(topC.y-center.y))/5));
            leftB = new Point((leftC.x+(bottomC.x-center.x))-((leftC.x+(bottomC.x-center.x))/5),(leftC.y+(bottomC.y-center.y))-((leftC.y+(bottomC.y-center.y))/5));

            rightT = new Point((rightC.x+(topC.x-center.x))-((rightC.x+(topC.x-center.x))/5),(rightC.y+(topC.y-center.y))-((rightC.y+(topC.y-center.y))/5));
            rightB = new Point((rightC.x+(bottomC.x-center.x))-((rightC.x+(bottomC.x-center.x))/5),(rightC.y+(bottomC.y-center.y))-((rightC.y+(bottomC.y-center.y))/5));

            Adelante = new ArrayList<Pair>();
            Adelante.add(new Pair(topL,false));
            Adelante.add(new Pair(topC,false));
            Adelante.add(new Pair(topR,false));

            Izquierda = new ArrayList<Pair>();
            Izquierda.add(new Pair(leftT,false));
            Izquierda.add(new Pair(leftC,false));
            Izquierda.add(new Pair(leftB,false));

            Derecha = new ArrayList<Pair>();
            Derecha.add(new Pair(rightT,false));
            Derecha.add(new Pair(rightC,false));
            Derecha.add(new Pair(rightB,false));

            Atras = new ArrayList<Pair>();
            Atras.add(new Pair(bottomL,false));
            Atras.add(new Pair(bottomC,false));
            Atras.add(new Pair(bottomR,false));

            Body = new ArrayList<ArrayList<Pair>>();
            Body.add(Adelante);
            Body.add(Derecha);
            Body.add(Izquierda);
            Body.add(Atras);

            Log.i("Check","Fin esamblado");
        }

        Point Normalizacion(Point bot,Point top){
            Log.i("Check","Normalizar");
            Point result;
            double len = Math.sqrt(((top.x-bot.x)*(top.x-bot.x))+((top.y-bot.y)*(top.y-bot.y)));

            double dx = (top.x-bot.x)/len;
            double dy = (top.y-bot.y)/len;

            result = new Point(bot.x+(distancia*dx),bot.y+(distancia*dy));
            Log.i("Check","fin normalizacion");


            return result;
        }
    }
    class Pair{
        Point punto;
        boolean colision;

        Pair(Point punto,boolean colision){
            this.punto = punto;
            this.colision = colision;
        }
    }
    Point punto_medio(Point leg1, Point leg2){

        Point PM = new Point ( x, y);

        PM.x = ((int)(leg1.x+leg2.x)/2);
        PM.y = ((int)(leg1.y+leg2.y)/2);

        return PM;

}
}

