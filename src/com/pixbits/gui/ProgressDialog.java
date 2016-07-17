package com.pixbits.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ConcurrentModificationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class ProgressDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	JLabel title;
	JLabel desc;
	JProgressBar progress;
	Runnable callback;
	
	public ProgressDialog(Frame parent, String title, Runnable cb)
	{
		super(parent, title);
		this.callback = cb;
		
		this.setUndecorated(true);

		JPanel panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
		
		progress = new JProgressBar();

		desc = new JLabel("...");
		desc.setHorizontalAlignment(SwingConstants.CENTER);
		
		//progress.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		panel.add(progress, BorderLayout.CENTER);
		panel.add(desc, BorderLayout.NORTH);
		
		if (callback != null)
		{
		  JButton cancelButton = new JButton("Cancel");
		  panel.add(cancelButton, BorderLayout.SOUTH);
		  cancelButton.addActionListener( e -> { callback.run(); finished(); });
		}
		
		panel.setPreferredSize(new Dimension(400,100));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		this.add(panel);
		
		pack();
		this.setLocationRelativeTo(parent);
	}
	
	public ProgressDialog()
	{
		this(null, "", null);
	}

	private static ProgressDialog dialog;
	
	public static void init(Frame parent, String title, Runnable callback)
	{
	  if (dialog != null)
	    throw new ConcurrentModificationException("Progress dialog reinit while it was already displayed");
	  
	  dialog = new ProgressDialog(parent, title, callback);
	  dialog.progress.setMaximum(100);
	  dialog.progress.setValue(0);
	  dialog.setVisible(true);
	}
	
	public static void update(SwingWorker<?,?> worker, String desc)
	{
	  dialog.progress.setValue(worker.getProgress());
	  dialog.desc.setText(desc);
	}
	
	public static void update(float value, String desc)
	{
	  dialog.progress.setValue((int)(value*100));
	  dialog.desc.setText(desc);
	}
	
	public static void finished()
	{
	  dialog.dispose();
	  dialog = null;
	}
}
