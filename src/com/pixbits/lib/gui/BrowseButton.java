package com.pixbits.lib.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.*;

import com.pixbits.lib.io.PathFileFilter;

public class BrowseButton extends JTextField
{
  private class PathFileFilter extends FileFilter
  {
    private final PathMatcher matcher;
    private final String description;

    public PathFileFilter(PathMatcher matcher, String description)
    {
      this.matcher = matcher;
      this.description = description;
    }
    
    @Override
    public boolean accept(File pathname)
    {      
      return (pathname.isDirectory() && type != Type.FILES) || matcher.matches(pathname.toPath().getFileName());
    }

    @Override
    public String getDescription()
    {
      return description;
    }
  }

  public static enum Type
  {
    FILES(JFileChooser.FILES_ONLY),
    DIRECTORIES(JFileChooser.DIRECTORIES_ONLY),
    FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES)
    ;
    final int fctype;

    private Type(int fctype) { this.fctype = fctype; }
  }
  
  private final JButton button;
  private final JFileChooser chooser;
  
  private Type type;
  private Path path;
  
  private Supplier<Path> getBasePath;
  private Consumer<Path> optionalCallback = p -> { };
  
  public BrowseButton(int width)
  {
    super(width);
    
    Font currentFont = this.getFont();
    this.setFont(currentFont.deriveFont(currentFont.getSize()*1.0f));

    button = new JButton("\u00B7\u00B7\u00B7");
    button.setMargin(new Insets(0,0,0,0));
    UIDefaults def = new UIDefaults();
    def.put("Button.contentMargins", new Insets(0,0,0,0));
    button.putClientProperty("Nimbus.Overrides", def);
    add(button);

    chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    setChooserType(Type.FILES_AND_DIRECTORIES);
    
    button.addActionListener(e -> dialogClosed(chooser.showOpenDialog(this)));
    this.setFocusable(false);
    
    getBasePath = () -> FileSystemView.getFileSystemView().getHomeDirectory().toPath();
  }
  
  @Override
  public void paintComponent(Graphics g) {
      g.setFont(button.getFont());
      Rectangle2D rect = g.getFontMetrics().getStringBounds(button.getText(), g);
      int stringWidth = (int)rect.getWidth();

      int topOffset = 3;
      int rightOffset = 3;
      int bottomOffset = 3;
      int width = (int)Math.ceil(stringWidth * 2.5);
      int height = getHeight() - (bottomOffset + topOffset);         
      int x_coord = getWidth() - (rightOffset + width);

      button.setMargin(new Insets(0, 0, 0, 0));
      button.setBounds(x_coord, topOffset, width, height);
      super.paintComponent(g);
  }

  public BrowseButton(int width, Type type)
  {
    this(width);
    setChooserType(type);
  }
  
  public void setChooserType(Type type)
  {
    this.type = type;
    chooser.setFileSelectionMode(type.fctype);
  }
  
  public void setFilter(PathMatcher matcher, String description)
  {
    chooser.setFileFilter(new PathFileFilter(matcher, description));
  }
  
  private void dialogClosed(int result)
  {
    if (result == JFileChooser.APPROVE_OPTION)
    {
      path = chooser.getSelectedFile().toPath().toAbsolutePath();
      this.setText(path.toString());
      optionalCallback.accept(path);
    }
  }
}
