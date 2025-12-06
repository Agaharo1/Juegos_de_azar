# Poker Equity Calculator Juegos de Azar ♠️♥️♣️♦️

**Texas Hold’em Decision Simulator** – Java + Swing + MVC

Este repositorio contiene el desarrollo completo de la **Práctica 3** de la asignatura *Herramientas Informáticas para Juegos de Azar*, centrada en:

* el **cálculo de equity** en Texas Hold’em,
* la **toma de decisiones automática** basada en rangos y equity mínima,
* la simulación de acciones durante una mano,
* y el análisis de decisión en el **Turn (outs medios)**.

El proyecto está implementado en **Java 17**, con arquitectura **MVC** y una interfaz gráfica en **Swing**, incluyendo un evaluador de manos completo, un motor de simulación Monte Carlo y un sistema gráfico interactivo para 6 jugadores.

---

# 🧭 Índice

1. [Características principales](#-características-principales)
2. [Arquitectura del proyecto (MVC)](#-arquitectura-del-proyecto-mvc)
3. [Estructura del repositorio](#-estructura-del-repositorio)
4. [Funcionamiento del sistema](#-funcionamiento-del-sistema)
5. [Cálculo de equity (Internals)](#-cálculo-de-equity-internals)
6. [Rangos, EM y decisiones automáticas](#-rangos-em-y-decisiones-automáticas)
7. [Decisión en el Turn – Outs medios](#-decisión-en-el-turn--outs-medios)
8. [Compilación y ejecución](#-compilación-y-ejecución)
10. [Autoría](#-autoría)

---

# ⭐ Características principales

✔️ Simulación de mesa **6-max** con interfaz gráfica interactiva

✔️ Cálculo de **equity real** mediante Monte Carlo o **PokerStove** si está disponible

✔️ Sistema de **rangos textuales** y **rangos por porcentaje**

✔️ Evaluador completo de manos de Texas Hold’em

✔️ Motor de apuestas simplificado (Bet, Call, Fold)

✔️ Sistema de **decisión automática** basado en RG + EM

✔️ Editor manual de board (Flop/Turn/River)

✔️ Modo análisis para el **Turn (outs medios)**

✔️ Interfaz moderna con tema oscuro y cartas gráficas

---

# 🧱 Arquitectura del proyecto (MVC)

## 🖥️ Vista — `p3.gui`

* **PokerEquityGUI** → Ventana principal
  Dibuja la mesa, gestiona eventos, botones, players, board y fases.

* **PlayerPanel**
  Muestra cartas, equity, RG/EM, botones de edición, y modo manual.

* **HeroPanel**
  Rango textual, rango por porcentaje, random board/cards, etc.

* **TurnDecisionDialog**
  Análisis del apartado 2.2: outs medios + CALL/FOLD.

* **BoardEditorDialog**
  Editor manual del flop/turn/river.

* **CardImages** + **UiTheme**
  Carga de imágenes PNG + paleta de colores + tipografías UI.

---

## ⚙️ Lógica — `p3.logic`

* **PokerHandEvaluator**
  Evalúa una mano de 7 cartas mediante combinaciones de 5.
  Soporta:

  * Escaleras, flush, full, póker, straight flush, wheel…

* **RealEquityCalculator**
  Monte Carlo mejorado:

  * Completa manos/board desconocidos.
  * Evalúa todas las manos en cada simulación.
  * Reparte victorias y empates.

* **PokerStoveAdapter**
  Llama a `ps-eval.exe` si existe; si falla → fallback automático a Monte Carlo.

* **RangeParser**
  Interpreta rangos como:

  ```
  JJ+, ATs-A2s, 76o+, QQ-AA, AKs, AQo
  ```

* **RankingProvider**
  Ranking estándar de 169 manos Sklansky-like:

  * `getTopByPercent(25)` → top 25%

* **TurnDecisionLogic**
  Calcula outs medios vs rango y decide CALL/FOLD.

---

## 📊 Modelo — `p3.model`

* **GameState**
  Mantiene:

  * manos de los 6 jugadores
  * board
  * fase (PREFLOP/FLOP/TURN/RIVER)
  * tracking de cartas usadas y foldeadas

* **BettingState**
  Sistema simplificado de apuestas:

  * currentBet
  * stacks
  * betsInRound
  * totalPot
  * acciones: `call`, `bet`, `check`, `fold`

* **Hand** / **Board** / **CardValidator**

---

# 📂 Estructura del repositorio

```bash
Práctica3/
├── src/
│   └── p3/
│       ├── gui/        # Interfaz gráfica
│       ├── logic/      # Evaluador, equity, rangos, decisiones
│       ├── model/      # Estado del juego y cartas
│
├── resources/
│   └── cartas/         # Ah.png, Kd.png, red_joker.png, etc.
│
├── bin/                # .class compilados
├── README.md
└── .gitignore
```

---

# 🔁 Funcionamiento del sistema

## 1. Deal

* Se crea un `Deck`.
* Se resetea `GameState` y `BettingState`.
* Se reparten manos (aleatorias salvo el héroe si se desactiva "Random Cards").

## 2. Establecer rangos (RG) y equity mínima (EM)

* Cada jugador debe tener RG + EM para activar la toma de decisiones.
* La GUI colorea en **verde** si la mano cumple RG/EM y **rojo** si no.

## 3. Cálculo automático de equity

El número de simulaciones depende de la fase:

| Fase    | Simulaciones     |
| ------- | ---------------- |
| Preflop | 100 000          |
| Flop    | 200 000          |
| Turn    | 300 000          |
| River   | 1 (determinista) |

## 4. Avance de fases (Flop → Turn → River)

Según modo:

* **Random Board**: se roban cartas automáticamente.
* **Manual**: se abre `BoardEditorDialog` para escribir las cartas.

## 5. Toma de decisiones automáticas

Cada turno:

* Si modo **Manual**, el jugador elige: `FOLD / CALL / BET`.
* Si modo **Auto**:

  * Si la mano **no** está en RG → **FOLD**.
  * Si equity < EM → **FOLD**.
  * Si equity ≥ EM:

    * Si debe igualar → **CALL**.
    * Si nadie apostó → **BET**.

---

# 🔢 Cálculo de equity (Internals)

`RealEquityCalculator`:

1. Construye un mazo sin cartas usadas.
2. Para cada simulación:

   * Completa board y manos desconocidas.
   * Evalúa con `PokerHandEvaluator.evaluate7`.
   * Identifica ganador(es).
3. Reparte puntuación (empates incluidos).
4. Devuelve equity de cada jugador en **%**.

Si existe `ps-eval.exe`, `PokerStoveAdapter` lo usa automáticamente.

---

# 🎯 Rangos, EM y decisiones automáticas

### Rangos textuales permitidos

```
JJ+
ATs-A2s
88-TT
KQo
T8s+
```

### Rangos por porcentaje

```
25   → top 25%
25%  → top 25%
```

### Validación automática

El sistema marca:

* **RG**: verde si la mano entra en el rango.
* **EM**: verde si equity ≥ EM.

---

# 🔍 Decisión en el Turn – Outs medios

Este apartado reproduce la lógica pedida en la práctica:

### Entrada:

* Mano del héroe
* Rango del villano
* Board de 4 cartas
* EM deseada

`TurnDecisionLogic`:

1. Expande el rango del villano a manos concretas.
2. Para cada mano del rival:

   * Recorre todos los **44 rivers posibles**.
   * Cuenta cuántos producen victoria o empate.
3. Calcula:

   * Outs medios
   * Equity aproximada
4. Devuelve **CALL/FOLD**.

`TurnDecisionDialog` muestra:

* Las cartas gráficamente
* Outs medios
* Equity
* Decisión en verde/rojo

---

# ⚙️ Compilación y ejecución

### Compilación:

```bash
javac -d bin -sourcepath src src/p3/gui/PokerEquityGUI.java
```

### Ejecución:

```bash
java -cp bin p3.gui.PokerEquityGUI
```

### Asegúrate de copiar imágenes:

```bash
xcopy resources\cartas bin\cartas /E /I /Y
```

---

# 👤 Autoría

**Rodrigo Mendoza García**

**Pablo Sánchez Lozano**

**Alberto Sáenz Pérez**

**Antonio García Rodrigo**

Grado en Ingeniería de Datos e IA – *Universidad Complutense de Madrid*

Proyecto desarrollado para la **Práctica – Juegos de Azar**, integrando:

* Evaluación de manos
* Cálculo de equity
* Simulación de rondas
* Decisiones automáticas
* Análisis de outs y estrategias simples
