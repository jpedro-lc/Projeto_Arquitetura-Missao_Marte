package br.unifor.missaomarte.servidor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import br.unifor.missaomarte.servidor.nuvem.UrlDoBanco;

/** Servidor de contas, histórico e ranking. Não executa a lógica do jogo. */
@SpringBootApplication
public class ServidorApplication {
    public static void main(String[] args) {
        // Na nuvem (Render) o banco chega em DATABASE_URL; propriedades de sistema vencem o H2 do application.properties.
        UrlDoBanco.paraSpring(System.getenv("DATABASE_URL")).forEach(System::setProperty);
        SpringApplication.run(ServidorApplication.class, args);
    }
}
