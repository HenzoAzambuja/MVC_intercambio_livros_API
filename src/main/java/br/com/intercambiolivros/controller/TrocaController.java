
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.model.Livro;
import br.com.intercambiolivros.model.Troca;
import br.com.intercambiolivros.service.TrocaService;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@WebServlet("/api/trocas/*")
public class TrocaController extends BaseController {

    private final TrocaService trocaService =
            new TrocaService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            String pathInfo = req.getPathInfo();

            // GET /api/trocas
            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isEmpty()) {

                List<Troca> trocas =
                        trocaService.listarPorUsuario(usuarioId);

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        converterLista(trocas, TrocaResponse::new)
                );

                return;
            }

            // GET /api/trocas/pendentes
            if (pathInfo.equals("/pendentes")) {

                List<Troca> trocas =
                        trocaService.listarPendentesPorUsuario(
                                usuarioId
                        );

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        converterLista(trocas, TrocaResponse::new)
                );

                return;
            }

            // GET /api/trocas/concluidas
            if (pathInfo.equals("/concluidas")) {

                List<Troca> trocas =
                        trocaService.listarConcluidasPorUsuario(
                                usuarioId
                        );

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        converterLista(trocas, TrocaResponse::new)
                );

                return;
            }

            // GET /api/trocas/{id}
            String[] partes = obterPartesCaminho(req);

            if (partes.length != 1) {
                erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada."
                );

                return;
            }

            Long id = obterId(partes[0]);

            if (id == null) {
                throw new IllegalArgumentException(
                        "Id da troca invalido."
                );
            }

            Troca troca =
                    trocaService.buscarPorId(id, usuarioId);

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new TrocaResponse(troca)
            );

        } catch (SecurityException e) {

            erro(
                    resp,
                    usuarioId == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (NoSuchElementException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao consultar trocas."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        // POST /api/trocas
        String pathInfo = req.getPathInfo();

        if (pathInfo != null
                && !pathInfo.equals("/")
                && !pathInfo.isEmpty()) {

            erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Rota nao encontrada."
            );

            return;
        }

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            SolicitarTrocaRequest dados =
                    gson.fromJson(
                            req.getReader(),
                            SolicitarTrocaRequest.class
                    );

            trocaService.solicitar(
                    usuarioId,
                    dados == null ? null : dados.livroOferecidoId,
                    dados == null ? null : dados.livroRecebidoId,
                    dados == null ? null : dados.mensagem
            );

            responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Troca solicitada com sucesso."
                    )
            );

        } catch (JsonParseException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "JSON invalido."
            );

        } catch (SecurityException e) {

            erro(
                    resp,
                    usuarioId == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (NoSuchElementException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao solicitar troca."
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            // PUT /api/trocas/{id}/{acao}
            String[] partes = obterPartesCaminho(req);

            if (partes.length != 2) {
                erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada."
                );

                return;
            }

            Long trocaId = obterId(partes[0]);

            if (trocaId == null) {
                throw new IllegalArgumentException(
                        "Id da troca invalido."
                );
            }

            String acao = partes[1];
            String mensagem;

            switch (acao) {

                case "aceitar":

                    trocaService.aceitar(
                            trocaId,
                            usuarioId
                    );

                    mensagem = "Troca aceita com sucesso.";
                    break;

                case "recusar":

                    trocaService.recusar(
                            trocaId,
                            usuarioId
                    );

                    mensagem = "Troca recusada com sucesso.";
                    break;

                case "concluir":

                    trocaService.concluir(
                            trocaId,
                            usuarioId
                    );

                    mensagem = "Troca concluida com sucesso.";
                    break;

                default:

                    erro(
                            resp,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Acao nao encontrada."
                    );

                    return;
            }

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Map.of("mensagem", mensagem)
            );

        } catch (SecurityException e) {

            erro(
                    resp,
                    usuarioId == null
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );

        } catch (NoSuchElementException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_CONFLICT,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao atualizar troca."
            );
        }
    }

        // DTO de entrada.
        private static class SolicitarTrocaRequest {

        private Long livroOferecidoId;
        private Long livroRecebidoId;
        private String mensagem;
        }

        // DTO de saída.
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
                                troca.getLivroOferecido()
                        );
                }

                if (troca.getLivroRecebido() != null) {
                this.livroRecebido =
                        new LivroResponse(
                                troca.getLivroRecebido()
                        );
                }
        }
        }

        // DTO resumido do livro dentro da troca.
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