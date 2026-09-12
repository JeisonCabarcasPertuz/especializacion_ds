# Tareas — Plan de Implementación del Core Banking Refactoring Lab

> **Para agentes IA:** ejecutar este documento tarea por tarea, respetando los
> checkboxes y los puntos de verificación. No comenzar una tarea si sus dependencias
> no están terminadas.
>
> **Estado:** Borrador final para aprobación — Fase 3 de 3
>
> **Dependencias aprobadas:**
>
> 1. `docs/rubricas/proyecto.md`
> 2. `requerimientos.md`
> 3. `diseño.md`
>
> **Objetivo:** construir dos aplicaciones Spring Boot funcionalmente equivalentes,
> `legacy` y `refactor`, demostrar cinco problemas concretos del código inicial y
> resolverlos con Strategy, State, Chain of Responsibility, Template Method y Observer.
>
> **Arquitectura:** dos proyectos Maven independientes con Java 21, Spring Boot 4.1.1,
> MVC por capas, JPA, PostgreSQL, Flyway, Lombok y Docker Compose. `legacy` implementa
> la funcionalidad con cinco problemas de diseño realistas; `refactor` conserva los
> contratos y comportamiento mientras corrige esos problemas con cinco patrones GoF.
>
> **Tech Stack:** Java 21, Spring Boot 4.1.1, Maven, Lombok, Spring Web, Bean Validation,
> Spring Data JPA, PostgreSQL, Flyway, H2, JUnit 5, Mockito, Docker Compose.
>
> **Spec:** `entregas/jeison_cabarcas/proyecto/diseño.md`

---

# 1. Restricciones globales

Estas reglas aplican a todas las tareas.

- [ ] Todo el código debe quedar bajo `entregas/jeison_cabarcas/proyecto`.
- [ ] Usar Java 21.
- [ ] Usar Spring Boot 4.1.1.
- [ ] Usar Maven Wrapper.
- [ ] Usar Lombok donde reduzca boilerplate sin ocultar decisiones de diseño.
- [ ] Usar Spring Data JPA.
- [ ] Usar PostgreSQL para ejecución normal.
- [ ] Usar Flyway para esquema normal.
- [ ] Usar H2 únicamente para pruebas.
- [ ] Usar `BigDecimal` para dinero.
- [ ] Usar `java.time.Instant` para timestamps.
- [ ] Usar UUID para identificadores técnicos.
- [ ] No exponer entidades JPA desde controllers.
- [ ] Usar inyección por constructor.
- [ ] No usar `@Autowired` sobre atributos.
- [ ] No agregar Spring Security, Kafka, Redis, RabbitMQ, WebFlux, MapStruct ni
      Testcontainers.
- [ ] No agregar patrones GoF distintos de los cinco aprobados.
- [ ] No usar emojis en código, comentarios, logs o documentación técnica.
- [ ] No afirmar que una prueba, build o contenedor funciona sin ejecutarlo.
- [ ] No crear `refactor` hasta congelar y diagnosticar `legacy`.

---

# 2. Convenciones del plan

## 2.1 Definición de terminado

Una tarea está terminada únicamente cuando:

1. los archivos indicados existen;
2. el código compila;
3. las pruebas indicadas pasan;
4. se ejecuta el comando de verificación;
5. no se modifica funcionalidad fuera del alcance;
6. se registra el commit indicado cuando Git esté disponible.

## 2.2 Comandos base

Linux/macOS/Git Bash:

```bash
./mvnw test
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Docker:

```bash
docker compose up --build -d
docker compose ps
docker compose logs app
docker compose down -v
```

## 2.3 Regla de TDD para refactor

Cada patrón de `refactor` debe seguir este ciclo:

```text
1. escribir prueba que exprese el comportamiento;
2. ejecutar y confirmar fallo;
3. implementar mínimo necesario;
4. ejecutar y confirmar éxito;
5. ejecutar suite completa;
6. commit.
```

---

# FASE A — PREPARACIÓN

### Task 1: Crear estructura raíz y archivos de gobierno

**Dependencias:** ninguna.

**Archivos:**
- Crear: `entregas/jeison_cabarcas/proyecto/legacy/`
- Crear: `entregas/jeison_cabarcas/proyecto/refactor/`
- Crear: `entregas/jeison_cabarcas/proyecto/docs/api/`
- Crear: `entregas/jeison_cabarcas/proyecto/docs/refactor/`
- Preservar: `entregas/jeison_cabarcas/proyecto/requerimientos.md`
- Preservar: `entregas/jeison_cabarcas/proyecto/diseño.md`
- Crear: `entregas/jeison_cabarcas/proyecto/.gitignore`

**Produce:**
- estructura física base;
- exclusiones de build/IDE;
- ubicación definitiva para ambas versiones.

- [ ] **Step 1: Crear directorios**

```bash
mkdir -p entregas/jeison_cabarcas/proyecto/{legacy,refactor,docs/api,docs/refactor}
```

- [ ] **Step 2: Crear `.gitignore`**

Contenido mínimo:

```gitignore
**/target/
**/.idea/
**/.vscode/
**/*.iml
**/.classpath
**/.project
**/.settings/
**/.DS_Store
**/.env
```

- [ ] **Step 3: Verificar que `requerimientos.md` y `diseño.md` estén en la raíz**

```bash
ls -la entregas/jeison_cabarcas/proyecto
```

Esperado:

```text
legacy
refactor
docs
requerimientos.md
diseño.md
```

- [ ] **Step 4: Commit**

```bash
git add entregas/jeison_cabarcas/proyecto
git commit -m "chore: prepare core banking project structure"
```

**Criterio de terminado:** estructura raíz creada sin código de aplicación.

---

# FASE B — CONSTRUCCIÓN DE LEGACY

### Task 2: Inicializar proyecto Spring Boot legacy

**Dependencias:** Task 1.

**Archivos:**
- Crear: `legacy/pom.xml`
- Crear: `legacy/mvnw`
- Crear: `legacy/mvnw.cmd`
- Crear: `legacy/.mvn/wrapper/*`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/CoreBankingApplication.java`
- Crear: `legacy/src/main/resources/application.yml`
- Crear: `legacy/src/test/resources/application-test.yml`
- Crear: `legacy/src/test/java/com/unimagdalena/corebanking/CoreBankingApplicationTests.java`

**Dependencias Maven obligatorias:**

```text
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-actuator
postgresql
flyway-core
flyway-database-postgresql
lombok
spring-boot-starter-test
h2
```

**Configuración normal:**

```yaml
spring:
  application:
    name: core-banking-legacy
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5433/core_banking_legacy}
    username: ${SPRING_DATASOURCE_USERNAME:corebanking}
    password: ${SPRING_DATASOURCE_PASSWORD:corebanking}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health

server:
  port: ${SERVER_PORT:8081}
```

**Perfil test:**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:corebanking;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  flyway:
    enabled: false
  jpa:
    hibernate:
      ddl-auto: create-drop
```

- [ ] **Step 1: Crear `pom.xml` con Java 21 y Spring Boot 4.1.1.**
- [ ] **Step 2: Crear clase principal.**
- [ ] **Step 3: Crear configuración normal y test.**
- [ ] **Step 4: Crear context test mínimo.**

```java
@SpringBootTest
@ActiveProfiles("test")
class CoreBankingApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 5: Ejecutar prueba**

```bash
cd entregas/jeison_cabarcas/proyecto/legacy
./mvnw test
```

Esperado:

```text
BUILD SUCCESS
```

- [ ] **Step 6: Commit**

```bash
git add legacy
git commit -m "chore: initialize legacy Spring Boot application"
```

**Criterio de terminado:** proyecto legacy vacío compila con Java 21.

---

### Task 3: Crear enums y modelo JPA legacy

**Dependencias:** Task 2.

**Archivos:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/enums/DocumentType.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/enums/AccountType.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/enums/AccountStatus.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/enums/TransactionType.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/enums/TransactionStatus.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/entity/Customer.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/entity/BankAccount.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/entity/BankTransaction.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/entity/AuditRecord.java`
- Crear: `legacy/src/main/resources/db/migration/V1__create_core_banking_schema.sql`

**Enums exactos:**

```java
public enum DocumentType { CC, CE, PASSPORT }
public enum AccountType { SAVINGS, CHECKING }
public enum AccountStatus { ACTIVE, BLOCKED, CLOSED }
public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }
public enum TransactionStatus { COMPLETED }
```

**Campos `BankAccount`:**

```text
UUID id
String accountNumber
Customer customer
AccountType accountType
AccountStatus status
BigDecimal balance
String currency
Long version
Instant createdAt
Instant updatedAt
```

- [ ] **Step 1: Crear enums.**
- [ ] **Step 2: Crear `Customer`.**
- [ ] **Step 3: Crear `BankAccount` con `@Version`.**
- [ ] **Step 4: Crear `BankTransaction`.**
- [ ] **Step 5: Crear `AuditRecord` con relación `ManyToOne` a `BankTransaction`.**
- [ ] **Step 6: Crear migración SQL con `NUMERIC(19,2)`.**
- [ ] **Step 7: Ejecutar tests**

```bash
./mvnw test
```

Esperado: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add src
git commit -m "feat: add legacy core banking domain model"
```

**Criterio de terminado:** modelo JPA y migración representan el mismo dominio aprobado.

---

### Task 4: Crear repositories legacy

**Dependencias:** Task 3.

**Archivos:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/repository/CustomerRepository.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/repository/BankAccountRepository.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/repository/BankTransactionRepository.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/repository/AuditRecordRepository.java`

**Interfaces:**

```java
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByDocumentNumber(String documentNumber);
}
```

```java
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    List<BankAccount> findAllByCustomerId(UUID customerId);
}
```

Para historial:

```java
@Query("""
    select t from BankTransaction t
    where t.sourceAccount.id = :accountId
       or t.destinationAccount.id = :accountId
    order by t.createdAt desc
    """)
List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
```

- [ ] **Step 1: Crear repositories.**
- [ ] **Step 2: Ejecutar compile**

```bash
./mvnw -q -DskipTests compile
```

- [ ] **Step 3: Ejecutar suite**

```bash
./mvnw test
```

- [ ] **Step 4: Commit**

```bash
git add src
git commit -m "feat: add legacy persistence repositories"
```

**Criterio de terminado:** repositories compilan y no contienen lógica de negocio.

---

### Task 5: Crear DTOs, mappers y manejo de errores legacy

**Dependencias:** Task 4.

**Archivos principales:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/CreateCustomerRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/CreateAccountRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/UpdateAccountStatusRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/DepositRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/WithdrawalRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/request/TransferRequest.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/CustomerResponse.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/AccountResponse.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/BalanceResponse.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/TransactionResponse.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/ApiErrorResponse.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/dto/response/FieldValidationError.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/mapper/CustomerMapper.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/mapper/AccountMapper.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/mapper/TransactionMapper.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/exception/BusinessException.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/exception/ResourceNotFoundException.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/exception/GlobalExceptionHandler.java`

**Request monetario obligatorio:**

```java
@NotNull
@DecimalMin("0.01")
@Digits(integer = 17, fraction = 2)
BigDecimal amount;
```

**ApiErrorResponse:**

```text
Instant timestamp
int status
String error
String code
String message
String path
List<FieldValidationError> details
```

- [ ] **Step 1: Crear requests con Bean Validation.**
- [ ] **Step 2: Crear responses.**
- [ ] **Step 3: Crear mappers manuales.**
- [ ] **Step 4: Crear excepciones y `@RestControllerAdvice`.**
- [ ] **Step 5: Verificar mapeos HTTP del diseño.**
- [ ] **Step 6: Ejecutar tests.**
- [ ] **Step 7: Commit.**

```bash
git add .
git commit -m "feat: add legacy API contracts and error handling"
```

**Criterio de terminado:** entidades no se exponen directamente.

---

### Task 6: Implementar CustomerService y CustomerController legacy

**Dependencias:** Task 5.

**Archivos:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/interfaces/CustomerService.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/CustomerServiceImpl.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/controller/CustomerController.java`
- Crear: `legacy/src/test/java/com/unimagdalena/corebanking/service/CustomerServiceImplTest.java`

**Interfaz:**

```java
public interface CustomerService {
    CustomerResponse create(CreateCustomerRequest request);
    CustomerResponse getById(UUID customerId);
    List<CustomerResponse> findAll();
}
```

**Endpoints:**

```text
POST /api/v1/customers
GET  /api/v1/customers/{customerId}
GET  /api/v1/customers
```

- [ ] **Step 1: Escribir prueba para documento duplicado.**
- [ ] **Step 2: Ejecutar y confirmar fallo.**
- [ ] **Step 3: Implementar servicio mínimo.**
- [ ] **Step 4: Crear controller delgado.**
- [ ] **Step 5: Ejecutar test.**
- [ ] **Step 6: Ejecutar suite.**
- [ ] **Step 7: Commit.**

```bash
git add .
git commit -m "feat: add legacy customer management"
```

**Criterio de terminado:** cliente puede crearse, consultarse y listarse.

---

### Task 7: Implementar AccountService y AccountController legacy

**Dependencias:** Task 6.

**Archivos:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/interfaces/AccountService.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/AccountServiceImpl.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/controller/AccountController.java`
- Crear: `legacy/src/test/java/com/unimagdalena/corebanking/service/AccountServiceImplTest.java`

**Interfaz:**

```java
public interface AccountService {
    AccountResponse create(UUID customerId, CreateAccountRequest request);
    AccountResponse getById(UUID accountId);
    List<AccountResponse> findByCustomerId(UUID customerId);
    BalanceResponse getBalance(UUID accountId);
    AccountResponse changeStatus(UUID accountId, UpdateAccountStatusRequest request);
}
```

**Regla legacy importante:**

`changeStatus()` debe usar condicionales explícitos por estado para que exista
`LEG-02`, pero debe implementar correctamente las transiciones definidas.

Ejemplo de estructura permitida:

```java
if (current == AccountStatus.CLOSED) {
    throw new BusinessException(...);
}

if (current == AccountStatus.ACTIVE) {
    // validar destino
}

if (current == AccountStatus.BLOCKED) {
    // validar destino
}
```

- [ ] **Step 1: Escribir tests de transición válida e inválida.**
- [ ] **Step 2: Confirmar fallo.**
- [ ] **Step 3: Implementar servicio con lógica condicional realista.**
- [ ] **Step 4: Crear controller.**
- [ ] **Step 5: Ejecutar tests.**
- [ ] **Step 6: Commit.**

```bash
git add .
git commit -m "feat: add legacy bank account management"
```

**Criterio de terminado:** cuentas y estados funcionan aunque el diseño de estados aún sea condicional.

---

### Task 8: Implementar TransactionService legacy con los cinco problemas diagnosticables

**Dependencias:** Tasks 6–7.

**Archivos:**
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/interfaces/TransactionService.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java`
- Crear: `legacy/src/main/java/com/unimagdalena/corebanking/controller/TransactionController.java`
- Crear: `legacy/src/test/java/com/unimagdalena/corebanking/service/TransactionServiceImplSmokeTest.java`

**Interfaz:**

```java
public interface TransactionService {
    TransactionResponse deposit(DepositRequest request);
    TransactionResponse withdraw(WithdrawalRequest request);
    TransactionResponse transfer(TransferRequest request);
    List<TransactionResponse> findByAccountId(UUID accountId);
}
```

**Problemas que deben existir sin romper funcionalidad:**

```text
LEG-01 -> calculateFee()/calculateLimit() con switch/if por AccountType
LEG-02 -> validaciones de AccountStatus con if repetidos
LEG-03 -> múltiples validaciones secuenciales dentro de métodos
LEG-04 -> estructura repetida entre deposit/withdraw/transfer
LEG-05 -> auditoría + notificación simulada llamadas directamente desde el servicio
```

**Reglas exactas:**

```text
SAVINGS transfer fee      1000
SAVINGS withdrawal fee       0
SAVINGS max debit       5000000

CHECKING transfer fee       1500
CHECKING withdrawal fee     2000
CHECKING max debit      10000000
```

**Transacción:**

```java
@Transactional
public TransactionResponse transfer(TransferRequest request) {
    // cargar
    // validar
    // calcular fee
    // actualizar ambos saldos
    // persistir
    // auditar directamente
    // registrar notificación simulada directamente
    // mapear response
}
```

Los comentarios anteriores son guía del plan y no deben copiarse literalmente al código final.

- [ ] **Step 1: Escribir smoke test de depósito.**
- [ ] **Step 2: Escribir smoke test de transferencia exitosa.**
- [ ] **Step 3: Escribir smoke test de saldo insuficiente.**
- [ ] **Step 4: Ejecutar y confirmar fallos.**
- [ ] **Step 5: Implementar `deposit()`.**
- [ ] **Step 6: Implementar `withdraw()`.**
- [ ] **Step 7: Implementar `transfer()`.**
- [ ] **Step 8: Implementar consulta de historial.**
- [ ] **Step 9: Implementar controller.**
- [ ] **Step 10: Ejecutar tests.**

```bash
./mvnw test
```

- [ ] **Step 11: Revisar manualmente que los cinco problemas sean visibles.**
- [ ] **Step 12: Commit.**

```bash
git add .
git commit -m "feat: complete functional legacy transaction flow"
```

**Criterio de terminado:** legacy funciona y contiene exactamente los problemas previstos de forma natural.

---

### Task 9: Dockerizar y verificar legacy

**Dependencias:** Task 8.

**Archivos:**
- Crear: `legacy/Dockerfile`
- Crear: `legacy/compose.yaml`

**Dockerfile:**

Multi-stage:

```text
build -> JDK/Maven
runtime -> JRE 21
```

**Puertos:**

```text
app: 8081
postgres host: 5433
postgres container: 5432
```

- [ ] **Step 1: Crear Dockerfile.**
- [ ] **Step 2: Crear compose con `app` y `postgres`.**
- [ ] **Step 3: Configurar healthcheck de PostgreSQL.**
- [ ] **Step 4: Configurar healthcheck de app sobre `/actuator/health`.**
- [ ] **Step 5: Construir.**

```bash
docker compose up --build -d
```

- [ ] **Step 6: Verificar estado.**

```bash
docker compose ps
curl http://localhost:8081/actuator/health
```

Esperado:

```json
{"status":"UP"}
```

- [ ] **Step 7: Detener.**

```bash
docker compose down -v
```

- [ ] **Step 8: Commit.**

```bash
git add Dockerfile compose.yaml
git commit -m "chore: dockerize legacy application"
```

**Criterio de terminado:** legacy ejecuta con Docker y PostgreSQL.

---

# FASE C — CONGELAMIENTO Y DIAGNÓSTICO

### Task 10: Congelar legacy y generar diagnóstico real

**Dependencias:** Task 9.

**Archivos:**
- Crear: `docs/refactor/diagnostico.md`

**Produce:**
- evidencia `archivo:línea` real;
- cinco problemas;
- relación preliminar con patrón.

- [ ] **Step 1: Ejecutar suite legacy.**

```bash
cd legacy
./mvnw clean test
```

Debe terminar `BUILD SUCCESS`.

- [ ] **Step 2: Ejecutar package.**

```bash
./mvnw clean package
```

Debe terminar `BUILD SUCCESS`.

- [ ] **Step 3: Capturar números reales de líneas.**

Git Bash/Linux:

```bash
nl -ba src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java
nl -ba src/main/java/com/unimagdalena/corebanking/service/AccountServiceImpl.java
```

PowerShell:

```powershell
$line=0; Get-Content .\src\main\java\com\unimagdalena\corebanking\service\TransactionServiceImpl.java |
  ForEach-Object { $line++; "{0,5} {1}" -f $line, $_ }
```

- [ ] **Step 4: Crear `diagnostico.md` con cinco secciones.**

Cada sección debe contener exactamente:

```text
ID
Ubicación archivo:línea
Fragmento o descripción concreta
Code smell
Principio SOLID afectado
Consecuencia
Patrón elegido
Razón por la que el patrón resuelve ese problema
```

- [ ] **Step 5: Verificar que cada referencia existe.**
- [ ] **Step 6: Commit.**

```bash
git add docs/refactor/diagnostico.md legacy
git commit -m "docs: freeze and diagnose legacy implementation"
```

- [ ] **Step 7: Crear tag.**

```bash
git tag v1-legacy
```

- [ ] **Step 8: Confirmar que no quedan cambios pendientes.**

```bash
git status
```

Esperado:

```text
working tree clean
```

**Criterio de terminado:** `legacy` queda congelado y los cinco problemas tienen líneas reales.

---

# FASE D — BASE DE REFACTOR

### Task 11: Crear refactor a partir de la base funcional legacy

**Dependencias:** Task 10.

**Archivos:**
- Crear/copiar toda la estructura de `refactor/`.
- Modificar: `refactor/pom.xml`
- Modificar: `refactor/src/main/resources/application.yml`
- Mantener los mismos contratos HTTP.

**Cambios permitidos inicialmente:**

```text
artifactId -> core-banking-refactor
spring.application.name -> core-banking-refactor
server.port -> 8082
datasource default port -> 5434
database -> core_banking_refactor
```

No aplicar todavía patrones.

- [ ] **Step 1: Copiar base funcional.**
- [ ] **Step 2: Cambiar identidad y puertos.**
- [ ] **Step 3: Ejecutar tests.**

```bash
cd refactor
./mvnw test
```

- [ ] **Step 4: Comparar endpoints con legacy.**
- [ ] **Step 5: Commit.**

```bash
git add refactor
git commit -m "chore: create refactor baseline from legacy behavior"
```

**Criterio de terminado:** `refactor` se comporta como legacy antes de aplicar patrones.

---

# FASE E — REFACTOR POR PATRONES

### Task 12: Aplicar Strategy a LEG-01

**Dependencias:** Task 11.

**Archivos:**
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/strategy/AccountTransactionPolicy.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/strategy/SavingsTransactionPolicy.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/strategy/CheckingTransactionPolicy.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/strategy/TransactionPolicyResolver.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/strategy/SavingsTransactionPolicyTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/strategy/CheckingTransactionPolicyTest.java`
- Modificar: `refactor/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java`.

**Interfaz exacta:**

```java
public interface AccountTransactionPolicy {
    AccountType supportedAccountType();
    BigDecimal calculateFee(TransactionType transactionType, BigDecimal amount);
    BigDecimal maxDebitAmount();
}
```

**Comportamientos:**

```text
Savings:
  deposit 0
  withdrawal 0
  transfer 1000
  limit 5000000

Checking:
  deposit 0
  withdrawal 2000
  transfer 1500
  limit 10000000
```

- [ ] **Step 1: Escribir `SavingsTransactionPolicyTest`.**
- [ ] **Step 2: Escribir `CheckingTransactionPolicyTest`.**
- [ ] **Step 3: Ejecutar y confirmar fallo.**
- [ ] **Step 4: Implementar interface y strategies.**
- [ ] **Step 5: Implementar resolver basado en `Map<AccountType, AccountTransactionPolicy>`.**
- [ ] **Step 6: Sustituir cálculo legacy de fee/límite por Strategy.**
- [ ] **Step 7: Eliminar `switch`/`if` de tipo de cuenta del servicio.**
- [ ] **Step 8: Ejecutar tests focalizados.**

```bash
./mvnw -Dtest="*TransactionPolicy*" test
```

- [ ] **Step 9: Ejecutar suite.**
- [ ] **Step 10: Commit.**

```bash
git add .
git commit -m "refactor: replace account type conditionals with Strategy"
```

**Criterio de terminado:** LEG-01 deja de existir en refactor y las reglas monetarias no cambian.

---

### Task 13: Aplicar State a LEG-02

**Dependencias:** Task 12.

**Archivos:**
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/AccountState.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/ActiveAccountState.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/BlockedAccountState.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/ClosedAccountState.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/AccountStateResolver.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/state/AccountStateContext.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/state/ActiveAccountStateTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/state/BlockedAccountStateTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/state/ClosedAccountStateTest.java`
- Modificar: `refactor/src/main/java/com/unimagdalena/corebanking/service/AccountServiceImpl.java`
- Modificar: `refactor/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java`.

**Interfaz:**

```java
public interface AccountState {
    AccountStatus status();
    void assertCanDebit();
    void assertCanCredit();
    void assertCanTransitionTo(AccountStatus target);
}
```

- [ ] **Step 1: Escribir tests de ACTIVE.**
- [ ] **Step 2: Escribir tests de BLOCKED.**
- [ ] **Step 3: Escribir tests de CLOSED.**
- [ ] **Step 4: Ejecutar y confirmar fallo.**
- [ ] **Step 5: Implementar estados.**
- [ ] **Step 6: Implementar resolver.**
- [ ] **Step 7: Implementar contexto.**
- [ ] **Step 8: Reemplazar condicionales de `changeStatus()`.**
- [ ] **Step 9: Reemplazar decisiones repetidas de debit/credit.**
- [ ] **Step 10: Ejecutar tests focalizados.**

```bash
./mvnw -Dtest="*AccountState*" test
```

- [ ] **Step 11: Ejecutar suite.**
- [ ] **Step 12: Commit.**

```bash
git add .
git commit -m "refactor: encapsulate account behavior with State"
```

**Criterio de terminado:** comportamiento por estado vive en clases State y no en condicionales dispersos.

---

### Task 14: Aplicar Chain of Responsibility a LEG-03

**Dependencias:** Tasks 12–13.

**Archivos:**
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/TransactionValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/AbstractTransactionValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/PositiveAmountValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/DifferentAccountsValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/DebitAccountStateValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/CreditAccountStateValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/TransactionLimitValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/SufficientBalanceValidator.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/chain/TransactionValidationChainProvider.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/TransactionContext.java` como objeto interno de trabajo.
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/chain/TransactionValidationChainTest.java`

**Contrato:**

```java
public interface TransactionValidator {
    TransactionValidator setNext(TransactionValidator next);
    void validate(TransactionContext context);
}
```

**Cadenas exactas:**

```text
DEPOSIT:
PositiveAmount -> CreditAccountState

WITHDRAWAL:
PositiveAmount -> DebitAccountState -> TransactionLimit -> SufficientBalance

TRANSFER:
PositiveAmount -> DifferentAccounts -> DebitAccountState ->
CreditAccountState -> TransactionLimit -> SufficientBalance
```

**Regla de concurrencia del patrón:**

El provider debe crear una cadena nueva por operación. No reutilizar un handler mutable
enlazándolo a diferentes `next`.

- [ ] **Step 1: Escribir test de transferencia válida.**
- [ ] **Step 2: Escribir test de misma cuenta.**
- [ ] **Step 3: Escribir test de saldo insuficiente.**
- [ ] **Step 4: Confirmar fallos.**
- [ ] **Step 5: Implementar contrato/base handler.**
- [ ] **Step 6: Implementar handlers concretos.**
- [ ] **Step 7: Implementar provider por tipo de transacción.**
- [ ] **Step 8: Integrar Strategy en `TransactionLimitValidator`.**
- [ ] **Step 9: Integrar State en validadores de estado.**
- [ ] **Step 10: Eliminar bloque monolítico de validaciones del servicio.**
- [ ] **Step 11: Ejecutar tests focalizados.**
- [ ] **Step 12: Ejecutar suite.**
- [ ] **Step 13: Commit.**

```bash
git add .
git commit -m "refactor: compose transaction validations with Chain of Responsibility"
```

**Criterio de terminado:** agregar una regla nueva no exige editar un método monolítico de validación.

---

### Task 15: Aplicar Template Method a LEG-04

**Dependencias:** Task 14.

**Archivos:**
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/TransactionCommand.java`
- Completar: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/TransactionContext.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/AbstractTransactionProcessor.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/DepositTransactionProcessor.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/WithdrawalTransactionProcessor.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/TransferTransactionProcessor.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/template/DepositTransactionProcessorTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/template/WithdrawalTransactionProcessorTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/template/TransferTransactionProcessorTest.java`
- Modificar: `refactor/src/main/java/com/unimagdalena/corebanking/service/TransactionServiceImpl.java`

**Método plantilla:**

```java
public final TransactionResponse process(TransactionCommand command) {
    TransactionContext context = loadContext(command);
    resolvePolicy(context);
    calculateFee(context);
    validate(context);
    applyMovement(context);
    persistTransaction(context);
    publishCompletedEvent(context);
    return buildResult(context);
}
```

**Responsabilidades concretas:**

```text
Deposit:
  destination += amount

Withdrawal:
  source -= amount + fee

Transfer:
  source -= amount + fee
  destination += amount
```

- [ ] **Step 1: Escribir test de processor de depósito.**
- [ ] **Step 2: Escribir test de processor de retiro.**
- [ ] **Step 3: Escribir test de processor de transferencia.**
- [ ] **Step 4: Confirmar fallos.**
- [ ] **Step 5: Crear `TransactionCommand`.**
- [ ] **Step 6: Completar `TransactionContext`.**
- [ ] **Step 7: Crear clase abstracta con `process()` final.**
- [ ] **Step 8: Implementar tres processors.**
- [ ] **Step 9: Reducir `TransactionServiceImpl` a coordinación/delegación.**
- [ ] **Step 10: Mantener `@Transactional` en métodos públicos del service.**
- [ ] **Step 11: Ejecutar tests.**
- [ ] **Step 12: Ejecutar suite completa.**
- [ ] **Step 13: Commit.**

```bash
git add .
git commit -m "refactor: standardize transaction flow with Template Method"
```

**Criterio de terminado:** desaparece la duplicación estructural de los tres flujos.

---

### Task 16: Aplicar Observer a LEG-05

**Dependencias:** Task 15.

**Archivos:**
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/observer/TransactionCompletedEvent.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/observer/TransactionObserver.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/observer/AuditTransactionObserver.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/observer/NotificationTransactionObserver.java`
- Crear: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/observer/TransactionEventPublisher.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/observer/TransactionEventPublisherTest.java`
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/pattern/observer/AuditTransactionObserverTest.java`
- Modificar: `refactor/src/main/java/com/unimagdalena/corebanking/pattern/template/AbstractTransactionProcessor.java`
- Eliminar dependencias directas de auditoría/notificación del servicio/procesador.

**Contrato:**

```java
public interface TransactionObserver {
    void onTransactionCompleted(TransactionCompletedEvent event);
}
```

**Publisher:**

```text
constructor -> List<TransactionObserver>
publish(event) -> invoca todos
```

- [ ] **Step 1: Escribir `TransactionEventPublisherTest`.**
- [ ] **Step 2: Escribir `AuditTransactionObserverTest`.**
- [ ] **Step 3: Confirmar fallos.**
- [ ] **Step 4: Crear evento e interfaz.**
- [ ] **Step 5: Implementar auditor.**
- [ ] **Step 6: Implementar notificación simulada mediante log.**
- [ ] **Step 7: Implementar publisher.**
- [ ] **Step 8: Publicar después de persistir transacción.**
- [ ] **Step 9: Eliminar llamadas directas del flujo principal.**
- [ ] **Step 10: Ejecutar tests.**
- [ ] **Step 11: Ejecutar suite.**
- [ ] **Step 12: Commit.**

```bash
git add .
git commit -m "refactor: decouple post-transaction actions with Observer"
```

**Criterio de terminado:** processor depende del publisher, no de observers concretos.

---

# FASE F — PRUEBAS Y PARIDAD

### Task 17: Completar suite unitaria obligatoria de refactor

**Dependencias:** Tasks 12–16.

**Tests mínimos que deben existir:**

```text
SavingsTransactionPolicyTest
CheckingTransactionPolicyTest

ActiveAccountStateTest
BlockedAccountStateTest
ClosedAccountStateTest

TransactionValidationChainTest

DepositTransactionProcessorTest
WithdrawalTransactionProcessorTest
TransferTransactionProcessorTest

TransactionEventPublisherTest
AuditTransactionObserverTest

TransactionServiceImplTest
```

**Casos obligatorios:**

```text
SAVINGS transfer fee = 1000
CHECKING withdrawal fee = 2000

ACTIVE allows debit
BLOCKED rejects debit
BLOCKED allows credit
CLOSED rejects credit
CLOSED cannot reopen

valid transfer passes chain
same account fails
insufficient funds fails

transfer processor debits source amount+fee
transfer processor credits destination amount

publisher invokes both observers
```

- [ ] **Step 1: Inventariar tests existentes.**
- [ ] **Step 2: Agregar únicamente casos faltantes.**
- [ ] **Step 3: Ejecutar todos.**

```bash
./mvnw test
```

- [ ] **Step 4: Corregir únicamente defectos reales descubiertos.**
- [ ] **Step 5: Reejecutar hasta `BUILD SUCCESS`.**
- [ ] **Step 6: Commit.**

```bash
git add .
git commit -m "test: complete refactor pattern coverage"
```

**Criterio de terminado:** cada patrón tiene evidencia automatizada.

---

### Task 18: Crear prueba de integración del flujo crítico

**Dependencias:** Task 17.

**Archivo:**
- Crear: `refactor/src/test/java/com/unimagdalena/corebanking/integration/TransferFlowIntegrationTest.java`

**Flujo:**

```text
crear customer
crear source SAVINGS
crear destination CHECKING
depositar 200000
transferir 75000
verificar source = 124000
verificar destination = 75000
verificar fee = 1000
verificar transacción COMPLETED
verificar AuditRecord
```

Cálculo:

```text
200000 - 75000 - 1000 = 124000
```

- [ ] **Step 1: Escribir integración completa.**
- [ ] **Step 2: Ejecutar test aislado.**

```bash
./mvnw -Dtest=TransferFlowIntegrationTest test
```

- [ ] **Step 3: Confirmar persistencia y auditoría.**
- [ ] **Step 4: Ejecutar suite.**
- [ ] **Step 5: Commit.**

```bash
git add .
git commit -m "test: verify end-to-end transfer flow"
```

**Criterio de terminado:** flujo crítico queda reproducible sin Docker.

---

### Task 19: Verificar paridad funcional legacy/refactor

**Dependencias:** Task 18.

**Comparar:**

```text
endpoints
DTOs
status HTTP
reglas de cuenta
fees
límites
transiciones
historial
errores
```

- [ ] **Step 1: Comparar mappings de controllers.**
- [ ] **Step 2: Comparar DTOs públicos.**
- [ ] **Step 3: Comparar enums persistidos.**
- [ ] **Step 4: Comparar migraciones conceptualmente.**
- [ ] **Step 5: Ejecutar suites de ambos proyectos.**

```bash
(cd legacy && ./mvnw test)
(cd refactor && ./mvnw test)
```

- [ ] **Step 6: Corregir cualquier desviación funcional en `refactor`, no modernizar `legacy`.**
- [ ] **Step 7: Commit si hubo correcciones.**

```bash
git add .
git commit -m "fix: preserve legacy and refactor functional parity"
```

**Criterio de terminado:** diferencias relevantes son de diseño, no de producto.

---

# FASE G — DOCKER Y DEMOSTRACIÓN

### Task 20: Dockerizar refactor y levantar ambas versiones en paralelo

**Dependencias:** Task 19.

**Archivos:**
- Crear: `refactor/Dockerfile`
- Crear: `refactor/compose.yaml`

**Puertos:**

```text
legacy app       8081
legacy postgres  5433

refactor app       8082
refactor postgres  5434
```

- [ ] **Step 1: Crear Dockerfile refactor.**
- [ ] **Step 2: Crear compose refactor.**
- [ ] **Step 3: Levantar legacy.**

```bash
cd legacy
docker compose up --build -d
```

- [ ] **Step 4: Levantar refactor.**

```bash
cd ../refactor
docker compose up --build -d
```

- [ ] **Step 5: Verificar ambos healthchecks.**

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

Ambos deben responder `UP`.

- [ ] **Step 6: Verificar cuatro contenedores operativos.**
- [ ] **Step 7: Detener ambos.**
- [ ] **Step 8: Commit.**

```bash
git add refactor/Dockerfile refactor/compose.yaml
git commit -m "chore: enable parallel legacy and refactor Docker execution"
```

**Criterio de terminado:** ambas aplicaciones pueden ejecutarse al mismo tiempo.

---

### Task 21: Crear requests reproducibles de la demo

**Dependencias:** Task 20.

**Archivo:**
- Crear: `docs/api/core-banking.http`

**Variables:**

```http
@legacyBaseUrl = http://localhost:8081/api/v1
@refactorBaseUrl = http://localhost:8082/api/v1
@baseUrl = {{refactorBaseUrl}}
```

**Flujo obligatorio:**

```text
1. crear cliente
2. crear SAVINGS
3. crear CHECKING
4. depositar 200000 en SAVINGS
5. transferir 75000
6. consultar saldo origen
7. consultar saldo destino
8. consultar historial
9. bloquear SAVINGS
10. intentar retiro y obtener ACCOUNT_BLOCKED
```

- [ ] **Step 1: Crear requests con variables capturables cuando el cliente HTTP lo permita.**
- [ ] **Step 2: Ejecutar flujo contra refactor.**
- [ ] **Step 3: Cambiar `@baseUrl` a legacy.**
- [ ] **Step 4: Ejecutar el mismo flujo.**
- [ ] **Step 5: Confirmar paridad.**
- [ ] **Step 6: Commit.**

```bash
git add docs/api/core-banking.http
git commit -m "docs: add reproducible core banking API demo"
```

**Criterio de terminado:** una misma secuencia demuestra las dos implementaciones.

---

# FASE H — DOCUMENTACIÓN DE EVIDENCIA

### Task 22: Crear matriz antes/después con referencias reales

**Dependencias:** Tasks 10 y 21.

**Archivo:**
- Crear: `docs/refactor/matriz-antes-despues.md`

**Columnas exactas:**

```text
ID
Legacy archivo:línea
Problema
Code smell
SOLID
Patrón
Refactor archivo/clases
Prueba
Resultado
```

- [ ] **Step 1: Leer `diagnostico.md`.**
- [ ] **Step 2: Localizar clases finales en refactor.**
- [ ] **Step 3: Localizar test que evidencia cada solución.**
- [ ] **Step 4: Completar cinco filas.**
- [ ] **Step 5: Validar que ninguna referencia de archivo sea inventada.**
- [ ] **Step 6: Commit.**

```bash
git add docs/refactor/matriz-antes-despues.md
git commit -m "docs: map legacy problems to refactored pattern solutions"
```

**Criterio de terminado:** cada patrón tiene trazabilidad completa.

---

### Task 23: Crear README de arquitectura y refactorización

**Dependencias:** Task 22.

**Archivo:**
- Crear: `docs/README-REFACTORIZACION.md`

**Secciones obligatorias:**

```text
1. Contexto
2. Arquitectura común
3. Legacy
4. Diagnóstico
5. Strategy
6. State
7. Chain of Responsibility
8. Template Method
9. Observer
10. UML legacy
11. UML refactor
12. Secuencia de transferencia
13. Comparación SOLID
14. Alternativas descartadas
15. Trade-offs
16. Pruebas
17. Mejoras futuras
```

**Para cada patrón incluir:**

```text
Problema
Ubicación legacy
Por qué se eligió
Alternativa descartada
Roles GoF
Clases
Principio SOLID
Trade-off
Prueba
```

- [ ] **Step 1: Incorporar los diagramas Mermaid aprobados de `diseño.md`.**
- [ ] **Step 2: Ajustar nombres únicamente si la implementación real difiere.**
- [ ] **Step 3: Incluir referencias reales a clases.**
- [ ] **Step 4: No incluir afirmaciones no demostradas por código/pruebas.**
- [ ] **Step 5: Commit.**

```bash
git add docs/README-REFACTORIZACION.md
git commit -m "docs: explain architecture and pattern refactoring"
```

**Criterio de terminado:** el documento permite comprender el antes/después sin leer todo el código.

---

### Task 24: Crear README principal de ejecución

**Dependencias:** Tasks 20–23.

**Archivo:**
- Crear: `README.md`

**Secciones obligatorias:**

```text
Proyecto
Objetivo
Estructura legacy/refactor
Tecnologías
Prerrequisitos
Ejecutar legacy
Ejecutar refactor
Ejecutar ambos
Healthchecks
Ejecutar tests
Ejecutar demo HTTP
Detener contenedores
Limpiar volúmenes
Endpoints
Documentación adicional
```

**Comandos que deben aparecer y ser probados:**

```bash
cd legacy
docker compose up --build

cd refactor
docker compose up --build

./mvnw test
```

- [ ] **Step 1: Escribir README.**
- [ ] **Step 2: Ejecutar todos los comandos documentados.**
- [ ] **Step 3: Corregir documentación si algún comando difiere.**
- [ ] **Step 4: Commit.**

```bash
git add README.md
git commit -m "docs: add project execution guide"
```

**Criterio de terminado:** un tercero puede levantar el proyecto sin instrucciones externas.

---

### Task 25: Crear guía de sustentación

**Dependencias:** Task 23.

**Archivo:**
- Crear: `docs/SUSTENTACION.md`

**Estructura:**

```text
1. Guion de 10–15 minutos
2. Qué mostrar en legacy
3. LEG-01 → Strategy
4. LEG-02 → State
5. LEG-03 → Chain of Responsibility
6. LEG-04 → Template Method
7. LEG-05 → Observer
8. Demo funcional
9. Pruebas
10. Preguntas probables
11. Respuestas razonadas
12. Qué haría diferente con más tiempo
```

**Preguntas obligatorias:**

```text
¿Por qué Strategy y no switch?
¿Por qué State y no enum con if?
¿Por qué Chain y no validate()?
¿Por qué Template Method y no tres métodos separados?
¿Por qué Observer y no llamadas directas?
¿Qué SOLID mejora cada patrón?
¿Qué desventaja introdujo cada patrón?
¿Por qué no microservicios?
¿Por qué H2 en tests y PostgreSQL en ejecución?
¿Qué pasaría con concurrencia?
¿Qué cambiarías para producción?
```

- [ ] **Step 1: Escribir respuestas basadas en código real.**
- [ ] **Step 2: Mantener respuestas breves y defendibles.**
- [ ] **Step 3: Referenciar clases reales.**
- [ ] **Step 4: Commit.**

```bash
git add docs/SUSTENTACION.md
git commit -m "docs: prepare oral defense guide"
```

**Criterio de terminado:** guía cubre las preguntas improvisadas más probables.

---

# FASE I — AUDITORÍA CONTRA RÚBRICA

### Task 26: Auditoría de diagnóstico — 60 puntos

**Dependencias:** Tasks 10 y 22.

- [ ] Existen cinco problemas concretos y distintos.
- [ ] Cada problema cita `archivo:línea`.
- [ ] Las líneas existen en legacy.
- [ ] Se identifica smell concreto.
- [ ] Se identifica SOLID cuando corresponde.
- [ ] No se usan descripciones genéricas.

**Comando útil:**

```bash
grep -R "legacy/src" docs/refactor
```

**Criterio de terminado:** evidencia preparada para nivel Excelente de diagnóstico.

---

### Task 27: Auditoría de patrones — 100 puntos

**Dependencias:** Tasks 12–16.

Para cada patrón:

- [ ] Resuelve su problema asignado.
- [ ] Tiene roles GoF identificables.
- [ ] No es solamente una anotación/framework feature.
- [ ] No contiene un `switch` que contradiga la intención del patrón.
- [ ] Tiene prueba.
- [ ] Está documentado.

**Matriz:**

```text
LEG-01 Strategy
LEG-02 State
LEG-03 Chain of Responsibility
LEG-04 Template Method
LEG-05 Observer
```

**Criterio de terminado:** cinco patrones implementados realmente.

---

### Task 28: Auditoría de justificación — 50 puntos

**Dependencias:** Task 23.

Para cada patrón confirmar:

- [ ] explica por qué aplica;
- [ ] identifica alternativa razonable;
- [ ] explica por qué se descarta;
- [ ] conecta con SOLID;
- [ ] declara trade-off;
- [ ] referencia problema concreto.

**Criterio de terminado:** cinco justificaciones preparadas para nivel Excelente.

---

### Task 29: Auditoría de calidad — 60 puntos

**Dependencias:** Tasks 17–24.

Ejecutar:

```bash
(cd legacy && ./mvnw clean test)
(cd refactor && ./mvnw clean test)
```

Después:

```bash
(cd legacy && ./mvnw clean package)
(cd refactor && ./mvnw clean package)
```

Verificar:

- [ ] ambas builds exitosas;
- [ ] refactor no introduce violaciones SOLID evidentes;
- [ ] nombres claros;
- [ ] controllers delgados;
- [ ] repositories sin negocio;
- [ ] DTOs separados;
- [ ] pruebas esenciales presentes;
- [ ] demo reproducible;
- [ ] Docker funcional.

**Criterio de terminado:** evidencia preparada para nivel Excelente de calidad.

---

### Task 30: Auditoría de sustentación — 80 puntos

**Dependencias:** Task 25.

Simular una exposición sin leer documentos.

Duración:

```text
10–15 min
```

El estudiante debe poder explicar de memoria:

- [ ] problema general;
- [ ] cinco problemas legacy;
- [ ] cinco patrones;
- [ ] por qué cada patrón;
- [ ] alternativa descartada;
- [ ] SOLID;
- [ ] trade-off;
- [ ] funcionamiento de una transferencia;
- [ ] concurrencia con `@Version`;
- [ ] qué cambiaría en producción.

**Criterio de terminado:** exposición dentro del tiempo y respuestas consistentes.

---

# FASE J — VERIFICACIÓN FINAL Y ENTREGA

### Task 31: Ejecutar verificación final completa

**Dependencias:** Tasks 26–30.

- [ ] **Step 1: Verificar árbol.**

```bash
find . -maxdepth 4 -type f | sort
```

- [ ] **Step 2: Verificar legacy.**

```bash
cd legacy
./mvnw clean test
./mvnw clean package
docker compose up --build -d
curl http://localhost:8081/actuator/health
docker compose down -v
```

- [ ] **Step 3: Verificar refactor.**

```bash
cd ../refactor
./mvnw clean test
./mvnw clean package
docker compose up --build -d
curl http://localhost:8082/actuator/health
docker compose down -v
```

- [ ] **Step 4: Verificar ejecución paralela.**
- [ ] **Step 5: Ejecutar `core-banking.http` contra ambas versiones.**
- [ ] **Step 6: Revisar diagnóstico y matriz.**
- [ ] **Step 7: Revisar README.**
- [ ] **Step 8: Revisar guía de sustentación.**
- [ ] **Step 9: Buscar marcadores prohibidos.**

```bash
grep -R -n -E "TODO|TBD|FIXME|implement later" \
  legacy refactor docs README.md || true
```

Los resultados deben revisarse manualmente; no debe quedar ningún placeholder real.

- [ ] **Step 10: Revisar estado Git.**

```bash
git status
```

- [ ] **Step 11: Commit final.**

```bash
git add .
git commit -m "chore: finalize core banking refactoring project"
```

- [ ] **Step 12: Crear tag final.**

```bash
git tag v2-refactor
```

**Criterio de terminado:** proyecto completo, ejecutable, documentado y defendible.

---

# 3. Matriz de dependencias

```text
Task 1
  ↓
Task 2
  ↓
Task 3
  ↓
Task 4
  ↓
Task 5
  ↓
Task 6
  ↓
Task 7
  ↓
Task 8
  ↓
Task 9
  ↓
Task 10  ───────────── Legacy congelado
  ↓
Task 11
  ↓
Task 12 Strategy
  ↓
Task 13 State
  ↓
Task 14 Chain of Responsibility
  ↓
Task 15 Template Method
  ↓
Task 16 Observer
  ↓
Task 17
  ↓
Task 18
  ↓
Task 19
  ↓
Task 20
  ↓
Task 21
  ↓
Task 22
  ↓
Task 23
  ├── Task 24
  └── Task 25
       ↓
Tasks 26–30
       ↓
Task 31
```

---

# 4. Archivos finales esperados

```text
entregas/jeison_cabarcas/proyecto/
├── legacy/
│   ├── src/main/java/com/unimagdalena/corebanking/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/
│   ├── src/test/
│   ├── Dockerfile
│   ├── compose.yaml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── refactor/
│   ├── src/main/java/com/unimagdalena/corebanking/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── service/
│   │   └── pattern/
│   │       ├── strategy/
│   │       ├── state/
│   │       ├── chain/
│   │       ├── template/
│   │       └── observer/
│   ├── src/main/resources/
│   ├── src/test/
│   ├── Dockerfile
│   ├── compose.yaml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
├── docs/
│   ├── api/core-banking.http
│   ├── refactor/
│   │   ├── diagnostico.md
│   │   └── matriz-antes-despues.md
│   ├── README-REFACTORIZACION.md
│   └── SUSTENTACION.md
│
├── .gitignore
├── README.md
├── requerimientos.md
├── diseño.md
└── tareas.md
```

---

# 5. Checklist final contra los 350 puntos

## Diagnóstico — 60

- [ ] 5 problemas concretos.
- [ ] Archivo y línea.
- [ ] Smell/SOLID.
- [ ] Consecuencia real.
- [ ] Evidencia legacy congelada.

## Patrones — 100

- [ ] Strategy correcto.
- [ ] State correcto.
- [ ] Chain of Responsibility correcto.
- [ ] Template Method correcto.
- [ ] Observer correcto.
- [ ] Cada uno resuelve su diagnóstico.

## Justificación — 50

- [ ] Por qué se eligió cada patrón.
- [ ] Alternativa descartada.
- [ ] Relación con SOLID.
- [ ] Trade-off.

## Calidad — 60

- [ ] Build legacy.
- [ ] Build refactor.
- [ ] Tests.
- [ ] Docker.
- [ ] API reproducible.
- [ ] Código legible.
- [ ] Sin nuevas violaciones evidentes.

## Sustentación — 80

- [ ] Guion 10–15 minutos.
- [ ] Explicación sin leer README.
- [ ] Preguntas improvisadas preparadas.
- [ ] Decisiones defendibles.
- [ ] Mejoras futuras identificadas.

---

# 6. Regla final para el agente implementador

El agente no debe optimizar el plan reduciendo etapas críticas.

En particular, queda prohibido:

```text
crear refactor antes de congelar legacy;
inventar números de línea;
modificar legacy para que parezca peor después de congelarlo;
agregar patrones distintos para aumentar la cantidad;
cambiar endpoints entre versiones sin justificarlo;
sustituir una prueba real por una afirmación documental;
marcar una tarea como completada sin ejecutar su verificación.
```

La implementación termina únicamente después de completar `Task 31`.
