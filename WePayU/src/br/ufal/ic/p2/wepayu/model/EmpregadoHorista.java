package br.ufal.ic.p2.wepayu.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmpregadoHorista extends Empregado {
    private double salarioPorHora;
    private List<CartaoDePonto> cartoesDePonto = new ArrayList<>();

    public EmpregadoHorista() {}
    public EmpregadoHorista(int id, String nome, String endereco, double salarioPorHora) {
        super(id, nome, endereco);
        this.salarioPorHora = salarioPorHora;
        setAgendaPagamento("semanal 5");
    }

    public double getSalarioPorHora() { return salarioPorHora; }
    public void setSalarioPorHora(double salarioPorHora) { this.salarioPorHora = salarioPorHora; }
    public List<CartaoDePonto> getCartoesDePonto() { return cartoesDePonto; }
    public void setCartoesDePonto(List<CartaoDePonto> cartoesDePonto) { this.cartoesDePonto = cartoesDePonto; }

    public void lancaCartao(CartaoDePonto cartao) { this.cartoesDePonto.add(cartao); }

    public double[] getHorasTrabalhadas(LocalDate dataInicial, LocalDate dataFinal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        double horasNormais = 0;
        double horasExtras = 0;
        for (CartaoDePonto cartao : getCartoesDePonto()) {
            LocalDate dataCartao = LocalDate.parse(cartao.getData(), formatter);
            if (!dataCartao.isBefore(dataInicial) && dataCartao.isBefore(dataFinal)) {
                double horasDoCartao = cartao.getHoras();
                if (horasDoCartao > 8) { horasNormais += 8; horasExtras += horasDoCartao - 8; }
                else { horasNormais += horasDoCartao; }
            }
        }
        return new double[]{horasNormais, horasExtras};
    }

    @Override
    public String getAtributo(String atributo) throws Exception {
        switch (atributo) {
            case "tipo": return "horista";
            case "salario": return String.format("%.2f", getSalarioPorHora()).replace('.', ',');
            case "comissao": throw new Exception("Empregado nao eh comissionado.");
            default: return super.getAtributo(atributo);
        }
    }

    @Override
    public double calcularSalarioBruto(LocalDate dataFinal, LocalDate dataInicio) {
        double[] horas = getHorasTrabalhadas(dataInicio.plusDays(1), dataFinal.plusDays(1));
        double bruto = (horas[0] * getSalarioPorHora()) + (horas[1] * getSalarioPorHora() * 1.5);
        return SistemaFolha.truncar(bruto);
    }

    @Override
    public void limparDadosPagamento(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        getCartoesDePonto().removeIf(c -> !LocalDate.parse(c.getData(), formatter).isAfter(data));
        if (getMembroSindicato() != null) getMembroSindicato().limparTaxas(data);
        setDataUltimoPagamento(data);
    }
}