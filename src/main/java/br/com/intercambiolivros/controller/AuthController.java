
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.GsonConfig;
import br.com.intercambiolivros.config.JwtUtil;
import br.com.intercambiolivros.model.Usuario;
import br.com.intercambiolivros.service.UsuarioService;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/auth/login")
public class AuthController extends HttpServlet {

    private final UsuarioService usuarioService =
            new UsuarioService();

    private final Gson gson =
            GsonConfig.getGson();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {

            LoginRequest dados = this.gson.fromJson(
                    req.getReader(),
                    LoginRequest.class);

            if (dados == null
                    || dados.login == null
                    || dados.senha == null
                    || dados.login.isBlank()
                    || dados.senha.isBlank()) {

                resp.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                resp.getWriter().write(
                        "{\"erro\":\"Login e senha sao obrigatorios.\"}");

                return;
            }

            Usuario usuario;

            try {

                usuario = this.usuarioService.autenticar(
                        dados.login,
                        dados.senha);

            } catch (IllegalArgumentException e) {

                resp.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                resp.getWriter().write(
                        "{\"erro\":\"Login ou senha invalidos.\"}");

                return;
            }

            String token = JwtUtil.gerarToken(
                    usuario.getId(),
                    usuario.getPerfilId());

            LoginResponse resultado =
                    new LoginResponse();

            resultado.token = token;
            resultado.usuarioId = usuario.getId();
            resultado.nome = usuario.getNome();
            resultado.perfilId = usuario.getPerfilId();

            resp.setStatus(
                    HttpServletResponse.SC_OK);

            resp.getWriter().write(
                    this.gson.toJson(resultado));

        } catch (com.google.gson.JsonParseException e) {

            resp.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            resp.getWriter().write(
                    "{\"erro\":\"JSON invalido.\"}");
        }
    }

    private static class LoginRequest {

        private String login;
        private String senha;
    }

    private static class LoginResponse {

        private String token;
        private Long usuarioId;
        private String nome;
        private Long perfilId;
    }
}
