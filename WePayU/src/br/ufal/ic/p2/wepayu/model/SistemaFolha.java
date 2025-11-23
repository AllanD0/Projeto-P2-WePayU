package br.ufal.ic.p2.wepayu.model;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;

public class SistemaFolha implements Serializable {

    private Map<Integer, Empregado> empregados = new HashMap<>();
    private List<String> agendas = new ArrayList<>();
    private final String ARQUIVO_DE_DADOS = "data.xml";
    private int proximoId = 1;

    public SistemaFolha() {
        agendas.add("mensal $");
        agendas.add("semanal 5");
        agendas.add("semanal 2 5");
    }

    public List<String> getAgendas() { return agendas; }

    // --- UNDO/REDO ---
    public int getNumeroDeEmpregados() { return empregados.size(); }

    public byte[] getSnapshot() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(empregados);
            oos.writeObject(agendas);
            oos.writeInt(proximoId);
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar snapshot: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void restaurarSnapshot(byte[] snapshot) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(snapshot);
            ObjectInputStream ois = new ObjectInputStream(bais);
            this.empregados = (Map<Integer, Empregado>) ois.readObject();
            this.agendas = (List<String>) ois.readObject();
            this.proximoId = ois.readInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- PERSISTÊNCIA ---
    public void salvarEstado() {
        try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(ARQUIVO_DE_DADOS))) {
            encoder.setPersistenceDelegate(LocalDate.class,
                    new java.beans.PersistenceDelegate() {
                        @Override
                        protected java.beans.Expression instantiate(Object oldInstance, java.beans.Encoder out) {
                            LocalDate date = (LocalDate) oldInstance;
                            return new java.beans.Expression(oldInstance,
                                    LocalDate.class, "of",
                                    new Object[]{date.getYear(), date.getMonthValue(), date.getDayOfMonth()});
                        }
                    });
            encoder.writeObject(new ArrayList<>(empregados.values()));
            encoder.writeObject(agendas);
            encoder.writeObject(proximoId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void carregarEstado() {
        File arquivo = new File(ARQUIVO_DE_DADOS);
        if (!arquivo.exists()) return;
        try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(arquivo))) {
            ArrayList<Empregado> listaEmpregados = (ArrayList<Empregado>) decoder.readObject();
            try { this.agendas = (List<String>) decoder.readObject(); } catch (Exception e) {}
            this.proximoId = (Integer) decoder.readObject();
            this.empregados.clear();
            for (Empregado e : listaEmpregados) {
                this.empregados.put(e.getId(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zerarSistema() {
        empregados.clear();
        agendas.clear();
        agendas.add("mensal $");
        agendas.add("semanal 5");
        agendas.add("semanal 2 5");
        proximoId = 1;
        File arquivo = new File(ARQUIVO_DE_DADOS);
        if (arquivo.exists()) arquivo.delete();
    }

    // --- EMPREGADOS ---
    public int adicionarEmpregadoHorista(String nome, String endereco, double salarioPorHora) {
        int id = proximoId++;
        EmpregadoHorista novoEmpregado = new EmpregadoHorista(id, nome, endereco, salarioPorHora);
        empregados.put(id, novoEmpregado);
        return id;
    }

    public int adicionarEmpregadoAssalariado(String nome, String endereco, double salarioMensal) {
        int id = proximoId++;
        EmpregadoAssalariado novoEmpregado = new EmpregadoAssalariado(id, nome, endereco, salarioMensal);
        empregados.put(id, novoEmpregado);
        return id;
    }

    public int adicionarEmpregadoComissionado(String nome, String endereco, double salarioMensal, double taxaDeComissao) {
        int id = proximoId++;
        EmpregadoComissionado novoEmpregado = new EmpregadoComissionado(id, nome, endereco, salarioMensal, taxaDeComissao);
        empregados.put(id, novoEmpregado);
        return id;
    }

    public Empregado getEmpregado(int id) { return empregados.get(id); }

    public String getAtributoEmpregado(int id, String atributo) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        return e.getAtributo(atributo);
    }

    public void removerEmpregado(int id) throws Exception {
        if (!empregados.containsKey(id)) throw new Exception("Empregado nao existe.");
        empregados.remove(id);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        ArrayList<Empregado> empregadosEncontrados = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e.getNome().equals(nome)) empregadosEncontrados.add(e);
        }
        if (indice > empregadosEncontrados.size() || empregadosEncontrados.isEmpty()) throw new Exception("Nao ha empregado com esse nome.");
        return String.valueOf(empregadosEncontrados.get(indice - 1).getId());
    }

    private LocalDate parseData(String dataStr, String nomeCampo) throws Exception {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
            return LocalDate.parse(dataStr, formatter);
        } catch (DateTimeParseException e) {
            throw new Exception(nomeCampo + " invalida.");
        }
    }

    // --- VENDAS, HORAS, TAXAS ---
    public double getHorasNormaisTrabalhadas(int id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dataInicial = parseData(dataInicialStr, "Data inicial");
        LocalDate dataFinal = parseData(dataFinalStr, "Data final");
        if (dataInicial.isAfter(dataFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        return ((EmpregadoHorista) e).getHorasTrabalhadas(dataInicial, dataFinal)[0];
    }

    public double getHorasExtrasTrabalhadas(int id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoHorista)) throw new Exception("Empregado nao eh horista.");
        LocalDate dataInicial = parseData(dataInicialStr, "Data inicial");
        LocalDate dataFinal = parseData(dataFinalStr, "Data final");
        if (dataInicial.isAfter(dataFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        return ((EmpregadoHorista) e).getHorasTrabalhadas(dataInicial, dataFinal)[1];
    }

    public void lancaCartao(int id, String data, double horas) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        parseData(data, "Data");
        if (e instanceof EmpregadoHorista) ((EmpregadoHorista) e).lancaCartao(new CartaoDePonto(data, horas));
        else throw new Exception("Empregado nao eh horista.");
    }

    public void lancaVenda(int id, String data, double valor) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        parseData(data, "Data");
        if (e instanceof EmpregadoComissionado) ((EmpregadoComissionado) e).lancaVenda(new ResultadoVenda(data, valor));
        else throw new Exception("Empregado nao eh comissionado.");
    }

    public double getVendasRealizadas(int id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (!(e instanceof EmpregadoComissionado)) throw new Exception("Empregado nao eh comissionado.");
        LocalDate dataInicial = parseData(dataInicialStr, "Data inicial");
        LocalDate dataFinal = parseData(dataFinalStr, "Data final");
        if (dataInicial.isAfter(dataFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        return ((EmpregadoComissionado) e).getVendasRealizadas(dataInicial, dataFinal);
    }

    public void lancaTaxaServico(String idSindicato, String data, double valor) throws Exception {
        for (Empregado e : empregados.values()) {
            if (e.getMembroSindicato() != null && e.getMembroSindicato().getIdSindicato().equals(idSindicato)) {
                parseData(data, "Data");
                e.getMembroSindicato().lancaTaxaServico(new TaxaServico(data, valor));
                return;
            }
        }
        throw new Exception("Membro nao existe.");
    }

    public double getTaxasServico(int id, String dataInicialStr, String dataFinalStr) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (e.getMembroSindicato() == null) throw new Exception("Empregado nao eh sindicalizado.");
        LocalDate dataInicial = parseData(dataInicialStr, "Data inicial");
        LocalDate dataFinal = parseData(dataFinalStr, "Data final");
        if (dataInicial.isAfter(dataFinal)) throw new Exception("Data inicial nao pode ser posterior aa data final.");
        return e.getMembroSindicato().getTaxasServicoNoPeriodo(dataInicial, dataFinal);
    }

    // --- ALTERAÇÕES EM EMPREGADO ---
    public void alteraEmpregadoSindicalizado(int id, String idSindicato, double taxaSindical) throws Exception {
        for (Empregado e : empregados.values()) {
            if (e.getId() != id && e.getMembroSindicato() != null && e.getMembroSindicato().getIdSindicato().equals(idSindicato)) {
                throw new Exception("Ha outro empregado com esta identificacao de sindicato");
            }
        }
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        MembroSindicato membro = new MembroSindicato();
        membro.setIdSindicato(idSindicato);
        membro.setTaxaSindical(taxaSindical);
        e.setMembroSindicato(membro);
    }

    public void removeEmpregadoDoSindicato(int id) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        e.setMembroSindicato(null);
    }

    public void alteraNome(int id, String nome) throws Exception {
        if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
        getEmpregado(id).setNome(nome);
    }

    public void alteraEndereco(int id, String endereco) throws Exception {
        if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
        getEmpregado(id).setEndereco(endereco);
    }

    public void alteraSalario(int id, double salario) throws Exception {
        if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");
        Empregado e = getEmpregado(id);
        if (e instanceof EmpregadoHorista) ((EmpregadoHorista) e).setSalarioPorHora(salario);
        else if (e instanceof EmpregadoAssalariado) ((EmpregadoAssalariado) e).setSalarioMensal(salario);
    }

    public void alteraComissao(int id, double comissao) throws Exception {
        if (comissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
        Empregado e = getEmpregado(id);
        if (e instanceof EmpregadoComissionado) ((EmpregadoComissionado) e).setTaxaDeComissao(comissao);
        else throw new Exception("Empregado nao eh comissionado.");
    }

    public void alteraMetodoPagamento(int id, String metodo) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        switch (metodo.toLowerCase()) {
            case "emmaos": e.setMetodoPagamento(new EmMaos()); break;
            case "correios": e.setMetodoPagamento(new Correios()); break;
            default: throw new Exception("Metodo de pagamento invalido.");
        }
    }

    public void alteraMetodoPagamento(int id, String banco, String agencia, String conta) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
        if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
        if (conta == null || conta.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");
        Banco b = new Banco();
        b.setBanco(banco); b.setAgencia(agencia); b.setContaCorrente(conta);
        e.setMetodoPagamento(b);
    }

    public void alteraEmpregadoTipo(int id, String tipo, double salario, double comissao) throws Exception {
        Empregado eAntigo = getEmpregado(id);
        if (eAntigo == null) throw new Exception("Empregado nao existe.");
        Empregado eNovo = null;
        switch (tipo.toLowerCase()) {
            case "horista":
                eNovo = new EmpregadoHorista(id, eAntigo.getNome(), eAntigo.getEndereco(), salario);
                break;
            case "assalariado":
                eNovo = new EmpregadoAssalariado(id, eAntigo.getNome(), eAntigo.getEndereco(), salario);
                break;
            case "comissionado":
                double salarioBase;
                if (salario != -1) salarioBase = salario;
                else if (eAntigo instanceof EmpregadoAssalariado) salarioBase = ((EmpregadoAssalariado) eAntigo).getSalarioMensal();
                else if (eAntigo instanceof EmpregadoHorista) salarioBase = ((EmpregadoHorista) eAntigo).getSalarioPorHora();
                else salarioBase = 0;
                eNovo = new EmpregadoComissionado(id, eAntigo.getNome(), eAntigo.getEndereco(), salarioBase, comissao);
                break;
            default: throw new Exception("Tipo invalido.");
        }
        eNovo.setMetodoPagamento(eAntigo.getMetodoPagamento());
        eNovo.setMembroSindicato(eAntigo.getMembroSindicato());
        empregados.put(id, eNovo);
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        if (agendas.contains(descricao)) throw new Exception("Agenda de pagamentos ja existe");
        String[] partes = descricao.split(" ");
        boolean valida = false;
        if (partes.length == 2 && partes[0].equals("mensal")) {
            if (partes[1].equals("$")) valida = true;
            else {
                try {
                    int dia = Integer.parseInt(partes[1]);
                    if (dia >= 1 && dia <= 28) valida = true;
                } catch (NumberFormatException e) {}
            }
        } else if (partes.length == 2 && partes[0].equals("semanal")) {
            try {
                int dow = Integer.parseInt(partes[1]);
                if (dow >= 1 && dow <= 7) valida = true;
            } catch (NumberFormatException e) {}
        } else if (partes.length == 3 && partes[0].equals("semanal")) {
            try {
                int intervalo = Integer.parseInt(partes[1]);
                int dow = Integer.parseInt(partes[2]);
                if (intervalo >= 1 && intervalo <= 52 && dow >= 1 && dow <= 7) valida = true;
            } catch (NumberFormatException e) {}
        }
        if (!valida) throw new Exception("Descricao de agenda invalida");
        agendas.add(descricao);
    }

    public void alteraAgendaPagamento(int id, String agenda) throws Exception {
        Empregado e = getEmpregado(id);
        if (e == null) throw new Exception("Empregado nao existe.");
        e.setAgendaPagamento(agenda);
    }

    public boolean isDiaDePagamento(Empregado empregado, LocalDate data) {
        String agenda = empregado.getAgendaPagamento();
        String[] partes = agenda.split(" ");
        String tipo = partes[0];
        if (tipo.equalsIgnoreCase("mensal")) {
            String diaStr = partes[1];
            if (diaStr.equals("$")) {
                LocalDate ultimo = data.withDayOfMonth(data.lengthOfMonth());
                if (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY) ultimo = ultimo.minusDays(1);
                if (ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) ultimo = ultimo.minusDays(2);
                return data.equals(ultimo);
            } else {
                return data.getDayOfMonth() == Integer.parseInt(diaStr);
            }
        } else if (tipo.equalsIgnoreCase("semanal")) {
            int interval = 1;
            int dow;
            if (partes.length == 2) dow = Integer.parseInt(partes[1]);
            else { interval = Integer.parseInt(partes[1]); dow = Integer.parseInt(partes[2]); }
            if (data.getDayOfWeek().getValue() != dow) return false;
            LocalDate ref = LocalDate.of(2004, 12, 31);
            while (ref.getDayOfWeek().getValue() != dow) ref = ref.minusDays(1);
            long weeks = ChronoUnit.WEEKS.between(ref, data);
            return weeks % interval == 0;
        }
        return false;
    }

    private LocalDate getInicioPeriodo(Empregado e, LocalDate dataPagamento) {
        String agenda = e.getAgendaPagamento();
        if (agenda.startsWith("mensal")) {
            return dataPagamento.minusMonths(1);
        } else {
            String[] parts = agenda.split(" ");
            int weeks = 1;
            if (parts.length > 2) weeks = Integer.parseInt(parts[1]);
            return dataPagamento.minusWeeks(weeks);
        }
    }

    // --- FOLHA DE PAGAMENTO ---
    public static double truncar(double valor) {
        return Math.floor(valor * 100 + 0.00001) / 100.0;
    }
    private String formatarDouble(double valor) { return String.format("%.2f", valor).replace('.', ','); }

    private String getMetodoPagamentoFormatado(Empregado e) {
        MetodoPagamento mp = e.getMetodoPagamento();
        if (mp instanceof EmMaos) return "Em maos";
        if (mp instanceof Correios) return "Correios, " + e.getEndereco();
        if (mp instanceof Banco) {
            Banco b = (Banco) mp;
            return b.getBanco() + ", Ag. " + b.getAgencia() + " CC " + b.getContaCorrente();
        }
        return "";
    }

    private double calcularDeducoes(Empregado empregado, LocalDate dataPagamento) {
        double deducoes = 0;
        if (empregado.getMembroSindicato() != null) {
            MembroSindicato membro = empregado.getMembroSindicato();
            double taxaSindical = membro.getTaxaSindical();
            LocalDate ultimoPagamento = empregado.getDataUltimoPagamento();
            long dias = 0;
            if (ultimoPagamento != null) dias = ChronoUnit.DAYS.between(ultimoPagamento, dataPagamento);
            if (dias == 0) {
                if (empregado instanceof EmpregadoHorista) dias = 7;
                else if (empregado instanceof EmpregadoComissionado) dias = 14;
                else dias = dataPagamento.lengthOfMonth();
            }
            deducoes += taxaSindical * dias;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            for (TaxaServico taxa : membro.getTaxasDeServico()) {
                LocalDate dataTaxa = LocalDate.parse(taxa.getData(), formatter);
                if (!dataTaxa.isAfter(dataPagamento)) deducoes += taxa.getValor();
            }
        }
        return deducoes;
    }

    public double totalFolha(String dataStr) throws Exception {
        LocalDate data = parseData(dataStr, "Data");
        double total = 0.0;
        for (Empregado empregado : empregados.values()) {
            if (isDiaDePagamento(empregado, data)) {
                LocalDate inicio = empregado.getDataUltimoPagamento();
                if (inicio == null) inicio = getInicioPeriodo(empregado, data);
                total += empregado.calcularSalarioBruto(data, inicio);
            }
        }
        return truncar(total);
    }

    public void rodaFolha(String dataStr, String nomeArquivo) throws Exception {
        LocalDate data = parseData(dataStr, "Data");
        ArrayList<EmpregadoHorista> horistas = new ArrayList<>();
        ArrayList<EmpregadoAssalariado> assalariados = new ArrayList<>();
        ArrayList<EmpregadoComissionado> comissionados = new ArrayList<>();

        for (Empregado e : empregados.values()) {
            if (isDiaDePagamento(e, data)) {
                if (e instanceof EmpregadoHorista) horistas.add((EmpregadoHorista) e);
                else if (e instanceof EmpregadoComissionado) comissionados.add((EmpregadoComissionado) e);
                else if (e instanceof EmpregadoAssalariado) assalariados.add((EmpregadoAssalariado) e);
            }
        }
        Comparator<Empregado> comp = (e1, e2) -> e1.getNome().compareTo(e2.getNome());
        horistas.sort(comp); assalariados.sort(comp); comissionados.sort(comp);

        try (PrintWriter out = new PrintWriter(new FileWriter(nomeArquivo))) {
            out.println("FOLHA DE PAGAMENTO DO DIA " + data.toString());
            out.println("====================================");
            out.println();
            // HORISTAS
            out.println("===============================================================================================================================");
            out.println("===================== HORISTAS ================================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo");
            out.println("==================================== ===== ===== ============= ========= =============== ======================================");
            double hBruto = 0, hDesc = 0, hLiq = 0, hHoras = 0, hExtra = 0;
            for (EmpregadoHorista h : horistas) {
                LocalDate inicio = (h.getDataUltimoPagamento() == null) ? getInicioPeriodo(h, data) : h.getDataUltimoPagamento();
                double[] horas = h.getHorasTrabalhadas(inicio.plusDays(1), data.plusDays(1));
                double bruto = h.calcularSalarioBruto(data, inicio);
                double deducoesCalc = calcularDeducoes(h, data);
                double descontoImpresso = (bruto < deducoesCalc) ? bruto : deducoesCalc;
                double liquido = bruto - descontoImpresso;
                hHoras += horas[0]; hExtra += horas[1]; hBruto += bruto; hDesc += descontoImpresso; hLiq += liquido;
                out.printf("%-36s %5s %5s %13s %9s %15s %s%n", h.getNome(), formatarDouble(horas[0]).replace(",00", ""), formatarDouble(horas[1]).replace(",00", ""), formatarDouble(bruto), formatarDouble(descontoImpresso), formatarDouble(liquido), getMetodoPagamentoFormatado(h));
                if (bruto > 0) h.limparDadosPagamento(data);
            }
            out.println();
            out.println(String.format("%-36s %5s %5s %13s %9s %15s", "TOTAL HORISTAS", formatarDouble(hHoras).replace(",00", ""), formatarDouble(hExtra).replace(",00", ""), formatarDouble(hBruto), formatarDouble(hDesc), formatarDouble(hLiq)));
            out.println();
            // ASSALARIADOS
            out.println("===============================================================================================================================");
            out.println("===================== ASSALARIADOS ============================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                                             Salario Bruto Descontos Salario Liquido Metodo");
            out.println("================================================ ============= ========= =============== ======================================");
            double aBruto = 0, aDesc = 0, aLiq = 0;
            for (EmpregadoAssalariado a : assalariados) {
                LocalDate inicio = (a.getDataUltimoPagamento() == null) ? getInicioPeriodo(a, data) : a.getDataUltimoPagamento();
                double bruto = a.calcularSalarioBruto(data, inicio);
                double deducoesCalc = calcularDeducoes(a, data);
                double descontoImpresso = (bruto < deducoesCalc) ? bruto : deducoesCalc;
                double liquido = bruto - descontoImpresso;
                aBruto += bruto; aDesc += descontoImpresso; aLiq += liquido;
                out.printf("%-48s %13s %9s %15s %s%n", a.getNome(), formatarDouble(bruto), formatarDouble(descontoImpresso), formatarDouble(liquido), getMetodoPagamentoFormatado(a));
                if (bruto > 0) a.limparDadosPagamento(data);
            }
            out.println();
            out.printf("TOTAL ASSALARIADOS                               %13s %9s %15s%n", formatarDouble(aBruto), formatarDouble(aDesc), formatarDouble(aLiq));
            out.println();
            // COMISSIONADOS
            out.println("===============================================================================================================================");
            out.println("===================== COMISSIONADOS ===========================================================================================");
            out.println("===============================================================================================================================");
            out.println("Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo");
            out.println("===================== ======== ======== ======== ============= ========= =============== ======================================");
            double cFixo = 0, cVendas = 0, cComissao = 0, cBruto = 0, cDesc = 0, cLiq = 0;
            for (EmpregadoComissionado c : comissionados) {
                LocalDate inicio = (c.getDataUltimoPagamento() == null) ? getInicioPeriodo(c, data) : c.getDataUltimoPagamento();
                double vendas = c.getVendasRealizadas(inicio.plusDays(1), data.plusDays(1));

                double bruto = c.calcularSalarioBruto(data, inicio);

                String agenda = c.getAgendaPagamento();
                double fixo;
                if (agenda.startsWith("mensal")) fixo = c.getSalarioMensal();
                else {
                    long weeks = ChronoUnit.WEEKS.between(inicio, data);
                    if (weeks == 0) weeks = 1;
                    fixo = truncar(c.getSalarioMensal() * 12.0 / 52.0 * weeks);
                }

                double comissao = truncar(vendas * c.getTaxaDeComissao());
                double deducoesCalc = calcularDeducoes(c, data);
                double descontoImpresso = (bruto < deducoesCalc) ? bruto : deducoesCalc;
                double liquido = bruto - descontoImpresso;
                cFixo += fixo; cVendas += vendas; cComissao += comissao; cBruto += bruto; cDesc += descontoImpresso; cLiq += liquido;
                out.printf("%-21s %8s %8s %8s %13s %9s %15s %s%n", c.getNome(), formatarDouble(fixo), formatarDouble(vendas), formatarDouble(comissao), formatarDouble(bruto), formatarDouble(descontoImpresso), formatarDouble(liquido), getMetodoPagamentoFormatado(c));
                if (bruto > 0) c.limparDadosPagamento(data);
            }
            out.println();
            out.printf("TOTAL COMISSIONADOS   %8s %8s %8s %13s %9s %15s%n", formatarDouble(cFixo), formatarDouble(cVendas), formatarDouble(cComissao), formatarDouble(cBruto), formatarDouble(cDesc), formatarDouble(cLiq));
            out.println();
            out.println("TOTAL FOLHA: " + formatarDouble(hBruto + aBruto + cBruto));
        }
    }
}