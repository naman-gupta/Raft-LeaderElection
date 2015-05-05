package com.raft;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.eclipsesource.json.JsonObject;

/*
 * Class to help making json constructions
 */
class JSonHelper {

	public JSonHelper() {

	}

	/**
	 * Returns a vote to a candidate
	 * 
	 * @param term
	 *            : Current Term
	 * @param voteGranted
	 *            : Yes or No
	 * @return
	 */
	public JsonObject resultVote(int term, int voteGranted) {
		return new JsonObject().add("term", term).add("voteGranted",
				voteGranted);
	}

	/**
	 * Returns a response
	 * 
	 * @param term
	 *            : Current Term
	 * @param success
	 *            : Yes or No
	 * @return
	 */
	public JsonObject resultAppend(int term, int success) {
		return new JsonObject().add("term", term).add("success", success);
	}

	/**
	 * Returns request vote message
	 * 
	 * @param term
	 *            : Candidates Term
	 * @param candidateId
	 *            : candidate requesting vote
	 * @return
	 */
	public JsonObject makeRequestVote(int term, int candidateId) {
		return new JsonObject().add("rpc-command", RaftServer.RPC_VOTE)
				.add("term", term).add("candidateId", candidateId);

	}

	/**
	 * Make a Hearbeat message.
	 * 
	 * @param term
	 *            : Candidates Term
	 * @param leaderId
	 *            : ServerId of the Leader
	 * @return
	 */
	public JsonObject makeHearbeatMessage(int term, int leaderId) {
		return new JsonObject().add("rpc-command", RaftServer.RPC_APPEND)
				.add("term", term).add("leaderId", leaderId).add("payload", "")
				.add("leaderCommit", 0);
	}

	public JsonObject sendMessageToClient(String address, int port,
			JsonObject msg,int senderId,int callerId) {
		try {
			Socket client = new Socket();
			client.connect(new InetSocketAddress(address, port));
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			out.writeUTF(msg.toString());
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			JsonObject r = com.eclipsesource.json.JsonObject.readFrom(in
					.readUTF());
			client.close();
			return r;
		} catch (IOException e) {
			return new JsonObject().add("error", e.toString());
		}
	}

}