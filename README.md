# TenisSkillGames — TPO Final (UADE)

Plataforma de organización y emparejamiento de partidos amistosos de tenis.
Backend REST en **Java 17 + Spring Boot 3.5** con arquitectura en capas, modelo de dominio
desacoplado de Spring/JPA, seis patrones de diseño implementados de forma explícita y una
suite de pruebas unitarias, de integración y de rendimiento.

| Dato | Valor |
|---|---|
| Proyecto | TenisSkillGames (App Móvil + Backend REST) |
| Integrante | Nicolás Méndez (LU: 1172141) |
| Stack | Java 17+, Spring Boot, Spring Data JPA |
| Base de datos | H2 en memoria (desarrollo/pruebas) · PostgreSQL 15 (producción) |
| Estado | Compila, 45 pruebas automatizadas en verde |

---

## 1. Qué resuelve

Tenistas amateurs y federados pueden:

1. Registrarse con perfil tenístico (NTRP 1.5–7.0, mano hábil, golpe preferido, superficie, zona).
2. Buscar partidos con filtros dinámicos (formato, superficie, banda NTRP, radio en km, fecha, franja horaria).
3. Crear partidos amistosos validados paso a paso (Singles / Dobles / Dobles Mixtos).
4. Postularse, confirmar asistencia y quedar en lista de espera si no hay cupo.
5. Emparejar jugadores con tres algoritmos intercambiables (NTRP, cercanía, historial).
6. Cerrar el partido, cargar el marcador por sets y evaluar fair play.
7. Recibir notificaciones por Push, Email y WhatsApp/Telegram según el evento de estado.
8. Descargar la invitación `.ics` del encuentro confirmado.

---

## 2. Cómo ejecutar

### Requisitos
- JDK 17 o superior (probado con JDK 21/25).
- Maven 3.9+ (o usar el wrapper incluido).

### Desarrollo (H2 en memoria, notificadores simulados)
```bash
./mvnw spring-boot:run          # Linux/macOS
mvnw.cmd spring-boot:run        # Windows
```
La API queda en `http://localhost:8080`. Consola H2: `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:tenisskillgames`, usuario `sa`, sin contraseña).

Al arrancar se cargan **datos de demostración** (`tenis.demo.datos=true`):

| Rol | Email | Password |
|---|---|---|
| ADMIN | `admin@tenisskillgames.ar` | `Admin1234` |
| ORGANIZADOR | `nico@tenisskillgames.ar` | `Password123` |
| JUGADOR | `ana@tenisskillgames.ar` | `Password123` |
| JUGADOR | `bruno@tenisskillgames.ar` | `Password123` |

### Pruebas
```bash
./mvnw test
```
Resultado esperado: `Tests run: 45, Failures: 0, Errors: 0, Skipped: 0`.
El detalle está en [`docs/PRUEBAS.md`](docs/PRUEBAS.md).

### Producción (PostgreSQL + proveedores reales)
```bash
export DB_URL=jdbc:postgresql://localhost:5432/tenisskillgames
export DB_USER=postgres
export DB_PASSWORD=secreto
export JWT_SECRET="clave-de-al-menos-32-bytes-para-hs256"
export SPRING_PROFILES_ACTIVE=prod
java -jar target/tenis-skill-games.jar
```
En el perfil `prod` se activa `ProdNotifierFactory` (Firebase + SendGrid + WhatsApp Cloud).
Las credenciales se toman de `tenis.notificaciones.*.token` / `api-key` (ver `application-prod.yml`).
El DDL de PostgreSQL está en [`docs/schema-postgresql.sql`](docs/schema-postgresql.sql)
(alternativa rápida: `spring.jpa.hibernate.ddl-auto=update` en el primer arranque).

---

## 3. Arquitectura

```
                       ┌──────────────────────────────────────────┐
  HTTP / JWT  ───────► │  controller     Controllers + DTO        │
                       │  (AuthController, PartidoController...)  │
                       └───────────────────┬──────────────────────┘
                                           │
                       ┌───────────────────▼──────────────────────┐
                       │  service        Casos de uso / servicios  │
                       │  PartidoService, MatchmakingService, ...  │
                       └───────────────────┬──────────────────────┘
                                           │  (puertos)
        ┌──────────────────────────────────▼───────────────────────────────────┐
        │  domain   Modelo rico: Partido, Usuario, State, Strategy, Builder,   │
        │           Observer, Abstract Factory, Adapter (interfaces), ports     │
        │           → SIN dependencias de Spring ni JPA (exception: @Component  │
        │             en las 3 estrategias, tal como exige el esqueleto del PDF)│
        └──────────────────────────────────▲───────────────────────────────────┘
                                           │  (adaptadores)
        ┌──────────────────────────────────┴───────────────────────────────────┐
        │  infrastructure   JPA/Hibernate (entities + adapters), JWT/Security, │
        │                   notificadores (Firebase/SendGrid/WhatsApp), iCal,   │
        │                   scheduler, bus de eventos transaccional             │
        └──────────────────────────────────────────────────────────────────────┘
```

- **Dominio puro**: `Partido`, `Usuario`, estados, estrategias y eventos no conocen Spring.
  La persistencia se resuelve con *ports* (`PartidoRepository`, `UsuarioRepository`, …) y
  adaptadores JPA en `infrastructure`.
- **Control transaccional** en los servicios (`@Transactional`); los adaptadores hidratan el
  agregado completo (participantes, postulaciones, parejas, transiciones, resultado).
- **Eventos después del commit**: `PublicadorEventosTransaccional` acumula los eventos de la
  transacción y los publica recién en `afterCompletion`, evitando lecturas sucias y
  permitiendo el despacho asíncrono de notificaciones.

Detalle completo en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

---

## 4. Patrones de diseño implementados

| Patrón | Categoría | Implementación | Rol en el sistema |
|---|---|---|---|
| **State** | Comportamiento | `domain/state/PartidoState` + `BuscandoJugadoresState`, `CupoCompletoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState` | Ciclo de vida del partido sin condicionales; cada estado decide si la operación es válida y lanza `IllegalStateException` cuando no lo es. |
| **Strategy** | Comportamiento | `MatchmakingStrategy` + `PorNivelNTRPStrategy` (`@Component("NTRP")`), `PorCercaniaStrategy` (`CERCANIA`), `PorHistorialStrategy` (`HISTORIAL`) + `MatchmakingStrategyRegistry` | Criterio de selección de candidatos intercambiable en runtime vía `?estrategia=`. |
| **Observer** | Comportamiento | `DomainEventBus` (subject), `DomainEventSubscriber`, `NotificacionSubscriber`, `AlertaBusquedaSubscriber`, `AuditoriaSubscriber` | Desacopla las reacciones secundarias (notificar, alertar búsquedas, auditar) de la transición de estado. |
| **Abstract Factory** | Creacional | `NotifierFactory` + `DevNotifierFactory` (consola) / `ProdNotifierFactory` (Firebase, SendGrid, WhatsApp) | Familia coherente de notificadores según el entorno (`tenis.notificaciones.modo`). |
| **Builder** | Creacional | `PartidoBuilder` | Construcción paso a paso con validación de invariantes (cupo 2/4, NTRP 1.5–7.0, fecha futura, creador auto-inscripto). |
| **Adapter** | Estructural | `WhatsAppBotAdapter`, `SendGridEmailAdapter`, `FirebasePushAdapter`, `ICalendarAdapter` | Adapta APIs externas incompatibles (WhatsApp Cloud, SendGrid, FCM) y genera `.ics` RFC 5545. |

Diagramas en [`docs/DIAGRAMAS.md`](docs/DIAGRAMAS.md).

---

## 5. Modelo de dominio

`Usuario` · `Partido` · `Pareja` · `Postulacion` · `Confirmacion` · `Notificacion` ·
`ResultadoPartido` (+ `SetMarcador`) · `EvaluacionJugador` · `ReporteConducta` ·
`BusquedaFavorita` · `TransicionEstado` (auditoría).

Reglas de negocio encapsuladas en el dominio:

- NTRP entre 1.5 y 7.0 en pasos de 0.5.
- Singles ⇒ cupo 2; Dobles / Dobles Mixtos ⇒ cupo 4.
- 3 strikes ⇒ suspensión automática de 7 días.
- Confirmación al menos 2 h antes del inicio.
- Cancelación dentro de las últimas 3 h ⇒ 1 strike (salvo motivos climáticos).
- Marcador válido: 6-x con diferencia ≥ 2, 7-5, 7-6 o super tie-break 10-x con diferencia ≥ 2.
- Diferencia de NTRP promedio entre parejas ≤ 0.5.

---

## 6. API REST

Base: `http://localhost:8080`. Autenticación: `Authorization: Bearer <jwt>`.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registra tenista con perfil NTRP y zona (estado `PENDIENTE`). |
| POST | `/api/auth/verificar-email` | Verifica la cuenta con el token enviado por email. |
| POST | `/api/auth/login` | Autentica y devuelve JWT. |
| POST | `/api/auth/oauth2/google` | Login delegado (Google) simulado. |
| GET | `/api/auth/me` | Perfil del usuario autenticado. |
| GET | `/api/partidos` | Búsqueda con filtros dinámicos. |
| POST | `/api/partidos` | Crea partido con `PartidoBuilder`. |
| GET | `/api/partidos/{id}` | Detalle con participantes, parejas e historial de estados. |
| POST | `/api/partidos/{id}/postulaciones` | Postulación con evaluación de compatibilidad. |
| DELETE | `/api/partidos/{id}/postulaciones` | Baja de participante (libera plaza y promueve lista de espera). |
| POST | `/api/partidos/{id}/confirmaciones` | Confirma o rechaza asistencia. |
| POST | `/api/partidos/{id}/emparejar?estrategia=NTRP\|CERCANIA\|HISTORIAL` | Ejecuta la estrategia seleccionada. |
| GET | `/api/partidos/{id}/calendario.ics` | Exporta el encuentro confirmado (iCal). |
| POST | `/api/partidos/{id}/cancelar` | Cancela antes del inicio y notifica. |
| POST | `/api/partidos/{id}/iniciar` | Inicia el partido (organizador / scheduler). |
| POST | `/api/partidos/{id}/finalizar` | Cierra el partido en juego. |
| POST | `/api/partidos/{id}/resultados` | Carga marcador por sets y duración real. |
| POST | `/api/partidos/{id}/evaluaciones` | Evaluación de fair play y puntualidad (1–5). |
| POST | `/api/partidos/{id}/reportes` | Reporta no-show o conducta antideportiva. |
| POST | `/api/partidos/{id}/parejas` | Reasigna una pareja (dobles). |
| GET | `/api/usuarios/me` | Perfil, reputación y strikes. |
| PUT | `/api/usuarios/me` | Actualiza perfil tenístico y canales. |
| GET | `/api/usuarios/me/partidos` | Historial de partidos del jugador. |
| GET | `/api/usuarios/me/notificaciones` | Bandeja de notificaciones. |
| GET | `/api/usuarios/me/busquedas` · POST · DELETE | Búsquedas favoritas y alertas. |
| GET | `/api/admin/reportes` | Reportes pendientes de moderación. |
| POST | `/api/admin/reportes/{id}/resolver` | Confirma (aplica strike) o desestima. |
| GET | `/api/admin/estadisticas` | Métricas generales. |
| GET | `/api/admin/notificaciones` | Auditoría de envíos. |

Ejemplos `curl` y colección Postman en [`docs/API.md`](docs/API.md) y
[`postman/TenisSkillGames.postman_collection.json`](postman/TenisSkillGames.postman_collection.json).

---

## 7. Seguridad

- Contraseñas con **BCrypt**.
- **JWT HS256** stateless con claims de rol (`JUGADOR`, `ORGANIZADOR`, `ADMIN`).
- Autorización por roles (`/api/admin/**` sólo `ADMIN`) y validación de propiedad del recurso
  (sólo el organizador puede cancelar/finalizar/emparejar).
- **Rate limiting** de ventana deslizante en `/api/auth/**` (5 req/min por IP y endpoint).
- Bloqueo de login tras 5 intentos fallidos en un minuto (CU2).
- Verificación de email vía token de un solo propósito (24 h).
- Errores homogéneos (`status`, `error`, `mensaje`, `path`, `detalles`), sin filtrar stack traces.

---

## 8. Estructura de carpetas

```
src/main/java/ar/edu/uade/tenisskillgames
├── controller
│   ├── dto          Comandos / Vistas
│   ├── mapper       VistaMapper
│   └── *Controller  Auth, Partido, Resultado, Perfil, Admin + GlobalExceptionHandler
├── service          AuthService, PartidoService, PostulacionService, ConfirmacionService,
│                    MatchmakingService, ResultadoService, ModeracionService,
│                    CalendarioService, BusquedaFavoritaService, NotificacionService
├── domain
│   ├── builder      PartidoBuilder
│   ├── enums        FormatoTenis, Superficie, EstadoPartido, ...
│   ├── event        DomainEvent, DomainEventBus, eventos concretos
│   ├── exception    DomainException y derivadas
│   ├── model        Partido, Usuario, Postulacion, ...
│   ├── notification Notifier, NotifierFactory
│   ├── port         PartidoRepository, UsuarioRepository, CalendarioPort, ...
│   ├── state        PartidoState + 6 estados
│   ├── strategy     MatchmakingStrategy + 3 estrategias + registry
│   └── vo           UbicacionGeo, HistorialDeportivo
├── infrastructure
│   ├── config       DomainConfig, DemoDataSeeder
│   ├── event        PublicadorEventosTransaccional
│   ├── notification adapters, clientes, factories, subscribers
│   ├── persistence  jpa (entities), repository (Spring Data), adapter, mapper
│   ├── scheduler    PartidoScheduler
│   └── security     JwtService, filtros, SecurityConfig
```

---

## 9. Trazabilidad con la consigna

| Requerimiento | Dónde está implementado |
|---|---|
| RF1 Registro / autenticación | `AuthService`, `AuthController`, `JwtService`, `SecurityConfig` |
| RF2 Búsqueda y alertas | `PartidoService.buscarCercanos`, `AlertaBusquedaSubscriber`, `BusquedaFavorita` |
| RF3 Creación (Builder) | `PartidoBuilder`, `PartidoService.crear` |
| RF4 Ciclo de vida (State) | `domain/state/*`, `PartidoScheduler` |
| RF5 Estrategias | `domain/strategy/*` |
| RF6 Parejas y suplentes | `Partido.reasignarParejas`, `promoverListaEspera`, `PostulacionService` |
| RF7 Notificaciones | `NotifierFactory`, adapters, `NotificacionSubscriber`, `NotificacionService` |
| RF8 Marcadores y feedback | `ResultadoPartido`, `SetMarcador`, `EvaluacionJugador` |
| RF9 Moderación y strikes | `ReporteConducta`, `ModeracionService`, `Usuario.aplicarStrike` |
| RF10 Calendario | `ICalendarAdapter`, `CalendarioService`, scheduler de recordatorios |
| RF11 Multisuperficie / multizona | `Superficie`, `UbicacionGeo`, `Partido.ubicacion` |
| RNF Rendimiento | `MatchmakingPerformanceTest` (500 candidatos < 2000 ms) |
| RNF Trazabilidad | `TRANSICIONES_ESTADO` + `AuditoriaSubscriber` |

---

## 10. Notas de diseño

- Las tres estrategias llevan `@Component("NTRP" | "CERCANIA" | "HISTORIAL")` **tal como
  indica el esqueleto de código de la consigna**; es la única anotación de Spring que aparece
  en el paquete `domain` y se eligió para que el endpoint `/emparejar?estrategia=` resuelva
  por nombre de bean sin un `switch` en el servicio.
- `Partido.cambiarEstado` publica `PartidoEstadoCambiadoEvent` para auditoría; el
  `NotificacionSubscriber` ignora las variantes `CUPO_COMPLETO` y `CANCELADO` porque esos
  flujos emiten eventos dedicados (`PartidoCupoCompletoEvent`, `PartidoCanceladoEvent`) y se
  evita notificar dos veces al mismo jugador.
- El despacho es asíncrono por defecto (`tenis.notificaciones.asincrono=true`) usando el
  `TaskExecutor` de Spring; en pruebas se fuerza síncrono para tener determinismo.

---

## 11. Guion sugerido para el video (≤ 5 min)

1. **Arranque** (20 s): `mvnw spring-boot:run`, mostrar el log con los datos demo y las
   notificaciones de consola `[PUSH]/[EMAIL]/[CHAT]`.
2. **Registro y login** (40 s): `POST /api/auth/register`, verificación con el token,
   `POST /api/auth/login` y `GET /api/usuarios/me` (Postman).
3. **Búsqueda** (30 s): `GET /api/partidos?formato=DOBLES&radioKm=20`.
4. **Builder** (30 s): `POST /api/partidos` y mostrar en la respuesta el estado
   `BUSCANDO_JUGADORES` y el creador ya inscripto.
5. **State** (60 s): postular con un segundo jugador ⇒ `CUPO_COMPLETO`; confirmar ambos ⇒
   `CONFIRMADO` (mostrar `historialEstados`). Intentar cancelar un partido `EN_JUEGO` para
   mostrar la transición denegada (`409`).
6. **Strategy** (45 s): `POST /api/partidos/{id}/emparejar?estrategia=HISTORIAL` y comparar con
   `NTRP` (distinto orden y puntajes).
7. **Adapter iCal** (20 s): descargar `calendario.ics` y abrirlo en Google Calendar.
8. **Observer + Abstract Factory** (40 s): mostrar las notificaciones generadas en el paso 5
   (`GET /api/usuarios/me/notificaciones`) y los logs de los tres canales.
9. **Resultados y moderación** (40 s): cargar `6-4 7-6`, evaluar fair play, reportar un no-show
   y resolverlo como `ADMIN` (strike + notificación).
10. **Cierre** (15 s): `GET /api/admin/estadisticas` y `mvnw test` con 45 pruebas en verde.

