package com.hexgen.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public abstract class Elemento {

  public void paintMerged(Graphics2D g2, Point2D centroBase, Path2D triangulo, Elemento one, Elemento other) {
    Color first = mezcla(color(), one.color());
    g2.setColor( mezcla(first, other.color()) );
    g2.fill(triangulo);
  }
  
  public void paintAttached(Graphics2D g2, Point2D centroBase, Path2D triangulo, Elemento el) {
    g2.setColor( mezcla(color(), el.color()) );
    g2.fill(triangulo);
  }
  
  public void paint(Graphics2D g2, Point2D centroBase, Path2D triangulo) {
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && getClass().isAssignableFrom(obj.getClass());
  }
  
  protected abstract Color color();
  
  protected Color mezcla(Color c1, Color c2) {
    int r = (c1.getRed() + c2.getRed()) / 2;
    int g = (c1.getGreen() + c2.getGreen()) / 2;
    int b = (c1.getBlue() + c2.getBlue()) / 2;
    int a = (c1.getAlpha() + c2.getAlpha() ) / 2;
    return new Color(r, g, b, a);
  }
}
