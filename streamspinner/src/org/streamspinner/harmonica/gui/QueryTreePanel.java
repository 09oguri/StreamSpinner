/*
 * Copyright 2005-2009 StreamSpinner Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.streamspinner.harmonica.gui;

import org.streamspinner.harmonica.*;
import org.streamspinner.harmonica.validator.*;
import org.streamspinner.harmonica.query.hamql.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.w3c.dom.*;

/**
 * 問合せ木を表示するパネル
 * <BR><BR>
 * 変更履歴:
 * <PRE>
 * 1.1 2006.8.7 ツールチップを表示するようにした．
 *              ノードを画像で表現した．
 * </PRE>
 *
 * @author snic
 * @version 1.1 (2006.8.7)
 */
public class QueryTreePanel extends JComponent 
{
	private ValidatingResult result = null;
	private PaintableNode node = null;
	private int base_size_x = 80;
	private int base_size_y = 60;
	private int vacancy = 20;
	private Map<Point, Point> arrow = null;
	private Cursor c1 = null;
	private Cursor c2 = null;
	private int clicked_x = -1;
	private int clicked_y = -1;
	private int sa_x = -1;
	private int sa_y = -1;
	private int moved_x = -1;
	private int moved_y = -1;
	private NodeType clicked_ntype = null;
	private OperatorType clicked_otype = null;
	private int current_x = -1;
	private int current_y = -1;
	private int dx = 0;
	private int dy = 0;
	private JScrollPane scroll = null;
	private int toggle = 1;
	private QueryNodeToolTip tooltip = null;

	/**
	 * デフォルトのスケールで初期化<BR>
	 * X=80, Y=60, VACANCY=20
	 */
	public QueryTreePanel(){
		super();
		arrow = new HashMap<Point, Point>();
		this.c1 = new Cursor(Cursor.DEFAULT_CURSOR);
		this.c2 = new Cursor(Cursor.MOVE_CURSOR);
	}

	/**
	 * 1ノード辺りのスケールを手動で設定して初期化
	 *
	 * @param size_x 横の大きさ
	 * @param size_y 縦の大きさ
	 * @param vacancy ノード間の大きさ
	 */
	public QueryTreePanel(int size_x, int size_y, int vacancy){
		super();
		this.base_size_x = size_x;
		this.base_size_y = size_y;
		this.vacancy = vacancy;
		arrow = new HashMap<Point, Point>();
		this.c1 = new Cursor(Cursor.DEFAULT_CURSOR);
		this.c2 = new Cursor(Cursor.MOVE_CURSOR);
		this.setBackground(Color.WHITE);
	}

	/**
	 * 描画に用いるValidatingResultオブジェクトをセットする．
	 *
	 * @param result 描画に用いるresultオブジェクト
	 */
	public void setValidatingResult(ValidatingResult result){
		this.result = result;
		init();
	}

	private void init(){
		RepaintManager rm = new RepaintManager(){
			public void adDirtyRegion
				(JComponent c, int x, int y, int w, int h){
				super.addDirtyRegion(c,x,y,w,h);
				JComponent root = getRootJComponent(c);
				if(c != root){
					super.addDirtyRegion
						(root,0,0,root.getWidth(),root.getHeight());
				}
			}
			public JComponent getRootJComponent(JComponent c){
				Container p = c.getParent();
				if(p instanceof JComponent){
					return getRootJComponent((JComponent)p);
				}
				return c;
			}
		};
		RepaintManager.setCurrentManager(rm);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

		Map<String, Element> id_map = new HashMap<String, Element>();
		Document xml = result.getQueryTree().getXMLDocument();
		Element root = (Element)xml.getFirstChild();

		NodeList list = root.getChildNodes();
		Element root_node = null;
		for(int i=0;i<list.getLength();i++){
			Element elm = (Element)list.item(i);
			id_map.put(elm.getAttribute("id"),elm);

			if(elm.getAttribute("type").equals("root")) root_node = elm;
		}
		
		node = makePrintableNodes(root_node, id_map);
		super.setSize(node.getWidth(), node.getHeight());

		// ノードデータに対して座標を設置
		calcurate_axis(0, 0, node);

		// 矢印用のデータの座標を設置
		arrow = getArrowMap();

		repaint();

		// マウスのイベントをON
		super.enableEvents(MouseEvent.MOUSE_MOTION_EVENT_MASK);
		super.enableEvents(MouseEvent.MOUSE_EVENT_MASK);

		current_x = node.getWidth();
		current_y = node.getHeight();
	}


	public void setParentScrollPane(JScrollPane sp){
		this.scroll = sp;
	}

	/**
	 * マウスを動かしたり，ドラッグしたり・・・
	 */
	protected void processMouseMotionEvent(MouseEvent e){
		super.processMouseMotionEvent(e);
		
		if(e.getID() == MouseEvent.MOUSE_MOVED){
			if(node == null) return;
			int x = e.getX();
			int y = e.getY();
				
			boolean b = refreshToolTipText(x,y,node);
			if(!b){
				super.setToolTipText(null);
			}
		}else if(e.getID() == MouseEvent.MOUSE_DRAGGED){
			if(node == null) return;
			moved_x = e.getX();
			moved_y = e.getY();

			if(current_x < e.getX() + (base_size_x - sa_x) + vacancy)
			   	dx = e.getX() - current_x - (base_size_x - sa_x) - vacancy;
			if(current_y < e.getY() + (base_size_y - sa_y) + vacancy) 
				dy = e.getY() - current_y - (base_size_y - sa_y) - vacancy;


			repaint();
		}
	}
	
	/**
	 * マウスをクリックしたり，離したり・・・
	 */
	protected void processMouseEvent(MouseEvent e){
		super.processMouseEvent(e);

		if(e.getID() == MouseEvent.MOUSE_PRESSED){
			if(SwingUtilities.isLeftMouseButton(e)){ // 左クリック
				clicked_x = e.getX();
				clicked_y = e.getY();
			
				PaintableNode p = getTargetNode(node);
				if(p == null) return;
					
				super.setCursor(c2);
				clicked_ntype = p.getOperatorCost().getNodeType();
				clicked_otype = p.getOperatorCost().getType();
				sa_x = clicked_x - p.getNodeX();
				sa_y = clicked_y - p.getNodeY();
				moved_x = e.getX();
				moved_y = e.getY();

			}else if(SwingUtilities.isRightMouseButton(e)){ // 右クリック
				clicked_x = e.getX();
				clicked_y = e.getY();

				PaintableNode p = getTargetNode(node);
				if(p == null){
					Dimension screen = 
						Toolkit.getDefaultToolkit().getScreenSize();
					int w = screen.width;
					int h = screen.height;

					JDialog dialog = new JDialog(
							HarmonicaManager.getHarmonicaMonitor(),
							"DOM",
							true);
					JTextArea area = new JTextArea();
					JScrollPane scroll = new JScrollPane();
					JPanel panel = new JPanel();

					area.setText(result.getQueryTree().toString());
					scroll.setViewportView(area);
					panel.setLayout(new BorderLayout());
					panel.add(scroll, BorderLayout.CENTER);
					dialog.setBounds((w-500)/2,(h-350)/2,500,350);
					dialog.setContentPane(panel);
					dialog.setVisible(true);

					return;
				}

				if(HarmonicaManager.locale.equals(Locale.JAPAN))
				JOptionPane.showMessageDialog(
						this,
						p.getText("\n"),
						"演算の情報",
						JOptionPane.INFORMATION_MESSAGE);
				else
				JOptionPane.showMessageDialog(
						this,
						p.getText("\n"),
						"Operator Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}else if(e.getID() == MouseEvent.MOUSE_RELEASED){
			if(SwingUtilities.isLeftMouseButton(e)){
				PaintableNode n = getTargetNode(node);
				moveNode(n,e.getX(), e.getY());
				super.setCursor(c1);
				
				dx = 0; 
				dy = 0;
				current_x = 0;
				current_y = 0;
				recalculateSize(node);

			super.setSize(super.getWidth()+toggle,super.getHeight()+toggle);
			if(toggle == 1) toggle = -1; else toggle = 1;
				repaint();
			}else if(SwingUtilities.isLeftMouseButton(e)){

			}
		}
	}

	private void paintGhost
	(Graphics g, NodeType ntype, OperatorType otype, int x, int y){
		if(!this.getCursor().equals(c2)) return;
		Graphics2D g2 = (Graphics2D)g;
		AlphaComposite com = 
			AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		Composite orig_com = g2.getComposite();
		g2.setComposite(com);
		Image img = null;
		
		try{
			if(ntype != NodeType.OPERATOR){
				img = ImageFactory.getInstance().getImage(ntype);
			}else{
				img = ImageFactory.getInstance().getImage(otype);
			}
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}
		
		if(img != null){
			g2.drawImage(img,x,y,base_size_x,base_size_y,this);
		}
		g2.setComposite(orig_com);
	}

	private PaintableNode getTargetNode(PaintableNode n){
		if(n == null) return null;
		if(clicked_x < 0 || clicked_y < 0) return null;

		if(n.getNodeX() <= clicked_x && 
				clicked_x <= n.getNodeX() + base_size_x){
			if(n.getNodeY() <= clicked_y && 
					clicked_y <= n.getNodeY() + base_size_y){
				return n;
			}
		}

		PaintableNode pn = getTargetNode(n.getLeftNode());
		if(pn != null) return pn;

		return getTargetNode(n.getRightNode());
	}

	private void recalculateSize(PaintableNode n){
		if(n == null) return;
		if(current_x < n.getNodeX() + base_size_x + vacancy) 
			current_x = n.getNodeX() + base_size_x + vacancy;
		if(current_y < n.getNodeY() + base_size_y + vacancy) 
			current_y = n.getNodeY() + base_size_y + vacancy;

		if(n.getLeftNode() != null) recalculateSize(n.getLeftNode());
		if(n.getRightNode() != null) recalculateSize(n.getRightNode());
	}

	private Map<Point, Point> getArrowMap(){
		Map<Point, Point> m = new HashMap<Point, Point>();

		connectArrow(m, node);
		return m;
	}

	private void connectArrow(Map<Point, Point> m, PaintableNode n){
		if(n == null) return;

		int hosei = 0;

		if(n.getRightNode() != null) hosei = 3;

		if(n.getLeftNode() != null){
			Point p1 = new Point(
					n.getInputPoint()[0] - hosei,
					n.getInputPoint()[1]
			);
			Point p2 = new Point(
					n.getLeftNode().getOutputPoint()[0],
					n.getLeftNode().getOutputPoint()[1]
			);
			m.put(p2,p1);
			connectArrow(m, n.getLeftNode());
			hosei = 3;
		}else{
			hosei = 0;
		}

		if(n.getRightNode() != null){
			Point p1 = new Point(
					n.getInputPoint()[0] + hosei,
					n.getInputPoint()[1]
			);
			Point p2 = new Point(
					n.getRightNode().getOutputPoint()[0],
					n.getRightNode().getOutputPoint()[1]
			);
			m.put(p2,p1);
			connectArrow(m, n.getRightNode());
		}
	}

	private void moveNode(PaintableNode n, int x, int y){
		if(n == null) return;

		int move_x = x - clicked_x;
		int move_y = y - clicked_y;
		int[] ip = n.getInputPoint();
		int[] op = n.getOutputPoint();

		ip[0] += move_x;
		ip[1] += move_y;
		op[0] += move_x;
		op[1] += move_y;

		n.setInputPoint(ip);
		n.setOutputPoint(op);

		arrow = getArrowMap();

		n.setNodeLocation(
				n.getNodeX() + move_x,
				n.getNodeY() + move_y
		);

		// ノードの大きさを変える
		//node.setSize(node.getWidth() + move_x, node.getHeight() + move_y);
	}

	public JToolTip createToolTip(){
		tooltip = new QueryNodeToolTip();
		tooltip.setComponent(this);

		return tooltip;
	}

	private boolean refreshToolTipText(int x, int y, PaintableNode n){
		if(n == null) return false;

		if(n.getNodeX() <= x && x <= n.getNodeX() + base_size_x){
			if(n.getNodeY() <= y && y <= n.getNodeY() + base_size_y){
				setToolTipText(
						"<html>&nbsp;&nbsp;"+
						n.getText("&nbsp;&nbsp;<BR>&nbsp;&nbsp;")+
						"</html>");
				return true;
			}
		}

		boolean b = refreshToolTipText(x,y,n.getLeftNode());
		if(b) return true;

		return refreshToolTipText(x,y,n.getRightNode());
	}

	/**
	 * オーバーライドしてノードを描画するようにしている．
	 */
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;

		g2.setBackground(Color.white);
		g2.clearRect(0, 0, getWidth(), getHeight());

		// 問合せ可能かどうかを描画
		paintFeasible(g);

		// 矢印を描画
		paintArrow(g);

		// ノードを描画
		paintNode(node, g);

		// ゴーストを描画
		paintGhost(g, 
				   clicked_ntype, 
				   clicked_otype, 
				   moved_x - sa_x, 
				   moved_y - sa_y
		);

		super.paint(g);
	}

	private void paintFeasible(Graphics g){
		int gyap = 10;
		int x = this.getWidth() - base_size_x - gyap;
		int y = gyap;
		QueryType type = result.getQueryTree().getQuery().getQueryType();

		if(result.isProcessable())
			paintImage
				(g,ImageType.PROCESSABLE,x,y,base_size_x,base_size_y);
		else
			paintImage
				(g,ImageType.UNPROCESSABLE,x,y,base_size_x,base_size_y);

		y += base_size_y + gyap;

		if(type == QueryType.INSERT){
			if(result.isInsertable() && result.isProcessable())
				paintImage
					(g,ImageType.INSERTABLE,x,y,base_size_x, base_size_y);
			else
				paintImage
					(g,ImageType.UNINSERTABLE,x,y,base_size_x, base_size_y);
		}else{
			paintImage
				(g,ImageType.INSERTABLE_GRAY,x,y,base_size_x, base_size_y);
			
		}

		int bar_width = 18;
		int bar_height = 102;
		int bar1_x = x + (base_size_x/2 - bar_width)/2 + 8;
		int bar2_x = x + (base_size_x*3/2 - bar_width)/2 - 8;
		int bar_y = y + base_size_y + gyap;

		g.setColor(new Color(105,105,105));
		g.drawRect(bar1_x,bar_y-1,bar_width+1,bar_height+1);
		g.drawRect(bar2_x,bar_y-1,bar_width+1,bar_height+1);

		if(result.isProcessable()){
			int rate = 100 - (int)Math.ceil(result.getProcessingTime()*100);
			paintImage(
					g,
					ImageType.ABLE_BAR,
					bar2_x+2,
					bar_y+101-rate,
					16,
					rate);
		}

		if(type == QueryType.INSERT){
			if(result.isInsertable() && result.isProcessable()){
				double store_rate = result.getInsertionRate()*100;
				double query_rate = result.getOutputRate()*100;
				int rate = 100 - (int)Math.ceil((query_rate*100)/store_rate);

				paintImage(
						g,
						ImageType.ABLE_BAR,
						bar1_x+2,
						bar_y+101-rate,
						16,
						rate);
			}
		}else{
			int rate = 100;
			paintImage(
					g,
					ImageType.ABLE_BAR_GRAY,
					bar1_x+2,
					bar_y+101-rate,
					16,
					rate);
		}

		g.drawString("D",bar1_x+6,bar_y+bar_height+15);
		g.drawString("S",bar2_x+6,bar_y+bar_height+15);
	}

	private void paintImage
		(Graphics g, ImageType t, int x, int y, int w, int h){
		
		Image img = null;

		try{
			img = ImageFactory.getInstance().getImage(t);
			g.drawImage(img,x,y,w,h,this);
			return;
		}catch(HarmonicaException e){
			HarmonicaManager.createdException(e);
		}

		g.drawArc(x,y,w,h,10,360);
	}

	private void paintNode(PaintableNode n, Graphics g){
		if(n == null) return;
		NodeType ntype = n.getOperatorCost().getNodeType();

		if(ntype != NodeType.OPERATOR){
			Image img = null;
			try{
				img = ImageFactory.getInstance().getImage(ntype);
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}

			if(img == null){ // imageファイルが無い
				g.drawRect(
						n.getNodeX(),
						n.getNodeY(),
						base_size_x,
						base_size_y
				);
			}else{ // imageファイルがある
				g.drawImage(
						img,
						n.getNodeX(),
						n.getNodeY(),
						base_size_x,
						base_size_y,
						this
				);
			}
		}

		if(ntype == NodeType.OPERATOR){
			OperatorType otype = n.getOperatorCost().getType();
			Image img = null;
			try{
				img = ImageFactory.getInstance().getImage(otype);
			}catch(HarmonicaException e){
				HarmonicaManager.createdException(e);
			}

			if(img == null){ // imageファイルが無い
				g.drawArc(
						n.getNodeX(),
						n.getNodeY(),
						base_size_x,
						base_size_y,
						10,
						360
				);
			}else{ // imageファイルがある
				g.drawImage(
						img,
						n.getNodeX(),
						n.getNodeY(),
						base_size_x,
						base_size_y,
						this
				);
			}
		}

		paintNode(n.getLeftNode(), g);
		paintNode(n.getRightNode(), g);
	}
	private void drawArrow
		(Graphics2D g, 
		 double x0, 
		 double y0, 
		 double x1, 
		 double y1, 
		 int l)
	{
		double theta;
    	int x,y;
	    double dt = Math.PI / 6.0;
   		theta = Math.atan2((double)(y1-y0),(double)(x1-x0));
   		g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);
   		x = (int)(x1-l*Math.cos(theta-dt));
		y = (int)(y1-l*Math.sin(theta-dt));
    	g.drawLine((int)x1,(int)y1,x,y);
    	x = (int)(x1-l*Math.cos(theta+dt));
   		y = (int)(y1-l*Math.sin(theta+dt));
    	g.drawLine((int)x1,(int)y1,x,y);
	}
	private void paintArrow(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		Set<Point> keys = arrow.keySet();
		for(Point e : keys){
			Point s = arrow.get(e);
			
			Stroke orig_stroke = g2.getStroke();
			Color orig_color = g2.getColor();

			Stroke stroke = new BasicStroke(
					2.0f, 
					BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			Color color = new Color(100,100,100);

			g2.setStroke(stroke);
			g2.setColor(color);

			drawArrow(g2,e.getX(),e.getY(),s.getX(),s.getY(),10);
			g2.setStroke(orig_stroke);
			g2.setColor(orig_color);
		}
	}

	private void calcurate_axis(int bx, int by, PaintableNode n){
		n.setLocation(bx,by);
		n.setNodeLocation
			((2 * bx + n.getWidth() - base_size_x)/2, by + vacancy);

		PaintableNode l = n.getLeftNode();
		PaintableNode r = n.getRightNode();

		if(l == null && r == null) return;

		if(l != null && r != null){
			calcurate_axis(bx, by + base_size_y + vacancy, l);
			calcurate_axis(
				bx + l.getWidth() + vacancy, 
				by + base_size_y + vacancy,
				r
			);
		}else if(r == null){
			calcurate_axis(bx, by + base_size_y + vacancy, l);
		}else{
			calcurate_axis(bx, by + base_size_y + vacancy, r);	
		}
	}

	private PaintableNode makePrintableNodes
	(Element elm, Map<String, Element>id_map){
		String[] ids = extractIDs(elm);

		if(ids[1] == null){ // source
			PaintableNode sn = 
				new PaintableNode(base_size_x, base_size_y, vacancy);
			OperatorCost cost = result.getCost().get(ids[0]);
			sn.setOperatorCost(cost);
			
			String name = elm.getAttribute("name");
			StreamArchiver arc = HarmonicaManager.getStreamArchiver();
			if(cost.getOutputRate() <= 0.0){
				try{
					if(arc.getSchema(name) != null){
						cost.setNodeType(NodeType.HARMONICA);
					}else{
						cost.setNodeType(NodeType.RDB);
					}
				}catch(HarmonicaException e){
					cost.setNodeType(NodeType.RDB);
				}
			}

			return sn;
		}else if(ids[2] == null){ // 1-input node
			PaintableNode n = makePrintableNodes(id_map.get(ids[1]), id_map);
			PaintableNode rn = 
				new PaintableNode(base_size_x, base_size_y, vacancy);
			OperatorCost oc = result.getCost().get(ids[0]);
			rn.setOperatorCost(oc);
			if(oc.getType() == OperatorType.INSERTION){
				rn.setInsertionRate(result.getInsertionRate());
			}
			rn.setLeftNode(n);
			return rn;
		}else{ // 2-inputs node
			PaintableNode l = makePrintableNodes(id_map.get(ids[1]), id_map);
			PaintableNode r = makePrintableNodes(id_map.get(ids[2]), id_map);
			PaintableNode rn = 
				new PaintableNode(base_size_x, base_size_y, vacancy);
			rn.setOperatorCost(result.getCost().get(ids[0]));
			rn.setLeftNode(l);
			rn.setRightNode(r);
			return rn;
		}
	}
	/**
	 * Elementから自IDと入出力IDを取得する．
	 */
	private String[] extractIDs(Element elm){
		String[] ids = {null,null,null,null};
		ids[0] = elm.getAttribute("id");
		NodeList list = elm.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Element ch = (Element)list.item(i);
			if(ch.getNodeName().equals("input")){
				String refid = ch.getAttribute("refid");
				if(ids[1] == null) ids[1] = refid;
				else ids[2] = refid;
			}else if(ch.getNodeName().equals("output")){
				ids[3] = ch.getAttribute("refid");
			}
		}	
		return ids;
	}

	public Dimension getPreferredSize(){
		Dimension d = new Dimension(current_x+dx,current_y+dy);
		return d;
	}

	private void debug(Object o){
		HarmonicaManager.debug("TreePanel",o);
	}
}
