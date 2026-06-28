package br.edu.ifsp.gru.agendamento_consultas.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções que padroniza as respostas de erro da API.
 *
 * <p>Garante que nenhuma exceção chegue ao cliente (ou ao console) como a página
 * padrão do Spring/Tomcat ou um stack trace bruto: cada tipo conhecido é convertido
 * em um JSON {@code {"error": "<mensagem>"}} com o status HTTP adequado, e qualquer
 * exceção não mapeada cai no handler genérico, que loga o erro no servidor e responde
 * {@code 500} com uma mensagem neutra.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Converte uma {@link AppException} em resposta HTTP com o status e a mensagem definidos.
     *
     * @param ex exceção de negócio lançada pelos serviços
     * @return resposta com o status e o corpo {@code {"error": "<mensagem>"}}
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, String>> handleApp(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Converte erros de validação em uma resposta {@code 400 Bad Request}
     * com os campos inválidos e as respectivas mensagens.
     *
     * @param ex exceção gerada pelo Spring ao processar um {@code @Valid} com erros
     * @return resposta {@code 400} com o corpo {@code {"errors": {"campo": "mensagem"}}}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    /**
     * Converte falhas de autenticação (ex: credenciais inválidas no login) em {@code 401 Unauthorized}.
     *
     * @param ex exceção lançada pelo {@link org.springframework.security.authentication.AuthenticationManager}
     * @return resposta {@code 401} com o corpo {@code {"error": "Credenciais inválidas"}}
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciais inválidas"));
    }

    /**
     * Converte falhas de autorização (ex: role insuficiente em rota restrita) em {@code 403 Forbidden}.
     *
     * @param ex exceção lançada pelo Spring Security quando o usuário autenticado não tem permissão
     * @return resposta {@code 403} com o corpo {@code {"error": "Acesso negado"}}
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso negado"));
    }

    /**
     * Converte requisições malformadas (parâmetro ausente, tipo inválido, método HTTP ou rota
     * inexistente) em {@code 400 Bad Request}, evitando expor a página de erro padrão do Spring.
     *
     * @param ex exceção de requisição inválida capturada
     * @return resposta {@code 400} com o corpo {@code {"error": "<mensagem>"}}
     */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpRequestMethodNotSupportedException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Requisição inválida"));
    }

    /**
     * Último recurso: captura qualquer exceção não mapeada, registra o stack trace completo
     * no log do servidor e retorna {@code 500 Internal Server Error} com uma mensagem genérica,
     * sem expor detalhes internos ao cliente.
     *
     * @param ex exceção não tratada por nenhum handler específico
     * @return resposta {@code 500} com o corpo {@code {"error": "Erro interno no servidor"}}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro interno no servidor"));
    }
}