package br.ufal.ic.p2.wepayu.model;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Empregado implements Serializable {
    private int id;
    private String nome;
    private String endereco;
    private MembroSindicato membroSindicato;
    private MetodoPagamento metodoPagamento;
    private LocalDate dataUltimoPagamento;
    private String agendaPagamento;

    public Empregado() {
        this.metodoPagamento = new EmMaos();
    }

    public Empregado(int id, String nome, String endereco) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.metodoPagamento = new EmMaos();
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public MembroSindicato getMembroSindicato() { return membroSindicato; }
    public void setMembroSindicato(MembroSindicato membroSindicato) { this.membroSindicato = membroSindicato; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public LocalDate getDataUltimoPagamento() { return dataUltimoPagamento; }
    public void setDataUltimoPagamento(LocalDate dataUltimoPagamento) { this.dataUltimoPagamento = dataUltimoPagamento; }
    public String getAgendaPagamento() { return agendaPagamento; }
    public void setAgendaPagamento(String agendaPagamento) { this.agendaPagamento = agendaPagamento; }

    public String getAtributo(String atributo) throws Exception {
        switch (atributo) {
            case "nome": return getNome();
            case "endereco": return getEndereco();
            case "sindicalizado": return String.valueOf(this.membroSindicato != null);
            case "agendaPagamento": return getAgendaPagamento();
            case "idSindicato":
                if (membroSindicato == null) throw new Exception("Empregado nao eh sindicalizado.");
                return membroSindicato.getIdSindicato();
            case "taxaSindical":
                if (membroSindicato == null) throw new Exception("Empregado nao eh sindicalizado.");
                return String.format("%.2f", membroSindicato.getTaxaSindical()).replace('.', ',');
            case "metodoPagamento":
                if (metodoPagamento instanceof EmMaos) return "emMaos";
                if (metodoPagamento instanceof Correios) return "correios";
                if (metodoPagamento instanceof Banco) return "banco";
                return "";
            case "banco": case "agencia": case "contaCorrente":
                if (!(metodoPagamento instanceof Banco)) throw new Exception("Empregado nao recebe em banco.");
                Banco b = (Banco) metodoPagamento;
                if (atributo.equals("banco")) return b.getBanco();
                if (atributo.equals("agencia")) return b.getAgencia();
                return b.getContaCorrente();
            default:
                throw new Exception("Atributo nao existe.");
        }
    }

    public abstract double calcularSalarioBruto(LocalDate dataFinal, LocalDate dataInicio);
    public abstract void limparDadosPagamento(LocalDate data);
}