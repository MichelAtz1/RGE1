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
import java.util.StringTokenizer;

public class CadastrarConsumidores extends AppCompatActivity {

    private RadioGroup grupLado;
    private RadioButton radioDireita, radioEsquerda;
    private Spinner spnD10, spnQ10, spnW01, spnS03, spnQ25, spnW02, spnAF, spnQ35, spnW031, spnW032, spnW033;
    ArrayList<String> spn1 = new ArrayList<String>();

    private RadioGroup  grupTipo, grupClasse, grupRamal, grupNumeroFases, grupFaseamento;
    private RadioButton radioIndustrial, radioComercial, radioResidencial;
    private RadioButton radioClasseA, radioClasseB , radioClasseC;
    private RadioButton D10, T10, Q10, S01, S02, S03, Q16, Q25, Q35, W01, W02, W03, W35;
    private RadioButton AN, BN, CN, ABN, ACN, BCN, ABCN;
    private RadioButton radioFase1, radioFase2, radioFase3;
    private String selectedIdRamal = " ";
    private String selectedIdFase = " ";
    private TextView textNumeroCasa, textMedidor;

    SQLiteDatabase db;
    String BANCO = "banco.db";
    String TABELACONSUMIDOR = "consumidor";
    private String vaiEditar = "0", veioEdicao = "0";
    ArrayAdapter adapter;
    private String tipoEquipamento, numeroPlacaEquipamento, tensao, descricao, idConsumidor, idLocacao, idPoste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_consumidores);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Aplicativo HCC");
        toolbar.setSubtitle("Dados do Consumidores");

        //Supote ao Toobar(Apresenta Icone voltar, na barra de cima da tela)
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        textNumeroCasa = (TextView) findViewById(R.id.textNumeroCasa);
        textMedidor = (TextView) findViewById(R.id.textMedidor);

        grupLado = (RadioGroup) findViewById(R.id.grupLado);
        radioDireita = (RadioButton) findViewById(R.id.radioDireita);
        radioEsquerda = (RadioButton) findViewById(R.id.radioEsquerda);

        grupTipo = (RadioGroup) findViewById(R.id.grupTipo);
        grupClasse = (RadioGroup) findViewById(R.id.grupClasse);
        grupRamal = (RadioGroup) findViewById(R.id.grupRamal);
        grupFaseamento = (RadioGroup) findViewById(R.id.grupFaseamento);
        //grupNumeroFases = (RadioGroup) findViewById(R.id.grupNumeroFases);

        radioIndustrial = (RadioButton) findViewById(R.id.radioIndustrial);
        radioComercial = (RadioButton) findViewById(R.id.radioComercial);
        radioResidencial = (RadioButton) findViewById(R.id.radioResidencial);

        radioClasseA = (RadioButton) findViewById(R.id.radioClasseA);
        radioClasseB = (RadioButton) findViewById(R.id.radioClasseB);
        radioClasseC = (RadioButton) findViewById(R.id.radioClasseC);

        //radioFase1 = (RadioButton) findViewById(R.id.radioFase1);
        //radioFase2 = (RadioButton) findViewById(R.id.radioFase2);
        //radioFase3 = (RadioButton) findViewById(R.id.radioFase3);

        D10 = (RadioButton) findViewById(R.id.radioD10);
        T10 = (RadioButton) findViewById(R.id.radioT10);
        Q10 = (RadioButton) findViewById(R.id.radioQ10);
        Q16 = (RadioButton) findViewById(R.id.radioQ16);
        Q25 = (RadioButton) findViewById(R.id.radioQ25);
        Q35 = (RadioButton) findViewById(R.id.radioQ35);
        S01 = (RadioButton) findViewById(R.id.radioS01);
        S02 = (RadioButton) findViewById(R.id.radioS02);
        S03 = (RadioButton) findViewById(R.id.radioS03);
        W01 = (RadioButton) findViewById(R.id.radioW01);
        W02 = (RadioButton) findViewById(R.id.radioW02);
        W03 = (RadioButton) findViewById(R.id.radioW03);
        W35 = (RadioButton) findViewById(R.id.radioW35);

        AN = (RadioButton) findViewById(R.id.radioAN);
        BN = (RadioButton) findViewById(R.id.radioBN);
        CN = (RadioButton) findViewById(R.id.radioCN);
        ABN = (RadioButton) findViewById(R.id.radioABN);
        ACN = (RadioButton) findViewById(R.id.radioACN);
        BCN = (RadioButton) findViewById(R.id.radioBCN);
        ABCN = (RadioButton) findViewById(R.id.radioABCN);

/*
        spnD10 = (Spinner) findViewById(R.id.spnD10);
        spnQ10 = (Spinner) findViewById(R.id.spnQ10);
        spnW01 = (Spinner) findViewById(R.id.spnW01);
        spnS03 = (Spinner) findViewById(R.id.spnS03);
        spnQ25 = (Spinner) findViewById(R.id.spnQ25);
        spnW02 = (Spinner) findViewById(R.id.spnW02);
        spnAF = (Spinner) findViewById(R.id.spnAF);
        spnQ35 = (Spinner) findViewById(R.id.spnQ35);
        spnW031 = (Spinner) findViewById(R.id.spnW031);
        spnW032 = (Spinner) findViewById(R.id.spnW032);
        spnW033 = (Spinner) findViewById(R.id.spnW033);

        spn1.add("0");
        spn1.add("1");
        spn1.add("2");
        spn1.add("3");
        spn1.add("4");
        spn1.add("5");
        spn1.add("6");
        spn1.add("7");
        spn1.add("8");
        spn1.add("9");
        spn1.add("10");

        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spn1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnD10.setAdapter(adapter);
        spnQ10.setAdapter(adapter);
        spnW01.setAdapter(adapter);
        spnS03.setAdapter(adapter);
        spnQ25.setAdapter(adapter);
        spnW02.setAdapter(adapter);
        spnAF.setAdapter(adapter);
        spnQ35.setAdapter(adapter);
        spnW031.setAdapter(adapter);
        spnW032.setAdapter(adapter);
        spnW033.setAdapter(adapter);
*/
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // Verifica se existe dados enviados da pagina anterior
        if (getIntent().getStringExtra("USERTELA") != null) {
            tipoEquipamento = bundle.getString("tipo");
            numeroPlacaEquipamento = bundle.getString("placa");
            tensao = bundle.getString("tensao");
            descricao = bundle.getString("descricao");

            // Verifica se veio para edição, se sim: variavel Vaieditar recebe 1 e é pego o id do Consumidor que será editado
            if (getIntent().getStringExtra("USERTELA").equals("EDITAR")) {
                vaiEditar = "1";
                idConsumidor = bundle.getString("id");
                preencheDadosEdicaoConsumidores(idConsumidor);
            }

            // Verifica se a pagina anterior estava sendo editada ou inserida uma nova
            // Se sim, variavel veiEdicao recebe 1 (pois qunado retornar a pagina anterior, avisar o sistema que aquela pagina era uma edição)
            if (getIntent().getStringExtra("edit") != null && getIntent().getStringExtra("edit").equals("1")) {
                veioEdicao = "1";
            }
        }
    }

    //Metodo que pega ação do botçao voltar, no toolbar bem em cima da tela
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
                                if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                                    Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                                    intent.putExtra("USERTELA", "EDITAR");
                                    CadastrarConsumidores.this.startActivity(intent);
                                    finish();
                                } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                                    Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                                    intent.putExtra("USERTELA", "10");
                                    intent.putExtra("tipo", tipoEquipamento);
                                    intent.putExtra("placa", numeroPlacaEquipamento);
                                    intent.putExtra("descricao", descricao);
                                    intent.putExtra("tensao", tensao);
                                    CadastrarConsumidores.this.startActivity(intent);
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
            default:
                break;
        }
        return true;
    }

    //Metodo responsavel em buscar no banco os dados do consumidor selecionado inseri-los nos seus devidos campos.
    public void preencheDadosEdicaoConsumidores(String idConsumidor) {
        String lado = null;
        String ramal = null;

        String tipo = null;
        String classe = null;
        String fase = null;
        String numCasa = null;
        String medidor = null;

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);

        //select que busca os dados na tabela Consumidor, utilizando ID do consumidor selecionado
        Cursor linhas = db.rawQuery("SELECT * FROM " + TABELACONSUMIDOR + " WHERE ID = " + idConsumidor + ";", null);
        if (linhas.moveToFirst()) {
            do {
                //Pega o lado(Direito ou Esquerdo) e o ramal do respectivo consumidor
                lado = linhas.getString(1);
                tipo = linhas.getString(2);
                classe = linhas.getString(3);
                ramal = linhas.getString(4);
                fase = linhas.getString(5);
                numCasa = linhas.getString(6);
                medidor = linhas.getString(7);
            }
            while (linhas.moveToNext());
            linhas.close();
        }
        db.close();

        //Verifica se o Lado não é nulo
        //Caso não seja nulo, verifica se é direito(D) ou esquerdo(E), deixando clicado seu respectivo radioButton
        if (lado != null) {
            if (lado.equals("E")) {
                grupLado.check(R.id.radioEsquerda);
            } else if (lado.equals("D")) {
                grupLado.check(R.id.radioDireita);
            }
        }

        if (numCasa != null) {
            if (numCasa.equals("-")) {
                textNumeroCasa.setText("");
            }else{
                textNumeroCasa.setText(numCasa);
            }
        }

        if (medidor != null) {
            if (medidor.equals("-")) {
                textMedidor.setText("");
            }else{
                textMedidor.setText(medidor);
            }
        }

        if (tipo != null) {
            if (tipo.equals("I")) {
                grupTipo.check(R.id.radioIndustrial);
            } else if (tipo.equals("C")) {
                grupTipo.check(R.id.radioComercial);
            } else if (tipo.equals("R")) {
                grupTipo.check(R.id.radioResidencial);
            }
        }

        if (classe != null) {
            if (classe.equals("A")) {
                grupClasse.check(R.id.radioClasseA);
            } else if (classe.equals("B")) {
                grupClasse.check(R.id.radioClasseB);
            } else if (classe.equals("C")) {
                grupClasse.check(R.id.radioClasseC);
            }
        }

        if (ramal != null) {

            if (ramal.equals("D10")) {
                selectedIdRamal="D10";
                grupRamal.check(R.id.radioD10);

            } else if (ramal.equals("T10")) {
                selectedIdRamal="T10";
                grupRamal.check(R.id.radioT10);

            } else if (ramal.equals("Q10")) {
                selectedIdRamal="Q10";
                grupRamal.check(R.id.radioQ10);

            } else if (ramal.equals("Q16")) {
                selectedIdRamal="Q16";
                grupRamal.check(R.id.radioQ16);

            } else if (ramal.equals("Q25")) {
                selectedIdRamal="Q25";
                grupRamal.check(R.id.radioQ25);

            } else if (ramal.equals("Q35")) {
                selectedIdRamal="Q35";
                grupRamal.check(R.id.radioQ35);

            } else if (ramal.equals("W10")) {
                selectedIdRamal="W10";
                grupRamal.check(R.id.radioW01);

            } else if (ramal.equals("W16")) {
                selectedIdRamal="W16";
                grupRamal.check(R.id.radioW02);

            } else if (ramal.equals("W25")) {
                selectedIdRamal="W25";
                grupRamal.check(R.id.radioW03);

            } else if (ramal.equals("W35")) {
                selectedIdRamal="W35";
                grupRamal.check(R.id.radioW35);

            } else if (ramal.equals("S01")) {
                selectedIdRamal="S01";
                grupRamal.check(R.id.radioS01);

            } else if (ramal.equals("S02")) {
                selectedIdRamal="S02";
                grupRamal.check(R.id.radioS02);

            } else if (ramal.equals("S03")) {
                selectedIdRamal="S03";
                grupRamal.check(R.id.radioS03);

            }
        }

        if (fase != null) {

            if (fase.equals("AN")) {
                selectedIdFase="AN";
                grupFaseamento.check(R.id.radioAN);

            } else if (fase.equals("BN")) {
                selectedIdFase="BN";
                grupFaseamento.check(R.id.radioBN);

            } else if (fase.equals("CN")) {
                selectedIdFase="CN";
                grupFaseamento.check(R.id.radioCN);

            } else if (fase.equals("ABN")) {
                selectedIdFase="ABN";
                grupFaseamento.check(R.id.radioABN);

            } else if (fase.equals("ACN")) {
                selectedIdFase="ACN";
                grupFaseamento.check(R.id.radioACN);

            } else if (fase.equals("BCN")) {
                selectedIdFase="BCN";
                grupFaseamento.check(R.id.radioBCN);

            } else if (fase.equals("ABCN")) {
                selectedIdFase="ABCN";
                grupFaseamento.check(R.id.radioABCN);

            }
        }
    }

    //Pega o clique do botão e chama o metodo de inserção no banco
    public void salvaConsumidor(View v) {
        salvaDadosConsumidor();
    }

    //Metodo que salva os dados no banco
    private void salvaDadosConsumidor() {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        idPoste = sharedpreferences.getString("idPosteKey", null);
        idLocacao = sharedpreferences.getString("idLocacaoKey", null);

        String lado = " ";
        String RamalCompleto = "";
        String tipo=" ";
        String classe =" ";
        String fase =" ";

        int selectedIdLado = grupLado.getCheckedRadioButtonId();
        int selectedIdTipo = grupTipo.getCheckedRadioButtonId();
        int selectedIdClasse = grupClasse.getCheckedRadioButtonId();
        //int selectedIdFases = grupNumeroFases.getCheckedRadioButtonId();

        String numCasa = "", medidor = "";

        if(textNumeroCasa.getText().toString().equals("")){
            numCasa = "-";
        }else{
            numCasa = textNumeroCasa.getText().toString();
        }

        if(textMedidor.getText().toString().equals("")){
            medidor = "-";
        }else{
            medidor = textMedidor.getText().toString();
        }

        if (selectedIdLado == radioDireita.getId()) {
            lado = "D";
        } else if (selectedIdLado == radioEsquerda.getId()) {
            lado = "E";
        }

        if (selectedIdTipo == radioIndustrial.getId()) {
            tipo = "I";
        } else if (selectedIdTipo == radioComercial.getId()) {
            tipo = "C";
        } else if (selectedIdTipo == radioResidencial.getId()) {
            tipo = "R";
        }

        if (selectedIdClasse == radioClasseA.getId()) {
            classe = "A";
        } else if (selectedIdClasse == radioClasseB.getId()) {
            classe = "B";
        } else if (selectedIdClasse == radioClasseC.getId()) {
            classe = "C";
        }


        //Verifica os itens obrigatórios
        if (lado.equals(" ") || tipo.equals(" ")|| classe.equals(" ")|| selectedIdRamal.equals(" ") || selectedIdFase.equals(" ")) {
            Toast.makeText(getApplicationContext(), "Por Favor! Insira os dados obrigatórios!!", Toast.LENGTH_SHORT).show();
        } else {

            //Verifica se é uma edição
            if (vaiEditar.equals("1")) { //se for, faz update
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("LADO", lado);
                values.put("TIPO", tipo);
                values.put("CLASSE", classe);
                values.put("RAMAL", selectedIdRamal);
                values.put("FASE", selectedIdFase);
                values.put("NUMCASA", numCasa);
                values.put("MEDIDOR", medidor);
                db.update(TABELACONSUMIDOR, values, "ID=" + idConsumidor, null);
                db.close();
            } else { //se não for, faz um insert
                db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
                ContentValues values = new ContentValues();
                values.put("LADO", lado);
                values.put("TIPO", tipo);
                values.put("CLASSE", classe);
                values.put("RAMAL", selectedIdRamal);
                values.put("FASE", selectedIdFase);
                values.put("NUMCASA", numCasa);
                values.put("MEDIDOR", medidor);
                values.put("IDPOSTE", idPoste);

                db.insert(TABELACONSUMIDOR, null, values);
                db.close();
            }

            //Verifica se a pagina anterior estava sendo editada
            if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                Intent intent = new Intent(this, CadastrarEquipamento.class);
                intent.putExtra("USERTELA", "EDITAR");
                this.startActivity(intent);
                finish();
            } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                Intent intent = new Intent(this, CadastrarEquipamento.class);
                intent.putExtra("USERTELA", "10");
                intent.putExtra("tipo", tipoEquipamento);
                intent.putExtra("placa", numeroPlacaEquipamento);
                intent.putExtra("descricao", descricao);
                intent.putExtra("tensao", tensao);
                this.startActivity(intent);
                finish();
            }
        }
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Tem certeza que deseja sair desta aba? Os dados ainda não foram salvos");
        alertDialogBuilder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        //Verifica se a pagina anterior estava sendo editada
                        if (veioEdicao.equals("1")) { // se sim, manda um put extra avisando para continuar a edição
                            Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                            intent.putExtra("USERTELA", "EDITAR");
                            CadastrarConsumidores.this.startActivity(intent);
                            finish();
                        } else { // se não, envia os dados que estavam sendo inseridos(Caso haja)
                            Intent intent = new Intent(CadastrarConsumidores.this, CadastrarEquipamento.class);
                            intent.putExtra("USERTELA", "10");
                            intent.putExtra("tipo", tipoEquipamento);
                            intent.putExtra("placa", numeroPlacaEquipamento);
                            intent.putExtra("descricao", descricao);
                            intent.putExtra("tensao", tensao);

                            CadastrarConsumidores.this.startActivity(intent);
                            finish();
                        }
                        CadastrarConsumidores.super.onBackPressed();
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

    public void D10(View v){
        selectedIdRamal = "D10";

        //D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }
    public void T10(View v){
        selectedIdRamal = "T10";

        D10.setChecked(false);
        //T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void Q10(View v){
        selectedIdRamal = "Q10";

        D10.setChecked(false);
        T10.setChecked(false);
        //Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void Q16(View v){
        selectedIdRamal = "Q16";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        //Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void Q25(View v){
        selectedIdRamal = "Q25";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        //Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }
    public void Q35(View v){
        selectedIdRamal = "Q35";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        //Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void W01(View v){
        selectedIdRamal = "W10";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        //W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void W02(View v){
        selectedIdRamal = "W16";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        //W02.setChecked(false);
        W03.setChecked(false);
        W35.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void W03(View v){
        selectedIdRamal = "W25";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        //W03.setChecked(false);
        S01.setChecked(false);
        W35.setChecked(false);
        S02.setChecked(false);
        S03.setChecked(false);
    }

    public void S01(View v){
        selectedIdRamal = "S01";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        //S01.setChecked(false);
        S02.setChecked(false);
        W35.setChecked(false);
        S03.setChecked(false);
    }

    public void S02(View v){
        selectedIdRamal = "S02";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        S01.setChecked(false);
        W35.setChecked(false);
        //S02.setChecked(false);
        S03.setChecked(false);
    }

    public void S03(View v){
        selectedIdRamal = "S03";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        S01.setChecked(false);
        W35.setChecked(false);
        S02.setChecked(false);
        //S03.setChecked(false);
    }

    public void W35(View v){
        selectedIdRamal = "W35";

        D10.setChecked(false);
        T10.setChecked(false);
        Q10.setChecked(false);
        Q16.setChecked(false);
        Q25.setChecked(false);
        Q35.setChecked(false);
        W01.setChecked(false);
        W02.setChecked(false);
        W03.setChecked(false);
        S01.setChecked(false);
        S02.setChecked(false);
        //S03.setChecked(false);
    }

    public void AN(View v){
        selectedIdFase = "AN";

        //AN.setChecked(false);
        BN.setChecked(false);
        CN.setChecked(false);
        ABN.setChecked(false);
        ACN.setChecked(false);
        BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void BN(View v){
        selectedIdFase = "BN";

        AN.setChecked(false);
        //BN.setChecked(false);
        CN.setChecked(false);
        ABN.setChecked(false);
        ACN.setChecked(false);
        BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void CN(View v){
        selectedIdFase = "CN";

        AN.setChecked(false);
        BN.setChecked(false);
        //CN.setChecked(false);
        ABN.setChecked(false);
        ACN.setChecked(false);
        BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void ABN(View v){
        selectedIdFase = "ABN";

        AN.setChecked(false);
        BN.setChecked(false);
        CN.setChecked(false);
        //ABN.setChecked(false);
        ACN.setChecked(false);
        BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void ACN(View v){
        selectedIdFase = "ACN";

        AN.setChecked(false);
        BN.setChecked(false);
        CN.setChecked(false);
        ABN.setChecked(false);
        //ACN.setChecked(false);
        BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void BCN(View v){
        selectedIdFase = "BCN";

        AN.setChecked(false);
        BN.setChecked(false);
        CN.setChecked(false);
        ABN.setChecked(false);
        ACN.setChecked(false);
        //BCN.setChecked(false);
        ABCN.setChecked(false);
    }

    public void ABCN(View v){
        selectedIdFase = "ABCN";

        AN.setChecked(false);
        BN.setChecked(false);
        CN.setChecked(false);
        ABN.setChecked(false);
        ACN.setChecked(false);
        BCN.setChecked(false);
        //ABCN.setChecked(false);
    }
}
