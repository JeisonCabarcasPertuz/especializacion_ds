# Requerimientos — Proyecto Integrador de Diseño Orientado a Objetos

> Estado: **Borrador final para aprobación — Fase 1 de 3**
>
> Este documento debe quedar aprobado antes de crear `diseño.md`.
> `diseño.md` deberá satisfacer todos los requisitos aquí definidos y `tareas.md`
> deberá derivarse posteriormente del diseño aprobado.

## 1. Propósito

Construir una API REST académica que simule un **Core Bancario** y permita demostrar,
de forma verificable y defendible, la refactorización de un código inicial hacia una
solución orientada a objetos con principios SOLID, código limpio y al menos cinco
patrones de diseño GoF.

El proyecto debe estar optimizado para la rúbrica definida en:

`docs/rubricas/proyecto.md`

La rúbrica es la fuente de verdad principal para cualquier decisión relacionada con
evaluación, alcance académico o evidencia del antes/después.

## 2. Orden de precedencia de las especificaciones

En caso de contradicción, se debe aplicar el siguiente orden:

1. `docs/rubricas/proyecto.md`
2. `requerimientos.md`
3. `diseño.md`
4. `tareas.md`
5. Código fuente y documentación generada

No se debe iniciar `diseño.md` hasta que `requerimientos.md` haya sido revisado,
refinado y aprobado.

No se debe iniciar `tareas.md` hasta que `diseño.md` haya sido revisado, refinado y
aprobado.

## 3. Objetivo académico

El proyecto debe permitir demostrar claramente:

- El estado original del código.
- Al menos cuatro problemas concretos del código original.
- La ubicación exacta de cada problema mediante `archivo:línea`.
- El principio SOLID o code smell relacionado con cada problema.
- La selección de al menos cinco patrones GoF.
- El problema concreto que resuelve cada patrón.
- Por qué se eligió cada patrón y por qué no se escogió una alternativa razonable.
- La implementación correcta de los roles de cada patrón.
- La diferencia observable entre el código antes y después de la refactorización.
- Que la versión refactorizada ejecuta correctamente.
- Que existen pruebas o ejemplos reproducibles.
- Que las decisiones pueden sustentarse oralmente sin depender de leer el README.

## 4. Alcance funcional del Core Bancario

La solución será una API REST sin frontend.

### RF-01. Gestión de clientes

El sistema debe permitir:

- Crear un cliente.
- Consultar un cliente por identificador.
- Listar clientes.

Datos mínimos:

- Identificador.
- Tipo y número de documento.
- Nombre.
- Correo electrónico.

No se requiere implementar autenticación, autorización, KYC real ni integración con
servicios externos de identidad.

### RF-02. Gestión de cuentas bancarias

El sistema debe permitir:

- Crear una cuenta asociada a un cliente.
- Consultar una cuenta.
- Listar las cuentas de un cliente.
- Consultar su saldo.
- Cambiar su estado mediante las operaciones permitidas por el dominio.

Tipos mínimos de cuenta:

- `SAVINGS`
- `CHECKING`

Estados mínimos:

- `ACTIVE`
- `BLOCKED`
- `CLOSED`

Las reglas de comportamiento asociadas al estado no deben quedar dispersas en
controladores mediante condicionales repetitivos.

### RF-03. Consignación

El sistema debe permitir consignar dinero en una cuenta válida.

Reglas mínimas:

- Monto mayor que cero.
- Cuenta existente.
- Estado de cuenta compatible con la operación.
- Actualización consistente del saldo.
- Registro de la transacción.

### RF-04. Retiro

El sistema debe permitir retirar dinero de una cuenta válida.

Reglas mínimas:

- Monto mayor que cero.
- Cuenta existente.
- Estado de cuenta compatible con la operación.
- Saldo suficiente.
- Aplicación de las políticas o costos que correspondan.
- Actualización consistente del saldo.
- Registro de la transacción.

### RF-05. Transferencia interna

El sistema debe permitir transferir dinero entre dos cuentas del mismo Core Bancario.

Reglas mínimas:

- Cuenta origen existente.
- Cuenta destino existente.
- Las cuentas origen y destino deben ser diferentes.
- Monto mayor que cero.
- Estado válido de ambas cuentas.
- Saldo suficiente en la cuenta origen.
- Aplicación de políticas o costos definidos.
- Débito y crédito ejecutados de forma atómica.
- Registro de la transacción.

Si la operación falla, no debe persistirse una transferencia parcial.

### RF-06. Historial de transacciones

El sistema debe permitir consultar las transacciones de una cuenta.

Tipos mínimos:

- `DEPOSIT`
- `WITHDRAWAL`
- `TRANSFER`

Cada transacción debe conservar como mínimo:

- Identificador.
- Tipo.
- Monto.
- Fecha y hora.
- Cuenta origen cuando aplique.
- Cuenta destino cuando aplique.
- Estado de la transacción.
- Valor de costo o comisión cuando aplique.

### RF-07. Políticas bancarias simples

El sistema debe contener reglas suficientemente distintas para justificar el uso de
polimorfismo y patrones de diseño.

Como mínimo deben existir variaciones de comportamiento por tipo de cuenta u
operación, por ejemplo:

- Cálculo de comisión.
- Límites de operación.
- Reglas de retiro o transferencia.

Los valores usados serán académicos y deterministas; no representan tarifas bancarias
reales.

### RF-08. Validaciones de negocio desacopladas

Las validaciones de una operación bancaria no deben concentrarse en un único método
con múltiples `if`, `switch` o responsabilidades no relacionadas.

La solución refactorizada debe permitir:

- Añadir una nueva validación sin reescribir toda la operación.
- Probar validaciones relevantes de manera aislada.
- Identificar claramente cuál validación rechazó una operación.

### RF-09. Eventos posteriores a una transacción

Una transacción exitosa debe permitir ejecutar acciones posteriores sin acoplarlas
directamente al flujo principal.

Debe existir al menos una acción observable, por ejemplo:

- Registro de auditoría.
- Registro técnico de notificación simulada.

No se requiere enviar correos, SMS ni consumir proveedores reales.

## 5. Estrategia obligatoria de “antes” y “después”

El proyecto debe contener **dos implementaciones completas y separadas** del mismo
Core Bancario:

- `legacy/`: representa el código **antes** de la refactorización.
- `refactor/`: representa el código **después** de la refactorización.

La comparación entre ambas implementaciones constituye una parte central de la
entrega y no debe depender únicamente del historial de Git.

### REFA-01. Dos versiones ejecutables

Tanto `legacy/` como `refactor/` deben ser proyectos Spring Boot independientes,
compilables y ejecutables.

La versión `legacy` debe funcionar. Sus problemas deben ser principalmente de diseño,
mantenibilidad, acoplamiento, extensibilidad o incumplimiento de principios SOLID.

No se deben introducir errores absurdos, código deliberadamente roto o malas
prácticas artificiales únicamente para justificar la posterior aplicación de
patrones.

La versión `refactor` debe conservar el comportamiento funcional esperado mientras
mejora el diseño interno.

### REFA-02. Paridad funcional

`legacy/` y `refactor/` deben implementar el mismo alcance funcional definido en este
documento.

Como regla general deben conservar:

- Los mismos casos de uso.
- Los mismos endpoints públicos.
- Los mismos métodos HTTP.
- Los mismos datos principales de entrada y salida.
- Las mismas reglas funcionales del Core Bancario.
- Un modelo de persistencia conceptualmente equivalente.
- Los mismos escenarios principales de demostración.

El objetivo es que el profesor pueda observar que **el comportamiento del sistema se
mantiene mientras cambia la calidad del diseño**.

Cualquier diferencia funcional entre ambas versiones deberá documentarse y
justificarse expresamente.

### REFA-03. Paridad estructural

Las dos versiones deben mantener una estructura técnica equivalente para facilitar la
comparación.

Como mínimo ambas deben contener:

```text
legacy/ o refactor/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── Dockerfile
├── compose.yaml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

Dentro del código Java, ambas versiones deben partir de la misma separación base:

- `controller`
- `dto`
- `service`
- `service/interfaces`
- `repository`
- `entity` o `model`
- `exception`

La versión `refactor` podrá incorporar clases o subpaquetes adicionales necesarios
para representar correctamente los patrones, por ejemplo:

- `strategy`
- `state`
- `validator`
- `observer`

Estas extensiones no se consideran una ruptura de la paridad estructural siempre que
la organización base y los casos de uso sigan siendo comparables.

### REFA-04. Legacy como evidencia canónica del “antes”

La carpeta:

`entregas/jeison_cabarcas/proyecto/legacy`

será la fuente de verdad del código anterior.

Por esta razón no será necesario duplicar el código legacy dentro de
`docs/refactor/before/`.

Todo diagnóstico deberá citar directamente archivos reales de esta carpeta, por
ejemplo:

```text
legacy/src/main/java/.../TransactionService.java:45-78
```

### REFA-05. Refactor como evidencia canónica del “después”

La carpeta:

`entregas/jeison_cabarcas/proyecto/refactor`

será la fuente de verdad de la solución refactorizada.

Cada problema diagnosticado en `legacy` deberá poder relacionarse con las clases o
componentes de `refactor` que lo solucionan.

### REFA-06. Transparencia académica

Si la versión `legacy` se crea específicamente como punto de partida de esta
actividad y no proviene de un proyecto histórico realizado con anterioridad, debe
describirse de forma transparente como:

> versión inicial del Core Bancario construida para demostrar el proceso de
> diagnóstico y refactorización del proyecto integrador.

No debe presentarse como código histórico previo si realmente no lo fue.

### REFA-07. Historial Git recomendado

Además de conservar físicamente las dos carpetas, se recomienda mantener hitos Git
claros:

- Commit o tag inicial recomendado: `v1-legacy`.
- Commit o tag final recomendado: `v2-refactor`.

El historial Git es evidencia complementaria. Las carpetas `legacy/` y `refactor/`
seguirán siendo la evidencia principal para la evaluación.

### REFA-08. Diagnóstico mínimo

Se deben documentar al menos **cuatro problemas concretos y distintos** de
`legacy/`.

Para cada problema debe registrarse:

| Campo | Obligatorio |
|---|---|
| ID del problema | Sí |
| Archivo de `legacy` | Sí |
| Línea o rango de líneas | Sí |
| Fragmento o descripción concreta | Sí |
| Code smell | Sí |
| Principio SOLID afectado, cuando aplique | Sí |
| Consecuencia del problema | Sí |
| Patrón que lo resuelve | Sí |
| Archivo o clases equivalentes en `refactor` | Sí |
| Evidencia después de refactorizar | Sí |
| Prueba o forma de verificar | Sí |

Aunque la rúbrica exige al menos cuatro problemas para alcanzar el nivel superior del
diagnóstico, el objetivo interno del proyecto será documentar **cinco o más problemas
concretos**, de forma que cada uno de los cinco patrones tenga una causa real,
independiente y trazable.

### REFA-09. Matriz de trazabilidad

Debe existir una matriz explícita con la siguiente relación:

`problema legacy -> code smell/SOLID -> patrón -> clases refactor -> prueba -> evidencia`

No se considerará suficiente indicar solamente que “se aplicó un patrón”.

### REFA-10. Comparación controlada

Para que el antes y el después sean académicamente comparables:

- No se debe eliminar funcionalidad de `refactor` para simplificar la solución.
- No se debe agregar funcionalidad importante únicamente a `refactor` si no es
  necesaria para demostrar una mejora de diseño.
- Cuando sea posible, los mismos requests de demostración deben ejecutarse contra las
  dos versiones.
- Las diferencias relevantes deben explicarse desde diseño, mantenibilidad,
  extensibilidad, testabilidad o aplicación de SOLID.


## 6. Patrones de diseño

### PAT-01. Cantidad

La solución final debe implementar **exactamente cinco patrones principales GoF** que
sean fáciles de explicar y que resuelvan problemas reales del dominio.

Se evita añadir patrones innecesarios únicamente para aumentar la cantidad.

### PAT-02. Candidatos preferidos

Los siguientes patrones son los candidatos iniciales por su claridad académica y
aplicabilidad al Core Bancario:

1. **Strategy**
   - Candidato para políticas variables de comisión o reglas por operación/cuenta.

2. **State**
   - Candidato para comportamiento dependiente del estado de la cuenta.

3. **Chain of Responsibility**
   - Candidato para la cadena de validaciones de una transacción.

4. **Template Method**
   - Candidato para el flujo común de procesamiento de operaciones bancarias con
     pasos especializados.

5. **Observer**
   - Candidato para acciones posteriores a una transacción, como auditoría o
     notificación simulada.

La selección se considera **propuesta**, no diseño definitivo. `diseño.md` deberá
confirmar que cada patrón corrige un problema real del baseline y que no existe una
alternativa más simple que haga innecesario el patrón.

### PAT-03. Implementación real

Cada patrón debe mostrar claramente sus roles.

No se debe contar como patrón principal:

- Un `switch` renombrado como Factory.
- Una anotación de framework sin estructura justificable.
- El scope singleton por defecto de Spring como implementación del patrón Singleton.
- `@Builder` de Lombok como única evidencia de Builder.
- Una clase con nombre de patrón cuya estructura no corresponda al patrón GoF.

### PAT-04. Justificación comparativa

Para cada patrón debe documentarse:

- Problema original.
- Por qué aplica.
- Qué alternativa razonable se evaluó.
- Por qué se descartó esa alternativa.
- Qué principio SOLID mejora.
- Coste o trade-off introducido por el patrón.

## 7. Requisitos técnicos

### RT-01. Lenguaje

- Java 21.

### RT-02. Framework

- Spring Boot estable.
- Versión fijada para esta especificación: **4.1.1**.
- No usar versiones milestone, release candidate o snapshot.

Si la implementación se ejecuta mucho después de la aprobación de estas
especificaciones, una actualización de Spring Boot debe hacerse únicamente si sigue
siendo compatible con Java 21 y no altera los objetivos académicos.

### RT-03. Build

- Maven.
- El proyecto debe compilarse con Maven Wrapper (`mvnw` / `mvnw.cmd`) para reducir
  dependencias locales del evaluador.

### RT-04. Lombok

Usar Lombok para reducir boilerplate donde sea conveniente.

Lombok no debe ocultar decisiones importantes del dominio ni reemplazar la
implementación académica de un patrón de diseño.

### RT-05. Persistencia

- Spring Data JPA.
- PostgreSQL como base de datos principal.
- Entidades JPA separadas de los DTO expuestos por la API.

### RT-06. Migraciones

Usar Flyway para crear un esquema reproducible.

No depender de cambios manuales sobre la base de datos para levantar el proyecto.

### RT-07. Precisión monetaria

Todo valor monetario debe usar `BigDecimal`.

No usar `float` ni `double` para saldos, montos o comisiones.

La moneda del proyecto se limitará a `COP` para evitar complejidad de conversión de
divisas fuera del alcance académico.

### RT-08. Fechas

Usar tipos de `java.time`.

Los timestamps persistidos deben manejarse de manera consistente y documentada.

### RT-09. Identificadores

Usar UUID para los identificadores principales expuestos por la API, salvo que en
`diseño.md` se documente una razón concreta para otra estrategia.

## 8. Arquitectura exigida a nivel de requisitos

La arquitectura detallada se definirá en `diseño.md`, pero la implementación final
debe respetar una estructura MVC académica con separación mínima de responsabilidades.

Paquetes o carpetas esperadas:

- `controller`
- `dto`
- `service`
- `service/interfaces`
- `repository`
- `entity` o `model`
- `exception`

Los patrones de diseño podrán requerir paquetes adicionales específicos, por ejemplo:

- `strategy`
- `state`
- `validator`
- `observer`

Estos paquetes solo deben añadirse si mejoran la comprensión.

### ARQ-01. Controller

Los controllers deben:

- Recibir solicitudes HTTP.
- Validar estructura básica de entrada.
- Delegar casos de uso al servicio.
- Convertir el resultado en respuesta HTTP.

No deben contener lógica bancaria.

### ARQ-02. Service

Los services deben coordinar reglas y operaciones de negocio.

Las dependencias relevantes deben expresarse mediante interfaces cuando ello aporte
desacoplamiento y testabilidad.

### ARQ-03. Repository

Los repositories deben encargarse únicamente del acceso a datos mediante JPA.

Los controllers no deben acceder directamente a repositories.

### ARQ-04. DTO

Los contratos HTTP deben usar DTO.

No se deben exponer entidades JPA directamente desde los controllers.

### ARQ-05. Manejo de errores

Debe existir manejo centralizado mediante `@ControllerAdvice` o mecanismo equivalente.

Los errores de dominio deben producir respuestas HTTP consistentes y entendibles.

### ARQ-06. Transacciones

Las operaciones que modifiquen saldo, especialmente transferencias, deben ejecutarse
dentro de límites transaccionales explícitos.

## 9. Código limpio y SOLID

### CC-01. Principios

La versión final debe favorecer:

- SRP.
- OCP.
- LSP.
- ISP.
- DIP.

No es obligatorio forzar los cinco principios en cada clase; sí es obligatorio evitar
violaciones evidentes creadas durante la refactorización.

### CC-02. Nombres

Clases, métodos y variables deben tener nombres técnicos y expresivos.

Evitar nombres genéricos como:

- `Manager`
- `Helper`
- `Util`

cuando oculten responsabilidades de dominio.

### CC-03. Tamaño y responsabilidades

Evitar:

- God Classes.
- Métodos extensos.
- Lógica duplicada.
- Condicionales repetidos por tipo o estado.
- Dependencias concretas innecesarias.
- Lógica de negocio en controllers o repositories.

### CC-04. Comentarios

Los comentarios deben ser breves, técnicos y explicar principalmente el **por qué**.

No comentar código evidente.

No usar emojis en comentarios, documentación técnica generada por el agente ni logs.

### CC-05. Inyección de dependencias

Preferir inyección por constructor.

Evitar `@Autowired` sobre atributos.

## 10. Validación de entrada y reglas de dominio

Usar Bean Validation cuando corresponda para validaciones estructurales como:

- Campos obligatorios.
- Formato de correo.
- Montos positivos.

Las reglas que dependen del estado del dominio deben vivir fuera del controller y
ser probables de forma aislada.

## 11. Pruebas

El proyecto no busca cobertura exhaustiva.

Debe cubrir componentes esenciales y evidenciar que la refactorización funciona.

### TEST-01. Pruebas unitarias mínimas

Debe existir como mínimo:

- Prueba de la política implementada mediante Strategy.
- Prueba de transiciones/comportamiento relevante de State.
- Prueba de la cadena principal de Chain of Responsibility.
- Prueba del flujo común de Template Method.
- Prueba del comportamiento de Observer.
- Prueba del servicio de transferencia para un caso exitoso.
- Prueba del servicio de transferencia para saldo insuficiente.

### TEST-02. Prueba de integración mínima

Debe existir al menos una prueba que compruebe un flujo crítico desde servicio y
persistencia, o un endpoint crítico mediante el contexto de Spring.

### TEST-03. Calidad

Las pruebas deben validar comportamiento, no únicamente aumentar cobertura.

No se establecerá un porcentaje de cobertura obligatorio.

## 12. Ejecución con Docker

Las dos implementaciones deben poder ejecutarse mediante Docker desde:

`entregas/jeison_cabarcas/proyecto`

La ejecución debe permitir demostrar tanto el sistema original como el refactorizado
sin requerir una instalación local de PostgreSQL.

### DOCKER-01. Docker en ambas versiones

Tanto `legacy/` como `refactor/` deben contener como mínimo:

- `Dockerfile`
- `compose.yaml` o `docker-compose.yml`
- Contenedor de la aplicación.
- Contenedor PostgreSQL.
- Variables de entorno documentadas.
- Healthcheck cuando sea razonable.

Las configuraciones deben ser equivalentes siempre que sea posible para que las
diferencias observadas correspondan al código y no al entorno de ejecución.

### DOCKER-02. Ejecución independiente

El README principal debe documentar un comando equivalente para cada versión:

```bash
cd legacy
docker compose up --build
```

y:

```bash
cd refactor
docker compose up --build
```

Cada versión debe poder levantarse y probarse de forma independiente.

### DOCKER-03. Demostración comparativa

`diseño.md` deberá definir si ambas versiones podrán levantarse simultáneamente usando
puertos distintos.

Si se habilita esta modalidad, deberá evitarse cualquier conflicto entre:

- Puertos HTTP.
- Puertos PostgreSQL.
- Nombres de contenedores.
- Volúmenes.
- Redes.

La ejecución simultánea será conveniente para la sustentación, pero no es obligatoria
si aumenta innecesariamente la complejidad.

### DOCKER-04. Limpieza reproducible

El README debe indicar cómo detener cada versión y, cuando sea necesario, eliminar sus
volúmenes para reiniciar la demostración desde cero.


## 13. Documentación obligatoria

### DOC-01. README principal

Crear en la raíz:

`README.md`

Debe incluir:

- Objetivo académico.
- Explicación de `legacy` y `refactor`.
- Tecnologías.
- Prerrequisitos.
- Cómo ejecutar `legacy` con Docker.
- Cómo ejecutar `refactor` con Docker.
- Cómo detener y limpiar cada ambiente.
- Cómo ejecutar pruebas.
- Endpoints principales.
- Ejemplos mínimos de requests.
- Cómo acceder a datos de prueba cuando existan.
- Estructura general del proyecto.
- Ruta recomendada para realizar la demostración antes/después.

### DOC-02. Documento de arquitectura y refactorización

Crear un documento Markdown adicional, recomendado:

`docs/README-REFACTORIZACION.md`

Debe incluir:

- Contexto.
- Arquitectura común de las dos versiones.
- Problemas encontrados en `legacy`.
- Matriz antes/después.
- Los cinco patrones.
- Roles de cada patrón.
- Problema que resuelve cada patrón.
- Alternativa considerada.
- Relación con SOLID.
- Trade-offs.
- Diagrama de clases UML del legacy cuando ayude a visualizar el problema.
- Diagrama de clases UML del refactor.
- Diagrama del flujo de una transferencia.
- Evidencias concretas de refactorización.
- Referencias cruzadas entre archivos equivalentes de `legacy` y `refactor`.

Los diagramas deben escribirse como código Mermaid o PlantUML para que sean
versionables.

### DOC-03. Diagnóstico

Crear:

`docs/refactor/diagnostico.md`

Debe citar ubicaciones exactas del código anterior en formato:

```text
legacy/<ruta-del-archivo>:línea-inicial-línea-final
```

Cada diagnóstico deberá indicar además dónde se encuentra la solución correspondiente
en `refactor`.

### DOC-04. Matriz de comparación

Crear:

`docs/refactor/matriz-antes-despues.md`

Debe incluir como mínimo:

| Problema | Legacy | SOLID / smell | Patrón | Refactor | Prueba |
|---|---|---|---|---|---|

Esta matriz será uno de los elementos principales durante la sustentación.

### DOC-05. Guía de sustentación

Crear:

`docs/SUSTENTACION.md`

Debe preparar una sustentación de 10 a 15 minutos y contener:

- Orden recomendado de exposición.
- Qué mostrar primero en `legacy`.
- Qué mostrar después en `refactor`.
- Qué problema resolvió cada patrón.
- Preguntas difíciles probables.
- Respuestas razonadas.
- Alternativas descartadas.
- Trade-offs.
- Qué se mejoraría con más tiempo.

Este documento es una guía de estudio. La explicación oral no debe depender de leerlo.

### DOC-06. Requests reproducibles

Crear un archivo como:

`docs/api/core-banking.http`

con un flujo demostrable:

1. Crear cliente.
2. Crear dos cuentas.
3. Consignar.
4. Transferir.
5. Consultar saldos.
6. Consultar historial.
7. Ejecutar al menos un caso inválido.

Cuando sea técnicamente viable, el mismo archivo debe permitir apuntar a `legacy` o
`refactor` cambiando únicamente una variable de URL base.


## 14. Trazabilidad con la rúbrica

La solución debe incluir una matriz que permita verificar los objetivos de evaluación:

| Dimensión de rúbrica | Evidencia requerida |
|---|---|
| Diagnóstico del antes | `legacy/` + `docs/refactor/diagnostico.md` + `archivo:línea` |
| 5 patrones | `refactor/` + UML + pruebas |
| Justificación | Matriz antes/después + alternativas + relación SOLID |
| Calidad | Build exitoso de `refactor` + Docker + pruebas + requests reproducibles |
| Sustentación | Comparación `legacy`/`refactor` + `docs/SUSTENTACION.md` + demo de 10-15 min |


## 15. Estructura física obligatoria

Todo el contenido generado para esta entrega debe ubicarse bajo:

`entregas/jeison_cabarcas/proyecto`

No modificar entregas de otros estudiantes ni carpetas externas salvo la consulta de
la rúbrica, que debe tratarse como fuente de solo lectura.

La estructura objetivo de alto nivel será:

```text
entregas/jeison_cabarcas/proyecto/
├── legacy/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │       └── java/
│   ├── Dockerfile
│   ├── compose.yaml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── refactor/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │       └── java/
│   ├── Dockerfile
│   ├── compose.yaml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── docs/
│   ├── api/
│   │   └── core-banking.http
│   ├── refactor/
│   │   ├── diagnostico.md
│   │   └── matriz-antes-despues.md
│   ├── README-REFACTORIZACION.md
│   └── SUSTENTACION.md
├── README.md
├── requerimientos.md
├── diseño.md          # solo después de aprobar requerimientos
└── tareas.md          # solo después de aprobar diseño
```

`legacy/` y `refactor/` deben mantener la misma estructura base y un alcance funcional
equivalente.

La ubicación exacta de paquetes Java, nombres de clases, endpoints y componentes de
cada patrón se definirá en `diseño.md`.


## 16. Fuera de alcance

Para evitar sobreingeniería, no se requiere:

- Frontend.
- Autenticación o autorización.
- OAuth2/JWT.
- Microservicios.
- Mensajería Kafka/RabbitMQ.
- Redis.
- Kubernetes.
- Integraciones bancarias reales.
- Conversión multimoneda.
- Intereses bancarios reales.
- Créditos.
- Tarjetas.
- Pagos PSE.
- Contabilidad de doble partida completa.
- CI/CD complejo.

Cualquier elemento adicional deberá justificar un beneficio directo frente a la
rúbrica.

## 17. Criterios de aceptación global

El proyecto se considerará satisfactorio cuando:

1. `legacy/` y `refactor/` compilen y ejecuten con Java 21.
2. Ambas versiones puedan levantar su aplicación y PostgreSQL mediante Docker Compose.
3. `legacy/` y `refactor/` sean proyectos completos, comparables y ejecutables.
4. Ambas versiones mantengan paridad funcional y estructura base equivalente.
5. Documente al menos cuatro problemas concretos de `legacy` con `archivo:línea`.
6. Mantenga trazabilidad entre cinco problemas de `legacy` y cinco patrones de `refactor`.
7. Implemente correctamente cinco patrones GoF.
8. Cada patrón tenga una justificación comparativa.
9. La versión refactorizada evite nuevas violaciones SOLID evidentes.
10. Las operaciones principales del Core Bancario funcionen en ambas versiones.
11. Las transferencias sean atómicas.
12. Los componentes esenciales de `refactor` tengan pruebas.
13. Exista un flujo reproducible que permita comparar `legacy` y `refactor`.
14. Existan diagramas que expliquen arquitectura y refactorización.
15. Exista una guía de sustentación.
16. La documentación permita a un tercero levantar, comparar y entender ambas
    versiones sin ayuda del autor.

## 18. Restricciones para un agente IA implementador

El agente que posteriormente implemente el proyecto debe:

1. Leer primero `docs/rubricas/proyecto.md`.
2. Leer después `requerimientos.md`.
3. Leer después `diseño.md`.
4. Ejecutar las tareas en el orden definido por `tareas.md`.
5. No sustituir silenciosamente tecnologías o patrones aprobados.
6. No implementar funcionalidad fuera de alcance salvo que sea imprescindible.
7. Mantener el proyecto ejecutable después de cada bloque relevante de trabajo.
8. Ejecutar las pruebas correspondientes antes de declarar una tarea terminada.
9. No inventar evidencia de ejecución.
10. No afirmar que Docker, Maven o las pruebas funcionan sin haberlos ejecutado.
11. Mantener comentarios breves y técnicos.
12. Preservar `legacy/` como evidencia inmutable del antes una vez aprobado su diagnóstico.
13. Implementar las mejoras únicamente en `refactor/` después de congelar el legacy.
14. No corregir silenciosamente en `legacy/` los problemas ya usados como evidencia.

## 19. Decisiones que deberán cerrarse en `diseño.md`

Una vez aprobado este documento, la fase de diseño deberá definir como mínimo:

- Modelo de dominio y relaciones JPA.
- Endpoints concretos.
- Estructura exacta de paquetes.
- Contratos DTO.
- Errores y códigos HTTP.
- Flujo transaccional.
- Mapeo definitivo problema -> patrón.
- Roles concretos de los cinco patrones.
- UML.
- Modelo de persistencia.
- Datos iniciales.
- Estrategia exacta de pruebas.
- Estrategia de Docker.
- Secuencia de construcción y congelamiento de `legacy` y posterior evolución en `refactor`.
- Estrategia para mantener paridad funcional y estructural entre ambas versiones.
- Puertos y estrategia de ejecución Docker de `legacy` y `refactor`.
