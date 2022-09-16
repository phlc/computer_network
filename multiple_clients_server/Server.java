import java.io.*;
import java.net.*;


/*
 * Event Class - Define objeto evento
 */
class Event{
  //Object Field
  private String name;
  private String city;
  private int available; //número de ingressos disponíveis

  //Constructor
  Event(String name, String city, int available){
    this.name = name;
    this.city = city;
    this.available = available;
  }

  //gets
  public int getAvailable(){
    return this.available;
  }
  public String getName() {
      return name;
  }
  public String getCity() {
      return city;
  }

  /*
   * buy - tenta realizar a compra de um ingresso do evento
   * @return true se compra realizada || false número de ingressos zerado
   */
  public boolean buy() {
    boolean ok = false;
    if(this.available>0){
      this.available--;
      ok = true;
    }
    return ok;
  }

  //toString
  @Override
  public String toString() {
      return this.name + ", " + this.city + " - Disponíveis: " + this.available;
  }
}

/*
 * Server Class - Serviço de venda de ingressos para eventos que atende um socket
 * @extends Thread 
 */
public class Server extends Thread {

  //Static Fields - Compartilhados por todos objetos Server
  private static Event[] events = new Event[5]; //Conjunto de eventos
  private static ServerSocket serverSocket;
  private static long ids = 0; //contador utilizado para gerar ids

  //Object Fields
  private Socket con;
  private long id; //id do cliente conectado a este Server
  private int[] tickets; //quantos ingressos esse cliente tem de cada evento
  private BufferedReader socketReader;
  private BufferedWriter socketWriter;

  // Constructor
  public Server(Socket con){
    this.tickets = new int[5]; //número de eventos demo
    this.id = ++ids; //id gerado a partir do contador global
    this.con = con; //socket recebido na criação do Server
    try {
      socketReader = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
      socketWriter = new BufferedWriter(new OutputStreamWriter(this.con.getOutputStream()));
    
      //Primeira mensagem - informa id para o cliente
      socketWriter.write(this.id + "\n");
      socketWriter.flush();
    }catch (Exception e) {}
  }

  /*
   * listAvailables - Envia para o cliente tela dos eventos disponíveis
   * @param error - Variável de controle - se true significa que o metodo
   *     foi chamado a partir de um erro de input do usuário - envia tela
   *     com mensagens adicionais de erro   
   */
  private void listAvailables(boolean error) throws IOException{
    socketWriter.write("\033[H\033[2J\n");
    socketWriter.flush();
    socketWriter.write("Nossos Eventos Disponíveis:\n");

    //Verifica eventos disponíveis
    for(int i=0; i<events.length; i++){
      if(events[i].getAvailable() > 0){
        socketWriter.write((i+1) + "- " + events[i].toString() + "\n");
      }
    }
    socketWriter.write("Para Comprar Digite o Número do Evento e aperte ENTER\n");
    socketWriter.write("Para Ver seus Eventos digite \"Liste\" e aperte ENTER\n");
    socketWriter.write("Para Sair digite \"Sair\" e aperte ENTER\n");
    socketWriter.flush();

    //Mensagens adicionais quanto houve erro de input do usuário
    if(error){
      socketWriter.write("\nVocê deve digitar:\n");
      socketWriter.write("- O número do evento\n");
      socketWriter.write("- \"Liste\" ou\n- \"Sair\"\nTente Novamente:\n");
      socketWriter.flush();
    }
  }

  /*
   * buy - tenta realizar uma compra de um ingresso
   * @paran número do evento
   * @return true se compra realizar || false
   * @synchronized - método só é executado por um Server de cada vez para 
   *                  evitar condições de corrida
   */
  private synchronized boolean buy(int event){
    boolean ok = false;
    if(0<event && event<=events.length){
      ok = events[event-1].buy();
      if(ok) this.tickets[event-1]++;
    }
    return ok;
  }

  /*
   * listMyEvents - Envia para o clinte tela com seus eventos
   */
  private void listMyEvents() throws IOException{
    socketWriter.write("\033[H\033[2J\n");
    socketWriter.flush();
    socketWriter.write("Seus Eventos:\n");
    for(int i=0; i<tickets.length; i++){

      //Se o cliente tem ingresso
      if(tickets[i] > 0){
        socketWriter.write("Você tem "+tickets[i]+" ingresso(s) do evento "
                        +events[i].getName() + " em " + events[i].getCity()+"\n");
      }
    }
    socketWriter.write("Aperter ENTER para voltar a tela inicial\n");
    socketWriter.flush();
  }


  /*
   * run - Implementação do metodo run() de Threads
   *       Método que é executado por cada thread após .start()
   */
  public void run(){
    String inMsg = null;  
    boolean error = false;  
    try{
      //Ler a primeira mensagem do cliente - .readLine() bloqueia a execução
      inMsg = socketReader.readLine();

      //Loop principal - até cliente enviar mensagem "end"
      while(true){

        //mostrar tela de eventos disponíveis e aguardar mensagem do cliente
        listAvailables(error); 
        inMsg = socketReader.readLine();
        error = false;

        //Mensagem recebida é para listar eventos do cliente
        if("liste".equalsIgnoreCase(inMsg)){
          listMyEvents();
          inMsg = socketReader.readLine();
        }

        //Mensagem recebida é para encerrar conexão 
        else if("end".equalsIgnoreCase(inMsg)){
          break;
        }

        //Demais mensagens - pressupoe um inteiro correspondente ao número do evento
        else{ 

          //Executado entro do try para controle da entrada como número 
          //inteiro a partir do método parseInt
          try{
            int event = Integer.parseInt(inMsg);
            socketWriter.write("\033[H\033[2J\n"); //limpa tela
            socketWriter.flush();

            //Compra com sucesso
            if(this.buy(event)){
              socketWriter.write("Parabéns. Compra realizada com sucesso.\n");
            }

            //Evento inexistente ou com número de ingressos zerado
            else{
              socketWriter.write("Desculpe. Evento Indisponível.\n");
            }

            socketWriter.write("Aperter ENTER para voltar a tela inicial\n");
            socketWriter.flush();

            //Novamente, aguarda um novo input do cliente
            inMsg = socketReader.readLine();
          }
          
          //Se a entrada não erra um inteiro na próxima iteração lista eventos
          // com a mensagem adicional de erro
          catch(NumberFormatException e){
            error = true;
          }
        }
      }

      //Finaliza conexão após break do loop por receber mensagem "end"
      this.socketReader.close();
      this.socketWriter.close();
      this.con.close();
      System.out.println("Cliente "+ this.id + " desconectado.");

    }catch (Exception e) {}
  }//end método run




  /*
   * main - Cria eventos e inicializa servidor de sockets
   */
  public static void main(String []args) {
    int port = 12345;

    //Eventos demo
    events[0] = new Event("Cruzeiro x Criciúma", "Belo Horizonte", 10);
    events[1] = new Event("Show Skank", "Belo Horizonte", 2);
    events[2] = new Event("Rock in Rio", "Rio de Janeiro", 3);
    events[3] = new Event("U2", "Nova Iorque", 4);
    events[4] = new Event("2Cello", "Roma", 2);


    try{
      serverSocket = new ServerSocket(port);
      System.out.println("Servidor ativo na porta: " + port);

      //Loop até que o programa seja interrompido
      while(true){
        System.out.println("Aguardando conexão...");

        //serverSocket aguarda uma nova conexão
        Socket con = serverSocket.accept();
        System.out.println("Cliente conectado...");

        //realizada uma nova conexão um novo objeto da classe Server
        // é criado recebendo o socket dessa nova conexão como parâmetro
        // então é criada uma thread para que roda o método run para aquele
        // Server que corresponde a uma conexão com um cliente específico.
        // Após a criação da Thread desse novo Server, a Thread principal
        // permanece em loop aguardando novas conexões e criando novas threads
        Thread t = new Server(con);
          t.start();
      }
    }
    catch (Exception e){}
  }
}