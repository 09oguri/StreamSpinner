import java.lang.reflect.Array;

import org.streamspinner.connection.*;

public class Example6 {
	/*
	 * 複数のUnitから取得
	 */
	private static final int ACTIVE = 0;
	private static final int IDLE = 1;
	private static final int STANDBY = 2;
	
	private static int datadisk = 2;
	private static double acc = 0.0;
	
	private static double[] wdataIdle;
	private static double[] wdataStandby;
	private static double[] jdataSpinup;
	private static double[] jdataSpindown;
	private int[] idleTime;
	private int[] standbyTime;
	private static int[] dataState;
	private static boolean[] dataSpindown;
	private static boolean[] dataSpinup;
	
	
	public Example6(int disk) {
		wdataIdle = new double[disk];
		wdataStandby = new double[disk];
		jdataSpinup = new double[disk];
		jdataSpindown = new double[disk];
		dataState = new int[disk];
		idleTime = new int[disk];
		standbyTime = new int[disk];
		dataSpinup = new boolean[disk];
		dataSpindown = new boolean[disk];
		for(int i = 0; i < dataState.length; i++) {
			dataState[i] = IDLE;
		}
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
	
	public void dataInte() {
		try {
			CQRowSet rs = new DefaultCQRowSet();
			rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
			rs.setCommand("MASTER Unit1 SELECT inte(Unit1.Power1),inte(Unit1.Power2) FROM Unit1[1000]");   // 問合せのセット
			CQRowSetListener ls = new MyListener2();
			rs.addCQRowSetListener(ls);   // リスナの登録
			rs.start();   // 問合せ処理の開始
		} catch(CQException e) {
			e.printStackTrace();
		}
	}

    // DATA DISKS AVG
    public class MyListener implements CQRowSetListener {
    	public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
    		CQRowSet rs = (CQRowSet)(e.getSource());
    		try {
    			int i = 0;
    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
    				if(dataState[i] == IDLE) {
    					wdataIdle[i] = rs.getDouble(i + 1);
    				}else if(dataState[i] == STANDBY) {
    					wdataStandby[i] = rs.getDouble(i + 1);
    				}
    				i++;
    			}
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
    			int i = 0;
    			double wcurrent = 0.0;
    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
    				wcurrent = (rs.getDouble(i + 1) * 100) / 100;	// TODO interval 100
    				if(dataSpinup[i]) {
    					jdataSpinup[i] += rs.getDouble(i + 1);
    				}else if(dataSpindown[i]) {
    					jdataSpindown[i] += rs.getDouble(i + 1);
    				}
    				if(wcurrent < wdataIdle[i] + acc) {	// TODO <,>,=
    					dataSpinup[i] = false;
    				}
    				if(wcurrent < wdataStandby[i] + acc) {
    					dataSpindown[i] = false;
    				}
    				i++;
    			}
    			System.out.println(wcurrent);
    		} catch (CQException e1) {
    			e1.printStackTrace();
			}
        }
    }
    
    private static void spinup(int index) {
    	dataSpinup[index] = true;
    	jdataSpinup[index] = 0.0;
    	dataState[index] = IDLE;
    }
    
    private static void spindown(int index) {
    	dataSpindown[index] = true;
    	jdataSpindown[index] = 0.0;
    	dataState[index] = STANDBY;
    }
    
    public static void main(String[] args) {
    	Example6 ex = new Example6(datadisk);
    	ex.dataAvg();
    	ex.dataInte();
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spindown(0);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spindown(1);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spinup(0);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spinup(1);
    	System.out.println((wdataIdle[0] * 10.0) + ": " + wdataStandby[0] * 10.0 + jdataSpinup[0] + jdataSpindown[0]);
    }
}