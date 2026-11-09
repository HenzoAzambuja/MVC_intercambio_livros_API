
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.GsonConfig;
import br.com.intercambiolivros.model.Usuario;
import br.com.intercambiolivros.service.UsuarioService;

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

@WebServlet("/api/usuarios/*")
public class UsuarioController extends HttpServlet {

    private static final Long PERFIL_ADMIN_ID = 1L;

    private final UsuarioService usuarioService =
            new UsuarioService();

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

        Long perfilId =
                (Long) req.getAttribute("perfilId");

        try {

            if (pathInfo == null
                    || pathInfo.equals("/")
                    || pathInfo.isEmpty()) {

                if (!this.eAdministrador(perfilId)) {
                    this.erro(
                            resp,
                            HttpServletResponse.SC_FORBIDDEN,
                            "Apenas administradores podem listar usuarios.");

                    return;
                }

                List<Usuario> usuarios =
                        this.usuarioService.listar();

                this.responderJson(
                        resp,
                        HttpServletResponse.SC_OK,
                        this.converterLista(usuarios));

                return;
            }

            Long id = this.obterId(req);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do usuario invalido.");

                return;
            }

            if (!id.equals(usuarioId)
                    && !this.eAdministrador(perfilId)) {

                this.erro(
                        resp,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Voce nao pode consultar este usuario.");

                return;
            }

            Usuario usuario =
                    this.usuarioService.buscarPorId(id);

            if (usuario == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Usuario nao encontrado.");

                return;
            }

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    new UsuarioResponse(usuario));

        } catch (Exception e) {

            e.printStackTrace();

            this.erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao consultar usuarios.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        if (!"/cadastro".equals(pathInfo)) {
            this.erro(
                    resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Rota nao encontrada.");

            return;
        }

        try {

            UsuarioRequest dados =
                    this.gson.fromJson(
                            req.getReader(),
                            UsuarioRequest.class);

            if (dados == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Dados do usuario sao obrigatorios.");

                return;
            }

            Usuario usuario = new Usuario();

            usuario.setNome(dados.nome);
            usuario.setEmail(dados.email);
            usuario.setLogin(dados.login);
            usuario.setSenha(dados.senha);

            this.usuarioService.cadastrarNormal(usuario);

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_CREATED,
                    Map.of(
                            "mensagem",
                            "Usuario cadastrado com sucesso."));

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
                    "Erro ao cadastrar usuario.");
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            Long id = this.obterId(req);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do usuario invalido.");

                return;
            }

            Long usuarioId =
                    (Long) req.getAttribute("usuarioId");

            Long perfilId =
                    (Long) req.getAttribute("perfilId");

            if (!id.equals(usuarioId)
                    && !this.eAdministrador(perfilId)) {

                this.erro(
                        resp,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Voce nao pode alterar este usuario.");

                return;
            }

            Usuario existente =
                    this.usuarioService.buscarPorId(id);

            if (existente == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Usuario nao encontrado.");

                return;
            }

            UsuarioRequest dados =
                    this.gson.fromJson(
                            req.getReader(),
                            UsuarioRequest.class);

            if (dados == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Dados do usuario sao obrigatorios.");

                return;
            }

            Usuario usuario = new Usuario();

            usuario.setId(id);
            usuario.setNome(dados.nome);
            usuario.setEmail(dados.email);
            usuario.setLogin(dados.login);
            usuario.setSenha(dados.senha);

            if (this.eAdministrador(perfilId)
                    && dados.perfilId != null) {

                usuario.setPerfilId(dados.perfilId);

            } else {

                usuario.setPerfilId(
                        existente.getPerfilId());
            }

            this.usuarioService.salvar(usuario);

            this.responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    Map.of(
                            "mensagem",
                            "Usuario atualizado com sucesso."));

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
                    "Erro ao atualizar usuario.");
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            Long perfilId =
                    (Long) req.getAttribute("perfilId");

            if (!this.eAdministrador(perfilId)) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Apenas administradores podem excluir usuarios.");

                return;
            }

            Long id = this.obterId(req);

            if (id == null) {
                this.erro(
                        resp,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Id do usuario invalido.");

                return;
            }

            this.usuarioService.deletar(id);

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
                    "Erro ao excluir usuario.");
        }
    }

    private boolean eAdministrador(Long perfilId) {

        return PERFIL_ADMIN_ID.equals(perfilId);
    }

    private Long obterId(HttpServletRequest req) {

        String pathInfo = req.getPathInfo();

        if (pathInfo == null
                || pathInfo.length() <= 1) {
            return null;
        }

        String valor = pathInfo.substring(1);

        if (valor.contains("/")) {
            return null;
        }

        try {
            return Long.valueOf(valor);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<UsuarioResponse> converterLista(
            List<Usuario> usuarios) {

        List<UsuarioResponse> resposta =
                new ArrayList<>();

        for (Usuario usuario : usuarios) {
            resposta.add(new UsuarioResponse(usuario));
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

    private static class UsuarioRequest {

        private String nome;
        private String email;
        private String login;
        private String senha;
        private Long perfilId;
    }

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
