package com.lw.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mec.util.view.IMecView;

public class FileReceiveProgressMonitorDialog extends JDialog implements IMecView {
	
	private static final long serialVersionUID = 1L;
	public static final int BASE_WIDTH = 400;
	public static final int BASE_HEIGHT = 90;
	public static final int HEIGHT_PER_PANEL = 50;

	private static final String TOPIC = "传输进程监听器";
	
	private JPanel jpnlBody;
	//接收端数量，即进度条数量
	private int senderCount;
	//正在发送的文件进程数量
	private int sendFileProcessCount;
	//进度条
	private ProgressPanel progressPanel; 

	public FileReceiveProgressMonitorDialog(Frame parentFrame, int senderCount) {
		//窗口为模态框
		super(parentFrame, true);
		//初始化发送进程为1
		this.sendFileProcessCount = 1;
		this.senderCount = senderCount;
		initView();
	}

	@Override
	public void init() {
		setLocationRelativeTo(getOwner());
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		
		JPanel jpnlEast = new JPanel();
		add(jpnlEast, BorderLayout.EAST);
		
		JPanel jpnlWest = new JPanel();
		add(jpnlWest, BorderLayout.WEST);
		
		JPanel jpnlFooter = new JPanel();
		add(jpnlFooter, BorderLayout.SOUTH);
		
		JLabel jlblTopic = new JLabel(TOPIC, JLabel.CENTER);
		jlblTopic.setFont(topicFont);
		jlblTopic.setForeground(topicColor);
		add(jlblTopic, BorderLayout.NORTH);
		
		this.jpnlBody = new JPanel(new GridLayout(0,1));
		add(jpnlBody, BorderLayout.CENTER);
		//这里是表明发送客户端连接的进度条，
		this.progressPanel = new ProgressPanel(0, this.senderCount, "共" + this.senderCount + "个文件发送方");
		this.jpnlBody.add(this.progressPanel);
	}
	
	//添加发送方
	public void addSender() {
		this.progressPanel.increase(1);
	}

	/**
	 * 添加进度条，即此时增加一个发送方发送文件进度条
	 * @param progressPanel
	 */
	public void addProgress(ProgressPanel progressPanel) {
		++this.sendFileProcessCount;
		this.jpnlBody.add(progressPanel);
		//重新计算模态框的尺寸并显示
		resizeDialog();
	}

	/**
	 * 删除进度条，一个发送端发送完毕
	 * @param progressPanel
	 */
	public void removeProgress(ProgressPanel progressPanel) {
		--this.sendFileProcessCount;
		this.jpnlBody.remove(progressPanel);
		resizeDialog();
	}

	/**
	 * 重新设置模态框尺寸
	 */
	private void resizeDialog() {
		setSize(BASE_WIDTH, BASE_HEIGHT + 
				(this.sendFileProcessCount + 1) * ProgressPanel.PER_HEIGHT);
		locationToCenter();
	}

	/**
	 * 重新设置完尺寸后居中
	 */
	private void locationToCenter() {
		int myWidth = getWidth();
		int myHeight = getHeight();
		Window owner = getOwner();
		int parentTop = owner.getY();
		int parentLeft = owner.getX();
		int parentWidth = owner.getWidth();
		int parentHeight = owner.getHeight();
		
		setLocation(parentLeft + (parentWidth - myWidth) / 2, 
				parentTop + (parentHeight - myHeight) / 2);
	}

	/**
	 * 失去焦点后重新设定尺寸
	 */
	@Override
	public void dealAction() {
		this.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				resizeDialog();
			}
		});
	}

	@Override
	public void beforeShowView() {
		
	}

	@Override
	public void afterShowView() {
		
	}

	@Override
	public boolean beforeCloseView() {
		return true;
	}

	@Override
	public void afterCloseView() {
		
	}
	
	@Override
	public Window getView() {
		return this;
	}


}
