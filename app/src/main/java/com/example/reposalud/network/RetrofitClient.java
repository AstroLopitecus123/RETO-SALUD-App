package com.example.reposalud.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// EDITADO POR ASTRO - Cliente Retrofit conectado a la pagina
public class RetrofitClient {
    private static final String BASE_URL = "https://backend-citas-production-4c29.up.railway.app/"; 
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
