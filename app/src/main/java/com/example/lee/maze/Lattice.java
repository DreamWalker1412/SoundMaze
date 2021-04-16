package com.example.lee.maze;
import java.util.Vector;

public class Lattice {//格子类
    static final int INTREE = 1;
    static final int NOTINTREE = 0;
    private int x;
    private int y;
    private int flag = 0;
    private Lattice father = null;

    public Lattice(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getFlag() {
        return flag;
    }
    public Lattice getFather() {
        return father;
    }
    public void setFather(Lattice f) {
        father = f;
    }
    public void setFlag(int f) {
        flag = f;
    }
    public static Vector findShortPath(Lattice[][] maze, int x, int y) {
        Vector<Lattice> vector = new Vector<>();
        Lattice p = maze[x][y];
        while (!(p.getFather() == null)) {
            p = p.getFather();
            vector.add(p);
        }
        return vector;
    }
}
