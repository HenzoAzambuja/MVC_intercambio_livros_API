
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.GsonConfig;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseController extends HttpServlet {

    protected final Gson gson = GsonConfig.getGson();

    // Converte um objeto Java para JSON e envia a resposta.
    protected void responderJson(
            HttpServletResponse resp,
            int status,
            Object dados
    ) throws IOException {

        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        resp.getWriter().write(gson.toJson(dados));
    }

    // Envia uma resposta de erro em JSON.
    protected void erro(
            HttpServletResponse resp,
            int status,
            String mensagem
    ) throws IOException {

        responderJson(
                resp,
                status,
                Map.of("erro", mensagem)
        );
    }

    // Obtém o ID do primeiro segmento da URL.
    protected Long obterId(HttpServletRequest req) {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            return null;
        }

        String[] partes = pathInfo.substring(1).split("/");

        return obterId(partes[0]);
    }

    // Converte uma String para Long.
    protected Long obterId(String valor) {

        try {
            return Long.valueOf(valor);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Obtém os segmentos do caminho da URL.
    protected String[] obterPartesCaminho(HttpServletRequest req) {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            return new String[0];
        }

        return pathInfo.substring(1).split("/");
    }

    // Converte listas de objetos para listas de DTOs.
    protected <T, R> List<R> converterLista(
            List<T> dados,
            Function<T, R> conversor
    ) {

        List<R> resposta = new ArrayList<>();

        for (T item : dados) {
            resposta.add(conversor.apply(item));
        }

        return resposta;
    }
}
