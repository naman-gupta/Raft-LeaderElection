package com.raft;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

/**
 * Sends Heart beats in parallel.
 */
public class HeartBeatManager extends Thread {
	protected Debug logger;
	protected List<RaftServer> servers;
	protected List<HeartBeatRequester> requests;
	protected JsonObject message;
	protected RaftServer caller;

	public HeartBeatManager(RaftServer caller, List<RaftServer> servers) {
		// super(caller, servers);
		this.logger = new Debug("HEAR-BEATMANAGER", Debug.DEBUG, System.out);
		JSonHelper hp = new JSonHelper();

		JsonObject requestMessage = hp.makeHearbeatMessage(caller.currentTerm,
				caller.serverId);
		this.message = requestMessage;
		requests= new ArrayList<HeartBeatRequester>();
		this.caller= caller;
		this.servers=servers;
	}

	public void run() {
		try {
			for (RaftServer rs : servers) {
				// not to send to myself!
				if (rs.serverId != caller.serverId) {
					HeartBeatRequester t = new HeartBeatRequester(this,
							"localhost", rs.port, this.message,caller.serverId,rs.serverId);
					requests.add(t);
					t.start();
				}
			}
		} catch (Exception e) {
			// logger.debug("Error during request: " + e.toString());
		}
		// logger.debug("Exiting requester thread.");
	}

}

class HeartBeatRequester extends Thread {

	int port;
	String address;
	JsonObject rq;
	protected Debug logger;
	HeartBeatManager vc;
	int senderId,callerId;

	public HeartBeatRequester(HeartBeatManager vc, String address,
			int port, JsonObject rq,int senderId,int callerId) {
		this.vc = vc;
		this.logger = new Debug("REQ-MANAGER", Debug.DEBUG,System.out);
		this.port = port;
		this.rq = rq;
		this.address = address;
		this.senderId = senderId;
		this.callerId = callerId;
	}

	@Override
	public void run() {
		new JSonHelper().sendMessageToClient(address, port, rq,senderId,callerId);
	}
}
