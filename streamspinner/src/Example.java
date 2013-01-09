import org.streamspinner.connection.*;

public class Example {
	private static long CHANGE_TIME = 1000 * 3;
	private static long LOW_TIME = 1500;
	private static long startTime = 0;
	private static long prevTime = 0;
	private static int spindown = 0;
	
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
            //rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1) FROM Unit1[1000]");   // 問合せのセット
            // TODO from unit1,unit2,...
            //rs.setCommand("MASTER R SELECT R.A2 FROM R[1]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT avg(R.A2) FROM R[1000]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM R[1000]");   // 問合せのセット
            rs.setCommand("MASTER R SELECT * FROM R[1] WHERE R.A2 < 10");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM( SELECT * FROM R[1000] WHERE R.A2 > 5)");
            
            CQRowSetListener ls = new MyListener();
            rs.addCQRowSetListener(ls);   // リスナの登録
            rs.start();   // 問合せ処理の開始
        } catch(CQException e){
            e.printStackTrace();
        }
    }

    public static class MyListener implements CQRowSetListener {
    	// SELECT avg(Unit1.Power1),avg(Unit1.Power2),...
    	// どのディスクがキャッシュディスクかわかる
        public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
            CQRowSet rs = (CQRowSet)(e.getSource());
            try {
				while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
				    long now = rs.getLong("R.Timestamp");
				     
				    if(prevTime == 0 || now - prevTime < LOW_TIME) {
				    	if(startTime == 0) {
				    		startTime = now;
				    	}
				    	if(now - startTime > CHANGE_TIME) {
				    		spindown = 1;
				    		System.out.println("spindown");
				    	}
				    }else {
				    	startTime = 0;
				    	System.out.println("--------");
				    }
				    prevTime = now;
				    
				    System.out.println(now + ": " + rs.getLong("R.A2") + ": " + startTime);
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}