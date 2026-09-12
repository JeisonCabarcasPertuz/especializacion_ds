# Matriz antes/después — Core Banking Refactoring Lab

Relación completa: problema legacy → code smell/SOLID → patrón → clases refactor →
prueba → evidencia.

| ID | Legacy archivo:línea | Problema | Code smell | SOLID | Patrón | Refactor archivo/clases | Prueba | Resultado |
|---|---|---|---|---|---|---|---|---|
| LEG-01 | `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:205-242` | `calculateFee`/`calculateLimit` con `switch`/`if` anidado por `AccountType` | Switch Statements | OCP, SRP | Strategy | `refactor/.../pattern/strategy/AccountTransactionPolicy.java`, `SavingsTransactionPolicy.java`, `CheckingTransactionPolicy.java`, `TransactionPolicyResolver.java` | `SavingsTransactionPolicyTest`, `CheckingTransactionPolicyTest` | El `switch` desaparece de `TransactionServiceImpl`; agregar un tipo de cuenta nuevo solo requiere una clase `AccountTransactionPolicy` adicional. |
| LEG-02 | `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:56-59,91-98,147-158` y `AccountServiceImpl.java:79-89` | Condicionales de `AccountStatus` repetidos en cuatro puntos distintos | Duplicated Conditional | OCP, SRP | State | `refactor/.../pattern/state/AccountState.java`, `ActiveAccountState.java`, `BlockedAccountState.java`, `ClosedAccountState.java`, `AccountStateResolver.java`, `AccountStateContext.java` | `ActiveAccountStateTest`, `BlockedAccountStateTest`, `ClosedAccountStateTest` | Las reglas de débito/crédito/transición viven una sola vez por estado; `TransactionServiceImpl` y `AccountServiceImpl` delegan en `AccountStateContext`. |
| LEG-03 | `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:136-172` | Bloque de siete validaciones secuenciales dentro de `transfer()` | Long Method | SRP, OCP | Chain of Responsibility | `refactor/.../pattern/chain/TransactionValidator.java`, `AbstractTransactionValidator.java`, `PositiveAmountValidator.java`, `DifferentAccountsValidator.java`, `DebitAccountStateValidator.java`, `CreditAccountStateValidator.java`, `TransactionLimitValidator.java`, `SufficientBalanceValidator.java`, `TransactionValidationChainProvider.java` | `TransactionValidationChainTest` | Cada regla es una clase probable de forma aislada; `TransactionValidationChainProvider` compone una cadena distinta por tipo de operación sin condicionales en el servicio. |
| LEG-04 | `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:49-80,84-131,135-196` | Estructura de siete pasos duplicada entre `deposit`, `withdraw` y `transfer` | Duplicated Code | SRP, OCP | Template Method | `refactor/.../pattern/template/TransactionCommand.java`, `TransactionContext.java`, `AbstractTransactionProcessor.java`, `DepositTransactionProcessor.java`, `WithdrawalTransactionProcessor.java`, `TransferTransactionProcessor.java` | `DepositTransactionProcessorTest`, `WithdrawalTransactionProcessorTest`, `TransferTransactionProcessorTest` | `AbstractTransactionProcessor.process()` fija el algoritmo común; `TransactionServiceImpl` queda reducido a construir un `TransactionCommand` y delegar. |
| LEG-05 | `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java:74-76,125-127,188-192,248-259` | Auditoría y notificación simulada invocadas directamente desde el flujo principal | High Coupling | SRP, DIP | Observer | `refactor/.../pattern/observer/TransactionCompletedEvent.java`, `TransactionObserver.java`, `AuditTransactionObserver.java`, `NotificationTransactionObserver.java`, `TransactionEventPublisher.java` | `TransactionEventPublisherTest`, `AuditTransactionObserverTest` | `AbstractTransactionProcessor` solo conoce `TransactionEventPublisher`; agregar una reacción nueva no exige modificar el procesador. |

## Verificación de trazabilidad

Comando usado para confirmar que ninguna referencia de `legacy` es inventada:

```bash
grep -R "legacy/src" docs/refactor
```

Las cinco ubicaciones citadas corresponden a `legacy` congelado en el tag `v1-legacy`
y fueron verificadas línea por línea contra el archivo real antes de escribir este
documento.
