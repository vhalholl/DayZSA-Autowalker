package autowalker;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.Robot;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.*;

import org.jnativehook.*;
import org.jnativehook.keyboard.*;

import java.util.Properties;
import java.io.*;
import java.net.URISyntaxException;

public class GUI extends JFrame implements NativeKeyListener {

	
	private String GUITitle;
	private Image ApplicationImage;
	private String ApplicationImagePath;
	private int chosenKey;
	private JFrame GUI;
	private JPanel GUIPanel;
	private JButton keyButton;
	private JButton startButton;
	private JLabel keyLabel;
	private Boolean isRunning;
	private Robot robot;
	private JLabel creditLabel;
	private Properties settings;
	private File settingsFile;
	
	public GUI()
	{
		settings = new Properties();
		
		settingsFile = new File("settings.properties");
		
		try {
			if(settingsFile.exists() == false)
			{
				settingsFile.createNewFile();
				System.out.println("Settingsfile not found, it has been created.");
				
			}
		} catch (IOException e) {
			
			System.out.println(e.getMessage());
		}
		

			
		
		if(settings.getProperty("keycode") == null)
		{
			chosenKey = 144;
	    	settings = new Properties();
	    	settings.setProperty("keycode", Integer.toString(chosenKey));
	    	
	    	try
	    	{
	    		settings.store(new FileOutputStream(settingsFile), null);
	    	}
	    	catch(Exception ex)
	    	{
	    		System.out.println(ex.getMessage());
	    	}
		}
		else
		{
			chosenKey = Integer.parseInt(settings.getProperty("keycode"));
		}
		
		try
		{
			settings.load(new FileReader(settingsFile));
		}
		catch(Exception use)
		{
			System.out.println(use.getMessage());
		}

		
		isRunning = false;

		ApplicationImagePath = "/images/dayz.JPG";	
		ApplicationImage = new ImageIcon(getClass().getResource(ApplicationImagePath)).getImage();
		
		GUI = this;
			
		GUITitle = "DayZ SA - Auto walker";
		
		GUI.setTitle(GUITitle);
		GUI.setResizable(false);
		GUI.setIconImage(ApplicationImage);
		
		GUIPanel = new JPanel();
		GUIPanel.setSize(GUI.getSize());
		GUIPanel.setLayout(null);
		GUIPanel.setVisible(true);
		GUI.add(GUIPanel);


		createComponents();
		createTrayIcon();
		
		initializeGlobalScreen();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run()
			{
				GlobalScreen.unregisterNativeHook();
				System.out.println("Unregisted Native hook");
			}
		}));
		

		
	}	
	private void createTrayIcon()
	{
		SystemTray tray = SystemTray.getSystemTray();
		PopupMenu trayMenu = new PopupMenu();
		MenuItem trayMenuItem = new MenuItem("Exit");

		trayMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent)
			{
				System.exit(0);
			}
		});
		
		trayMenu.add(trayMenuItem);
		
		TrayIcon trayIcon = new TrayIcon(ApplicationImage, GUITitle, trayMenu);
		trayIcon.setImageAutoSize(true);
		
		if(SystemTray.isSupported())
		{
			try
			{
				tray.add(trayIcon);
			}
			catch(AWTException e)
			{
				System.out.println(e.getMessage());
			}

			trayIcon.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent)
				{
					GUI.setVisible(true);
				}
			});
			
			System.out.println("TrayIcon succesfully created.");
		}
		else
		{
			System.out.println("SystemTray not supported.");
		}		
	}
	private void createComponents()
	{
		keyLabel = new JLabel("Action key: ");
		keyLabel.setLocation(new Point(10, 25));
		keyLabel.setSize(100, 15);
		keyLabel.setVisible(true);
		GUIPanel.add(keyLabel);

		keyButton = new JButton("Key: " + NativeKeyEvent.getKeyText(chosenKey));
		keyButton.setLocation(new Point(120, 20));
		keyButton.setSize(150, 25);
		keyButton.setVisible(true);
		keyButton.setEnabled(false);
		GUIPanel.add(keyButton);
	
		
		keyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent)
			{
				keyButton.setText("Press a key...");
			}
		});
		
		keyButton.addKeyListener(new KeyListener() {
		    public void keyTyped(KeyEvent e) {
		        
		    }

		    public void keyPressed(KeyEvent e) {
		        
		    }

		    public void keyReleased(KeyEvent e) {

		    	chosenKey = e.getKeyCode();

		    	settings = new Properties();
		    	settings.setProperty("keycode", Integer.toString(chosenKey));
		    	
		    	try
		    	{
		    		settings.store(new FileOutputStream(settingsFile), null);
		    	}
		    	catch(Exception ex)
		    	{
		    		System.out.println(ex.getMessage());
		    	}

		    	
		        keyButton.setText("Key: " + NativeKeyEvent.getKeyText(chosenKey));
		        

		    }
		});
		
		startButton = new JButton("Deactivate");
		startButton.setLocation(new Point(10, 60));
		startButton.setSize(270, 25);
		startButton.setVisible(true);
		startButton.setBackground(Color.green);
		startButton.setForeground(Color.black);
		GUIPanel.add(startButton);
		
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent)
			{
				if(keyButton.isEnabled() == false)
				{
					startButton.setText("Activate");
					startButton.setBackground(Color.red);
					startButton.setForeground(Color.white);
					keyButton.setEnabled(true);
				}
				else
				{
					startButton.setText("Deactivate");
					startButton.setBackground(Color.green);
					startButton.setForeground(Color.black);
					keyButton.setEnabled(false);	
				
				}
					
			}
		});
		
		creditLabel = new JLabel("Developed by Feonx | mike@feonx.com");
		creditLabel.setLocation(new Point(50, 110));
		creditLabel.setFont(new Font(creditLabel.getName(), Font.PLAIN, 9));
		creditLabel.setSize(250, 15);
		creditLabel.setVisible(true);
		GUIPanel.add(creditLabel);
	}
	
	public interface User32 extends StdCallLibrary 
	{
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
		HWND GetForegroundWindow();  // add this
		int GetWindowTextA(PointerType hWnd, byte[] lpString, int nMaxCount);
	}
	
	private Boolean isDayzForeground()
	{
	    byte[] windowText = new byte[512];
	    
	    PointerType hwnd = User32.INSTANCE.GetForegroundWindow(); // then you can call it!
	    User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
	    
	    if(Native.toString(windowText).contains("DayZ"))
	    {
	    	return true;
	    }
	    
		return false;
	}
	
	private void initializeGlobalScreen()
	{
        try {
            GlobalScreen.registerNativeHook();
            System.out.println("Registerd native hook.");
	    }
	    catch (NativeHookException ex) {
	            System.err.println("There was a problem registering the native hook.");
	            System.err.println(ex.getMessage());
	
	            System.exit(1);
	    }
	    
	    //Construct the example object and initialze native hook.
	    GlobalScreen.getInstance().addNativeKeyListener(this);

	}
	
    public void nativeKeyPressed(NativeKeyEvent e) {
    		
    	if(e.getKeyCode() == chosenKey && keyButton.isEnabled() == false && isDayzForeground())
    	{
    		try
			{
				robot = new Robot();
			}
			catch(AWTException ex)
			{
				System.out.println(ex.getMessage());
			}
    		
    		if(isRunning)
    		{
    			isRunning = false;
    			System.out.println("Running set to false");
    			
    			robot.keyRelease(KeyEvent.VK_W);

    		}
    		else
    		{
    			isRunning = true;
    			System.out.println("Running set to true");
    			robot.keyPress(KeyEvent.VK_W);
    			robot.keyRelease(KeyEvent.VK_W);
    			robot.keyPress(KeyEvent.VK_W);

    		}

    	}
    }

	public void nativeKeyReleased(NativeKeyEvent e) {
		 
	}
		
	public void nativeKeyTyped(NativeKeyEvent e) {

	}
}

