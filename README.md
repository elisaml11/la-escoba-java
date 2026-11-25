# La Escoba — Motor de Juego y Persistencia en Java

Implementación completa de la lógica del juego de cartas **La Escoba** (baraja española de 40 cartas) desarrollada en Java siguiendo la metodología **Test-Driven Development (TDD)**, con soporte para partidas multijugador, persistencia relacional y control de calidad continuo.

> **Contexto:** Proyecto desarrollado en equipo para la asignatura *Tecnologías para el Desarrollo de Software* (Grado en Ingeniería Informática — Universidad de Valladolid).  
> **Autoras:** Elisa Martínez Lafuente y Marta Pérez Alonso.

---

## Tecnologías y Calidad de Software

* **Lenguaje y Build:** Java, Maven
* **Persistencia:** Hibernate ORM, HSQLDB
* **Testing:** JUnit 5, Mocks (pruebas en aislamiento), DBUnit (pruebas de integración con base de datos)
* **Calidad y CI/CD:** JaCoCo (cobertura de código), SonarQube (análisis estático), GitLab CI/CD

---

## Arquitectura del Proyecto

El sistema está diseñado bajo principios **SOLID** con una clara separación entre dominio, servicios, adaptadores y persistencia:

* **`uva.tds.entidades` (Dominio):** Modelado de la baraja española (`Carta`, `Palo`, `Baraja`), estado de jugadores (`Jugador`, `Mano`) y jerarquía de partida (`Partida` → `Ronda` → `Turno` → `Jugada`), además de `ResumenPartida` y `Sala` multijugador.
* **`uva.tds.servicios` (Lógica de negocio y persistencia):**
  * `GestorPartida`, `GestorRonda` y `GestorTurno`: Control del flujo de juego, reglas de captura y cálculo de puntuaciones.
  * `ServicioPartida`: Validación de reglas de negocio, estadísticas y persistencia desacoplada mediante el patrón **Repository** (`RepositorioPartidaHibernate`).
  * `ServicioJuegoMultijugador`: Coordinación de salas, repartos y validación de jugadas remotas.
* **`uva.tds.ejecutores` (Adaptadores):** Deserialización y ejecución de partidas pregrabadas desde JSON (`LectorPartidaJSON`, `AdaptadorCartaJson`, `EjecutarPartida`).

---

## Testing y Refactorización

* **Suite de pruebas integral:** Tests unitarios de dominio, pruebas en aislamiento de servicios mediante mocks, pruebas de integración de base de datos con `DBUnit` (`dataset-partidas.xml`) y tests específicos etiquetados (`@Tag("Cobertura")`) orientados a maximizar la cobertura de ramas.
* **Refactorizaciones aplicadas (Catálogo de Martin Fowler):**
  * *Replace Array with Objects:* Sustitución de estructuras multidimensionales complejas por entidades explícitas (`Ronda`, `Turno`, `Jugada`).
  * *Replace Error Code with Exception:* Validación robusta en `ServicioPartida` mediante excepciones específicas.
  * *Extract Method & Dead Code Elimination:* Reducción de complejidad ciclomática y resolución de *code smells* detectados por SonarQube.

---

## Estructura de Directorios

```text
├── src/
│   ├── main/java/uva/tds/
│   │   ├── ejecutores/     # Lectura y ejecución de partidas desde JSON
│   │   ├── entidades/      # Modelo de dominio (Baraja, Carta, Partida, Ronda, Turno...)
│   │   ├── interfaces/     # Contratos de Repositorio y Servicio Multijugador
│   │   └── servicios/      # Lógica de juego, multijugador e implementación Hibernate
│   └── test/
│       ├── java/uva/tds/   # Tests unitarios, en aislamiento y de integración (DBUnit)
│       └── resources/      # Datasets XML y partidas JSON de prueba
└── pom.xml