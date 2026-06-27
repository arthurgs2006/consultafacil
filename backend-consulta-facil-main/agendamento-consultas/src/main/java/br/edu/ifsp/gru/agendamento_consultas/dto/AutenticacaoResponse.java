package br.edu.ifsp.gru.agendamento_consultas.dto;

/**
 * DTO de resposta após autenticação bem-sucedida (registro ou login).
 *
 * @param token       token JWT a ser incluído no header {@code Authorization: Bearer <token>}
 * @param nomeUsuario nome de exibição do usuário autenticado
 * @param papel       perfil de acesso ({@code USER} ou {@code ADMIN})
 */
public record AutenticacaoResponse(String token, String nomeUsuario, String papel) {}
