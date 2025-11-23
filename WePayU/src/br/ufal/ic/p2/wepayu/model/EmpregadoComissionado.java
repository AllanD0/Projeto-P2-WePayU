package br.ufal.ic.p2.wepayu.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoComissionado extends EmpregadoAssalariado {
    private double taxaDeComissao;
    private List<ResultadoVenda> resultadosDeVenda = new ArrayList<>();

    public EmpregadoComissionado() {}
    public EmpregadoComissionado(int id, String nome, String endereco, double salarioMensal, double taxaDeComissao) {
        super(id, nome, endereco, salarioMensal);
        this.taxaDeComissao = taxaDeComissao;
        setAgendaPagamento("semanal 2 5");
    }

    public double getTaxaDeComissao() { return taxaDeComissao; }
    public void setTaxaDeComissao(double taxaDeComissao) { this.taxaDeComissao = taxaDeComissao; }
    public List<ResultadoVenda> getResultadosDeVenda() { return resultadosDeVenda; }
    public void setResultadosDeVenda(List<ResultadoVenda> resultadosDeVenda) { this.resultadosDeVenda = resultadosDeVenda; }
    public void lancaVenda(ResultadoVenda venda) { this.resultadosDeVenda.add(venda); }

    public double getVendasRealizadas(LocalDate dataInicial, LocalDate dataFinal) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        double totalVendas = 0;
        for (ResultadoVenda venda : getResultadosDeVenda()) {
            LocalDate dataVenda = LocalDate.parse(venda.getData(), formatter);
            if (!dataVenda.isBefore(dataInicial) && dataVenda.isBefore(dataFinal)) {
                totalVendas += venda.getValor();
            }
        }
        return totalVendas;
    }

    @Override
    public String getAtributo(String atributo) throws Exception {
        switch (atributo) {
            case "tipo": return "comissionado";
            case "comissao": return String.format("%.2f", getTaxaDeComissao()).replace('.', ',');
            default: return super.getAtributo(atributo);
        }
    }

    @Override
    public double calcularSalarioBruto(LocalDate dataFinal, LocalDate dataInicio) {
        String agenda = getAgendaPagamento();
        double fixo;

        if (agenda.startsWith("mensal")) {
            fixo = getSalarioMensal();
        } else {
            long weeks = ChronoUnit.WEEKS.between(dataInicio, dataFinal);
            if (weeks == 0) weeks = 1;
            fixo = SistemaFolha.truncar(getSalarioMensal() * 12.0 / 52.0 * weeks);
        }

        double vendas = getVendasRealizadas(dataInicio.plusDays(1), dataFinal.plusDays(1));

        double comissao = SistemaFolha.truncar(vendas * getTaxaDeComissao());
        return fixo + comissao;
    }

    @Override
    public void limparDadosPagamento(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        getResultadosDeVenda().removeIf(v -> !LocalDate.parse(v.getData(), formatter).isAfter(data));
        if (getMembroSindicato() != null) getMembroSindicato().limparTaxas(data);
        setDataUltimoPagamento(data);
    }
}