package ar.edu.uade.tenisskillgames;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TenisSkillGames - plataforma de organizacion y emparejamiento de partidos
 * amistosos de tenis (UADE TPO Final).
 *
 * Arquitectura en capas: web -> application -> domain <- infrastructure.
 */
@SpringBootApplication
@EnableScheduling
public class TenisSkillGamesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenisSkillGamesApplication.class, args);
    }
}
