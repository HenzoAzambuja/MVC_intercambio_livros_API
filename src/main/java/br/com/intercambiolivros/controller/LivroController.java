
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.model.Livro;
import br.com.intercambiolivros.service.LivroService;

import com.google.gson.JsonParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@WebServlet("/api/livros/*")
public class LivroController extends BaseController {

    private final LivroService livroService =
            new LivroService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            String pathInfo = req.getPathInfo();

            // GET /api/livros
            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isEmpty()) {

                List<Livro> livros =
                        livroService.listarDisponiveis(usuarioId);

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros
                );

                return;
            }

            // GET /api/livros/usuario
            if (pathInfo.equals("/usuario")) {

                List<Livro> livros =
                        livroService.listarPorUsuario(usuarioId);

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros
                );

                return;
            }

            // GET /api/livros/usuario/disponiveis
            if (pathInfo.equals("/usuario/disponiveis")) {

                List<Livro> livros =
                        livroService.listarDisponiveisPorUsuario(
                                usuarioId
                        );

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        livros
                );

                return;
            }

            // GET /api/livros/{id}
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
                        "Id do livro invalido."
                );
            }

            Livro livro =
                    livroService.buscarPorId(id, usuarioId);

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    livro
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
                    "Erro ao consultar livros."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        // POST /api/livros
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

        try {

            Livro livro =
                    gson.fromJson(
                            req.getReader(),
                            Livro.class
                    );

            livroService.salvar(livro, usuarioId);

            responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Livro cadastrado com sucesso."
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
                    "Erro ao cadastrar livro."
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

            String[] partes = obterPartesCaminho(req);

            if (partes.length == 0) {

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
                        "Id do livro invalido."
                );
            }

            // PUT /api/livros/{id}/disponibilidade
            if (partes.length == 2
                    && partes[1].equals("disponibilidade")) {

                livroService.alterarDisponibilidade(
                        id,
                        usuarioId
                );

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        Map.of(
                                "mensagem",
                                "Disponibilidade alterada com sucesso."
                        )
                );

                return;
            }

            // PUT /api/livros/{id}
            if (partes.length != 1) {

                erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota nao encontrada."
                );

                return;
            }

            Livro livro =
                    gson.fromJson(
                            req.getReader(),
                            Livro.class
                    );

            livroService.atualizar(
                    id,
                    livro,
                    usuarioId
            );

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Map.of(
                            "mensagem",
                            "Livro atualizado com sucesso."
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
                    "Erro ao atualizar livro."
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        try {

            String[] partes = obterPartesCaminho(req);

            // DELETE /api/livros/{id}
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
                        "Id do livro invalido."
                );
            }

            livroService.deletar(id, usuarioId);

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
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
                    "Erro ao excluir livro."
            );
        }
    }
}
