package com.example.alunojiujitsu.activitys.Pagamentos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.alunojiujitsu.Interfaces.ComunicacaoServidorMercadoPago;
import com.example.alunojiujitsu.R;
import com.example.alunojiujitsu.activitys.ValidarPagamentoPix.ValidarPagamentoPixEspecie;
import com.example.alunojiujitsu.databinding.ActivityPagamentoMensalidadeBinding;
import com.example.alunojiujitsu.model.DB;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Payment;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class PagamentoMensalidade extends AppCompatActivity {

    ActivityPagamentoMensalidadeBinding binding;
    private String data, preco, nomeUsuario, cpfUsuario, telefoneUsuario, mensalidadeID, nomeAluno, email_aluno;
    private String alunoID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private final  String PUBLIC_KEY = "APP_USR-8dbf5835-ba25-4b98-946b-c677bab2d160";
    //"APP_USR-4f2bc8fd-e1a0-4c02-8cdd-e707bac08c36"
    //TEST-c475041e-a53a-4c66-b81d-f83ad54c1561
    private final String ACCESS_TOKEN = "APP_USR-261322189864537-012518-f198f6050712edd9ad6af006bb4eea96-272826687";

    private final String ACCESS_TOKEN2 = "APP_USR-4238822386408178-020115-5ca38772cb98d3190b3e830dcdd56692-272826687";
    //TEST-261322189864537-012518-ccc855746d84877e2a170aa35cbdb030-272826687
    DB db = new DB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPagamentoMensalidadeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        preco = getIntent().getExtras().getString("preco");
        data = getIntent().getExtras().getString("data");
        mensalidadeID = getIntent().getExtras().getString("mensalidadeID");
        nomeAluno = getIntent().getExtras().getString("nomeAluno");
        email_aluno = getIntent().getExtras().getString("email_aluno");



        // mascara Para o CPF
        binding.editCpfPagamento.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;
            String oldString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                String mascara = "";
                String cleanString = str.replaceAll("[^\\d]", "");

                if (isUpdating) {
                    oldString = cleanString;
                    isUpdating = false;
                    return;
                }

                int i = 0;
                String mask = "###.###.###-##";
                for (char m : mask.toCharArray()) {
                    if (m != '#' && cleanString.length() > oldString.length()) {
                        mascara += m;
                        continue;
                    }
                    try {
                        mascara += cleanString.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                binding.editCpfPagamento.setText(mascara);
                binding.editCpfPagamento.setSelection(mascara.length());
            }
        });


//  mascara Para o telefone
        binding.editTelefone.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;
            String oldString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    oldString = s.toString();
                    isUpdating = false;
                    return;
                }

                String cleanString = s.toString().replaceAll("[^\\d]", "");
                String maskedString;

                if (cleanString.length() <= 11) {
                    maskedString = cleanString.replaceFirst("(\\d{2})(\\d{5})(\\d+)", "($1) $2-$3");
                } else {
                    maskedString = oldString;
                }

                isUpdating = true;
                binding.editTelefone.setText(maskedString);
                binding.editTelefone.setSelection(maskedString.length());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.buttonPagamentoCartao.setOnClickListener(v -> {

            nomeUsuario = binding.editNomePagamento.getText().toString();
            cpfUsuario = binding.editCpfPagamento.getText().toString();
            telefoneUsuario = binding.editTelefone.getText().toString();


            if(!nomeUsuario.isEmpty() || !cpfUsuario.isEmpty() || !telefoneUsuario.isEmpty()){
                    criarJsonObjetcCartaoBoleto();
            }else{
                Snackbar snackbar = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.RED);
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();
            }

        });

        binding.buttonPagamentoPix.setOnClickListener(v -> {

            nomeUsuario = binding.editNomePagamento.getText().toString();
            cpfUsuario = binding.editCpfPagamento.getText().toString();
            telefoneUsuario = binding.editTelefone.getText().toString();

            if(nomeUsuario.isEmpty() || cpfUsuario.isEmpty() || telefoneUsuario.isEmpty()){
                Snackbar snackbar = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.RED);
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();

            }else{
                criarPagamentoPix();
            }

        });

        binding.buttonValidarPagamentoPix.setOnClickListener(v -> {


            Date hoje = new Date();

             nomeUsuario = binding.editNomePagamento.getText().toString();
            cpfUsuario = binding.editCpfPagamento.getText().toString();
            telefoneUsuario = binding.editTelefone.getText().toString();


            Log.d("PagamentoMensalidade", "Nome: " + nomeUsuario);
            Log.d("PagamentoMensalidade", "CPF: " + cpfUsuario);
            Log.d("PagamentoMensalidade", "Telefone: " + telefoneUsuario);

            if(nomeUsuario != null && !nomeUsuario.isEmpty() && cpfUsuario != null && !cpfUsuario.isEmpty() && telefoneUsuario != null && !telefoneUsuario.isEmpty()){

                Locale.setDefault(new Locale("pt", "BR"));
                String hojeFormatado =  DateFormat.getInstance().format(hoje);
                String titulo = "Pagamento: Pix/Especie";
                String status = "Em verificação";

                Intent intent = new Intent(this, ValidarPagamentoPixEspecie.class);
                intent.putExtra("titulo", titulo);
                intent.putExtra("nome", nomeUsuario);
                intent.putExtra("preco", preco);
                intent.putExtra("data", hojeFormatado);
                intent.putExtra("status_pagamento",status );
                intent.putExtra("refMes", data);
                intent.putExtra("mensalidadeID", mensalidadeID);
                intent.putExtra("nomeAluno", nomeAluno);
                intent.putExtra("email_aluno", email_aluno);

                startActivity(intent);


            }else{
                Snackbar snackbar = Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.RED);
                snackbar.setTextColor(Color.WHITE);
                snackbar.show();
            }


        });

    }

    private void criarPagamentoPix(){
        JsonObject dadosPagamento = new JsonObject();
        dadosPagamento.addProperty("transaction_amount", new BigDecimal(preco));
        dadosPagamento.addProperty("payment_method_id", "pix");
        dadosPagamento.addProperty("description", "Mensalidade");

        //adicionar dados do comprador
        JsonObject payer = new JsonObject();
        payer.addProperty("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        payer.addProperty("first_name", nomeUsuario);
        payer.add("identification", Identificao("CPF", cpfUsuario));
        dadosPagamento.add("payer", payer);

        processarPagamentoPix(dadosPagamento);


    }

    private JsonObject Identificao(String tipo, String numero){
        JsonObject identificao = new JsonObject();
        identificao.addProperty("type", tipo);
        identificao.addProperty("number", numero);
        return identificao;
    }

    private void processarPagamentoPix(JsonObject dadosPagamento){
        String site = "https://api.mercadopago.com";
        String autorizacao = "Bearer " + ACCESS_TOKEN2;
        String idempotencyKey = UUID.randomUUID().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(site)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ComunicacaoServidorMercadoPago servidorMercadoPago = retrofit.create(ComunicacaoServidorMercadoPago.class);
        Call<JsonObject> call = servidorMercadoPago.enviarPagamentoPix("/v1/payments", autorizacao, idempotencyKey, dadosPagamento);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    JsonObject respostaServidor = response.body();
                    assert response != null;
                    JsonObject pontoInteracao  = respostaServidor.getAsJsonObject("point_of_interaction");
                    if (pontoInteracao != null){
                        JsonObject transactionData = pontoInteracao.getAsJsonObject("transaction_data");
                        if (transactionData != null) {
                            String ticketUrl = transactionData.get("ticket_url").getAsString();
                            abrirUrlPagamento(ticketUrl);

                    }else{
                            Log.e("PagamentoPIX", "Campo 'transaction_data' não encontrado na resposta.");
                        }
                    }else{
                        Log.e("PagamentoPIX", "Campo 'point_of_interaction' não encontrado na resposta.");
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });

    }
     private void abrirUrlPagamento(String ticketURL){
        try {
            Intent abrirNavegadorIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ticketURL));
            startActivity(abrirNavegadorIntent);
        }catch (Exception e){
            Log.e("Pagamento", "Erro ao abrir URL", e);
        }
     }


     // metodos abaixos para pagamento do cartão e boleto

    private  void criarJsonObjetcCartaoBoleto(){
        // primeiro item
        JsonObject dados  = new JsonObject();
        JsonArray item_lista = new JsonArray();
        JsonObject item;

        JsonObject email = new JsonObject();

        item = new JsonObject();

        item.addProperty("title", "Mensalidade");
        item.addProperty("quantity", 1);
        item.addProperty("currency_id", "BRL");
        item.addProperty("unit_price", Double.parseDouble(preco));
        item_lista.add(item);
        dados.add("items", item_lista);

        String usuarioEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        email.addProperty("email", usuarioEmail);
        dados.add("payer", email);
        criarPreferenciaPagamento(dados);

    }

    private void criarPreferenciaPagamento(JsonObject dados){

        String site =  "https://api.mercadopago.com";
        String url = "/checkout/preferences?access_token=" + ACCESS_TOKEN;

        Gson gson  = new GsonBuilder().setLenient().create();
        Retrofit retrofit  = new Retrofit.Builder()
                .baseUrl(site)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ComunicacaoServidorMercadoPago pagamento = retrofit.create(ComunicacaoServidorMercadoPago.class);
        Call<JsonObject> request = pagamento.enviarPagamentoCartao(url, dados);
        request.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if (response.isSuccessful()){
                    String preferenceID = response.body().get("id").getAsString();
                    criarPagamentoCartao(preferenceID);
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("j", "Erro");
            }
        });

    }
    private void criarPagamentoCartao(String preferenceID){

        final AdvancedConfiguration advancedConfiguration = new AdvancedConfiguration.Builder().setBankDealsEnabled(false).build();
        new MercadoPagoCheckout.Builder(PUBLIC_KEY, preferenceID)
                .setAdvancedConfiguration(advancedConfiguration).build()
                .startPayment(this, 123);

    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if (resultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {

                final Payment pagamento = (Payment) data.getSerializableExtra(MercadoPagoCheckout.EXTRA_PAYMENT_RESULT);
                respostaMercadoPago(pagamento);

            } else if (resultCode == RESULT_CANCELED) {

                //Resolve error in checkout
            } else {
                //Resolve canceled checkout
            }
        }
    }
    private  void respostaMercadoPago(Payment pagamento){


        String status_pagamento = "Status Pagamento:  " + " " + "Pagamento Aprovado";
        String  nomeProduto = "Mensalidade - Cartão";
        String precoProduto ="Preço: "  +  preco;
        String nomePagador = "Nome: " + nomeUsuario;
        String referenteMes = "Ref: " + data;
        String foto = "pagamento com cartão nao necessita foto";

        Date hoje = new Date();

        Locale.setDefault(new Locale("pt", "BR"));
        String hojeFormatado =  DateFormat.getInstance().format(hoje);




        String status = pagamento.getPaymentStatus();
        if(status.equalsIgnoreCase("approved")){
            Snackbar snackbar = Snackbar.make(binding.containerPagamento, "Sucesso ao fazer pagamento", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.BLUE);
            snackbar.setTextColor(Color.WHITE);
            snackbar.show();
            db.salvarMensalidadeAlunos(nomePagador, nomeProduto, precoProduto, status_pagamento, hojeFormatado, referenteMes, foto, mensalidadeID, nomeAluno, email_aluno, alunoID);


        }else if(status.equalsIgnoreCase("rejected")){

            Snackbar snackbar = Snackbar.make(binding.containerPagamento, "Erro ao fazer pagamento", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.RED);
            snackbar.setTextColor(Color.WHITE);
            snackbar.show();

        }
    }
}
