# Proyecto: Juegos de Azar 🎲♠️♥️♣️♦️

Este repositorio contiene el desarrollo del **calculador de equity de poker** realizado en Java, estructurado bajo el patrón **MVC (Modelo-Vista-Controlador)** con una interfaz gráfica en **Swing**.  
Su objetivo es ofrecer una aplicación visual que permita simular partidas de Texas Hold’em, mostrando las cartas de los jugadores y las comunes, y calculando las probabilidades de ganar (equity) de cada mano.

---

## 📂 Estructura general del proyecto

```bash
Practica2/
├── src/
│   └── tp2/
│       ├── app/      # Punto de entrada del programa
│       ├── gui/      # Interfaz gráfica (Swing)
│       ├── logic/    # Lógica del juego y cálculo de equity
│       ├── model/    # (Reservado) Modelos de datos de la partida
│       └── parse/    # Parsers de rangos y utilidades
│
├── resources/
│   └── cartas/       # Imágenes PNG de las cartas
│
├── bin/              # Archivos compilados (.class)
├── .gitignore
├── .project / .classpath
└── README.md
````

---

## 🌿 Estructura de ramas

| Rama              | Propósito                                                       | Estado         |
| ----------------- | --------------------------------------------------------------- | -------------- |
| `main`            | Rama principal estable. Contiene la versión limpia y funcional. | ✅ Estable      |
| `dev`             | Rama de desarrollo general. Recibe los PR desde `feature/*`.    | ⚙️ Activa      |
| `feature/rodri`   | Rama personal de Rodrigo (desarrollo de lógica y mejoras GUI).  | 🧱 En progreso |
| `feature/alberto` | Rama personal de Alberto.                                       | 🧱 En progreso |
| `feature/pablo`   | Rama personal de Pablo.                                         | 🧱 En progreso |
| `feature/antonio` | Rama personal de Antonio.                                       | 🧱 En progreso |

---

## 🧭 Flujo de trabajo

1. Cada desarrollador trabaja en su rama `feature/<nombre>`.
2. Cuando una funcionalidad esté lista, se hace un **Pull Request hacia `dev`**.
3. Se revisa el código y, una vez validado, se fusiona.
4. Solo las versiones totalmente estables pasan de `dev` → `main`.

> 💡 Esto evita conflictos y asegura que `main` siempre esté limpia y funcional.

---

## 🧱 Arquitectura (MVC)

### 🖥️ Vista (`tp2.gui`)

Contiene todos los componentes gráficos en Swing:

* `PokerEquityGUI`: ventana principal, botones y tablero.
* `PlayerPanel`: representa visualmente a cada jugador.
* `HeroPanel`: controles de rango y opciones del jugador.
* `CardsPanel` (clase interna): dibuja las cartas del jugador.

### ⚙️ Lógica (`tp2.logic`)

Incluye los algoritmos y componentes de cálculo:

* `RankingProvider`: interfaz para rankings de manos.
* `EquityCalculator`: interfaz para cálculos de equity.
* Implementaciones dummy de prueba (`DummyRankingProvider`, `DummyEquityCalculator`).

### 🧩 Parser (`tp2.parse`)

Traduce los rangos de manos a estructuras manejables:

* `RangeParser`: interfaz base.
* `SimpleRangeParser`: implementación inicial (separa por comas).

### 📊 Modelo (`tp2.model`)

Reservado para representar:

* Manos, tablero, estadísticas y estados de la partida.

### 🚀 Aplicación (`tp2.app`)

* `MainApp`: punto de entrada, lanza la GUI con `SwingUtilities.invokeLater`.

---

## 🧮 Estado actual

| Componente        | Estado             | Detalles                                            |
| ----------------- | ------------------ | --------------------------------------------------- |
| GUI general       | ✅ Completa         | Todos los paneles y botones funcionales.            |
| Carga de cartas   | ✅ Corregida        | Carga de imágenes vía `getResource("/cartas/...")`. |
| Parser de rangos  | ⚙️ Prototipo       | Soporta formato simple (`AA, KK, AKo`).             |
| Cálculo de equity | ⏳ Pendiente        | Devuelve valores dummy (por implementar).           |
| Modelo de datos   | 🕓 Por implementar | Se añadirá en futuras iteraciones.                  |
| Tests unitarios   | ❌ No implementadas | Se integrarán cuando se implemente la lógica real.  |

---

## 💡 Próximos pasos

* Implementar el cálculo real de equity (probabilidad de victoria).
* Extender el `RangeParser` para soportar notaciones avanzadas (`AKs+, 77+`, etc.).
* Añadir un **StatusBar** en la GUI.
* Crear modelos de datos (`Hand`, `Board`, etc.).
* Añadir pruebas automáticas con **JUnit**.
* Mejorar documentación técnica del flujo GUI → Lógica → Modelo.

---

## ⚙️ Entorno de desarrollo

* **Entorno:** Eclipse IDE (Java SE 17)
* **Compilación:** Proyecto Java simple (sin Maven/Gradle)
* **Recursos:** `/src/resources/cartas/` (imágenes cargadas con `getResource`)
* **Control de versiones:** Git + GitHub (flujo main/dev/feature)
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
> A partir de ahora, **`main` contiene la versión estable del proyecto**.
> Toda nueva funcionalidad se desarrollará en las ramas personales y se integrará en `dev`.
> Solo versiones revisadas y funcionales se fusionarán en `main`. 🚀
