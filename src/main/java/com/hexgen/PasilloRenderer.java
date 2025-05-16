package com.hexgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.hexgen.model.Loseta;

public class PasilloRenderer {
  private static double straitProbability = 0.70;
  private static double mainProbability = 0.70;
  private static double secondProbability = 0.15;
  private static double terthProbability = 0.05;

  public static void drawPasilloDecoracion(Loseta loseta) {
    int entradaLado = 0;
    int ladoFrente = -1;
    Set<Integer> puertas = new HashSet<>();
    
    Random rnd = new Random();

    Set<Integer> ladosConPuerta = new HashSet<>();

    List<String> posiblesSalidas = new ArrayList<>(List.of("1", "2", "3", "4", "5"));
    Collections.shuffle(posiblesSalidas);

    ladosConPuerta.add(entradaLado);

    if (rnd.nextDouble() < mainProbability) {
      ladoFrente = (entradaLado + 3) % 6;
      if( rnd.nextDouble() > straitProbability ) {
        ladoFrente = Integer.parseInt( posiblesSalidas.remove(0) );
      } else {
        posiblesSalidas.remove(ladoFrente);
      }
      ladosConPuerta.add(ladoFrente);
    }

    while (!posiblesSalidas.isEmpty()) {
      double num = rnd.nextDouble();
      double currentProb = 0;
      switch (ladosConPuerta.size()) {
        case 1:
          currentProb = mainProbability;
          break;
        case 2:
          currentProb = secondProbability;
          break;
        case 3:
          currentProb = terthProbability;
          break;
      };
      boolean ok = num < currentProb;
      int salidaLado = Integer.parseInt( posiblesSalidas.remove(0) );
      if (ok) {
        puertas.add( salidaLado );
        ladosConPuerta.add(salidaLado);
      }
    }
    loseta.setLadoEntrada( entradaLado );
    if( ladoFrente != -1 ) {
      loseta.setLadoAbierto( ladoFrente );
    }
    loseta.setLadosConPuertas( puertas );
  }
}
