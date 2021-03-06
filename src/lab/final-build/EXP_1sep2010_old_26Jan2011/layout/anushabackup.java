import java.util.*;
import java.util.PropertyPermission.*;
import java.awt.*;
import java.awt.Color.*;
import java.awt.MediaTracker.*;
import java.awt.event.*;
import java.text.*;
import java.awt.datatransfer.*;
import java.net.*;
import java.net.URLEncoder.*;
import java.io.*;
import java.io.File.*;
import netscape.javascript.*;
import java.awt.Graphics2D;
import javax.swing.*;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.ImageIcon.*;
import java.awt.geom.*;

public class exp1 extends JApplet 
{
	public  void init()
	{
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
				}
				});
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */

	private  void createAndShowGUI() {
		MyPanel myPane = new MyPanel();
		myPane.setOpaque(true);
		setContentPane(myPane);
	}

	public  class MyPanel extends JPanel  implements ActionListener , MouseMotionListener 
	{
		/** Work Panel Variables ************************************************************************************************/
		double scale_x = 1 ;  // scaling of work pannel 
		double scale_y = 1 ;
	
		int stretch = 0;
		String content = new String("Selected ");
		int movestate = 0;
		int scalestate = 0;
		int copystate = 0;
		int work_x ;
		int work_y ;
		int stretching = 0;
		int X;
		int Y;
		int wire_button  = 0 ;// 0 not presed already , 1 -> already pressed 
		int img_button_pressed = -1 ;  
		int draw_work = 0 ; // if 1 -> draw the image on work 
		int previousnode = 0;	
	
		int[][] work_mat ;   // if -1 => no comp is there on mat .. if i the (i)th comp of node_comp is present
		int[][] work_mat2 ;   // if -1 => no comp is there on mat .. if i the (i)th comp of node_comp is present
		int[][] work_mat3 ;   // if -1 => no comp is there on mat .. if i the (i)th comp of node_comp is present
		int[][] end_points_mat ;   // 
		int[][] wire_mat ;   // if -1 => no comp is there on mat .. if i the (i)th comp of node_comp is present
		int[][] wire_points_mat ;   // 

		int work_img_width = 50;
		int work_img_height = 50;
		int work_panel_width = 800 ;
		int work_panel_height = 600;

		int node_drag = -1 ; // it rep the index of comp_node which is selected to be draged 
		int wire_drag = -1 ; // it rep the index of wire which is selected to be extented from its end 
		int wire_drag_end = 1 ; // from which end it should be draged 
		int[] comp_count = new int[20] ; //  comp_count[i] represents the count of ( comp"i".jpg ) component ..
		int total_comp = 0 ;
		node[] comp_node = new node[20];

		int total_wire = 0 ;
		line[] wire = new line[20];

		// Dialog Box -----------------------------------
		myDialog[] dialog = new myDialog[14]  ; //in this exp at max 6 comp can be used  (I assume that comp is used once )
		JFrame[] fr = new JFrame[14] ;
		String[] comp_str = {		// This will store what should at the Dialog Box for each component
			"This is shows which component is selected ." ,
			"PMOS ", "Ground Terminal " ," Wire ",  // 1, 2 , 3
			"INPUT " ," ", " " , // 4 , 5 ,6

			"NMOS", "Capacitor" ,"Vdd ",  // 7 , 8 , 9 
			"OUTPUT",  " " ,"Inductor ",  // 10, 11 , 12
			"This is CMOS Chip No:1" ,"This is CMOS Chip No:1"};
		String[] comp_str2 = {
			"Move " ,
			"Copy ",
			"Stretch "};
		//*************************************************************************************************************************************
		//Circuit Component values which need to be send to ngspice ***************************************************************************

		String Pmos_l = "50n";
		String Pmos_w = "100n";

		String Nmos_l = "50n";
		String Nmos_w = "100n";
		
		String Capacitance = "100";
		//*************************************************************************************************************************************
		boolean simulate_flag = false ;
		Image img[] = new Image[20] ;
		ImageIcon icon[] = new ImageIcon[20] ;

		ImageIcon icon_simulate ;
		ImageIcon icon_graph ;

		ImageIcon icon_movex1 ;
		ImageIcon icon_movex2 ;
		ImageIcon icon_movey1 ;
		ImageIcon icon_movey2 ;
		ImageIcon icon_mag ;
		ImageIcon icon_copy ;
		ImageIcon icon_copy1 ;
		ImageIcon icon_paste ;

		MediaTracker mt ;
		URL base ;

		public 	class node 
		{
			int node_x ;
			int node_y ;
			int img_no ;
			int width ;
			int height ;
			int matrix_number = 0;
			int virtual_w ;
			int virtual_h ;

			double angle ;
			int angle_count ; // 0-> 0 / 360 degree , 1 -> 90 degree , 2 -> 180 degree , 3 -> 270 degree

			boolean del ;

			// for connection with wire 
			int end_pointsX[] = new int[5];
			int end_pointsY[] = new int[5];
			int count_end_points = 0 ;

			public node (int x , int y ,int matrix_no, int no , int w , int h)
			{
				node_x = x ;
				node_y = y ;
				img_no = no ;
				del = false ;
				matrix_number = matrix_no;
				virtual_w = width = w ;
				virtual_h = height = h ;
				angle = 0 ;
				angle_count = 0 ;

				//make_end_points(no);
			}
			public void del ()
			{
				del = true ;
			}
			public void rotate(int index )
			{
//			 	remove_mat() ;// delete the previous value from work_mat
		//		angle = angle +  (java.lang.Math.PI/2);
				angle_count = (angle_count + 1 )% 4 ;
				angle = angle_count *  (java.lang.Math.PI/2);
				if ( (angle - 2* java.lang.Math.PI ) > .001 )
				{
					angle = 0 ;
					virtual_w = width ;
					virtual_h = height ;
				}
				if (angle_count == 0 ) // 90 degree
				{
					virtual_w = width;
					virtual_h = height  ;
				}
				else if (angle_count == 1 ) // 90 degree
				{
					virtual_w = -height;
					virtual_h = width  ;
				}
				else if (angle_count == 2 ) // 180 degree
				{
					virtual_w = -width ;
					virtual_h = -height ;
				}
				else if (angle_count == 3 ) // 270 degree
				{
					virtual_w = height ;
					virtual_h = -width ;
				}
//				update_mat(index,); // update the matrix value to work_mat // inde is the index of the node_comp matrix
			}
			public void update_mat(int index,int matrix_no) // update the matrix value to work_mat // index is the index of the node_comp matrix
			{
				int i , j ;
				System.out.println("index "+index+" matrixno "+matrix_no);
				for ( i = node_x ; i < node_x + width ; i++)
				{

						for ( j = node_y ; j < node_y + height  ; j++ )
						{
						
							if(matrix_no == 2)
								work_mat2[i][j] =  index ;   // update the matrix as the img is selected  
							else if(matrix_no == 3)
								work_mat3[i][j] =  index ;   // update the matrix as the img is selected  
							else if(matrix_no == 1)
								work_mat[i][j] =  index ;   // update the matrix as the img is selected  
//							System.out.println("work mat is "+work_mat[i][j]);
					//		System.out.println("index");
					//		System.out.println(index);
						}
				}
//				System.out.println("exited the update matrix");
				//make_end_points(img_no);
			}
			public void remove_mat(int matrix_no) // delete the previous value from work_mat
			{
				int i , j ;
					for ( i = node_x ; i < node_x + width ; i++)
					{
						for ( j = node_y ; j < node_y + height  ; j++ )
						{
							
							if(matrix_no == 2)
								work_mat2[i][j] =  -1 ;   // update the matrix as the img is selected  
							else if(matrix_no == 3)
								work_mat3[i][j] =  -1 ;   // update the matrix as the img is selected  
							else if(matrix_no == 1)
								work_mat[i][j] =  -1 ;   // update the matrix as the img is selected  
//							work_mat[i][j] =  -1 ;   // update the matrix as the img is selected  
						}
					}
				//remove_end_points();
			}
			public void remove_end_points()
			{
				for ( int k = 0 ; k < count_end_points ; k ++ )
				{
					for ( int i = end_pointsX[k] - 4 ; i < end_pointsX[k] +5 ; i ++ )
					{
						for ( int j = end_pointsY[k] - 4 ; j < end_pointsY[k] +5; j ++ )
						{
							end_points_mat[i][j] = -1;
						}
					}
					
				}
			}
		}
		public class line 
		{
			int x1 , y1 , x2 , y2 ; // end and start point of wire 
			int x[] = new int[20];
			int y[] = new int[20];
			int end_index ;
			boolean del ;
			public line (int a, int b , int c , int d)
			{
				x1 = a ;
				y1 = b ;
				x2 = c ;
				y2 = d ;
				del = false ;
				x[0] = a ; x[1] = c ;
				y[0] = b ; y[1] = d ;
				end_index = 1 ;
			}
			public void update2( int c , int d ) // update end point
			{
				x2 = c ;
				y2 = d ;
//				end_index++;
				x[end_index] = c;
				y[end_index] = d;
			}
			public void update1( int c , int d ) // update start point 
			{
				x[0] = x1 = c ;
				y[0] = y1 = d ;
			}
			public void update(int a , int b ) // update middle point 
			{
				end_index++;
				x[end_index] = a;
				y[end_index] = b ;
			}
			public void update_wire_mat(int index )
			{
				int i , j , tx1 , ty1 , tx2 , ty2 ; // local vriables 
				for ( int k = 0 ; k < end_index ; k ++)
				{ 
					tx1 = x[k] ;	tx2 = x[k + 1] ;
					ty1 = y[k] ;	ty2 = y[k + 1] ;
					for ( i = x1 ;  ;)
					{
						if ( tx2 >= tx1 && i >= tx2  ){break;}
						else if( tx2 <= tx1 && i <= tx2 ){break;}
						for ( j = ty1 - 4 ; j < ty1 + 5 ; j ++)
						{			
							wire_mat[i][j] =  index ;   // update the matrix as the img is selected  
						}

						if ( tx2 > tx1 ){i++;}else{i--;} 
					}
					for ( i = ty1 ;  ;)
					{
						if ( ty2 >= ty1 && i >= ty2  ){break;}
						else if( ty2 <= ty1 && i <= ty2 ){break;}
						for ( j = tx2 - 4 ; j < tx2 + 5 ; j ++)
						{
							wire_mat[j][i] =  index ;   // update the matrix as the img is selected  
						}
	
						if ( ty2 > ty1 ){i++;}else{i--;} 
					}
				}
				
			}
			public void update_mat (int index)
			{
				for ( int i = x1 - 4 ; i < x1 +5; i ++ )
				{
					for ( int j = y1 - 4 ; j < y1 + 5; j ++ )
					{
						wire_points_mat[i][j] = index;
					}
				}				
				for ( int i = x2 - 4 ; i < x2 +5; i ++ )
				{
					for ( int j = y2 - 4 ; j < y2 + 5; j ++ )
					{
						wire_points_mat[i][j] = index;
					}
				}
				update_wire_mat(index);
			}
			public void del()
			{
				update_mat(-1);
				del = true ;
			}
		}

		public class myDialog extends JDialog implements ActionListener 
		{
			JSpinner length ;
			JSpinner width;
			JSpinner capacitance;
			Container cp;
			JButton del ;
			JButton ok ;
			JButton rotate ;
			int del_flag = 0;
			int node_index ;
			public myDialog()
			{
				node_index = node_index;
			}
			public myDialog (JFrame fr , String comp, int node_no)
			{

				super (fr , "List of Options " , true ); // true to lock the main screen 
				node_index = node_no ;
				System.out.println(node_no ) ;

				cp = getContentPane();
				SpringLayout layout = new SpringLayout();
				cp.setLayout(layout);
				setSize(350 , 200);

				if ( comp_node[node_no].img_no == 8 ) // capacitor 
				{
					SpinnerModel capacitance_model =	new SpinnerNumberModel(100, //initial value
							0, //min
							1000, //max
							1);  //step
					//JLabel comp_name = new JLabel("<html><font size=4><b>"+comp+"</b></font></html>" );//,icon[icon_no],JLabel.CENTER);

					//layout.putConstraint(SpringLayout.WEST , comp_name , 50,   SpringLayout.WEST , cp );
					//layout.putConstraint(SpringLayout.NORTH , comp_name , 20,  SpringLayout.NORTH , cp);

					capacitance = new JSpinner(capacitance_model);
					//JLabel c = new JLabel("Select the Capacitance :");
					//JLabel c_unit = new JLabel("nanometer");

					del = new JButton("Delete Component");
					ok = new JButton("O.K");
					rotate = new JButton("Rotate");
					
					//layout.putConstraint(SpringLayout.WEST , c , 20,   SpringLayout.WEST , cp );
					//layout.putConstraint(SpringLayout.NORTH , c ,60,  SpringLayout.NORTH , cp);

					//layout.putConstraint(SpringLayout.WEST , capacitance , 20,   SpringLayout.EAST , c );
					//layout.putConstraint(SpringLayout.NORTH , capacitance , 60,  SpringLayout.NORTH , cp);

					//layout.putConstraint(SpringLayout.WEST , c_unit , 10,   SpringLayout.EAST , capacitance );
					//layout.putConstraint(SpringLayout.NORTH , c_unit , 60,  SpringLayout.NORTH , cp);
					
					layout.putConstraint(SpringLayout.WEST , del , 20,   SpringLayout.WEST , cp );
					layout.putConstraint(SpringLayout.NORTH , del , 100,  SpringLayout.NORTH , cp);
					
					layout.putConstraint(SpringLayout.WEST , rotate , 10,   SpringLayout.EAST , del);
					layout.putConstraint(SpringLayout.NORTH , rotate , 100,  SpringLayout.NORTH , cp);

					layout.putConstraint(SpringLayout.WEST , ok , 10,   SpringLayout.EAST , rotate );
					layout.putConstraint(SpringLayout.NORTH , ok , 100,  SpringLayout.NORTH , cp);
					

					//cp.add(comp_name);
					//cp.add(c);
					//cp.add(capacitance);
					//cp.add(c_unit);
					cp.add(del);
					cp.add(ok);
					cp.add(rotate);
					ok.addActionListener(this);
					del.addActionListener(this);
					rotate.addActionListener(this);
				}
				else if ( comp_node[node_no].img_no == 2 || comp_node[node_no].img_no == 9 ) // terminal
				{
					//JLabel comp_name = new JLabel("<html><font size=4><b>"+comp+"</b></font></html>" );//,icon[icon_no],JLabel.CENTER);

					//layout.putConstraint(SpringLayout.WEST , comp_name , 100,   SpringLayout.WEST , cp );
					//layout.putConstraint(SpringLayout.NORTH , comp_name , 20,  SpringLayout.NORTH , cp);

					del = new JButton("Delete Component");
					ok = new JButton("O.K");
					rotate = new JButton("Rotate");
					//layout.putConstraint(SpringLayout.WEST , del , 10,   SpringLayout.WEST , cp );
					//layout.putConstraint(SpringLayout.NORTH , del , 80,  SpringLayout.NORTH , cp);

					layout.putConstraint(SpringLayout.WEST , rotate , 10,   SpringLayout.EAST , del);
					//layout.putConstraint(SpringLayout.NORTH , rotate , 80,  SpringLayout.NORTH , cp);

					layout.putConstraint(SpringLayout.WEST , ok , 10,   SpringLayout.EAST , rotate );
					//layout.putConstraint(SpringLayout.NORTH , ok , 80,  SpringLayout.NORTH , cp);

					//cp.add(comp_name);
					cp.add(del);
					cp.add(ok);
					cp.add(rotate);
					ok.addActionListener(this);
					del.addActionListener(this);
					rotate.addActionListener(this);

				}
				else // NMOS chip 
				{
					SpinnerModel length_model =	new SpinnerNumberModel(50, //initial value
							0, //min
							1000, //max
							1);  //step
					SpinnerModel width_model =	new SpinnerNumberModel(100, //initial value
							0, //min
							1000, //max
							1);  //step

					//JLabel comp_name = new JLabel("<html><font size=4><b>"+comp+"</b></font></html>" );//,icon[icon_no],JLabel.CENTER);

					//	layout.putConstraint(SpringLayout.WEST , comp_name , 50,   SpringLayout.WEST , cp );
					//	layout.putConstraint(SpringLayout.NORTH , comp_name , 20,  SpringLayout.NORTH , cp);

					//	length = new JSpinner(length_model);
					//	width = new JSpinner(width_model);

					//	JLabel w = new JLabel("Select the Width :");
					//	JLabel w_unit = new JLabel("nanometer");
					del = new JButton("Delete Component");
					ok = new JButton("O.K");
					rotate = new JButton("Rotate");

					layout.putConstraint(SpringLayout.WEST , del , 20,   SpringLayout.WEST , cp );
					layout.putConstraint(SpringLayout.NORTH , del , 120,  SpringLayout.NORTH , cp);

					layout.putConstraint(SpringLayout.WEST , rotate , 10,   SpringLayout.EAST , del);
					layout.putConstraint(SpringLayout.NORTH , rotate , 120,  SpringLayout.NORTH , cp);

					layout.putConstraint(SpringLayout.WEST , ok , 10,   SpringLayout.EAST , rotate );
					layout.putConstraint(SpringLayout.NORTH , ok , 120,  SpringLayout.NORTH , cp);
					
					cp.add(del);
					cp.add(ok);
					cp.add(rotate);
					ok.addActionListener(this);
					del.addActionListener(this);
					rotate.addActionListener(this);
				}
				addWindowListener( new WA());

			}
			String get_length()
			{
				return length.getValue().toString()+"n";
			}
			String get_width()
			{
				return width.getValue().toString()+"n";
			}
			String get_capacitance()
			{
				return capacitance.getValue().toString();
			}
			public void delComp()
			{
					del_flag = 0;
//					System.out.println("delComp is pressed ");
					//	System.out.println(node_index);
//					System.out.println("node index is "+node_index);
					comp_node[node_index].del = true;
					comp_count[comp_node[node_index].img_no] -= 1; // for descrising the count to check no of each comp
					int i , j ;

					comp_node[node_index].remove_mat(comp_node[node_index].matrix_number);
					/*	for ( i = comp_node[node_index].node_x ; i < comp_node[node_index].node_x + work_img_height ; i ++ )
						{
						for ( j = comp_node[node_index].node_y ; j < comp_node[node_index].node_y + work_img_width ; j ++ )
						{
						work_mat[i][j] = -1 ;
						}
						}*/
					// updating values of comp in file -------------------------------
					setVisible(false);
					//					work_panel_repaint();
					workPanel.repaint();
			}
			public void actionPerformed(ActionEvent e )
			{
				if(e.getSource() == rotate )
				{
				}
				if(e.getSource() == ok )
				{
					comp_node[node_index].rotate(node_index);
					workPanel.repaint();
					//					System.out.println("Roate");
					//					System.out.println(comp_node[node_index].angle);
				}
				if(e.getSource() == del)
				{
					delComp();
				}
			}

			class WA extends WindowAdapter
			{
				public void windowClosing( WindowEvent ev)
				{
					setVisible(false);
				}
			}
		}
		public  class WorkPanel extends JPanel  implements  MouseMotionListener , MouseListener   
		{
			int width1[]={10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
			int width2[]={10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10};
			int pos_x[]={0,0};
			int pos_y[]={0,0};
			int c_x = 0;
			int c_y = 0;
			public WorkPanel()
			{
				Arrays.fill(comp_count , 0);

				//	JPanel Panel = new JPanel();
				//	Panel.setBackground(Color.white);
				//	Panel.setBorder(BorderFactory.createRaisedBevelBorder());
				addMouseMotionListener(this); // whole panel is made to detect 
				addMouseListener(this); // whole panel is made to detect 

				//	work_panel_width = Panel.getWidth() ;
				//	work_panel_height = Panel.getHeight() ;

				work_mat = new int[work_panel_width][work_panel_height];
				work_mat2 = new int[work_panel_width][work_panel_height];
				work_mat3 = new int[work_panel_width][work_panel_height];
				end_points_mat = new int[work_panel_width][work_panel_height];
				wire_mat = new int[work_panel_width][work_panel_height];
				wire_points_mat = new int[work_panel_width][work_panel_height];
				int i , j ;
				for ( i = 0 ; i < work_panel_width ; i++)
				{
					for ( j = 0 ; j < work_panel_height ; j++ )
					{
						work_mat[i][j] = -1 ;
						work_mat2[i][j] = -1 ;
						work_mat3[i][j] = -1 ;
						end_points_mat[i][j] = -1 ;
						wire_mat[i][j] = -1 ;
						wire_points_mat[i][j] = -1 ;
					}
				}

			}
	
			
			public void mouseMoved(MouseEvent me) 
			{ 
				work_x = me.getX();
				work_y = me.getY();
				X = work_x;
				Y = work_y;
//				System.out.println("x is "+work_x+"y is "+work_y);
				/*if ( img_button_pressed == 3 && wire_button == 1 ) 
				{
				//	System.out.println("in");
				//	System.out.println(total_wire);
					int x = (work_x % 10)>5 ? (work_x/20)*20+20 : (work_x/20)*20; // for making good 
					int y = (work_y % 10)>5 ? (work_y/20)*20+20 : (work_y/20)*20; // accurate wire point around end points 
					wire[total_wire-1 ].update2(x , y);
//					wire[total_wire-1 ].update2((work_x/20)*20 , (work_y/20)*20);
					repaint();
				}*/
			}
			public void mouseDragged(MouseEvent me)
			{
				int i , j;
				// Mouse co-ord
				work_x = me.getX();
				work_y = me.getY();
			
				int w_x = (int)work_x;		
				int w_y = (int)work_y;		
				
				// Block co-ord			
	
				int temp_x;
				int temp_y;
				
				temp_x = (int)comp_node[node_drag].node_x;	
				temp_y = (int)comp_node[node_drag].node_y;	
			
				// Deleting

				myDialog obj = new myDialog();
				obj.node_index = node_drag;
				obj.delComp();
				comp_node[node_drag].del = false ;	
				pos_x[0] = work_x;
				pos_y[0] = work_y;
				if(movestate != 0 && stretching == 0)
				{
					for ( i = work_x  ; i < work_x + comp_node[node_drag].width ; i++ ) 
					{
						for ( j = work_y  ; j < work_y + comp_node[node_drag].height ; j++ )
						{
							if(i >= work_panel_width || j >= work_panel_height)
							{
								return;
							}
						}	
					}
					if (node_drag != -1 )
					{
						comp_node[node_drag].remove_mat(comp_node[node_drag].matrix_number);
	
						comp_node[node_drag].node_x  = (work_x /10 )*10 ;
						comp_node[node_drag].node_y  = ( work_y /10)*10 ;
	
						comp_node[node_drag].update_mat(node_drag,comp_node[node_drag].matrix_number);
						repaint();
				//	System.out.println(node_drag ) ;
					}
				}
				//				if(node_drag == 1 || node_drag == 0)
				//				{
				if(movestate != 1 && stretching == 1)
				{
					// Below : Downward stretching
				//	System.out.println("mouse x "+work_x+"mouse y "+work_y+"block x "+temp_x+"block y "+temp_y+" w1 "+width1[node_drag]+"w2 "+width2[node_drag]);
					System.out.println("work_ x "+work_x+" work_y "+work_y);
				//	if(work_x < 720 && work_y < 570)
				//	{
					if((work_x > temp_x ) && (work_x < temp_x + width1[node_drag] )&& (work_y > temp_y))
					{
						if((-temp_y + work_y) > 0)
						{
							width2[node_drag] = (((-temp_y + work_y)/10) + 1)*10;
						}
						else	
						{
								width2[node_drag] -= -(((-temp_y + work_y)/10))*10;
						}
						//						System.out.println("w2 is "+width2[node_drag]);
						if(width2[node_drag] < 0)
						{
							width2[node_drag] = -width2[node_drag];
						}
						/*		if(((+temp_y - work_y)/10)*10 > 0)
								comp_node[node_drag].node_y = (int)comp_node[node_drag].node_y +((int)((+temp_y - work_y)/10) + 1)*10;
								else
								comp_node[node_drag].node_y = (int)comp_node[node_drag].node_y -((int)((+temp_y - work_y)/10) + 1)*10;*/		

						comp_node[node_drag].virtual_w = width1[node_drag];
						comp_node[node_drag].virtual_h = width2[node_drag];
						comp_node[node_drag].width = width1[node_drag];
						comp_node[node_drag].height = width2[node_drag];
						comp_node[node_drag].update_mat(node_drag,comp_node[node_drag].matrix_number);		
						repaint();

					}
					// Below : Code for side stretching
					else if((temp_x + width1[node_drag] -2 < work_x ) && (temp_y  < work_y && work_y < temp_y + width2[node_drag] ))
					{
						//		System.out.println("temp_y "+temp_y+" w2 "+width2[node_drag]+" work_y "+work_y);
						//			System.out.println("x mouse x block "+work_x+" "+temp_x);
						if((-temp_x + work_x) > 0)
							width1[node_drag] = (((-temp_x + work_x)/10) + 1)*10;
						else
						{
								width1[node_drag] -= -(((-temp_x + work_x)/10))*10;
						}
						//width1[node_drag] = (((work_x - temp_x)/10) + 1)*10;
						if(width1[node_drag] < 0)
							width1[node_drag] = -width1[node_drag];
						//System.out.println("width is before"+width1[0]);
						comp_node[node_drag].virtual_w = width1[node_drag];
						comp_node[node_drag].virtual_h = width2[node_drag];
						comp_node[node_drag].width = width1[node_drag];
						comp_node[node_drag].height = width2[node_drag];
						comp_node[node_drag].update_mat(node_drag,comp_node[node_drag].matrix_number);		
						repaint();
					}
					else if((temp_x +width1[node_drag]-1 < work_x) && (work_x < temp_x +4+width1[node_drag])&& temp_y < work_y)
					{
						if((-temp_x + work_x) > 0)
						{
							width1[node_drag] = (((-temp_x + work_x)/10) + 1)*10;
							width2[node_drag] = (( (work_x) - temp_x )/10 + 1)*10;
						}
						else
						{
								width1[node_drag] -= -(((-temp_x + work_x)/10))*10;
								width2[node_drag] -= -(((-temp_x + work_x)/10))*10;
						}
						System.out.println("width is before"+width1[node_drag]);
						comp_node[node_drag].virtual_w = width1[node_drag];
						comp_node[node_drag].virtual_h = width2[node_drag];
						comp_node[node_drag].width = width1[node_drag];
						comp_node[node_drag].height = width2[node_drag];
						System.out.println("debug before update");
						comp_node[node_drag].update_mat(node_drag,comp_node[node_drag].matrix_number);		
						System.out.println("debug after update");
						repaint();
					}
					//}
				}
			}
			public void mouseClicked(MouseEvent me) 
			{
				int i , j ;
//				System.out.println("Mouse Clicked");
				work_x = me.getX();
				work_y = me.getY();
				if(work_mat3[work_x][work_y] != -1)
				{
					node_drag = work_mat3[work_x][work_y];   // node is selected for drag
					System.out.println("work3 "+work_mat3[work_x][work_y]);
					System.out.println("node_drag "+node_drag);
				}
				else
				{
					if(work_mat2[work_x][work_y] != -1)
					{
						node_drag = work_mat2[work_x][work_y];   // node is selected for drag
						System.out.println("work2 "+work_mat2[work_x][work_y]);
					}
					else
					{
						if(work_mat[work_x][work_y] != -1)
						{
							node_drag = work_mat[work_x][work_y];   // node is selected for drag
//							System.out.println("work1 "+work_mat[work_x][work_y]);
						}
						//node_drag = -1;
					}
				}
				if(copystate == 1)
				{
					//Code to execute when the Copy Button is pressed.
					int m = 1;
					System.out.println("copy state entered "+node_drag);
					if(work_mat[work_x][work_y] != -1)
					{
						if(work_mat2[work_x][work_y] != -1)
						{
							m = 3;
						}
						else
							m = 2;	
					}
					// creating the node for printing each comp 
				///	System.out.println("Entered mouseC,copy");
					comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, comp_node[node_drag].img_no , 20 * 2 , 4 * 20);
					comp_node[total_comp].update_mat(total_comp,m);
					comp_node[total_comp].img_no = comp_node[node_drag].img_no;
					comp_count[node_drag]++ ;
					total_comp++;
					repaint();

				}
				if ( img_button_pressed == -1 ) // for selecting anything on work panel
				{
				/*		if (  temp1 == 1 || temp1 == 7 || temp1 == 8 || temp1 == 2 ||  temp1 == 9 )
						{
							if ( dialog[temp] == null )
							{
								fr[temp] = new JFrame(); // bec work_mat will store the index of that comp in mat
								dialog[temp] = new myDialog( fr[temp] ,comp_str[temp1] , temp);
							}
							dialog[temp].setVisible(true);
						}
						else if ( temp1 == 4 || temp1 == 10 )
						{
							JFrame in_output_f = new JFrame();
							int n = JOptionPane.showConfirmDialog( in_output_f, "Do u want to Delete this comp ?","in_out", JOptionPane.YES_NO_OPTION);
							if ( n == 0 )
							{
								//		System.out.println("Deletded ");
								comp_node[temp].del() ;
								comp_node[temp].update_mat(-1);
								comp_count[temp1] = 0 ;
								repaint();
							}
							else
							{
							}
						}
						else if (  temp != 0 )
						{
							JOptionPane.showMessageDialog(null, "You can't change value of this component !! :)");
						}	*/
				}

//			}
			else  // For adding comp 
			{
					int m = 1;
					if ( img_button_pressed != -1  )
					{
						draw_work = 1;
						//				System.out.println("draw_work set to 1 ");
					}
					if(work_mat[work_x][work_y] != -1)
					{
						if(work_mat2[work_x][work_y] != -1)
						{
							m = 3;
						}
						else
							m = 2;	
					}
					// creating the node for printing each comp 
					if ( img_button_pressed == 1|| img_button_pressed == 7) // cmos or nmos 
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 2 , 4 * 20);
					}
					else if ( img_button_pressed == 2) // ground  
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 2 , 2 * 20);
					}
					else if ( img_button_pressed == 8  ) // capicitor  
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 3, 2 * 20);
					}
					else if (  img_button_pressed == 9 ) // Vdd
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 2, 3 * 20);
					}
					else if (  img_button_pressed == 4 || img_button_pressed == 10 ) // Input , Output
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 2, 20);
					}
					else
					{
						comp_node[total_comp] = new node((work_x / 20)*20 , (work_y / 20 )*20 ,m, img_button_pressed , 20 * 3, 2 * 20);
					}


					// if the surrounding have some object -------- -30
					
				/*	for ( i = work_x -30; i < work_x + comp_node[total_comp].width +30; i++ ) 
					{
						for ( j = work_y -30 ; j < work_y + comp_node[total_comp].height+30 ; j++ )
						{
							if(i <= 20 || j <= 20||i >= work_panel_width || j >= work_panel_height || work_mat[i][j] != -1)
							{
								return;
							}
						}	
					}*/

					int t1 = comp_node[total_comp].height ;
					if ( comp_node[total_comp].width > t1 )
					{
						t1 = comp_node[total_comp].width ;
					}
					
					// for boundary check even if rotated (by adding heights bec that could be maxima )
					//--------------------------------------------------------------------
					// updating the matrix 
					comp_node[total_comp].update_mat(total_comp,m);
				/*	for ( i = work_x ; i < work_x + comp_node[total_comp].width ; i++ )
					{
						for ( j = work_y ; j < work_y + comp_node[total_comp].height ; j++ )
						{
							work_mat[i][j] = total_comp  ;   // update the matrix (as total comp is already increased)
						}

					}*/
					comp_count[img_button_pressed]++ ;
					total_comp++;

					//	System.out.println(work_x );
					//		for( i = 0 ; i < 12 ; i ++ )
					//		{	
					//			System.out.println(comp_count[i]);
					//		}

					repaint();
					img_button_pressed = -1 ;
					draw_work = 0 ;

					change_selected(0);
				}
			}

			public void mouseReleased(MouseEvent me) 
			{
				int i , j ;
				if ( node_drag != -1 )
				{
					comp_node[node_drag].virtual_w = width1[node_drag];
					comp_node[node_drag].virtual_h = width2[node_drag];
					comp_node[node_drag].width = width1[node_drag];
					comp_node[node_drag].height = width2[node_drag];
					comp_node[node_drag].update_mat(node_drag,comp_node[node_drag].matrix_number); // updating the matrix
					/*	for ( i = comp_node[node_drag].node_x ; i < comp_node[node_drag].node_x + comp_node[node_drag].width ; i++ )
						{
						for ( j = comp_node[node_drag].node_y ; j < comp_node[node_drag].node_y + comp_node[node_drag].height ; j++ )
						{
							work_mat[i][j] =  node_drag ;   // update the matrix 
						}
					}*/
				//	previousnode = node_drag;	
					//node_drag = -1;   // node is unseledted to drag
				}
				//	System.out.println("from Release : " + work_mat[work_x][work_y]); 
				//node_drag = work_mat[work_x][work_y];   // node is selected for drag

			}

			public void mouseEntered(MouseEvent me) 
			{
			}
			public void mouseExited(MouseEvent me) 
			{
			}

			public void mousePressed(MouseEvent me) 
			{
				int i , j ;
				work_x = me.getX();
				work_y = me.getY();
				if(work_mat3[work_x][work_y] != -1)
					node_drag = work_mat3[work_x][work_y];   // node is selected for drag
				else	
				{
					if(work_mat2[work_x][work_y] != -1)
						node_drag = work_mat2[work_x][work_y];   // node is selected for drag
					else
					{
						if ( work_mat[work_x][work_y] != -1 )
						{
							node_drag = work_mat[work_x][work_y];   // node is selected for drag

						//	comp_node[node_drag].remove_mat(comp_node[node_drag].matrix_number); 		   // update the matrix as the img is selected , so can be moved 


							/*	for ( i = comp_node[node_drag].node_x ; i < comp_node[node_drag].node_x + comp_node[node_drag].width ; i++ )
								{
								for ( j = comp_node[node_drag].node_y ; j < comp_node[node_drag].node_y + comp_node[node_drag].height ; j++ )
								{
								work_mat[i][j] =  -1 ;   // update the matrix as the img is selected , so can be moved 
								}
								}
								*/
							 
						}
					}
				}
			}


			public void paint(Graphics g) 
			{
				int i , j ;
				Graphics2D g2d = (Graphics2D)g;
				g2d.scale(scale_x , scale_y);
				//g2d.setColor(Color.white);
				//System.out.println("scale is "+scalestate);
				/*if(scalestate == 1)
				{
					g2d.drawLine(comp_node[node_drag].node_x,comp_node[node_drag].node_y,comp_node[node_drag].node_x + 30,comp_node[node_drag].node_y);
					System.out.println("x "+work_x+"y "+work_y);
					System.out.println("scale drawn");
				}
				g2d.setColor(Color.gray);*/
				/*double sx;
				double sy;
				sx = 2;
				sy = 2;
				g2d.scale(sx,sy);*/
				// background ----------------
				Stroke drawingStroke = new BasicStroke(6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10,1}, 1);
				g2d.setColor(Color.gray);
				g.fillRect(0,0,work_panel_width+500 , work_panel_height+500);
				g2d.setColor(Color.white);
//			        g2d.draw(lin);
				g2d.setStroke(drawingStroke);
				g2d.setColor(Color.white);
				g2d.setStroke(new BasicStroke(1));
				for ( i = 0 ; i < work_panel_width +400; i+=10)
				{
					for ( j = 0 ; j < work_panel_height+200 ; j+=10 )
					{
						g2d.drawOval(  i -1,j-1 , 0 , 0);
					}
				}


		//For Images --------------------------------
				for ( i = 0; i < total_comp ; i++ )
				{
					if ( comp_node[i].del != true )
					{
						comp_node[i].virtual_w = width1[i];
						comp_node[i].virtual_h = width2[i];
						if ( comp_node[i].img_no == 3)
						{
							if(pos_x[0] != 0)
							{
							draw_poly(g2d , comp_node[i].node_x  , comp_node[i].node_y , width1[i],width2[i] , comp_node[i].angle);
}
else
							draw_poly(g2d , comp_node[i].node_x  , comp_node[i].node_y ,10,10 , comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 1)
						{
							if(pos_x[0] != 0)
							{
									draw_active(g2d , comp_node[i].node_x, comp_node[i].node_y  , width1[i] ,width2[i], comp_node[i].angle);
							}
							else
								draw_active(g2d , comp_node[i].node_x, comp_node[i].node_y  , 10 ,10, comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 5)
						{
							if(pos_x[0] != 0)
							{
								draw_nselect(g2d , comp_node[i].node_x  , comp_node[i].node_y ,width1[i],width2[i], comp_node[i].angle);
							}
							else
								draw_nselect(g2d , comp_node[i].node_x  , comp_node[i].node_y , 10,10, comp_node[i].angle);

						}
						else if ( comp_node[i].img_no == 2)
						{
							if(pos_x[0] != 0)
							{
								draw_metal1(g2d , comp_node[i].node_x  , comp_node[i].node_y ,width1[i],width2[i], comp_node[i].angle);
							}
							else
								draw_metal1(g2d , comp_node[i].node_x  , comp_node[i].node_y , 10,10, comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 8)
						{
							if(pos_x[0] != 0)
							{
							draw_metal2(g2d , comp_node[i].node_x  , comp_node[i].node_y ,width1[i],width2[i] , comp_node[i].angle);
							}
							else
							draw_metal2(g2d , comp_node[i].node_x  , comp_node[i].node_y ,10,10, comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 9)
						{
							if(pos_x[0] == 0)
							{
								width1[0]=0;
								width2[0]=0;
							}
							if(pos_x[0] != 0)
							{
									draw_metalcontact(g2d , comp_node[i].node_x, comp_node[i].node_y  , width1[i] ,width2[i], comp_node[i].angle);
							}
							else
								draw_metalcontact(g2d , comp_node[i].node_x  , comp_node[i].node_y ,10,10 , comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 4 )
						{
							if(pos_x[0] != 0)
							{
								draw_nwell(g2d , comp_node[i].node_x  , comp_node[i].node_y , width1[i],width2[i] , comp_node[i].angle);
							}
							else
								draw_nwell(g2d , comp_node[i].node_x  , comp_node[i].node_y , 10,10 , comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 7 )
						{
							if(pos_x[0] != 0)
							{
							draw_contact(g2d , comp_node[i].node_x  , comp_node[i].node_y , width1[i],width2[i] , comp_node[i].angle);
}
							else
								draw_contact(g2d , comp_node[i].node_x  , comp_node[i].node_y ,10,10 , comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 6 )
						{
							if(pos_x[0] != 0)
							{
							draw_pselect(g2d , comp_node[i].node_x  , comp_node[i].node_y , width1[i],width2[i] , comp_node[i].angle);
							}
							else
								draw_pselect(g2d , comp_node[i].node_x  , comp_node[i].node_y , 10,10 , comp_node[i].angle);
						}
						else if ( comp_node[i].img_no == 10 )
						{
							if(pos_x[0] != 0)
							{
							draw_via(g2d , comp_node[i].node_x  , comp_node[i].node_y , width1[i],width2[i] , comp_node[i].angle);
							}
							else
								draw_via(g2d , comp_node[i].node_x  , comp_node[i].node_y , 10,10, comp_node[i].angle);
						}
						else 
						{
						g2d.drawImage(img[comp_node[i].img_no] , comp_node[i].node_x ,comp_node[i].node_y, work_img_width , work_img_height,  this);
						}
					}
				}

				// For Wires ------------------------------------
				/*g2d.setStroke(new BasicStroke(2));
				for ( i = 0 ; i < total_wire ; i++ )
				{
					if ( wire[i].del == false )
					{
						g2d.setColor(Color.green);
						for ( int k = 0 ; k < wire[i].end_index ; k ++ )
						{
							g2d.drawLine (wire[i].x[k] , wire[i].y[k] , wire[i].x[k+1] , wire[i].y[k] );
							g2d.drawLine (wire[i].x[k+1] , wire[i].y[k] , wire[i].x[k+1] , wire[i].y[k+1] );
						}
	
						g2d.setColor(Color.red);
						g2d.fillRect (wire[i].x1 -4  , wire[i].y1 -4 , 8 ,8);
						g2d.fillRect (wire[i].x2 - 4 , wire[i].y2 -4 , 8 ,8);
					}
				}*/
			}
			
			void draw_nselect(Graphics2D g , int x , int y , int width1,int width2 , double angle )
			{
				Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
				Line2D line = new Line2D.Double(20, 40, 100, 40);		
				g.rotate(angle , x , y);
				g.setColor(new Color(204,255,153,100));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
			void draw_active(Graphics2D g , int x , int y , int width1 ,int width2, double angle )
			{
				Stroke drawingStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
				Line2D line = new Line2D.Double(20, 40, 100, 40);		
				g.rotate(angle , x , y);
				g.setColor(new Color(0,255,0,128));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			//	setVisible(false);
			}
			void draw_contact(Graphics2D g , int x , int y , int width1 ,int width2, double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(0,0,0,128));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
			void draw_metal2(Graphics2D g , int x , int y , int width1,int width2 , double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(Color.gray);
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
			void draw_pselect(Graphics2D g , int x , int y , int width1,int width2 , double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(255,204,153,100));
				g.drawRect( x , y ,width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.setColor(Color.red);
				g.rotate(-angle , x , y);
			}
			void draw_nwell(Graphics2D g , int x , int y , int width1,int width2 , double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(255,255,255,100));
				g.drawRect( x , y ,width1 ,width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.setColor(Color.black);
				g.rotate(-angle , x , y);
			}
			void draw_via(Graphics2D g , int x , int y , int width1 ,int width2, double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(255,255,255,128));
				g.drawRect( x , y , width1, width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
			void draw_metalcontact(Graphics2D g , int x , int y , int width1,int width2 , double angle )
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(0,0,0,128));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
			void draw_poly(Graphics2D g , int x , int y , int width1 ,int width2, double angle)
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(255,0,0,108));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2);
				g.rotate(-angle , x , y);
			}
			void draw_metal1(Graphics2D g , int x , int y , int width1 ,int width2, double angle)
			{
					
				g.rotate(angle , x , y);
				g.setColor(new Color(0,0,255,88));
				g.drawRect( x , y , width1 , width2);
				g.fillRect( x, y ,width1 ,width2 );
				g.rotate(-angle , x , y);
			}
		}
/*		public class graph extends JPanel
		{
			String fileToRead = "outfile";

			StringBuffer strBuff;
			TextArea txtArea;
			String myline;
			JLabel l ;

			double[] time = new double[1000] ;
			double[] V_in = new double[1000] ;
			double[] V_out = new double[1000] ;
			int no_values = 0 ;
			//public  graph()
			public  graph ()
			{
			}
			public void make_graph()
			{
				fileToRead ="outfile";
				URL url = null;
				try
				{
					url = new URL(getCodeBase(), fileToRead);
				}
				catch(MalformedURLException e){
					System.out.println("I did't got the outfile to read :( :( So I am very said ");
				}
				String line;
				try{
					InputStream in = url.openStream();
					BufferedReader bf = new BufferedReader(new InputStreamReader(in));
					strBuff = new StringBuffer();
					myline = bf.readLine();
					while(!myline.equals("Values:"))
					{
						myline = bf.readLine();
					}
					int i = 0 ;
					while((line = bf.readLine()) != null){
						line = bf.readLine();
						line = bf.readLine();
						time[i] = Double.parseDouble(line);
						line = bf.readLine();
						line = bf.readLine();
						V_out[i] = Double.parseDouble(line);
						line = bf.readLine();
						line = bf.readLine();
						V_in[i] = Double.parseDouble(line);
						i++;
					}
					no_values = i ;

					repaint();

					//		System.out.println("Hi I am in the contrct func of the exp1_graph class :)");
				}
					catch(IOException e){
						e.printStackTrace();
					}


			}
				public void paint(Graphics g)
				{
					System.out.println("Hi I am in the gpaint func of the exp1_graph class :)");
					int i , j ;
					Graphics2D g2d = (Graphics2D)g ;
					g2d.setStroke(new BasicStroke(1));
					// back ground 
					g2d.setColor(new Color(204 , 255 , 255));
					g2d.fillRect(0,0,1000,1500);
					g2d.setColor(Color.lightGray);

					for ( i = 0 ; i < 1500 ; i += 20 )
					{
						for (j = 0 ; j < 1500 ; j +=5 )
						{
							g2d.fillOval(i , j , 1 , 2);
						}
					}
					for ( i = 0 ; i < 1500 ; i += 5 )
					{
						for (j = 0 ; j < 1500 ; j +=20 )
						{
							g2d.fillOval(i , j , 2 , 1);
						}
					}


				}

			}*/
/**==================================================================================================================
// All Panel Declarations ===========================================================================================
//==================================================================================================================**/
//		JPanel contentPane = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel () ;
			JButton simulate_button ;
			JButton graph_button ;
			
			JButton movex1_button ;
			JButton movex2_button ;
			JButton movey1_button ;
			JButton movey2_button ;
			JButton mag_button ;
			JButton copy_button ;
			JButton copy1_button ;
			JButton paste_button ;
		JSplitPane splitPane ; // devides center pane into left and right panel 
		JPanel rightPanel = new JPanel();// = new exp1_graph();
	//	graph waveRightPanel = new graph() ;// = new exp1_graph();
		//graph waveRightPanel ;//= new graph() ;// = new exp1_graph();

		JPanel leftPanel = new JPanel() ;
			JSplitPane leftSplitPane ;  // divides left Panel into ( tool Panel ) and (work panel )...
			JPanel toolPanel = new JPanel ();
				JPanel toolPanelUp ;
					JButton selected ;
				JPanel toolPanelDown ;
					JToolBar leftTool1 = new JToolBar(1);
					JToolBar rightTool1 = new JToolBar(1);
					//Array to store buttons
						JButton img_button1[] = new JButton[10] ;
						JButton option_button[] = new JButton[10] ;
					JToolBar leftTool2 = new JToolBar(1);
					JButton img_button2[] = new JButton[10] ;
					WorkPanel workPanel = new WorkPanel(); // above defines new class WorkPanel
					//myDialog ob = new myDialog();
				//	int v=ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
				//	int h=ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
				//	JScrollPane jsb=new JScrollPane(workpanel,v,h);
				//	contentPane.add(jsb);
//==================================================================================================================
//==================================================================================================================

		public  MyPanel()
		{	
			super(new BorderLayout());
			int i ;
//--------------------------------------------------------------------------------
//CREATIE AND SET UP THE CONTENT PAGE .===========================================
//--------------------------------------------------------------------------------

			try // geting base URL address of this applet 
			{
				base = getDocumentBase();
			}
			catch( Exception e) {}

//------------------------------------------------------------------------------------
// Setting Left Pannel Of (Main Center Panel)---------------------------------------------- 
			leftPanel.setLayout(new BorderLayout());
			leftPanel.setMinimumSize(new Dimension(900 , 1000)); // for fixing size

			leftSplitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , toolPanel , workPanel); // spliting left in tool & work
			leftSplitPane.setOneTouchExpandable(true); // this for one touch option 
			leftSplitPane.setDividerLocation(0.2);  
			leftPanel.add(leftSplitPane, BorderLayout.CENTER);

// setting work panel -----------------------------------
			//	System.out.println( leftSplitPane.getRightComponent().getSize());

			// setting tool panel --------------------------------------

			for ( i = 1 ; i <= 5 ; i ++ )
			{
				java.net.URL imgURL = getClass().getResource("images/comp" + i + ".gif");
				if (imgURL != null) 
				{
					icon[i] =  new ImageIcon(imgURL);
					img[i] =  getImage(imgURL);
				}
				else 
				{
					System.err.println("Couldn't find file: " );
					icon[i] =  null;
				}


				img_button1[i] = new JButton ( icon[i] );
				img_button1[i].setOpaque(true);
				img_button1[i].setMargin(new Insets (0,0,0,0));
				img_button1[i].addActionListener(this);
				img_button1[i].setBackground(Color.white);
				img_button1[i].setToolTipText(comp_str[i]);// setting name for hovering of mouse

				leftTool1.add(img_button1[i]);
			}
			int j = 0 ;
			for ( i = 0 ; i <= 4 ; i ++ )
			{
				j = 6 + i ; // for index setting 
				java.net.URL imgURL = getClass().getResource("images/comp" + j + ".gif");
				if (imgURL != null) 
				{
					icon[j] =  new ImageIcon(imgURL);
					img[j] =  getImage(imgURL);
				}
				else 
				{
					System.err.println("Couldn't find file: " );
					icon[j] =  null;
				}



				img_button2[i] = new JButton ( icon[j] );
				img_button2[i].setOpaque(true);
				img_button2[i].setMargin(new Insets (0,0,0,0));
				img_button2[i].addActionListener(this);
				img_button2[i].setBackground(Color.white);
				img_button2[i].setToolTipText(comp_str[j]); // setting name at hovering of mouse 

				leftTool2.add(img_button2[i]);
			}


			toolPanel.setLayout(new BorderLayout());


			//			MySelected toolPanelUp = new MySelected();
			toolPanelUp = new JPanel();
			toolPanelDown = new JPanel();

			URL selected_URL = getClass().getResource("images/comp" + 0 + ".gif");
			if (selected_URL != null) 
			{
				icon[0] =  new ImageIcon(selected_URL);
			}
			else 
			{
				System.err.println("Couldn't find file: " );
				icon[0] =  null;
			}
			selected = new JButton(icon[0]);

			selected.setBackground(Color.orange);
			selected.setToolTipText(comp_str[0]); // setting name at hovering of mouse 

			toolPanel.add(toolPanelUp , BorderLayout.CENTER);


			JPanel temp_p = new JPanel();
			temp_p.setLayout(new BorderLayout());

			JPanel temp_pp = new JPanel();
			JLabel temp = new JLabel("<html> <FONT color=white size=6 ><b>TOOL BAR<b/><font/><html/>");
			temp_pp.add(temp);
			temp_pp.setBackground(Color.gray);
			temp_pp.setBorder(BorderFactory.createRaisedBevelBorder( ));

			temp_p.add(temp_pp , BorderLayout.NORTH);
			temp_p.add(new JLabel("<html> <br/><br/><html/>") , BorderLayout.CENTER);


			toolPanel.add(temp_p , BorderLayout.NORTH);
			toolPanel.add(toolPanelDown , BorderLayout.SOUTH);
			selected.setBorder(BorderFactory.createTitledBorder(" SELECTED ICON "));
			toolPanelDown.setBorder(BorderFactory.createTitledBorder(" AVALIABLE ICONS "));


			toolPanelUp.setLayout(new BorderLayout());
			toolPanelUp.add(selected, BorderLayout.NORTH);
			toolPanelUp.add(new JLabel("<html> <br/><html/>"), BorderLayout.SOUTH);

			toolPanelDown.add(leftTool1);
			toolPanelDown.add(leftTool2);

			leftTool1.setFloatable(false);
			leftTool2.setFloatable(false);

			//leftPanel.setMaximumSize(new Dimension( 100, 1000)); // for fixing size 

//------------------------------------------------------------------------------------
// Setting (((Right Panel))) in center Panel ------------------------------------------------


/*			String fileToRead ="outfile";
			URL url2 = null;
			try{
				url2 = new URL(getCodeBase(), fileToRead);
			}
			catch(MalformedURLException e){
				System.out.println("I did't got the outfile to read :( :( So I am very said ");
			}
			graph rightPanel  = new graph(url2);

 */			
			add(rightPanel , BorderLayout.EAST);
			rightPanel.setBackground(Color.gray);

			JPanel headButton1 = new JPanel (new FlowLayout(FlowLayout.CENTER , 1000 , 20 )) ;
			JLabel heading1 = new JLabel (  "<html><FONT COLOR=WHITE SIZE=12 ><B>LIST OF OPTIONS</B></FONT></html>", JLabel.CENTER);
			heading1.setBorder(BorderFactory.createEtchedBorder( Color.black , Color.white));
			//			headButton.setBorder(BorderFactory.createLineBorder( Color.black));
			//			heading.setBorder(BorderFactory.createTitledBorder("."));

			rightPanel.setLayout(new BorderLayout());

			rightPanel.add(heading1 , BorderLayout.NORTH);
			rightPanel.add(headButton1 , BorderLayout.CENTER);
			java.net.URL imgURL4 = getClass().getResource("images/simulate1.png");
			if (imgURL4 != null) 
			{
				icon_movex1 =  new ImageIcon(imgURL4);
				icon_movex2 =  new ImageIcon(imgURL4);
				icon_movey1 =  new ImageIcon(imgURL4);
				icon_movey2 =  new ImageIcon(imgURL4);
				icon_copy =  new ImageIcon(imgURL4);
				icon_copy1 =  new ImageIcon(imgURL4);
				icon_paste =  new ImageIcon(imgURL4);
				icon_mag =  new ImageIcon(imgURL4);
			}
			else 
			{
				System.err.println("Couldn't find file: " );
				icon_movex1 =  null;
				icon_movex2 =  null;
				icon_movey1 =  null;
				icon_movey2 =  null;
				icon_copy =  null;
				icon_copy1 =  null;
				icon_paste =  null;
				icon_mag =  null;
			}
			movex1_button = new JButton(" DELETE " , icon_movex1 );
			icon_movex1.setImageObserver(movex1_button);


			//movey2_button = new JButton("UNSTRETCH" , icon_movey2 );
			//icon_movey2.setImageObserver(movey2_button);

			mag_button = new JButton (" MAGNIFY " , icon_mag);
			icon_mag.setImageObserver(mag_button);

			copy1_button = new JButton (" COPY " , icon_copy1);
			icon_copy1.setImageObserver(copy1_button);
			
			paste_button = new JButton ("SCALE" , icon_paste);
			icon_paste.setImageObserver(paste_button);
			
			movey1_button = new JButton(" STRETCH " , icon_movey1 );
			icon_movey1.setImageObserver(movey1_button);

			copy_button = new JButton (" MOVE " , icon_copy);
			icon_copy.setImageObserver(copy_button);
//			X = work_x;
//			Y = work_y;
			if(movestate == 1)
				headButton1.add(new JTextArea(content, 6, 10));
			movex2_button = new JButton("UNMAGNIFY" , icon_movex2 );
			icon_movex2.setImageObserver(movex2_button);

			headButton1.add(movex1_button );
//			headButton1.add(movex2_button );
			headButton1.add(movey1_button );
		//	headButton1.add(movey2_button );
			headButton1.add(copy_button );
			headButton1.add(copy1_button );
//			headButton1.add(mag_button );
			headButton1.add(paste_button );

			movex1_button.addActionListener(this);
//			movex2_button.addActionListener(this);
			movey1_button.addActionListener(this);
			copy_button.addActionListener(this);
			copy1_button.addActionListener(this);
//			mag_button.addActionListener(this);
			paste_button.addActionListener(this);

//			rightPanel.setLayout( new BorderLayout() );
			
//			JLabel wave_head = new JLabel("This is the wave simulation output");
//			JLabel wave_head = new JLabel ( "<html><FONT COLOR=white SIZE=6 ><B>SIMULATION OF CIRCUIT</B></FONT><br><br></html>", JLabel.CENTER);
//			wave_head.setBorder(BorderFactory.createRaisedBevelBorder( ));

	/*		rightPanel.setBackground(Color.white);
			for ( i = 1 ; i <= 3 ; i ++ )
			{
				java.net.URL imgURL = getClass().getResource("images/comp" + i + ".gif");
				if (imgURL != null) 
				{
					icon[i] =  new ImageIcon(imgURL);
					img[i] =  getImage(imgURL);
				}
				else 
				{
					System.err.println("Couldn't find file: " );
					icon[i] =  null;
				}

				option_button[i] = new JButton ( comp_str2[i-1] );
				option_button[i].setOpaque(true);
				option_button[i].setMargin(new Insets (0,0,0,0));
				option_button[i].addActionListener(this);
				option_button[i].setBackground(Color.gray);
				option_button[i].setToolTipText(comp_str2[i-1]);// setting name for hovering of mouse

			//	rightPanel.add(option_button[i],BorderLayout.CENTER);
				rightTool1.add(option_button[i]);
			}*/

//			rightPanel.add(waveRightPanel, BorderLayout.CENTER);
//			rightPanel.add(wave_head, BorderLayout.NORTH);
// 			waveRightPanel.setVisible(false);
			rightPanel.setBackground(Color.lightGray);



//			rightTool1.setFloatable(false);

			//rightPanel.addMouseMotionListener(); // whole panel is made to detect 

//---------------------------------------------------------------------------------
// Setting Center  Split ============================================================
			splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT , leftPanel , rightPanel);
			splitPane.setOneTouchExpandable(true); // this for one touch option 
			splitPane.setDividerLocation(0.2);  
			add(splitPane, BorderLayout.CENTER);
//-------------------------------------------------------------------------------------------
//Setting Top Panel ========================================================================== 

			add(topPanel , BorderLayout.NORTH);
			topPanel.setBackground(Color.gray);
			JPanel headButton = new JPanel (new FlowLayout(FlowLayout.CENTER , 100 , 10 )) ;
			JLabel heading = new JLabel (  "<html><FONT COLOR=WHITE SIZE=18 ><B>LAYOUT EDITOR </B></FONT></html>", JLabel.CENTER);
			heading.setBorder(BorderFactory.createEtchedBorder( Color.black , Color.white));
			//			headButton.setBorder(BorderFactory.createLineBorder( Color.black));
			//			heading.setBorder(BorderFactory.createTitledBorder("."));

			topPanel.setLayout(new BorderLayout());

			topPanel.add(heading , BorderLayout.CENTER);
//			topPanel.add(headButton , BorderLayout.SOUTH);
			java.net.URL imgURL = getClass().getResource("images/simulate1.png");
			java.net.URL imgURL2 = getClass().getResource("images/simulate1.png");
			if (imgURL != null && imgURL2 != null) 
			{
				icon_simulate =  new ImageIcon(imgURL);
				icon_graph =  new ImageIcon(imgURL2);
			}
			else 
			{
				System.err.println("Couldn't find file: " );
				icon_simulate =  null;
				icon_graph =  null;
			}
			//simulate_button = new JButton(" SIMULATE " , icon_simulate );
			//icon_simulate.setImageObserver(simulate_button);

			//graph_button = new JButton (" FULL GRAPH " , icon_graph);
			//icon_graph.setImageObserver(graph_button);
			//simulate_button.setToolTipText("For Simulation");

			//headButton.add(simulate_button );
			//headButton.add(graph_button );


			//simulate_button.addActionListener(this);
			//graph_button.addActionListener(this);
			//-------------------------------------------------------------------------------------------
			//Setting Bottom Panel ========================================================================== 
			JPanel bottom = new JPanel(new FlowLayout());
			add(bottom , BorderLayout.SOUTH);
			JLabel h = new JLabel (  "<html><FONT COLOR=WHITE SIZE=12 ><B>THIS IS VLSI EXPERIMENT</B></FONT></html>", JLabel.CENTER);
//			bottom.add(h );

			//================================================================================================
			setBorder(BorderFactory.createLineBorder( Color.black));
			//	setBorder(BorderFactory.createTitledBorder("HI"));

		}
		// To change the icon at the selected part ..................
		public void magnify()
		{
			scale_x += 0.5;
			scale_y += 0.5;
		}
		public void unmagnify()
		{
			scale_x -= 0.5;
			scale_y -= 0.5;
		}
		public boolean circuit_check()
		{
			int check_points_value[][] = new int[15][10] ; // each index will store corresponding  component for points of (comp point no -> index )
			int i = 0 , j , j1 = 0, k1 = 0 , j2 = 0 , k2 = 0 , l = 0 ;
			for (  i = 0 ; i < 15 ; i ++ )
			{
				for (  j = 0 ; j < 10 ; j ++ )
				{
					check_points_value[i][j] = -1 ;
				}
			}
			System.out.println("total_wire");
			System.out.println(total_wire);
			for (  i = 0 ; i < total_wire ; i ++ )
			{
				j1 = wire[i].x1;
				k1 = wire[i].y1;
				j2 = wire[i].x2;
				k2 = wire[i].y2;

				if (wire[i].del == false &&  end_points_mat[j1][k1] != -1 && end_points_mat[j2][k2] != -1 )
				{
					
					l = 0 ;
					while ( check_points_value[ end_points_mat[j1][k1] ][l] != -1 )
					{
						l++;
					}
					check_points_value[end_points_mat[j1][k1]][l] =  end_points_mat[j2][k2];

					l = 0 ;
					while ( check_points_value[ end_points_mat[j2][k2] ][l] != -1 )
					{
						l++;
					}
					check_points_value[end_points_mat[j2][k2]][l] =  end_points_mat[j1][k1];
				}

				
			}/*
			for ( int i = 0 ; i < work_panel_width ; i++ )
			{
				for ( int j = 0; j < work_panel_height ; j++ )
				{
					if ( end_points_mat[i][j] != -1 )
					{
						for ( int k = i  ; k < i + 2 ; k ++ )
						{
							for ( int l = j  ; l < j + 2 ; l ++ )
							{
								if ( wire_points_mat[k][l] != -1 )
								{
									check_points_value[end_points_mat[i][j]] = wire_points_mat[k][l] ;
									break;
								}
							}
						}
						
					}
				}
			}*/
			
			
			
			int temp ;
			for (  i = 0 ; i < 12 ; i ++ ) // for making aal connected point for each end_point in its array ..
			{	
				
				l = 0 ;
				while ( check_points_value[i][l] != -1 )
				{
					l++;
				}

				int r = 0 ;
				temp = check_points_value[i][r++];
				while ( temp != - 1 )
				{
					int k = 0 ;
					while ( check_points_value[temp][k] != - 1)
					{
						int flag = 0 ;
						for ( int m = 0 ; m < l ; m ++ )
						{
							if ( check_points_value[temp][k] == check_points_value[i][m] )
							{
								flag = 1 ;
								break ;
							}
						}
						if ( flag == 0 )
						{
							check_points_value[i][l++] = check_points_value[temp][k];
						}
						k++;
					}
					temp = check_points_value[i][r++]; // for each (element) value of i(th) element of array
				}
				
			}
			// checking 
			System.out.println("check_points_value[i]");
			for (  i = 0 ; i < 12 ; i ++ )
			{
				System.out.println(check_points_value[i][0]);
				l = 1 ;
				while ( check_points_value[i][l] != -1 )
				{
					System.out.println(check_points_value[i][l++]);
				}
			}
			// exp1 inverter circuit check 
			for (  i = 0 ; i < 12 ; i ++ )
			{
				if ( check_points_value[i][0] == -1 )
				{
					return false ; // if any end point of component is free then wrong circiut ...
				}
			}
			l = 0 ;
			while (  check_points_value[0][l] != -1 )
			{
				if ( check_points_value[0][l] != 9 && check_points_value[0][l] != 0)
				{
					return false ;
				}
				l++;
			}
			l = 0 ;
			while (  check_points_value[1][l] != -1 )
			{
				if ( check_points_value[1][l] != 3 &&  check_points_value[1][l] != 6 && check_points_value[1][l] != 7 && check_points_value[1][l] != 11 && check_points_value[1][l] != 1)
				{
					
					return false ;
				}
				l++;
			}

			l = 0 ;
			while (  check_points_value[4][l] != -1 )
			{
				if ( check_points_value[4][l] != 8 &&  check_points_value[4][l] != 6 && check_points_value[4][l] != 7 && check_points_value[4][l] != 4)
				{
					return false ;
				}
				l++;
			}

			l = 0 ;
			while (  check_points_value[2][l] != -1 )
			{
				if ( check_points_value[2][l] != 5 &&  check_points_value[2][l] != 10  &&  check_points_value[2][l] != 2  )
				{
					return false ;
				}
				l++;
			}
			return true ;
		}
		public void movecompx1 (int no)
		{
				comp_node[no].del = true;
						repaint();
		}
		public void movecompx2 (int no)
		{
			comp_node[no].node_x-=10;
		}
		public void movecompy1 (int no)
		{
			if(stretching == 0)
				stretching = 1;
			else 
				stretching = 0;
		}		
		public void change_selected (int no)
		{
			selected.setIcon(icon[no]);
		}
		public void scalefunc()
		{
			System.out.println("from scale function  "+work_x);
			JFrame frame = new JFrame("JFrame Source Demo");
			JTextField text = null;
			String s1 = Integer.toString(comp_node[node_drag].width);
			String s2 = Integer.toString(comp_node[node_drag].height);
			String s =s1+" "+s2;
			System.out.println("s is "+s);
			System.out.println("node_drag "+node_drag);
			text = new JTextField(s,10);
		// Add a window listner for close button
		frame.addWindowListener(new WindowAdapter() {

/*			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}	*/
		});
		// This is an empty content area in the frame
		JLabel jlbempty = new JLabel("");
		jlbempty.setPreferredSize(new Dimension(175, 100));
		frame.getContentPane().add(text, BorderLayout.CENTER);
		frame.pack();
				frame.setVisible(true);
		}
		public void actionPerformed(ActionEvent e )
		{
			// Place to write the calling function
			if(e.getSource() == paste_button )
			{
				if(scalestate == 0)
					scalestate = 1;
				else 
					scalestate = 0;
				scalefunc();
				System.out.println("scale is "+scalestate);
				repaint();
			}
			if(e.getSource() == copy1_button )
			{
				if(copystate == 0)
					copystate = 1;
				else 
					copystate = 0;
				System.out.println("copy is "+copystate);
			}
			if(e.getSource() == copy_button )
			{
			//	scale_x += 0.5;
			//	scale_y += 0.5;
//				img_button_pressed = node_drag ;	
//				change_selected(img_button_pressed);
				System.out.println("Before drag from copy button "+node_drag+"move "+movestate);
				content = "move";
//				System.out.println("content is "+content);
				if(movestate == 0)
					movestate = 1;
				else 
					movestate = 0;
				System.out.println("After drag from copy button "+node_drag+"move "+movestate);
				//repaint();
			}
			if(e.getSource() == movex1_button )
			{
				content = "delete";
				movecompx1(node_drag);
//				System.out.println("content is "+content);
				repaint();
			}
			if(e.getSource() == movex2_button )
			{
				unmagnify();
				content = "unmagnify";
//				System.out.println("content is "+content);
				repaint();
			}
			if(e.getSource() == movey1_button )
			{
				content = "stretch";
				movecompy1(node_drag);
//				System.out.println("content is "+content);
				repaint();
			}
			if(e.getSource() == mag_button )
			{
				content = "magnify";
//				System.out.println("content is "+content);
				magnify();
				repaint();
				//	System.out.println(node_index);
			}
			if(e.getSource() == option_button[3] )
			{
				//	System.out.println(node_index);
			}
			/*if(e.getSource() == simulate_button )
			{
				System.out.println("simulate_button");
				if ( circuit_check() )
				{
					System.out.println("Circuit Correct :)");
				}
			
				if ( Pmos_l != null && Pmos_w != null && Nmos_l != null && Nmos_w != null && Capacitance != null && circuit_check())
				{
					//		callJS();
					URL php_file =null ;
					URLConnection c =null;
					String encoded = "comp=" + URLEncoder.encode(Pmos_l+"_"+Pmos_w+"_"+Nmos_l+"_"+Nmos_w+"_"+Capacitance);
					URL CGIurl = null;
					try
					{
						php_file = new URL(getDocumentBase(),"exp1_simulate.php");
						System.out.println( php_file.toString());
					}
					catch(Exception mye )
					{
					}
					//				String theCGI = "http://localhost/VirtualLab/VLSI_VLab/exp1_out.php";

					try
					{
						//	CGIurl =  new URL (theCGI);//new URL(getDocumentBase(),"exp1_out.php");
						//	}
						//	catch(Exception eee)
						//	{}
						//	try
						//	{
						//	c = CGIurl.openConnection();
						c = php_file.openConnection();
						c.setDoOutput(true);
						c.setUseCaches(false);
						c.setRequestProperty("content-type","application/x-www-form-urlencoded");
						//	}
						//	catch(Exception me)
						//	{}
						//	try
						//	{
						DataOutputStream out = new DataOutputStream(c.getOutputStream());
						out.writeBytes(encoded);
						out.flush(); out.close();
	
						BufferedReader in =
							new BufferedReader(new InputStreamReader(c.getInputStream()));
	
						String aLine;
						while ((aLine = in.readLine()) != null) 
						{
							// data from the CGI
							System.out.println(aLine);
						}

					}	
					catch(Exception php_e )
					{
						System.out.println("Can't make connection");
					}
				waveRightPanel.make_graph() ;// Read file OUTFILE and draw the 
				waveRightPanel.repaint();
				waveRightPanel.setVisible(true);
				simulate_flag = true ;
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Circuit is not Complete , Please Complete it and press simulate again :)");
				}
			}
		

			else if(e.getSource() == graph_button )
			{
				System.out.println("graph_button");
				if ( simulate_flag == true )
				{
					try
					{
						//URL url = new URL("http://localhost/VirtualLab/VLSI_VLab/exp1_out.php?name=Shahsank");
						URL url = new URL(base ,"exp1_fullgraph.php?name=Shahsank");
						getAppletContext().showDocument(url , "Output Exp 1 ");
					}
					catch(Exception ee )
					{}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Circuit is not simulated , Please Simulate it and press Full Graph Button again");
				}
		
			}*/
			else if (e.getSource() == img_button1[1] )
			{
					img_button_pressed = 1 ;	
					change_selected(1);
				//	System.out.println("img_button1[1] clicked");
			}
			else if (e.getSource() == img_button1[2] )
			{
				img_button_pressed = 2 ;	
				change_selected(2);
				//	System.out.println("img_button1[2] clicked");
			}
			else if (e.getSource() == img_button1[3] ) // Wire :)
			{
				img_button_pressed = 3 ;	
				change_selected(3);
			}
			else if (e.getSource() == img_button1[4] ) // Input 
			{
					img_button_pressed = 4 ;	
					change_selected(4);
			//	JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
			//	img_button_pressed = 4 ;	
			//	change_selected(4);
			}
			else if (e.getSource() == img_button1[5] )
			{
			//	JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
				img_button_pressed = 5 ;	
				change_selected(5);
			}
			else if (e.getSource() == img_button2[0] )
			{
			//	JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
				img_button_pressed = 6 ;	
				change_selected(6);
			}
			else if (e.getSource() == img_button2[1] )
			{
				img_button_pressed = 7 ;	
				change_selected(7);
			}
			else if (e.getSource() == img_button2[2] )
			{
				img_button_pressed = 8 ;	
				change_selected(8);
			}
			else if (e.getSource() == img_button2[3] )
			{
				img_button_pressed = 9 ;	
				change_selected(9);
			}
			else if (e.getSource() == img_button2[4] )
			{
					img_button_pressed = 10 ;	
					change_selected(10);
			//	JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
			//	img_button_pressed = 10 ;	
			//	change_selected(10);
			}
			/*else if (e.getSource() == img_button2[5] )
			{
				JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
			//	img_button_pressed = 11 ;	
			//	change_selected(11);
			}*/
			/*else if (e.getSource() == img_button2[6] )
			{
				JOptionPane.showMessageDialog(null, "For this Exp your don't need this component !! :)");
			//	img_button_pressed = 12 ;	
			//	change_selected(12);
			}*/

		}
		public void mouseMoved(MouseEvent me) 
		{ 
			System.out.println("In Right Panel ");

		} 
		public void mouseDragged(MouseEvent e) {}

	}// MyPanel class extends JPanel Class Ends here
	void callJS()
	{
		/*
		   try {
		   JSObject window = JSObject.getWindow(this);
		   if ( window != null )
		   {
		//	System.out.println("hi :"+(String)window.getSlot(0));

		}

		//	String userName = "John Doe";
		String ans = (String)window.getMember("name");
		System.out.println("hi :"+ans+":hi");

		// set JavaScript variable
		//	window.setMember("userName", userName);
		}
		catch(JSException jse )
		{
		jse.printStackTrace();
		}*/

	}


}

