
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

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse resp =
                (HttpServletResponse) response;

        String uri = req.getRequestURI();

        if (uri.endsWith("/api/auth/login")
                        || (uri.endsWith("/api/usuarios/cadastro")
                        && req.getMethod().equals("POST"))) {

                chain.doFilter(request, response);
                return;
        }
        
        String authorization =
                req.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            resp.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write(
                    "{\"erro\":\"Token nao informado.\"}");

            return;
        }

        String token = authorization.substring(7);

        try {

            DecodedJWT jwt =
                    JwtUtil.validarToken(token);

            Long usuarioId =
                    Long.valueOf(jwt.getSubject());

            Long perfilId =
                    jwt.getClaim("perfilId").asLong();

            req.setAttribute("usuarioId", usuarioId);
            req.setAttribute("perfilId", perfilId);

            chain.doFilter(req, resp);

        } catch (Exception e) {

            resp.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write(
                    "{\"erro\":\"Token invalido ou expirado.\"}");
        }
    }
}
