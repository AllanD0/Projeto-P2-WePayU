package br.ufal.ic.p2.wepayu.Controller;

import br.ufal.ic.p2.wepayu.model.SistemaFolha;
import java.util.Stack;

public class Facade {

    private SistemaFolha sistema;
    private Stack<byte[]> undoStack = new Stack<>();
    private Stack<byte[]> redoStack = new Stack<>();
    private boolean sistemaEncerrado = false;

    public Facade() {
        sistema = new SistemaFolha();
        try {
            sistema.carregarEstado();
        } catch (Exception e) {
            System.out.println("Aviso: Nao foi possivel carregar o estado anterior. Iniciando novo sistema.");
            sistema.zerarSistema(); // Garante estado limpo se falhar
        }
    }

    private void preservaEstado() {
        undoStack.push(sistema.getSnapshot());
        redoStack.clear();
    }

    private void desfazAlteracaoRecente() {
        if (!undoStack.isEmpty()) {
            undoStack.pop();
        }
    }

    public void undo() throws Exception {
        if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        if (undoStack.isEmpty()) throw new Exception("Nao ha comando a desfazer.");
        redoStack.push(sistema.getSnapshot());
        byte[] anterior = undoStack.pop();
        sistema.restaurarSnapshot(anterior);
    }

    public void redo() throws Exception {
        if (sistemaEncerrado) throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        if (redoStack.isEmpty()) throw new Exception("Nao ha comando a refazer.");
        undoStack.push(sistema.getSnapshot());
        byte[] futuro = redoStack.pop();
        sistema.restaurarSnapshot(futuro);
    }

    public void encerrarSistema() {
        sistema.salvarEstado();
        sistemaEncerrado = true;
    }

    public String getNumeroDeEmpregados() { return String.valueOf(sistema.getNumeroDeEmpregados()); }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
        int id;
        try { id = Integer.parseInt(emp); } catch (NumberFormatException e) { throw new Exception("Empregado nao existe."); }
        return sistema.getAtributoEmpregado(id, atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception { return sistema.getEmpregadoPorNome(nome, indice); }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        int id = Integer.parseInt(emp);
        double horas = sistema.getHorasNormaisTrabalhadas(id, dataInicial, dataFinal);
        return formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        int id = Integer.parseInt(emp);
        double horas = sistema.getHorasExtrasTrabalhadas(id, dataInicial, dataFinal);
        return formatarHoras(horas);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        int id = Integer.parseInt(emp);
        double totalVendas = sistema.getVendasRealizadas(id, dataInicial, dataFinal);
        return String.format("%.2f", totalVendas).replace('.', ',');
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
        int id = Integer.parseInt(emp);
        double totalTaxas = sistema.getTaxasServico(id, dataInicial, dataFinal);
        return String.format("%.2f", totalTaxas).replace('.', ',');
    }

    public String totalFolha(String data) throws Exception {
        double total = sistema.totalFolha(data);
        return String.format("%.2f", total).replace('.', ',');
    }

    private String formatarHoras(double horas) {
        if (horas == (long) horas) return String.format("%d", (long) horas);
        return String.format("%.1f", horas).replace('.', ',');
    }

    public void zerarSistema() {
        preservaEstado();
        try { sistema.zerarSistema(); } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        preservaEstado();
        try { sistema.criarAgendaDePagamentos(descricao); } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr) throws Exception {
        preservaEstado();
        try {
            if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
            if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
            if (salarioStr == null || salarioStr.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
            double salario;
            try { salario = Double.parseDouble(salarioStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
            if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");
            int id;
            if ("horista".equalsIgnoreCase(tipo)) id = sistema.adicionarEmpregadoHorista(nome, endereco, salario);
            else if ("assalariado".equalsIgnoreCase(tipo)) id = sistema.adicionarEmpregadoAssalariado(nome, endereco, salario);
            else if ("comissionado".equalsIgnoreCase(tipo)) throw new Exception("Tipo nao aplicavel.");
            else throw new Exception("Tipo invalido.");
            return String.valueOf(id);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr, String comissaoStr) throws Exception {
        preservaEstado();
        try {
            if (nome == null || nome.isEmpty()) throw new Exception("Nome nao pode ser nulo.");
            if (endereco == null || endereco.isEmpty()) throw new Exception("Endereco nao pode ser nulo.");
            if (salarioStr == null || salarioStr.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
            if (comissaoStr == null || comissaoStr.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
            if (!"comissionado".equalsIgnoreCase(tipo)) throw new Exception("Tipo nao aplicavel.");
            double salario;
            try { salario = Double.parseDouble(salarioStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
            double comissao;
            try { comissao = Double.parseDouble(comissaoStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Comissao deve ser numerica."); }
            if (salario < 0) throw new Exception("Salario deve ser nao-negativo.");
            if (comissao < 0) throw new Exception("Comissao deve ser nao-negativa.");
            int id = sistema.adicionarEmpregadoComissionado(nome, endereco, salario, comissao);
            return String.valueOf(id);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void removerEmpregado(String emp) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); } catch (NumberFormatException e) { throw new Exception("Empregado nao existe."); }
            sistema.removerEmpregado(id);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void lancaCartao(String emp, String data, String horasStr) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); } catch (NumberFormatException e) { throw new Exception("Empregado nao existe."); }
            double horas;
            try { horas = Double.parseDouble(horasStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Horas devem ser numericas."); }
            if (horas <= 0) throw new Exception("Horas devem ser positivas.");
            sistema.lancaCartao(id, data, horas);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void lancaVenda(String emp, String data, String valorStr) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); } catch (NumberFormatException e) { throw new Exception("Empregado nao existe."); }
            double valor;
            try { valor = Double.parseDouble(valorStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Valor deve ser numerico."); }
            if (valor <= 0) throw new Exception("Valor deve ser positivo.");
            sistema.lancaVenda(id, data, valor);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void lancaTaxaServico(String membro, String data, String valorStr) throws Exception {
        preservaEstado();
        try {
            if (membro == null || membro.isEmpty()) throw new Exception("Identificacao do membro nao pode ser nula.");
            double valor;
            try { valor = Double.parseDouble(valorStr.replace(',', '.')); }
            catch (NumberFormatException e) { throw new Exception("Valor deve ser numerico."); }
            if (valor <= 0) throw new Exception("Valor deve ser positivo.");
            sistema.lancaTaxaServico(membro, data, valor);
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); if (sistema.getEmpregado(id) == null) throw new Exception(); }
            catch (Exception e) { throw new Exception("Empregado nao existe."); }

            if ("tipo".equals(atributo)) {
                sistema.alteraEmpregadoTipo(id, valor, -1, -1);
                return;
            }

            switch (atributo) {
                case "nome": sistema.alteraNome(id, valor); break;
                case "endereco": sistema.alteraEndereco(id, valor); break;
                case "sindicalizado":
                    if ("false".equals(valor)) sistema.removeEmpregadoDoSindicato(id);
                    else if (!"true".equals(valor)) throw new Exception("Valor deve ser true ou false.");
                    break;
                case "metodoPagamento": sistema.alteraMetodoPagamento(id, valor); break;
                case "salario":
                    if (valor == null || valor.isEmpty()) throw new Exception("Salario nao pode ser nulo.");
                    double sal;
                    try { sal = Double.parseDouble(valor.replace(',', '.')); }
                    catch (NumberFormatException e) { throw new Exception("Salario deve ser numerico."); }
                    sistema.alteraSalario(id, sal);
                    break;
                case "comissao":
                    if (valor == null || valor.isEmpty()) throw new Exception("Comissao nao pode ser nula.");
                    double com;
                    try { com = Double.parseDouble(valor.replace(',', '.')); }
                    catch (NumberFormatException e) { throw new Exception("Comissao deve ser numerica."); }
                    sistema.alteraComissao(id, com);
                    break;
                case "agendaPagamento":
                    if (!sistema.getAgendas().contains(valor)) {
                        throw new Exception("Agenda de pagamento nao esta disponivel");
                    }
                    sistema.alteraAgendaPagamento(id, valor);
                    break;
                default: throw new Exception("Atributo nao existe.");
            }
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String extra) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); if (sistema.getEmpregado(id) == null) throw new Exception(); }
            catch (Exception e) { throw new Exception("Empregado nao existe."); }

            if ("tipo".equals(atributo)) {
                double sal = -1, com = -1;
                if (extra != null && !extra.isEmpty()) {
                    try {
                        if ("horista".equalsIgnoreCase(valor) || "assalariado".equalsIgnoreCase(valor)) {
                            sal = Double.parseDouble(extra.replace(',', '.'));
                        } else if ("comissionado".equalsIgnoreCase(valor)) {
                            com = Double.parseDouble(extra.replace(',', '.'));
                        }
                    } catch (NumberFormatException e) {
                        if ("comissionado".equalsIgnoreCase(valor)) throw new Exception("Comissao deve ser numerica.");
                        else throw new Exception("Salario deve ser numerico.");
                    }
                }
                sistema.alteraEmpregadoTipo(id, valor, sal, com);
            } else { throw new Exception("Atributo nao existe."); }
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); if (sistema.getEmpregado(id) == null) throw new Exception(); }
            catch (Exception e) { throw new Exception("Empregado nao existe."); }

            if ("sindicalizado".equals(atributo) && "true".equals(valor)) {
                if (idSindicato == null || idSindicato.isEmpty()) throw new Exception("Identificacao do sindicato nao pode ser nula.");
                if (taxaSindical == null || taxaSindical.isEmpty()) throw new Exception("Taxa sindical nao pode ser nula.");
                double taxa;
                try { taxa = Double.parseDouble(taxaSindical.replace(',', '.')); }
                catch (NumberFormatException e) { throw new Exception("Taxa sindical deve ser numerica."); }
                if (taxa < 0) throw new Exception("Taxa sindical deve ser nao-negativa.");
                sistema.alteraEmpregadoSindicalizado(id, idSindicato, taxa);
            } else { throw new Exception("Atributo nao existe."); }
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        preservaEstado();
        try {
            if (emp == null || emp.isEmpty()) throw new Exception("Identificacao do empregado nao pode ser nula.");
            int id;
            try { id = Integer.parseInt(emp); if (sistema.getEmpregado(id) == null) throw new Exception(); }
            catch (Exception e) { throw new Exception("Empregado nao existe."); }

            if ("metodoPagamento".equals(atributo) && "banco".equalsIgnoreCase(valor1)) {
                if (banco == null || banco.isEmpty()) throw new Exception("Banco nao pode ser nulo.");
                if (agencia == null || agencia.isEmpty()) throw new Exception("Agencia nao pode ser nulo.");
                if (contaCorrente == null || contaCorrente.isEmpty()) throw new Exception("Conta corrente nao pode ser nulo.");
                sistema.alteraMetodoPagamento(id, banco, agencia, contaCorrente);
            } else { throw new Exception("Atributo nao existe."); }
        } catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }

    public void rodaFolha(String data, String saida) throws Exception {
        preservaEstado();
        try { sistema.rodaFolha(data, saida); }
        catch (Exception e) { desfazAlteracaoRecente(); throw e; }
    }
}