package br.desenvolvedor.michelatz.aplicativohcc;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CadastrarExtruturas extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String vaiEditar = "0", numeroPlaca =" ", selectedIdTipoEstrutura = " ", veioEdicao="0";
    private String telefonia1, telefonia2, telefonia3, telefonia4, vidaUtil, estai, selectedIdAltura, selectedIdIluminacao;
    private String selectedIdAcesso, TipoPosteSelecionado, CapacidadeSelecionado, TipoSoloSelecionado, idConsumidor, idPoste;

    private Spinner spnCruzeta;
    ArrayList<String> tiposCruzetaNovo = new ArrayList<String>();

    private RadioGroup grupClasseEstrutura, grupNivelEstrutura;
    private RadioButton radioClasse15Estrutura, radioClasse25Estrutura, radioClasse36Estrutura, radioClassePEstrutura;
    private RadioButton radioNivel1Estrutura, radioNivel2Estrutura, radioNivel3Estrutura, radioNivel4Estrutura;

    private TextView textDescricaoEstrutura;
    private TextView textoClasse, textoNivel, textoCruzeta;
    private RadioButton radioPrimaria, radioSecundaria;

    SQLiteDatabase db;
    String BANCO = "banco.db", TABELAESTRUTURA = "estrutura";
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastra_extrutura);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Inserção de Estrutura");

        //Supote ao Toobar(Apresenta Icone voltar, na barra de cima da tela)
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        textDescricaoEstrutura = (EditText) findViewById(R.id.textDescricaoEstrutura);
        radioPrimaria = (RadioButton) findViewById(R.id.radioPrimaria);
        radioSecundaria = (RadioButton) findViewById(R.id.radioSecundaria);

        textoClasse = (TextView) findViewById(R.id.textoClasse);
        textoNivel = (TextView) findViewById(R.id.textoNivel);
        textoCruzeta = (TextView) findViewById(R.id.textoCruzeta);

        grupClasseEstrutura = (RadioGroup) findViewById(R.id.grupClasseEstrutura);
        grupNivelEstrutura = (RadioGroup) findViewById(R.id.grupNivelEstrutura);

        radioClasse15Estrutura = (RadioButton) findViewById(R.id.radioClasse15Estrutura);
        radioClasse25Estrutura = (RadioButton) findViewById(R.id.radioClasse25Estrutura);
        radioClasse36Estrutura = (RadioButton) findViewById(R.id.radioClasse36Estrutura);
        radioClassePEstrutura = (RadioButton) findViewById(R.id.radioClassePEstrutura);

        radioNivel1Estrutura = (RadioButton) findViewById(R.id.radioNivel1Estrutura);
        radioNivel2Estrutura = (RadioButton) findViewById(R.id.radioNivel2Estrutura);
        radioNivel3Estrutura = (RadioButton) findViewById(R.id.radioNivel3Estrutura);
        radioNivel4Estrutura = (RadioButton) findViewById(R.id.radioNivel4Estrutura);

        spnCruzeta = (Spinner) findViewById(R.id.spnCruzeta);

        tiposCruzetaNovo.add("Selecione a Cruzeta");
        tiposCruzetaNovo.add("Madeira");
        tiposCruzetaNovo.add("Polimérica");
        tiposCruzetaNovo.add("Metálica");
        tiposCruzetaNovo.add("Concreto");

        adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,tiposCruzetaNovo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCruzeta.setAdapter(adapter);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // Verifica se existe dados vindos da tela anterior
        if (getIntent().getStringExtra("USERTELA") != null){
            numeroPlaca = bundle.getString("numeroPlaca");
            telefonia1 = bundle.getString("telefonia1");
            telefonia2 = bundle.getString("telefonia2");
            telefonia3 = bundle.getString("telefonia3");
            telefonia4 = bundle.getString("telefonia4");
            estai = bundle.getString("estai");
            selectedIdAltura = bundle.getString("selectedIdAltura");
            selectedIdIluminacao = bundle.getString("selectedIdIluminacao");
            selectedIdAcesso = bundle.getString("selectedIdAcesso");
            TipoPosteSelecionado = bundle.getString("TipoPosteSelecionado");
            CapacidadeSelecionado = bundle.getString("CapacidadeSelecionado");
            TipoSoloSelecionado = bundle.getString("TipoSoloSelecionado");
            vidaUtil = bundle.getString("vidautil");

            // Verifica se a tela anterior estava sendo editada
            if (getIntent().getStringExtra("edit")!= null && getIntent().getStringExtra("edit").equals("1")){
                veioEdicao = "1";
            }

            // Verifica se esta estrutura vai ser editada
            if (getIntent().getStringExtra("USERTELA").equals("EDITAR")){// Se sim, busca dados de edição
                vaiEditar = "1";
                idConsumidor = bundle.getString("id");
                preencheDadosEdicaoExtrutura(idConsumidor);
            }
        }

    }

    // Metodo responsavel em buscar os dados da Estrutura no banco e inseri-los nos seus respctivos campos
    private void preencheDadosEdicaoExtrutura(String idConsumidor) {
        String tipo = null;
        String descricao = null;
        String classe = null;
        String nivelExtrut = null;
        String cruzeta = null;

        String cruzetaSelec = null;

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        // Select que busca dados da estrutra
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELAESTRUTURA + " WHERE ID = " + idConsumidor + ";", null);
        if (linhas.moveToFirst()) {
            do {
                //coloca os dados nas suas respectivas variaveis
                tipo = linhas.getString(1);
                descricao = linhas.getString(2);
                classe = linhas.getString(3);
                nivelExtrut = linhas.getString(4);
                cruzeta = linhas.getString(5);
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        // Verifica se o tipo é nulo
        if (tipo != null) { // se não, verifica qual tipo é(Primaria, secundaria), e seta seu respectivo radioButton
            if (tipo.equals("Primaria")) {
                radioPrimaria.setChecked(true);
                selectedIdTipoEstrutura = "Primaria";
                if (descricao != null) { // Verifica se a drecricao é nula, se não é nula, seta o texto no editText
                    textDescricaoEstrutura.setText(descricao);
                }

                if (classe != null) {
                    if (classe.equals("15")) {
                        grupClasseEstrutura.check(R.id.radioClasse15Estrutura);

                    } else if (classe.equals("25")) {
                        grupClasseEstrutura.check(R.id.radioClasse25Estrutura);

                    } else if (classe.equals("36")) {
                        grupClasseEstrutura.check(R.id.radioClasse36Estrutura);

                    } else if (classe.equals("P")) {
                        grupClasseEstrutura.check(R.id.radioClassePEstrutura);
                    }
                }

                if (nivelExtrut != null) {
                    if (nivelExtrut.equals("1")) {
                        grupNivelEstrutura.check(R.id.radioNivel1Estrutura);

                    } else if (nivelExtrut.equals("2")) {
                        grupNivelEstrutura.check(R.id.radioNivel2Estrutura);

                    } else if (nivelExtrut.equals("3")) {
                        grupNivelEstrutura.check(R.id.radioNivel3Estrutura);

                    } else if (nivelExtrut.equals("4")) {
                        grupNivelEstrutura.check(R.id.radioNivel4Estrutura);
                    }
                }

                int spinnerPosition;
                if (cruzeta != null) {
                    if (cruzeta.equals("")) {
                        cruzetaSelec = "Selecione um Tipo";
                        spinnerPosition = adapter.getPosition(cruzetaSelec);
                    } else {
                        spinnerPosition = adapter.getPosition(cruzeta);
                    }
                    spnCruzeta.setSelection(spinnerPosition);
                }

            } else if (tipo.equals("Secundaria")) {
                radioSecundaria.setChecked(true);
                selectedIdTipoEstrutura = "Secundaria";
                grupClasseEstrutura.setVisibility(View.GONE);
                grupNivelEstrutura.setVisibility(View.GONE);
                spnCruzeta.setVisibility(View.GONE);
                textoClasse.setVisibility(View.GONE);
                textoCruzeta.setVisibility(View.GONE);
                textoNivel.setVisibility(View.GONE);

                if (descricao != null) { // Verifica se a drecricao é nula, se não é nula, seta o texto no editText
                    textDescricaoEstrutura.setText(descricao);
                }
                grupClasseEstrutura.setEnabled(false);
                grupNivelEstrutura.setEnabled(false);
                spnCruzeta.setEnabled(false);
            }
        }
    }

    //Metodo que pega ação do botão voltar no toolbar bem em cima da tela
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
                alertDialogBuilder.setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                //Verifica se a pagina anterior estava sendo editada
                                if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
                                    Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                    intent.putExtra("USERTELA","5");
                                    intent.putExtra("numeroPlaca",numeroPlaca);
                                    intent.putExtra("telefonia1",telefonia1);
                                    intent.putExtra("telefonia2",telefonia2);
                                    intent.putExtra("telefonia3",telefonia3);
                                    intent.putExtra("telefonia4",telefonia4);
                                    intent.putExtra("estai",estai);
                                    intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                    intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                    intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                    intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                    intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                    intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                    intent.putExtra("vidautil",vidaUtil);
                                    intent.putExtra("edit","1");
                                    CadastrarExtruturas.this.startActivity(intent);
                                    finish();
                                }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
                                    Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                    intent.putExtra("USERTELA","5");
                                    intent.putExtra("numeroPlaca",numeroPlaca);
                                    intent.putExtra("telefonia1",telefonia1);
                                    intent.putExtra("telefonia2",telefonia2);
                                    intent.putExtra("telefonia3",telefonia3);
                                    intent.putExtra("telefonia4",telefonia4);
                                    intent.putExtra("estai",estai);
                                    intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                    intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                    intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                    intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                    intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                    intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                    intent.putExtra("vidautil",vidaUtil);

                                    CadastrarExtruturas.this.startActivity(intent);
                                    finish();
                                }
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
                break;
            default:break;
        }
        return true;
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
            alertDialogBuilder.setPositiveButton("Sim",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            //Verifica se a pagina anterior estava sendo editada
                            if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
                                Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                intent.putExtra("USERTELA","5");
                                intent.putExtra("numeroPlaca",numeroPlaca);
                                intent.putExtra("telefonia1",telefonia1);
                                intent.putExtra("telefonia2",telefonia2);
                                intent.putExtra("telefonia3",telefonia3);
                                intent.putExtra("telefonia4",telefonia4);
                                intent.putExtra("estai",estai);
                                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                intent.putExtra("vidautil",vidaUtil);
                                intent.putExtra("edit","1");
                                CadastrarExtruturas.this.startActivity(intent);
                                finish();
                            }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
                                Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                                intent.putExtra("USERTELA","5");
                                intent.putExtra("numeroPlaca",numeroPlaca);
                                intent.putExtra("telefonia1",telefonia1);
                                intent.putExtra("telefonia2",telefonia2);
                                intent.putExtra("telefonia3",telefonia3);
                                intent.putExtra("telefonia4",telefonia4);
                                intent.putExtra("estai",estai);
                                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                                intent.putExtra("vidautil",vidaUtil);
                                CadastrarExtruturas.this.startActivity(intent);
                                finish();
                            }
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
    }

    // Navegação no menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_locacoes) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idPosteKey");
            editor.commit();
            editor.clear();
            Intent it;
            it = new Intent(this, GerenciarLocacoes.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_locacoesfinalizadas) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("idPosteKey");
            editor.commit();
            editor.clear();
            Intent it;
            it = new Intent(this, ListaLocacoesFinalizadas.class);
            startActivity(it);
            finish();
        } else if (id == R.id.nav_sair) {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("numeroNotaKey");
            editor.remove("idPosteKey");
            editor.remove("idLocacaoKey");
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

    // Pega ação do botão "Salvar" da tela
    public void salvarEstrutura(View v){

        String descricaoEstru = "";
        int selectedIdClasse = -1;
        int selectedIdNivel = -1;

        descricaoEstru = textDescricaoEstrutura.getText().toString();
        selectedIdClasse = grupClasseEstrutura.getCheckedRadioButtonId();
        selectedIdNivel = grupNivelEstrutura.getCheckedRadioButtonId();

        if(selectedIdTipoEstrutura.equals(" ") || descricaoEstru.equals("")){
            Toast.makeText(CadastrarExtruturas.this, "Por Favor! Insira os dados Obrigatórios!", Toast.LENGTH_SHORT).show();
        }else{
            // Chama o metodo responsavel por salvar no banco os dados inseridos
            salvarEstruturaBanco();

            //Verifica se a pagina anterior estava sendo editada
            if(veioEdicao.equals("1")){ // se sim, manda um putExtra(edit) avisando para continuar a edição
                Intent intent = new Intent(CadastrarExtruturas.this, InseriPoste.class);
                intent.putExtra("USERTELA","5");
                intent.putExtra("numeroPlaca",numeroPlaca);
                intent.putExtra("telefonia1",telefonia1);
                intent.putExtra("telefonia2",telefonia2);
                intent.putExtra("telefonia3",telefonia3);
                intent.putExtra("telefonia4",telefonia4);
                intent.putExtra("estai",estai);
                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                intent.putExtra("vidautil",vidaUtil);
                intent.putExtra("edit","1");
                CadastrarExtruturas.this.startActivity(intent);
                finish();
            }else{ // se não, envia os dados que estavam sendo inseridos(Caso hajam)
                Intent intent = new Intent(this, InseriPoste.class);
                intent.putExtra("USERTELA","5");
                intent.putExtra("numeroPlaca",numeroPlaca);
                intent.putExtra("telefonia1",telefonia1);
                intent.putExtra("telefonia2",telefonia1);
                intent.putExtra("telefonia3",telefonia3);
                intent.putExtra("telefonia4",telefonia4);
                intent.putExtra("estai",estai);
                intent.putExtra("selectedIdAltura",String.valueOf(selectedIdAltura));
                intent.putExtra("selectedIdIluminacao",String.valueOf(selectedIdIluminacao));
                intent.putExtra("selectedIdAcesso", selectedIdAcesso);
                intent.putExtra("TipoPosteSelecionado",TipoPosteSelecionado);
                intent.putExtra("CapacidadeSelecionado",CapacidadeSelecionado);
                intent.putExtra("TipoSoloSelecionado",TipoSoloSelecionado);
                intent.putExtra("vidautil",vidaUtil);
                this.startActivity(intent);
                finish();
            }
        }
    }

    // Método responsavel por inserir os novos dados no banco
    public void salvarEstruturaBanco(){

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idPoste = sharedpreferences.getString("idPosteKey", null);
        String descricaoEstru = textDescricaoEstrutura.getText().toString();
        descricaoEstru = descricaoEstru.toUpperCase();

        int selectedIdClasse = grupClasseEstrutura.getCheckedRadioButtonId();
        int selectedIdNivel = grupNivelEstrutura.getCheckedRadioButtonId();
        String classeSelect = "";
        String nivelSelect = "";

        //Log.d("resposta", String.valueOf(selectedIdClasse));
        //Log.d("resposta", String.valueOf(selectedIdNivel));

        if (selectedIdClasse == radioClasse15Estrutura.getId()) {
            classeSelect = "15";
        } else if (selectedIdClasse == radioClasse25Estrutura.getId()) {
            classeSelect = "25";
        } else if (selectedIdClasse == radioClasse36Estrutura.getId()) {
            classeSelect = "36";
        } else if (selectedIdClasse == radioClassePEstrutura.getId()) {
            classeSelect = "P";
        }

        if (selectedIdNivel == radioNivel1Estrutura.getId()) {
            nivelSelect = "1";
        } else if (selectedIdNivel == radioNivel2Estrutura.getId()) {
            nivelSelect = "2";
        } else if (selectedIdNivel == radioNivel3Estrutura.getId()) {
            nivelSelect = "3";
        } else if (selectedIdNivel == radioNivel4Estrutura.getId()) {
            nivelSelect = "4";
        }

        String TipoCruzetaSelecionado = "";
        if (spnCruzeta.getSelectedItem().toString().equals("Selecione um Tipo")) {
            TipoCruzetaSelecionado = "";
        } else {
            TipoCruzetaSelecionado = spnCruzeta.getSelectedItem().toString();
        }

        //Log.d("resposta", String.valueOf(selectedIdClasse));
        //Log.d("resposta", nivelSelect);

        // Verifica se é uma edição
        if(vaiEditar.equals("1")){ // se sim, usa um update
            db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
            ContentValues values = new ContentValues();
            values.put("TIPO", selectedIdTipoEstrutura);
            values.put("DESCRICAO", descricaoEstru);
            if(selectedIdTipoEstrutura.equals("Primaria")){
                values.put("CLASSE", classeSelect);
                values.put("NIVEL", nivelSelect);
                values.put("CRUZETA", TipoCruzetaSelecionado);
            } else if(selectedIdTipoEstrutura.equals("Secundaria")){
                values.put("CLASSE", "");
                values.put("NIVEL", "");
                values.put("CRUZETA", "");
            }
            db.update(TABELAESTRUTURA, values, "ID=" + idConsumidor, null);
            db.close();
        }else{ // se não, usa um insert
            if (selectedIdTipoEstrutura.equals("Primaria")) {
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("TIPO", selectedIdTipoEstrutura);
                values.put("DESCRICAO", descricaoEstru);
                values.put("CLASSE", classeSelect);
                values.put("NIVEL", nivelSelect);
                values.put("CRUZETA", TipoCruzetaSelecionado);
                values.put("IDPOSTE", idPoste);
                db.insert(TABELAESTRUTURA, null, values);
                db.close();
            } else if (selectedIdTipoEstrutura.equals("Secundaria")) {
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("TIPO", selectedIdTipoEstrutura);
                values.put("DESCRICAO", descricaoEstru);
                values.put("CLASSE", "");
                values.put("NIVEL", "");
                values.put("CRUZETA", "");
                values.put("IDPOSTE", idPoste);
                db.insert(TABELAESTRUTURA, null, values);
                db.close();
            }
        }
    }

    // Pega ação do clique no RadioButton Primaria
    public void clicouPrimaria(View v){
        selectedIdTipoEstrutura = "Primaria";
        grupClasseEstrutura.setVisibility(View.VISIBLE);
        grupNivelEstrutura.setVisibility(View.VISIBLE);
        spnCruzeta.setVisibility(View.VISIBLE);
        textoClasse.setVisibility(View.VISIBLE);
        textoCruzeta.setVisibility(View.VISIBLE);
        textoNivel.setVisibility(View.VISIBLE);

        grupClasseEstrutura.setEnabled(true);
        grupNivelEstrutura.setEnabled(true);
        spnCruzeta.setEnabled(true);
    }

    // Pega ação do clique no RadioButton Secundaria
    public void clicouSecundaria(View v){
        selectedIdTipoEstrutura = "Secundaria";
        grupClasseEstrutura.setVisibility(View.GONE);
        grupNivelEstrutura.setVisibility(View.GONE);
        spnCruzeta.setVisibility(View.GONE);
        textoClasse.setVisibility(View.GONE);
        textoCruzeta.setVisibility(View.GONE);
        textoNivel.setVisibility(View.GONE);

        grupClasseEstrutura.setEnabled(false);
        grupNivelEstrutura.setEnabled(false);
        spnCruzeta.setEnabled(false);
    }

}
