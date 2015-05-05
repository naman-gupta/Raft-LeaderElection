package com.raft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

public class RaftServerPool extends TimerTask {
	private int numberOfServers;
	private List<RaftServer> ServerPool; // List of Servers
	private static Debug logger = new com.raft.Debug("RAFTSERVERPOOL",
			Debug.DEBUG, System.out);

	private int caseNum = 0;

	boolean flag = true;

	int count = 0;
	int temp = 0;

	public RaftServerPool(Properties props, int caseNum) {

		this.ServerPool = new ArrayList<RaftServer>();
		this.numberOfServers = Integer.parseInt(props.getProperty("NumServer"));
		this.caseNum = caseNum;
		// creation of servers
		logger.debug("Creating " + numberOfServers + " servers");
		List<Integer> al = new ArrayList<Integer>();
		al.add(800);
		al.add(1400);
		al.add(2000);
		al.add(1600);
		al.add(2500);
		al.add(1900);
		al.add(2800);
		al.add(3000);
		al.add(3200);
		al.add(3500);
		al.add(3800);
		Collections.shuffle(al);
		for (int index = 1; index <= numberOfServers; index++) {
			try {
				int port = Integer.parseInt(props.getProperty("port" + index));
				System.out.println(al.get(index));
				RaftServer t = new RaftServer(index, port, this, al.get(index),
						al.get(index) / 5, caseNum);
				ServerPool.add(t);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.debug("Servers created");
	}

	public void run() {
		this.printStatus();
	}

	public int getMajorityNumber() {
		int maj = this.numberOfServers / 2;

		// if the number is odd we add up one more to get majority
		if ((this.numberOfServers & 1) != 0)
			maj++;

		return maj;
	}

	// start pool
	public void start() {

		for (RaftServer rs : ServerPool) {
			rs.start();
			// logger.debug("Server number "+rs.getNumber()+" started");
		}
		// this.status();
	}

	int counter = 0;
	// Prints the status of each server
	RaftServer rs1 = null;

	private synchronized void printStatus() {
		temp++;
		switch (caseNum) {
		case 2:
			testCase2();
			break;
		case 3:
			testCase3();
			break;
		case 4:
			testCase4();
			break;
		case 5:
			testCase5();
			break;
		case 6:
			testCase6();
		}

		logger.debug("Reporting pool status");
		logger.debug("=============================");
		for (RaftServer rs : ServerPool) {
			logger.debug("Server number " + rs.getServerId() + " is "
					+ rs.getStatus());

		}

		logger.debug("=============================");
	}

	/**
	 * Test Case 2 : Kill 1 Server.
	 */
	public synchronized void testCase2() {
		for (RaftServer rs : ServerPool) {
			if (rs.getStatus().equals("LEADER") && flag && temp==10) {
				logger.debug("Killing Leader --->  " + rs.getServerId());
				rs.setStatus(3);
				logger.debug("Killing Leader --->  " + rs.getStatus());
				flag = false;
			}

		}
	}

	/**
	 * Test Case 3 : Kill more than 2 servers.
	 */
	public synchronized void testCase3() {
		for (RaftServer rs : ServerPool) {
			if (rs.getStatus().equals("LEADER") && count < 3 && (temp==8||temp==12|| temp>15)) {
				logger.debug("Killing Leader --->  " + rs.getServerId());
				rs.setStatus(3);
				count++;
				logger.debug("Killing Leader --->  " + rs.getStatus());
			}

		}
	}

	/**
	 * Test Case 4 : One Leader Goes Down and Comes Again as a follower.
	 */
	public synchronized void testCase4() {
		for (RaftServer rs : ServerPool) {
			if (rs.getStatus().equals("LEADER") && flag && temp==8) {
				logger.debug("Killing Leader --->  " + rs.getServerId());
				rs.setStatus(3);
				logger.debug("Killing Leader --->  " + rs.getStatus());
				flag = false;
			}

			if (temp == 12 && rs.getStatus().equals("ERROR")) {
				rs.setStatus(1);
			}

		}
	}

	/**
	 * Test Case 5 : More than Two Leader goes down and comes back as a follower.
	 */
	public synchronized void testCase5() {
		for (RaftServer rs : ServerPool) {
			if (rs.getStatus().equals("LEADER") && count < 3) {
				logger.debug("Killing Leader --->  " + rs.getServerId());
				rs.setStatus(3);
				count++;
				logger.debug("Killing Leader --->  " + rs.getStatus());
			}

			if (rs.getStatus().equals("ERROR")
					&& (temp == 15 || temp == 20 || temp == 25)) {
				rs.setStatus(1);
				temp++;
			}
		}

	}
	
	
	/**
	 * Test Case 6 : One Leader Goes Down and Comes back as a leader.
	 */
	public synchronized void testCase6() {
		for (RaftServer rs : ServerPool) {
			if (rs.getStatus().equals("LEADER") && flag) {
				logger.debug("Killing Leader --->  " + rs.getServerId());
				rs.setStatus(3);
				logger.debug("Killing Leader --->  " + rs.getStatus());
				flag = false;
			}

			if (temp == 10 && rs.getStatus().equals("ERROR")) {
				rs.setStatus(0);
			}

		}
	}

	// returns the List of the servers
	public List<RaftServer> getServers() {
		return ServerPool;
	}

}
