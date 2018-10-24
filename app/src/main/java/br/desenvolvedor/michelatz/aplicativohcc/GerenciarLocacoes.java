package br.desenvolvedor.michelatz.aplicativohcc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewGeral;
import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewPostes;
import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewVegetacao;
import br.desenvolvedor.michelatz.aplicativohcc.Adapter.AdapterListViewVisitaTecnica;
import br.desenvolvedor.michelatz.aplicativohcc.ClassesExtras.Helper;
import br.desenvolvedor.michelatz.aplicativohcc.Modelo.DadosGerais;

public class GerenciarLocacoes extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner spnLocacoesAbertas;
    private AdapterListViewGeral adapterListViewFotosPoste;
    File fileFinal;
    ArrayList<String> locacoesAbertas = new ArrayList<String>();
    EditText edtNumeroNota, edtCliente;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private String caminhoImagem = "inicial";
    private ListView listViewFotosPostes;
    Uri outputUri;

    ProgressDialog progress;
    private File pdfFile;
    SQLiteDatabase db;

    String BANCO = "banco.db";
    String TABELALOCACAO = "locacao", TABELACOORDENADAS = "coordenadas", TABELADOCUMENTO = "documento", TABELACONSUMIDOR = "consumidor", TABELAFOTOSVEGETACAO = "vegetacao";
    String TABELAPOSTE = "poste", TABELAESTRUTURA = "estrutura", TABELAFOTOSPOSTE = "fotosposte", TABELAEQUIPAMENTO = "equipamento", TABELAFOTOSVISITATECNICA="visitatecnica";
    String textoEstruturaPrimaria, textoEstruturaSecundaria, texto, previa, notaLocacao, idLocacao;
    String idNota = "", buscaNota = "", buscaCliente = "";

    private ListView listViewPostes, listViewDoc, listViewVegetacao , listViewVisita;
    private AdapterListViewPostes adapterListViewPostes;
    private AdapterListViewGeral adapterListViewDocumentos;
    private AdapterListViewVegetacao adapterListViewVegetacao;
    private AdapterListViewVisitaTecnica adapterListViewVisitaTecnica;
    private ArrayList<DadosGerais> itens;
    private ArrayList<DadosGerais> itensDoc;
    private ArrayList<DadosGerais> itensVegetacao;
    private ArrayList<DadosGerais> itensVisita;
    private String imgString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locacoes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Locações");
        setSupportActionBar(toolbar);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        notaLocacao = sharedpreferences.getString("numeroNotaKey", null);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);

        spnLocacoesAbertas = (Spinner) findViewById(R.id.spnLocacoesAbertas);
        edtNumeroNota = (EditText) findViewById(R.id.edtNumeroNota);
        edtCliente = (EditText) findViewById(R.id.edtCliente);
        listViewPostes = (ListView) findViewById(R.id.listViewPostes);
        listViewDoc = (ListView) findViewById(R.id.listViewDocumentos);
        listViewVegetacao = (ListView) findViewById(R.id.listViewVegetacao);

        listViewFotosPostes = (ListView) findViewById(R.id.listViewVisitaTecnica);
        listViewVisita = (ListView) findViewById(R.id.listViewVisitaTecnica);

        if (idLocacao != null) {
            buscaDados();
            inflaListaPostes();
            inflaListaDocumentosComIdLocacao();
            inflaListaVegetacaoComIdLocacao();
        } else if (notaLocacao != null) {
            buscaDados();
            inflaListaPostes();
            inflaListaDocumentosComIdLocacao();
            inflaListaVegetacaoComIdLocacao();
        }
        buscarSQLPreencheSpinner();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, locacoesAbertas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocacoesAbertas.setAdapter(adapter);

        spnLocacoesAbertas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringTokenizer st = new StringTokenizer(parent.getItemAtPosition(position).toString());
                previa = st.nextToken(" ");
                if (previa.equals("Selecione")) {

                } else {
                    SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    buscaDadosClickSpn();
                    editor.putString("numeroNotaKey", previa);
                    //editor.putString("idLocacaoKey", previa);
                    editor.commit();

                    Intent it;
                    it = new Intent(GerenciarLocacoes.this, GerenciarLocacoes.class);
                    startActivity(it);
                    finish();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Another interface callback
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GerenciarLocacoes.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            return;
        }
        inflaListaFotosPostes();
    }

    public void buscarSQLPreencheSpinner() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE STATUS = 0", null);

        locacoesAbertas.add("Selecione uma Solicitação");
        if (linhas.moveToFirst()) {
            do {
                String buscaNumeroNota = linhas.getString(1);
                String buscaNomeCliente = linhas.getString(3);
                locacoesAbertas.add(buscaNumeroNota + " - " + buscaNomeCliente);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();
    }

    public void buscaDados() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE ID = '" + idLocacao + "'", null);

        if (linhas.moveToFirst()) {
            do {
                buscaNota = linhas.getString(1);
                buscaCliente = linhas.getString(3);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();

        edtNumeroNota.setText(buscaNota);
        edtCliente.setText(buscaCliente);
    }

    public void buscaDadosClickSpn() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELALOCACAO + " WHERE NOTA = '" + previa + "'", null);

        if (linhas.moveToFirst()) {
            do {
                idNota = linhas.getString(0);
                buscaNota = linhas.getString(1);
                buscaCliente = linhas.getString(3);
            }
            while (linhas.moveToNext());
        }
        linhas.close();
        db.close();

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("idLocacaoKey", idNota);
        editor.commit();
    }

    private void inflaListaPostes() {
        itens = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = "Poste " + contador;

                DadosGerais item1 = new DadosGerais(idPoste, texto);
                itens.add(item1);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewPostes = new AdapterListViewPostes(this, itens);
        listViewPostes.setAdapter(adapterListViewPostes);
        listViewPostes.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewPostes);
        db.close();
    }

    private void inflaListaDocumentosComIdLocacao() {
        itensDoc = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT ID, NOMEDOCUMENTO FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + ";", null); //where nota = nota locacao
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = contador + " - " + linhas.getString(1);

                DadosGerais itemDoc = new DadosGerais(idPoste, texto);
                itensDoc.add(itemDoc);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewDocumentos = new AdapterListViewGeral(this, itensDoc);
        listViewDoc.setAdapter(adapterListViewDocumentos);
        listViewDoc.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewDoc);
        db.close();
    }

    private void inflaListaVegetacaoComIdLocacao() {
        itensVegetacao = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT ID, DESCRICAO FROM " + TABELAFOTOSVEGETACAO + " WHERE IDLOCACAO = " + idLocacao + ";", null); //where nota = nota locacao
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = " Foto "+contador+ " - " + linhas.getString(1);

                DadosGerais itemDoc = new DadosGerais(idPoste, texto);
                itensVegetacao.add(itemDoc);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewVegetacao = new AdapterListViewVegetacao(this, itensVegetacao);
        listViewVegetacao.setAdapter(adapterListViewVegetacao);
        listViewVegetacao.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        Helper.getListViewSize(listViewVegetacao);
        db.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent it;
            it = new Intent(GerenciarLocacoes.this, TelaPrincipal.class);
            startActivity(it);
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_locacoes) {

            Intent it;
            it = new Intent(this, GerenciarLocacoes.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_locacoesfinalizadas) {

            Intent it;
            it = new Intent(this, ListaLocacoesFinalizadas.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_sair) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idKey");
            editor.remove("nomeKey");
            editor.remove("emailKey");

            editor.commit();
            editor.clear();

            Intent it;
            it = new Intent(this, Login.class);
            startActivity(it);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addLocacao(View v) {
        Bundle bundle = new Bundle();
        Intent it;
        bundle.putString("tipo", "1");
        it = new Intent(this, CadastrarLocacao.class);
        it.putExtras(bundle);
        startActivity(it);
        finish();
    }

    public void editarCliente(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Solicitação para edita-lá!", Toast.LENGTH_SHORT).show();
        } else {
            Bundle bundle = new Bundle();
            Intent it;
            bundle.putString("tipo", "2");
            it = new Intent(this, CadastrarLocacao.class);
            it.putExtras(bundle);
            startActivity(it);
            finish();
        }
    }

    public void adicionarPoste(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Nota!", Toast.LENGTH_SHORT).show();
        } else {
            criarPosteBanco();
            Intent intent = new Intent(this, InseriPoste.class);
            intent.putExtra("USERTELA", "1");
            this.startActivity(intent);
            finish();
        }
    }

    public void criarPosteBanco() {

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        ContentValues values = new ContentValues();
        values.put("IDLOCACAO", idLocacao);
        values.put("TIPOPOSTE", "");
        values.put("ALTURA", "");
        values.put("CAPACIDADE", "");
        values.put("NUMPLACA", "");
        values.put("ILUMICACAOPUBLICA", "");
        values.put("TELEFONIA", "");
        values.put("QUANTIDADE", "");
        values.put("ACESSO", "");
        values.put("TIPOSOLO", "");

        long ultimoId = db.insert(TABELAPOSTE, null, values);
        String retorno = String.valueOf(ultimoId);
        inseriLocalizacao(retorno);
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("idPosteKey", retorno);
        editor.commit();
        db.close();

    }

    private void inseriLocalizacao(String retorno) {
        String latitudeBanco = "8888888888";
        String longitudeBanco = "99999999999";
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        db.execSQL("INSERT INTO " + TABELACOORDENADAS + "(LATITUDE, LONGITUDE, IDPOSTE) VALUES (" +
                "'" + latitudeBanco + "','" + longitudeBanco + "','" + retorno + "')");
        db.close();
    }

    public void adicionarDocumento(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Nota!", Toast.LENGTH_SHORT).show();
        } else {
            Intent it;
            it = new Intent(this, InseriImagemDocumento.class);
            startActivity(it);
            finish();
        }
    }

    public void adicionarVegetacao(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Nota!", Toast.LENGTH_SHORT).show();
        } else {
            Intent it;
            it = new Intent(this, ManejoVegetacao.class);
            startActivity(it);
            finish();
        }
    }

    public void deletaItem(View v) {
        adapterListViewDocumentos.removeItem((Integer) v.getTag());
        String idMensagem = adapterListViewDocumentos.idSelecionado;
        confirmarDelete(idMensagem);
    }

    public void deletaItemVegetacao(View v) {
        adapterListViewVegetacao.removeItem((Integer) v.getTag());
        String idMensagem = adapterListViewVegetacao.idSelecionado;
        confirmarDeleteVegetacao(idMensagem);
    }

    public void editarItem(View v) {
        adapterListViewPostes.editaItem((Integer) v.getTag());
        String idMensagem = adapterListViewPostes.idSelecionado;
        editarPoste(idMensagem);
    }

    public void deletaItemVisita(View v) {
        Log.d("teste","entrou aqui");
        adapterListViewVisitaTecnica.removeItem((Integer) v.getTag());
        String idMensagem = adapterListViewVisitaTecnica.idSelecionado;
        confirmarDeleteVisita(idMensagem);
    }

    private void confirmarDeleteVisita(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar esta Imagem?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarMensagemVisita(idMensagem);
                    }
                });

        alertDialogBuilder.setNegativeButton("Não",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deletarMensagemVisita(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELAFOTOSVISITATECNICA + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaFotosPostes();
    }

    private void confirmarDelete(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar este Documento?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarMensagem(idMensagem);
                    }
                });

        alertDialogBuilder.setNegativeButton("Não",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deletarMensagem(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELADOCUMENTO + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaDocumentosComIdLocacao();
    }

    private void confirmarDeleteVegetacao(final String idMensagem) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja deletar este Documento?");

        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deletarMensagemVegetacao(idMensagem);
                    }
                });

        alertDialogBuilder.setNegativeButton("Não",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deletarMensagemVegetacao(final String idMens) {
        int idExcluido = Integer.parseInt(idMens.toString());
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("DELETE FROM " + TABELAFOTOSVEGETACAO + " WHERE ID = " + idExcluido + "");
        db.close();

        inflaListaVegetacaoComIdLocacao();
    }

    private void editarPoste(final String idMens) {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString("idPosteKey", idMens);
        editor.commit();

        Intent intent = new Intent(this, InseriPoste.class);
        intent.putExtra("USERTELA", "EDITAR");
        this.startActivity(intent);
        finish();
    }

    public void salvarLocacao(View v) {
        finalizarLocacao();
    }

    private void finalizarLocacao() {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Solicitação!", Toast.LENGTH_SHORT).show();
        } else {
            progress = ProgressDialog.show(GerenciarLocacoes.this, "Geração de Arquivos",
                    "Os Arquivos estão sendo gerados, Aguarde!", true);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date data = new Date();

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(data);
                    Date data_atual = cal.getTime();
                    String data_completa = dateFormat.format(data_atual);

                    db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                    ContentValues values = new ContentValues();
                    values.put("STATUS", "1");
                    values.put("DATA", data_completa);
                    db.update(TABELALOCACAO, values, "ID=" + idLocacao, null);
                    db.close();

                    criaTxtPoste(idLocacao);
                    criaTxtConsumidores(idLocacao);
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            sucessoGeracaoArquivos();
                        }
                    });
                }
            }).start();
        }
    }

    public void sucessoGeracaoArquivos() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Sucesso na Geração de Arquivos");
        alertDialogBuilder.setMessage("Os arquivos TXTs e PDFs foram gerados com sucesso!");

        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.remove("numeroNotaKey");
                        editor.remove("idLocacaoKey");

                        editor.commit();
                        editor.clear();

                        Intent it;
                        it = new Intent(GerenciarLocacoes.this, TelaPrincipal.class);
                        startActivity(it);
                        finish();
                    }
                });


        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void criaTxtPoste(String idLocacao) {
        String textoFinal = "";
        String valorNulo = "1";

        String textoFinalEstruturaPrimaria = "";
        String textoFinalEstruturaSecundaria = "";
        String id = "";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);

        if(linhas.getCount() != 0) {
            int contador = 1;
            if (linhas.moveToFirst()) {
                do {
                    if (contador < 10) {
                        id = "00" + contador;
                    } else if (contador >= 10 && contador < 100) {
                        id = "0" + contador;
                    } else if (contador >= 100) {
                        id = String.valueOf(contador);
                    }

                    Cursor linhasCoordenadas = db.rawQuery("SELECT * FROM " + TABELACOORDENADAS + " WHERE IDPOSTE = " + linhas.getString(0) + ";", null);

                    String latitudeUTM = "";
                    String longitudeUTM = "";

                    if (linhasCoordenadas.moveToFirst()) {
                        do {
                            latitudeUTM = linhasCoordenadas.getString(2);
                            longitudeUTM = linhasCoordenadas.getString(1);

                        }
                        while (linhasCoordenadas.moveToNext());
                        linhasCoordenadas.close();
                    }

                    Cursor linhasEstrutura = db.rawQuery("SELECT * FROM " + TABELAESTRUTURA + " WHERE IDPOSTE = " + linhas.getString(0) + ";", null);

                    if (linhasEstrutura.moveToFirst()) {
                        do {
                            if (linhasEstrutura.getString(1).equals("Primaria")) {
                                String textoCentro = "";
                                String textoParteFinal = "";
                                //Log.d("teste",linhasEstrutura.getString(4));
                                //if(!linhasEstrutura.getString(3).equals("")){
                                    //textoCentro = "("+linhasEstrutura.getString(3)+")";
                                //}

                                //if(linhasEstrutura.getString(5).equals("Metálica")){
                                    //textoParteFinal = "CM";
                                //}

                                textoEstruturaPrimaria = (linhasEstrutura.getString(2));// +textoCentro+textoParteFinal
                                textoFinalEstruturaPrimaria = textoFinalEstruturaPrimaria + textoEstruturaPrimaria;
                            } else if (linhasEstrutura.getString(1).equals("Secundaria")) {
                                textoEstruturaSecundaria = (linhasEstrutura.getString(2));
                                textoFinalEstruturaSecundaria = textoFinalEstruturaSecundaria + textoEstruturaSecundaria;
                            }
                            textoEstruturaPrimaria = "";
                            textoEstruturaSecundaria = "";
                        }
                        while (linhasEstrutura.moveToNext());
                        linhasEstrutura.close();
                    }
                    String AlturaCapacidade = "";
                    String verificaCapacidade = "";

                    if(!linhas.getString(3).equals("")) {
                        if (linhas.getString(3).equals("Nenhuma")) {
                            verificaCapacidade = "";
                        } else if (linhas.getString(3).equals("150")) {
                            verificaCapacidade = "1,5";
                        } else{
                            int inteiroCapacidade = Integer.parseInt(linhas.getString(3));
                            inteiroCapacidade = inteiroCapacidade/100;
                            verificaCapacidade = Integer.toString(inteiroCapacidade);
                        }
                    }

                    if (linhas.getString(2) != null && !verificaCapacidade.equals("")) {
                        AlturaCapacidade = linhas.getString(2) + "(" +verificaCapacidade+ ")";
                    } else if (linhas.getString(2) != null && verificaCapacidade.equals("")) {
                        AlturaCapacidade = linhas.getString(2);
                    } else if (linhas.getString(2) == null && !verificaCapacidade.equals("")) {
                        AlturaCapacidade = verificaCapacidade;
                    } else if (linhas.getString(2) == null && verificaCapacidade.equals("")) {
                        AlturaCapacidade = "";
                    }
                    if (latitudeUTM.equals("") && longitudeUTM.equals("") && AlturaCapacidade.equals("") && textoFinalEstruturaPrimaria.equals("") && textoFinalEstruturaSecundaria.equals("") && linhas.getString(7).equals("") && linhas.getString(4).equals("") && linhas.getString(1).equals("") && linhas.getString(6).equals("") && linhas.getString(9).equals("") && linhas.getString(8).equals("")) {
                        valorNulo = "1";
                    } else {
                        valorNulo = "2";
                    }

                    String numero = String.format("%-3s", id);
                    String coordenadaX = String.format("%-10s", latitudeUTM);
                    String coordenadaY = String.format("%-11s", longitudeUTM);
                    String alturaCapacidade = String.format("%-7s", AlturaCapacidade);
                    //String codigo = String.format("%-10s", linhas.getString(4));
                    String bloco = String.format("%-4s", linhas.getString(1));
                    String iluminacaoPublica = String.format("%-3s", linhas.getString(5));
                    String solo = String.format("%-1s", linhas.getString(9));
                    String acesso = String.format("%-1s", linhas.getString(8));
                    String vidaUtil = String.format("%-3s", linhas.getString(10));
                    String estai;
                    String linhaViva;
                    String tlSemelhantes;

                    if (valorNulo.equals("1")) {
                        texto = ("");
                    } else {
                        if (textoFinalEstruturaPrimaria.equals("")) {
                            textoFinalEstruturaPrimaria = "-";
                        }
                        if (textoFinalEstruturaSecundaria.equals("")) {
                            textoFinalEstruturaSecundaria = "-";
                        }

                        if (linhas.getString(6).equals("")) {
                            tlSemelhantes = "-";
                        } else {
                            tlSemelhantes = linhas.getString(6);
                        }

                        if (linhas.getString(7).equals("")) {
                            estai = "-";
                        } else {
                            estai = linhas.getString(7);
                        }

                        if (linhas.getString(5).equals("")) {
                            linhaViva = "-";
                        } else {
                            linhaViva = linhas.getString(5);
                        }

                        String estruturaMT = String.format("%-30s", textoFinalEstruturaPrimaria);
                        String estruturaBT = String.format("%-30s", textoFinalEstruturaSecundaria);
                        String formatoLinhaViva = String.format("%-3s", linhaViva);

                        texto = (numero + " " + coordenadaX + " " + coordenadaY + " " + alturaCapacidade + " " + estruturaMT + " " + estruturaBT + " " + String.format("%-15s", estai) + " " + bloco + " " + iluminacaoPublica + " " + String.format("%-13s", tlSemelhantes) + " " + solo + " " + acesso + " " + vidaUtil + " " + formatoLinhaViva + " \n");
                    }

                    textoFinal = textoFinal + texto;
                    textoFinalEstruturaSecundaria = "";
                    textoFinalEstruturaPrimaria = "";
                    estai = "";
                    tlSemelhantes = "";
                    linhaViva="";
                    contador++;
                }
                while (linhas.moveToNext());
                linhas.close();
            }
            db.close();
            if (textoFinal.equals("")) {
                textoFinal = "Não foram inseridos dados de nenhum poste neste Relatório!";
            }
            gerarArquivo(this, notaLocacao + "_Postes.txt", textoFinal);
        }
    }

    public void criaTxtConsumidores(String idLocacao) {
        String textoFinal = "";
        String texto1 = "";
        String texto2 = "";
        String textoSegundoRelatorio = "";
        String textoFinalSegundoRelatorio = "";
        String id = "";
        String idPoste = "";

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);

        Log.d("Resp - linhas - eita", String.valueOf(linhas.getCount()));

        if(linhas.getCount() != 0) {
            int contador = 1;
            if (linhas.moveToFirst()) {
                do {
                    idPoste = linhas.getString(0);
                    if (contador < 10) {
                        id = "00" + contador;
                    } else if (contador >= 10 && contador < 100) {
                        id = "0" + contador;
                    } else if (contador >= 100) {
                        id = "" + String.valueOf(contador);
                    }

                    Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELACONSUMIDOR + " WHERE IDPOSTE = " + idPoste + ";", null);

                    int contador2 = 0;
                    if (linhas2.moveToFirst()) {
                        do {
                            texto2 = (id + " " + linhas2.getString(1) + " " + String.format("%-3s", linhas2.getString(4)) + " " + String.format("%-1s", linhas2.getString(3)) +" " + String.format("%-1s", linhas2.getString(2))+ " " + String.format("%-4s", linhas2.getString(5)) + " " + String.format("%-4s", linhas2.getString(6)) + " " + String.format("%-7s", linhas2.getString(7))+" \n");
                            textoFinal = textoFinal + texto2;
                            contador2++;
                        }
                        while (linhas2.moveToNext());
                        linhas2.close();
                    }
                    textoSegundoRelatorio = id + " " + contador2 + " " + linhas.getString(5);
                    textoFinalSegundoRelatorio = textoFinalSegundoRelatorio + textoSegundoRelatorio + " \n";
                    texto2 = "";
                    contador++;
                }
                while (linhas.moveToNext());
                linhas.close();
            }
            db.close();

            if (textoFinal.equals("")) {
                textoFinal = "Não foram inseridos dados sobre os Consumidores neste relatório!";
            }
            gerarArquivo(this, notaLocacao + "_Consumidores.txt", textoFinal);
            gerarArquivo(this, notaLocacao + "_Queda.txt", textoFinalSegundoRelatorio);
        }
    }

    public void gerarArquivo(Context context, String nomeArquivo, String conteudo) {
        if (conteudo.equals("")) {
            conteudo = "Não foram inseridos dados!";
        }

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "HCC");
            if (!root.exists()) {
                root.mkdirs();
            }

            File root2 = new File(Environment.getExternalStorageDirectory(), "HCC/" + notaLocacao);
            if (!root2.exists()) {
                root2.mkdirs();
            }

            File gpxfile = new File(root2, nomeArquivo);
            //FileWriter writer = new FileWriter(gpxfile);
            //writer.append(conteudo);
            //writer.flush();
            //writer.close();

            //OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(gpxfile, true),
            //"Cp1252");

            Writer out = new OutputStreamWriter(new FileOutputStream(gpxfile), "windows-1252");
            out.append(conteudo);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // verifica permissões
    public void createPdfWrapper() throws FileNotFoundException, DocumentException {
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("Você precisa permitir o acesso ao Armazenamento",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            try {
                createPdf();
                createPdfVegetecao();
                createPdfVisita();
                createPdfImagens();
                //createPdfImagensTeste();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo responsavel por verificar se as permissões foram aceitas
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Se a permissão foi aceita, chama o metodo para criar o PDF
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Permissão Negada", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Metodo do Alert
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Cria PDF com inserções de imagens dos postes.
    private void createPdf() throws IOException, DocumentException {

        int contador = 1;
        String idPoste = "", conferiTexto = "", textoDadosEquipamento = "", id = "";

        // Verifica se a pasta HCC já existe no smartfone, se não existir, cria a pasta
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        // Verifica se a pasta com o numero da nota ja existe dentro da pasta HCC
        // Se não existir, cria a nova pasta
        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAPOSTE + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador2 = 0;
        if(linhas.getCount() != 0) {
            if (linhas.moveToFirst()) {
                do {
                    idPoste = linhas.getString(0);
                    if (contador < 10) {
                        id = "00" + contador;
                    } else if (contador >= 10 && contador < 100) {
                        id = "0" + contador;
                    } else if (contador >= 100) {
                        id = "" + String.valueOf(contador);
                    }

                    Cursor linhas3 = db.rawQuery("SELECT * FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE= " + idPoste + ";", null);
                    int  contador3 = 1;
                    if (linhas3.moveToFirst() && linhas3.getCount() > 0) {

                        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Relatorio Fotografico.pdf");
                        OutputStream output = new FileOutputStream(pdfFile);
                        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                        PdfWriter.getInstance(document, output);

                        document.addTitle("Equipamentos");
                        document.open();

                        do {
                            byte[] imagemBase64 = linhas3.getBlob(1);
                            if (imagemBase64 != null) {
                                contador2++;

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                Image myImg = Image.getInstance(stream.toByteArray());
                                myImg.setAlignment(Image.MIDDLE);
                                document.add(myImg);

                                ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                                Bitmap imagemBitmap = decodeBase64(imagemBase64);
                                Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.7), (int) (imagemBitmap.getHeight() * 0.7), true);//tava 0.68
                                novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                                Image myImg3 = Image.getInstance(stream3.toByteArray());


                                document.add(new Paragraph("     Imagem "+contador2+": \n     Poste " + id+" - Foto "+contador3));
                                Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                document.add(paragrafo1);

                                myImg3.setAlignment(Image.MIDDLE);
                                myImg3.setAbsolutePosition(110f, 90f);
                                document.add(myImg3);

                                ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 80, true);
                                tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                Image myImg2 = Image.getInstance(stream2.toByteArray());
                                myImg2.setAbsolutePosition(3, 3);
                                document.add(myImg2);
                                document.newPage();
                                contador3++;

                            }
                            document.newPage();
                        }
                        while (linhas3.moveToNext());
                        linhas3.close();
                        document.close();
                    }
                    contador++;
                }
                while (linhas.moveToNext());
                linhas.close();
            }

        }
        db.close();

        /*
        if(linhas.getCount() != 0) {
            if (linhas.moveToFirst()) {

                do {
                    idPoste = linhas.getString(0);
                    // Contador que controla a quantidade de imagens
                    if (contador < 10) {
                        id = "00" + contador;
                    } else if (contador >= 10 && contador < 100) {
                        id = "0" + contador;
                    } else if (contador >= 100) {
                        id = "" + String.valueOf(contador);
                    }
                    Log.d("teste","entrou primeiro");

                    // Select que busca os dados da locação
                    Cursor linhas2 = db.rawQuery("SELECT * FROM " + TABELAEQUIPAMENTO + " WHERE IDPOSTE= " + idPoste + ";", null);

                    if(linhas2.getCount() != 0) {
                        pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Equipamento.pdf");
                        OutputStream output = new FileOutputStream(pdfFile);
                        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
                        PdfWriter.getInstance(document, output);

                        document.addTitle("Equipamentos");
                        document.open();
                        Log.d("teste","entrou segubndo");

                        if (linhas2.moveToFirst()) {
                            do {
                                // Caso não existirem dados daquele poste
                                if (linhas2.getString(1).equals("") && linhas2.getString(2).equals("")) {
                                    textoDadosEquipamento = "Não foram inseridos dados do Equipamento!";
                                    conferiTexto = "1";
                                } else {
                                    textoDadosEquipamento = "   Tipo de Equipamento: " + linhas2.getString(1) + "\n   Numero da Placa: " + linhas2.getString(2) + "\n\n";
                                    conferiTexto = "0";
                                }

                                // Select para buscar as imagens referentes ao id daquele poste
                                Cursor linhas3 = db.rawQuery("SELECT * FROM " + TABELAFOTOSPOSTE + " WHERE IDPOSTE= " + idPoste + ";", null);

                                if (linhas3.moveToFirst() && linhas3.getCount() > 0) {
                                    Log.d("teste","entrou terceiro");
                                    int contador2 = 1;
                                    do {
                                        byte[] imagemBase64 = linhas3.getBlob(1);
                                        if (imagemBase64 != null) {
                                            contador2++;
                                            Log.d("teste","eita"+imagemBase64);
                                            // Cria a parte de cima da pagina, com a imagem padrão
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                            Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                            tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            Image myImg = Image.getInstance(stream.toByteArray());
                                            myImg.setAlignment(Image.MIDDLE);
                                            document.add(myImg);

                                            ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                                            Bitmap imagemBitmap = decodeBase64(imagemBase64);
                                            Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.38), (int) (imagemBitmap.getHeight() * 0.38), true);//tava 0.68
                                            novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                                            Image myImg3 = Image.getInstance(stream3.toByteArray());


                                            document.add(new Paragraph(" Poste: " + id));
                                            Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                            //inseri paragrafo com os dados do poste
                                            document.add(paragrafo1);

                                            myImg3.setAlignment(Image.MIDDLE);
                                            myImg3.setAbsolutePosition(132f, 190f);
                                            // Inseri paragrafo com imagens do poste
                                            document.add(myImg3);

                                            // Cria a parte de baixo da pagina, com a imagem padrão
                                            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                            Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                            Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 190, true);
                                            tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                            Image myImg2 = Image.getInstance(stream2.toByteArray());
                                            myImg2.setAbsolutePosition(3, 3);
                                            document.add(myImg2);
                                            document.newPage();

                                        }
                                        document.newPage();
                                    }
                                    while (linhas3.moveToNext());
                                    linhas3.close();
                                } else {

                                    if (conferiTexto.equals("0")) {
                                        // Cria a parte de cima da pagina, com a imagem padrão
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                                        Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                                        tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                        Image myImg = Image.getInstance(stream.toByteArray());
                                        myImg.setAlignment(Image.MIDDLE);
                                        document.add(myImg);

                                        // Inseri paragrafo com os dados do poste
                                        document.add(new Paragraph(" Poste: " + id));
                                        Paragraph paragrafo1 = new Paragraph(textoDadosEquipamento);
                                        document.add(paragrafo1);

                                        // Inseri outro paragrafo com o feedback
                                        Paragraph paragrafo2 = new Paragraph(" Não foi inserida imagem deste Equipamento");
                                        paragrafo2.setAlignment(Element.ALIGN_CENTER);

                                        // Cria a parte de baixo da pagina, com a imagem padrão
                                        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
                                        Bitmap bitmap2 = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldebaixo);
                                        Bitmap tamanhoReduzidoParaBaixo = Bitmap.createScaledBitmap(bitmap2, 589, 190, true);
                                        tamanhoReduzidoParaBaixo.compress(Bitmap.CompressFormat.JPEG, 100, stream2);
                                        Image myImg2 = Image.getInstance(stream2.toByteArray());
                                        myImg2.setAbsolutePosition(3, 3);
                                        document.add(myImg2);

                                        document.newPage();
                                    }
                                }
                            }
                            while (linhas2.moveToNext());
                            linhas2.close();
                        }
                        document.close();
                    }
                    contador++;
                }
                while (linhas.moveToNext());
                linhas.close();
            }
        }
        db.close();
        */
    }

    private void createPdfVegetecao() throws IOException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        Cursor linhas1 = db.rawQuery("SELECT ID, IMAGEM, DESCRICAO FROM " + TABELAFOTOSVEGETACAO+ " WHERE IDLOCACAO = " + idLocacao + " ;", null);

        if(linhas1.getCount() != 0) {

            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Vegetacao.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            document.addTitle("Manejo Vegetação");
            int contador=1;
            if (linhas1.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph(" Foto "+contador+": "+linhas1.getString(2));
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas1.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap imagemVirada;
                    if(imagemBitmap.getWidth() > imagemBitmap.getHeight()){
                        imagemVirada = RotateBitmap(imagemBitmap, 90);
                    }else{
                        imagemVirada = imagemBitmap;
                    }

                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.7), (int) (imagemVirada.getHeight() * 0.7), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    document.newPage();
                    contador++;
                }
                while (linhas1.moveToNext());
                linhas1.close();
                document.close();
            }
        }
        db.close();
    }

    private void createPdfVisita() throws IOException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        Cursor linhas1 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELAFOTOSVISITATECNICA+ " WHERE IDLOCACAO = " + idLocacao + " ;", null);

        if(linhas1.getCount() != 0) {

            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Visita_Tecnica.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            document.addTitle("Visita Técnica");

            int contador = 1;
            if (linhas1.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph(" Visita Técnica - Foto "+contador);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas1.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap imagemVirada;
                    if(imagemBitmap.getWidth() > imagemBitmap.getHeight()){
                        imagemVirada = RotateBitmap(imagemBitmap, 90);
                    }else{
                        imagemVirada = imagemBitmap;
                    }

                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.7), (int) (imagemVirada.getHeight() * 0.7), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    contador++;
                    document.newPage();
                }
                while (linhas1.moveToNext());
                linhas1.close();
                document.close();
            }
        }
        db.close();
    }

    // Cria o PDF de todas as Imagens dos documentos, retiradas da Locação
    public void createPdfImagens() throws IOException, DocumentException {

        // Verifica se a pasta HCC já existe no smartfone, se não existir, cria a pasta
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        // Verifica se a pasta com o numero da nota ja existe dentro da pasta HCC
        // Se não existir, cria a nova pasta
        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }


        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        Cursor linhas1 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'ARSP';", null);

        if(linhas1.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_ARSP.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            document.addTitle("ARSP");
            int ARSP = 1;

            if (linhas1.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("ARSP - Foto "+ARSP);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas1.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                        // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap imagemVirada;
                    if(imagemBitmap.getWidth() > imagemBitmap.getHeight()){
                        imagemVirada = RotateBitmap(imagemBitmap, 90);
                    }else{
                        imagemVirada = imagemBitmap;
                    }

                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.7), (int) (imagemVirada.getHeight() * 0.7), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    ARSP++;
                    document.newPage();
                }
                while (linhas1.moveToNext());
                linhas1.close();
                document.close();
            }
        }

        Cursor linhas2 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Autorização de Passagem';", null);

        if(linhas2.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Autorizaçao de Passagem.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int Autoriz = 1;
            document.addTitle("Autorização de Passagem");
            if (linhas2.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Autorização de Passagem - Foto "+Autoriz);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas2.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    Autoriz++;
                    document.newPage();
                }
                while (linhas2.moveToNext());
                linhas2.close();
                document.close();
            }
        }

        Cursor linhas3 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Croqui';", null);

        if(linhas3.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Croqui.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int croqui = 1;
            document.addTitle("Croqui");
            if (linhas3.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Croqui - Foto "+croqui);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas3.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    croqui++;
                    document.newPage();
                }
                while (linhas3.moveToNext());
                linhas3.close();
                document.close();
            }
        }

        Cursor linhas4 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Escritura ou Contrato';", null);

        if(linhas4.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Escritura ou Contrato.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int escritura = 1;
            document.addTitle("Escritura ou Contrato");
            if (linhas4.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Escritura ou Contrato - Foto "+escritura);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas4.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    escritura++;
                    document.newPage();
                }
                while (linhas4.moveToNext());
                linhas4.close();
                document.close();
            }
        }

        Cursor linhas5 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Levantamento de Carga';", null);

        if(linhas5.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Levantamento de Carga.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int levantamento = 1;
            document.addTitle("Levantamento de Carga");
            if (linhas5.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Levantamento de Carga - Poste "+levantamento);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas5.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    levantamento++;
                    document.newPage();
                }
                while (linhas5.moveToNext());
                linhas5.close();
                document.close();
            }
        }

        Cursor linhas6 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Notificação';", null);

        if(linhas6.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Notificacao.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int notificacao = 1;
            document.addTitle("Notificação");
            if (linhas6.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Notificação - Foto "+notificacao);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas6.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    notificacao++;
                    document.newPage();
                }
                while (linhas6.moveToNext());
                linhas6.close();
                document.close();
            }
        }

        db.close();
    }

    public void createPdfImagensTeste() throws IOException, DocumentException {

        // Verifica se a pasta HCC já existe no smartfone, se não existir, cria a pasta
        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/HCC");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }

        // Verifica se a pasta com o numero da nota ja existe dentro da pasta HCC
        // Se não existir, cria a nova pasta
        File docsFolder2 = new File(Environment.getExternalStorageDirectory() + "/HCC/" + notaLocacao);
        if (!docsFolder2.exists()) {
            docsFolder2.mkdir();
        }


        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        Cursor linhas2 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Autorização de Passagem';", null);

        if(linhas2.getCount() != 0) {
            String FILE = Environment.getExternalStorageDirectory().toString()
                    + "/PDF/" + "Name.pdf";

            Document document = new Document(PageSize.A4);
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/PDF");
            myDir.mkdirs();
            PdfWriter.getInstance(document, new FileOutputStream(FILE));

            document.open();
            if (linhas2.moveToFirst()) {
                do {
                    addMetaData(document);
                    addTitlePage(document);
                }
                while (linhas2.moveToNext());
                linhas2.close();
            }
            document.close();
        }

        Cursor linhas3 = db.rawQuery("SELECT ID, IMAGEM FROM " + TABELADOCUMENTO + " WHERE IDLOCACAO = " + idLocacao + " AND NOMEDOCUMENTO = 'Croqui';", null);

        if(linhas3.getCount() != 0) {
            // Criando o arquivo PDF, com o n da nota como nome do arquivo
            pdfFile = new File(docsFolder2.getAbsolutePath(), notaLocacao + "_Croqui.pdf");
            OutputStream output = new FileOutputStream(pdfFile);
            Document document = new Document(PageSize.A4, 0, 0, 0, 0);
            PdfWriter.getInstance(document, output);

            int croqui = 1;
            document.addTitle("Croqui");
            if (linhas3.moveToFirst()) {
                document.open();
                do {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.moldecima);
                    Bitmap tamanhoReduzidoParaCima = Bitmap.createScaledBitmap(bitmap, 589, 80, true);
                    tamanhoReduzidoParaCima.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Image myImg = Image.getInstance(stream.toByteArray());
                    myImg.setAlignment(Image.MIDDLE);

                    document.add(myImg);

                    Font titleFont = new Font(Font.FontFamily.COURIER, 20, Font.BOLD);
                    Paragraph paragrafo1 = new Paragraph("Croqui - Foto " + croqui);
                    paragrafo1.setFont(titleFont);
                    paragrafo1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragrafo1);

                    Paragraph paragrafo3 = new Paragraph("\n");

                    byte[] imagemBase64 = linhas3.getBlob(1);

                    if (imagemBase64 != null) {
                        String imagem64 = new String(imagemBase64, "UTF-8");
                    }

                    // Converte a base64 para bitmap, configurando a imagem(tamanho, alinhamento, etc)
                    ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
                    Bitmap imagemBitmap = decodeBase64(imagemBase64);
                    Bitmap novoTamanho = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.53), (int) (imagemBitmap.getHeight() * 0.53), true);// tava 0.71
                    novoTamanho.compress(Bitmap.CompressFormat.JPEG, 100, stream3);
                    Image myImg3 = Image.getInstance(stream3.toByteArray());
                    myImg3.setAlignment(Image.ALIGN_CENTER);

                    document.add(myImg3);
                    document.add(paragrafo3);

                    croqui++;
                    document.newPage();
                }
                while (linhas3.moveToNext());
                linhas3.close();
                document.close();
            }
        }

        db.close();
    }

    // Set PDF document Properties
    public void addMetaData(Document document) {
        document.addTitle("RESUME");
        document.addSubject("Person Info");
        document.addKeywords("Personal,	Education, Skills");
        document.addAuthor("TAG");
        document.addCreator("TAG");
    }

    public void addTitlePage(Document document) throws DocumentException {
        // Font Style for Document
        Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD
                | Font.UNDERLINE, BaseColor.GRAY);
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

        // Start New Paragraph
        Paragraph prHead = new Paragraph();
        // Set Font in this Paragraph
        prHead.setFont(titleFont);
        // Add item into Paragraph
        prHead.add("RESUME – Name\n");

        // Create Table into Document with 1 Row
        PdfPTable myTable = new PdfPTable(1);
        // 100.0f mean width of table is same as Document size
        myTable.setWidthPercentage(100.0f);

        // Create New Cell into Table
        PdfPCell myCell = new PdfPCell(new Paragraph(""));
        myCell.setBorder(Rectangle.BOTTOM);

        // Add Cell into Table
        myTable.addCell(myCell);

        prHead.setFont(catFont);
        prHead.add("\nName1 Name2\n");
        prHead.setAlignment(Element.ALIGN_CENTER);

        // Add all above details into Document
        document.add(prHead);
        document.add(myTable);

        document.add(myTable);

        // Now Start another New Paragraph
        Paragraph prPersinalInfo = new Paragraph();
        prPersinalInfo.setFont(smallBold);
        prPersinalInfo.add("Address 1\n");
        prPersinalInfo.add("Address 2\n");
        prPersinalInfo.add("City: SanFran. State: CA\n");
        prPersinalInfo.add("Country: USA Zip Code: 000001\n");
        prPersinalInfo
                .add("Mobile: 9999999999 Fax: 1111111 Email: john_pit@gmail.com \n");

        prPersinalInfo.setAlignment(Element.ALIGN_CENTER);

        document.add(prPersinalInfo);
        document.add(myTable);

        document.add(myTable);

        Paragraph prProfile = new Paragraph();
        prProfile.setFont(smallBold);
        prProfile.add("\n \n Profile : \n ");
        prProfile.setFont(normal);
        prProfile
                .add("\nI am Mr. XYZ. I am Android Application Developer at TAG.");

        prProfile.setFont(smallBold);
        document.add(prProfile);

        // Create new Page in PDF
        document.newPage();
    }

    // Metodo que decodifica para base64
    public static Bitmap decodeBase64(byte[] input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void inseriImagemVisitaTecnica(View v) {
        if (idLocacao == null) {
            Toast.makeText(GerenciarLocacoes.this, "Por Favor! Cadastre ou selecione uma Solicitação para poder inserir uma Visita Técnica!", Toast.LENGTH_SHORT).show();
        } else {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GerenciarLocacoes.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        // Verifica versão
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // versão superior a M
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            outputUri = getTempCameraUri(); // busca caminho via URI

            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            startActivityForResult(intent, 1);
        } else { // versão inferior a M
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (it.resolveActivity(getPackageManager()) != null) {
                File arquivoFoto = null;
                try {
                    arquivoFoto = criaArquivoImagem(); //Cria caminho via
                } catch (IOException ex) {

                }
                if (arquivoFoto != null) {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                    startActivityForResult(it, 0);
                }
            }
        }
        }
    }
/*
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(Localizacao.this, "Desculpe!!! você não pode usar este aplicativo sem conceder permissão!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                if (outputUri == null) {
                    return;
                }
                if (outputUri != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputUri);
                        Bitmap tamanhoReduzidoImagem;
                        tamanhoReduzidoImagem = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.2), (int) (bitmap.getHeight() * 0.2), true);
                        if (tamanhoReduzidoImagem.getWidth() > tamanhoReduzidoImagem.getHeight()) {
                            tamanhoReduzidoImagem = RotateBitmap(tamanhoReduzidoImagem, 90);
                        }
                        imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                        salvaFotoPosteBanco();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Você NÃO inseriu a foto!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (resultCode == RESULT_OK) {
                File arquivoImagem = new File(caminhoImagem);
                if (arquivoImagem.exists()) {
                    Bitmap imagemBitmap = BitmapFactory.decodeFile(
                            arquivoImagem.getAbsolutePath());

                    Bitmap tamanhoReduzidoImagem, imagemVirada;
                    imagemVirada = RotateBitmap(imagemBitmap, 90);
                    tamanhoReduzidoImagem = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.2), (int) (imagemVirada.getHeight() * 0.2), true);
                    imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                    salvaFotoPosteBanco();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Você NÃO inseriu a foto!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri getTempCameraUri() {
        try {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            // Cria o nome do arquivo com numeroDaNota+NomeDoDocumento+horarioDoSistema
            String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);
            String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nomeArquivoImagem = "Visita_Tecnica" + numeroNotaSelect + "_" + horarioSistema + "_";
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

            // Verifica se a pasta HCC existe, caso não exista, crie o diretório
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            // Verifica se a pasta com o numero da nota existe, caso não exista, cria o diretório dentro da pasta HCC
            String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
            File dir2 = new File(path2);
            if (!dir2.exists())
                dir2.mkdirs();

            // Salva a imagem no caminho criado
            fileFinal = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
            Uri photoURI2 = FileProvider.getUriForFile(GerenciarLocacoes.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    fileFinal);

            return photoURI2;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método que transforma a imagem em byte[]
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // Método que gira a imagem em 90 graus

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Salva os dados da imagem no banco de dados
    private void salvaFotoPosteBanco() {
        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        db.execSQL("INSERT INTO " + TABELAFOTOSVISITATECNICA + "(IMAGEM, IDLOCACAO) VALUES ('" + imgString + "','" + idLocacao + "')");
        db.close();
        inflaListaFotosPostes();
        Toast.makeText(getApplicationContext(), "Inserção realizada com sucesso!", Toast.LENGTH_SHORT).show();
    }

    //Gera lista de postes
    private void inflaListaFotosPostes() {
        itensVisita = new ArrayList<DadosGerais>();

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        // Busca quantidade de fotos na tabela FotosPoste
        Cursor linhas = db.rawQuery("SELECT ID FROM " + TABELAFOTOSVISITATECNICA + " WHERE IDLOCACAO = " + idLocacao + ";", null);
        int contador = 1;
        if (linhas.moveToFirst()) {
            do {
                String idPoste = linhas.getString(0);
                String texto = "Foto " + contador;

                DadosGerais item1 = new DadosGerais(idPoste, texto);
                itensVisita.add(item1);
                contador++;
            }
            while (linhas.moveToNext());
        }
        adapterListViewVisitaTecnica = new AdapterListViewVisitaTecnica(this, itensVisita);
        listViewVisita.setAdapter(adapterListViewVisitaTecnica);
        listViewVisita.setCacheColorHint(Color.TRANSPARENT);

        linhas.close();
        // Aumenta a quantidade de linhas de acordo com o que tem no banco de dados
        Helper.getListViewSize(listViewVisita);
        db.close();
    }

    // Método que cria o caminho para armazenar a imagem
    private File criaArquivoImagem() throws IOException {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);
        String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomeArquivoImagem = "Visita_Tecnica" + numeroNotaSelect + "_" + horarioSistema + "_";

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

        // Verifica se a pasta da HCC existe, caso não exista, cria o diretório
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        // Veriifica se a pasta com o numero da nota da locação existe, caso não exista, cria o diretório dentro da pasta da HCC
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
        File dir2 = new File(path2);
        if (!dir2.exists())
            dir2.mkdirs();

        File imagem = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
        caminhoImagem = imagem.getAbsolutePath();

        return imagem;
    }

}
