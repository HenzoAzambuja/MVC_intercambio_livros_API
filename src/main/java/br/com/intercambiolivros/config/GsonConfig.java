package br.com.intercambiolivros.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConfig {

    private static final Gson gson =
            new GsonBuilder().create();

    public static Gson getGson() {
        return gson;
    }
}