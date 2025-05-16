package com.hexgen.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.hexgen.HexUtils;
import com.hexgen.PasilloRenderer;

public class Loseta {
  private static final int[][] VECINOS_AXIALES =
      {{1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}};

  private int radius = 30;
  private int layers = 2;
  private boolean flatTop = true;
  private String tileType = "Pasillo";
  private List<Posicion> posiciones = new ArrayList<>();
  private Map<Point, Posicion> lookup = new HashMap<>();
  private Set<Integer> ladosConPuertas = new HashSet<>();
  private Integer ladoAbierto;
  private Integer ladoEntrada;

  public int getRadius() {
    return radius;
  }

  public void setRadius(int radius) {
    this.radius = radius;
  }

  public int getLayers() {
    return layers;
  }

  public void setLayers(int layers) {
    this.layers = layers;
  }

  public boolean isFlatTop() {
    return flatTop;
  }

  public void setFlatTop(boolean flatTop) {
    this.flatTop = flatTop;
  }

  public String getTileType() {
    return tileType;
  }

  public void setTileType(String tileType) {
    this.tileType = tileType;
  }

  public void drawLoseta(Graphics2D g2, int cx, int cy) {
    PasilloRenderer.drawPasilloDecoracion(this);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke(1));

    // Creamos el contorno del hexágono exterior como Shape
    int posicion = 0;
    posiciones.clear();

    Path2D generateHexPath =
        HexUtils.generateHexPath(false, cx, cy, radius * layers, layers, flatTop);
    g2.setClip(generateHexPath);

    List<Path2D> paths = new ArrayList<>();
    for (int q = -layers; q <= layers; q++) {
      int r1 = Math.max(-layers, -q - layers);
      int r2 = Math.min(layers, -q + layers);
      for (int r = r1; r <= r2; r++) {
        Point axial = new Point(q, r);
        Posicion pos = new Posicion();
        pos.setAxial(axial);
        pos.setPosicion(posicion++);
        pos.setFlatTop(flatTop);
        pos.setRadius(radius);
        pos.setLayers(layers);
        pos.setLoseta(this);
        Path2D generatePosPath = pos.generatePath(cx, cy);
        paths.add(generatePosPath);
        pos.setLateral(
            HexUtils.getLadosContactadosConInterseccion(generateHexPath, generatePosPath));
        posiciones.add(pos);
        lookup.put(axial, pos);
      }
    }
    g2.draw(generateHexPath);

    for (int lado = 0; lado < 6; lado++) {
      Color color = null;// Color.RED; // "Nada"
      if (lado == ladoEntrada) {
        color = Color.GREEN;
      } else if (null != ladoAbierto && lado == ladoAbierto) {
        color = Color.BLUE;
      } else if (ladosConPuertas.contains(lado)) {
        color = Color.RED;
      } else {
        color = Color.GRAY;
      }

      if (null != color) {
        // Centro del lado
        Point2D centroLado = HexUtils.puntoCentralDelLado(cx, cy, radius, layers, lado, flatTop);

        // Tamaño del rectángulo
        int width = radius * (layers - 1);
        int height = 30;

        // Rotación del rectángulo para alinearlo
        double angle = HexUtils.getAngleRadians(lado, flatTop);

        Graphics2D gSide = (Graphics2D) g2.create();
        gSide.translate(centroLado.getX(), centroLado.getY());
        gSide.rotate(angle + Math.PI / 2); // para que quede pegado al borde

        gSide.setColor(color);
        gSide.fillRect(-(width / 2), -(height / 2), width, height);

        gSide.setColor(Color.BLACK);
        gSide.drawRect(-(width / 2), -(height / 2), width, height);

        gSide.dispose();
      }
    }
    for (int i = 0; i < paths.size(); i++) {
      Posicion pos = posiciones.get(i);
      Path2D generatePosPath = paths.get(i);
      pos.drawDecoration(g2, cx, cy);
      g2.setColor(Color.BLACK);
      g2.draw(generatePosPath);
    }
  }

  public Map<Integer, Posicion> obtenerVecinos(int q, int r) {
    Map<Integer, Posicion> vecinos = new HashMap<>();
    for (int i = 0; i < 6; i++) {
      int dq = VECINOS_AXIALES[i][0];
      int dr = VECINOS_AXIALES[i][1];
      Point p = new Point(q + dq, r + dr);
      vecinos.put(i, lookup.get(p)); // puede ser null si no existe
    }
    return vecinos;
  }

  public Set<Integer> getLadosConPuertas() {
    return ladosConPuertas;
  }

  public void setLadosConPuertas(Set<Integer> ladosConPuertas) {
    this.ladosConPuertas = ladosConPuertas;
  }

  public Integer getLadoAbierto() {
    return ladoAbierto;
  }

  public void setLadoAbierto(Integer ladoAbierto) {
    this.ladoAbierto = ladoAbierto;
  }

  public Integer getLadoEntrada() {
    return ladoEntrada;
  }

  public void setLadoEntrada(Integer ladoEntrada) {
    this.ladoEntrada = ladoEntrada;
  }
}
