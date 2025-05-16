package com.hexgen;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class HexUtils {

  public static double getAngleRadians(int lado, boolean flatTop) {
    double angleDeg = flatTop ? (60 * lado + 30) : (60 * lado);
    return Math.toRadians(angleDeg);
  }

  public static Point hexToPixel(int q, int r, int radius, boolean flatTop) {
    int x, y;
    if (flatTop) {
      x = (int) (radius * 3.0 / 2 * q);
      y = (int) (radius * Math.sqrt(3) * (r + q / 2.0));
    } else {
      x = (int) (radius * Math.sqrt(3) * (q + r / 2.0));
      y = (int) (radius * 3.0 / 2 * r);
    }
    return new Point(x, y);
  }

  public static Point2D puntoCentralDelLado(int cx, int cy, int radius, int layers, int lado,
      boolean flatTop) {
    // Ángulo en grados al centro del lado (a mitad de camino entre vértices)
    double angleDeg = flatTop ? (60 * lado + 30) : (60 * lado);
    double angleRad = Math.toRadians(angleDeg);

    // Distancia del centro del hexágono al centro de un lado (≠ vértice)
    double distance = radius * layers * Math.sqrt(3) / 2.0;

    double dx = distance * Math.cos(angleRad);
    double dy = distance * Math.sin(angleRad);

    return new Point2D.Double(cx + dx, cy + dy);
  }

  public static Path2D generateHexPath(boolean inner, double cx, double cy, double radius,
      long layers, boolean flatTop) {
    if (inner && layers % 2 != 0) {
      cx -= radius;
    } else if (inner) {
      cx += radius;
    }

    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(flatTop ? (60 * i) : (60 * i - 30));
      double dx = cx + radius * Math.cos(angle);
      double dy = cy + radius * Math.sin(angle);
      if (i == 0)
        hex.moveTo(dx, dy);
      else
        hex.lineTo(dx, dy);
    }
    hex.closePath();
    return hex;
  }

  public static void debugPath(Path2D path, String nombre) {
    System.out.println("\t\tPath2D: " + nombre);
    PathIterator it = path.getPathIterator(null);
    double[] coords = new double[6];

    while (!it.isDone()) {
      int type = it.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          System.out.printf("\t\t\tMOVETO   (%.2f, %.2f)%n", coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          System.out.printf("\t\t\tLINETO   (%.2f, %.2f)%n", coords[0], coords[1]);
          break;
        case PathIterator.SEG_CLOSE:
          System.out.println("\t\t\tCLOSE");
          break;
        default:
          System.out.println("\t\t\tOtro tipo de segmento");
      }
      it.next();
    }
  }

  public static List<Integer> getLadosContactadosConInterseccion(Path2D exterior, Path2D interior) {
    double margin = 2.0;
    List<Integer> lados = new ArrayList<>();
    Area interiorArea = new Area(interior);
    List<Point2D> exteriorVertices = extractVertices(exterior);

    for (int i = 0; i < 6; i++) {
      Point2D p1 = exteriorVertices.get(i);
      Point2D p2 = exteriorVertices.get((i + 1) % 6);

      // Vector perpendicular al lado, normalizado y escalado por el margen
      double dx = p2.getY() - p1.getY();
      double dy = p1.getX() - p2.getX();
      double len = Math.hypot(dx, dy);
      dx = dx / len * margin;
      dy = dy / len * margin;

      Path2D banda = new Path2D.Double();
      banda.moveTo(p1.getX() + dx, p1.getY() + dy);
      banda.lineTo(p2.getX() + dx, p2.getY() + dy);
      banda.lineTo(p2.getX() - dx, p2.getY() - dy);
      banda.lineTo(p1.getX() - dx, p1.getY() - dy);
      banda.closePath();

      Area bandaArea = new Area(banda);
      bandaArea.intersect(interiorArea);

      if (!bandaArea.isEmpty()) {
        lados.add(i);
      }
    }

    return lados;
  }


  private static List<Point2D> extractVertices(Path2D path) {
    List<Point2D> vertices = new ArrayList<>();
    PathIterator it = path.getPathIterator(null);
    double[] coords = new double[6];
    while (!it.isDone()) {
      int type = it.currentSegment(coords);
      if (type != PathIterator.SEG_CLOSE) {
        vertices.add(new Point2D.Double(coords[0], coords[1]));
      }
      it.next();
    }
    return vertices;
  }

}
