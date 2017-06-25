package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import server.ServerInfo;
public class KMP implements Processor {
	private final int MAX = 100;
	private int[] pi;
	
	private String command;
	private String arg;
	private int taskIdentifier;
	private int taskCount;
	public ArrayList<Integer> result;
	
	public KMP(String command, String arg, int taskIdentifier, int taskCount) {
		this.command = command;
		this.arg = arg;
		this.taskIdentifier = taskIdentifier;
		this.taskCount = taskCount;
		pi = new int[MAX];
		result = new ArrayList<Integer>();
	}
	
	/* pi값 구하기 */
	public void make_pi(String pattern) {
		pi[0] = 0;
		for(int i = 1, j = 0 ; i < pattern.length() ; i++) {
			while(j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
			if (pattern.charAt(i) == pattern.charAt(j)) {
				j++;
				
			}
			pi[i] = j;
		}
	}
	
	public void find_index(String text, String pattern, int offset) {		
		//for(int i = 0, j = 0 ; i < text.length() - MAX + pattern.length() ; i++) {
		for(int i = 0, j = 0 ; i < text.length() ; i++) {
			/* 패턴이 틀릴 경우 pi값에 해당하는 인덱스부터 다시 패턴을 검사 */ 
			/* 패턴을 처음부터 다시 검색하지 않으므로, 선형적인 결과를 가져옴 */
			while(j > 0 && text.charAt(i) != pattern.charAt(j))
				j = pi[j - 1];
			
			/* 현재 글자가 일치할 경우 */
			if(text.charAt(i) == pattern.charAt(j)) {
				/* 패턴 모두 일치할 경우 리스트에 인덱스를 추가 */
				if(j == pattern.length() -1) {
					result.add(offset + i - pattern.length() + 1);
					j = pi[j];
				}
				/* 패턴이 현재 인덱스까지 일치할 경우 다음 패턴의 인덱스로 이동 */
				else
					j++;
			}
		}
	}
	
	public ArrayList<Integer> bad_process() {
		return result;
	}
	
	public ArrayList<Integer> process() {
		char[] cbuf;
		char[] buf;
		String pattern;
		int offset, end;
		
		pattern =arg;
		make_pi(pattern);
		
		try {
			File oFile = new File(command);
			FileReader fr = new FileReader(oFile);
			BufferedReader br = new BufferedReader(fr);
			
			offset = (int) (oFile.length() * (taskIdentifier / taskCount));
			
			if(taskIdentifier + 1 == taskCount) {
				end = (int) (oFile.length() * ((taskIdentifier + 1) / taskCount) + (oFile.length() % taskCount));
			} else {
				end = (int) (oFile.length() * ((taskIdentifier + 1) / taskCount) + pattern.length());
			}
			
			int readlen = 0;
			buf = new char[MAX*2];

			int seek;
			cbuf = new char[MAX];
			for(seek = 0 ; seek+MAX < offset ; seek+=MAX) {
				readlen += br.read(cbuf, 0, MAX);
			}
			readlen += br.read(cbuf, 0, offset-seek);
			
			readlen = 0;
			int i = 0;
			System.out.println(offset + " " + end);
			for(i = 0 ; offset+MAX < end ; i++) {
				System.arraycopy(buf, MAX, buf, 0, MAX);
				cbuf = new char[MAX];
				readlen = br.read(cbuf, 0, MAX);
				System.arraycopy(cbuf, 0, buf, MAX, MAX);
				if(i>0) {
					String text = new String(buf);
					find_index(text, pattern, offset-MAX);
				}
				offset+=readlen;
			}
			
			if (i > 0) {
				System.arraycopy(buf, MAX, buf, 0, MAX);
				cbuf = new char[MAX];
				readlen = br.read(cbuf, 0, end-offset);
				System.arraycopy(cbuf, 0, buf, MAX, MAX);
				String text = new String(buf);
				find_index(text, pattern, offset-MAX);
				offset+=readlen;
			} else {
				cbuf = new char[MAX];
				readlen = br.read(cbuf, 0, end-offset);
				String text = new String(cbuf);
				find_index(text, pattern, offset);
				offset+=readlen;
			}

			br.close();
			fr.close();
		} catch (IOException e) {

		}

		return result;
	}
}
