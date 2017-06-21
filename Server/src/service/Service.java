package service;

import java.io.*;
import java.net.*;
import java.util.*;

import server.ServerInfo;

public class Service{
	public String requestAddress;
	public HashSet<String> result;
	public HashSet<Integer> done;
	public int taskCount;
	public String command;
	public String arg;
	
	public Service(String requestAddress, int taskCount, String command, String arg) {
		this.requestAddress = requestAddress;
		this.taskCount = taskCount;
		this.command = command;
		this.arg = arg;
		result = new HashSet<String>();
		done = new HashSet<Integer>();
	}	
}
