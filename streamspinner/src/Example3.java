import org.streamspinner.connection.*;

public class Example3 {
	/*
	 * SQL�œn�����z��̒������擾����֐�
	 */
    public static void main(String[] args) {
        try {
            CQRowSet rs = new DefaultCQRowSet();
            rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinner�̉ғ�����}�V�������w��
            
            // �⍇���̃Z�b�g
            rs.setCommand("MASTER R SELECT count(R.A2) FROM R [1000]");
            
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
				    System.out.println(rs.getLong(1));
				}
			} catch (CQException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
}