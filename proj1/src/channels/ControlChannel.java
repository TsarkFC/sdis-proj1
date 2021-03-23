package channels;

import messages.Message;
import messages.PutChunk;
import messages.Stored;
import utils.MulticastAddress;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ControlChannel extends Channel {


    public ControlChannel(MulticastAddress mcAddr){
        super(mcAddr);
    }

    @Override
    public void handle(DatagramPacket packet) {
        String rcvd = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Control Channel received MBD Msg: " + rcvd);
        parseMsg(rcvd);
    }

    public void parseMsg(String msgString){
        Stored msg = new Stored(msgString);
        //Ele aqui tem que receber as mensagens durante 1 segundo, se o rep degree for mais pequeno
        //Entao ele envia outra vez mensagem de putchunk
        //Esta ativo durante 1 segundo depois de ter sido enviada o chunk,
        //Depois continua a executar normalmente? ou pode ficar em wait?
        //Ele teria que consguir depois desse um segundo verificar se o numero de mensagens que recebeu era sufucuentemente alto
        //Caso nao seja, teria que repetir o processo um maximo de 5 vezes.

        msg.getReplicationDeg();
        System.out.println(msg.getMessageType());
    }



}
