package br.unifor.missaomarte.servidor.partida;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo de POST /api/partidas. Não há campo de usuário: ele sai do token (um "usuario" no JSON é ignorado).
 * Tipos que aceitam null fazem um campo ausente virar 400, e não zero. Os limites só barram valores absurdos.
 */
public record PartidaRequisicao(
        @NotNull(message = "Informe a dificuldade.") Dificuldade dificuldade,
        @NotNull(message = "Informe o resultado.") Resultado resultado,
        @NotNull(message = "Informe a pontuação.")
        @Min(value = -100_000, message = "Pontuação fora da faixa.")
        @Max(value = 100_000, message = "Pontuação fora da faixa.") Integer pontuacao,
        @NotNull(message = "Informe a duração.")
        @Min(value = 0, message = "Duração inválida.")
        @Max(value = 86_400, message = "Duração inválida.") Long duracaoSegundos,
        @NotNull(message = "Informe os movimentos.")
        @Min(value = 0, message = "Número de movimentos inválido.")
        @Max(value = 100_000, message = "Número de movimentos inválido.") Integer movimentos) {
}
