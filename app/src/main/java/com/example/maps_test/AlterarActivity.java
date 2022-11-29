package com.example.maps_test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.util.Arrays;

public class AlterarActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, LocationListener {
    private SQLiteDatabase bancoDados;
    public EditText editTextNome, editTextObs;
    Button cameraButton, buttonAlterar, buttonConcluir, excluirFoto1, excluirFoto2, excluirFoto3, excluirFoto4;
    String[] riscos = { "Queda", "Rede elétrica", "Danificação de edificações", "Poda agendada", "Outros"};
    RadioButton CB, CM, CA;
    CheckBox CBCaminhaoGuindaste, CBTrator, CBBritadeira, CBOutro;
    String risco = "", imageFromCamera1 = "", imageFromCamera2 = "", imageFromCamera3 = "", imageFromCamera4 = "", imageFromCameraAll = "", location = "";
    Spinner riscoSpinner;
    Switch urgente, multipleLocation;
    public Integer id;
    private ImageView imgCapture1, imgCapture2, imgCapture3, imgCapture4;
    private static final int Image_Capture_Code = 1;

    int previousButtonHeight = 0;
    int previousHeight = 0;
    int previousWidth = 0;
    int pontoPodaIndex = 0;

    private boolean mapZoom = false, completed = false, mapsEnabled = true;

    ScrollView scroll;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    LocationManager locationManager;

    double lat;
    double lon;

    @SuppressLint({"ClickableViewAccessibility", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar);

        buttonAlterar = (Button) findViewById(R.id.buttonAlterar);
        buttonConcluir = (Button) findViewById(R.id.buttonConcluir);
        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextObs = (EditText) findViewById(R.id.editTextObs);
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
        riscoSpinner = (Spinner) findViewById(R.id.spinner);
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

        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);

        createSpinner();
        carregarDados();

        buttonAlterar.setOnClickListener((view -> {alterar();}));
        buttonConcluir.setOnClickListener((view -> {setCompleted();}));

        cameraButton.setOnClickListener((view -> {openCamera();}));
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);

    }

    public void setCompleted(){
        completed = true;
        alterar();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMapLongClickListener((this::addNewMarket));
        if(location.length()>0){
            loadMarkers();
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
        if(mapsEnabled){
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

    private void createSpinner(){

        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item, riscos);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);
    }

    public void setCBOutro(){
        Toast.makeText(getApplicationContext(), "Descreva o equipamento no campo observações", Toast.LENGTH_LONG).show();
    }

    public void carregarDados(){
        try {
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            Cursor cursor = bancoDados.rawQuery("SELECT id, nome, obs, complexidade, equipamentosEspeciais, risco, urgencia, foto, gps, status FROM coisa WHERE id = " + id.toString(), null);
            cursor.moveToFirst();
            editTextNome.setText(cursor.getString(1));
            editTextObs.setText(cursor.getString(2));
            String comp = cursor.getString(3);
            if(comp.equals("3")){
                CB.toggle();
            }
            if(comp.equals("2")){
                CM.toggle();
            }
            if(comp.equals("1")){
                CA.toggle();
            }
            String[] equip = cursor.getString(4).split(";");
            for (String e: equip) {
                if(e.equals("CaminhaoGuindaste")){
                    CBCaminhaoGuindaste.toggle();
                }
                if(e.equals("Trator")){
                    CBTrator.toggle();
                }
                if(e.equals("Britadeira")){
                    CBBritadeira.toggle();
                }
                if(e.equals("Outro")){
                    CBOutro.toggle();
                }
            }
            riscoSpinner.setSelection(Arrays.asList(riscos).indexOf(cursor.getString(5)));
            if(cursor.getString(6).equals("true")){
                urgente.toggle();
            }

            imageFromCameraAll = cursor.getString(7);
            System.out.println("aaaaah "+imageFromCameraAll);
            for (String image:imageFromCameraAll.split(";")) {
                //if(!image.equals("")){
                System.out.println("aquiiiii");
                if(image.length()>0){
                    System.out.println(image);
                    loadImages(ImageUtil.convert(image));
                }
                //}
            }

            location = cursor.getString(8);
            if(location.contains(";")){
                multipleLocation.toggle();
            }
            if(mMap!=null){
                loadMarkers();
            }

            if(cursor.getString(9).equals("completed")){
                System.out.println("completed");
                editTextNome.setEnabled(false);
                editTextObs.setEnabled(false);
                CA.setEnabled(false);
                CM.setEnabled(false);
                CB.setEnabled(false);
                CBCaminhaoGuindaste.setEnabled(false);
                CBTrator.setEnabled(false);
                CBBritadeira.setEnabled(false);
                CBOutro.setEnabled(false);
                riscoSpinner.setEnabled(false);
                urgente.setEnabled(false);
                imgCapture1.setEnabled(false);
                imgCapture2.setEnabled(false);
                imgCapture3.setEnabled(false);
                imgCapture4.setEnabled(false);
                excluirFoto1.setEnabled(false);
                mapsEnabled = false;
                multipleLocation.setEnabled(false);

                cameraButton.setEnabled(false);
                buttonConcluir.setEnabled(false);
                buttonAlterar.setEnabled(false);
            }



        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void alterar(){
        String valueNome = editTextNome.getText().toString();
        String valueObs = editTextObs.getText().toString();
        String valueComplexidade = "";
        if(CB.isChecked()){
            valueComplexidade = "3";
        }
        if(CM.isChecked()){
            valueComplexidade = "2";
        }
        if(CA.isChecked()){
            valueComplexidade = "1";
        }
        String valueEquipamentos = "";
        if(CBCaminhaoGuindaste.isChecked()){
            valueEquipamentos = valueEquipamentos + "CaminhaoGuindaste;";
        }
        if(CBTrator.isChecked()){
            valueEquipamentos = valueEquipamentos + "Trator;";
        }
        if(CBBritadeira.isChecked()){
            valueEquipamentos = valueEquipamentos + "Britadeira;";
        }
        if(CBOutro.isChecked()){
            valueEquipamentos = valueEquipamentos + "Outro;";
        }
        String valueUrgente = "false";
        if(urgente.isChecked()){
            valueUrgente = "true";
        }
        try{
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            //, obs, complexidade, equipamentosEspeciais, risco, urgencia
            String sql = "UPDATE coisa SET nome=?, obs=?, complexidade=?, equipamentosEspeciais=?, risco=?, urgencia=?, foto=?, gps=?, status=? WHERE id=?";
            SQLiteStatement stmt = bancoDados.compileStatement(sql);
            stmt.bindString(1,valueNome);
            stmt.bindString(2,valueObs);
            stmt.bindString(3,valueComplexidade);
            stmt.bindString(4,valueEquipamentos);
            stmt.bindString(5,risco);
            stmt.bindString(6,valueUrgente);
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

            stmt.bindString(7,imageFromCameraAll);
            stmt.bindString(8,location);
            String status = completed ? "completed" : "active";
            stmt.bindString(9,status);
            stmt.bindLong(10,id);
            stmt.executeUpdateDelete();
            bancoDados.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        //Toast.makeText(getApplicationContext(),riscos[position] , Toast.LENGTH_LONG).show();
        risco = riscos[position];
        if(riscos[position].equals("Outros")){
            Toast.makeText(getApplicationContext(), "Descreva o risco no campo observações", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}