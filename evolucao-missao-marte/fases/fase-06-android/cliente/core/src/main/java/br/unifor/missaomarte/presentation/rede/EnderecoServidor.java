package br.unifor.missaomarte.presentation.rede;

/**
 * Endereço padrão do servidor no Android, sem depender da API do Android (testável no core).
 * O emulador enxerga o localhost do computador como 10.0.2.2; o celular no cabo USB usa
 * 127.0.0.1 com "adb reverse tcp:8080 tcp:8080".
 */
public final class EnderecoServidor {
    public static final String EMULADOR = "http://10.0.2.2:8080";
    public static final String APARELHO_USB = "http://127.0.0.1:8080";

    private EnderecoServidor() { }

    /** Endereço informado no build (-Pmissaomarte.servidor) vence; vazio escolhe pelo tipo de aparelho. */
    public static String android(String configurado, boolean emulador) {
        if (configurado != null && !configurado.isBlank()) return configurado.trim();
        return emulador ? EMULADOR : APARELHO_USB;
    }
}
