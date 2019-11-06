package com.neodynamica.userinterface.gui;

class State {

    private String frame;
    private FormulaData data;
    //create state values

    State(String s, FormulaData d) {
        frame = s;
        data = d;
    }

    public FormulaData getData() {
        return data;
    }

    String getFrame() {
        return frame;
    }
}
