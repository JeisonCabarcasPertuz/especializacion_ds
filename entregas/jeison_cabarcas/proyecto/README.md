# Core Banking Refactoring Lab

Proyecto integrador de la Especialización en Desarrollo de Software — Actividad 4
(proyecto integrador, 350 puntos). Demuestra la refactorización de un Core Bancario
académico aplicando SOLID, código limpio y cinco patrones de diseño GoF.

## Objetivo académico

Construir y comparar dos implementaciones completas y ejecutables del mismo Core
Bancario:

- **`legacy/`**: versión inicial construida específicamente para este proyecto
  integrador (no proviene de un proyecto histórico previo), con problemas reales de
  diseño, mantenibilidad y extensibilidad.
- **`refactor/`**: la misma funcionalidad, con esos problemas resueltos mediante
  Strategy, State, Chain of Responsibility, Template Method y Observer.

Ver `docs/README-REFACTORIZACION.md` para la explicación detallada de arquitectura y
patrones, `docs/refactor/diagnostico.md` para el diagnóstico con `archivo:línea`, y
`docs/refactor/matriz-antes-despues.md` para la trazabilidad completa.

## Estructura del proyecto

```text
entregas/jeison_cabarcas/proyecto/
├── legacy/                          # Core Bancario antes de refactorizar
├── refactor/                        # Core Bancario despues de refactorizar
├── docs/
│   ├── api/core-banking.http        # Flujo de demostracion reproducible
│   ├── refactor/
│   │   ├── diagnostico.md
│   │   └── matriz-antes-despues.md
│   ├── README-REFACTORIZACION.md
│   └── SUSTENTACION.md
├── README.md                        # este archivo
├── requerimientos.md
├── diseño.md
└── tareas.md
```

Ambas versiones (`legacy/` y `refactor/`) comparten la misma estructura Maven base:

```text
src/main/java/...
src/main/resources/
src/test/java/...
Dockerfile
compose.yaml
mvnw / mvnw.cmd
pom.xml
```

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (Web, Data JPA, Validation, Actuator)
- Maven Wrapper
- Lombok
- PostgreSQL 16 + Flyway
- H2 (solo para pruebas)
- JUnit 5, Mockito, AssertJ
- Docker / Docker Compose

## Prerrequisitos

- Java 21 (JDK) instalado, o usar únicamente `./mvnw`/`.\mvnw.cmd` que descarga Maven
  automáticamente.
- Docker Desktop (o equivalente) para ejecutar las bases de datos PostgreSQL y las
  aplicaciones en contenedores.
- No se requiere una instalación local de PostgreSQL: las pruebas usan H2 en memoria
  y la ejecución normal usa el PostgreSQL de Docker Compose.

## Ejecutar legacy con Docker

```bash
cd legacy
docker compose up --build
```

Puertos:

- Aplicación: `http://localhost:8081`
- PostgreSQL: `localhost:5433`

## Ejecutar refactor con Docker

```bash
cd refactor
docker compose up --build
```

Puertos:

- Aplicación: `http://localhost:8082`
- PostgreSQL: `localhost:5434`

## Ejecutar ambas versiones en paralelo

Los puertos, nombres de red y volúmenes de cada `compose.yaml` están aislados por el
nombre de carpeta (`legacy`/`refactor`), por lo que ambas pueden levantarse al mismo
tiempo sin conflicto:

```bash
(cd legacy && docker compose up --build -d)
(cd refactor && docker compose up --build -d)
```

## Healthchecks

Cada aplicación expone `/actuator/health`:

```bash
curl http://localhost:8081/actuator/health   # legacy
curl http://localhost:8082/actuator/health   # refactor
```

Respuesta esperada: `{"status":"UP"}`. Los contenedores `app` de cada `compose.yaml`
dependen de que PostgreSQL responda saludable (`pg_isready`) antes de arrancar.

## Ejecutar pruebas

Cada proyecto se prueba de forma independiente y no requiere Docker (usa H2 en
memoria en el perfil `test`):

```bash
cd legacy
./mvnw test      # Linux/macOS/Git Bash
.\mvnw.cmd test  # Windows PowerShell
```

```bash
cd refactor
./mvnw test
.\mvnw.cmd test
```

Estado verificado en este repositorio: `legacy` con 10 pruebas y `refactor` con 43
pruebas, ambos en `BUILD SUCCESS`.

## Ejecutar la demo HTTP reproducible

El archivo `docs/api/core-banking.http` contiene el flujo completo de demostración
(crear cliente, crear cuentas, depositar, transferir, consultar saldos e historial,
bloquear una cuenta e intentar un retiro inválido) usando las convenciones de la
extensión "REST Client" de VS Code.

Para comparar `legacy` contra `refactor`, cambia únicamente la variable `@baseUrl` al
inicio del archivo entre `{{legacyBaseUrl}}` y `{{refactorBaseUrl}}`; el resto del
flujo es idéntico.

## Detener y limpiar cada ambiente

```bash
cd legacy
docker compose down -v   # -v elimina tambien el volumen de PostgreSQL
```

```bash
cd refactor
docker compose down -v
```

Usa `-v` cuando quieras reiniciar la demostración desde una base de datos vacía.

## Endpoints principales

Base path: `/api/v1` (igual en `legacy` y `refactor`).

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/customers` | Crear cliente |
| GET | `/customers/{customerId}` | Consultar cliente |
| GET | `/customers` | Listar clientes |
| POST | `/customers/{customerId}/accounts` | Crear cuenta |
| GET | `/customers/{customerId}/accounts` | Listar cuentas de un cliente |
| GET | `/accounts/{accountId}` | Consultar cuenta |
| GET | `/accounts/{accountId}/balance` | Consultar saldo |
| PATCH | `/accounts/{accountId}/status` | Cambiar estado de la cuenta |
| POST | `/transactions/deposits` | Consignar |
| POST | `/transactions/withdrawals` | Retirar |
| POST | `/transactions/transfers` | Transferir |
| GET | `/accounts/{accountId}/transactions` | Historial de transacciones |

## Ruta recomendada para la demostración antes/después

1. Ejecutar `docs/api/core-banking.http` contra `refactor` (`@baseUrl = {{refactorBaseUrl}}`).
2. Repetir exactamente el mismo flujo contra `legacy` cambiando solo `@baseUrl`.
3. Confirmar que ambos producen los mismos saldos, códigos HTTP y errores.
4. Abrir `docs/refactor/diagnostico.md` y `docs/refactor/matriz-antes-despues.md` para
   explicar qué problema de `legacy` resuelve cada patrón en `refactor`.
5. Seguir el guion detallado en `docs/SUSTENTACION.md`.

## Documentación adicional

- `docs/README-REFACTORIZACION.md`: arquitectura, los cinco patrones, diagramas UML y
  de secuencia, comparación SOLID y trade-offs.
- `docs/refactor/diagnostico.md`: los cinco problemas de `legacy` con `archivo:línea`.
- `docs/refactor/matriz-antes-despues.md`: trazabilidad problema → patrón → clases → prueba.
- `docs/SUSTENTACION.md`: guía para la sustentación oral de 10-15 minutos.
- `requerimientos.md`, `diseño.md`, `tareas.md`: especificaciones que gobernaron la
  construcción de este proyecto, en orden de precedencia.
