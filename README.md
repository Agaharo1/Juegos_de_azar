# Proyecto: Juegos de Azar 🎲♠️♥️♣️♦️

Este repositorio contiene el desarrollo del **calculador de equity de poker** realizado en Java, estructurado bajo un patrón **MVC (Modelo-Vista-Controlador)** con una interfaz gráfica en **Swing**.  
Su objetivo es ofrecer una aplicación visual que permita simular partidas de Texas Hold’em, mostrando las cartas de los jugadores y las comunes, y calculando las probabilidades de ganar (equity) de cada mano.

---

## 📂 Estructura general del repositorio

```bash
Juegos_de_azar/
├── Practica1/           # Proyecto de la primera práctica
│   ├── GUI/             # Interfaz gráfica antigua (no modularizada)
│   ├── poker/           # Lógica básica inicial
│   ├── resources/cartas/ # Imágenes PNG de las cartas
│   ├── *.txt            # Ficheros de entrada y salida
│   └── (.classpath, .project) Archivos de Eclipse
│
├── Practica2/           # Proyecto actual (estructura MVC moderna)
│   ├── src/
│   │   ├── tp2.app/     # Punto de entrada de la aplicación
│   │   ├── tp2.gui/     # Componentes visuales (Swing)
│   │   ├── tp2.logic/   # Lógica del juego y cálculo de equity
│   │   ├── tp2.model/   # (Reservado) Modelos de datos de la partida
│   │   ├── tp2.parse/   # Parsers de rangos y utilidades
│   │   └── resources/cartas/ # Imágenes integradas como recursos
│   └── (.project, .classpath, .settings/)
│
├── .gitignore           # Ignora bin/, .settings/, .idea/, etc.
└── README.md            # Este documento
````

---

## 🌿 Estructura de ramas

| Rama                      | Propósito                                                                                    | Estado                      |
| ------------------------- | -------------------------------------------------------------------------------------------- | --------------------------- |
| `feature/estructura-base` | Versión actual funcional con estructura MVC, GUI completa y recursos cargados por classpath. | ✅ Activa                   |
| `dev`                     | Rama de desarrollo general. Recibe los PR desde `feature/*`.                                 | ⚙️ Estable                  |
| `main`                    | Versión inicial. Contiene errores y estructura obsoleta.                                     | ⚠️ Desactualizada – No usar |

> 🔒 *Actualmente `main` no se utiliza. El desarrollo se realiza en `feature/estructura-base` y `dev`.*

---

## 🧱 Arquitectura del proyecto (MVC)

### 🖥️ Vista (`tp2.gui`)

Contiene todos los componentes de la interfaz gráfica.
Implementado completamente con **Swing**.

| Clase                                             | Descripción                                                                                                                                                                        |
| ------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`PokerEquityGUI`**                              | Ventana principal. Contiene los paneles de jugadores, el tablero central y los botones (Deal, Flop, Turn, River, Reset). Gestiona los eventos del usuario y conecta con la lógica. |
| **`PlayerPanel`**                                 | Representa visualmente a un jugador. Muestra su nombre, cartas y porcentaje de equity. <br>📸 Carga las imágenes de las cartas mediante `getResource("/cartas/..png")`.            |
| **`HeroPanel`**                                   | Panel inferior con controles para seleccionar el rango de manos o porcentaje, ranking y opciones aleatorias. <br>Incluye campos de texto, spinners y botones.                      |
| **`CardsPanel`** *(clase interna de PlayerPanel)* | Dibuja las cartas del jugador, superpuestas con antialiasing y escalado dinámico.                                                                                                  |

---

### ⚙️ Lógica (`tp2.logic`)

Encapsula los cálculos y algoritmos del programa.
Actualmente incluye **implementaciones dummy (provisionales)**, pensadas para conectar con la GUI sin errores de compilación.

| Clase                       | Descripción                                                                                                                                 |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| **`RankingProvider`**       | Interfaz que define métodos para obtener rankings de manos y posiciones.                                                                    |
| **`DummyRankingProvider`**  | Implementación simple con una lista fija de manos (AA, KK, QQ...).                                                                          |
| **`EquityCalculator`**      | Interfaz para el cálculo de la equity dado un conjunto de cartas.                                                                           |
| **`DummyEquityCalculator`** | Implementación temporal que devuelve 0.0 para todas las manos. <br>🔜 *Pendiente de conectar con la lógica real de cálculo probabilístico.* |

---

### 🧩 Parser (`tp2.parse`)

Se encarga de traducir los rangos de manos de texto a estructuras manejables.

| Clase                   | Descripción                                                                                                                |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **`RangeParser`**       | Interfaz genérica para parsear expresiones de rangos y convertirlas en conjuntos o porcentajes.                            |
| **`SimpleRangeParser`** | Versión básica: separa por comas y devuelve los tokens normalizados. <br>Ejemplo: `"AA, KK, AKo"` → `["AA", "KK", "AKo"]`. |

---

### 📊 Modelo (`tp2.model`)

Actualmente vacío, reservado para futuras clases que representen:

* Manos de los jugadores
* Tablero (flop, turn, river)
* Estado de la partida y estadísticas

---

### 🚀 Aplicación (`tp2.app`)

Contiene el **punto de entrada** del programa.

| Clase         | Descripción                                                                                |
| ------------- | ------------------------------------------------------------------------------------------ |
| **`MainApp`** | Método `main` que lanza la GUI mediante `SwingUtilities.invokeLater(PokerEquityGUI::new)`. |

---

## 🧮 Estado actual del desarrollo

| Componente        | Estado             | Detalles                                                                                |
| ----------------- | ------------------ | --------------------------------------------------------------------------------------- |
| GUI general       | ✅ Completa         | Todos los botones, jugadores y paneles funcionan.                                       |
| Carga de cartas   | ✅ Corregida        | Las imágenes se cargan mediante `getResource` desde `src/resources/cartas/`.            |
| Parser de rangos  | ⚙️ Prototipo       | Implementación básica con expresiones separadas por comas.                              |
| Cálculo de equity | ⏳ Pendiente        | Actualmente devuelve 0.0. Se conectará con la lógica estadística en próximas versiones. |
| RankingProvider   | ✅ Dummy operativo  | Retorna una lista estática de manos predefinidas.                                       |
| Modelo de datos   | 🕓 Por implementar | Pendiente de crear clases `Hand`, `Board`, etc.                                         |
| Pruebas unitarias | ❌ No implementadas | Se planearán cuando se integre la lógica real.                                          |

---

## 💡 Pendiente de realizar

### 🔜 Próximos pasos

* Implementar el **cálculo real de equity** (probabilidad de victoria) combinando simulaciones Monte Carlo o tablas precalculadas.
* Extender `RangeParser` para soportar notación avanzada (`AKs+, 77+, A2s-A5s`, etc.).
* Añadir un **StatusBar** en la GUI para mostrar mensajes (rango parseado, errores, etc.).
* Añadir tests automáticos (`JUnit`) para validar la lógica.
* Integrar persistencia opcional (guardar configuraciones del usuario).
* Documentar el flujo de datos entre GUI → Parser → Lógica.

---

## ⚙️ Configuración de desarrollo

* **Entorno:** Eclipse IDE (Java SE 17)
* **Compilación:** Proyecto Java simple (sin Maven ni Gradle)
* **Recursos:** Las imágenes de cartas están en `/src/resources/cartas/` y se cargan con:

  ```java
  getClass().getResource("/cartas/" + code + ".png");
  ```
* **.gitignore:**

  ```gitignore
  # IDEs
  .idea/ .vscode/ *.iml
  # Eclipse
  .project
  .classpath
  .settings/
  # Builds
  bin/ out/ target/ build/
  # Sistema
  .DS_Store
  Thumbs.db
  ```

---

## 🧾 Resumen visual de paquetes

| Paquete            | Rol                 | Contenido principal                                                                    |
| ------------------ | ------------------- | -------------------------------------------------------------------------------------- |
| `tp2.app`          | Aplicación          | `MainApp.java`                                                                         |
| `tp2.gui`          | Vista (Swing)       | `PokerEquityGUI`, `PlayerPanel`, `HeroPanel`                                           |
| `tp2.logic`        | Lógica y simulación | `RankingProvider`, `DummyRankingProvider`, `EquityCalculator`, `DummyEquityCalculator` |
| `tp2.parse`        | Parsing de rangos   | `RangeParser`, `SimpleRangeParser`                                                     |
| `tp2.model`        | Modelo (vacío)      | Reservado                                                                              |
| `resources.cartas` | Recursos gráficos   | Imágenes PNG de las cartas                                                             |

---

## 🧠 Nota final

> 💬 **Importante:**
> La rama `main` contiene una versión antigua y errónea del proyecto subida antes de reestructurar el código.
> Todo el desarrollo actual se encuentra en `feature/estructura-base` (en curso) y se integra progresivamente a `dev`.
>
> Se recomienda ignorar la rama `main` y trabajar exclusivamente en `dev`.
