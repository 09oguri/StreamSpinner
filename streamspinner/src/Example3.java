import org.streamspinner.connection.*;

public class Example3 {
	/*
	 * SQLで渡した配列の長さを取得する関数
	 */
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinnerの稼働するマシン名を指定
            
            // 問合せのセット
            rs.setCommand("MASTER R SELECT count(R.A2) FROM R [1000]");
            
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
				    System.out.println(rs.getLong(1));
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}