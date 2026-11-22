# API REST — TenisSkillGames

Base URL: `http://localhost:8080`
Autenticación: `Authorization: Bearer <jwt>` (excepto endpoints públicos).
Formato: `application/json; charset=utf-8`.

## Errores

```json
{
  "status": 409,
  "error": "Conflict",
  "mensaje": "El partido está en juego: no admite nuevas postulaciones",
  "path": "/api/partidos/{id}/postulaciones",
  "timestamp": "2026-11-22T10:31:00",
  "detalles": null
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación de entrada (Bean Validation) / JSON inválido |
| 401 | Credenciales inválidas o token ausente/expirado |
| 403 | Sin permisos o cuenta suspendida por strikes |
| 404 | Recurso inexistente |
| 409 | Transición de estado inválida, conflicto de negocio (email duplicado, horario superpuesto) |
| 422 | Regla de dominio (NTRP fuera de banda, fecha pasada, marcador inválido) |
| 429 | Rate limit en `/api/auth/**` |

---

## Autenticación

### POST `/api/auth/register`
```json
{
  "nombre": "Nicolás Méndez",
  "email": "nico@test.com",
  "password": "Password123",
  "nivelNTRP": 3.5,
  "zona": "Núñez",
  "superficieFavorita": "POLVO_DE_LADRILLO",
  "manoHabil": "DIESTRO",
  "golpePreferido": "DRIVE",
  "latitud": -34.5453,
  "longitud": -58.4498,
  "telefono": "+5491155550000",
  "pushToken": "fcm-token-demo",
  "franjasHorarias": ["LUN_18-22", "SAB_09-13"]
}
```
`201 Created`
```json
{
  "usuario": { "id": "…", "estadoVerificacion": "PENDIENTE", "…": "…" },
  "tokenVerificacion": "eyJhbGciOiJIUzI1NiJ9…",
  "mensaje": "Cuenta creada en estado PENDIENTE. Se despachó el correo de verificación."
}
```

### POST `/api/auth/verificar-email`
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9…" }
```

### POST `/api/auth/login`
```json
{ "email": "nico@test.com", "password": "Password123" }
```
`200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9…",
  "tipo": "Bearer",
  "expiraEnSegundos": 7200,
  "usuario": { "id": "…", "roles": ["JUGADOR"], "…": "…" }
}
```

---

## Partidos

### GET `/api/partidos`
Parámetros opcionales: `formato`, `superficie`, `ntrpMin`, `ntrpMax`, `radioKm`, `fecha`
(`YYYY-MM-DD`), `horaDesde`/`horaHasta` (`HH:mm`), `zona`, `estado`, `latitud`, `longitud`.

`GET /api/partidos?formato=DOBLES&superficie=POLVO_DE_LADRILLO&radioKm=10`

Si se envía `radioKm` y el usuario está autenticado, el radio se calcula desde su ubicación.

```json
[
  {
    "id": "9f1c…",
    "clubSede": "Club Náutico Núñez",
    "formato": "SINGLES",
    "superficie": "POLVO_DE_LADRILLO",
    "estado": "CUPO_COMPLETO",
    "fechaHora": "2026-11-24T19:00:00",
    "ntrpMin": 3.0,
    "ntrpMax": 4.5,
    "cupoMaximo": 2,
    "plazasLibres": 0,
    "participantes": [
      { "id": "…", "nombre": "Nicolás Méndez", "nivelNTRP": 3.5, "lado": "A", "confirmado": false }
    ],
    "listaEspera": [],
    "parejas": [],
    "historialEstados": [
      { "estadoAnterior": "BUSCANDO_JUGADORES", "estadoNuevo": "CUPO_COMPLETO", "fecha": "…", "motivo": null }
    ]
  }
]
```

### POST `/api/partidos`
```json
{
  "formato": "DOBLES",
  "superficie": "CESPED_SINTETICO",
  "clubSede": "Club Atlético San Isidro",
  "direccion": "Av. Centenario 1200",
  "latitud": -34.4707,
  "longitud": -58.5123,
  "ntrpMin": 3.0,
  "ntrpMax": 4.0,
  "fechaHora": "2026-12-05T20:00:00",
  "duracionMinutos": 120,
  "modalidad": "AMISTOSO_RECREATIVO",
  "radioMaxKm": 20,
  "estrategia": "HISTORIAL",
  "observaciones": "Se juega con pelotas nuevas"
}
```

### POST `/api/partidos/{id}/postulaciones`
```json
{ "ladoDeseado": "A" }
```
Respuestas: `200` con el partido actualizado · `422` si el NTRP o la distancia no son
compatibles (CA1) · `409` si el horario se superpone con otro partido.

### POST `/api/partidos/{id}/confirmaciones`
```json
{ "confirmado": true, "motivo": null }
```
Al confirmar el último participante el estado pasa a `CONFIRMADO` (CA3).
Con `"confirmado": false` se libera la plaza y se promueve la lista de espera.

### POST `/api/partidos/{id}/emparejar?estrategia=NTRP&autoInscribir=true`
```json
{
  "estrategia": "NTRP",
  "candidatos": [
    { "id": "…", "nombre": "Ana Gómez", "nivelNTRP": 4.0, "distanciaKm": 2.3,
      "fairPlay": 4.8, "tasaAsistencia": 0.97, "puntaje": 0.89, "estrategia": "NTRP" }
  ],
  "parejas": [
    { "lado": "A", "promedioNTRP": 3.5, "jugadores": [ … ] }
  ],
  "jugadoresInscriptos": 4,
  "plazasLibres": 0
}
```

### GET `/api/partidos/{id}/calendario.ics`
`200` con `Content-Type: text/calendar` y `Content-Disposition: attachment; filename="partido-<id>.ics"`.

### POST `/api/partidos/{id}/cancelar`
```json
{ "motivo": "Lluvia sobre polvo de ladrillo" }
```

### POST `/api/partidos/{id}/resultados`
```json
{
  "ganadorLado": "A",
  "sets": [ { "local": 6, "visitante": 4 }, { "local": 7, "visitante": 6 } ],
  "duracionRealMinutos": 105,
  "observaciones": "Cancha 3"
}
```

### POST `/api/partidos/{id}/evaluaciones`
```json
{ "evaluadoId": "…", "fairPlay": 5, "puntualidad": 4, "comentario": "Muy buen partido" }
```

### POST `/api/partidos/{id}/reportes`
```json
{ "reportadoId": "…", "motivo": "NO_SHOW", "descripcion": "No se presentó ni avisó" }
```

---

## Administración (requiere rol ADMIN)

### GET `/api/admin/reportes`
### POST `/api/admin/reportes/{id}/resolver`
```json
{ "confirmar": true, "resolucion": "No-show comprobado por testigos" }
```
Al llegar a 3 strikes el usuario queda suspendido 7 días y se notifica por todos sus canales.

### GET `/api/admin/estadisticas`
```json
{
  "usuarios": 12,
  "partidos": 7,
  "notificacionesEnviadas": 48,
  "notificacionesFallidas": 0,
  "partidosPorEstado": {
    "BUSCANDO_JUGADORES": 2, "CUPO_COMPLETO": 1, "CONFIRMADO": 3,
    "EN_JUEGO": 0, "FINALIZADO": 1, "CANCELADO": 0
  }
}
```

---

## Ejemplo de flujo con curl

```bash
BASE=http://localhost:8080

# 1) login con el usuario de demostración
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"nico@tenisskillgames.ar","password":"Password123"}' | jq -r .token)

# 2) crear partido
PARTIDO=$(curl -s -X POST $BASE/api/partidos \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"formato":"SINGLES","superficie":"POLVO_DE_LADRILLO","clubSede":"Club Núñez",
       "latitud":-34.5453,"longitud":-58.4498,"ntrpMin":3.0,"ntrpMax":4.5,
       "fechaHora":"2026-12-10T19:00:00","duracionMinutos":90,
       "modalidad":"AMISTOSO_RECREATIVO","radioMaxKm":15,"estrategia":"NTRP"}' | jq -r .id)

# 3) emparejar
curl -s -X POST "$BASE/api/partidos/$PARTIDO/emparejar?estrategia=HISTORIAL" \
  -H "Authorization: Bearer $TOKEN" | jq

# 4) descargar la invitación iCal
curl -s "$BASE/api/partidos/$PARTIDO/calendario.ics" -o partido.ics
```
