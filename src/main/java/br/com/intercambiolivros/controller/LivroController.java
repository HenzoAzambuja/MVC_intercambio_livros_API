
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.GsonConfig;
import br.com.intercambiolivros.model.Livro;
import br.com.intercambiolivros.service.LivroService;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/livros/*")
public class LivroController extends HttpServlet {

    private final LivroService livroService =
            new LivroService();

    private final Gson gson =
            GsonConfig.getGson();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isEmpty()) {

                List<Livro> livros =
                        this.livroService.listarDisponiveis(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros);

                return;
            }

            if (pathInfo.equals("/usuario")) {

                List<Livro> livros =
                        this.livroService.listarPorUsuario(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros);

                return;
            }

            if (pathInfo.equals("/usuario/disponiveis")) {

                List<Livro> livros =
                        this.livroService.listarDisponiveisPorUsuario(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros);

                return;
            }

            Long id = this.obterId(req);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do livro invalido.");

                return;
            }

            Livro livro =
                    this.livroService.buscarPorId(id);

            if (livro == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Livro nao encontrado.");

                return;
            }

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    livro);

        } catch (IllegalArgumentException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            this.erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao consultar livros.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (pathInfo != null
                && !pathInfo.equals("/")
                && !pathInfo.isEmpty()) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Rota nao encontrada.");

            return;
        }

        try {

            Livro livro =
                    this.gson.fromJson(
                            req.getReader(),
                            Livro.class);

            if (livro == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Dados do livro sao obrigatorios.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            this.livroService.salvar(
                    livro,
                    usuarioId);

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Livro cadastrado com sucesso."));

        } catch (JsonParseException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "JSON invalido.");

        } catch (IllegalArgumentException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            this.erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao cadastrar livro.");
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        try {

            if (pathInfo == null
                    || pathInfo.isEmpty()
                    || pathInfo.equals("/")) {

                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada.");

                return;
            }

            String[] partes =
                    pathInfo.substring(1).split("/");

            Long id = this.obterId(partes[0]);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do livro invalido.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            if (partes.length == 2
                    && partes[1].equals("disponibilidade")) {

                this.livroService.alterarDisponibilidade(
                        id,
                        usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        Map.of(
                                "mensagem",
                                "Disponibilidade alterada com sucesso."));

                return;
            }

            if (partes.length != 1) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada.");

                return;
            }

            Livro livro =
                    this.gson.fromJson(
                            req.getReader(),
                            Livro.class);

            if (livro == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Dados do livro sao obrigatorios.");

                return;
            }

            livro.setId(id);

            this.livroService.salvar(
                    livro,
                    usuarioId);

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Map.of(
                            "mensagem",
                            "Livro atualizado com sucesso."));

        } catch (JsonParseException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "JSON invalido.");

        } catch (IllegalArgumentException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            this.erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar livro.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            Long id = this.obterId(req);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do livro invalido.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            this.livroService.deletar(
                    id,
                    usuarioId);

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT);

        } catch (IllegalArgumentException e) {

            this.erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            this.erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao excluir livro.");
        }
    }

    private Long obterId(HttpServletRequest req) {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null
                || pathInfo.length() <= 1) {
            return null;
        }

        String[] partes =
                pathInfo.substring(1).split("/");

        return this.obterId(partes[0]);
    }

    private Long obterId(String valor) {

        try {
            return Long.valueOf(valor);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void responderJson(
            HttpServletResponse resp,
            int status,
            Object dados) throws IOException {

        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        resp.getWriter().write(
                this.gson.toJson(dados));
    }

    private void erro(
            HttpServletResponse resp,
            int status,
            String mensagem) throws IOException {

        this.responderJson(
                resp,
                status,
                Map.of("erro", mensagem));
    }
}
