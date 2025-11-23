package br.ufal.ic.p2.wepayu.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EmpregadoAssalariado extends Empregado {
    private double salarioMensal;

    public EmpregadoAssalariado(int id, String nome, String endereco, double salarioMensal) {
        super(id, nome, endereco);
        this.salarioMensal = salarioMensal;
        setAgendaPagamento("mensal $");
    }
    public EmpregadoAssalariado() {}

    public double getSalarioMensal() { return salarioMensal; }
    public void setSalarioMensal(double salarioMensal) { this.salarioMensal = salarioMensal; }

    @Override
    public String getAtributo(String atributo) throws Exception {
        switch (atributo) {
            case "tipo": return "assalariado";
            case "salario": return String.format("%.2f", getSalarioMensal()).replace('.', ',');
            case "comissao": throw new Exception("Empregado nao eh comissionado.");
            default: return super.getAtributo(atributo);
        }
    }

    @Override
    public double calcularSalarioBruto(LocalDate dataFinal, LocalDate dataInicio) {
        String agenda = getAgendaPagamento();
        if (agenda.startsWith("mensal")) {
            return SistemaFolha.truncar(getSalarioMensal());
        } else {
            long weeks = ChronoUnit.WEEKS.between(dataInicio, dataFinal);
            if (weeks == 0) weeks = 1;
            return SistemaFolha.truncar(getSalarioMensal() * 12.0 / 52.0 * weeks);
        }
    }

    @Override
    public void limparDadosPagamento(LocalDate data) {
        if (getMembroSindicato() != null) getMembroSindicato().limparTaxas(data);
        setDataUltimoPagamento(data);
    }
}