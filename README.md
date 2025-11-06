# Proyecto: Juegos de Azar - Práctica 3 🎲♠️♥️♣️♦️

Este repositorio contiene el desarrollo de la **Práctica 3** de la asignatura *Herramientas Informáticas para Juegos de Azar*, centrada en la **toma de decisiones en el póker Texas Hold’em**.  
El proyecto está desarrollado en **Java**, bajo el patrón **MVC (Modelo–Vista–Controlador)**, e implementa una interfaz gráfica en **Swing**.

---

## 📂 Estructura general del proyecto

```bash
HJA/
├── src/
│   └── tp3/
│       ├── gui/      # Interfaz gráfica (Swing)
│       ├── logic/    # Lógica del juego (equity, decisiones, evaluador, etc.)
│       ├── model/    # Estado del juego (jugadores, manos, board, fases)
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

## 🧱 Arquitectura (MVC)

### 🖥️ Vista (`tp3.gui`)

* `PokerEquityGUI`: ventana principal del juego, con tablero, jugadores y control de fases.
* `HeroPanel`: controles del jugador principal (rango, porcentaje, equity mínimo).
* `PlayerPanel`: muestra nombre, cartas, equity y acción (Bet, Call, Fold).
* `StatusBar`: muestra información contextual (fase, cartas restantes, acciones).
* `UiTheme` y `CardImages`: definen los colores, tipografía y carga de imágenes.

### ⚙️ Lógica (`tp3.logic`)

* `PokerHandEvaluator`: evalúa la fuerza de manos de 7 cartas.
* `RealEquityCalculator`: calcula la **equity real** mediante simulación Monte Carlo.
* `RangeParser` y `RankingProvider`: interpretan rangos y rankings por porcentaje.
* `DecisionEngine`: nuevo módulo para determinar acciones (Bet, Call, Fold) en base a equity mínima.
* `RoundManager`: gestiona las fases del juego y las decisiones automáticas.
* `OutsCalculator`: calcula **outs medios** contra el rango de un rival.

### 📊 Modelo (`tp3.model`)

* `Hand`: representa una mano de dos cartas.
* `Board`: representa las cartas comunes (flop, turn, river).
* `GameState`: estado general del juego (jugadores activos, board y fase).
* `Phase`: enum con las fases (`PREFLOP`, `FLOP`, `TURN`, `RIVER`).
* `CardValidator`: valida el formato de las cartas.

---

## ⚙️ Funcionamiento general

1. **Inicialización (Deal):**
   - Se reparten las manos de los jugadores.
   - Se genera el board de forma aleatoria o manual.
   - Se calcula la **equity inicial** de cada jugador.

2. **Simulación de fases:**
   - Cada fase (Flop, Turn, River) actualiza el board y recalcula el equity.
   - El estado del mazo y los jugadores se sincroniza con `GameState`.

3. **Toma de decisiones (Novedad en Práctica 3):**
   - Cada jugador tiene un **rango** y un **equity mínimo (EM)**.
   - Si la mano está dentro del rango y el equity ≥ EM → **Bet/Call**.
   - Si no cumple las condiciones → **Fold**.
   - En el **Turn**, se calcula la media de outs contra el rango rival para decidir.

4. **Actualización visual:**
   - La GUI refleja automáticamente las acciones y el estado del juego.

---

## ⚙️ Compilación y ejecución

**Compilación manual:**
```bash
javac -d bin -sourcepath src src/tp3/gui/PokerEquityGUI.java
```

**Ejecución:**
```bash
java -cp bin tp3.gui.PokerEquityGUI
```

**Recursos:**
Asegúrate de copiar las imágenes de cartas:
```bash
xcopy resources\cartas bin\cartas /E /I /Y
```

---

## 🧠 Próximos pasos

* 🧮 Implementar el cálculo de outs medios (Turn vs rango).
* 🧠 Ampliar la toma de decisiones con factores de riesgo y pot odds.
* 🧪 Crear tests unitarios con JUnit 5.
* 💾 Guardar configuraciones de usuario (rango, equity mínima).
* 🚀 Optimizar el simulador con hilos (multithreading).

---

## 📘 Entorno de desarrollo

* **Lenguaje:** Java 17  
* **Entorno:** Eclipse IDE  
* **Gestor de versiones:** Git + GitHub  
* **Flujo de trabajo:** `feature → dev → main`  
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

## ✉️ Nota final

> Este proyecto corresponde a la **Práctica 3**, centrada en la automatización de decisiones y simulación de estrategias en Texas Hold’em.  
> La base de cálculo de equity se hereda de la práctica anterior, pero el foco actual está en la toma de decisiones y la integración de la inteligencia básica de juego.
