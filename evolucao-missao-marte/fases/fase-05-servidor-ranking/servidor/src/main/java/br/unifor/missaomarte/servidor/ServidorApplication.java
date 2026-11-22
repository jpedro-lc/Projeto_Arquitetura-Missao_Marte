package br.unifor.missaomarte.servidor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Servidor de contas, histórico e ranking. Não executa a lógica do jogo. */
@SpringBootApplication
public class ServidorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServidorApplication.class, args);
    }
}
