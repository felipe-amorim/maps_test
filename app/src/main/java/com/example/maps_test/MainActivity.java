package com.example.maps_test;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase bancoDados;
    public ListView listViewDados;
    public Button botao;
    public ArrayList<Integer> arrayIds;
    public ArrayList<Integer> arrayIdsDB;
    public Integer idSelecionado;
    ArrayList<String> linhasDB = new ArrayList<String>();
    ArrayList<String> statusPerLine = new ArrayList<String>();
    ArrayList<String> complexityPerLine = new ArrayList<String>();
    ArrayList<String> contentPerLine = new ArrayList<String>();
    ArrayList<String> datePerLine = new ArrayList<String>();
    ArrayList<String> urgentPerLine = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //System.out.println("passando por aqui");
        //deleteDatabase("crudapp");

        listViewDados = (ListView) findViewById(R.id.listViewDados);
        botao = (Button) findViewById(R.id.buttonAlterar);

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirTelaCadastro();
            }
        });

        listViewDados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                idSelecionado = arrayIds.get(i);
                abrirTelaAlterar();
            }
        });

        criarBancoDados();
        listarDados();

        if(!hasPermissions(this, PERMISSIONS)){
            System.out.println("=========================================nottttt========================================================");
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }


    final int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION};


    public static boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        listarDados();
    }

    public void criarBancoDados(){
        try {
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            bancoDados.execSQL("CREATE TABLE IF NOT EXISTS coisa(" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT" +
                    " , nome VARCHAR"+
                    " , obs VARCHAR"+
                    " , complexidade VARCHAR"+
                    " , equipamentosEspeciais VARCHAR"+
                    " , risco VARCHAR"+
                    " , urgencia VARCHAR"+
                    " , data VARCHAR"+
                    " , foto VARCHAR"+
                    " , gps VARCHAR"+
                    " , status VARCHAR)");
            bancoDados.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listarDados(){
        try {
            complexityPerLine = new ArrayList<String>();
            statusPerLine = new ArrayList<String>();
            contentPerLine = new ArrayList<String>();
            datePerLine = new ArrayList<String>();
            urgentPerLine = new ArrayList<String>();
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            Cursor meuCursor = bancoDados.rawQuery("SELECT id, nome, obs, complexidade, equipamentosEspeciais, risco, urgencia, data, foto, gps, status FROM coisa ORDER BY status, urgencia DESC, complexidade, data DESC, nome", null);
            meuCursor.moveToFirst();
            while(meuCursor!=null){
                complexityPerLine.add(meuCursor.getString(3));
                statusPerLine.add(meuCursor.getString(10));
                contentPerLine.add(meuCursor.getString(1) + " (" + meuCursor.getString(2)+")");
                datePerLine.add(meuCursor.getString(7));
                urgentPerLine.add(meuCursor.getString(6));
                arrayIds.add(meuCursor.getInt(0));
                meuCursor.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            applyTheme();
        }
    }

    public void applyTheme(){
        try {
            arrayIds = new ArrayList<>();
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            Cursor meuCursor = bancoDados.rawQuery("SELECT id, nome, obs, complexidade, equipamentosEspeciais, risco, urgencia, data, foto, gps, status FROM coisa ORDER BY status, urgencia DESC, complexidade, data DESC, nome", null);
            ArrayList<String> linhas = new ArrayList<String>();
            ArrayAdapter meuAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    linhas
            )
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent){

                    LayoutInflater inflater=getLayoutInflater();
                    View row=inflater.inflate(R.layout.row, parent, false);
                    TextView label=(TextView)row.findViewById(R.id.label);

                    label.setText(datePerLine.get(position)+"    "+contentPerLine.get(position));

                    ImageView icon=(ImageView)row.findViewById(R.id.icon);
                    if (urgentPerLine.get(position).equals("true")) {
                        icon.setImageResource(android.R.drawable.ic_dialog_alert);
                        icon.setColorFilter(Color.RED);
                    }

                    String comp = complexityPerLine.get(position);
                    int colr = 0;
                    if(comp.equals("3")){
                        colr = Color.parseColor("#a9d6a3");
                    }
                    if(comp.equals("2")){
                        colr = Color.parseColor("#f4f498");
                    }
                    if(comp.equals("1")){
                        colr = Color.parseColor("#f49898");
                    }
                    String status = statusPerLine.get(position);
                    System.out.println("status "+status);
                    if(status.equals("completed")){
                        colr = Color.parseColor("#B9B9B9");
                    }
                    row.setBackgroundColor(colr);


                    return(row);
                };

            };
            listViewDados.setAdapter(meuAdapter);
            meuCursor.moveToFirst();
            while(meuCursor!=null){
                linhas.add(meuCursor.getString(7) + "    " + meuCursor.getString(1) + " (" + meuCursor.getString(2)+")");
                arrayIds.add(meuCursor.getInt(0));
                meuCursor.moveToNext();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirTelaCadastro(){
        Intent intent = new Intent(this,CadastroActivity.class);
        startActivity(intent);
    }

    public void confirmaExcluir() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
        msgBox.setTitle("Excluir");
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage("Você realmente deseja excluir esse registro?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                excluir();
                listarDados();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        msgBox.show();
    }

    public void excluir(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Por favor confirme a exclusão");

        alert.setPositiveButton("EXCLUIR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try{
                    bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
                    String sql = "DELETE FROM coisa WHERE id =?";
                    SQLiteStatement stmt = bancoDados.compileStatement(sql);
                    stmt.bindLong(1, idSelecionado);
                    stmt.executeUpdateDelete();
                    listarDados();
                    bancoDados.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();

    }

    public void abrirTelaAlterar(){
        //Intent intent = new Intent(this, AlterarActivity.class);
        //intent.putExtra("id",idSelecionado);
        Intent intent = new Intent(this, AlterarActivity.class);
        //startActivity(intent);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Selecione uma ação:");
        // alert.setMessage("Message");

        alert.setPositiveButton("Detalhes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Your action here
                intent.putExtra("id",idSelecionado);
                startActivity(intent);
            }
        });

        alert.setNegativeButton("Excluir",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    excluir();
                }
            });

        alert.show();
    }
}