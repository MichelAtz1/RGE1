package br.desenvolvedor.michelatz.aplicativohcc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ManejoVegetacao extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private EditText postesRelacionadosgh, textDescricaoVegetacaoPoste1, textDescricaoVegetacaoPoste2;
    private TextView tvResposta;
    String caminhoImagem = "inicial", BANCO = "banco.db", TABELAFOTOSVEGETACAO = "vegetacao", imgString, nomeDocumentoParte1, nomeDocumentoParte2;
    static final int CODIGO_REQUISICAO = 0;
    ArrayList<String> tipoDocumento = new ArrayList<String>();

    SQLiteDatabase db;
    Uri outputUri;
    File fileFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manejo_vegetacao);

        //postesRelacionados = (EditText) findViewById(R.id.textDescricaoVegetacao);
        textDescricaoVegetacaoPoste1 = (EditText) findViewById(R.id.textDescricaoVegetacaoPoste1);
        textDescricaoVegetacaoPoste2 = (EditText) findViewById(R.id.textDescricaoVegetacaoPoste2);
        tvResposta = (TextView) findViewById(R.id.tvResposta);

        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String idLocacaoSelecionada = sharedpreferences.getString("idLocacaoKey", null);

        db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
        Cursor mCount= db.rawQuery("select count(*) from " + TABELAFOTOSVEGETACAO+ " WHERE IDLOCACAO = " + idLocacaoSelecionada + " ;", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        count=count+1;
        tvResposta.setText("Inserção da Foto "+count);

    }

    public void foto(View v) {
        nomeDocumentoParte1 = textDescricaoVegetacaoPoste1.getText().toString().trim();
        nomeDocumentoParte2 = textDescricaoVegetacaoPoste2.getText().toString().trim();
        if (nomeDocumentoParte1.equals("") || nomeDocumentoParte2.equals("")) {
            Toast.makeText(ManejoVegetacao.this, "Por Favor! Insira a qual postes esta vegetação pertence!", Toast.LENGTH_SHORT).show();

        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ManejoVegetacao.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                outputUri = getTempCameraUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                startActivityForResult(intent, 1);
            } else {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (it.resolveActivity(getPackageManager()) != null) {
                    File arquivoFoto = null;
                    try {
                        arquivoFoto = criaArquivoImagem();
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

    private File criaArquivoImagem() throws IOException {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);

        String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomeArquivoImagem = "Vegetacao " + "_Poste" + nomeDocumentoParte1+" ao "+nomeDocumentoParte1 +" "+ numeroNotaSelect +  "_" + horarioSistema;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

        // Verificar se a pasta HCC existe, caso não exista, crie o diretório
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        // Verificar se a pasta com o numero da nota existe, caso não exista, crie o diretório dentro da pasta HCC
        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
        File dir2 = new File(path2);
        if (!dir2.exists())
            dir2.mkdirs();

        File imagem = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
        caminhoImagem = imagem.getAbsolutePath();

        return imagem;
    }

    // Método com resultado da negação da permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(ManejoVegetacao.this, "Desculpe!!! você não pode usar este aplicativo sem conceder permissão!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        ImageView iv = (ImageView) findViewById(R.id.foto);
        Bitmap bitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (outputUri == null) {
                return;
            }
            if (outputUri != null) {

                try {
                    Bitmap imagemBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), outputUri);
                    Bitmap tamanhoReduzidoImagem, imagemVirada;
                    //imagemVirada = RotateBitmap(imagemBitmap, 90);
                    tamanhoReduzidoImagem = Bitmap.createScaledBitmap(imagemBitmap, (int) (imagemBitmap.getWidth() * 0.2), (int) (imagemBitmap.getHeight() * 0.2), true);
                    if (tamanhoReduzidoImagem.getWidth() > tamanhoReduzidoImagem.getHeight()) {
                        tamanhoReduzidoImagem = RotateBitmap(tamanhoReduzidoImagem, 90);
                    }
                    imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                    if (imagemBitmap.getWidth() > imagemBitmap.getHeight()) {
                        imagemBitmap = RotateBitmap(imagemBitmap, 90);
                    }
                    iv.setImageBitmap(imagemBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (resultCode == RESULT_OK) {
                if (requestCode == CODIGO_REQUISICAO) {

                    File arquivoImagem = new File(caminhoImagem);
                    if (arquivoImagem.exists()) {
                        Bitmap imagemBitmap = BitmapFactory.decodeFile(
                                arquivoImagem.getAbsolutePath());

                        Bitmap tamanhoReduzidoImagem, imagemVirada;
                        imagemVirada = RotateBitmap(imagemBitmap, 90);
                        tamanhoReduzidoImagem = Bitmap.createScaledBitmap(imagemVirada, (int) (imagemVirada.getWidth() * 0.2), (int) (imagemVirada.getHeight() * 0.2), true);
                        imgString = Base64.encodeToString(getBytesFromBitmap(tamanhoReduzidoImagem), Base64.NO_WRAP);
                        iv.setImageBitmap(RotateBitmap(imagemBitmap, 90));
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Você NÃO inseriu a foto!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método que gira a imagem em 90 graus
    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Método de codifica a imagem, para base64
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    // Método que transforma a imagem em byte[]
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Cria local onde será salvo a imagem
    private Uri getTempCameraUri() {
        try {
            SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
            String numeroNotaSelect = sharedpreferences.getString("numeroNotaKey", null);

            // Cria o nome do arquivo com numeroDaNota+NomeDoDocumento+horarioDoSistema
            String horarioSistema = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String nomeArquivoImagem = "Vegetacao " + "_Poste" + nomeDocumentoParte1 + " ao "+ nomeDocumentoParte1 +" "+ numeroNotaSelect +  "_" + horarioSistema;

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/";

            // Verificar se a pasta HCC existe, caso não exista, crie o diretório
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            // Verificar se a pasta com o numero da nota existe, caso não exista, cria o diretório dentro da pasta HCC
            String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HCC/" + numeroNotaSelect;
            File dir2 = new File(path2);
            if (!dir2.exists())
                dir2.mkdirs();

            // Salva a imagem no caminho criado
            fileFinal = File.createTempFile(nomeArquivoImagem, ".jpg", dir2);
            Uri photoURI2 = FileProvider.getUriForFile(ManejoVegetacao.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    fileFinal);

            return photoURI2;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método que pega  a ação do Botão "Salvar"
    public void salvarImagemDocumento(View v) {
        if (imgString == null) { // Verifica se a imagem foi tirada
            Toast.makeText(ManejoVegetacao.this, "A Foto eh Obrigatória ", Toast.LENGTH_SHORT).show();
        } else {
            addImagemDocBanco();
        }
    }

    // Método que salva a imagem no banco de dados
    private void addImagemDocBanco() {
        SharedPreferences sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        String idLocacaoSelecionada = sharedpreferences.getString("idLocacaoKey", null);

        if (nomeDocumentoParte1.equals("") || nomeDocumentoParte2.equals("")) {// verifica se o nome do documento foi selecionado
            Toast.makeText(ManejoVegetacao.this, "Por Favor!Insira os postes desta vegetação!", Toast.LENGTH_SHORT).show();
        } else {
            String doPosteTalaoTal = "Poste "+nomeDocumentoParte1+ " ao "+nomeDocumentoParte2;
            db = openOrCreateDatabase(BANCO, Context.MODE_PRIVATE, null);
            // Inserção dos dados no banco
            db.execSQL("INSERT INTO " + TABELAFOTOSVEGETACAO + "(DESCRICAO, IMAGEM, IDLOCACAO) VALUES ('" + doPosteTalaoTal + "','" + imgString + "','" + idLocacaoSelecionada + "')");
            db.close();
            Toast.makeText(getApplicationContext(), "Inserção realizada com sucesso!", Toast.LENGTH_SHORT).show();

            Intent it;
            it = new Intent(this, GerenciarLocacoes.class);
            startActivity(it);
            finish();
        }
    }

    //Metodo responsavel por pegar ação do botão nativo "Voltar" do smartfone
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GerenciarLocacoes.class);
        this.startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
