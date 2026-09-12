# Guía de sustentación oral — Core Banking Refactoring Lab

Duración objetivo: 10-15 minutos. Esta guía es material de estudio; la explicación
oral no debe depender de leerla en voz alta.

## 1. Guion de 10-15 minutos

```text
1 min  -> problema y objetivo del proyecto
2 min  -> recorrido de legacy y sus problemas
6 min  -> los cinco problemas y los cinco patrones (~1.2 min cada uno)
2 min  -> ejecución de la API (demo)
2 min  -> pruebas y conclusión
```

## 2. Qué mostrar en legacy

1. Abrir `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java`.
2. Señalar `calculateFee()`/`calculateLimit()` (líneas 205-242): el `switch` anidado
   por tipo de cuenta.
3. Señalar los `if (account.getStatus() == ...)` repetidos en `deposit()`,
   `withdraw()` y `transfer()`.
4. Señalar el bloque de validaciones secuenciales dentro de `transfer()`
   (líneas 136-172).
5. Señalar la estructura repetida entre los tres métodos públicos.
6. Señalar las llamadas directas a `createAuditRecord()`/`sendSimulatedNotification()`.
7. Aclarar: `legacy` compila y funciona (10 pruebas en verde); el problema es de
   diseño, no de funcionalidad.

## 3. LEG-01 → Strategy

- **Problema:** `switch`/`if` anidado por `AccountType` para calcular comisión y
  límite, dentro del servicio de transacciones.
- **Patrón:** Strategy — `AccountTransactionPolicy`, `SavingsTransactionPolicy`,
  `CheckingTransactionPolicy`, `TransactionPolicyResolver`.
- **Por qué:** el comportamiento cambia por tipo de cuenta; se quiere poder agregar
  una política nueva sin modificar el algoritmo central.
- **Prueba:** `SavingsTransactionPolicyTest`, `CheckingTransactionPolicyTest`.

## 4. LEG-02 → State

- **Problema:** condicionales de `AccountStatus` repetidos en cuatro lugares
  distintos (tres en `TransactionServiceImpl`, uno en `AccountServiceImpl`).
- **Patrón:** State — `AccountState`, `ActiveAccountState`, `BlockedAccountState`,
  `ClosedAccountState`, `AccountStateContext`, `AccountStateResolver`.
- **Por qué:** el comportamiento permitido (débito/crédito/transición) es claramente
  distinto por estado y el enum persistido (`AccountStatus`) no debe cargar esa
  lógica.
- **Prueba:** `ActiveAccountStateTest`, `BlockedAccountStateTest`, `ClosedAccountStateTest`.

## 5. LEG-03 → Chain of Responsibility

- **Problema:** siete validaciones secuenciales concentradas en `transfer()`.
- **Patrón:** Chain of Responsibility — `TransactionValidator`,
  `AbstractTransactionValidator`, seis validadores concretos,
  `TransactionValidationChainProvider`.
- **Por qué:** las reglas cambian de forma independiente y no todas aplican a cada
  operación; la cadena permite componer orden y reglas sin agrandar el servicio.
- **Prueba:** `TransactionValidationChainTest`.

## 6. LEG-04 → Template Method

- **Problema:** `deposit`, `withdraw` y `transfer` repetían la misma estructura de
  siete pasos.
- **Patrón:** Template Method — `AbstractTransactionProcessor.process()` (`final`),
  `DepositTransactionProcessor`, `WithdrawalTransactionProcessor`,
  `TransferTransactionProcessor`.
- **Por qué:** existe un algoritmo común estable con pocos pasos variables; una
  jerarquía de un nivel hace visible el flujo compartido.
- **Prueba:** `DepositTransactionProcessorTest`, `WithdrawalTransactionProcessorTest`,
  `TransferTransactionProcessorTest`.

## 7. LEG-05 → Observer

- **Problema:** auditoría y notificación simulada invocadas directamente desde el
  flujo principal, en tres puntos del código.
- **Patrón:** Observer — `TransactionObserver`, `AuditTransactionObserver`,
  `NotificationTransactionObserver`, `TransactionEventPublisher`,
  `TransactionCompletedEvent`.
- **Por qué:** auditar y notificar no forman parte del cálculo del movimiento;
  Observer permite agregar reacciones sin modificar el procesador.
- **Prueba:** `TransactionEventPublisherTest`, `AuditTransactionObserverTest`.

## 8. Demo funcional

Usar `docs/api/core-banking.http` contra `refactor` (`@baseUrl = {{refactorBaseUrl}}`):

1. Crear cliente.
2. Crear cuenta SAVINGS y cuenta CHECKING.
3. Depositar 200.000 COP en SAVINGS.
4. Transferir 75.000 COP de SAVINGS a CHECKING (comisión SAVINGS = 1.000).
5. Mostrar saldos: origen 124.000, destino 75.000.
6. Mostrar historial de transacciones de la cuenta origen.
7. Bloquear la cuenta SAVINGS (`PATCH .../status`).
8. Intentar un retiro sobre la cuenta bloqueada y mostrar `422 ACCOUNT_BLOCKED`.
9. Repetir el mismo flujo contra `legacy` cambiando solo `@baseUrl` para mostrar
   paridad funcional.

## 9. Pruebas

- `legacy`: 10 pruebas, `BUILD SUCCESS` — demuestra que el "antes" funciona.
- `refactor`: 43 pruebas, `BUILD SUCCESS` — cubre los cinco patrones, el servicio de
  coordinación y un flujo de integración completo (`TransferFlowIntegrationTest`)
  sobre H2, sin depender de Docker.

## 10-11. Preguntas probables y respuestas razonadas

**¿Por qué Strategy y no un `switch`?**
Porque el comportamiento cambia por tipo de cuenta y se quiere agregar una política
nueva sin modificar el algoritmo central. Cumple OCP: `TransactionPolicyResolver`
selecciona la política sin condicionales.

**¿Por qué State y no un enum con `if`?**
El enum `AccountStatus` identifica el estado persistido; las clases `AccountState`
encapsulan el comportamiento permitido por ese estado. Esto evita distribuir la
misma decisión en varios servicios.

**¿Por qué Chain of Responsibility y no un solo método `validate()`?**
Las reglas cambian independientemente y no todas aplican a cada operación (depósito,
retiro y transferencia tienen cadenas distintas). La cadena permite agregar o probar
una regla sin tocar las demás.

**¿Por qué Template Method y no tres métodos separados?**
Existe un algoritmo común estable (cargar, resolver política, calcular comisión,
validar, aplicar movimiento, persistir, publicar evento, construir respuesta) con
pocos pasos variables. Una jerarquía de un nivel hace visible ese flujo compartido.
Si los algoritmos divergieran mucho, composición sería preferible.

**¿Por qué Observer y no llamar al auditor directamente?**
Auditar y notificar no forman parte del cálculo del movimiento de dinero. Observer
permite agregar reacciones posteriores sin modificar el procesador principal.

**¿Qué principio SOLID mejora cada patrón?**
Strategy y State mejoran principalmente OCP; Chain of Responsibility mejora SRP y
OCP; Template Method mejora SRP y OCP; Observer mejora SRP y DIP. Ver la tabla
comparativa completa en `docs/README-REFACTORIZACION.md` sección 13.

**¿Qué desventaja introdujo cada patrón?**
Más clases (~30 clases nuevas bajo `pattern/`) a cambio de responsabilidades
aisladas. Chain of Responsibility depende de que el orden de ensamblaje sea correcto.
Template Method usa herencia, que es más rígida que composición. Observer hace que el
flujo posterior a una transacción deje de ser evidente mirando solo el procesador.

**¿Por qué no microservicios?**
El alcance académico es un único Core Bancario pequeño; introducir microservicios
agregaría complejidad de despliegue, comunicación de red y consistencia distribuida
sin aportar valor a la demostración de SOLID y patrones GoF, que es el objetivo de
esta actividad.

**¿Por qué H2 en pruebas y PostgreSQL en ejecución?**
H2 en memoria permite que `./mvnw test` corra rápido y sin Docker. PostgreSQL es la
base de datos real de ejecución, más cercana a un entorno productivo, y donde se
verifican las restricciones (`CHECK`, `UNIQUE`) definidas en la migración Flyway.

**¿Qué pasaría con concurrencia?**
`BankAccount` usa `@Version` (control optimista). Si dos solicitudes modifican la
misma cuenta concurrentemente, la segunda en persistir recibe
`409 CONFLICT / ACCOUNT_CONCURRENT_UPDATE` y debe reintentarse. No se usan locks
pesimistas ni bloqueo distribuido, que quedan fuera del alcance académico.

**¿Qué cambiarías para producción?**
Ver sección 12 (mejoras futuras).

## 12. Qué se mejoraría con más tiempo

- Reintento automático ante `ACCOUNT_CONCURRENT_UPDATE` en vez de propagar el 409 al
  cliente.
- Externalizar las tarifas de las políticas de transacción a configuración.
- Paginación en listados de clientes e historial de transacciones.
- Un mecanismo de outbox/eventos asíncronos si `TransactionEventPublisher` necesitara
  reacciones que crucen procesos o servicios.
- Autenticación/autorización, explícitamente fuera de alcance en `requerimientos.md`.
