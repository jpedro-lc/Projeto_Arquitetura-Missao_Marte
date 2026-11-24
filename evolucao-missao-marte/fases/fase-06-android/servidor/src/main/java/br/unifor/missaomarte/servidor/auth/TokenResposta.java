package br.unifor.missaomarte.servidor.auth;

import java.time.Instant;

public record TokenResposta(String token, String tipo, Instant expiraEm) { }
