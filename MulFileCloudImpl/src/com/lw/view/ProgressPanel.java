package com.lw.view;

import java.awt.GridBagLayout;
import java.awt.Panel;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.mec.util.view.IMecView;

public class ProgressPanel  extends Panel{
	//序列号
	private static final long serialVersionUID = 1L;
	//进度条高度
	public static final int PER_HEIGHT = 45;

	//进度条
	private JProgressBar jpgbProgressBody; 
	//当前进度条内容长度
	private long currentValue;
	//进度条名字，即文件名
	private JLabel jlblTopic;

	/**
	 *初始化进度条
	 * @param currentValue 当前长度
	 * @param maxValue 最大长度
	 * @param topic
	 */
	public ProgressPanel(long currentValue, long maxValue, String topic) {
		this.currentValue = currentValue;
		setLayout(new GridBagLayout());

		this.jlblTopic = new JLabel(topic);
		this.jlblTopic.setFont(IMecView.normalFont);
		add(this.jlblTopic);
		
		this.jpgbProgressBody = new JProgressBar((int)this.currentValue, (int)maxValue);
		this.jpgbProgressBody.setFont(IMecView.normalFont);
		//设置显示百分数
		this.jpgbProgressBody.setStringPainted(true);
		add(this.jpgbProgressBody);
		
	}

    /**
     * 进度条的滑动
     * @param data
     */
	public void increase(int data) {
		this.currentValue += data;
		this.jpgbProgressBody.setValue((int)this.currentValue);
	}
	
	
	

}
