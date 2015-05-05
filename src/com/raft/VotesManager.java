package com.raft;

import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

public class VotesManager extends Thread {

	/**
	 * How many votes did I get.
	 */
	public int votes = 0;

	/**
	 * 
	 */
	public int majority;

	/**
	 * For Logging.
	 */
	protected Debug logger;

	protected List<VoteRequester> requests;

	protected List<RaftServer> servers;

	protected JsonObject message;

	private int serverId;

	protected RaftServer caller;

	public VotesManager(RaftServer caller, List<RaftServer> servers,
			int majority) {
		super("Votes Collector");
		this.logger = new Debug("Votes-Collector", Debug.DEBUG, System.out);
		this.servers = servers;
		this.caller = caller;
		this.serverId = caller.serverId;
		this.requests = new ArrayList<VoteRequester>();
		this.servers = servers;
		this.majority = majority;

		// make a requestVote message
		JsonObject rq = new JSonHelper().makeRequestVote(caller.currentTerm,
				caller.serverId);

		logger.debug("Inside Vote Manager " + caller.getServerId());
		this.message = rq;
		votes++; // Vote for myself
		caller.votedFor = caller.getServerId();

		// elections timeout
		/*
		 * this.timer.schedule(etTimeoutTask, this.ELECTIONS_TIMEOUT,
		 * this.ELECTIONS_TIMEOUT);
		 */
	}

	public void run() {
		try {
			for (RaftServer rs : servers) {
				// not to send to myself!
				if (rs.serverId != this.serverId) {
					VoteRequester t = new VoteRequester(this, "localhost",
							rs.port, this.message);
					requests.add(t);
					t.start();
				}
			}
		} catch (Exception e) {
			// logger.debug("Error during request: " + e.toString());
		}
		// logger.debug("Exiting requester thread.");
	}

	// a vote requester sets vote
	public void setVote(int term, boolean voteGranted) {
		// synchronize because multiple threads can access to this section at
		// the same time
		if (voteGranted)
			this.votes++;
		
		// if we reached majority
		if (this.votes == this.majority && caller.getStatus().equals("CANDIDATE")) {
			logger.debug("Win!! I'm the new leader!! -->" + caller.getServerId()
					+ "  " + votes);
			caller.setStatus(RaftServer.STATE_LEADER);
			caller.sendHeartBeat();
			caller.votedFor = caller.getServerId();
			// interrupt all senders
			interruptSenders();
		}
	}

	protected void interruptSenders() {
		for (VoteRequester rt : this.requests) {
			rt.interrupt();
		}
	}
}

class VoteRequester extends Thread {

	int port;
	String address;
	JsonObject rq;
	protected Debug logger;
	VotesManager vc;

	public VoteRequester(VotesManager vc, String address, int port,
			JsonObject rq) {
		this.vc = vc;
		this.logger = new com.raft.Debug("REQ-MANAGER", Debug.DEBUG,
				System.out);
		this.port = port;
		this.rq = rq;
		this.address = address;
	}

	@Override
	public void run() {
		// // logger.debug("Sending requestvote to " + port);

		JsonObject resp = new JSonHelper().sendMessageToClient(address, port, rq,0,0);

		// logger.debug("Response to requestvote is " + resp.toString());
		if (resp.get("error") != null) {
			// logger.debug("Error from requestvote");
		} else {
			if (resp != null && !resp.get("term").isNull()
					&& !resp.get("voteGranted").isNull())
				vc.setVote(resp.get("term").asInt(), resp.get("voteGranted")
						.asInt() != 0);
		}
	}

}