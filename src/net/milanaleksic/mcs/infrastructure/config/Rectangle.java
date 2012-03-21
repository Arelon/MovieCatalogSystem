package net.milanaleksic.mcs.infrastructure.config;

/**
 * User: Milan Aleksic
 * Date: 3/21/12
 * Time: 2:25 PM
 */
public class Rectangle {

    private int x;

    private int y;

    private int width;

    private int height;

    public Rectangle() {}

    public Rectangle(org.eclipse.swt.graphics.Rectangle rectangle) {
        this.x = rectangle.x;
        this.y = rectangle.y;
        this.width = rectangle.width;
        this.height = rectangle.height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public org.eclipse.swt.graphics.Rectangle toSWTRectangle() {
        return new org.eclipse.swt.graphics.Rectangle(x, y, width, height);
    }
}
