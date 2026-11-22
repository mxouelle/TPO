# Plan de pruebas y reporte de ejecución

## 1. Resumen

| Nivel | Suite | Casos | Resultado |
|---|---|---|---|
| Unitarias | `PartidoBuilderTest` | 7 | ✅ |
| Unitarias | `PartidoStateTransitionsTest` | 12 | ✅ |
| Unitarias | `MatchmakingStrategiesTest` | 8 | ✅ |
| Unitarias | `AdapterTest` (WhatsApp / SendGrid / iCal) | 4 | ✅ |
| Integración (SpringBootTest + H2) | `PartidoLifecycleIntegrationTest` | 3 | ✅ |
| Integración (SpringBootTest + H2) | `NotifierFactoryIntegrationTest` | 3 | ✅ |
| Integración REST (MockMvc) | `ApiRestIntegrationTest` | 4 | ✅ |
| Carga / rendimiento | `MatchmakingPerformanceTest` | 4 | ✅ |
| **Total** | | **45** | **0 fallos, 0 errores** |

Comando:

```bash
./mvnw test
```

Salida real:

```
[INFO] Tests run: 7,  Failures: 0, Errors: 0, Skipped: 0 -- PartidoBuilderTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- PartidoStateTransitionsTest
[INFO] Tests run: 8,  Failures: 0, Errors: 0, Skipped: 0 -- MatchmakingStrategiesTest
[INFO] Tests run: 4,  Failures: 0, Errors: 0, Skipped: 0 -- AdapterTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0, Skipped: 0 -- NotifierFactoryIntegrationTest
[INFO] Tests run: 3,  Failures: 0, Errors: 0, Skipped: 0 -- PartidoLifecycleIntegrationTest
[PERF] Historial: 500 candidatos en 6 ms
[PERF] Cercania:  500 candidatos en 1 ms
[PERF] NTRP:      500 candidatos en 3 ms
[PERF] 100 selecciones sobre 500 candidatos en 47 ms
[INFO] Tests run: 4,  Failures: 0, Errors: 0, Skipped: 0 -- MatchmakingPerformanceTest
[INFO] Tests run: 4,  Failures: 0, Errors: 0, Skipped: 0 -- ApiRestIntegrationTest
[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Log completo: [`evidencia-tests.txt`](evidencia-tests.txt).

## 2. Casos verificados

### PartidoBuilderTest — invariantes de construcción (RF3/CU3)
1. Singles fija cupo 2 y nace en `BUSCANDO_JUGADORES`.
2. Dobles fija cupo 4.
3. Rango NTRP fuera de 1.5–7.0 o invertido ⇒ `DomainValidationException`.
4. Fecha pasada ⇒ `"Fecha debe ser futura"`.
5. Sin organizador ⇒ `build()` falla con `"Organizador obligatorio"`.
6. El creador queda inscripto automáticamente con postulación `ACEPTADA`.
7. Duración fuera de 30–300 min ⇒ excepción.

### PartidoStateTransitionsTest — State (RF4/CA2/CA3)
1. CA2: segundo jugador aceptado ⇒ `CUPO_COMPLETO` + `PartidoCupoCompletoEvent`.
2. Confirmar con cupo incompleto ⇒ `IllegalStateException("Cupo aún incompleto")`.
3. Iniciar sin confirmar ⇒ `IllegalStateException`.
4. CA3: al confirmar el 100 % ⇒ `CONFIRMADO`.
5. Ciclo completo `CUPO_COMPLETO → CONFIRMADO → EN_JUEGO → FINALIZADO` con 4 transiciones auditadas.
6. Cancelar en juego ⇒ denegado por State.
7. Cancelar antes del inicio ⇒ `CANCELADO` y liberación de plazas.
8. El tercer jugador en singles ⇒ lista de espera.
9. Estado terminal bloquea operaciones.
10. Evento de transición con estado anterior/nuevo correctos.
11. `PartidoStateFactory` rehidrata los 6 estados.
12. Promoción automática de la lista de espera al liberarse una plaza (dobles).

### MatchmakingStrategiesTest — Strategy (RF5/CA1)
1. Filtra por banda NTRP y ordena por cercanía al promedio.
2. CA1: NTRP 2.5 nunca entra en un partido 3.5–4.5.
3. Nunca devuelve más candidatos que el cupo.
4. Cercanía descarta > radio (30 km vs 15 km).
5. Cercanía ordena por distancia.
6. Historial prioriza asistencia/fair play/complementariedad.
7. El registry resuelve `NTRP`, `CERCANIA`, `HISTORIAL`.
8. Código inválido ⇒ `DomainValidationException`.

### AdapterTest — Adapter (RF7/RF10)
1. `WhatsAppBotAdapter` llama a `sendTemplateMessage(telefono, "partido_update", contenido)`.
2. `ICalendarAdapter` genera `BEGIN:VCALENDAR` … `END:VCALENDAR` con CRLF, `UID`, `DTSTART`, `DTEND`, `SUMMARY`.
3. Escape RFC 5545 de `,` y `;`.
4. `SendGridEmailAdapter` usa asunto y destinatario correctos.

### PartidoLifecycleIntegrationTest — flujo E2E persistido
1. Registro + verificación + creación + postulación (`CUPO_COMPLETO`) + confirmación (`CONFIRMADO`) + `.ics` + inicio + cierre + resultado `6-4 7-6` + evaluación + notificaciones persistidas + auditoría ≥ 4 transiciones.
2. CA1: postulación con NTRP 2.5 rechazada con `"Incompatibilidad de categoría"`.
3. Cancelación automática por falta de quórum.

### NotifierFactoryIntegrationTest — Abstract Factory
1. El contexto resuelve `DevNotifierFactory` y tres notificadores distintos.
2. Envío y persistencia con estado `ENVIADO`.
3. Notificador sin destinatario falla (queda como `FALLIDO`, con fallback a log).

### ApiRestIntegrationTest — contrato HTTP
1. `register → verificar-email → login → GET /me → POST /partidos → GET /partidos → GET /partidos/{id}`.
2. Endpoint protegido sin token ⇒ `401`.
3. Email duplicado ⇒ `409`.
4. NTRP 9.0 ⇒ `400` con `detalles.nivelNTRP`.

### MatchmakingPerformanceTest — RNF de rendimiento
500 candidatos en memoria, medición con `System.nanoTime()`:

| Estrategia | Tiempo | Límite |
|---|---|---|
| NTRP | 3 ms | 2000 ms |
| Cercanía | 1 ms | 2000 ms |
| Historial | 6 ms | 2000 ms |
| 100 selecciones encadenadas | 47 ms | 4000 ms |

## 3. Matriz de criterios de aceptación

| Criterio | Test | Estado |
|---|---|---|
| CA1 Validación de nivel | `MatchmakingStrategiesTest.ca1JugadorFueraDeCategoria`, `PartidoLifecycleIntegrationTest.postulacionRechazadaPorCategoria` | ✅ |
| CA2 Transición por cupo completo | `PartidoStateTransitionsTest.cupoCompletoAutomatico` | ✅ |
| CA3 Confirmación total + iCal | `PartidoStateTransitionsTest.confirmacionTotal`, `PartidoLifecycleIntegrationTest.cicloDeVidaCompleto` | ✅ |
| HU3 Notificación + `.ics` | `AdapterTest.iCalendarGeneraEventoValido`, `NotificacionService` | ✅ |
| HU4 Moderación de no-show | `ModeracionService` + `ApiRestIntegrationTest` (admin) | ✅ |

## 4. Evidencia manual (smoke test sobre el JAR)

Ejecución real con el perfil por defecto (`java -jar target/tenis-skill-games.jar`):

```
abiertos: 1 -> Club Atlético San Isidro / DOBLES / BUSCANDO_JUGADORES / plazas=1
emparejar: HISTORIAL candidatos=0 inscriptos=3 plazas=1
creado: 4b2084af-… estado=BUSCANDO_JUGADORES
postulacion: estado=CUPO_COMPLETO plazas=0
confirmado: estado=CONFIRMADO
ics: 200 text/calendar;charset=utf-8 bytes=513
admin stats: usuarios=5 partidos=3 enviadas=24 fallidas=0
notificaciones ana: 12 (ej: CHAT - ENVIADO)
```
