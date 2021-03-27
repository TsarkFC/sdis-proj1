package channels;

import messages.CoordMessage;
import messages.Stored;
import peer.Peer;
import utils.AddressList;

import java.net.DatagramPacket;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ControlChannel extends Channel {

    public ControlChannel(AddressList addressList, Peer peer){
        super(addressList, peer);
        super.currentAddr = addressList.getMcAddr();
    }

    @Override
    public void handle(DatagramPacket packet) {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Control Channel received MBD Msg: " + rcvd);
        parseMsg(rcvd);

    }

    public void parseMsg(String msgString){
        //Stored msg = new Stored(msgString);
        //Guardar em memoria nao volatil a quantidade de mensagens stored que recebeu de cada chunk



        //Ele aqui tem que receber as mensagens durante 1 segundo, se o rep degree for mais pequeno
        //Entao ele envia outra vez mensagem de putchunk
        //Esta ativo durante 1 segundo depois de ter sido enviada o chunk,
        //Depois continua a executar normalmente? ou pode ficar em wait?
        //Ele teria que consguir depois desse um segundo verificar se o numero de mensagens que recebeu era sufucuentemente alto
        //Caso nao seja, teria que repetir o processo um maximo de 5 vezes.

        //Funciona, mas talvez ele fique aqui preso
        //Talvez começar uma thread que faça wait pelo outro?
        //Talvez para isso mais valia ter uma classe central para os dois

        while (true){
            double timeSinceBackup = peer.getTimer().getElapsedTimeInSeconds();
            if (timeSinceBackup==-1) break;
            if(timeSinceBackup!=-1 && timeSinceBackup > 1){
                System.out.println("1 second has passed");
                //if stordemsgs<= repDeg
                //Enviar ao backup channel para enviar outra vez, a mensagem de putchunk
                //e espera o dobro do intervalo
                //Caso stored messafe <=repDegree  //Como conseguimos ter acesso ao rep degree? a mensagem stored ÑAO TEM O REP DEGREE
                    //Enviar ao backup channel para enviar outra vez, a mensagem de putchunk
                    //e espera o dobro do intervalo
                //else ta tudo bem
                break;
            }
        }



        //msg.getReplicationDeg();
        //System.out.println(msg.getMessageType());
    }




}
