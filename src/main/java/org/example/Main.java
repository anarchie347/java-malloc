package org.example;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Storage s = new Storage();
        s.initialise(40);

        JFrame frame = new JFrame();
        Visualiser v = new Visualiser(s);
        frame.add(v);
        frame.setSize(1500,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}