package br.ufal.ic.p2.wepayu.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;

public class MembroSindicato implements Serializable {

    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasDeServico = new ArrayList<>();

    public MembroSindicato() {
    }

    public String getIdSindicato() {
        return idSindicato;
    }

    public void setIdSindicato(String idSindicato) {
        this.idSindicato = idSindicato;
    }

    public double getTaxaSindical() {
        return taxaSindical;
    }

    public void setTaxaSindical(double taxaSindical) {
        this.taxaSindical = taxaSindical;
    }

    public List<TaxaServico> getTaxasDeServico() {
        return taxasDeServico;
    }

    public void setTaxasDeServico(List<TaxaServico> taxasDeServico) {
        this.taxasDeServico = taxasDeServico;
    }

    public void lancaTaxaServico(TaxaServico taxa) {
        this.taxasDeServico.add(taxa);
    }
    public double getTaxasServicoNoPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        double totalTaxas = 0;

        for (TaxaServico taxa : getTaxasDeServico()) {
            LocalDate dataTaxa = LocalDate.parse(taxa.getData(), formatter);
            if (!dataTaxa.isBefore(dataInicial) && dataTaxa.isBefore(dataFinal)) {
                totalTaxas += taxa.getValor();
            }
        }
        return totalTaxas;
    }
    public void limparTaxas(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        taxasDeServico.removeIf(t -> !LocalDate.parse(t.getData(), formatter).isAfter(data));
    }
}