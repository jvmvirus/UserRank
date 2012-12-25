package com.userrank;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.userrank.UserRank;

public class Main extends Frame implements ActionListener, WindowListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Label lblwarning;
	private Checkbox cbActionLike;	
	private Label lblActionLikeWeight;    
	private TextField tfActionLikeWeight; 
	private Checkbox cbActionComment;		
	private Label lblActionCommentWeight;    
	private TextField tfActionCommentWeight; 
	private Checkbox cbActionWallPost;		
	private Label lblActionWallPostWeight;    
	private TextField tfActionWallPostWeight; 
	private Checkbox cbUseEdgeRank;
	private Label lblTotalTime; 

	
	private Button btnRun;   
	   
	public static void main(String[] args)   {
		Main main = new Main();
	}

	private void start() {
		System.out.println("start");
		UserRank ur = new UserRank();
		
		ur.useActionLike = cbActionLike.getState();
		ur.useActionComment = cbActionComment.getState();
		ur.useActionWallPost = cbActionWallPost.getState();
		ur.useEdgeRank = cbUseEdgeRank.getState();
		
		if ( !( cbActionLike.getState() || cbActionComment.getState() || cbActionWallPost.getState() ) ) {
			System.out.println("stop");
			lblwarning.setText("You must choose at least 1 action!!!");
			lblwarning.setVisible(true);
			return;
		} else {
			lblwarning.setVisible(false);
			
			ur.action_like = Double.parseDouble(tfActionLikeWeight.getText());
			ur.action_comment = Double.parseDouble(tfActionCommentWeight.getText());
			ur.action_post_wall = Double.parseDouble(tfActionWallPostWeight.getText());

			if ( (ur.action_like * ur.action_comment * ur.action_post_wall ) == 0 ) {
				lblwarning.setText("At least 1 weight must different 0.");				
				lblwarning.setVisible(true);
				return;
			}
			
			try {
				ur.execute();
				String resultTotalTime = "Total time: " + ur.total_time + " miliseconds";
				System.out.println(resultTotalTime);

				lblTotalTime.setText( resultTotalTime );
			    lblTotalTime.setVisible(true);
				
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		start();
	}
	
	public Main() {
		setLayout(new GridLayout(10, 2, 0, 0)); // "this" Frame sets to BorderLayout
		
		Panel panelWaringLabel = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		lblwarning = new Label("At least 1 weight must different 0.");
		panelWaringLabel.add(lblwarning);
		add(panelWaringLabel);
		//lblwarning.setVisible(false);
		
		Panel rightpanelWaringLabel = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		add(rightpanelWaringLabel);
		
		Panel panelActionLikeCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		cbActionLike = new Checkbox("Action like", false);
		cbActionLike.setState(true);
		panelActionLikeCheckBox.add(cbActionLike);
		add(panelActionLikeCheckBox);
		
		Panel rightPanelActionLikeCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		add(rightPanelActionLikeCheckBox);
		
		Panel panelLblActionLike = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		lblActionLikeWeight = new Label("Weight of action like");
		panelLblActionLike.add(lblActionLikeWeight);
		add(panelLblActionLike);

		Panel panelTfActionLike = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		tfActionLikeWeight = new TextField("0.5", 20);
		panelTfActionLike.add(tfActionLikeWeight);
		add(panelTfActionLike);
		
		Panel panelActionCommentCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		cbActionComment = new Checkbox("Action comment", false);
		cbActionComment.setState(true);		
		panelActionCommentCheckBox.add(cbActionComment);
		add(panelActionCommentCheckBox);
		
		Panel rightPanelActionCommentCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		add(rightPanelActionCommentCheckBox);
		
		Panel panelLblActionComment = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		lblActionCommentWeight = new Label("Weight of action comment");
		panelLblActionComment.add(lblActionCommentWeight);
		add(panelLblActionComment);
		
		Panel panelTfActionComment = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		tfActionCommentWeight = new TextField("0.33", 20);
		panelTfActionComment.add(tfActionCommentWeight);
		add(panelTfActionComment);
		
		Panel panelActionWallPostCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		cbActionWallPost = new Checkbox("Action wall post", false);
		cbActionWallPost.setState(true);				
		panelActionWallPostCheckBox.add(cbActionWallPost);
		add(panelActionWallPostCheckBox);
		
		Panel rightPanelActionWallPostCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		add(rightPanelActionWallPostCheckBox);
		
		Panel panelLblActionWallPost = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		lblActionWallPostWeight = new Label("Weight of action wall post");
		panelLblActionWallPost.add(lblActionWallPostWeight);
		add(panelLblActionWallPost);
		
		Panel panelTfActionWallPost = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		tfActionWallPostWeight = new TextField("0.17", 20);
		panelTfActionWallPost.add(tfActionWallPostWeight);
		add(panelTfActionWallPost);
		
		Panel panelUseTimeDecayCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		cbUseEdgeRank = new Checkbox("Use EdgeRank", false);
		cbUseEdgeRank.setState(true);				
		panelUseTimeDecayCheckBox.add(cbUseEdgeRank);
		add(panelUseTimeDecayCheckBox);
		
		Panel rightPanelUseTimeDecayCheckBox = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		add(rightPanelUseTimeDecayCheckBox);
		
		Panel panelLblTotalTime = new Panel( new FlowLayout( FlowLayout.LEFT ) );
		lblTotalTime = new Label("Total time: 12973.0 miliseconds   ");
		panelLblTotalTime.add(lblTotalTime);
		add(panelLblTotalTime);
		
		Panel panelRightLblTotalTime = new Panel(  );
		add(panelRightLblTotalTime);
		
		Panel panelLeftBtnRun = new Panel(  );
		add(panelLeftBtnRun);		
		
		Panel panelBtnRun = new Panel( new FlowLayout(  FlowLayout.LEFT ) );
		btnRun = new Button("Run");  // Button is a Component
		panelBtnRun.add(btnRun);
		btnRun.addActionListener(this);
		add(panelBtnRun);
				
		addWindowListener(this);
		
	    //this.add(panelActionLike, BorderLayout.NORTH);
	    //this.add(panelBtnRun, BorderLayout.CENTER);
		
		setTitle("User Rank");  // "this" Frame sets title
	    setSize(500, 400);        // "this" Frame sets initial window size
	    setLocation(400, 250);
	    setVisible(true);         // "this" Frame shows
	    
	    lblwarning.setVisible(false);
	    lblTotalTime.setVisible(false);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		
	}
}