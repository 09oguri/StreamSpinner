import java.lang.reflect.Array;
import java.rmi.RMISecurityManager;

import org.streamspinner.connection.*;

public class Example5 {
	/*
	 * ������Unit����擾
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
			rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinner�̉ғ�����}�V�������w��
			rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power3),avg(Unit1.Power4) FROM Unit1[1000]");   // �⍇���̃Z�b�g
			CQRowSetListener ls = new MyListener();
			rs.addCQRowSetListener(ls);   // ���X�i�̓o�^
			rs.start();   // �⍇�������̊J�n
		} catch(CQException e) {
			e.printStackTrace();
		}
	}
	
	public void cacheAvg() {
		try {
			CQRowSet rs = new DefaultCQRowSet();
			rs.setUrl("rmi://localhost/StreamSpinnerServer");   // StreamSpinner�̉ғ�����}�V�������w��
			rs.setCommand("MASTER Unit1 SELECT avg(Unit1.Power1),avg(Unit1.Power2) FROM Unit1[1000]");   // �⍇���̃Z�b�g
			CQRowSetListener ls = new MyListener2();
			rs.addCQRowSetListener(ls);   // ���X�i�̓o�^
			rs.start();   // �⍇�������̊J�n
		} catch(CQException e) {
			e.printStackTrace();
		}
	}

    // DATA DISKS AVG SUM
    public class MyListener implements CQRowSetListener {
    	public void dataDistributed(CQRowSetEvent e){   // �������ʂ̐����ʒm���󂯎�郁�\�b�h
    		CQRowSet rs = (CQRowSet)(e.getSource());
    		try {
    			wdata[index] = 0.0;
    			int i = 0;
    			while( rs.next() ){   // JDBC���C�N�ȃJ�[�\�������ɂ��C�P�s���������ʂ��擾
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
    	public void dataDistributed(CQRowSetEvent e){   // �������ʂ̐����ʒm���󂯎�郁�\�b�h
    		CQRowSet rs = (CQRowSet)(e.getSource());
    		try {
    			wcache = 0.0;
    			int i = 0;
    			while( rs.next() ){   // JDBC���C�N�ȃJ�[�\�������ɂ��C�P�s���������ʂ��擾
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
    	
    	// TODO �X�s���_�E�����͔�r���Ȃ��悤�ɂ���
    	
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	spindown();
    }
}