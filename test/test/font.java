/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

/**
 *
 * @author Monster Huma H5 v4.1
 */
public class font {

    public static void main(String[] args) {
        java.awt.GraphicsEnvironment ge
                = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        for (String font : fonts) {
            System.out.println(font);
        }
    }
}
