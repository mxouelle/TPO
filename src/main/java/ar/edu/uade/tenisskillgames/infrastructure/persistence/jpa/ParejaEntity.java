package ar.edu.uade.tenisskillgames.infrastructure.persistence.jpa;

import ar.edu.uade.tenisskillgames.domain.enums.Lado;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Proyeccion JPA de PAREJAS / EQUIPOS de un partido de dobles (RF6).
 */
@Entity
@Table(name = "parejas")
public class ParejaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "partido_id", nullable = false)
    public UUID partidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lado", length = 15)
    public Lado lado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pareja_jugadores", joinColumns = @JoinColumn(name = "pareja_id"))
    @OrderColumn(name = "orden")
    @Column(name = "usuario_id")
    public List<UUID> jugadores = new ArrayList<>();
}
