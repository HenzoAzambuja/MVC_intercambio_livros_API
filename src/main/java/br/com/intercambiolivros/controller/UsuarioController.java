
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.model.Usuario;
import br.com.intercambiolivros.service.UsuarioService;

import com.google.gson.JsonParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@WebServlet("/api/usuarios/*")
public class UsuarioController extends BaseController {

    private final UsuarioService usuarioService =
            new UsuarioService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        Long usuarioId =
                (Long) req.getAttribute("usuarioId");

        Long perfilId =
                (Long) req.getAttribute("perfilId");

        try {

            String pathInfo = req.getPathInfo();

            // GET /api/usuarios
            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isEmpty()) {

                List<Usuario> usuarios =
                        usuarioService.listar(
                                usuarioId,
                                perfilId
                        );

                responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        converterLista(
                                usuarios,
                                UsuarioResponse::new
                        )
                );

                return;
            }

            // GET /api/usuarios/{id}
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

            Usuario usuario =
                    usuarioService.buscarPorId(
                            id,
                            usuarioId,
                            perfilId
                    );

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new UsuarioResponse(usuario)
            );

        } catch (SecurityException e) {

            erro(
                    resp,
                    usuarioId == null || perfilId == null
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
                    "Erro ao consultar usuarios."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        // POST /api/usuarios/cadastro
        String pathInfo = req.getPathInfo();

        if (!"/cadastro".equals(pathInfo)) {
            erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Rota nao encontrada."
            );

            return;
        }

        try {

            UsuarioRequest dados =
                    gson.fromJson(
                            req.getReader(),
                            UsuarioRequest.class
                    );

            Usuario usuario =
                    dados == null
                            ? null
                            : dados.paraUsuario();

            usuarioService.cadastrarNormal(usuario);

            responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Usuario cadastrado com sucesso."
                    )
            );

        } catch (JsonParseException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "JSON invalido."
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
                    "Erro ao cadastrar usuario."
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

        Long perfilId =
                (Long) req.getAttribute("perfilId");

        try {

            // PUT /api/usuarios/{id}
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

            UsuarioRequest dados =
                    gson.fromJson(
                            req.getReader(),
                            UsuarioRequest.class
                    );

            Usuario usuario =
                    dados == null
                            ? null
                            : dados.paraUsuario();

            if (usuario != null) {
                usuario.setId(id);
            }

            usuarioService.atualizar(
                    usuario,
                    usuarioId,
                    perfilId
            );

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Map.of(
                            "mensagem",
                            "Usuario atualizado com sucesso."
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
                    usuarioId == null || perfilId == null
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
                    "Erro ao atualizar usuario."
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

        Long perfilId =
                (Long) req.getAttribute("perfilId");

        try {

            // DELETE /api/usuarios/{id}
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

            usuarioService.deletar(
                    id,
                    usuarioId,
                    perfilId
            );

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (SecurityException e) {

            erro(
                    resp,
                    usuarioId == null || perfilId == null
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
                    "Erro ao excluir usuario."
            );
        }
    }

    // -------------------------
    // DTO de entrada
    // -------------------------

    private static class UsuarioRequest {

        private String nome;
        private String email;
        private String login;
        private String senha;
        private Long perfilId;

        public Usuario paraUsuario() {

            Usuario usuario = new Usuario();

            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setLogin(login);
            usuario.setSenha(senha);
            usuario.setPerfilId(perfilId);

            return usuario;
        }
    }

    // -------------------------
    // DTO de saída
    // -------------------------

    private static class UsuarioResponse {

        private Long id;
        private String nome;
        private String email;
        private String login;
        private Long perfilId;

        public UsuarioResponse(Usuario usuario) {

            this.id = usuario.getId();
            this.nome = usuario.getNome();
            this.email = usuario.getEmail();
            this.login = usuario.getLogin();
            this.perfilId = usuario.getPerfilId();
        }
    }
}
