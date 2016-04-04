package autowalker;

import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
//import java.io.PrintStream;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GUI
  extends JFrame
  implements NativeKeyListener
{
  private static final long serialVersionUID = 1L;
  private String GUITitle;
  private String GUIVersion;
  private Image ApplicationImage;
  private String ApplicationImagePath;
  private int chosenKey;
  private int chosenKey1;
  private int walkKey = 90;
  private int sprintKey = 44;
  private JFrame GUI;
  private JPanel GUIPanel;
  private JButton keyButton;
  private JButton keyButton1;
  private JButton startButton;
  private JLabel keyLabel;
  private Boolean isRunning;
  private Boolean isSprinting;
  private Robot robot;
  private JLabel creditLabel;
  private Properties settings;
  private File settingsFile;
  
  public GUI()
  {
    this.settings = new Properties();
    
    this.settingsFile = new File("autowalker.properties");
    try
    {
      if (!this.settingsFile.exists())
      {
        this.settingsFile.createNewFile();
        System.out
          .println("Settings file not found, it has been created.");
      }
    }
    catch (IOException e)
    {
      System.out.println(e.getMessage());
    }
    if (this.settings.getProperty("keycode") == null)
    {
      this.chosenKey = 96;
      this.chosenKey1 = 110;
      this.settings = new Properties();
      this.settings.setProperty("keycode", 
        Integer.toString(this.chosenKey));
      this.settings.setProperty("keycode1", 
        Integer.toString(this.chosenKey1));
      try
      {
        this.settings.store(new FileOutputStream(this.settingsFile), 
          null);
      }
      catch (Exception ex)
      {
        System.out.println(ex.getMessage());
      }
    }
    else
    {
      this.chosenKey = Integer.parseInt(this.settings
        .getProperty("keycode"));
      this.chosenKey1 = Integer.parseInt(this.settings
        .getProperty("keycode1"));
    }
    try
    {
      this.settings.load(new FileReader(this.settingsFile));
    }
    catch (Exception use)
    {
      System.out.println(use.getMessage());
    }
    this.isRunning = Boolean.valueOf(false);
    this.isSprinting = Boolean.valueOf(false);
    
    this.ApplicationImagePath = "/images/dayz.JPG";
    this.ApplicationImage = new ImageIcon(getClass().getResource(
      this.ApplicationImagePath)).getImage();
    
    this.GUI = this;
    
    this.GUIVersion = "0.1";
    this.GUITitle = "AutoWalker";
    
    this.GUI.setTitle(this.GUITitle);
    this.GUI.setResizable(false);
    this.GUI.setIconImage(this.ApplicationImage);
    
    this.GUIPanel = new JPanel();
    this.GUIPanel.setSize(this.GUI.getSize());
    this.GUIPanel.setLayout(null);
    this.GUIPanel.setVisible(true);
    this.GUI.add(this.GUIPanel);
    
    createComponents();
    createTrayIcon();
    
    initializeGlobalScreen();
    
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    {
      public void run()
      {
        GlobalScreen.unregisterNativeHook();
        System.out.println("Unregistered Native hook");
      }
    }));
  }
  
  private void createTrayIcon()
  {
    SystemTray tray = SystemTray.getSystemTray();
    PopupMenu trayMenu = new PopupMenu();
    MenuItem trayMenuItem = new MenuItem("Exit");
    
    trayMenuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        System.exit(0);
      }
    });
    trayMenu.add(trayMenuItem);
    
    TrayIcon trayIcon = new TrayIcon(this.ApplicationImage, this.GUITitle, 
      trayMenu);
    trayIcon.setImageAutoSize(true);
    if (SystemTray.isSupported())
    {
      try
      {
        tray.add(trayIcon);
      }
      catch (AWTException e)
      {
        System.out.println(e.getMessage());
      }
      trayIcon.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          GUI.this.GUI.setVisible(true);
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
    this.keyLabel = new JLabel("Walk/Run Auto: ");
    this.keyLabel.setLocation(new Point(10, 15));
    this.keyLabel.setSize(100, 15);
    this.keyLabel.setVisible(true);
    
    this.GUIPanel.add(this.keyLabel);
    
    this.keyButton = new JButton(NativeKeyEvent.getKeyText(this.chosenKey));
    this.keyButton.setLocation(new Point(120, 10));
    this.keyButton.setSize(200, 25);
    this.keyButton.setVisible(true);
    this.keyButton.setEnabled(false);
    
    this.GUIPanel.add(this.keyButton);
    
    this.keyLabel = new JLabel("Sprint Auto: ");
    this.keyLabel.setLocation(new Point(10, 45));
    this.keyLabel.setSize(100, 15);
    this.keyLabel.setVisible(true);
    
    this.GUIPanel.add(this.keyLabel);
    
    this.keyButton1 = new JButton(
      NativeKeyEvent.getKeyText(this.chosenKey1));
    this.keyButton1.setLocation(new Point(120, 40));
    this.keyButton1.setSize(200, 25);
    this.keyButton1.setVisible(true);
    this.keyButton1.setEnabled(false);
    
    this.GUIPanel.add(this.keyButton1);
    
    this.keyButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        GUI.this.keyButton.setText("Press a key...");
      }
    });
    this.keyButton.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e) {}
      
      public void keyPressed(KeyEvent e) {}
      
      public void keyReleased(KeyEvent e)
      {
        GUI.this.chosenKey = e.getKeyCode();
        System.out.println(GUI.this.chosenKey);
        GUI.this.settings = new Properties();
        GUI.this.settings.setProperty("keycode", 
          Integer.toString(GUI.this.chosenKey));
        try
        {
          GUI.this.settings.store(new FileOutputStream(
            GUI.this.settingsFile), null);
        }
        catch (Exception ex)
        {
          System.out.println(ex.getMessage());
        }
        GUI.this.keyButton.setText(
          NativeKeyEvent.getKeyText(GUI.this.chosenKey));
      }
    });
    this.keyButton1.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        GUI.this.keyButton1.setText("Press a key...");
      }
    });
    this.keyButton1.addKeyListener(new KeyListener()
    {
      public void keyTyped(KeyEvent e) {}
      
      public void keyPressed(KeyEvent e) {}
      
      public void keyReleased(KeyEvent e)
      {
        GUI.this.chosenKey1 = e.getKeyCode();
        System.out.println(GUI.this.chosenKey1);
        GUI.this.settings = new Properties();
        GUI.this.settings.setProperty("keycode1", 
          Integer.toString(GUI.this.chosenKey1));
        try
        {
          GUI.this.settings.store(new FileOutputStream(
            GUI.this.settingsFile), null);
        }
        catch (Exception ex)
        {
          System.out.println(ex.getMessage());
        }
        GUI.this.keyButton1.setText(
          NativeKeyEvent.getKeyText(GUI.this.chosenKey1));
      }
    });
    this.startButton = new JButton("Status: Actif");
    this.startButton.setLocation(new Point(30, 80));
    this.startButton.setSize(270, 25);
    this.startButton.setVisible(true);
    this.startButton.setToolTipText("Cliquez pour D�sactiver AutoWalker");
    this.startButton.setBackground(Color.green);
    this.startButton.setForeground(Color.black);
    
    this.GUIPanel.add(this.startButton);
    
    this.startButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        if (!GUI.this.keyButton.isEnabled())
        {
          GUI.this.startButton.setText("Status: Inactif");
          GUI.this.startButton
            .setToolTipText("Cliquez pour Activer AutoWalker");
          GUI.this.startButton.setBackground(Color.red);
          GUI.this.startButton.setForeground(Color.white);
          GUI.this.keyButton.setEnabled(true);
          GUI.this.keyButton
            .setToolTipText("Cliquez pour Affecter une touche � l'action");
        }
        else
        {
          GUI.this.startButton.setText("Status: Actif");
          GUI.this.startButton
            .setToolTipText("Cliquez pour D�sactiver et ConfigurerAutoWalker");
          GUI.this.startButton.setBackground(Color.green);
          GUI.this.startButton.setForeground(Color.black);
          GUI.this.keyButton.setEnabled(false);
        }
        if (!GUI.this.keyButton1.isEnabled())
        {
          GUI.this.startButton.setText("Status: Inactif");
          GUI.this.startButton
            .setToolTipText("Cliquez pour Activer AutoWalker");
          GUI.this.startButton.setBackground(Color.red);
          GUI.this.startButton.setForeground(Color.white);
          GUI.this.keyButton1.setEnabled(true);
          GUI.this.keyButton1
            .setToolTipText("Cliquez pour Affecter une touche � l'action");
        }
        else
        {
          GUI.this.startButton.setText("Status: Actif");
          GUI.this.startButton
            .setToolTipText("Cliquez pour D�sactiver et Configurer AutoWalker");
          GUI.this.startButton.setBackground(Color.green);
          GUI.this.startButton.setForeground(Color.black);
          GUI.this.keyButton1.setEnabled(false);
        }
      }
    });
    this.creditLabel = new JLabel("AutoWalker " + this.GUIVersion + 
      " pour DayZ");
    this.creditLabel.setLocation(new Point(100, 110));
    this.creditLabel.setFont(new Font(this.creditLabel.getName(), 1, 9));
    this.creditLabel.setSize(250, 15);
    this.creditLabel.setToolTipText("http://autowalker.vhalholl.info/");
    this.creditLabel.setVisible(true);
    
    this.GUIPanel.add(this.creditLabel);
  }
  
  public static abstract interface User32
    extends StdCallLibrary
  {
    public static final User32 INSTANCE = (User32)Native.loadLibrary(
      "user32", User32.class);
    
    public abstract WinDef.HWND GetForegroundWindow();
    
    public abstract int GetWindowTextA(PointerType paramPointerType, byte[] paramArrayOfByte, int paramInt);
  }
  
  private Boolean isDayzForeground()
  {
    byte[] windowText = new byte['?'];
    
    PointerType hwnd = User32.INSTANCE.GetForegroundWindow();
    User32.INSTANCE.GetWindowTextA(hwnd, windowText, 512);
    if (Native.toString(windowText).contains("DayZ")) {
      return Boolean.valueOf(true);
    }
    return Boolean.valueOf(false);
  }
  
  private void initializeGlobalScreen()
  {
    try
    {
      GlobalScreen.registerNativeHook();
      System.out.println("Registered native hooks.");
    }
    catch (NativeHookException ex)
    {
      System.err.println("There was a problem when registering the native hooks.");
      System.err.println(ex.getMessage());
      
      System.exit(1);
    }
    GlobalScreen.getInstance().addNativeKeyListener(this);
  }
  
  public void nativeKeyPressed(NativeKeyEvent e)
  {
    if (isDayzForeground().booleanValue()) {
      try
      {
        this.robot = new Robot();
        if ((e.getKeyCode() == this.chosenKey) && 
          (!this.keyButton.isEnabled())) {
          if (this.isRunning.booleanValue())
          {
            this.isRunning = Boolean.valueOf(false);
            System.out.println("Run/Walk set to false");
            this.robot.keyRelease(this.walkKey);
          }
          else
          {
            this.isRunning = Boolean.valueOf(true);
            System.out.println("Run/Walk set to true");
            this.robot.keyPress(this.walkKey);
          }
        }
        if ((e.getKeyCode() == this.chosenKey1) && 
          (!this.keyButton1.isEnabled())) {
          if (this.isSprinting.booleanValue())
          {
            this.isSprinting = Boolean.valueOf(false);
            System.out.println("Sprint set to false");
            
            this.robot.keyRelease(this.sprintKey);
          }
          else
          {
            this.isSprinting = Boolean.valueOf(true);
            System.out.println("Sprint set to true");
            
            this.robot.keyPress(this.sprintKey);
          }
        }
      }
      catch (AWTException ex)
      {
        System.out.println(ex.getMessage());
      }
    }
  }
  
  public void nativeKeyReleased(NativeKeyEvent e) {}
  
  public void nativeKeyTyped(NativeKeyEvent e) {}
}
