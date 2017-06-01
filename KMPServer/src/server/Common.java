package server;

import java.util.*;
import manager.*;

public class Common {	
	public static String coordinator = "";
	public static boolean isAlive = false;
	
	public static ArrayList<String> list;
	public static String ip;
	public static int index;
	
	public static HashMap<String, Integer> nodes;
	public static int nodeCount;
	
	public static ElectionManager em;
	public static NodeManager nm;
	public static ResourceManager rm;
}
