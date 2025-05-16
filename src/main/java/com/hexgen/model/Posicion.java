package com.hexgen.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import com.hexgen.HexUtils;
import com.hexgen.model.elementos.Entrada;
import com.hexgen.model.elementos.Muro;
import com.hexgen.model.elementos.Puerta;
import com.hexgen.model.elementos.Salida;

public class Posicion {
  private boolean debug = false;
  private int radius;
  private int layers;
  private Point axial;
  private Loseta loseta;
  private List<Integer> lateral;
  private int posicion;
  private boolean flatTop = true;
  private String kind;

  public Path2D generatePath(int cx, int cy) {
    Point p = HexUtils.hexToPixel(axial.x, axial.y, radius, flatTop);
    return HexUtils.generateHexPath(true, cx + p.x, cy + p.y, radius, layers, flatTop);
  }

  public void drawDecoration(Graphics2D g2, int cx, int cy) {
    Point offset = HexUtils.hexToPixel(axial.x, axial.y, radius, flatTop);
    double centerX = cx + offset.x - radius;
    double centerY = cy + offset.y;

    double r = radius;

    Map<Integer, Posicion> vecinos = getVecinos();
    for (int i = 0; i < 6; i++) {
      // Ángulos de los vértices del lado
      double angle1 = Math.toRadians(flatTop ? (60 * i) : (60 * i - 30));
      double angle2 = Math.toRadians(flatTop ? (60 * (i + 1)) : (60 * (i + 1) - 30));

      // Vértices del lado
      double x1 = centerX + r * Math.cos(angle1);
      double y1 = centerY + r * Math.sin(angle1);
      double x2 = centerX + r * Math.cos(angle2);
      double y2 = centerY + r * Math.sin(angle2);

      double scale = 0.92; // margen interno

      // Acercar vértices al centro
      double x1s = centerX - (x1 - centerX) * scale;
      double y1s = centerY - (y1 - centerY) * scale;
      double x2s = centerX - (x2 - centerX) * scale;
      double y2s = centerY - (y2 - centerY) * scale;

      // Centro del lado también escalado
      double midX = (x1s + x2s) / 2;
      double midY = (y1s + y2s) / 2;

      // Construir el triángulo
      Path2D triangle = new Path2D.Double();
      triangle.moveTo(centerX, centerY);
      triangle.lineTo(x1s, y1s);
      triangle.lineTo(x2s, y2s);
      triangle.closePath();

      pintarTriangulo(g2, (i + 3) % 6, vecinos, new Point2D.Double(midX, midY), triangle);
    }
    if (debug) {
      addNumber(g2, cx, cy);
    }
  }

  public Elemento getElemento() {
    if (lateral.contains(loseta.getLadoEntrada())) {
      return new Entrada();
    } else if (lateral.contains(loseta.getLadoAbierto())) {
      return new Salida();
    } else if (loseta.getLadosConPuertas().stream().anyMatch(lateral::contains)) {
      return new Puerta();
    } else if( !lateral.isEmpty() ) {
      return new Muro();
    } else {
      return null;
    }
  }

  private static int mod(int x, int m) {
    return (x % m + m) % m;
  }

  private Elemento elementoVecino(Map<Integer, Posicion> vecinos, int lado) {
    Posicion vecino = vecinos.get(mod(lado, 6));
    return null == vecino ? null : vecino.getElemento();
  }

  private void pintarTriangulo(Graphics2D g2, int lado, Map<Integer, Posicion> vecinos,
      Point2D centroBase, Path2D triangulo) {
    Posicion vecino = vecinos.get(lado);
    boolean traze = posicion == 111 && lado == 0;
    if (traze) {
      // System.out.println("Soy " + posicion + " en " + lado + " lindo con " + vecino.getPosicion()
      // );
    }
    Elemento self = getElemento();
    Elemento attached = null;
    Elemento merged = null;
    if (self != null) {
      if (traze) {
        System.out.println("Estoy en el lado " + lado);
        System.out.println("\tMi vecino es " + vecino);
      }
      if (null != vecino) {
        attached = vecino.getElemento();
      }
      if (traze) {
        System.out.println("\tEstoy ahora con " + attached);
      }
      if (null == attached) {
        Elemento elementoPrevio = elementoVecino(vecinos, lado - 1);
        Elemento elementoPoseterior = elementoVecino(vecinos, lado + 1);

        if (traze) {
          System.out.println("\t\tCon elemento en anterior " + elementoPrevio);
          System.out.println(
              "\t\tVecinos " + ((lado - 1) % 6) + " organizados: " + vecinos.get((lado - 1) % 6));
          System.out.println("\t\tCon elemento en posterior " + elementoPoseterior);
        }
        if (elementoPrevio != null && elementoPoseterior != null) {
          if (elementoPrevio.equals(elementoPoseterior)) {
            attached = elementoPrevio;
          } else {
            attached = elementoPrevio;
            merged = elementoPoseterior;
          }
        }
      }
      if (attached != null && merged != null) {
        self.paintMerged(g2, centroBase, triangulo, attached, merged);
      } else if (null != attached) {
        self.paintAttached(g2, centroBase, triangulo, attached);
        // } else if( null != merged ) {
        // self.paintAttached(g2, centroBase, triangulo, merged);
      } else {
        self.paint(g2, centroBase, triangulo);
      }
    }
    if (debug) {
      addTriangleColors(g2, lado, centroBase, triangulo);
    }
  }

  private Map<Integer, Posicion> getVecinos() {
    return loseta.obtenerVecinos(axial.x, axial.y);
  }

  private void addTriangleColors(Graphics2D g2, int lado, Point2D centroBase, Path2D triangulo) {
    // Ejemplo básico: pintar de gris claro con borde
    Color[] colores = new Color[] {new Color(200, 0, 0, 80), new Color(0, 200, 0, 80),
        new Color(0, 0, 200, 80), new Color(200, 100, 100, 80), new Color(100, 200, 100, 80),
        new Color(100, 100, 200, 80),};
    g2.setColor(colores[lado % 6]);
    g2.fill(triangulo);

    g2.setColor(Color.DARK_GRAY);
    g2.draw(triangulo);

    // Ejemplo: etiqueta de lado
    g2.setColor(Color.BLACK);
    g2.drawString("L" + lado, (int) centroBase.getX(), (int) centroBase.getY());
  }

  private void addNumber(Graphics2D g2, int cx, int cy) {
    Point p = HexUtils.hexToPixel(axial.x, axial.y, radius, flatTop);
    int px = cx + p.x - radius;
    int py = cy + p.y;

    // Configurar fuente y estilo
    g2.setColor(Color.BLACK);
    g2.setFont(g2.getFont().deriveFont(Font.BOLD, radius * 0.3f)); // tamaño relativo al radio

    // Convertir número a string
    String label = Integer.toString(posicion);

    label += ": " + String.join(",", lateral.stream().map(i -> i.toString()).toList());

    // Medir ancho y alto del texto para centrarlo
    FontMetrics metrics = g2.getFontMetrics();
    int textWidth = metrics.stringWidth(label);
    int textHeight = metrics.getAscent(); // Usamos ascent, no height completo

    // Dibujar texto centrado en el hexágono
    g2.drawString(label, px - textWidth / 2, py + textHeight / 2);
  }

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

  public Point getAxial() {
    return axial;
  }

  public void setAxial(Point axial) {
    this.axial = axial;
  }

  public Loseta getLoseta() {
    return loseta;
  }

  public void setLoseta(Loseta loseta) {
    this.loseta = loseta;
  }

  public int getPosicion() {
    return posicion;
  }

  public void setPosicion(int posicion) {
    this.posicion = posicion;
  }

  public boolean isFlatTop() {
    return flatTop;
  }

  public void setFlatTop(boolean flatTop) {
    this.flatTop = flatTop;
  }

  public List<Integer> getLateral() {
    return lateral;
  }

  public void setLateral(List<Integer> lateral) {
    this.lateral = lateral;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  // private Point axialCoordinates() {
  // int count = 0;
  // for (int q = -layers; q <= layers; q++) {
  // int r1 = Math.max(-layers, -q - layers);
  // int r2 = Math.min(layers, -q + layers);
  // for (int r = r1; r <= r2; r++) {
  // if (count == posicion) {
  // return new Point(q, r);
  // }
  // count++;
  // }
  // }
  // throw new IllegalArgumentException("No axial coordinates for " + posicion);
  // }
}
