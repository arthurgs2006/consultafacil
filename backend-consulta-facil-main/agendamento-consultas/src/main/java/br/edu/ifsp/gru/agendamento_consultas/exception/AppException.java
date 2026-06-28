package br.edu.ifsp.gru.agendamento_consultas.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de negócio customizada que carrega um status HTTP.
 *
 * <p>Lançada pelos serviços quando uma regra de negócio é violada
 * (ex: acesso negado, recurso não encontrado, conflito de dados).
 * É capturada pelo {@link GlobalExceptionHandler} e convertida
 * em uma resposta HTTP com o status e a mensagem adequados.</p>
 */
public class AppException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Cria uma nova exceção de negócio.
     *
     * @param message mensagem descritiva do erro, retornada no corpo da resposta
     * @param status  status HTTP que deve ser retornado ao cliente
     */
    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Retorna o status HTTP associado a esta exceção.
     *
     * @return status HTTP
     */
    public HttpStatus getStatus() {
        return status;
    }
}