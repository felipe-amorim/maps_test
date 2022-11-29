package com.example.maps_test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CadastroActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, LocationListener {
    EditText editTextNome, editTextObs;
    Button botao, cameraButton, excluirFoto1, excluirFoto2, excluirFoto3, excluirFoto4;
    SQLiteDatabase bancoDados;
    String[] riscos = { "Queda", "Rede elétrica", "Danificação de edificações", "Poda agendada", "Outros"};
    RadioButton CB, CM, CA;
    CheckBox CBCaminhaoGuindaste, CBTrator, CBBritadeira, CBOutro;
    String risco = "", imageFromCamera1 = "", imageFromCamera2 = "", imageFromCamera3 = "", imageFromCamera4 = "", imageFromCameraAll = "", location = "";
    Switch urgente, multipleLocation;
    private ImageView imgCapture1, imgCapture2, imgCapture3, imgCapture4;
    private static final int Image_Capture_Code = 1;

    int previousButtonHeight = 0;
    int previousWidth = 0;
    int previousHeight = 0;
    int pontoPodaIndex = 0;

    private GoogleMap mMap;
    ScrollView scroll;
    double lat;
    double lon;
    private boolean mapZoom = false;

    LocationManager locationManager;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextObs = (EditText) findViewById(R.id.editTextObs);
        botao = (Button) findViewById(R.id.buttonAlterar);
        CB = (RadioButton) findViewById(R.id.radioCB);
        CM = (RadioButton) findViewById(R.id.radioCM);
        CA = (RadioButton) findViewById(R.id.radioCA);
        CBCaminhaoGuindaste = (CheckBox) findViewById(R.id.CBCaminhaoGuindaste);
        CBTrator = (CheckBox) findViewById(R.id.CBTrator);
        CBBritadeira = (CheckBox) findViewById(R.id.CBBritadeira);
        CBOutro = (CheckBox) findViewById(R.id.CBOutro);
        CBOutro.setOnClickListener((view) -> {setCBOutro();});
        urgente = (Switch) findViewById(R.id.urgente);
        multipleLocation = (Switch) findViewById(R.id.multipleL);
        cameraButton = (Button) findViewById(R.id.buttonFotos);
        imgCapture1 = (ImageView) findViewById(R.id.foto1);
        previousHeight = imgCapture1.getLayoutParams().height;;
        previousWidth = imgCapture1.getLayoutParams().width;
        imgCapture1.getLayoutParams().width = 0;
        imgCapture1.getLayoutParams().height = 0;
        imgCapture2 = (ImageView) findViewById(R.id.foto2);
        imgCapture2.getLayoutParams().width = 0;
        imgCapture2.getLayoutParams().height = 0;
        imgCapture3 = (ImageView) findViewById(R.id.foto3);
        imgCapture3.getLayoutParams().width = 0;
        imgCapture3.getLayoutParams().height = 0;
        imgCapture4 = (ImageView) findViewById(R.id.foto4);
        imgCapture4.getLayoutParams().width = 0;
        imgCapture4.getLayoutParams().height = 0;
        excluirFoto1 = (Button) findViewById(R.id.excluirFoto1);
        previousButtonHeight = excluirFoto1.getLayoutParams().height;
        excluirFoto1.setVisibility(View.INVISIBLE);
        excluirFoto1.setOnClickListener((view -> {removeImage(excluirFoto1, imgCapture1, 1);}));
        excluirFoto2 = (Button) findViewById(R.id.excluirFoto2);
        excluirFoto2.setVisibility(View.INVISIBLE);
        excluirFoto2.setOnClickListener((view -> {removeImage(excluirFoto2, imgCapture2, 2);}));
        excluirFoto3 = (Button) findViewById(R.id.excluirFoto3);
        excluirFoto3.setVisibility(View.INVISIBLE);
        excluirFoto3.setOnClickListener((view -> {removeImage(excluirFoto3, imgCapture3, 3);}));
        excluirFoto4 = (Button) findViewById(R.id.excluirFoto4);
        excluirFoto4.setVisibility(View.INVISIBLE);
        excluirFoto4.setOnClickListener((view -> {removeImage(excluirFoto4, imgCapture4, 4);}));
        excluirFoto1.getLayoutParams().height = 0;
        excluirFoto2.getLayoutParams().height = 0;
        excluirFoto3.getLayoutParams().height = 0;
        excluirFoto4.getLayoutParams().height = 0;
        scroll = (ScrollView) findViewById(R.id.scroll);

        createSpinner();

        botao.setOnClickListener((view -> {cadastrar();}));

        cameraButton.setOnClickListener((view -> {openCamera();}));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMapLongClickListener((this::addNewMarket));
        //mock
        double lat = -22.703859;
        double lon = -47.654072;
        //if(!mapZoom){
        //    updateMap(new LatLng(lat, lon));
        //}
        location = lat +":"+lon;
        //--
        if(location.length()!=0){
            updateMap(new LatLng(lat, lon));
        }
    }

    protected void loadMarkers(){
        String[] locations = location.split(";");
        for (String loc:locations) {
            String[] pieces = loc.split(":");
            double lat = Double.parseDouble(pieces[0]);
            double lon = Double.parseDouble(pieces[1]);
            updateMap(new LatLng(lat, lon));
        }
    }

    protected void updateMap(LatLng latLng){
        if(!multipleLocation.isChecked()){
            mMap.clear();
            pontoPodaIndex = 1;
        }else{
            pontoPodaIndex++;
        }
        if(!mapZoom){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ponto "+pontoPodaIndex));
            mapZoom = true;
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ponto "+pontoPodaIndex));
        }
    }

    protected void addNewMarket(LatLng latLng){
        if(!multipleLocation.isChecked()){
            location = latLng.latitude +":"+latLng.longitude;
        }else{
            if(location.length()>0){
                location += ";";
            }
            location += latLng.latitude +":"+latLng.longitude;
        }
        loadMarkers();
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLatitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Latitude disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Latitude enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("Latitude status");
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            scroll.requestDisallowInterceptTouchEvent(true);

        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            scroll.requestDisallowInterceptTouchEvent(true);

        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            scroll.requestDisallowInterceptTouchEvent(true);

        }
    }

    private void openCamera(){
        Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cInt,Image_Capture_Code);
    }

    protected void removeImage(Button excluirButton, ImageView imageView, int fotoIndex){
        excluirButton.setVisibility(View.INVISIBLE);
        imageView.setImageBitmap(null);
        imageView.getLayoutParams().width = 0;
        imageView.getLayoutParams().height = 0;
        switch (fotoIndex){
            case 1:
                imageFromCamera1 = "";
                break;
            case 2:
                imageFromCamera2 = "";
                break;
            case 3:
                imageFromCamera3 = "";
                break;
            case 4:
                imageFromCamera4 = "";
                break;
        }
        cameraButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadImages((Bitmap) data.getExtras().get("data"));
        }
    }

    public void loadImages(Bitmap imageBp){
        int fotoIndex = 0;
        if(imageFromCamera1.length()==0){
            fotoIndex = 1;
        }else
        if(imageFromCamera2.length()==0){
            fotoIndex = 2;
        }else
        if(imageFromCamera3.length()==0){
            fotoIndex = 3;
        }else
        if(imageFromCamera4.length()==0){
            fotoIndex = 4;
        }

        Button excluirButton = null;
        ImageView imageView = null;
        switch (fotoIndex){
            case 1:
                excluirButton = excluirFoto1;
                imageView = imgCapture1;
                imageFromCamera1 = ImageUtil.convert(imageBp);
                excluirFoto1.setVisibility(View.VISIBLE);
                break;
            case 2:
                excluirButton = excluirFoto2;
                imageView = imgCapture2;
                imageFromCamera2 = ImageUtil.convert(imageBp);
                excluirFoto2.setVisibility(View.VISIBLE);
                break;
            case 3:
                excluirButton = excluirFoto3;
                imageView = imgCapture3;
                imageFromCamera3 = ImageUtil.convert(imageBp);
                excluirFoto3.setVisibility(View.VISIBLE);
                break;
            case 4:
                excluirButton = excluirFoto4;
                imageView = imgCapture4;
                imageFromCamera4 = ImageUtil.convert(imageBp);
                excluirFoto4.setVisibility(View.VISIBLE);
                cameraButton.setVisibility(View.INVISIBLE);
                break;
        }
        if(excluirButton!=null && imageView!=null){
            excluirButton.setVisibility(View.VISIBLE);
            excluirButton.getLayoutParams().height = previousButtonHeight;
            imageView.getLayoutParams().width = previousWidth;
            imageView.getLayoutParams().height = previousHeight;
            imageView.setImageBitmap(imageBp);
        }
    }

    public void setCBOutro(){
        Toast.makeText(getApplicationContext(), "Descreva o equipamento no campo observações", Toast.LENGTH_LONG).show();
    }

    private void createSpinner(){

        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item, riscos);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
    }

    public void cadastrar(){
        if(!TextUtils.isEmpty(editTextNome.getText().toString())){
            try{
                String complexidade = "";
                if(CB.isChecked()){
                    complexidade = "3";
                }
                if(CM.isChecked()){
                    complexidade = "2";
                }
                if(CA.isChecked()){
                    complexidade = "1";
                }
                String equipamentos = "";
                if(CBCaminhaoGuindaste.isChecked()){
                    equipamentos = equipamentos + "CaminhaoGuindaste;";
                }
                if(CBTrator.isChecked()){
                    equipamentos = equipamentos + "Trator;";
                }
                if(CBBritadeira.isChecked()){
                    equipamentos = equipamentos + "Britadeira;";
                }
                if(CBOutro.isChecked()){
                    equipamentos = equipamentos + "Outro;";
                }

                String urg = "false";
                if(urgente.isChecked()){
                    urg = "true";
                }

                bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
                String dataFormatted = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String sql = "INSERT INTO coisa (nome, obs, complexidade, equipamentosEspeciais, risco, urgencia, data, foto, gps, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                //String sql = "INSERT INTO coisa (nome, obs) VALUES (?, ?)";
                SQLiteStatement stmt = bancoDados.compileStatement(sql);
                stmt.bindString(1,editTextNome.getText().toString());
                stmt.bindString(2,editTextObs.getText().toString());
                stmt.bindString(3,complexidade);
                stmt.bindString(4,equipamentos);
                stmt.bindString(5,risco);
                stmt.bindString(6,urg);
                stmt.bindString(7,dataFormatted);
                imageFromCameraAll = "";
                if(imageFromCamera1.length()>0){
                    imageFromCameraAll += imageFromCamera1;
                }
                if(imageFromCamera2.length()>0){
                    if(imageFromCameraAll.length()>0){
                        imageFromCameraAll += ";";
                    }
                    imageFromCameraAll += imageFromCamera2;
                }
                if(imageFromCamera3.length()>0){
                    if(imageFromCameraAll.length()>0){
                        imageFromCameraAll += ";";
                    }
                    imageFromCameraAll += imageFromCamera3;
                }
                if(imageFromCamera4.length()>0){
                    if(imageFromCameraAll.length()>0){
                        imageFromCameraAll += ";";
                    }
                    imageFromCameraAll += imageFromCamera4;
                }

                stmt.bindString(8,imageFromCameraAll);
                stmt.bindString(9,location);
                stmt.bindString(10,"active");
                stmt.executeInsert();
                bancoDados.close();
                finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        risco = riscos[position];
        if(riscos[position].equals("Outros")){
            Toast.makeText(getApplicationContext(), "Descreva o risco no campo observações", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}