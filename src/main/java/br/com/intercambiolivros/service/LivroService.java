package br.com.intercambiolivros.service;

import br.com.intercambiolivros.dao.LivroDAO;
import br.com.intercambiolivros.model.Livro;

import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

public class LivroService {

    private final LivroDAO livroDAO;

    public LivroService() {
        this.livroDAO = new LivroDAO();
    }

    public List<Livro> listar() {
        return this.livroDAO.listarTodos();
    }

    public List<Livro> listarDisponiveis(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.livroDAO.listarDisponiveis(usuarioId);
    }

    public List<Livro> listarPorUsuario(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.livroDAO.listarPorUsuario(usuarioId);
    }

    public List<Livro> listarDisponiveisPorUsuario(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.livroDAO.listarDisponiveisPorUsuario(usuarioId);
    }

    public Livro buscarPorId(Long id) {

        this.validarId(id);

        return this.buscarLivroExistente(id);
    }

    public Livro buscarPorId(Long id, Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.buscarPorId(id);
    }

    public void alterarDisponibilidade(Long id, Long usuarioId) {

        this.validarId(id);
        this.validarUsuario(usuarioId);

        Livro livro = this.buscarLivroExistente(id);

        this.validarProprietario(
                livro,
                usuarioId,
                "Voce nao pode alterar este livro."
        );

        if (this.livroDAO.existeTrocaAtivaParaLivro(id)) {
            throw new IllegalStateException(
                    "Este livro esta envolvido em uma troca."
            );
        }

        this.livroDAO.alterarDisponibilidade(
                id,
                !livro.isDisponivel()
        );
    }

    public void salvar(Livro livro, Long usuarioId) {

        if (livro == null) {
            throw new IllegalArgumentException(
                    "Livro e obrigatorio."
            );
        }

        this.validarUsuario(usuarioId);

        this.prepararDados(livro);
        this.validarCamposObrigatorios(livro);

        if (livro.getId() == null) {

            livro.setUsuarioId(usuarioId);
            this.livroDAO.inserir(livro);

            return;
        }

        Livro existente =
                this.buscarLivroExistente(livro.getId());

        this.validarProprietario(
                existente,
                usuarioId,
                "Voce nao pode alterar este livro."
        );

        livro.setUsuarioId(usuarioId);

        this.livroDAO.alterar(livro);
    }

    public void deletar(Long id, Long usuarioId) {

        this.validarId(id);
        this.validarUsuario(usuarioId);

        Livro livro = this.buscarLivroExistente(id);

        this.validarProprietario(
                livro,
                usuarioId,
                "Voce nao pode excluir este livro."
        );

        if (this.livroDAO.existeTrocaParaLivro(id)) {
            throw new IllegalStateException(
                    "Nao e possivel excluir um livro que "
                    + "ja participou de uma troca."
            );
        }

        this.livroDAO.deletar(id);
    }

    private void validarUsuario(Long usuarioId) {

        if (usuarioId == null) {
            throw new SecurityException(
                    "Usuario nao autenticado."
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Id do livro invalido."
            );
        }
    }

    private Livro buscarLivroExistente(Long id) {

        Livro livro = this.livroDAO.buscarPorId(id);

        if (livro == null) {
            throw new NoSuchElementException(
                    "Livro nao encontrado."
            );
        }

        return livro;
    }

    private void validarProprietario(
            Livro livro,
            Long usuarioId,
            String mensagem) {

        if (!Objects.equals(livro.getUsuarioId(), usuarioId)) {
            throw new SecurityException(mensagem);
        }
    }

    private void prepararDados(Livro livro) {

        livro.setTitulo(
                this.normalizar(livro.getTitulo())
        );

        livro.setAutor(
                this.normalizar(livro.getAutor())
        );
    }

    private void validarCamposObrigatorios(Livro livro) {

        if (livro.getTitulo() == null) {
            throw new IllegalArgumentException(
                    "Titulo e obrigatorio."
            );
        }

        if (livro.getAutor() == null) {
            throw new IllegalArgumentException(
                    "Autor e obrigatorio."
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
