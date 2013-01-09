import org.streamspinner.connection.*;

public class Example2 {
	private static int WINDOW = 1000 * 5;
	private static long CHANGE_VARIATION = 10;
	private static long[] variation = new long[WINDOW / 1000];
	private static int count = 0;
	private static long idleTime = 1000;
	
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinner�̉ғ�����}�V�������w��
            //rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1) FROM Unit1[1000]");   // �⍇���̃Z�b�g
            // TODO from unit1,unit2,...
            //rs.setCommand("MASTER R SELECT R.A2 FROM R[1]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT avg(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT mul2(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            rs.setCommand("MASTER R SELECT * FROM R[1] WHERE R.A2 < 10");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT * FROM R[1]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM( SELECT * FROM R[1000] WHERE R.A2 > 5)");
            
            CQRowSetListener ls = new MyListener();
            rs.addCQRowSetListener(ls);   // ���X�i�̓o�^
            rs.start();   // �⍇�������̊J�n
        } catch(CQException e) {
            e.printStackTrace();
        }
    }

    public static class MyListener implements CQRowSetListener {
        public void dataDistributed(CQRowSetEvent e){   // �������ʂ̐����ʒm���󂯎�郁�\�b�h
            CQRowSet rs = (CQRowSet)(e.getSource());
            try {
				while( rs.next() ){   // JDBC���C�N�ȃJ�[�\�������ɂ��C�P�s���������ʂ��擾
				    variation[count] = rs.getLong("R.A2") - (long) rs.getDouble("R.A3");
				    count++;
				    if(count == WINDOW / 1000) {
				    	//long sum = 0;
				    	long sum = CHANGE_VARIATION + 1;	// XXX
				    	for(int i = 0; i < variation.length; i++) {
				    		sum += variation[i];
				    	}
				    	if(sum > CHANGE_VARIATION) {
				    		idleTime += WINDOW;
				    		System.out.println("idle time: " + idleTime);
				    	}else {	// XXX
				    		idleTime -= WINDOW;
				    		System.out.println("idle time: " + idleTime);
				    	}
				    	count = 0;
				    }
				    System.out.println(count + ": " + rs.getLong("R.A2") + ": " + rs.getDouble("R.A3"));
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}