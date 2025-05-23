package com.hexgen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PasilloRendererDecorator {
  private static final int SQUARE_SIZE = 40;
  private static double straitProbability = 0.70;
  private static double mainProbability = 0.70;
  private static double secondProbability = 0.15;
  private static double terthProbability = 0.05;

  public static void drawPasilloDecoracion(Graphics2D g2, int cx, int cy, int radius, int layers,
      boolean flatTop) {
    int entradaLado = 0; // fijo, por ejemplo lado 0
    Random rnd = new Random();

    Set<Integer> ladosConPuerta = new HashSet<>();

    List<String> posiblesSalidas = new ArrayList<>(List.of("1", "2", "3", "4", "5"));
    Collections.shuffle(posiblesSalidas);

    ladosConPuerta.add(entradaLado);

    if (rnd.nextDouble() < mainProbability) {
      int ladoFrente = (entradaLado + 3) % 6;
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
        ladosConPuerta.add(salidaLado);
      }
    }
  }

  private static void pintaTransiciones(Graphics2D g2, Map<Integer, Color> colorPorLado, int cx,
      int cy, int radius, int layers, boolean flatTop) {
    for (int i = 0; i < 6; i++) {
      int j = (i + 1) % 6;

      Point2D pi = puntoCentralDelLado(cx, cy, radius, layers, i, flatTop);
      Point2D pj = puntoCentralDelLado(cx, cy, radius, layers, j, flatTop);

      Color ci = colorPorLado.get(i);
      Color cj = colorPorLado.get(j);
      Color mixto = mezclar(ci, cj);

      g2.setColor(mixto);
      g2.setStroke(new BasicStroke(6));
      g2.drawLine((int) pi.getX(), (int) pi.getY(), (int) pj.getX(), (int) pj.getY());
    }
  }

  private static Point2D puntoCentralDelLado(int cx, int cy, int radius, int layers, int lado,
      boolean flatTop) {
    double angleDeg = flatTop ? (60 * lado + 30) : (60 * lado);
    double angleRad = Math.toRadians(angleDeg);

    double distance = radius * layers * Math.sqrt(3) / 2.0;
    double dx = distance * Math.cos(angleRad);
    double dy = distance * Math.sin(angleRad);

    return new Point2D.Double(cx + dx, cy + dy);
  }

  private static void drawAlignedSquare(Graphics2D g2, double cx, double cy, double angleRad,
      Color color) {
    Graphics2D g = (Graphics2D) g2.create(); // Clonar el contexto para no afectar el resto

    g.translate(cx, cy); // Mover origen al centro del cuadrado
    g.rotate(angleRad); // Girar el sistema de coordenadas
    g.setColor(color);
    g.fillRect(-SQUARE_SIZE / 2, -SQUARE_SIZE / 2, SQUARE_SIZE, SQUARE_SIZE);

    g.dispose();
  }

  private static double getAngleRadians(int lado, boolean flatTop) {
    double angleDeg = flatTop ? (60 * lado + 30) : (60 * lado);
    return Math.toRadians(angleDeg);
  }

  private static Color mezclar(Color c1, Color c2) {
    int r = (c1.getRed() + c2.getRed()) / 2;
    int g = (c1.getGreen() + c2.getGreen()) / 2;
    int b = (c1.getBlue() + c2.getBlue()) / 2;
    return new Color(r, g, b);
  }

  private static void pintarMuroOscuro(Graphics2D g2, int cx, int cy, int radius, int layers,
      int lado, boolean flatTop) {
//    int qOffset, rOffset;
//
//    // direcciones axial según orientación
//    if (flatTop) {
//      int[][] axialDirs = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};
//      qOffset = axialDirs[lado][0];
//      rOffset = axialDirs[lado][1];
//    } else {
//      int[][] axialDirs = {{0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}, {1, 0}};
//      qOffset = axialDirs[lado][0];
//      rOffset = axialDirs[lado][1];
//    }
//
//    // dibujamos 1 o 2 hexágonos desde el borde hacia fuera
//    for (int step = 0; step < 2; step++) {
//      int q = qOffset * (layers - 1 + step);
//      int r = rOffset * (layers - 1 + step);
//
//      Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
//      g2.setColor(new Color(50, 50, 50)); // oscuro
//      g2.fill( HexTilePanel.generateHexPath(true, cx + p.x, cy + p.y, radius, layers, flatTop));
//    }
  }
}
