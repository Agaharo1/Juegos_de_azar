# Proyecto: Juegos de Azar 🎲♠️♥️♣️♦️

Este repositorio contiene el desarrollo del **calculador de equity de póker Texas Hold’em** realizado en **Java**, estructurado bajo el patrón **MVC (Modelo-Vista-Controlador)** con una interfaz gráfica en **Swing**.
Su objetivo es ofrecer una aplicación visual que simule partidas de Texas Hold’em, mostrando las cartas de los jugadores y del board, y calculando las **probabilidades reales de ganar (equity)** mediante **simulación Monte Carlo**.

---

## 📂 Estructura general del proyecto

```bash
Practica2/
├── src/
│   └── tp2/
│       ├── app/      # Punto de entrada del programa
│       ├── gui/      # Interfaz gráfica (Swing)
│       ├── logic/    # Lógica de cálculo (Deck, EquityCalculator, Evaluador, etc.)
│       ├── model/    # Representación del estado del juego (Manos, Board, GameState)
│       └── parse/    # Parsers y utilidades de notación de rangos
│
├── resources/
│   └── cartas/       # Imágenes PNG de las cartas (Ah.png, Kd.png, etc.)
│
├── bin/              # Archivos compilados (.class)
├── .gitignore
├── .project / .classpath
└── README.md
```

---

## 🌿 Estructura de ramas

| Rama              | Propósito                                                         | Estado         |
| ----------------- | ----------------------------------------------------------------- | -------------- |
| `main`            | Rama principal estable. Contiene la versión funcional y validada. | ✅ Estable      |
| `dev`             | Rama de integración y pruebas de nuevas características.          | ⚙️ Activa      |
| `feature/rodri`   | Rama personal de Rodrigo (controlador, GUI, y equity real).       | 🧱 En progreso |
| `feature/alberto` | Rama personal de Alberto.                                         | 🧱 En progreso |
| `feature/pablo`   | Rama personal de Pablo.                                           | 🧱 En progreso |
| `feature/antonio` | Rama personal de Antonio.                                         | 🧱 En progreso |

---

## 🧭 Flujo de trabajo

1. Cada desarrollador trabaja en su rama `feature/<nombre>`.
2. Al terminar una funcionalidad, se hace **Pull Request → `dev`**.
3. El código se revisa y se fusiona tras las pruebas.
4. Solo las versiones 100 % estables pasan de `dev` a `main`.

> 💡 Esto garantiza que `main` siempre mantenga una versión funcional y libre de conflictos.

---

## 🧱 Arquitectura (MVC)

### 🖥️ Vista (`tp2.gui`)

Componentes gráficos implementados en **Swing**:

* `PokerEquityGUI`: ventana principal y controlador de eventos.
* `HeroPanel`: controles del jugador principal (rango, porcentaje, opciones).
* `PlayerPanel`: visualización de cada jugador con sus cartas y equity.
* `StatusBar`: barra inferior que muestra información contextual (fase, cartas restantes).
* `UiTheme`: define colores y tipografías.
* `CardImages`: gestiona la carga de imágenes de las cartas (caché interna).

### ⚙️ Lógica (`tp2.logic`)

Contiene toda la lógica funcional del juego y el cálculo de equity:

* `Deck`: gestión del mazo y extracción de cartas únicas.
* `RangeParser`: parser básico de notación de rangos (ej. `AA,KK,AKs,AQo`).
* `RankingProvider`: lista ordenada de manos para rankings por porcentaje.
* `PokerHandEvaluator`: evaluador de manos de 7 cartas (determinístico).
* `RealEquityCalculator`: simulador Monte Carlo que estima la probabilidad de victoria real.
* `EquityCalculator`: versión dummy anterior (se mantiene como referencia).

### 📊 Modelo (`tp2.model`)

Representa el estado de la partida:

* `Hand`: mano de dos cartas.
* `Board`: cartas comunes (flop, turn, river).
* `GameState`: estado completo de la partida (jugadores, board y fase).
* `CardValidator`: valida códigos de carta (`Ah`, `Td`, etc.).

### 🧩 Parser (`tp2.parse`)

* `RangeParser`: traduce cadenas de rango a listas de manos legibles por el programa.
  (ej. `"AA,KK,AKs"` → `["AA","KK","AKs"]`).

### 🚀 Aplicación (`tp2.app`)

* `MainApp`: punto de entrada de la aplicación, lanza `PokerEquityGUI` mediante `SwingUtilities.invokeLater`.

---

## 🧮 Estado actual del proyecto

| Componente             | Estado         | Descripción breve                                           |
| ---------------------- | -------------- | ----------------------------------------------------------- |
| GUI principal          | ✅ Completa     | Interfaz 100 % funcional, responsive y temática.            |
| `HeroPanel`            | ✅ Completa     | Selector de rango, porcentaje y checkboxes de aleatoriedad. |
| `StatusBar`            | ✅ Nueva        | Muestra fase actual y cartas restantes del mazo.            |
| Parser de rangos       | ⚙️ Prototipo   | Soporta notación básica (`AA,KK,AKs,AQo`).                  |
| Evaluador de manos     | ✅ Nuevo        | Evalúa fuerza real de 7 cartas (flush, straight, etc.).     |
| Cálculo de equity real | ✅ Implementado | Basado en simulación Monte Carlo.                           |
| Modelo de datos        | ✅ Integrado    | Incluye `Hand`, `Board`, `GameState`, `Phase`.              |
| Tests unitarios        | 🚧 Pendiente   | Se implementarán tras estabilizar la lógica.                |

---

## ⚙️ Funcionamiento del cálculo de equity (Monte Carlo)

El **RealEquityCalculator** estima las probabilidades reales de victoria:

1. Construye el mazo restante (52 cartas menos las usadas).
2. Completa las cartas desconocidas (manos o board) al azar.
3. Evalúa las 7 cartas de cada jugador con `PokerHandEvaluator`.
4. Suma victorias y empates (½ punto cada uno).
5. Repite el proceso *N* veces y normaliza los resultados.

> ⚖️ Cuantas más simulaciones (`trials`), mayor precisión.
> Por defecto:
>
> * PREFLOP → 5 000 simulaciones
> * FLOP → 15 000
> * TURN → 30 000
> * RIVER → evaluación determinista (1 simulación)

---

## 💡 Próximos pasos

* 🔧 Añadir selector de precisión (Low / Med / High) en la GUI.
* 🧮 Optimizar Monte Carlo con paralelismo (multi-thread).
* 🧠 Extender `RangeParser` con notación avanzada (`AKs+`, `77+`, `A2s-A5s`).
* 🧪 Implementar tests unitarios con **JUnit 5**.
* 📊 Añadir estadísticas adicionales (equity media, varianza).
* 💾 Guardar/recuperar configuraciones del usuario (último rango, modo, etc.).

---

## ⚙️ Entorno de desarrollo

* **Lenguaje:** Java 17
* **Entorno:** Eclipse IDE
* **Compilación:**

  ```bash
  javac -d bin -sourcepath src src/tp2/gui/PokerEquityGUI.java
  java -cp bin tp2.gui.PokerEquityGUI
  ```
* **Recursos:** `resources/cartas/` (deben copiarse a `bin/cartas` para ejecución)

  ```bash
  xcopy resources\cartas bin\cartas /E /I /Y
  ```
* **Control de versiones:** Git + GitHub
* **Flujo:** `feature → dev → main`
* **.gitignore:**

  ```
  .idea/ .vscode/ *.iml
  .project
  .classpath
  .settings/
  bin/ out/ target/ build/
  .DS_Store
  Thumbs.db
  ```

---

## 🧠 Nota final

> 💬 **Importante:**
> La versión actual en `main` implementa el **cálculo real de equity** con simulación Monte Carlo, el **evaluador completo de manos de 7 cartas** y una interfaz totalmente funcional.
> Las ramas `feature/*` se utilizarán para ampliar funcionalidades y optimizaciones futuras. 🚀
