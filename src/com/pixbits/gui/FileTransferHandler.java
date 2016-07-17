package com.pixbits.gui;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.TransferHandler;

public class FileTransferHandler extends TransferHandler {
  @FunctionalInterface
  public static interface Listener
  {
    public void filesDropped(Path[] files);
  }
  
  private static final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;
  
  private final Listener listener;

  public FileTransferHandler(Listener listener)
  {
    this.listener = listener;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean importData(TransferHandler.TransferSupport support)
  {
    if (!canImport(support))
      return false;
    
    Transferable t = support.getTransferable();
    
    try
    {
      List<File> files = (List<File>)t.getTransferData(FILE_FLAVOR);
      support.setDropAction(LINK);

      Path[] paths = files.stream().map( f -> f.toPath() ).toArray(Path[]::new);
      listener.filesDropped(paths);
    } 
    catch (IOException e)
    {
      return false;
    } 
    catch (UnsupportedFlavorException e)
    {
      return false;
    }
    
    return true;
  }

  @Override
  public boolean canImport(TransferHandler.TransferSupport support)
  {
    if (!support.isDataFlavorSupported(FILE_FLAVOR))
      return false;
    
    return true;
  }
}