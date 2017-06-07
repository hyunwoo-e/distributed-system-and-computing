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
	
	/* pi값 구하기 */
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
			/* 패턴이 틀릴 경우 pi값에 해당하는 인덱스부터 다시 패턴을 검사 */ 
			/* 패턴을 처음부터 다시 검색하지 않으므로, 선형적인 결과를 가져옴 */
			while(j > 0 && text.charAt(i) != pattern.charAt(j))
				j = pi[j - 1];
			
			/* 현재 글자가 일치할 경우 */
			if(text.charAt(i) == pattern.charAt(j)) {
				/* 패턴 모두 일치할 경우 리스트에 인덱스를 추가 */
				if(j == pattern.length() -1) {
					result.add(i - pattern.length() + 1);
					j = pi[j];
				}
				/* 패턴이 현재 인덱스까지 일치할 경우 다음 패턴의 인덱스로 이동 */
				else
					j++;
			}
		}
		
		return result;
	}
}
