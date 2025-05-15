package com.hexgen;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class HexTilePanel extends JPanel {
  private int radius = 30;
  private int layers = 2;
  private boolean flatTop = true;
  private static final long serialVersionUID = -3614530822472919342L;

  public void setRadius(int radius) {
    this.radius = radius;
    repaint();
  }

  public void setLayers(int layers) {
    this.layers = layers;
    repaint();
  }

  public void setFlatTop(boolean flatTop) {
    this.flatTop = flatTop;
    repaint();
  }

  public void paintFolioOnly(Graphics2D g2, int x, int y) {
    Object[] dim = dimensions(false);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];

    // fondo blanco del folio
    g2.setColor(Color.WHITE);
    g2.fillRect(x, y, pageWidth, pageHeight);

    // contenido del folio: losetas
    paintLosetas(20, g2, x, y, pageWidth, pageHeight);
  }

  
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();

    // Fondo gris del área total del componente
    g2.setColor(new Color(220, 220, 220)); // gris claro
    g2.fillRect(0, 0, getWidth(), getHeight());

    Object[] dim = dimensions(true);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    Insets contentMargin = (Insets) dim[2];

    int x = contentMargin.left;
    int y = contentMargin.top;

    g2.setColor(Color.WHITE); // folio blanco
    g2.fillRect(x, y, pageWidth, pageHeight);

    g2.setColor(Color.BLACK); // borde del folio
    g2.setStroke(new BasicStroke(1f));
    g2.drawRect(x, y, pageWidth, pageHeight);

    paintLosetas(20, g2, x, y, pageWidth, pageHeight);
  }

  private void paintLosetas(int margin, Graphics2D g2, int x0, int y0, int folioWidth, int folioHeight) {
    int spacing = 10;

    int usableWidth = folioWidth - 2 * margin;
    int usableHeight = folioHeight - 2 * margin;

    int losetaWidth = getLosetaAncho();
    int losetaHeight = getLosetaAlto();

    int cols = Math.max(1, (usableWidth + spacing) / (losetaWidth + spacing));
    int rows = Math.max(1, (usableHeight + spacing) / (losetaHeight + spacing));

    // calcular el espacio real ocupado
    int totalWidth = cols * losetaWidth + (cols - 1) * spacing;
    int totalHeight = rows * losetaHeight + (rows - 1) * spacing;

    // calcular el offset para centrar dentro del folio
    int offsetX = x0 + (folioWidth - totalWidth) / 2;
    int offsetY = y0 + (folioHeight - totalHeight) / 2;

    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            int centerX = offsetX + col * (losetaWidth + spacing) + losetaWidth / 2;
            int centerY = offsetY + row * (losetaHeight + spacing) + losetaHeight / 2;
            drawLoseta(g2, centerX, centerY);
        }
    }
}

  private int getLosetaAncho() {
    return flatTop ? (int) (radius * 3.0 * layers) + radius
        : (int) (radius * Math.sqrt(3) * (2 * layers + 1));
  }

  private int getLosetaAlto() {
    return flatTop ? (int) (radius * Math.sqrt(3) * (2 * layers + 1))
        : (int) (radius * 3.0 * layers) + radius;
  }

  @Override
  public void updateUI() {
    Object[] dim = dimensions(true);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    Insets contentMargin = (Insets) dim[2];

    setPreferredSize(new Dimension(pageWidth + contentMargin.left + contentMargin.right,
        pageHeight + contentMargin.top + contentMargin.bottom));
  }

  private Object[] dimensions(boolean withMargin) {
    int dpi = 72; // o 300 si trabajas a alta resolución
    int pageWidth = (int) (210 * dpi / 25.4); // A4 = 210mm
    int pageHeight = (int) (297 * dpi / 25.4); // A4 = 297mm

    int availableWidth = pageWidth;
    int availableHeight = pageHeight;

    Container parent = getParent();
    if (parent instanceof JViewport viewport) {
      Dimension viewSize = viewport.getExtentSize();
      availableWidth = Math.max(pageWidth, viewSize.width);
      availableHeight = Math.max(pageHeight, viewSize.height);
      // Usar para centrar el folio
    }

    int margin = withMargin ? 20 : 0;
    
    int marginX = Math.max((availableWidth - pageWidth) / 2, margin);
    int marginY = Math.max((availableHeight - pageHeight) / 2, margin);

    Insets contentMargin = new Insets(marginY, marginX, marginY, marginX);

    return new Object[] {pageWidth, pageHeight, contentMargin};
  }

  private void drawLoseta(Graphics2D g2, int cx, int cy) {
    setBackground(Color.WHITE);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.GRAY);
    g2.setStroke(new BasicStroke(1));

    // Creamos el contorno del hexágono exterior como Shape
    Shape clipHex = buildOuterBoundaryShape(cx, cy, radius, layers);
    g2.setClip(clipHex); // cualquier dibujo fuera se recorta automáticamente

    for (int q = -layers; q <= layers; q++) {
      int r1 = Math.max(-layers, -q - layers);
      int r2 = Math.min(layers, -q + layers);
      for (int r = r1; r <= r2; r++) {
        Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
        drawHex(g2, cx + p.x, cy + p.y, radius);
      }
    }
    drawOuterBoundary(g2, cx, cy, radius, layers);
  }

  private void drawOuterBoundary(Graphics2D g2, int cx, int cy, int radius, int layers) {
    int[][] directions = flatTop ? new int[][] {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}
        : new int[][] {{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}, {1, 1}};

    Path2D hex = new Path2D.Double();

    for (int i = 0; i < 6; i++) {
      int dq = directions[i][0];
      int dr = directions[i][1];
      int q = dq * layers;
      int r = dr * layers;
      Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
      double x = cx + p.x;
      double y = cy + p.y;
      if (i == 0)
        hex.moveTo(x, y);
      else
        hex.lineTo(x, y);
    }

    hex.closePath();
    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke(2));
    g2.draw(hex);
  }

  private Shape buildOuterBoundaryShape(int cx, int cy, int radius, int layers) {
    int[][] directions = flatTop ? new int[][] {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}
        : new int[][] {{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}, {1, 1}};

    Path2D hex = new Path2D.Double();

    for (int i = 0; i < 6; i++) {
      int q = directions[i][0] * layers;
      int r = directions[i][1] * layers;
      Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
      double x = cx + p.x;
      double y = cy + p.y;
      if (i == 0)
        hex.moveTo(x, y);
      else
        hex.lineTo(x, y);
    }

    hex.closePath();
    return hex;
  }


  private void drawHex(Graphics2D g2, int x, int y, int radius) {
    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(flatTop ? (60 * i) : (60 * i - 30));
      int dx = (int) (x + radius * Math.cos(angle));
      int dy = (int) (y + radius * Math.sin(angle));
      if (i == 0)
        hex.moveTo(dx, dy);
      else
        hex.lineTo(dx, dy);
    }
    hex.closePath();
    g2.draw(hex);
  }
}
