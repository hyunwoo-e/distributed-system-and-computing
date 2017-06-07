package node;

import java.util.*;
public class KMP {
	private final int MAX = 100;
	private int[] pi;
	
	public String text;
	public String pattern;
	public ArrayList<Integer> result;
	
	public KMP(String text, String pattern) {
		this.text = text;
		this.pattern = pattern;
		this.pi = new int[MAX];
	}
	
	/* pi�� ���ϱ� */
	public void make_pi() {
		pi[0] = 0;
		for(int i = 1, j = 0 ; i < pattern.length() ; i++) {
			while(j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
			if (pattern.charAt(i) == pattern.charAt(j)) {
				j++;
				
			}
			pi[i] = j;
		}
	}
	
	public ArrayList<Integer> find_index() {
		result = new ArrayList<Integer>();
		pi = new int[MAX];
		
		make_pi();
		
		for(int i = 0, j = 0  ; i < text.length() ; i++) {
			/* ������ Ʋ�� ��� pi���� �ش��ϴ� �ε������� �ٽ� ������ �˻� */ 
			/* ������ ó������ �ٽ� �˻����� �����Ƿ�, �������� ����� ������ */
			while(j > 0 && text.charAt(i) != pattern.charAt(j))
				j = pi[j - 1];
			
			/* ���� ���ڰ� ��ġ�� ��� */
			if(text.charAt(i) == pattern.charAt(j)) {
				/* ���� ��� ��ġ�� ��� ����Ʈ�� �ε����� �߰� */
				if(j == pattern.length() -1) {
					result.add(i - pattern.length() + 1);
					j = pi[j];
				}
				/* ������ ���� �ε������� ��ġ�� ��� ���� ������ �ε����� �̵� */
				else
					j++;
			}
		}
		
		return result;
	}
}
