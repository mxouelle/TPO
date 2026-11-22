# Diagramas — TenisSkillGames

## 1. Ciclo de vida del Partido (patrón State)

```mermaid
stateDiagram-v2
    [*] --> BUSCANDO_JUGADORES : PartidoBuilder.build()
    BUSCANDO_JUGADORES --> CUPO_COMPLETO : postular() y cupo lleno\n(PartidoCupoCompletoEvent)
    CUPO_COMPLETO --> BUSCANDO_JUGADORES : baja / rechazo de confirmación
    CUPO_COMPLETO --> CONFIRMADO : todos confirman
    CONFIRMADO --> EN_JUEGO : scheduler / iniciar()
    EN_JUEGO --> FINALIZADO : finalizar() o expiración de duración
    BUSCANDO_JUGADORES --> CANCELADO : cancelar()
    CUPO_COMPLETO --> CANCELADO : cancelar()
    CONFIRMADO --> CANCELADO : cancelar()
    FINALIZADO --> [*] : cargar resultado + evaluaciones
    CANCELADO --> [*]
```

Transiciones denegadas (todas lanzan `IllegalStateException`, traducida a `409`):

- `BUSCANDO_JUGADORES` → `confirmar()`, `iniciar()`, `finalizar()`
- `CUPO_COMPLETO` → `iniciar()`, `finalizar()`
- `CONFIRMADO` → `postular()`, `finalizar()`
- `EN_JUEGO` → `postular()`, `confirmar()`, `cancelar()`
- `FINALIZADO` / `CANCELADO` → todas

## 2. Diagrama de clases (patrones)

```mermaid
classDiagram
    class Partido {
        -UUID id
        -PartidoState estadoActual
        -EstadoPartido estado
        -int cupoMaximo
        -List~Usuario~ participantes
        -List~Postulacion~ postulaciones
        -List~Pareja~ parejas
        -List~TransicionEstado~ historialEstados
        +postular(Usuario)
        +confirmar(Usuario)
        +iniciar()
        +finalizar()
        +cancelar(String)
        +cambiarEstado(PartidoState)
        +publicarEvento(DomainEvent)
        +getNtrpPromedio() double
    }

    class PartidoState {
        <<interface>>
        +postular(Partido, Usuario)
        +confirmar(Partido, Usuario)
        +iniciar(Partido)
        +finalizar(Partido)
        +cancelar(Partido)
        +getEstado() EstadoPartido
    }

    class BuscandoJugadoresState
    class CupoCompletoState
    class ConfirmadoState
    class EnJuegoState
    class FinalizadoState
    class CanceladoState

    Partido --> PartidoState : delega
    PartidoState <|.. BuscandoJugadoresState
    PartidoState <|.. CupoCompletoState
    PartidoState <|.. ConfirmadoState
    PartidoState <|.. EnJuegoState
    PartidoState <|.. FinalizadoState
    PartidoState <|.. CanceladoState

    class PartidoBuilder {
        +organizador(Usuario) PartidoBuilder
        +formato(FormatoTenis) PartidoBuilder
        +rangoNTRP(double, double) PartidoBuilder
        +sedeYFecha(String, Superficie, LocalDateTime) PartidoBuilder
        +duracionMinutos(int) PartidoBuilder
        +radioMaxKm(int) PartidoBuilder
        +build() Partido
    }
    PartidoBuilder ..> Partido : crea

    class MatchmakingStrategy {
        <<interface>>
        +seleccionarCandidatos(List~Usuario~, Partido) List~Usuario~
        +getCodigo() String
    }
    class PorNivelNTRPStrategy
    class PorCercaniaStrategy
    class PorHistorialStrategy
    class MatchmakingStrategyRegistry
    MatchmakingStrategy <|.. PorNivelNTRPStrategy
    MatchmakingStrategy <|.. PorCercaniaStrategy
    MatchmakingStrategy <|.. PorHistorialStrategy
    MatchmakingStrategyRegistry --> MatchmakingStrategy

    class DomainEventBus {
        -List~DomainEventSubscriber~ suscriptores
        +subscribe(DomainEventSubscriber)
        +unsubscribe(DomainEventSubscriber)
        +publish(DomainEvent)
    }
    class DomainEventSubscriber {
        <<interface>>
        +onEvent(DomainEvent)
    }
    class NotificacionSubscriber
    class AlertaBusquedaSubscriber
    class AuditoriaSubscriber
    DomainEventBus --> DomainEventSubscriber
    DomainEventSubscriber <|.. NotificacionSubscriber
    DomainEventSubscriber <|.. AlertaBusquedaSubscriber
    DomainEventSubscriber <|.. AuditoriaSubscriber

    class NotifierFactory {
        <<interface>>
        +createPushNotifier() Notifier
        +createEmailNotifier() Notifier
        +createChatNotifier() Notifier
    }
    class DevNotifierFactory
    class ProdNotifierFactory
    class Notifier {
        <<interface>>
        +enviar(Notificacion)
    }
    class WhatsAppBotAdapter
    class SendGridEmailAdapter
    class FirebasePushAdapter
    NotifierFactory <|.. DevNotifierFactory
    NotifierFactory <|.. ProdNotifierFactory
    Notifier <|.. WhatsAppBotAdapter
    Notifier <|.. SendGridEmailAdapter
    Notifier <|.. FirebasePushAdapter
    ProdNotifierFactory ..> WhatsAppBotAdapter : construye
    ProdNotifierFactory ..> SendGridEmailAdapter : construye
    ProdNotifierFactory ..> FirebasePushAdapter : construye

    class ICalendarAdapter {
        +generarArchivoICS(Partido) String
        +generar(Partido) byte[]
        +nombreArchivo(Partido) String
    }
```

## 3. Secuencia — crear → postular → confirmar → notificar

```mermaid
sequenceDiagram
    actor J1 as Organizador
    actor J2 as Jugador
    participant C as PartidoController
    participant S as PartidoService
    participant A as Partido (dominio)
    participant R as PartidoJpaAdapter
    participant P as PublicadorEventosTransaccional
    participant B as DomainEventBus
    participant N as NotificacionSubscriber
    participant F as NotifierFactory
    actor D as Destinatario

    J1->>C: POST /api/partidos
    C->>S: crear(organizadorId, cmd)
    S->>A: PartidoBuilder...build()
    A-->>S: Partido en BUSCANDO_JUGADORES
    S->>R: guardar(partido)
    S->>A: publicarEvento(PartidoCreadoEvent)
    A->>P: publish()
    Note over P: buffer en ThreadLocal (tx activa)
    S-->>C: 201 Created
    C-->>J1: PartidoDTO
    Note over P: afterCommit → bus.publish(eventos)
    P->>B: publish(PartidoCreadoEvent)
    B->>N: onEvent() (alertas de búsqueda / auditoría)

    J2->>C: POST /api/partidos/{id}/postulaciones
    C->>S: postular(...)
    S->>A: postular(jugador)
    A->>A: BuscandoJugadoresState.postular → cupo lleno
    A->>A: cambiarEstado(CupoCompletoState)
    A->>P: PartidoEstadoCambiadoEvent + PartidoCupoCompletoEvent
    S->>R: guardar(partido)
    S-->>C: 200 OK (estado CUPO_COMPLETO)

    Note over P,B: afterCommit
    P->>B: publicar eventos
    B->>N: onEvent(CUPO_COMPLETO)
    N->>F: createPushNotifier / createEmailNotifier / createChatNotifier
    F-->>N: Notifier (Dev o Prod)
    N->>D: enviar(mensaje) por cada canal
```

## 4. Despliegue lógico

```mermaid
flowchart LR
    subgraph Cliente
        APP[App móvil / Panel web]
    end
    subgraph Backend["Spring Boot :8080"]
        SEC[JWT + Rate limit]
        API[API REST]
        DOM[Dominio + patrones]
        SCH[Scheduler 60s]
        NOT[Notificadores]
    end
    subgraph Datos
        DB[(H2 dev / PostgreSQL prod)]
    end
    subgraph Externos
        FB[Firebase]
        SG[SendGrid]
        WA[WhatsApp Cloud]
        CAL[Google / Apple Calendar]
    end

    APP -->|HTTPS + Bearer| SEC --> API --> DOM
    DOM --> DB
    DOM -.eventos.-> NOT
    SCH --> DOM
    NOT --> FB
    NOT --> SG
    NOT --> WA
    API -->|.ics| CAL
```

## 5. DER

```mermaid
erDiagram
    USUARIOS ||--o{ PARTIDOS : organiza
    USUARIOS ||--o{ POSTULACIONES : postula
    PARTIDOS ||--o{ POSTULACIONES : recibe
    PARTIDOS ||--o{ PAREJAS : conforma
    PAREJAS ||--o{ PAREJA_JUGADORES : integra
    PARTIDOS ||--o| RESULTADOS_PARTIDO : registra
    USUARIOS ||--o{ NOTIFICACIONES : recibe
    PARTIDOS ||--o{ NOTIFICACIONES : genera
    PARTIDOS ||--o{ EVALUACIONES : habilita
    USUARIOS ||--o{ EVALUACIONES : evalua
    PARTIDOS ||--o{ TRANSICIONES_ESTADO : audita
    PARTIDOS ||--o{ REPORTES_CONDUCTA : origina
    USUARIOS ||--o{ BUSQUEDAS_FAVORITAS : guarda

    USUARIOS {
        UUID id PK
        VARCHAR email UK
        VARCHAR nombre
        DECIMAL nivel_ntrp
        VARCHAR zona
        INT strikes
        BOOLEAN verificado
        BOOLEAN activo
    }
    PARTIDOS {
        UUID id PK
        UUID organizador_id FK
        VARCHAR formato
        VARCHAR superficie
        VARCHAR club_sede
        DECIMAL ntrp_min
        DECIMAL ntrp_max
        TIMESTAMP fecha_hora
        VARCHAR estado_actual
        INT cupo_maximo
    }
    POSTULACIONES {
        UUID id PK
        UUID usuario_id FK
        UUID partido_id FK
        CHAR lado_asignado
        BOOLEAN confirmado
        VARCHAR estado
    }
    RESULTADOS_PARTIDO {
        UUID id PK
        UUID partido_id FK "UK"
        CHAR ganador_lado
        VARCHAR marcador_sets
        INT duracion_min
    }
    NOTIFICACIONES {
        UUID id PK
        UUID usuario_id FK
        UUID partido_id FK
        VARCHAR canal
        VARCHAR estado_envio
        TIMESTAMP fecha_envio
    }
    EVALUACIONES {
        UUID id PK
        UUID partido_id FK
        UUID evaluador_id FK
        UUID evaluado_id FK
        SMALLINT fair_play
        SMALLINT puntualidad
        VARCHAR moderacion
    }
```
