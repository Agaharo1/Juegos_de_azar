# Proyecto: Juegos de Azar 🎲♠️♥️♣️♦️

Este proyecto forma parte de la asignatura **Herramientas Informáticas para los Juegos de Azar**.  
El objetivo es crear un programa en **Java** capaz de **evaluar manos de póker** y resolver distintos apartados prácticos.

---

## 🚀 ¿Qué hace este proyecto?
- Lee una **entrada de texto** que contiene jugadas de póker.
- Evalúa cada mano según las reglas del póker (parejas, tríos, escaleras, etc.).
- Genera un **archivo de salida** con los resultados obtenidos.
- Incluye una interfaz de **línea de comandos** (se ejecuta desde la terminal).

---

## 📂 Estructura básica del proyecto
- **`/src/poker`** → Aquí está el código fuente en Java (clases como `Main`, `Mano`, `Carta`, etc.).
- **`entrada.txt`** → Archivo de ejemplo con jugadas de póker.
- **`salida.txt`** → Archivo generado automáticamente con el resultado.
- **`README.md`** → Este archivo de explicación.

---

## ▶️ ¿Cómo se ejecuta?
1. Compila el proyecto en Java:
   ```bash
   javac -d bin src/poker/*.java
