
package br.com.intercambiolivros.service;

import br.com.intercambiolivros.dao.LivroDAO;
import br.com.intercambiolivros.dao.TrocaDAO;
import br.com.intercambiolivros.model.Livro;
import br.com.intercambiolivros.model.Troca;

import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

public class TrocaService {

    private final TrocaDAO trocaDAO;
    private final LivroDAO livroDAO;

    public TrocaService() {
        this.trocaDAO = new TrocaDAO();
        this.livroDAO = new LivroDAO();
    }

    public void solicitar(
            Long usuarioId,
            Long livroOferecidoId,
            Long livroRecebidoId,
            String mensagem) {

        this.validarUsuario(usuarioId);
        this.validarId(livroOferecidoId);
        this.validarId(livroRecebidoId);

        if (livroOferecidoId.equals(livroRecebidoId)) {
            throw new IllegalArgumentException(
                    "Os livros devem ser diferentes."
            );
        }

        mensagem = this.normalizar(mensagem);

        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Informe uma mensagem ou o local da troca."
            );
        }

        Livro livroOferecido =
                this.buscarLivroExistente(livroOferecidoId);

        Livro livroRecebido =
                this.buscarLivroExistente(livroRecebidoId);

        if (!Objects.equals(
                livroOferecido.getUsuarioId(),
                usuarioId)) {

            throw new SecurityException(
                    "O livro oferecido deve pertencer a voce."
            );
        }

        if (Objects.equals(
                livroRecebido.getUsuarioId(),
                usuarioId)) {

            throw new IllegalArgumentException(
                    "O livro solicitado deve pertencer a outra pessoa."
            );
        }

        if (!livroOferecido.isDisponivel()
                || !livroRecebido.isDisponivel()) {

            throw new IllegalStateException(
                    "Os dois livros precisam estar disponiveis para troca."
            );
        }

        if (this.trocaDAO.existeTrocaAtivaOuConcluidaParaLivro(
                livroOferecidoId)) {

            throw new IllegalStateException(
                    "Seu livro ja esta envolvido em uma troca."
            );
        }

        if (this.trocaDAO.existeTrocaAtivaOuConcluidaParaLivro(
                livroRecebidoId)) {

            throw new IllegalStateException(
                    "O livro escolhido ja esta envolvido em uma troca."
            );
        }

        Troca troca = new Troca();

        troca.setLivroOferecidoId(livroOferecidoId);
        troca.setLivroRecebidoId(livroRecebidoId);

        troca.setUsuarioOferecedorId(usuarioId);
        troca.setUsuarioRecebedorId(
                livroRecebido.getUsuarioId()
        );

        troca.setStatus("PENDENTE");
        troca.setMensagem(mensagem);

        this.trocaDAO.inserir(troca);
    }

    public List<Troca> listarPorUsuario(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.trocaDAO.listarPorUsuario(usuarioId);
    }

    public List<Troca> listarPendentesPorUsuario(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.trocaDAO.listarPendentesPorUsuario(usuarioId);
    }

    public List<Troca> listarConcluidasPorUsuario(Long usuarioId) {

        this.validarUsuario(usuarioId);

        return this.trocaDAO.listarConcluidasPorUsuario(usuarioId);
    }

    public Troca buscarPorId(Long id) {

        return this.buscarTrocaExistente(id);
    }

    public Troca buscarPorId(Long id, Long usuarioId) {

        this.validarUsuario(usuarioId);

        Troca troca = this.buscarTrocaExistente(id);

        this.validarParticipacao(troca, usuarioId);

        return troca;
    }

    public void aceitar(Long trocaId, Long usuarioId) {

        this.validarUsuario(usuarioId);

        Troca troca = this.buscarTrocaExistente(trocaId);

        this.validarTrocaPendente(troca);
        this.validarDonoDoLivroRecebido(troca, usuarioId);

        this.trocaDAO.alterarStatus(
                trocaId,
                "ACEITA"
        );
    }

    public void recusar(Long trocaId, Long usuarioId) {

        this.validarUsuario(usuarioId);

        Troca troca = this.buscarTrocaExistente(trocaId);

        this.validarTrocaPendente(troca);
        this.validarDonoDoLivroRecebido(troca, usuarioId);

        this.trocaDAO.alterarStatus(
                trocaId,
                "RECUSADA"
        );
    }

    public void concluir(Long trocaId, Long usuarioId) {

        this.validarUsuario(usuarioId);

        Troca troca = this.buscarTrocaExistente(trocaId);

        if (!"ACEITA".equals(troca.getStatus())) {
            throw new IllegalStateException(
                    "Somente trocas aceitas podem ser concluidas."
            );
        }

        this.validarParticipacao(troca, usuarioId);

        Livro livroOferecido = troca.getLivroOferecido();
        Livro livroRecebido = troca.getLivroRecebido();

        if (livroOferecido == null || livroRecebido == null) {
            throw new IllegalStateException(
                    "Nao foi possivel identificar os livros da troca."
            );
        }

        Long usuarioOferecedor = livroOferecido.getUsuarioId();
        Long usuarioRecebedor = livroRecebido.getUsuarioId();

        if (usuarioOferecedor == null || usuarioRecebedor == null) {
            throw new IllegalStateException(
                    "Nao foi possivel identificar os proprietarios dos livros."
            );
        }

        this.trocaDAO.concluir(
                troca,
                usuarioOferecedor,
                usuarioRecebedor
        );
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
                    "Id invalido."
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

    private Troca buscarTrocaExistente(Long id) {

        this.validarId(id);

        Troca troca = this.trocaDAO.buscarPorId(id);

        if (troca == null) {
            throw new NoSuchElementException(
                    "Troca nao encontrada."
            );
        }

        return troca;
    }

    private void validarTrocaPendente(Troca troca) {

        if (!"PENDENTE".equals(troca.getStatus())) {
            throw new IllegalStateException(
                    "Esta troca nao esta pendente."
            );
        }
    }

    private void validarDonoDoLivroRecebido(
            Troca troca,
            Long usuarioId) {

        Livro livroRecebido = troca.getLivroRecebido();

        if (livroRecebido == null) {
            throw new IllegalStateException(
                    "Nao foi possivel identificar o proprietario do livro."
            );
        }

        if (!Objects.equals(
                livroRecebido.getUsuarioId(),
                usuarioId)) {

            throw new SecurityException(
                    "Voce nao pode responder esta troca."
            );
        }
    }

    private void validarParticipacao(
            Troca troca,
            Long usuarioId) {

        Livro oferecido = troca.getLivroOferecido();
        Livro recebido = troca.getLivroRecebido();

        boolean participa =
                (oferecido != null
                        && Objects.equals(
                                oferecido.getUsuarioId(),
                                usuarioId))
                ||
                (recebido != null
                        && Objects.equals(
                                recebido.getUsuarioId(),
                                usuarioId));

        if (!participa) {
            throw new SecurityException(
                    "Voce nao participa desta troca."
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
