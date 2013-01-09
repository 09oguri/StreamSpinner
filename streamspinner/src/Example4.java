import org.streamspinner.connection.*;

public class Example4 {
	/*
	 * 複数のUnitから取得
	 */
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
            //rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1) FROM Unit1[1000]");   // 問合せのセット
            
            // 今のままだと毎回データを取得しに行くので途中で止まる???
            rs.setCommand("MASTER Unit1,Unit2 SELECT Unit1.Power1,Unit2.Power1 FROM Unit1[1000],Unit2[1000]");   // 問合せのセット
            
            //rs.setCommand("MASTER R SELECT R.A2 FROM R[1]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT avg(R.A2) FROM R[1000]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM R[1000]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT mul2(R.A2) FROM R[1000]");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT mul2(R.A2) AS mul2 FROM R[1000]");
            //rs.setCommand("MASTER R SELECT * FROM R[1] WHERE R.A2 < 10");   // 問合せのセット
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM( SELECT * FROM R[1000] WHERE R.A2 > 5)");
            
            CQRowSetListener ls = new MyListener();
            rs.addCQRowSetListener(ls);   // リスナの登録
            rs.start();   // 問合せ処理の開始
        } catch(CQException e){
            e.printStackTrace();
        }
    }

    public static class MyListener implements CQRowSetListener {
        public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
            CQRowSet rs = (CQRowSet)(e.getSource());
            try {
				while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
				    System.out.println(rs.getDouble(1));
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}