package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.GsonConfig;
import br.com.intercambiolivros.model.Livro;
import br.com.intercambiolivros.model.Troca;
import br.com.intercambiolivros.service.TrocaService;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/trocas/*")
public class TrocaController extends HttpServlet {

    private final TrocaService trocaService =
            new TrocaService();

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

                List<Troca> trocas =
                        this.trocaService.listarPorUsuario(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        this.converterLista(trocas));

                return;
            }

            if (pathInfo.equals("/pendentes")) {

                List<Troca> trocas =
                        this.trocaService.listarPendentesPorUsuario(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        this.converterLista(trocas));

                return;
            }

            if (pathInfo.equals("/concluidas")) {

                List<Troca> trocas =
                        this.trocaService.listarConcluidasPorUsuario(
                                usuarioId);

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        this.converterLista(trocas));

                return;
            }

            String[] partes =
                    pathInfo.substring(1).split("/");

            if (partes.length != 1) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada.");

                return;
            }

            Long id = this.obterId(partes[0]);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id da troca invalido.");

                return;
            }

            Troca troca =
                    this.trocaService.buscarPorId(id);

            if (troca == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Troca nao encontrada.");

                return;
            }

            if (!this.participaDaTroca(
                    troca,
                    usuarioId)) {

                this.erro(
                        resp,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Voce nao participa desta troca.");

                return;
            }

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new TrocaResponse(troca));

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
                    "Erro ao consultar trocas.");
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

            SolicitarTrocaRequest dados =
                    this.gson.fromJson(
                            req.getReader(),
                            SolicitarTrocaRequest.class);

            if (dados == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Dados da troca sao obrigatorios.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            this.trocaService.solicitar(
                    usuarioId,
                    dados.livroOferecidoId,
                    dados.livroRecebidoId,
                    dados.mensagem);

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Troca solicitada com sucesso."));

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
                    "Erro ao solicitar troca.");
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

            if (partes.length != 2) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada.");

                return;
            }

            Long trocaId =
                    this.obterId(partes[0]);

            if (trocaId == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id da troca invalido.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            Troca troca =
                    this.trocaService.buscarPorId(trocaId);

            if (troca == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Troca nao encontrada.");

                return;
            }

            String acao = partes[1];

            switch (acao) {

                case "aceitar":

                    if (!this.eDonoDoLivroRecebido(
                            troca,
                            usuarioId)) {

                        this.erro(
                                resp,
                                HttpServletResponse.SC_FORBIDDEN,
                                "Voce nao pode responder esta troca.");

                        return;
                    }

                    this.trocaService.aceitar(
                            trocaId,
                            usuarioId);

                    this.responderJson(
                            resp,
                            HttpServletResponse.SC_OK,
                            Map.of(
                                    "mensagem",
                                    "Troca aceita com sucesso."));

                    break;

                case "recusar":

                    if (!this.eDonoDoLivroRecebido(
                            troca,
                            usuarioId)) {

                        this.erro(
                                resp,
                                HttpServletResponse.SC_FORBIDDEN,
                                "Voce nao pode responder esta troca.");

                        return;
                    }

                    this.trocaService.recusar(
                            trocaId,
                            usuarioId);

                    this.responderJson(
                            resp,
                            HttpServletResponse.SC_OK,
                            Map.of(
                                    "mensagem",
                                    "Troca recusada com sucesso."));

                    break;

                case "concluir":

                    if (!this.participaDaTroca(
                            troca,
                            usuarioId)) {

                        this.erro(
                                resp,
                                HttpServletResponse.SC_FORBIDDEN,
                                "Voce nao participa desta troca.");

                        return;
                    }

                    this.trocaService.concluir(
                            trocaId,
                            usuarioId);

                    this.responderJson(
                            resp,
                            HttpServletResponse.SC_OK,
                            Map.of(
                                    "mensagem",
                                    "Troca concluida com sucesso."));

                    break;

                default:

                    this.erro(
                            resp,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Acao nao encontrada.");
            }

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
                    "Erro ao atualizar troca.");
        }
    }

    private boolean participaDaTroca(
            Troca troca,
            Long usuarioId) {

        if (usuarioId == null) {
            return false;
        }

        Livro oferecido =
                troca.getLivroOferecido();

        Livro recebido =
                troca.getLivroRecebido();

        return (oferecido != null
                && usuarioId.equals(
                        oferecido.getUsuarioId()))
                ||
                (recebido != null
                && usuarioId.equals(
                        recebido.getUsuarioId()));
    }

    private boolean eDonoDoLivroRecebido(
            Troca troca,
            Long usuarioId) {

        if (usuarioId == null
                || troca.getLivroRecebido() == null) {

            return false;
        }

        return usuarioId.equals(
                troca.getLivroRecebido().getUsuarioId());
    }

    private Long obterId(String valor) {

        try {
            return Long.valueOf(valor);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<TrocaResponse> converterLista(
            List<Troca> trocas) {

        List<TrocaResponse> resposta =
                new ArrayList<>();

        for (Troca troca : trocas) {
            resposta.add(new TrocaResponse(troca));
        }

        return resposta;
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

    private static class SolicitarTrocaRequest {

        private Long livroOferecidoId;
        private Long livroRecebidoId;
        private String mensagem;
    }

    private static class TrocaResponse {

        private Long id;
        private Long livroOferecidoId;
        private Long livroRecebidoId;
        private String status;
        private String mensagem;

        private LivroResponse livroOferecido;
        private LivroResponse livroRecebido;

        public TrocaResponse(Troca troca) {

            this.id = troca.getId();

            this.livroOferecidoId =
                    troca.getLivroOferecidoId();

            this.livroRecebidoId =
                    troca.getLivroRecebidoId();

            this.status = troca.getStatus();
            this.mensagem = troca.getMensagem();

            if (troca.getLivroOferecido() != null) {
                this.livroOferecido =
                        new LivroResponse(
                                troca.getLivroOferecido());
            }

            if (troca.getLivroRecebido() != null) {
                this.livroRecebido =
                        new LivroResponse(
                                troca.getLivroRecebido());
            }
        }
    }

    private static class LivroResponse {

        private Long id;
        private String titulo;
        private String autor;
        private Long usuarioId;

        public LivroResponse(Livro livro) {

            this.id = livro.getId();
            this.titulo = livro.getTitulo();
            this.autor = livro.getAutor();
            this.usuarioId = livro.getUsuarioId();
        }
    }
}