package com.raft;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection extends Thread
{
    protected Debug logger;
    protected int serverId;
    private ServerSocket serverSocket;
    protected Socket t_server;
    protected DataInputStream din;
    protected DataOutputStream dout;
    public static final String ERROR = "error";
    protected int port;

    public Connection(int number, int port) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.serverId=number;
        this.port=port;
        this.logger = new Debug("RAFTSERVER"+number, Debug.DEBUG, System.out);
        this.logger.debug("Server created");
    }

    public void run()
    {
        while(true)
        {
            try
            {
                // logger.debug("Waiting for client on port " +serverSocket.getLocalPort() + "...");
                t_server = this.serverSocket.accept();
                // logger.debug("Just connected to "+ this.t_server.getRemoteSocketAddress());
                din = new DataInputStream(this.t_server.getInputStream());
                dout =new DataOutputStream(this.t_server.getOutputStream());
                process();
                t_server.close();
            }catch(IOException e)
            {
                logger.error("IOException"+e.toString());
                break;
            }
        }
        logger.warning("Server down");
    }

    // Read form client
    protected String read() {
        try {
            return din.readUTF();
        }catch(IOException e)
        {
            logger.error("read"+e.toString());
            return ERROR;
        }
    }

    // Write Response to client
    protected boolean write(String response) {
        try {
            this.dout.writeUTF(response);
            return true;
        }catch(IOException e)
        {
            logger.error("write"+e.toString());
            return false;
        }
    }
    
    public int getPort()
    {
    	return this.port;
    }
    
    
    public ServerSocket getServerSocket()
    {
    	return this.serverSocket;
    }


    protected void process() {/*
        try {
            String from_client=this.read();
            this.logger.debug("Read form client: "+from_client);
            String response="You said "+from_client;
            this.write(response);
        }catch(Exception e)
        {
            logger.error("process"+e.toString());
        }
    */}
}