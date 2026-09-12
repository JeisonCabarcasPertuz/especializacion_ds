# Diagnóstico del código "antes" (legacy)

> La versión `legacy` es una **versión inicial del Core Bancario construida para
> demostrar el proceso de diagnóstico y refactorización del proyecto integrador**. No
> proviene de un proyecto histórico previo.
>
> `legacy` queda congelado a partir de este documento (tag `v1-legacy`). Las líneas
> citadas corresponden al código real congelado; cualquier corrección posterior a un
> bug crítico de ejecución deberá documentarse y estas referencias deberán
> actualizarse.

## LEG-01 — Política de comisión y límite por tipo de cuenta mediante `switch`

**Ubicación:**

```text
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:205-242
```

**Fragmento/descripción concreta:**

`calculateFee(AccountType, TransactionType, BigDecimal)` (líneas 205-232) usa un
`switch` anidado por `AccountType` y, dentro de cada rama, otro `switch` por
`TransactionType`. `calculateLimit(AccountType)` (líneas 234-242) repite la misma
familia de condicionales con una cadena de `if` por tipo de cuenta.

**Code smell:** Switch Statements / Shotgun Surgery potencial.

**Principio SOLID afectado:** OCP (agregar un tipo de cuenta nuevo, p. ej.
`MONEY_MARKET`, obliga a modificar ambos métodos) y SRP (el servicio de transacciones
concentra además la política monetaria del banco).

**Consecuencia:** cada nuevo tipo de cuenta o cada cambio de tarifa exige editar
`TransactionServiceImpl` directamente, con riesgo de olvidar una de las dos ramas
(`calculateFee` o `calculateLimit`) y dejar reglas inconsistentes.

**Patrón elegido:** Strategy.

**Razón:** cada tipo de cuenta se convierte en una implementación de
`AccountTransactionPolicy` seleccionable por `TransactionPolicyResolver`, eliminando el
`switch` central y permitiendo agregar políticas nuevas sin tocar el procesador de
transacciones.

---

## LEG-02 — Comportamiento por estado de cuenta mediante condicionales repetidos

**Ubicación:**

```text
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:56-59
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:91-98
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:147-158
legacy/src/main/java/com/unimagdalena/corebanking/service/AccountServiceImpl.java:79-89
```

**Fragmento/descripción concreta:**

`deposit()`, `withdraw()` y `transfer()` repiten, cada uno por su cuenta, condicionales
del tipo `if (account.getStatus() == AccountStatus.CLOSED) ...` /
`if (account.getStatus() == AccountStatus.BLOCKED) ...` para decidir si una cuenta
puede debitarse o acreditarse. `AccountServiceImpl.changeStatus()` (líneas 79-89)
repite una tercera variante de la misma decisión para validar transiciones de estado.

**Code smell:** Duplicated Conditional / Divergent Change.

**Principio SOLID afectado:** OCP y SRP — la regla "qué puede hacer una cuenta en
cada estado" vive dispersa en cuatro lugares distintos en lugar de en un solo punto de
verdad.

**Consecuencia:** agregar un estado nuevo (o cambiar el comportamiento de uno
existente) exige revisar y modificar cuatro métodos distintos, con alto riesgo de
inconsistencia entre ellos.

**Patrón elegido:** State.

**Razón:** cada estado (`ActiveAccountState`, `BlockedAccountState`,
`ClosedAccountState`) encapsula sus propias reglas de débito, crédito y transición
detrás de `AccountState`/`AccountStateContext`, de modo que los validadores y el
servicio de cuentas delegan la decisión en un único lugar por estado.

---

## LEG-03 — Bloque de validaciones secuenciales dentro de un único método

**Ubicación:**

```text
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:136-172
```

**Fragmento/descripción concreta:**

`transfer()` concentra en un solo método siete validaciones secuenciales antes de
mover dinero: monto positivo (136-138), cuentas distintas (139-142), estado de origen
cerrado (147-149), estado de origen bloqueado (151-153), estado de destino cerrado
(155-158), límite de operación (160-164) y saldo suficiente (169-172). `deposit()` y
`withdraw()` repiten la misma estructura con su propio subconjunto de validaciones.

**Code smell:** Long Method / validación monolítica.

**Principio SOLID afectado:** SRP (el método mezcla orquestación con reglas de
validación) y OCP (agregar una validación nueva implica editar el método existente y
puede afectar el orden de las demás).

**Consecuencia:** el método crece con cada regla nueva, es difícil de leer de punta a
punta y no es posible probar una validación de forma aislada del resto del flujo.

**Patrón elegido:** Chain of Responsibility.

**Razón:** cada regla se convierte en un `TransactionValidator` independiente
(`PositiveAmountValidator`, `DifferentAccountsValidator`,
`DebitAccountStateValidator`, etc.) ensamblado en una cadena específica por tipo de
operación mediante `TransactionValidationChainProvider`, permitiendo agregar o probar
reglas sin tocar las demás.

---

## LEG-04 — Estructura duplicada entre `deposit`, `withdraw` y `transfer`

**Ubicación:**

```text
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:49-80
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:84-131
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:135-196
```

**Fragmento/descripción concreta:**

Los tres métodos públicos repiten la misma secuencia de siete pasos: validar
monto/estado, calcular comisión, actualizar saldo(s), construir y persistir
`BankTransaction`, registrar auditoría, enviar notificación simulada y mapear la
respuesta. La única diferencia real entre ellos es qué cuenta(s) se cargan y cómo se
mueve el saldo.

**Code smell:** Duplicated Code.

**Principio SOLID afectado:** SRP y OCP — cualquier ajuste al flujo común (por
ejemplo, agregar un paso transversal nuevo) debe replicarse manualmente en los tres
métodos.

**Consecuencia:** correcciones y mejoras al flujo común deben aplicarse tres veces,
con riesgo de que una de las copias quede desactualizada.

**Patrón elegido:** Template Method.

**Razón:** `AbstractTransactionProcessor.process()` fija el algoritmo común
(`loadContext -> resolvePolicy -> calculateFee -> validate -> applyMovement ->
persistTransaction -> publishCompletedEvent -> buildResult`) como método `final`, y
`DepositTransactionProcessor`, `WithdrawalTransactionProcessor` y
`TransferTransactionProcessor` solo implementan el paso variable de movimiento de
saldo.

---

## LEG-05 — Auditoría y notificación acopladas directamente al flujo de transacción

**Ubicación:**

```text
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:74-76
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:125-127
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:188-192
legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:248-259
```

**Fragmento/descripción concreta:**

Tras persistir cada `BankTransaction`, los tres métodos llaman directamente a
`createAuditRecord(...)` y `sendSimulatedNotification(...)` (métodos privados
definidos en 248-259). El servicio de transacciones conoce y ejecuta explícitamente
ambas acciones secundarias.

**Code smell:** High Coupling — el flujo principal de negocio está atado a acciones
que no son parte del cálculo del movimiento de dinero.

**Principio SOLID afectado:** SRP (el servicio hace más de lo que su nombre indica) y
DIP (depende directamente de `AuditRecordRepository` para una preocupación
secundaria en lugar de depender de una abstracción de notificación de eventos).

**Consecuencia:** agregar una nueva acción posterior a una transacción exitosa (por
ejemplo, otra forma de notificación) exige modificar el flujo principal en sus tres
puntos de uso.

**Patrón elegido:** Observer.

**Razón:** `TransactionEventPublisher` publica un `TransactionCompletedEvent` una sola
vez al final de `AbstractTransactionProcessor.process()`, y
`AuditTransactionObserver`/`NotificationTransactionObserver` reaccionan de forma
desacoplada, sin que el procesador conozca a los observadores concretos.
