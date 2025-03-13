package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class Visualiser extends JPanel {
    private final int sqrSize = 33;
    private final int sqrSpacing = 1;
    private final int y = 10;

    public int[][] data;


    public Visualiser(Storage s) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedIndex = getClickedBoxIndex(e.getX(),e.getY());
                if (clickedIndex >= 0) {
                    if (data[1][clickedIndex - 2] == 1) {
                        s.free(clickedIndex);
                        System.out.println(clickedIndex);
                        data = s.export();
                        repaint();
                    }
                } else {
                    int p = s.malloc(Integer.parseInt(JOptionPane.showInputDialog("Enter malloc size")));
                    System.out.println("mallocd " + p);
                    data = s.export();
                    repaint();
                }

            }

        });
        data = s.export();
        repaint();
    }

    private int getClickedBoxIndex(int mouseX, int mouseY) {
        for (int i = 0; i < data[0].length; i++) {
            int x = i * (sqrSize + sqrSpacing);
            if (mouseX >= x && mouseX <= x + sqrSize && mouseY >= y && mouseY <= y + sqrSize)
                return i;

        }
        return -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null) {
            return;
        }


        int[] mem = data[0];
        int[] metadata = data[1];

        for (int i = 0; i < mem.length; i++) {
            int x = i * (sqrSize + sqrSpacing);
            Color c = switch (metadata[i]) {
                case 0 -> Color.red; //problem
                case 1 -> Color.yellow; //back ptr
                case 2 -> Color.pink; //fwd busy
                case 3 -> Color.green; //fwd free
                case 4 -> Color.blue; //reg busy
                case 5 -> Color.cyan; //reg free
                default -> throw new IllegalArgumentException();
            };

            g.setColor(c);
            g.fillRect(x,y,sqrSize,sqrSize);
            g.setColor(Color.black);
            g.drawRect(x,y,sqrSize,sqrSize);

            //draw data
            g.setColor(Color.black);
            String prefix = switch (metadata[i]) {
                case 0 -> "X";
                case 1 -> "B";
                case 2 -> "F";
                case 3 -> "F";
                case 4 -> "U";
                case 5 -> "A";
                default -> throw new IllegalArgumentException();
            };
            String text = prefix + String.valueOf(mem[i]);
            FontMetrics fm = g.getFontMetrics();
            int tx = x + (sqrSize - fm.stringWidth(text)) / 2;
            int ty = y + (sqrSize + fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text,tx,ty);
            g.drawString(String.valueOf(i), tx, ty - (sqrSize / 2));
        }
    }
}
