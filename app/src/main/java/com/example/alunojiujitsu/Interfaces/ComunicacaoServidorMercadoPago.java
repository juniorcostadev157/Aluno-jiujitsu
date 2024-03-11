package com.example.alunojiujitsu.Interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ComunicacaoServidorMercadoPago {

    @Headers("Content-Type: application/json")
    @POST
    Call<JsonObject> enviarPagamentoPix(
            @Url String url,
            @Header("Authorization") String authorization,
            @Header("X-Idempotency-Key") String idempotencyKey,
            @Body JsonObject dados
    );

    @POST
    Call<JsonObject> enviarPagamentoCartao(
            @Url String url,
            @Body JsonObject dados
    );
}
