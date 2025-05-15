package com.hexgen;

import java.awt.*;

public class HexUtils {
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

  public static Point hexToPixel(double q, double r, int radius, boolean flatTop,
      boolean offsetCenter) {
    return hexToPixel((int)q, (int)r,radius, flatTop);
  }
  
  public static Point nohexToPixel(double q, double r, int radius, boolean flatTop,
      boolean offsetCenter) {
    if (offsetCenter) {
      q += 0.5;
      r += 0.5;
    }

    double x, y;
    if (flatTop) {
      x = radius * 3.0 / 2 * q;
      y = radius * Math.sqrt(3) * (r + q / 2.0);
    } else {
      x = radius * Math.sqrt(3) * (q + r / 2.0);
      y = radius * 3.0 / 2 * r;
    }

    return new Point((int) x, (int) y);
  }
}
