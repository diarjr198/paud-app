/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.paud;

/**
 *
 * @author diarjr198
 */
import java.awt.*;

public class Theme {
    public static final Color BG_MAIN      = new Color(236, 248, 244);
    public static final Color BG_SIDEBAR   = Color.WHITE;
    public static final Color SIDEBAR_SEL  = new Color(100, 181, 246);
    public static final Color HEADER_BG    = new Color(220, 240, 255);
    public static final Color TEXT_DARK    = new Color(30, 30, 30);
    public static final Color TEXT_GRAY    = new Color(100, 100, 100);

    public static final Color GREEN        = new Color(76, 175, 80);
    public static final Color ORANGE       = new Color(255, 152, 0);
    public static final Color RED          = new Color(229, 57, 53);
    public static final Color GRAY_BTN     = new Color(158, 158, 158);
    public static final Color BLUE_BTN     = new Color(33, 150, 243);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(220, 220, 220);

    public static final Font FONT_TITLE    = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADER   = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_NAV      = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_NAV_SEL  = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BIG_PCT  = new Font("SansSerif", Font.BOLD, 26);

    // Menangani proses: make card.
    public static RoundedPanel makeCard(int arc) {
        RoundedPanel p = new RoundedPanel(arc, CARD_BG);
        p.setBackground(CARD_BG);
        return p;
    }
}
