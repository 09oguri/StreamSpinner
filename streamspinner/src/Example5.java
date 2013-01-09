import java.lang.reflect.Array;
import java.rmi.RMISecurityManager;

import org.streamspinner.connection.*;

public class Example5 {
	/*
	 * 複数のUnitから取得
	 */
	private static int datadisk = 2;
	private static int cachedisk = 2;
	
	private double[] wdata;
	private double wcache = 0.0;
	private static int index = 0;
	
	public Example5(int disk) {
		wdata = new double[disk];
	}
	
	public void dataAvg() {
		try {
			CQRowSet rs = new DefaultCQRowSet();
			rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
			rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power3),avg(Unit1.Power4) FROM Unit1[1000]");   // 問合せのセット
			CQRowSetListener ls = new MyListener();
			rs.addCQRowSetListener(ls);   // リスナの登録
			rs.start();   // 問合せ処理の開始
		} catch(CQException e) {
			e.printStackTrace();
		}
	}
	
	public void cacheAvg() {
		try {
			CQRowSet rs = new DefaultCQRowSet();
			rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
			rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1),avg(Unit1.Power2) FROM Unit1[1000]");   // 問合せのセット
			CQRowSetListener ls = new MyListener2();
			rs.addCQRowSetListener(ls);   // リスナの登録
			rs.start();   // 問合せ処理の開始
		} catch(CQException e) {
			e.printStackTrace();
		}
	}

    // DATA DISKS AVG SUM
    public class MyListener implements CQRowSetListener {
    	public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
    		CQRowSet rs = (CQRowSet)(e.getSource());
    		try {
    			wdata[index] = 0.0;
    			int i = 0;
    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
    				wdata[index] += rs.getDouble(i + 1);
    				i++;
    			}
    			System.out.println(index + ": " + wdata[index]);
    		} catch (CQException e1) {
    			e1.printStackTrace();
			}
        }
    }
    
    // CACHE DISK AVG
    public class MyListener2 implements CQRowSetListener {
    	public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
    		CQRowSet rs = (CQRowSet)(e.getSource());
    		try {
    			wcache = 0.0;
    			int i = 0;
    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
    				wcache += rs.getDouble(i + 1);
    				i++;
    			}
    			wcache = wcache / (double) cachedisk;
    			System.out.println(wcache);
    		} catch (CQException e1) {
    			e1.printStackTrace();
			}
        }
    }
    
    private static void spinup() {
    	if(index > 0) {
    		index--;
    	}
    }
    
    private static void spindown() {
    	index++;
    }
    
    public static void main(String[] args) {
    	Example5 ex = new Example5(datadisk);
    	ex.dataAvg();
    	ex.cacheAvg();
    	
    	// TODO スピンダウン中は比較しないようにする
    	
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spindown();
    }
}