
package br.com.intercambiolivros.controller;

import br.com.intercambiolivros.config.JwtUtil;
import br.com.intercambiolivros.model.Usuario;
import br.com.intercambiolivros.service.UsuarioService;

import com.google.gson.JsonParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/auth/login")
public class AuthController extends BaseController {

    private final UsuarioService usuarioService =
            new UsuarioService();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            LoginRequest dados =
                    gson.fromJson(
                            req.getReader(),
                            LoginRequest.class
                    );

            // O Service será responsável por validar
            // se login e senha foram informados.
            String login = dados == null
                    ? null
                    : dados.login;

            String senha = dados == null
                    ? null
                    : dados.senha;

            Usuario usuario =
                    usuarioService.autenticar(login, senha);

            // Gera o token após autenticar o usuário.
            String token = JwtUtil.gerarToken(
                    usuario.getId(),
                    usuario.getPerfilId()
            );

            LoginResponse resultado =
                    new LoginResponse();

            resultado.token = token;
            resultado.usuarioId = usuario.getId();
            resultado.nome = usuario.getNome();
            resultado.perfilId = usuario.getPerfilId();

            responderJson(
                    resp,
                    HttpServletResponse.SC_OK,
                    resultado
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

        } catch (SecurityException e) {

            erro(
                    resp,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            erro(
                    resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao realizar login."
            );
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
