
package br.com.intercambiolivros.filter;

import br.com.intercambiolivros.config.JwtUtil;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class JwtFilter implements Filter {

    private static final Long PERFIL_ADMIN_ID = 1L;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String metodo = req.getMethod();

        // Permite acesso ao login e ao cadastro sem token
        if ("POST".equalsIgnoreCase(metodo)
                && (
                    "/api/auth/login".equals(servletPath)
                    || (
                        "/api/usuarios".equals(servletPath)
                        && "/cadastro".equals(pathInfo)
                    )
                )) {

            chain.doFilter(request, response);
            return;
        }

        // Verifica se o token foi informado
        String authorization = req.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"erro\":\"Token nao informado.\"}");
            return;
        }

        // Valida o token e obtém os dados do usuário
        String token = authorization.substring(7);

        Long usuarioId;
        Long perfilId;

        try {
            DecodedJWT jwt = JwtUtil.validarToken(token);

            usuarioId = Long.valueOf(jwt.getSubject());
            perfilId = jwt.getClaim("perfilId").asLong();

            if (perfilId == null) {
                throw new IllegalArgumentException("Perfil não informado no token.");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"erro\":\"Token invalido ou expirado.\"}");
            return;
        }

        req.setAttribute("usuarioId", usuarioId);
        req.setAttribute("perfilId", perfilId);

        //rotas exclusivas de administrador
        boolean listarUsuarios =
                "/api/usuarios".equals(servletPath)
                && "GET".equalsIgnoreCase(metodo)
                && (
                    pathInfo == null
                    || pathInfo.isEmpty()
                    || "/".equals(pathInfo)
                );

        boolean excluirUsuario =
                "/api/usuarios".equals(servletPath)
                && "DELETE".equalsIgnoreCase(metodo)
                && pathInfo != null
                && pathInfo.matches("/\\d+");

        if ((listarUsuarios || excluirUsuario)
                && !PERFIL_ADMIN_ID.equals(perfilId)) {

            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(
                    "{\"erro\":\"Acesso permitido somente para administradores.\"}"
            );
            return;
        }

        chain.doFilter(req, resp);
    }
}