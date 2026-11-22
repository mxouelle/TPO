# Arquitectura — TenisSkillGames

## 1. Capas

| Capa | Paquete | Responsabilidad | Depende de |
|---|---|---|---|
| Presentación | `controller` | Contrato HTTP, validación de entrada, mapeo a DTO, manejo de errores | `service`, `infrastructure.security` |
| Aplicación | `service` | Orquestación de casos de uso, transacciones, armado de vistas | `domain` |
| Dominio | `domain` | Reglas e invariantes, patrones de diseño, eventos, puertos | Nada (Java puro) |
| Infraestructura | `infrastructure` | JPA/Hibernate, seguridad JWT, notificadores, iCal, scheduler, bus | `domain` |

El flujo de una petición es siempre:

```
Controller → Service (@Transactional) → Agregado de dominio → Puerto → Adapter JPA → H2/PostgreSQL
                                     ↘ Eventos de dominio → Bus → Subscribers → Notificadores
```

## 2. Desacoplamiento del dominio

El paquete `domain` no importa `org.springframework.*` salvo las tres estrategias
(`@Component("NTRP"/"CERCANIA"/"HISTORIAL")`), decisión tomada para respetar literalmente el
esqueleto de código de la consigna y permitir que `/api/partidos/{id}/emparejar?estrategia=`
resuelva por nombre de bean.

La persistencia se implementa con el patrón *ports & adapters*:

- `domain/port/*` define contratos (`PartidoRepository`, `UsuarioRepository`,
  `NotificacionRepository`, `ResultadoRepository`, `BusquedaFavoritaRepository`, `CalendarioPort`).
- `infrastructure/persistence/jpa/*` contiene las entidades JPA (proyecciones de tabla).
- `infrastructure/persistence/mapper/*` traduce entidad ⇄ agregado.
- `infrastructure/persistence/adapter/*` implementa los puertos e hidrata el agregado completo.

Consecuencia: `Partido` puede testearse sin base de datos y la base puede cambiarse sin tocar
el dominio.

## 3. Agregado Partido

`Partido` es la raíz del agregado y el *context* del patrón State. Contiene:

- datos de configuración (formato, superficie, sede, banda NTRP, fecha, duración, modalidad);
- `estado` (enum persistido) + `estadoActual` (`PartidoState` rehidratado por `PartidoStateFactory`);
- `participantes`, `listaEspera`, `postulaciones`, `confirmaciones`, `parejas`;
- `historialEstados` (auditoría de transiciones);
- `resultado` (0..1);
- un `DomainEventPublisher` transitorio inyectado por el adaptador.

Métodos de negocio: `postular`, `confirmar`, `iniciar`, `finalizar`, `cancelar`,
`liberarPlaza`, `promoverListaEspera`, `reasignarParejas`, `validarEquilibrioParejas`,
`cargarResultado`, `estaCupoCompleto`, `todosConfirmaron`, `getNtrpPromedio`,
`distanciaAlClubKm`, `dentroDelRadio`.

## 4. Máquina de estados

| Estado | postular | confirmar | iniciar | finalizar | cancelar |
|---|---|---|---|---|---|
| `BUSCANDO_JUGADORES` | agrega participante; si completa cupo → `CUPO_COMPLETO` | ✗ *"Cupo aún incompleto"* | ✗ | ✗ | → `CANCELADO` |
| `CUPO_COMPLETO` | → lista de espera | registra; si todos → `CONFIRMADO` | ✗ | ✗ | → `CANCELADO` |
| `CONFIRMADO` | ✗ | idempotente si ya confirmó | → `EN_JUEGO` | ✗ | → `CANCELADO` |
| `EN_JUEGO` | ✗ | ✗ | ✗ | → `FINALIZADO` | ✗ |
| `FINALIZADO` | ✗ | ✗ | ✗ | ✗ | ✗ |
| `CANCELADO` | ✗ | ✗ | ✗ | ✗ | ✗ |

Cada transición agrega una fila en `TRANSICIONES_ESTADO` y publica
`PartidoEstadoCambiadoEvent`.

## 5. Eventos y notificaciones

```
Agregado ──publish──► PublicadorEventosTransaccional ──afterCommit──► DomainEventBus
                                                                          │
                 ┌────────────────────────────┬───────────────────────────┤
                 ▼                            ▼                           ▼
        NotificacionSubscriber        AlertaBusquedaSubscriber    AuditoriaSubscriber
        (cupo, confirmado,            (partido creado vs.          (log de transiciones)
         iniciado, finalizado,         búsquedas favoritas)
         cancelado, strike)
                 │
                 ▼
        NotificacionService ──► NotifierFactory ──► Push / Email / Chat (Adapter)
                 │                    (Dev: consola · Prod: Firebase/SendGrid/WhatsApp)
                 ▼
        NOTIFICACIONES (estado ENVIADO / FALLIDO, intentos, último error)
```

- Reintento **exponencial** (200 ms, 400 ms, 800 ms por defecto, 3 intentos).
- Fallback: la notificación queda `FALLIDO` con el error, sin romper la transacción de negocio.
- Despacho asíncrono opcional (`tenis.notificaciones.asincrono`).
- La invitación `.ics` viaja como adjunto del email cuando el partido pasa a `CONFIRMADO`.

## 6. Scheduler

`PartidoScheduler` corre cada 60 s (`tenis.scheduler.fixed-delay-ms`):

1. `CONFIRMADO` + `fechaHora <= now` → `iniciar()`.
2. `EN_JUEGO` + `fechaHora + duración < now` → `finalizar()`.
3. `BUSCANDO_JUGADORES`/`CUPO_COMPLETO` + hora vencida → cancelación por falta de quórum.
4. Recordatorios a 24 h y 2 h (flags `recordatorio24Enviado` / `recordatorio2Enviado`).

## 7. Persistencia (DER)

Tablas: `USUARIOS`, `PARTIDOS`, `POSTULACIONES` (incluye `confirmado` y `fecha_confirmacion`),
`PAREJAS` + `PAREJA_JUGADORES`, `RESULTADOS_PARTIDO`, `NOTIFICACIONES`, `EVALUACIONES`,
`REPORTES_CONDUCTA`, `BUSQUEDAS_FAVORITAS`, `TRANSICIONES_ESTADO` y tablas de colecciones
(`usuario_roles`, `usuario_canales`, `usuario_franjas`).

Claves primarias UUID, email único, índice compuesto
`(estado_actual, fecha_hora, superficie)` en `PARTIDOS` y borrado lógico de usuarios (`activo`).

## 8. Decisiones y compromisos

| Decisión | Motivo |
|---|---|
| Eventos publicados en `afterCompletion` | Evita que un suscriptor lea datos no confirmados. |
| Agregado hidratado completo en cada carga | Simplicidad y consistencia; el volumen esperado lo tolera. |
| `@Component` en las estrategias | Requisito del esqueleto de la consigna. |
| Simuladores en `dev` | Permite correr y demostrar sin credenciales de Firebase/SendGrid/Meta. |
| Rate limiting en memoria | Suficiente para una instancia; en cluster se reemplaza por Redis. |
| Verificación de email con token JWT de un propósito | Evita tabla extra y expira a las 24 h. |
