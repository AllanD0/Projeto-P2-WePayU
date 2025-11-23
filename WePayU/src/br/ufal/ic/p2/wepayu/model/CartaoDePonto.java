package br.ufal.ic.p2.wepayu.model;

import java.io.Serializable;

public class CartaoDePonto implements Serializable {
    private String data;
    private double horas;

    public CartaoDePonto() {}

    public CartaoDePonto(String data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }
}