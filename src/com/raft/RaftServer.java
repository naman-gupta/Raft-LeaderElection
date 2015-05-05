package com.raft;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.eclipsesource.json.JsonObject;

/**
 * Implementation of RAFT Server.
 */

public class RaftServer extends Connection {

	/**
	 * 
	 */
	public static final int STATE_LEADER = 0;

	/**
	 * 
	 */
	public static final int STATE_FOLLOWER = 1;

	/**
	 * 
	 */
	public static final int STATE_CANDIDATE = 2;

	/**
	 * Dummy State. For Testing Purpose.
	 */
	public static final int STATE_DEAD = 3;

	/**
	 * Message for Asking Vote.
	 */
	public static final String RPC_VOTE = "RequestVote";

	/**
	 * 
	 */
	public static final String RPC_APPEND = "AppendEntries";

	/**
	 * Request From Client. Not implemented.
	 */
	public static final String CLIENT_REQUEST = "ClientRequest";

	private int electionTimeout;

	/**
	 * Time between leader's heartbeats.
	 */
	private int heartBeatPeriod;

	/**
	 * Current state of this server
	 */
	private int state;

	/**
	 * Pool that this server belongs
	 */
	private RaftServerPool pool;

	/**
	 * Id of the server
	 */
	public int serverId;

	/**
	 * Manages sending and collection of votes.
	 */
	private VotesManager vc;

	/**
	 * Helper class to make json responses
	 */
	public static JSonHelper response = new JSonHelper();

	/**
	 * Latest term server has seen
	 */
	public int currentTerm = 0;

	/**
	 * To whom I have voted for, -1 for none.
	 */
	public int votedFor = -1;

	/**
	 * 
	 */
	private Timer electionTimer = new Timer();

	/**
	 * 
	 */
	private Timer heartbeatTimer = new Timer();

	private int caseNum = 0;

	class RescheduleElectionTask extends TimerTask {
		@Override
		public void run() {
			runElections();
		}
	}

	/**
	 * Schedule the task of triggering election after Election Timeout.
	 */
	private TimerTask electionTimeoutTask = new TimerTask() {
		public void run() {
			logger.debug("Starting Elections -->" + serverId);
			runElections();
		}
	};

	/**
	 * Schedule the Task of Sending Hearbeat by the leader to the Follower.
	 */
	private TimerTask hbTimeoutTask = new TimerTask() {
		public void run() {
			sendHeartBeat();
		}
	};

	/**
	 * Timer for Testing purpose.
	 */
	private TimerTask testTimer1 = new TimerTask() {
		public void run() {
			if (state == STATE_LEADER) {
				logger.debug("\n\n\n\nChanging Leader " + serverId
						+ " to follower");
				state = STATE_DEAD;
				votedFor = -1;
				electionTimeout = 1000 + (int) (Math.random() * 2000);
				heartBeatPeriod = 200;
				electionTimer.cancel();
				// resetElectionTimeout(electionTimeoutTask,100000);
			}

		}
	};

	private TimerTask hbTimeoutTask2 = new TimerTask() {
		public void run() {
			if (state == STATE_DEAD) {
				logger.debug("\n\n\n\nREVOKING" + serverId + " to follower");
				state = STATE_FOLLOWER;
				votedFor = -1;
				electionTimer.cancel();
				resetElectionTimeout(2000);
			}

		}
	};

	/**
	 * @param number
	 *            : Server Id
	 * @param port
	 *            : Server Port
	 * @param pool
	 *            : Pointer to Server Pool
	 * @throws IOException
	 */
	public RaftServer(int number, int port, RaftServerPool pool,
			int electiontimeout, int heartBeatPeriod, int caseNum)
			throws IOException {
		super(number, port); // Set Up Connection
		this.state = STATE_FOLLOWER; // Start with Follower State
		this.serverId = number;
		this.pool = pool;
		this.electionTimeout = electiontimeout;
		this.heartBeatPeriod = 1;
		this.caseNum = caseNum;
		// schedule election timeout
		this.electionTimer.schedule(new RescheduleElectionTask(),
				this.electionTimeout, this.electionTimeout);

		// Schedule Heartbeat Timeout
		this.heartbeatTimer.schedule(hbTimeoutTask, this.heartBeatPeriod,
				this.heartBeatPeriod);
	}

	protected void process() {

		// Read Message sent by other servers i.e. Candidate or Leader.
		JsonObject message = JsonObject.readFrom(this.read());

		// For Testing
		// logger.debug("\n\n\n\nI am Server --> " +
		// this.getServerId()+"   "+this.getStatus());

		if (state == STATE_DEAD) {
			return;
		}

		if (message == null || message.get("rpc-command").isNull())
			return;

		// Extract rpc command
		String rpcCommand = message.get("rpc-command").asString();

		// I'm a follower or a candidate and I receive a hearbeat.
		if ((state == STATE_FOLLOWER || state == STATE_CANDIDATE || state==STATE_LEADER)
				&& rpcCommand.equals(RPC_APPEND)) {

			resetElectionTimeout(this.electionTimeout);

			if ("".equals(message.get("payload").asString())) {
				this.write(handleHeartbeat(message).toString());
			} else {
				// For Log Replication.
			}

		}

		// I'm a follower or a candidate and I receive a request for vote
		if ((state == STATE_FOLLOWER || state == STATE_CANDIDATE)
				&& rpcCommand.equals(RPC_VOTE)) {
			this.write(response.resultVote(
					this.currentTerm,
					this.requestVote(message.get("term").asInt(),
							message.get("candidateId").asInt()) ? 1 : 0)
					.toString());
		}

		// I'm a follower, I accept a client request and I redirect to the
		// leader
		if (state == STATE_FOLLOWER && rpcCommand.equals(CLIENT_REQUEST)) {
			// Not implemented.
		}

		// I'm the leader, I accept client operation
		if (state == STATE_LEADER && rpcCommand.equals(CLIENT_REQUEST)) {
			// Not Implemented.
		}

	}

	// Handles a leader's heartbeat
	private JsonObject handleHeartbeat(JsonObject message) {

		// reset election timeout
		// logger.debug("\n\n\nHEARBET --->"+this.electionTimeout+" "+this.number);
		// resetElectionTimeout(electionTimeoutTask, this.electionTimeout);

		// if we are on elections
		if (vc != null) {
			vc.interruptSenders();
			// stop the thread
			vc.interrupt();
			// destroy object
			vc = null;
		}

		// check the leaders term
		int leaderTerm = message.get("term").asInt();
		if (this.getStatus().equals("LEADER")) {
			//logger.debug("\n\n\nHEARBET --->"+this.serverId+" "+this.currentTerm+"  "+leaderTerm+"\n\n");
		}
		
		// update server's term
		if (currentTerm < leaderTerm) {
			currentTerm = leaderTerm;
			if (this.getStatus().equals("LEADER")) {
				this.setStatus(1);
			}
		}

		return response.resultAppend(this.currentTerm, 1);
	}

	/**
	 * @param termC
	 *            : candidate's term
	 * @param candidateId
	 *            : candidate requesting vote
	 * @return
	 */
	protected boolean requestVote(int termC, int candidateId) {

		// Check "step down" condition
		boolean result = false;

		logger.debug("Inside Vote  " + this.getServerId() + " to  "
				+ candidateId + " " + this.votedFor);
		logger.debug("New Term :" + termC + " Current Term " + this.currentTerm);

		if (termC > this.currentTerm) {
			this.currentTerm = termC;

			this.setStatus(STATE_FOLLOWER);
			this.votedFor = candidateId;
			logger.debug(this.getServerId() + "voting for " + candidateId);
			result = true;
		} else if (termC == this.currentTerm
				&& (this.votedFor == -1 || this.votedFor == this.getServerId())) {

			this.setStatus(STATE_FOLLOWER);
			this.votedFor = candidateId;
			result = true;
			logger.debug(this.getServerId() + "voting for " + candidateId);

		} else if (termC < this.currentTerm) {
			// this.votedFor = -1;
			result = false;
		}

		return result;
	}

	/**
	 * If the task election timeouts it triggers this method
	 */
	public void runElections() {

		if (state == STATE_DEAD) {
			return;
		}

		try {
			// Change the state to candidate
			// logger.debug("\n\n\nChanging "+
			// this.getStatus()+" to Candidate started by ->>>>>>>>>>>>"+
			// number);
			if (state != STATE_LEADER) {
				// logger.debug("\n\n\nElections Started by :"+this.getNumber());

				setStatus(STATE_CANDIDATE);

				// increment current term
				incrementCurrentTerm();

				// reset election timeout

				resetElectionTimeout(electionTimeout);

				// gather votes
				// setup the votes collector
				logger.debug("Asking for Votes -->" + serverId + " "
						+ this.getStatus());
				vc = new VotesManager(this, pool.getServers(),
						pool.getMajorityNumber());
				vc.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// if something goes wrong, set server as follower
			setStatus(STATE_FOLLOWER);
		}

	}

	/**
	 * Triggered by heartbeat Timer. Send heartbeats to every server if I'm the
	 * leader
	 */
	public void sendHeartBeat() {

		
		if (state == STATE_LEADER) {
			//logger.debug("\nSending heartbeats to pool by "+this.getServerId()+" "+this.getTerm() +"\n");
			// logger.debug("\nSending heartbeats to pool by ");
			HeartBeatManager hbs = new HeartBeatManager(this, pool.getServers());
			hbs.start();
		}
	}

	/*
	 * public void resetElectionTimeout(int tout) { try { sleep(tout); } catch
	 * (Exception s) { ; } }
	 */

	/**
	 * Reset Election Timeout.
	 * 
	 * @param timeout
	 *            : Timeout
	 */
	public void resetElectionTimeout(int timeout) {
		try {
			// this.electionTimeoutTask.cancel();
			// this.timer.schedule(task, timeout, timeout);
			this.electionTimer.cancel();
			// this.electionTimeoutTask.cancel();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			electionTimer = new Timer();
			electionTimer.schedule(new RescheduleElectionTask(), timeout,
					timeout);
		}

	}

	/**
	 * Sets server state
	 * 
	 * @param st
	 */
	public void setStatus(int st) {
		this.state = st;
		// this.votedFor = -1;

	}

	private void incrementCurrentTerm() {
		++this.currentTerm;
	}
	
	public int getTerm(){
		return currentTerm;
	}

	public int getServerId() {
		return this.serverId;
	}

	public String getStatus() {
		switch (this.state) {
		case STATE_LEADER:
			return "LEADER";
		case STATE_FOLLOWER:
			return "FOLLOWER";
		case STATE_CANDIDATE:
			return "CANDIDATE";
		default:
			return "ERROR";
		}
	}
}
