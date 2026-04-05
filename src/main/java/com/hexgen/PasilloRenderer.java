package com.hexgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.hexgen.model.Loseta;

public class PasilloRenderer {

  private static final Random rnd = new Random();

  // Probabilidades de puertas (rojo) en lados no especiales
  private static final double PROB_PUERTA_PRIMER_EXTRA = 0.15;
  private static final double PROB_PUERTA_SEGUNDO_EXTRA = 0.05;

  public static void drawPasilloDecoracion(Loseta loseta) {
    int entradaLado = 0;
    int numAzules = determinarNumAzules(loseta.getTileType());

    Set<Integer> ladosAbiertos = new HashSet<>();
    Set<Integer> puertas = new HashSet<>();

    List<Integer> ladosDisponibles = new ArrayList<>(List.of(1, 2, 3, 4, 5));
    Collections.shuffle(ladosDisponibles);

    // Para pasillos, preferir el lado de enfrente (lado 3, opuesto a entrada 0)
    if (numAzules == 1 && esPasillo(loseta.getTileType())) {
      if (rnd.nextDouble() < 0.70) {
        ladosAbiertos.add(3);
        ladosDisponibles.remove(Integer.valueOf(3));
      } else {
        ladosAbiertos.add(ladosDisponibles.remove(0));
      }
    } else {
      for (int i = 0; i < numAzules && !ladosDisponibles.isEmpty(); i++) {
        ladosAbiertos.add(ladosDisponibles.remove(0));
      }
    }

    // Añadir puertas (rojo) en algunos lados restantes
    int puertasExtra = 0;
    for (int lado : ladosDisponibles) {
      double prob = puertasExtra == 0 ? PROB_PUERTA_PRIMER_EXTRA : PROB_PUERTA_SEGUNDO_EXTRA;
      if (rnd.nextDouble() < prob) {
        puertas.add(lado);
        puertasExtra++;
      }
    }

    loseta.setLadoEntrada(entradaLado);
    loseta.setLadosAbiertos(ladosAbiertos);
    loseta.setLadosConPuertas(puertas);
  }

  private static boolean esPasillo(String tileType) {
    return "Pasillos".equals(tileType) || "Pasillos angostos".equals(tileType);
  }

  private static int determinarNumAzules(String tileType) {
    return switch (tileType) {
      case "Pasillos angostos" -> 0;
      case "Pasillos" -> rnd.nextDouble() < 0.70 ? 1 : 0;
      case "Salas pequeñas" -> rnd.nextDouble() < 0.60 ? 1 : 2;
      case "Salas grandes" -> rnd.nextDouble() < 0.60 ? 2 : 3;
      case "Salas muy grandes" -> {
        double r = rnd.nextDouble();
        yield r < 0.20 ? 2 : (r < 0.70 ? 3 : 4);
      }
      default -> rnd.nextDouble() < 0.70 ? 1 : 0;
    };
  }
}
