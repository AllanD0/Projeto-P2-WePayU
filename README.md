# WePayU - Sistema de Folha de Pagamento

O **WePayU** é um sistema de gestão de folha de pagamento desenvolvido como parte da disciplina de Programação 2 (P2). O objetivo do sistema é gerenciar o pagamento de empregados de uma empresa, lidando com diferentes tipos de salários, comissões, taxas sindicais e agendas de pagamento, tudo isso controlado por uma lógica de negócio robusta sem interface gráfica (GUI).

## 📋 Sobre o Projeto

O sistema foi construído seguindo uma abordagem incremental baseada em **User Stories**. A validação do sistema é feita através de testes de aceitação automatizados utilizando o framework **EasyAccept**.

O sistema suporta:
* Gerenciamento de empregados (Horistas, Assalariados e Comissionados).
* Cálculos automáticos de folha de pagamento.
* Gestão de sindicatos e taxas de serviço.
* Funcionalidade de **Undo/Redo** (Desfazer/Refazer).
* Agendas de pagamento flexíveis e customizáveis.

## 🚀 Funcionalidades (User Stories)

O projeto implementa as seguintes funcionalidades principais:

1.  **Adição de Empregados**: Suporte a empregados Horistas, Assalariados e Comissionados.
2.  **Remoção de Empregados**: Exclusão de registros do sistema.
3.  **Lançamento de Cartão de Ponto**: Registro de horas para empregados horistas (incluindo cálculo de horas extras).
4.  **Lançamento de Vendas**: Registro de vendas para empregados comissionados.
5.  **Lançamento de Taxas de Serviço**: Associação de taxas sindicais extras a membros do sindicato.
6.  **Alteração de Detalhes**: Edição de atributos do empregado (nome, endereço, tipo, método de pagamento, vínculo sindical).
7.  **Rodar a Folha de Pagamento**: Cálculo do salário líquido considerando descontos e datas de pagamento.
8.  **Undo/Redo**: Capacidade de desfazer e refazer qualquer transação de alteração de estado.
9.  **Agenda de Pagamento**: Suporte a agendas padrões ("semanal", "mensal", "bi-semanal").
10. **Criação de Agendas Customizadas**: Flexibilidade para criar novas agendas (ex: "mensal 1", "semanal 2 5").

## 🛠️ Tecnologias Utilizadas

* **Linguagem**: Java
* **Testes de Aceitação**: EasyAccept
* **Persistência**: Arquivos XML (`java.beans.XMLEncoder` / `XMLDecoder`) e Serialização Nativa para Undo/Redo.
* **Padrão de Projeto**: Facade (Controlador principal) e Singleton (implícito na gestão do sistema).

## 📂 Estrutura do Projeto

A estrutura de pacotes principal é:

* `br.ufal.ic.p2.wepayu.Controller`: Contém a `Facade`, que é a porta de entrada para todas as operações do sistema.
* `br.ufal.ic.p2.wepayu.model`: Contém as classes de domínio (`Empregado`, `SistemaFolha`, `Venda`, `CartaoPonto`, etc.).
* `br.ufal.ic.p2.wepayu.Exception`: Exceções personalizadas do sistema.

## ▶️ Como Executar

O projeto não possui interface gráfica. A execução é realizada através da classe `Main`, que roda os scripts de teste do EasyAccept localizados na pasta `WePayU/tests/`.

Para rodar o projeto e verificar os testes:

1.  Certifique-se de ter o Java JDK instalado.
2.  Compile o projeto.
3.  Execute a classe `Main`.

A classe `Main` está configurada para executar a bateria de testes das User Stories (US1 a US10) sequencialmente.

```java
// Exemplo do fluxo no Main.java
EasyAccept.main(new String[]{facade, "WePayU/tests/us1.txt"});
// ... outros testes ...
EasyAccept.main(new String[]{facade, "WePayU/tests/us10_1.txt"});
