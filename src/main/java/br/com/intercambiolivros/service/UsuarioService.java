
package br.com.intercambiolivros.service;

import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

import br.com.intercambiolivros.dao.PerfilDAO;
import br.com.intercambiolivros.dao.UsuarioDAO;
import br.com.intercambiolivros.model.Usuario;

public class UsuarioService {

    private static final int SENHA_MINIMA = 6;
    private static final Long PERFIL_NORMAL_ID = 2L;
    private static final Long PERFIL_ADMIN_ID = 1L;

    private final UsuarioDAO usuarioDAO;
    private final PerfilDAO perfilDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.perfilDAO = new PerfilDAO();
    }

    public Usuario autenticar(String login, String senha) {

    login = this.normalizar(login);
    senha = this.normalizar(senha);

    if (login == null || senha == null) {
        throw new IllegalArgumentException(
                "Informe login e senha."
        );
    }

    Usuario usuario =
            this.usuarioDAO.buscarPorLoginESenha(login, senha);

    if (usuario == null) {
        throw new SecurityException(
                "Login ou senha invalidos."
        );
    }

    return usuario;
}

    public List<Usuario> listar(
            Long usuarioAutenticadoId,
            Long perfilAutenticadoId) {

        this.validarUsuarioAutenticado(
                usuarioAutenticadoId,
                perfilAutenticadoId
        );

        this.validarAdministrador(perfilAutenticadoId);

        return this.usuarioDAO.listarTodos();
    }

    public Usuario buscarPorId(Long id) {

        this.validarId(id);

        Usuario usuario = this.usuarioDAO.buscarPorId(id);

        if (usuario == null) {
            throw new NoSuchElementException(
                    "Usuario nao encontrado."
            );
        }

        return usuario;
    }

    public Usuario buscarPorId(
            Long id,
            Long usuarioAutenticadoId,
            Long perfilAutenticadoId) {

        this.validarUsuarioAutenticado(
                usuarioAutenticadoId,
                perfilAutenticadoId
        );

        this.validarId(id);

        if (!id.equals(usuarioAutenticadoId)
                && !this.eAdministrador(perfilAutenticadoId)) {

            throw new SecurityException(
                    "Voce nao pode consultar este usuario."
            );
        }

        return this.buscarPorId(id);
    }

    public void cadastrarNormal(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Usuario e obrigatorio."
            );
        }

        usuario.setId(null);
        usuario.setPerfilId(PERFIL_NORMAL_ID);

        this.salvar(usuario);
    }

    public void atualizar(
            Usuario usuario,
            Long usuarioAutenticadoId,
            Long perfilAutenticadoId) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Usuario e obrigatorio."
            );
        }

        this.validarUsuarioAutenticado(
                usuarioAutenticadoId,
                perfilAutenticadoId
        );

        this.validarId(usuario.getId());

        Usuario existente =
                this.buscarPorId(usuario.getId());

        if (!usuario.getId().equals(usuarioAutenticadoId)
                && !this.eAdministrador(perfilAutenticadoId)) {

            throw new SecurityException(
                    "Voce nao pode alterar este usuario."
            );
        }

        if (!this.eAdministrador(perfilAutenticadoId)
                || usuario.getPerfilId() == null) {

            usuario.setPerfilId(existente.getPerfilId());
        }

        this.salvar(usuario);
    }

    private void salvar(Usuario usuario) {

        this.prepararDados(usuario);

        this.validarCamposObrigatorios(usuario);
        this.validarEmail(usuario.getEmail());
        this.validarSenha(usuario.getSenha());
        this.validarPerfilExistente(usuario.getPerfilId());
        this.validarLoginUnico(usuario);
        this.validarEmailUnico(usuario);

        if (usuario.getId() == null) {
            this.usuarioDAO.inserir(usuario);
            return;
        }

        this.buscarPorId(usuario.getId());

        this.usuarioDAO.alterar(usuario);
    }

    public void deletar(
            Long id,
            Long usuarioAutenticadoId,
            Long perfilAutenticadoId) {

        this.validarUsuarioAutenticado(
                usuarioAutenticadoId,
                perfilAutenticadoId
        );

        this.validarAdministrador(perfilAutenticadoId);
        this.validarId(id);

        this.buscarPorId(id);

        this.usuarioDAO.deletar(id);
    }

    private void validarUsuarioAutenticado(
            Long usuarioId,
            Long perfilId) {

        if (usuarioId == null || perfilId == null) {
            throw new SecurityException(
                    "Usuario nao autenticado."
            );
        }
    }

    private void validarAdministrador(Long perfilId) {

        if (!this.eAdministrador(perfilId)) {
            throw new SecurityException(
                    "Acesso permitido somente para administradores."
            );
        }
    }

    public boolean eAdministrador(Long perfilId) {
        return PERFIL_ADMIN_ID.equals(perfilId);
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Id do usuario invalido."
            );
        }
    }

    private void prepararDados(Usuario usuario) {

        usuario.setNome(
                this.normalizar(usuario.getNome())
        );

        usuario.setEmail(
                this.normalizar(usuario.getEmail())
        );

        usuario.setLogin(
                this.normalizar(usuario.getLogin())
        );

        usuario.setSenha(
                this.normalizar(usuario.getSenha())
        );
    }

    private void validarCamposObrigatorios(Usuario usuario) {

        if (usuario.getNome() == null) {
            throw new IllegalArgumentException(
                    "Nome e obrigatorio."
            );
        }

        if (usuario.getEmail() == null) {
            throw new IllegalArgumentException(
                    "Email e obrigatorio."
            );
        }

        if (usuario.getLogin() == null) {
            throw new IllegalArgumentException(
                    "Login e obrigatorio."
            );
        }

        if (usuario.getSenha() == null) {
            throw new IllegalArgumentException(
                    "Senha e obrigatoria."
            );
        }

        if (usuario.getPerfilId() == null) {
            throw new IllegalArgumentException(
                    "Perfil e obrigatorio."
            );
        }
    }

    private void validarSenha(String senha) {

        if (senha.length() < SENHA_MINIMA) {
            throw new IllegalArgumentException(
                    "Senha deve ter no minimo "
                            + SENHA_MINIMA
                            + " caracteres."
            );
        }
    }

    private void validarEmail(String email) {

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException(
                    "Informe um email valido."
            );
        }
    }

    private void validarPerfilExistente(Long perfilId) {

        if (this.perfilDAO.buscarPorId(perfilId) == null) {
            throw new IllegalArgumentException(
                    "Perfil informado nao existe."
            );
        }
    }

    private void validarLoginUnico(Usuario usuario) {

        Usuario existente =
                this.usuarioDAO.buscarPorLogin(
                        usuario.getLogin()
                );

        if (existente != null
                && !Objects.equals(
                        existente.getId(),
                        usuario.getId())) {

            throw new IllegalStateException(
                    "Ja existe um usuario com este login."
            );
        }
    }

    private void validarEmailUnico(Usuario usuario) {

        Usuario existente =
                this.usuarioDAO.buscarPorEmail(
                        usuario.getEmail()
                );

        if (existente != null
                && !Objects.equals(
                        existente.getId(),
                        usuario.getId())) {

            throw new IllegalStateException(
                    "Ja existe um usuario com este email."
            );
        }
    }

    private String normalizar(String valor) {

        if (valor == null) {
            return null;
        }

        String limpo = valor.trim();

        return limpo.isEmpty() ? null : limpo;
    }
}
