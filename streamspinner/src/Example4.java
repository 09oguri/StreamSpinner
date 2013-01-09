import org.streamspinner.connection.*;

public class Example4 {
	/*
	 * ������Unit����擾
	 */
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinner�̉ғ�����}�V�������w��
            //rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1) FROM Unit1[1000]");   // �⍇���̃Z�b�g
            
            // ���̂܂܂��Ɩ���f�[�^���擾���ɍs���̂œr���Ŏ~�܂�???
            rs.setCommand("MASTER Unit1,Unit2 SELECT Unit1.Power1,Unit2.Power1 FROM Unit1[1000],Unit2[1000]");   // �⍇���̃Z�b�g
            
            //rs.setCommand("MASTER R SELECT R.A2 FROM R[1]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT avg(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT mul2(R.A2) FROM R[1000]");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT mul2(R.A2) AS mul2 FROM R[1000]");
            //rs.setCommand("MASTER R SELECT * FROM R[1] WHERE R.A2 < 10");   // �⍇���̃Z�b�g
            //rs.setCommand("MASTER R SELECT sum(R.A2) FROM( SELECT * FROM R[1000] WHERE R.A2 > 5)");
            
            CQRowSetListener ls = new MyListener();
            rs.addCQRowSetListener(ls);   // ���X�i�̓o�^
            rs.start();   // �⍇�������̊J�n
        } catch(CQException e){
            e.printStackTrace();
        }
    }

    public static class MyListener implements CQRowSetListener {
        public void dataDistributed(CQRowSetEvent e){   // �������ʂ̐����ʒm���󂯎�郁�\�b�h
            CQRowSet rs = (CQRowSet)(e.getSource());
            try {
				while( rs.next() ){   // JDBC���C�N�ȃJ�[�\�������ɂ��C�P�s���������ʂ��擾
				    System.out.println(rs.getDouble(1));
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}