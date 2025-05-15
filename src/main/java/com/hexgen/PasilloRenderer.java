package com.hexgen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PasilloRenderer {
  private static final int SQUARE_SIZE = 40;
  private static double mainProbability = 0.70;
  private static double secondProbability = 0.15;
  private static double terthProbability = 0.05;

  public static void drawPasilloDecoracion(Graphics2D g2, int cx, int cy, int radius, int layers,
      boolean flatTop) {
    int entradaLado = 0; // fijo, por ejemplo lado 0
    Random rnd = new Random();

    Set<Integer> ladosConPuerta = new HashSet<>();

    List<Integer> posiblesSalidas = new ArrayList<>(List.of(1, 2, 3, 4, 5));
    Collections.shuffle(posiblesSalidas);

    Map<Integer, Color> colorPorLado = new HashMap<>();

    // dibujar entrada
    ladosConPuerta.add(entradaLado);

    double angleRadEntrada = getAngleRadians(entradaLado, flatTop);
    Point2D pEntrada = puntoCentralDelLado(cx, cy, radius, layers, entradaLado, flatTop);
    drawAlignedSquare(g2, pEntrada.getX(), pEntrada.getY(), angleRadEntrada, Color.BLUE);
    colorPorLado.put(entradaLado, Color.BLUE);

    if (rnd.nextDouble() < mainProbability) {
      int ladoFrente = (entradaLado + 3) % 6;
      colorPorLado.put(ladoFrente, Color.GREEN);
      ladosConPuerta.add(ladoFrente);
      posiblesSalidas.remove(ladoFrente);
      double angleRadSalida = getAngleRadians(ladoFrente, flatTop);
      Point2D pSalida = puntoCentralDelLado(cx, cy, radius, layers, ladoFrente, flatTop);
      drawAlignedSquare(g2, pSalida.getX(), pSalida.getY(), angleRadSalida, Color.GREEN);
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
      int salidaLado = posiblesSalidas.remove(0);
      if (ok) {
        ladosConPuerta.add(salidaLado);
        colorPorLado.put(salidaLado, Color.RED);

        double angleRadSalida = getAngleRadians(salidaLado, flatTop);
        Point2D pSalida = puntoCentralDelLado(cx, cy, radius, layers, salidaLado, flatTop);
        drawAlignedSquare(g2, pSalida.getX(), pSalida.getY(), angleRadSalida, Color.RED);

      }
    }

    for (int lado = 0; lado < 6; lado++) {
      if (!ladosConPuerta.contains(lado)) {
        colorPorLado.put(lado, Color.BLACK);
        double angle = getAngleRadians(lado, flatTop);
        Point2D p = puntoCentralDelLado(cx, cy, radius, layers, lado, flatTop);
        drawAlignedSquare(g2, p.getX(), p.getY(), angle, Color.BLACK);
      }
    }

    pintaTransiciones(g2, colorPorLado, cx, cy, radius, layers, flatTop);
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

  private static Point2D puntoCentralDelLado(int cx, int cy, int radius, int layers, int lado, boolean flatTop) {
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
}
